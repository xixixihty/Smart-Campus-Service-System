package com.hxq.smart_campus.service.ai;

import com.hxq.smart_campus.entity.vo.BorrowRecordListVO;
import com.hxq.smart_campus.entity.vo.ReadingAnalysisVO;
import com.hxq.smart_campus.mapper.BorrowRecordMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ReadingAnalysisService {

    private final AiService aiService;
    private final BorrowRecordMapper borrowRecordMapper;

    public ReadingAnalysisVO analyzeReadingBehavior(Long studentId) {
        // 1. 基础数据
        List<BorrowRecordListVO> records = borrowRecordMapper.getBorrowRecordList(studentId, null, null);
        List<Map<String, Object>> categoryStats = borrowRecordMapper.getCategoryBorrowStatistics(studentId, null, null);
        List<Map<String, Object>> monthlyTrend = borrowRecordMapper.getBorrowTrend7Days(studentId);

        ReadingAnalysisVO analysis = new ReadingAnalysisVO();
        analysis.setTotalBorrows(records.size());

        // 本月借阅量
        YearMonth currentMonth = YearMonth.now();
        int monthCount = (int) records.stream()
                .filter(r -> r.getBorrowDate() != null)
                .filter(r -> YearMonth.from(r.getBorrowDate()).equals(currentMonth))
                .count();
        analysis.setMonthBorrows(monthCount);

        // 2. 无记录时直接返回
        if (records.isEmpty()) {
            analysis.setPreference("--");
            analysis.setAnalysis("您还没有借阅过图书，建议前往图书馆借阅感兴趣的书籍，开启阅读之旅！");
            return analysis;
        }

        // 3. 真实偏好分析（基于分类统计）
        if (!categoryStats.isEmpty()) {
            // 计算各分类占比
            long total = categoryStats.stream()
                    .mapToLong(m -> ((Number) m.get("borrowCount")).longValue())
                    .sum();
            String prefSummary = categoryStats.stream()
                    .limit(3)
                    .map(m -> {
                        String name = (String) m.get("categoryName");
                        long count = ((Number) m.get("borrowCount")).longValue();
                        double pct = total > 0 ? (double) count / total * 100 : 0;
                        return String.format("%s（%d本，%.0f%%）", name, count, pct);
                    })
                    .collect(Collectors.joining("、"));
            analysis.setPreference(prefSummary);

            // 取最多的分类作为 favCategory
            String topCategory = (String) categoryStats.get(0).get("categoryName");
            analysis.setFavCategory(topCategory != null ? topCategory : "综合阅读");
        } else {
            analysis.setPreference("综合阅读");
        }

        // 4. AI 个性化分析（改走用户端 ChatClient，获取学生真实数据）
        try {
            String categoryData = categoryStats.stream()
                    .map(m -> String.format("- **%s**：%s本", m.get("categoryName"), m.get("borrowCount")))
                    .collect(Collectors.joining("\n"));

            String trendData = monthlyTrend.isEmpty() ? "- 暂无趋势数据" : monthlyTrend.stream()
                    .map(m -> String.format("- %s：%s本", m.get("name"), m.get("value")))
                    .collect(Collectors.joining("\n"));

            long overdueCount = records.stream()
                    .filter(r -> "逾期".equals(r.getStatus()))
                    .count();
            long returnedCount = records.stream()
                    .filter(r -> "已归还".equals(r.getStatus()))
                    .count();

            String systemPrompt =
                    "你是一个专业的阅读分析顾问。根据学生的借阅统计数据，提供个性化分析报告。" +
                    "分析维度：\n" +
                    "1. **阅读偏好**：基于图书分类分布，判断学生的阅读兴趣领域\n" +
                    "2. **阅读活跃度**：基于借阅频率和月度趋势，给出活跃度评价\n" +
                    "3. **借阅健康度**：分析逾期、归还情况\n" +
                    "4. **个性化建议**：基于偏好推荐拓展阅读方向\n" +
                    "请使用Markdown格式输出，保持简洁但数据驱动。";

            String userMessage = String.format(
                    "我是一位学生，以下是我的阅读统计：\n\n" +
                    "**总借阅量**：%d本\n" +
                    "**本月借阅**：%d本\n" +
                    "**已归还**：%d本\n" +
                    "**逾期记录**：%d条\n\n" +
                    "**阅读偏好分布（TOP3）**：\n%s\n\n" +
                    "**近7天借阅趋势**：\n%s\n\n" +
                    "请分析我的阅读偏好并给出阅读建议。",
                    records.size(), monthCount, returnedCount, overdueCount,
                    categoryData, trendData);

            // 使用用户端非流式方法 + 独立 sessionId，不污染对话历史
            String sessionId = "reading_analysis_" + UUID.randomUUID().toString();
            String aiAnalysis = aiService.chatWithUserTools(systemPrompt, userMessage, sessionId, studentId);
            if (aiAnalysis != null && !aiAnalysis.isEmpty()) {
                analysis.setAnalysis(aiAnalysis);
            }
        } catch (Exception e) {
            log.warn("AI阅读分析调用失败，使用默认分析: studentId={}", studentId, e);
            analysis.setAnalysis(buildDefaultAnalysis(categoryStats, records));
        }

        return analysis;
    }

    /**
     * 默认分析文案（AI不可用时降级使用）
     */
    private String buildDefaultAnalysis(List<Map<String, Object>> categoryStats,
                                         List<BorrowRecordListVO> records) {
        if (categoryStats.isEmpty()) {
            return "暂无借阅数据，无法进行分析。";
        }
        String topCategory = (String) categoryStats.get(0).get("categoryName");
        long total = records.size();
        long overdueCount = records.stream().filter(r -> "逾期".equals(r.getStatus())).count();

        StringBuilder sb = new StringBuilder();
        sb.append("### 阅读分析报告\n\n");
        sb.append("**总借阅量**：").append(total).append("本  \n");
        sb.append("**最偏好分类**：").append(topCategory).append("  \n");

        if (overdueCount > 0) {
            sb.append("**逾期记录**：").append(overdueCount).append("条，建议注意归还日期，避免影响信用。  \n");
        }
        sb.append("\n建议多尝试其他类型的图书，拓展阅读视野。继续保持阅读习惯！");
        return sb.toString();
    }
}