package com.hxq.smart_campus.service.impl;

import com.hxq.smart_campus.entity.dto.StudentCreateDTO;
import com.hxq.smart_campus.entity.dto.TeacherCreateDTO;
import com.hxq.smart_campus.service.StudentService;
import com.hxq.smart_campus.service.TeacherService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
public class PasswordValidationTest {

    @Autowired
    private StudentService studentService;

    @Autowired
    private TeacherService teacherService;

    /**
     * 测试弱密码（"123456"）在教师模块是否被拒绝
     */
    @Test
    public void testTeacherWeakPassword() {
        TeacherCreateDTO teacherCreateDTO = new TeacherCreateDTO();
        teacherCreateDTO.setTeacherNo("test-teacher-001");
        teacherCreateDTO.setName("测试教师");
        teacherCreateDTO.setPassword("123456");
        teacherCreateDTO.setGender("男");
        teacherCreateDTO.setCollegeId(1L);
        teacherCreateDTO.setTitle("讲师");
        teacherCreateDTO.setPhone("13800138000");
        teacherCreateDTO.setEmail("test@example.com");

        assertThrows(IllegalArgumentException.class, () -> {
            teacherService.createTeacher(teacherCreateDTO);
        });
    }

    /**
     * 测试弱密码（"123456"）在学生模块是否被拒绝
     */
    @Test
    public void testStudentWeakPassword() {
        StudentCreateDTO studentCreateDTO = new StudentCreateDTO();
        studentCreateDTO.setStudentNo("test-student-001");
        studentCreateDTO.setName("测试学生");
        studentCreateDTO.setPassword("123456");
        studentCreateDTO.setGender("男");
        studentCreateDTO.setClassId(1L);
        studentCreateDTO.setIdCard("110101199001011234");
        studentCreateDTO.setPhone("13800138000");
        studentCreateDTO.setEnrollmentDate(java.time.LocalDate.parse("2023-09-01"));

        assertThrows(RuntimeException.class, () -> {
            studentService.createStudent(studentCreateDTO);
        });
    }

    /**
     * 测试强密码在教师模块是否被接受
     */
    @Test
    public void testTeacherStrongPassword() {
        TeacherCreateDTO teacherCreateDTO = new TeacherCreateDTO();
        teacherCreateDTO.setTeacherNo("test-teacher-002");
        teacherCreateDTO.setName("测试教师2");
        teacherCreateDTO.setPassword("Test123!");
        teacherCreateDTO.setGender("女");
        teacherCreateDTO.setCollegeId(1L);
        teacherCreateDTO.setTitle("副教授");
        teacherCreateDTO.setPhone("13900139000");
        teacherCreateDTO.setEmail("test2@example.com");

        // 应该成功创建，不会抛出异常
        teacherService.createTeacher(teacherCreateDTO);
    }

    /**
     * 测试强密码在学生模块是否被接受
     */
    @Test
    public void testStudentStrongPassword() {
        StudentCreateDTO studentCreateDTO = new StudentCreateDTO();
        studentCreateDTO.setStudentNo("test-student-002");
        studentCreateDTO.setName("测试学生2");
        studentCreateDTO.setPassword("Test123!");
        studentCreateDTO.setGender("女");
        studentCreateDTO.setClassId(1L);
        studentCreateDTO.setIdCard("110101199001011235");
        studentCreateDTO.setPhone("13900139000");
        studentCreateDTO.setEnrollmentDate(java.time.LocalDate.parse("2023-09-01"));

        // 应该成功创建，不会抛出异常
        studentService.createStudent(studentCreateDTO);
    }
}
