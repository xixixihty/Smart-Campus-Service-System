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
            return MarkdownFormatter.noData("成绩");
        }
        String[] headers = {"课程", "平时分", "期末分", "总分", "学分"};
        List<String[]> rows = scores.stream()
                .map(s -> new String[]{
                        s.getCourseName(),
                        s.getUsualScore() != null ? String.valueOf(s.getUsualScore()) : "--",
                        s.getFinalScore() != null ? String.valueOf(s.getFinalScore()) : "--",
                        s.getTotalScore() != null ? String.valueOf(s.getTotalScore()) : "--",
                        s.getCredit() != null ? String.valueOf(s.getCredit()) : "--"
                })
                .collect(Collectors.toList());
        return MarkdownFormatter.header("我的成绩列表") + "\n\n" + MarkdownFormatter.toTable(headers, rows);
    }

    @Tool(description = "查询学生的借阅历史记录。当用户询问借阅记录、读过什么书、借书情况时调用此工具。")
    public String getMyBorrowHistory(@ToolParam(description = "学生ID") Long studentId,
                                     @ToolParam(description = "返回条数限制（默认10）", required = false) Integer limit) {
        log.info("Tool调用: getMyBorrowHistory, studentId={}, limit={}", studentId, limit);
        List<BorrowRecordListVO> records = borrowRecordMapper.getBorrowRecordList(studentId, null, null);
        if (records.isEmpty()) {
            return MarkdownFormatter.noData("借阅记录");
        }
        int maxSize = limit != null ? Math.min(limit, records.size()) : Math.min(10, records.size());
        String[] headers = {"书名", "借阅日期", "应还日期", "状态"};
        List<String[]> rows = records.stream()
                .limit(maxSize)
                .map(r -> new String[]{
                        r.getBookTitle(),
                        r.getBorrowDate() != null ? r.getBorrowDate().toString() : "--",
                        r.getDueDate() != null ? r.getDueDate().toString() : "--",
                        r.getStatus() != null ? r.getStatus() : "--"
                })
                .collect(Collectors.toList());
        return MarkdownFormatter.header("我的借阅记录（共" + records.size() + "条）") + "\n\n"
                + MarkdownFormatter.toTable(headers, rows);
    }

    @Tool(description = "搜索图书。当用户询问有什么书、找书、推荐图书时调用此工具。")
    public String searchBooks(@ToolParam(description = "搜索关键词（书名或作者）", required = false) String keyword,
                              @ToolParam(description = "分类ID（可选）", required = false) Integer categoryId) {
        log.info("Tool调用: searchBooks, keyword={}, categoryId={}", keyword, categoryId);
        List<BookListVO> books = bookMapper.getBookList(null, keyword, keyword, categoryId, null);
        if (books.isEmpty()) {
            return MarkdownFormatter.noData("图书");
        }
        int maxSize = Math.min(books.size(), 15);
        String[] headers = {"书名", "作者", "分类", "可借", "状态"};
        List<String[]> rows = books.stream()
                .limit(maxSize)
                .map(b -> new String[]{
                        b.getTitle(),
                        b.getAuthor() != null ? b.getAuthor() : "--",
                        b.getCategoryName() != null ? b.getCategoryName() : "--",
                        String.valueOf(b.getAvailableCopies() != null ? b.getAvailableCopies() : 0),
                        b.getStatus() != null ? b.getStatus() : "--"
                })
                .collect(Collectors.toList());
        String result = MarkdownFormatter.header("图书搜索结果") + "\n\n" + MarkdownFormatter.toTable(headers, rows);
        if (books.size() > maxSize) {
            result += "\n\n> 共 " + books.size() + " 条结果，仅显示前 " + maxSize + " 条。";
        }
        return result;
    }

    @Tool(description = "查询可选课程列表。当用户询问选课、有什么课可以选时调用此工具。")
    public String getAvailableCourses(@ToolParam(description = "学期ID", required = false) Long semesterId,
                                      @ToolParam(description = "学生ID（当前登录用户ID）", required = false) Long studentId) {
        log.info("Tool调用: getAvailableCourses, semesterId={}, studentId={}", semesterId, studentId);
        List<AvailableCourseVO> courses = courseMapper.getAvailableCourseList(semesterId, studentId);
        if (courses.isEmpty()) {
            return MarkdownFormatter.noData("可选课程");
        }
        int maxSize = Math.min(courses.size(), 15);
        String[] headers = {"课程名称", "类型", "学分", "容量", "已选"};
        List<String[]> rows = courses.stream()
                .limit(maxSize)
                .map(c -> new String[]{
                        c.getCourseName(),
                        c.getType() != null ? c.getType() : "--",
                        c.getCredit() != null ? String.valueOf(c.getCredit()) : "--",
                        String.valueOf(c.getCapacity() != null ? c.getCapacity() : 0),
                        String.valueOf(c.getSelectedCount() != null ? c.getSelectedCount() : 0)
                })
                .collect(Collectors.toList());
        return MarkdownFormatter.header("可选课程列表") + "\n\n" + MarkdownFormatter.toTable(headers, rows);
    }

    @Tool(description = "查询座位可用情况。当用户询问座位、图书馆座位、预约座位时调用此工具。")
    public String getSeatAvailability(@ToolParam(description = "阅览室ID（可选）", required = false) Long roomId) {
        log.info("Tool调用: getSeatAvailability, roomId={}", roomId);
        List<SeatListVO> seats = seatMapper.getAvailableSeatList(null);
        if (seats == null || seats.isEmpty()) {
            return MarkdownFormatter.noData("座位");
        }
        if (roomId != null) {
            seats = seats.stream()
                    .filter(s -> s.getRoomId() != null && s.getRoomId().equals(roomId))
                    .collect(Collectors.toList());
            if (seats.isEmpty()) {
                return "> 该阅览室暂无可预约的座位。";
            }
        }
        long freeCount = seats.stream().filter(s -> "空闲".equals(s.getStatus())).count();
        long inUseCount = seats.stream().filter(s -> "使用中".equals(s.getStatus())).count();

        StringBuilder sb = new StringBuilder();
        sb.append(MarkdownFormatter.header("座位可用情况")).append("\n\n");
        sb.append(MarkdownFormatter.toKeyValueList(
                "可预约总数", String.valueOf(seats.size()),
                "空闲", String.valueOf(freeCount),
                "使用中（可预约后续时段）", String.valueOf(inUseCount))).append("\n\n");

        int maxSize = Math.min(seats.size(), 15);
        String[] headers = {"座位号", "阅览室ID", "状态"};
        List<String[]> rows = seats.stream()
                .limit(maxSize)
                .map(s -> new String[]{
                        s.getSeatNumber(),
                        String.valueOf(s.getRoomId()),
                        s.getStatus()
                })
                .collect(Collectors.toList());
        sb.append(MarkdownFormatter.toTable(headers, rows));
        return sb.toString();
    }

    @Tool(description = "查询我的座位预约记录。当用户询问我的预约、预约记录时调用此工具。")
    public String getMyReservations(@ToolParam(description = "学生ID") Long studentId) {
        log.info("Tool调用: getMyReservations, studentId={}", studentId);
        List<SeatReservationListVO> reservations = seatReservationMapper.getSeatReservationList(
                studentId, null, null, null);
        if (reservations.isEmpty()) {
            return MarkdownFormatter.noData("座位预约");
        }
        int maxSize = Math.min(reservations.size(), 10);
        String[] headers = {"座位号", "日期", "开始时间", "结束时间", "状态"};
        List<String[]> rows = reservations.stream()
                .limit(maxSize)
                .map(r -> new String[]{
                        r.getSeatNumber(),
                        r.getDate() != null ? r.getDate().toString() : "--",
                        r.getStartTime() != null ? r.getStartTime().toString() : "--",
                        r.getEndTime() != null ? r.getEndTime().toString() : "--",
                        r.getStatus() != null ? r.getStatus() : "--"
                })
                .collect(Collectors.toList());
        return MarkdownFormatter.header("我的座位预约记录") + "\n\n" + MarkdownFormatter.toTable(headers, rows);
    }
}