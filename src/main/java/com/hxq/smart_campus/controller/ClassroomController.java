package com.hxq.smart_campus.controller;

import com.github.pagehelper.PageInfo;
import com.hxq.smart_campus.entity.dto.ClassroomCreateDTO;
import com.hxq.smart_campus.entity.dto.ClassroomResponseDTO;
import com.hxq.smart_campus.entity.dto.ClassroomUpdateDTO;
import com.hxq.smart_campus.entity.vo.ClassroomDetailVO;
import com.hxq.smart_campus.entity.vo.ClassroomListVO;
import com.hxq.smart_campus.result.Result;
import com.hxq.smart_campus.service.ClassroomService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RequiredArgsConstructor
@RestController
@Slf4j
@RequestMapping("/classrooms")
@Tag(name = "教室管理")
public class ClassroomController {
    private final ClassroomService classroomService;


    /**
     * 获取教室列表
     * @param pageNum
     * @param pageSize
     * @param building
     * @param type
     * @param status
     * @return
     */
    @GetMapping
    @Operation(summary = "获取教室列表")
    public Result<PageInfo<ClassroomListVO>> getClassroomList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String building,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status
    ) {
        log.info("获取教室列表: {}, {}, {}, {}, {}", pageNum, pageSize, building, type, status);
        PageInfo<ClassroomListVO> pageInfo = classroomService.getClassroomList(pageNum, pageSize, building, type, status);
        return Result.success(pageInfo);
    }
    @GetMapping("/{id}")
    @Operation(summary = "获取教室详情")
    public Result<ClassroomDetailVO> getClassroomDetail(@PathVariable Long id) {
        log.info("获取教室详情: {}", id);
        ClassroomDetailVO classroomDetailVO = classroomService.getClassroomDetail(id);
        return Result.success(classroomDetailVO);

    }

    /**
     * 删除教室
     * @param ids
     * @return
     */
    @DeleteMapping("/{ids}")
    @Operation(summary = "删除教室")
    public Result<Boolean> deleteClassroom(@PathVariable List<Long> ids) {
        log.info("删除教室: {}", ids);
        boolean b = classroomService.deleteClassroom(ids);
        return Result.success(b);
    }
    /**
     * 添加教室
     * @param classroomCreateDTO
     * @return
     */
    @PostMapping
    @Operation(summary = "添加教室")
    public Result<ClassroomResponseDTO> insertClassroom(@RequestBody ClassroomCreateDTO classroomCreateDTO) {
        log.info("添加教室: {}", classroomCreateDTO);
        ClassroomResponseDTO classroomResponseDTO = classroomService.insertClassroom(classroomCreateDTO);
        return Result.success(classroomResponseDTO);
    }
    /**
     * 修改教室
     * @param classroomUpdateDTO
     * @return
     */
    @PutMapping
    @Operation(summary = "修改教室")
    public Result<ClassroomResponseDTO> updateClassroom(@RequestBody ClassroomUpdateDTO classroomUpdateDTO) {
        log.info("修改教室: {}", classroomUpdateDTO);
        ClassroomResponseDTO classroomResponseDTO = classroomService.updateClassroom(classroomUpdateDTO);
        return Result.success(classroomResponseDTO);
    }
}
