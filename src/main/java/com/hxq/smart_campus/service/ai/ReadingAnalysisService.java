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
        }
        
        return analysis;
    }
}
