package com.hxq.smart_campus.service.ai;

import com.hxq.smart_campus.entity.vo.BorrowRecordListVO;
import com.hxq.smart_campus.entity.vo.ReadingAnalysisVO;
import com.hxq.smart_campus.mapper.BorrowRecordMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ReadingAnalysisService {

    private final AiService aiService;
    private final BorrowRecordMapper borrowRecordMapper;

    public ReadingAnalysisVO analyzeReadingBehavior(Long studentId) {
        List<BorrowRecordListVO> records = borrowRecordMapper.getBorrowRecordList(studentId, null, null);
        
        ReadingAnalysisVO analysis = new ReadingAnalysisVO();
        analysis.setTotalBorrows(records.size());
        
        YearMonth currentMonth = YearMonth.now();
        int monthCount = (int) records.stream()
                .filter(r -> r.getBorrowDate() != null)
                .filter(r -> {
                    LocalDate date = r.getBorrowDate();
                    return date != null && YearMonth.from(date).equals(currentMonth);
                })
                .count();
        analysis.setMonthBorrows(monthCount);
        
        if (records.isEmpty()) {
            analysis.setPreference("--");
            analysis.setAnalysis("暂无借阅记录");
        } else {
            analysis.setPreference("综合阅读");
            analysis.setAnalysis("您的阅读习惯良好，继续保持！");

            // 调用AI生成个性化阅读分析
            try {
                String data = records.stream()
                        .map(r -> String.format("《%s》| 借阅日期：%s",
                                r.getBookTitle() != null ? r.getBookTitle() : "--",
                                r.getBorrowDate() != null ? r.getBorrowDate().toString() : "--"))
                        .limit(20)
                        .collect(java.util.stream.Collectors.joining("\n"));

                String systemPrompt = "你是一个阅读分析专家。根据学生的借阅记录，分析其阅读偏好和习惯，"
                        + "给出阅读建议和推荐方向。请基于真实数据给出简洁、个性化的分析。";

                String userMessage = String.format("我的借阅记录如下（共%d本）：\n%s\n\n请分析我的阅读偏好并给出建议。",
                        records.size(), data);

                String aiAnalysis = aiService.analyzeWithAdminTools(systemPrompt, userMessage);
                if (aiAnalysis != null && !aiAnalysis.isEmpty()) {
                    analysis.setAnalysis(aiAnalysis);
                }
            } catch (Exception e) {
                log.warn("AI阅读分析调用失败，使用默认分析: studentId={}", studentId, e);
            }
        }
        
        return analysis;
    }
}
