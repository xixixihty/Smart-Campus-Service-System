package com.hxq.smart_campus.controller.admin;

import com.github.pagehelper.PageInfo;
import com.hxq.smart_campus.entity.dto.ClassCreateDTO;
import com.hxq.smart_campus.entity.dto.ClassResponseDTO;
import com.hxq.smart_campus.entity.dto.ClassUpdateDTO;
import com.hxq.smart_campus.entity.vo.ClassDetailVO;
import com.hxq.smart_campus.entity.vo.ClassListVO;
import com.hxq.smart_campus.result.Result;
import com.hxq.smart_campus.service.ClassService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/classes/admin")
@Slf4j
@Tag(name = "班级管理模块")
public class ClassController {
    private final ClassService classService;


    /**
     * 查询班级列表
     *
     * @param pageNum
     * @param pageSize
     * @param majorId
     * @param className
     * @param status
     * @return
     */
    @GetMapping
    @Operation(summary = "查询班级列表")
    public Result<PageInfo<ClassListVO>> getClassList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long majorId,
            @RequestParam(required = false) String className,
            @RequestParam(required = false) String status
    ) {
        log.info("查询班级列表，参数：pageNum={}, pageSize={}, majorId={}, className={}, status={}",
                pageNum, pageSize, majorId, className, status);
        PageInfo<ClassListVO> pageInfo = classService.getClassList(
                pageNum, pageSize, majorId, className, status);
        return Result.success(pageInfo);
    }
    /**
     * 新增班级
     *
     * @param classCreateDTO
     * @return
     */
    @PostMapping
    @Operation(summary = "新增班级")
    public Result<ClassResponseDTO> insertClass(@RequestBody ClassCreateDTO classCreateDTO) {
        log.info("新增班级，参数：{}", classCreateDTO);
        ClassResponseDTO classResponseDTO = classService.insertClass(classCreateDTO);
        return Result.success(classResponseDTO);
    }
    /**
     * 查询班级详情
     *
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    @Operation(summary = "查询班级详情")
    public Result<ClassDetailVO> getClassDetail(@PathVariable Long id) {
        log.info("查询班级详情，参数：{}", id);
        ClassDetailVO classDetailVO = classService.getClassDetail(id);
        return Result.success(classDetailVO);
    }
    /**
     * 更新班级
     *
     * @param classUpdateDTO
     * @return
     */
    @PutMapping
    @Operation(summary = "更新班级")
    public Result<ClassResponseDTO> updateClass(@RequestBody ClassUpdateDTO classUpdateDTO) {
        log.info("更新班级，参数：{}", classUpdateDTO);
        ClassResponseDTO classResponseDTO = classService.updateClass(classUpdateDTO);
        return Result.success(classResponseDTO);
    }
    /**
     * 删除班级
     *
     * @param ids
     * @return
     */
    @DeleteMapping("/{ids}")
    @Operation(summary = "删除班级")
    public Result<Boolean> deleteClass(@PathVariable List<Long> ids) {
        log.info("删除班级，参数：{}", ids);
        boolean result = classService.deleteClass(ids);
        return Result.success(result);
    }
    /**
     * 更新班级状态
     *
     * @param id
     * @return
     */
    @PutMapping("/{id}/status")
    @Operation(summary = "更新班级状态")
    public Result<Boolean> updateStatus(@PathVariable Long id
    ) {
        log.info("更新班级状态，参数：{}", id);
        boolean result = classService.updateStatus(id);
        return Result.success(result);
    }
}
