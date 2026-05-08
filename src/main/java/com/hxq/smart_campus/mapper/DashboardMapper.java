package com.hxq.smart_campus.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

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
}
