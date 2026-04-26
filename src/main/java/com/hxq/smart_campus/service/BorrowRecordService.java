package com.hxq.smart_campus.service;

import com.github.pagehelper.PageInfo;
import com.hxq.smart_campus.entity.dto.BorrowCreateDTO;
import com.hxq.smart_campus.entity.dto.BorrowResponseDTO;
import com.hxq.smart_campus.entity.dto.BorrowStatisticsDTO;
import com.hxq.smart_campus.entity.vo.BorrowRecordDetailVO;
import com.hxq.smart_campus.entity.vo.BorrowRecordListVO;

import java.time.LocalDate;

public interface BorrowRecordService {
    /**
     * 新增借阅记录
     * @param borrowCreateDTO 借阅记录创建DTO
     * @return 借阅记录响应DTO
     */
    BorrowResponseDTO insertBorrowRecord(BorrowCreateDTO borrowCreateDTO);
    /**
     * 归还图书
     * @param id 借阅记录ID
     * @return 是否成功
     */
    Boolean returnBook(Long id);
    /**
     * 获取借阅记录列表
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @param userId 借阅人ID
     * @param bookId 图书ID
     * @param status 状态
     * @return 借阅记录列表分页信息
     */
    PageInfo<BorrowRecordListVO> getBorrowRecordList(Integer pageNum, Integer pageSize, Long userId, Long bookId, String status);
    /**
     * 获取借阅记录详情
     * @param id 借阅记录ID
     * @return 借阅记录详情VO
     */
    BorrowRecordDetailVO getBorrowRecordDetail(Long id);

    /**
     * 获取借阅记录列表
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @param status 状态
     * @return 分页信息
     */
    PageInfo<BorrowRecordListVO> getBorrowRecordMyList(Integer pageNum, Integer pageSize, String status, Long userId);
    /**
     * 获取借阅记录统计信息
     * @param userId 借阅人ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 借阅记录统计信息
     */
    BorrowStatisticsDTO getBorrowRecordStatistics(Long userId, LocalDate startDate, LocalDate endDate);
}
