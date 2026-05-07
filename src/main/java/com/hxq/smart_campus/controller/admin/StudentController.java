package com.hxq.smart_campus.controller.admin;

import com.github.pagehelper.PageInfo;
import com.hxq.smart_campus.entity.dto.StudentCreateDTO;
import com.hxq.smart_campus.entity.dto.StudentResponseDTO;
import com.hxq.smart_campus.entity.dto.StudentUpdateDTO;
import com.hxq.smart_campus.entity.vo.StudentDetailVO;
import com.hxq.smart_campus.entity.vo.StudentListVO;
import com.hxq.smart_campus.result.Result;
import com.hxq.smart_campus.service.StudentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/students/admin")
@Slf4j
@RequiredArgsConstructor
@Tag(name = "学生管理模块")
public class StudentController {
    private final StudentService studentService;

    /**
     * 创建学生
     * @param studentCreateDTO
     * @return
     */
    @PostMapping
    @Operation(summary = "创建学生")
    public Result<StudentResponseDTO> createStudent(@RequestBody StudentCreateDTO studentCreateDTO) {
        log.info("创建学生: {}", studentCreateDTO);
        StudentResponseDTO studentResponseDTO = studentService.createStudent(studentCreateDTO);
        return Result.success(studentResponseDTO);
    }
    /**
     * 更新学生
     * @param studentUpdateDTO
     * @return
     */
    @PutMapping
    @Operation(summary = "更新学生")
    public Result<StudentResponseDTO> updateStudent(@RequestBody StudentUpdateDTO studentUpdateDTO) {
        log.info("更新学生: {}", studentUpdateDTO);
        StudentResponseDTO studentResponseDTO = studentService.updateStudent(studentUpdateDTO);
        return Result.success(studentResponseDTO);
    }
    /**
     * 删除学生
     * @param ids
     * @return
     */
    @DeleteMapping("/{ids}")
    @Operation(summary = "删除学生")
    public Result<Boolean> deleteStudent(@PathVariable List<Long> ids) {
        log.info("删除学生: {}", ids);
        boolean b = studentService.deleteStudent(ids);
        return Result.success(b);
    }

    /**
     * 获取学生列表
     * @param pageNum
     * @param pageSize
     * @param studentNo
     * @param name
     * @param classId
     * @param status
     * @param accountStatus
     * @return
     */
    @GetMapping
    @Operation(summary = "获取学生列表")
    public Result<PageInfo<StudentListVO>> getStudentList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String studentNo,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Long classId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String accountStatus
    ) {
        log.info("获取学生列表，参数：pageNum={}, pageSize={}, studentNo={}, name={}, classId={}, status={}, accountStatus={}", pageNum, pageSize, studentNo, name, classId, status, accountStatus);
        PageInfo<StudentListVO> studentList = studentService.getStudentList(pageNum, pageSize, studentNo, name, classId, status, accountStatus);
        return Result.success(studentList);
    }
    /**
     * 获取学生详情
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取学生详情")
    public Result<StudentDetailVO> getStudentDetail(@PathVariable Long id) {
        log.info("获取学生详情，参数：id={}", id);
        StudentDetailVO studentDetail = studentService.getStudentDetail(id);
        return Result.success(studentDetail);
    }

    /**
     * 重置学生密码
     * @param id
     * @return
     */
    @PutMapping("/{id}/reset-password")
    @Operation(summary = "重置学生密码")
    public Result<Boolean> reSetPassword(@PathVariable Long id) {
        log.info("重置学生密码，ID：{}", id);
        boolean b = studentService.reSetPassword(id);
        return Result.success(b);
    }
}
