package com.hxq.smart_campus.service.ai;

import com.hxq.smart_campus.entity.vo.*;
import com.hxq.smart_campus.mapper.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
@RequiredArgsConstructor
public class UserAITools {

    private final ScoreEntryMapper scoreEntryMapper;
    private final BorrowRecordMapper borrowRecordMapper;
    private final BookMapper bookMapper;
    private final CourseMapper courseMapper;
    private final SeatMapper seatMapper;
    private final SeatReservationMapper seatReservationMapper;

    @Tool(description = "查询学生的个人成绩列表。当用户询问成绩、分数、考试情况时调用此工具。")
    public String getMyScores(@ToolParam(description = "学生ID") Long studentId,
                              @ToolParam(description = "学期ID（可选）", required = false) Long semesterId) {
        log.info("Tool调用: getMyScores, studentId={}, semesterId={}", studentId, semesterId);
        List<ScoreEntryListVO> scores = scoreEntryMapper.getScoreList(null, studentId, semesterId);
        if (scores.isEmpty()) {
            return "暂无成绩记录。";
        }
        return scores.stream()
                .map(s -> String.format("课程：%s | 平时分：%s | 期末分：%s | 总分：%s | 学分：%s",
                        s.getCourseName(),
                        s.getUsualScore() != null ? s.getUsualScore() : "--",
                        s.getFinalScore() != null ? s.getFinalScore() : "--",
                        s.getTotalScore() != null ? s.getTotalScore() : "--",
                        s.getCredit() != null ? s.getCredit() : "--"))
                .collect(Collectors.joining("\n"));
    }

    @Tool(description = "查询学生的借阅历史记录。当用户询问借阅记录、读过什么书、借书情况时调用此工具。")
    public String getMyBorrowHistory(@ToolParam(description = "学生ID") Long studentId,
                                     @ToolParam(description = "返回条数限制（默认10）", required = false) Integer limit) {
        log.info("Tool调用: getMyBorrowHistory, studentId={}, limit={}", studentId, limit);
        List<BorrowRecordListVO> records = borrowRecordMapper.getBorrowRecordList(studentId, null, null);
        if (records.isEmpty()) {
            return "暂无借阅记录。";
        }
        int maxSize = limit != null ? Math.min(limit, records.size()) : Math.min(10, records.size());
        return records.stream()
                .limit(maxSize)
                .map(r -> String.format("书名：%s | 借阅日期：%s | 应还日期：%s | 状态：%s",
                        r.getBookTitle(),
                        r.getBorrowDate() != null ? r.getBorrowDate().toString() : "--",
                        r.getDueDate() != null ? r.getDueDate().toString() : "--",
                        r.getStatus() != null ? r.getStatus() : "--"))
                .collect(Collectors.joining("\n"));
    }

    @Tool(description = "搜索图书。当用户询问有什么书、找书、推荐图书时调用此工具。")
    public String searchBooks(@ToolParam(description = "搜索关键词（书名或作者）", required = false) String keyword,
                              @ToolParam(description = "分类ID（可选）", required = false) Integer categoryId) {
        log.info("Tool调用: searchBooks, keyword={}, categoryId={}", keyword, categoryId);
        List<BookListVO> books = bookMapper.getBookList(null, keyword, keyword, categoryId, null);
        if (books.isEmpty()) {
            return "未找到匹配的图书。";
        }
        int maxSize = Math.min(books.size(), 15);
        return books.stream()
                .limit(maxSize)
                .map(b -> String.format("书名：%s | 作者：%s | 分类：%s | 可借：%d | 状态：%s",
                        b.getTitle(),
                        b.getAuthor() != null ? b.getAuthor() : "--",
                        b.getCategoryName() != null ? b.getCategoryName() : "--",
                        b.getAvailableCopies() != null ? b.getAvailableCopies() : 0,
                        b.getStatus() != null ? b.getStatus() : "--"))
                .collect(Collectors.joining("\n"));
    }

    @Tool(description = "查询可选课程列表。当用户询问选课、有什么课可以选时调用此工具。")
    public String getAvailableCourses(@ToolParam(description = "学期ID", required = false) Long semesterId) {
        log.info("Tool调用: getAvailableCourses, semesterId={}", semesterId);
        List<AvailableCourseVO> courses = courseMapper.getAvailableCourseList(semesterId, null);
        if (courses.isEmpty()) {
            return "暂无可选课程。";
        }
        int maxSize = Math.min(courses.size(), 15);
        return courses.stream()
                .limit(maxSize)
                .map(c -> String.format("课程：%s | 类型：%s | 学分：%s | 容量：%d | 已选：%d",
                        c.getCourseName(),
                        c.getType() != null ? c.getType() : "--",
                        c.getCredit() != null ? c.getCredit() : "--",
                        c.getCapacity() != null ? c.getCapacity() : 0,
                        c.getSelectedCount() != null ? c.getSelectedCount() : 0))
                .collect(Collectors.joining("\n"));
    }

    @Tool(description = "查询座位可用情况。当用户询问座位、图书馆座位、预约座位时调用此工具。")
    public String getSeatAvailability(@ToolParam(description = "阅览室ID（可选）", required = false) Long roomId) {
        log.info("Tool调用: getSeatAvailability, roomId={}", roomId);
        List<SeatListVO> seats = seatMapper.getAvailableSeatList(null);
        if (seats == null || seats.isEmpty()) {
            return "暂无可预约的座位。";
        }
        if (roomId != null) {
            seats = seats.stream()
                    .filter(s -> s.getRoomId() != null && s.getRoomId().equals(roomId))
                    .collect(Collectors.toList());
            if (seats.isEmpty()) {
                return "该阅览室暂无可预约的座位。";
            }
        }
        long freeCount = seats.stream().filter(s -> "空闲".equals(s.getStatus())).count();
        long inUseCount = seats.stream().filter(s -> "使用中".equals(s.getStatus())).count();
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("可预约座位总数：%d（空闲：%d，使用中（可预约后续时段）：%d）\n", seats.size(), freeCount, inUseCount));
        int maxSize = Math.min(seats.size(), 15);
        seats.stream().limit(maxSize).forEach(s ->
                sb.append(String.format("座位号：%s | 阅览室ID：%s | 状态：%s\n",
                        s.getSeatNumber(), s.getRoomId(), s.getStatus())));
        return sb.toString();
    }

    @Tool(description = "查询我的座位预约记录。当用户询问我的预约、预约记录时调用此工具。")
    public String getMyReservations(@ToolParam(description = "学生ID") Long studentId) {
        log.info("Tool调用: getMyReservations, studentId={}", studentId);
        List<SeatReservationListVO> reservations = seatReservationMapper.getSeatReservationList(
                studentId, null, null, null);
        if (reservations.isEmpty()) {
            return "暂无座位预约记录。";
        }
        int maxSize = Math.min(reservations.size(), 10);
        return reservations.stream()
                .limit(maxSize)
                .map(r -> String.format("座位号：%s | 日期：%s | 时间：%s-%s | 状态：%s",
                        r.getSeatNumber(),
                        r.getDate() != null ? r.getDate().toString() : "--",
                        r.getStartTime() != null ? r.getStartTime().toString() : "--",
                        r.getEndTime() != null ? r.getEndTime().toString() : "--",
                        r.getStatus() != null ? r.getStatus() : "--"))
                .collect(Collectors.joining("\n"));
    }
}