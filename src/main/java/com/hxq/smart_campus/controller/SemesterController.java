package com.hxq.smart_campus.controller;

import com.github.pagehelper.PageInfo;
import com.hxq.smart_campus.entity.dto.SemesterCreateDTO;
import com.hxq.smart_campus.entity.dto.SemesterResponseDTO;
import com.hxq.smart_campus.entity.dto.SemesterUpdateDTO;
import com.hxq.smart_campus.entity.vo.SemesterDetailVO;
import com.hxq.smart_campus.entity.vo.SemesterListVO;
import com.hxq.smart_campus.result.Result;
import com.hxq.smart_campus.service.SemesterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@Slf4j
@RequestMapping("/semesters")
@Tag(name = "学期管理")
public class SemesterController {
    private final SemesterService semesterService;

    /**
     * 分页查询学期列表
     */
    @GetMapping
    @Operation(summary = "分页查询学期列表")
    public Result<PageInfo<SemesterListVO>> getSemesterList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String status
    ) {
        log.info("查询学期列表, pageNum: {}, pageSize: {}, name: {}, status: {}", pageNum, pageSize, name, status);
        PageInfo<SemesterListVO> semesterList = semesterService.getSemesterList(pageNum, pageSize, name, status);
        return Result.success(semesterList);
    }

    /**
     * 查询学期详情
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    @Operation(summary = "查询学期详情")
    public Result<SemesterDetailVO> getSemesterDetail(@PathVariable Long id) {
        log.info("查询学期详情, id: {}", id);
        SemesterDetailVO semesterDetailVO = semesterService.getSemesterDetail(id);
        return Result.success(semesterDetailVO);
    }
    /**
     * 新增学期
     * @param semesterCreateDTO
     * @return
     */
    @PostMapping
    @Operation(summary = "新增学期")
    public Result<SemesterResponseDTO> insertSemester(@RequestBody SemesterCreateDTO semesterCreateDTO) {
        log.info("新增学期, semesterCreateDTO: {}", semesterCreateDTO);
        SemesterResponseDTO semesterResponseDTO = semesterService.insertSemester(semesterCreateDTO);
        return Result.success(semesterResponseDTO);
    }
    /**
     * 更新学期
     * @param semesterUpdateDTO
     * @return
     */
    @PutMapping
    @Operation(summary = "更新学期")
    public Result<SemesterResponseDTO> updateSemester(@RequestBody SemesterUpdateDTO semesterUpdateDTO) {
        log.info("更新学期, semesterUpdateDTO: {}", semesterUpdateDTO);
        SemesterResponseDTO semesterResponseDTO = semesterService.updateSemester(semesterUpdateDTO);
        return Result.success(semesterResponseDTO);
    }
    /**
     * 批量删除学期
     * @param ids
     * @return
     */
    @DeleteMapping("/{ids}")
    @Operation(summary = "批量删除学期")
    public Result<Boolean> deleteSemester(@PathVariable List<Long> ids) {
        log.info("批量删除学期, ids: {}", ids);
        boolean result = semesterService.deleteSemester(ids);
        return Result.success(result);
    }
    /**
     * 查询当前学期
     * @return
     */
    @GetMapping("/current")
    @Operation(summary = "查询当前学期")
    public Result<SemesterDetailVO> getCurrentSemester() {
        log.info("查询当前学期");
        SemesterDetailVO semesterDetailVO = semesterService.getCurrentSemester();
        return Result.success(semesterDetailVO);
    }

    /**
     * 设置当前学期
     * @param id
     * @return
     */
    @PutMapping("/{id}/current")
    @Operation(summary = "设置当前学期")
    public Result<Boolean> setCurrentSemester(@PathVariable Long id) {
        log.info("设置当前学期, id: {}", id);
        boolean result = semesterService.setCurrentSemester(id);
        return Result.success(result);
    }
}
