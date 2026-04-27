package com.hxq.smart_campus.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.hxq.smart_campus.entity.dto.BorrowCreateDTO;
import com.hxq.smart_campus.entity.dto.BorrowResponseDTO;
import com.hxq.smart_campus.entity.dto.BorrowStatisticsDTO;
import com.hxq.smart_campus.entity.vo.BorrowRecordDetailVO;
import com.hxq.smart_campus.entity.vo.BorrowRecordListVO;
import com.hxq.smart_campus.mapper.BorrowRecordMapper;
import com.hxq.smart_campus.service.BorrowRecordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Service
@Slf4j
public class BorrowRecordServiceImpl implements BorrowRecordService {
    private final BorrowRecordMapper borrowRecordMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BorrowResponseDTO insertBorrowRecord(BorrowCreateDTO borrowCreateDTO) {
        log.info("创建借阅记录，参数：{}", borrowCreateDTO);
        if (borrowCreateDTO == null) {
            throw new IllegalArgumentException("参数不能为空！");
        }
        if (borrowCreateDTO.getUserId() == null) {
            throw new IllegalArgumentException("用户ID不能为空！");
        }
        if (borrowCreateDTO.getBookId() == null) {
            throw new IllegalArgumentException("图书ID不能为空！");
        }
        if (borrowCreateDTO.getDueDate() == null) {
            throw new IllegalArgumentException("应还日期不能为空！");
        }
        int result = borrowRecordMapper.insertBorrowRecord(borrowCreateDTO);
        if (result <= 0) {
            throw new RuntimeException("创建借阅记录失败！");
        }
        Long id = borrowRecordMapper.getLastInsertId();
        BorrowRecordDetailVO borrowRecordDetailVO = borrowRecordMapper.getBorrowRecordDetail(id);
        log.info("创建借阅记录成功，ID：{}", id);
        return convertToResponseDTO(borrowRecordDetailVO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean returnBook(Long id) {
        log.info("归还图书，参数：id={}", id);
        if (id == null) {
            throw new IllegalArgumentException("参数不能为空！");
        }
        BorrowRecordDetailVO record = borrowRecordMapper.getBorrowRecordDetail(id);
        if (record == null) {
            throw new IllegalArgumentException("借阅记录不存在！");
        }
        if ("已归还".equals(record.getStatus())) {
            throw new IllegalArgumentException("该图书已归还！");
        }
        int result = borrowRecordMapper.returnBook(id);
        if (result <= 0) {
            throw new RuntimeException("归还图书失败！");
        }
        borrowRecordMapper.updateBookAvailableOnReturn(record.getBookId());
        log.info("归还图书成功，ID：{}", id);
        return true;
    }

    @Override
    public PageInfo<BorrowRecordListVO> getBorrowRecordList(Integer pageNum, Integer pageSize, Long userId, Long bookId, String status) {
        log.info("获取借阅记录列表，参数：pageNum={}, pageSize={}, userId={}, bookId={}, status={}", pageNum, pageSize, userId, bookId, status);
        PageHelper.startPage(pageNum, pageSize);
        List<BorrowRecordListVO> list = borrowRecordMapper.getBorrowRecordList(userId, bookId, status);
        // 计算逾期天数
        list.forEach(this::calculateOverdueDays);
        return new PageInfo<>(list);
    }

    @Override
    public BorrowRecordDetailVO getBorrowRecordDetail(Long id) {
        log.info("获取借阅记录详情，参数：id={}", id);
        if (id == null) {
            throw new IllegalArgumentException("参数不能为空！");
        }
        BorrowRecordDetailVO record = borrowRecordMapper.getBorrowRecordDetail(id);
        if (record == null) {
            throw new IllegalArgumentException("借阅记录不存在！");
        }
        // 计算逾期天数
        calculateOverdueDays(record);
        return record;
    }
    // TODO： 获取我的借阅记录列表，从登录信息中获取用户ID
    @Override
    public PageInfo<BorrowRecordListVO> getBorrowRecordMyList(Integer pageNum, Integer pageSize, String status, Long userId) {
        log.info("获取我的借阅记录，参数：pageNum={}, pageSize={}, status={}, userId={}", pageNum, pageSize, status, userId);
        if (userId == null) {
            throw new IllegalArgumentException("用户ID不能为空！");
        }
        PageHelper.startPage(pageNum, pageSize);
        List<BorrowRecordListVO> list = borrowRecordMapper.getBorrowRecordList(userId, null, status);
        // 计算逾期天数
        list.forEach(this::calculateOverdueDays);
        return new PageInfo<>(list);
    }

    /**
     * 计算逾期天数
     * @param record 借阅记录列表VO
     */
    private void calculateOverdueDays(BorrowRecordListVO record) {
        if (record == null || record.getDueDate() == null) {
            return;
        }
        LocalDate today = LocalDate.now();
        int overdueDays = 0;
        if (today.isAfter(record.getDueDate())) {
            overdueDays = (int) ChronoUnit.DAYS.between(record.getDueDate(), today);
        }
        record.setOverdueDays(overdueDays);
    }

    /**
     * 计算逾期天数
     * @param record 借阅记录详情VO
     */
    private void calculateOverdueDays(BorrowRecordDetailVO record) {
        if (record == null || record.getDueDate() == null) {
            return;
        }
        LocalDate today = LocalDate.now();
        int overdueDays = 0;
        if (today.isAfter(record.getDueDate())) {
            overdueDays = (int) ChronoUnit.DAYS.between(record.getDueDate(), today);
        }
        record.setOverdueDays(overdueDays);
    }

    @Override
    public BorrowStatisticsDTO getBorrowRecordStatistics(Long userId, LocalDate startDate, LocalDate endDate) {
        log.info("获取借阅统计信息，参数：userId={}, startDate={}, endDate={}", userId, startDate, endDate);
        Map<String, Object> stats = borrowRecordMapper.getBorrowStatistics(userId, startDate, endDate);
        List<Map<String, Object>> categoryStats = borrowRecordMapper.getCategoryBorrowStatistics(userId, startDate, endDate);

        BorrowStatisticsDTO dto = new BorrowStatisticsDTO();
        dto.setTotalBorrows(getIntValue(stats.get("totalBorrows")));
        dto.setCurrentBorrows(getIntValue(stats.get("currentBorrows")));
        dto.setOverdueBorrows(getIntValue(stats.get("overdueBorrows")));
        dto.setAverageBorrowDays(getDoubleValue(stats.get("averageBorrowDays")));

        List<BorrowStatisticsDTO.CategoryBorrowDTO> categoryList = new ArrayList<>();
        if (categoryStats != null) {
            for (Map<String, Object> categoryStat : categoryStats) {
                BorrowStatisticsDTO.CategoryBorrowDTO categoryDTO = new BorrowStatisticsDTO.CategoryBorrowDTO();
                categoryDTO.setCategoryName((String) categoryStat.get("categoryName"));
                categoryDTO.setBorrowCount(getIntValue(categoryStat.get("borrowCount")));
                categoryList.add(categoryDTO);
            }
        }
        dto.setCategoryBorrows(categoryList);

        log.info("获取借阅统计信息成功，总借阅：{}，当前借阅：{}，逾期：{}，平均借阅天数：{}",
                dto.getTotalBorrows(), dto.getCurrentBorrows(), dto.getOverdueBorrows(), dto.getAverageBorrowDays());
        return dto;
    }

    private Integer getIntValue(Object value) {
        if (value == null) return 0;
        if (value instanceof Integer) return (Integer) value;
        if (value instanceof Long) return ((Long) value).intValue();
        if (value instanceof Number) return ((Number) value).intValue();
        return 0;
    }

    private Double getDoubleValue(Object value) {
        if (value == null) return 0.0;
        if (value instanceof Double) return (Double) value;
        if (value instanceof Number) return ((Number) value).doubleValue();
        return 0.0;
    }

    private BorrowResponseDTO convertToResponseDTO(BorrowRecordDetailVO borrowRecordDetailVO) {
        if (borrowRecordDetailVO == null) {
            return null;
        }
        BorrowResponseDTO borrowResponseDTO = new BorrowResponseDTO();
        borrowResponseDTO.setId(borrowRecordDetailVO.getId());
        borrowResponseDTO.setUserId(borrowRecordDetailVO.getUserId());
        borrowResponseDTO.setUserName(borrowRecordDetailVO.getUserName());
        borrowResponseDTO.setBookId(borrowRecordDetailVO.getBookId());
        borrowResponseDTO.setBookTitle(borrowRecordDetailVO.getBookTitle());
        borrowResponseDTO.setBookIsbn(borrowRecordDetailVO.getBookIsbn());
        borrowResponseDTO.setBorrowDate(borrowRecordDetailVO.getBorrowDate());
        borrowResponseDTO.setDueDate(borrowRecordDetailVO.getDueDate());
        borrowResponseDTO.setReturnDate(borrowRecordDetailVO.getReturnDate());
        borrowResponseDTO.setStatus(borrowRecordDetailVO.getStatus());
        borrowResponseDTO.setOverdueDays(borrowRecordDetailVO.getOverdueDays());
        return borrowResponseDTO;
    }
}
