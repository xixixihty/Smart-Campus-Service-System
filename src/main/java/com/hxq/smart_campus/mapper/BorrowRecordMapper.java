package com.hxq.smart_campus.mapper;

import com.hxq.smart_campus.entity.dto.BorrowCreateDTO;
import com.hxq.smart_campus.entity.vo.BorrowRecordDetailVO;
import com.hxq.smart_campus.entity.vo.BorrowRecordListVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Mapper
public interface BorrowRecordMapper {
    /**
     * 创建借阅记录
     * @param borrowCreateDTO
     * @return
     */
    int insertBorrowRecord(BorrowCreateDTO borrowCreateDTO);

    /**
     * 获取最近插入的借阅记录ID
     * @return
     */
    @Select("SELECT LAST_INSERT_ID()")
    Long getLastInsertId();
    /**
     * 获取借阅记录详情
     * @param id 借阅记录ID
     * @return 借阅记录详情VO
     */
    BorrowRecordDetailVO getBorrowRecordDetail(Long id);

    /**
     * 归还图书
     * @param id 借阅记录ID
     * @return
     */
    int returnBook(Long id);

    /**
     * 归还图书时更新图书状态（可用数量+1）
     * @param bookId 图书ID
     * @return
     */
    int updateBookAvailableOnReturn(@Param("bookId") Long bookId);

    /**
     * 获取借阅记录列表
     * @param userId
     * @param bookId
     * @param status
     * @return
     */
    List<BorrowRecordListVO> getBorrowRecordList(@Param("userId") Long userId,
                                                          @Param("bookId") Long bookId,
                                                          @Param("status") String status);

    /**
     * 获取借阅统计信息
     * @param userId 用户ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 统计信息Map
     */
    Map<String, Object> getBorrowStatistics(@Param("userId") Long userId,
                                              @Param("startDate") LocalDate startDate,
                                              @Param("endDate") LocalDate endDate);

    /**
     * 获取分类借阅统计
     * @param userId 用户ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 分类统计列表
     */
    List<Map<String, Object>> getCategoryBorrowStatistics(@Param("userId") Long userId,
                                                            @Param("startDate") LocalDate startDate,
                                                            @Param("endDate") LocalDate endDate);
}
