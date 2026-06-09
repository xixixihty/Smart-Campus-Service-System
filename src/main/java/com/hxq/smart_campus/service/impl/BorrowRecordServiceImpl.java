package com.hxq.smart_campus.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.hxq.smart_campus.entity.dto.BorrowCreateDTO;
import com.hxq.smart_campus.entity.dto.BorrowMessage;
import com.hxq.smart_campus.entity.dto.BorrowResponseDTO;
import com.hxq.smart_campus.entity.dto.BorrowStatisticsDTO;
import com.hxq.smart_campus.entity.vo.BorrowRecordBase;
import com.hxq.smart_campus.entity.vo.BorrowRecordDetailVO;
import com.hxq.smart_campus.entity.vo.BorrowRecordListVO;
import com.hxq.smart_campus.exception.BusinessException;
import com.hxq.smart_campus.mapper.BookMapper;
import com.hxq.smart_campus.mapper.BorrowRecordMapper;
import com.hxq.smart_campus.service.BorrowRecordService;
import com.hxq.smart_campus.service.mq.BorrowRecordProducer;
import com.hxq.smart_campus.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RequiredArgsConstructor
@Service
@Slf4j
public class BorrowRecordServiceImpl implements BorrowRecordService {
    private final BorrowRecordMapper borrowRecordMapper;
    private final BookMapper bookMapper;
    private final RedisBorrowService redisBorrowService;
    @Lazy
    private final BorrowRecordProducer borrowRecordProducer;

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

        // 1. 布隆过滤器前置校验
        if (!redisBorrowService.checkBookExists(borrowCreateDTO.getBookId())) {
            throw new BusinessException("BOOK_NOT_EXIST", "图书不存在");
        }
        if (!redisBorrowService.checkUserExists(borrowCreateDTO.getUserId())) {
            throw new BusinessException("USER_NOT_EXIST", "用户不存在");
        }

        // 2. Lua脚本原子借阅（防重借 + 库存扣减）
        Long result = redisBorrowService.executeBorrow(borrowCreateDTO.getBookId(), borrowCreateDTO.getUserId());
        if (result == -2) {
            throw new BusinessException("ALREADY_BORROWED", "已借未还，不可重复借阅");
        } else if (result == -1) {
            throw new BusinessException("STOCK_EMPTY", "库存不足，无法借阅");
        } else if (result != 1) {
            throw new RuntimeException("借阅失败，请稍后再试");
        }

        // 3. 同步落库
        String borrowNo = generateBorrowNo();
        borrowRecordMapper.insertBorrowRecordDirect(borrowCreateDTO.getUserId(), borrowCreateDTO.getBookId(), borrowNo);
        bookMapper.decrementAvailableCopies(borrowCreateDTO.getBookId());

        // 4. 发送MQ消息（失败不影响主流程，仅用于通知等非关键路径）
        try {
            BorrowMessage message = new BorrowMessage(borrowCreateDTO.getBookId(), borrowCreateDTO.getUserId(), borrowNo, "BORROW");
            borrowRecordProducer.sendBorrowMessage(message);
        } catch (Exception e) {
            log.warn("发送借阅MQ消息失败: borrowNo={}", borrowNo, e);
        }

        BorrowResponseDTO response = new BorrowResponseDTO();
        response.setBorrowNo(borrowNo);
        response.setUserId(borrowCreateDTO.getUserId());
        response.setBookId(borrowCreateDTO.getBookId());
        response.setStatus("借出中");
        return response;
    }

    private String generateBorrowNo() {
        return "BOR" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean returnBook(Long id) {
        log.info("归还图书，参数：id={}", id);
        if (id == null) {
            throw new IllegalArgumentException("参数不能为空！");
        }
        // 查询借阅记录获取 bookId 和 userId
        BorrowRecordDetailVO record = borrowRecordMapper.getBorrowRecordDetail(id);
        if (record == null) {
            throw new IllegalArgumentException("借阅记录不存在！");
        }
        if ("已归还".equals(record.getStatus())) {
            throw new IllegalArgumentException("该图书已归还！");
        }

        // 1. Lua脚本原子归还（释放库存）
        Long luaResult = redisBorrowService.executeReturn(record.getBookId(), record.getUserId());
        if (luaResult == -1L) {
            throw new BusinessException("NOT_BORROWED", "未借阅该书");
        }

        // 2. 同步落库
        String borrowNo = record.getBorrowNo();
        if (borrowNo == null) {
            borrowNo = generateBorrowNo();
        }
        borrowRecordMapper.returnBookByBorrowNo(borrowNo);
        bookMapper.incrementAvailableCopies(record.getBookId());

        // 4. 发送MQ消息（失败不影响主流程，仅用于通知等非关键路径）
        try {
            BorrowMessage message = new BorrowMessage(record.getBookId(), record.getUserId(), borrowNo, "RETURN");
            borrowRecordProducer.sendReturnMessage(message);
        } catch (Exception e) {
            log.warn("发送归还MQ消息失败: borrowNo={}", borrowNo, e);
        }

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
    @Override
    public PageInfo<BorrowRecordListVO> getBorrowRecordMyList(Integer pageNum, Integer pageSize, String status, Long userId) {
        log.info("获取我的借阅记录，参数：pageNum={}, pageSize={}, status={}, userId={}", pageNum, pageSize, status, userId);
        PageHelper.startPage(pageNum, pageSize);
        Long queryUserId = userId != null ? userId : SecurityUtils.getCurrentUserId();
        List<BorrowRecordListVO> list = borrowRecordMapper.getBorrowRecordList(queryUserId, null, status);
        // 计算逾期天数
        list.forEach(this::calculateOverdueDays);
        return new PageInfo<>(list);
    }

    /**
     * 计算逾期天数
     * @param record 借阅记录VO
     */
    private void calculateOverdueDays(BorrowRecordBase record) {
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
        List<Map<String, Object>> trendStats = borrowRecordMapper.getBorrowTrend7Days(userId);

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

        List<BorrowStatisticsDTO.TrendDTO> trendList = new ArrayList<>();
        if (trendStats != null) {
            for (Map<String, Object> trendStat : trendStats) {
                BorrowStatisticsDTO.TrendDTO trendDTO = new BorrowStatisticsDTO.TrendDTO();
                trendDTO.setName((String) trendStat.get("name"));
                trendDTO.setValue(getIntValue(trendStat.get("value")));
                trendList.add(trendDTO);
            }
        }
        dto.setBorrowTrend7Days(trendList);

        log.info("获取借阅统计信息成功，总借阅：{}，当前借阅：{}，逾期：{}，平均借阅天数：{}",
                dto.getTotalBorrows(), dto.getCurrentBorrows(), dto.getOverdueBorrows(), dto.getAverageBorrowDays());
        return dto;
    }

    private Integer getIntValue(Object value) {
        if (value == null) {
            return 0;
        }
        if (value instanceof Integer) {
            return (Integer) value;
        }
        if (value instanceof Long) {
            return ((Long) value).intValue();
        }
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return 0;
    }

    private Double getDoubleValue(Object value) {
        if (value == null) {
            return 0.0;
        }
        if (value instanceof Double) {
            return (Double) value;
        }
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
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
