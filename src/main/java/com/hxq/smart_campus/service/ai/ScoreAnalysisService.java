package com.hxq.smart_campus.service.ai;

import com.hxq.smart_campus.entity.vo.ScoreAnalysisVO;
import com.hxq.smart_campus.entity.vo.ScoreEntryListVO;
import com.hxq.smart_campus.mapper.ScoreEntryMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.DoubleSummaryStatistics;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ScoreAnalysisService {

    private final AiService aiService;
    private final ScoreEntryMapper scoreEntryMapper;

    public ScoreAnalysisVO analyzeStudentScores(Long studentId) {
        List<ScoreEntryListVO> scores = scoreEntryMapper.getScoreList(null, studentId, null);
        
        ScoreAnalysisVO analysis = new ScoreAnalysisVO();
        
        if (scores.isEmpty()) {
            analysis.setAvgScore("--");
            analysis.setMaxScore("--");
            analysis.setMinScore("--");
            analysis.setGpa("--");
            analysis.setSuggestion("暂无成绩记录");
            return analysis;
        }

        DoubleSummaryStatistics stats = scores.stream()
                .filter(s -> s.getTotalScore() != null)
                .mapToDouble(s -> s.getTotalScore().doubleValue())
                .summaryStatistics();

        analysis.setAvgScore(String.format("%.2f", stats.getAverage()));
        analysis.setMaxScore(String.format("%.2f", stats.getMax()));
        analysis.setMinScore(String.format("%.2f", stats.getMin()));

        double totalCredit = scores.stream()
                .filter(s -> s.getCredit() != null)
                .mapToDouble(s -> s.getCredit().doubleValue())
                .sum();
        
        double weightedScore = scores.stream()
                .filter(s -> s.getTotalScore() != null && s.getCredit() != null)
                .mapToDouble(s -> s.getTotalScore().doubleValue() * s.getCredit().doubleValue())
                .sum();
        
        double gpa = totalCredit > 0 ? weightedScore / totalCredit / 100 * 4 : 0;
        analysis.setGpa(String.format("%.2f", gpa));

        analysis.setSuggestion("继续保持，你的学习表现良好！建议重点提升薄弱科目。");

        // 调用AI生成个性化分析建议
        try {
            String data = scores.stream()
                    .map(s -> String.format("课程：%s | 总分：%s | 学分：%s",
                            s.getCourseName(),
                            s.getTotalScore() != null ? s.getTotalScore() : "--",
                            s.getCredit() != null ? s.getCredit() : "--"))
                    .collect(java.util.stream.Collectors.joining("\n"));

            String systemPrompt = "你是一个学习分析专家。根据学生的成绩数据，分析其学习表现，"
                    + "包括优势科目、薄弱科目、学习建议等。请基于真实数据给出简洁、有针对性的分析。";

            String userMessage = String.format("我的成绩统计如下：\n平均分：%s | 最高分：%s | 最低分：%s | GPA：%s\n\n"
                    + "以下是各科成绩明细：\n%s\n\n请分析我的学习表现并给出改进建议。",
                    analysis.getAvgScore(), analysis.getMaxScore(), analysis.getMinScore(), analysis.getGpa(), data);

            String aiSuggestion = aiService.analyzeWithAdminTools(systemPrompt, userMessage);
            if (aiSuggestion != null && !aiSuggestion.isEmpty()) {
                analysis.setSuggestion(aiSuggestion);
            }
        } catch (Exception e) {
            log.warn("AI成绩分析调用失败，使用默认建议: studentId={}", studentId, e);
        }
        
        return analysis;
    }

    public String analyzeClassScores(Long classId, Long semesterId) {
        List<ScoreEntryListVO> scores = scoreEntryMapper.getScoreList(null, null, semesterId);
        if (scores.isEmpty()) {
            return "该班级暂无成绩数据。";
        }

        String data = scores.stream()
                .map(s -> String.format("学生：%s，课程：%s，成绩：%s",
                        s.getStudentName(), s.getCourseName(), s.getTotalScore()))
                .collect(java.util.stream.Collectors.joining("\n"));

        String systemPrompt = "你是一个教学数据分析专家。根据班级成绩数据，分析整体学习情况，"
                + "包括成绩分布、各课程表现差异、需要重点关注的学生等。";

        String userMessage = String.format("以下是班级成绩数据：\n%s\n\n请分析班级整体学习情况。", data);

        return aiService.analyzeWithAdminTools(systemPrompt, userMessage);
    }
}
