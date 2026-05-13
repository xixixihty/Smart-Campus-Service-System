package com.hxq.smart_campus.service.ai;

import com.hxq.smart_campus.entity.vo.CourseListVO;
import com.hxq.smart_campus.entity.vo.ScoreEntryListVO;
import com.hxq.smart_campus.mapper.CourseMapper;
import com.hxq.smart_campus.mapper.ScoreEntryMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class CampusStatisticsService {

    private final AiService aiService;
    private final CourseMapper courseMapper;
    private final ScoreEntryMapper scoreEntryMapper;

    public String analyzeCampusOverview() {
        String systemPrompt = "你是一个校园数据统计分析专家。基于校园基础数据，生成校园数据统计分析报告。"
                + "分析维度：\n1. 师生规模与结构\n2. 课程开设情况\n"
                + "3. 教学资源概况\n4. 数据背后的趋势洞察与建议";

        String userMessage = "请基于智慧校园系统的整体数据，生成一份校园数据统计分析报告，描述当前校园运营概况。";

        return aiService.analyze(systemPrompt, userMessage);
    }

    public String analyzeTeachingQuality(Long courseId, Long semesterId) {
        List<ScoreEntryListVO> scores = scoreEntryMapper.getScoreList(courseId, null, semesterId);
        if (scores.isEmpty()) {
            return "该课程暂无成绩数据，无法评估教学质量。";
        }

        String data = scores.stream()
                .map(s -> String.format("学生：%s，成绩：%s", s.getStudentName(), s.getTotalScore()))
                .collect(Collectors.joining("\n"));

        String systemPrompt = "你是一个教学质量评估专家。基于课程成绩数据，分析教学质量。"
                + "分析维度：\n1. 成绩分布（优秀率、及格率等）\n"
                + "2. 教学效果评价\n3. 改进建议";

        String userMessage = String.format("以下是课程ID=%s的成绩数据：\n%s\n\n请进行教学质量评估。", courseId, data);

        return aiService.analyze(systemPrompt, userMessage);
    }

    public String analyzeResourceUtilization() {
        String systemPrompt = "你是一个校园资源优化专家。基于座位预约、图书借阅等数据，分析资源利用情况。"
                + "分析维度：\n1. 座位资源利用率\n2. 图书借阅活跃度\n"
                + "3. 资源使用高峰期识别\n4. 优化建议与改进方案";

        String userMessage = "请分析当前校园资源利用情况，给出优化建议。";

        return aiService.analyze(systemPrompt, userMessage);
    }
}
