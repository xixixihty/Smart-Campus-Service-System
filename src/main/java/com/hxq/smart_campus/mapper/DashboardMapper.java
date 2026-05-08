package com.hxq.smart_campus.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface DashboardMapper {

    @Select("SELECT COUNT(*) FROM college")
    Long countColleges();

    @Select("SELECT COUNT(*) FROM teacher")
    Long countTeachers();

    @Select("SELECT COUNT(*) FROM student")
    Long countStudents();

    @Select("SELECT COUNT(*) FROM course")
    Long countCourses();

    @Select("SELECT COUNT(*) FROM borrow_record")
    Long countBorrowRecords();

    @Select("SELECT COUNT(*) FROM leave_request")
    Long countLeaveRequests();

    @Select("SELECT COUNT(*) FROM notice WHERE status = '发布'")
    Long countPublishedNotices();

    @Select("SELECT c.college_name AS name, COUNT(s.id) AS value " +
            "FROM college c " +
            "LEFT JOIN major m ON c.id = m.college_id " +
            "LEFT JOIN class cl ON m.id = cl.major_id " +
            "LEFT JOIN student s ON cl.id = s.class_id " +
            "GROUP BY c.id, c.college_name " +
            "ORDER BY value DESC")
    List<Map<String, Object>> studentByCollege();

    @Select("SELECT type AS name, COUNT(*) AS value " +
            "FROM course " +
            "GROUP BY type " +
            "ORDER BY value DESC")
    List<Map<String, Object>> courseByType();

    @Select("SELECT title AS name, COUNT(*) AS value " +
            "FROM teacher " +
            "GROUP BY title " +
            "ORDER BY value DESC")
    List<Map<String, Object>> teacherByTitle();

    @Select("SELECT DATE_FORMAT(borrow_date, '%Y-%m-%d') AS name, COUNT(*) AS value " +
            "FROM borrow_record " +
            "WHERE borrow_date >= DATE_SUB(CURDATE(), INTERVAL 7 DAY) " +
            "GROUP BY DATE_FORMAT(borrow_date, '%Y-%m-%d') " +
            "ORDER BY name")
    List<Map<String, Object>> borrowTrend7Days();

    @Select("SELECT status AS name, COUNT(*) AS value " +
            "FROM leave_request " +
            "GROUP BY status " +
            "ORDER BY value DESC")
    List<Map<String, Object>> leaveByStatus();

    @Select("SELECT DATE_FORMAT(create_time, '%Y-%m-%d') AS name, COUNT(*) AS value " +
            "FROM student " +
            "WHERE create_time >= DATE_SUB(CURDATE(), INTERVAL 7 DAY) " +
            "GROUP BY DATE_FORMAT(create_time, '%Y-%m-%d') " +
            "ORDER BY name")
    List<Map<String, Object>> studentTrend7Days();
}
