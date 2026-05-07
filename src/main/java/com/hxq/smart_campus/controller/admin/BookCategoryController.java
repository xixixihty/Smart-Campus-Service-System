package com.hxq.smart_campus.controller.admin;

import com.github.pagehelper.PageInfo;
import com.hxq.smart_campus.entity.dto.BookCategoryCreateDTO;
import com.hxq.smart_campus.entity.dto.BookCategoryResponseDTO;
import com.hxq.smart_campus.entity.dto.BookCategoryUpdateDTO;
import com.hxq.smart_campus.entity.vo.BookCategoryDetailVO;
import com.hxq.smart_campus.entity.vo.BookCategoryListVO;
import com.hxq.smart_campus.result.Result;
import com.hxq.smart_campus.service.BookCategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@Tag(name = "图书类别管理")
@RequestMapping("/api/book-categories/admin")
@RequiredArgsConstructor
public class BookCategoryController {
    private final BookCategoryService bookCategoryService;

    /**
     * 获取图书类别列表
     * @param pageNum
     * @param pageSize
     * @param categoryName
     * @return
     */
    @GetMapping
    @Operation(summary = "获取图书类别列表")
    public Result<PageInfo<BookCategoryListVO>> getCategoryList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String categoryName
    ) {
        log.info("获取图书类别列表，参数：pageNum={}, pageSize={}, categoryName={}", pageNum, pageSize, categoryName);
        PageInfo<BookCategoryListVO> pageInfo = bookCategoryService.getCategoryList(pageNum, pageSize, categoryName);
        return Result.success(pageInfo);
    }

    /**
     * 获取图书类别详情
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取图书类别详情")
    public Result<BookCategoryDetailVO> getCategoryDetail(@PathVariable Integer id) {
        log.info("获取图书类别详情，参数：categoryId={}", id);
        BookCategoryDetailVO bookCategoryDetailVO = bookCategoryService.getCategoryDetail(id);
        return Result.success(bookCategoryDetailVO);
    }

    /**
     * 新增图书类别
     * @param bookCategoryCreateDTO
     * @return
     */
    @PostMapping
    @Operation(summary = "新增图书类别")
    public Result<BookCategoryResponseDTO> insertCategory(@RequestBody BookCategoryCreateDTO bookCategoryCreateDTO) {
        log.info("新增图书类别，参数：{}", bookCategoryCreateDTO);
        BookCategoryResponseDTO bookCategoryResponseDTO = bookCategoryService.insertCategory(bookCategoryCreateDTO);
        return Result.success(bookCategoryResponseDTO);
    }

    /**
     * 更新图书类别
     * @param bookCategoryUpdateDTO
     * @return
     */
    @PutMapping
    @Operation(summary = "更新图书类别")
    public Result<BookCategoryResponseDTO> updateCategory(@RequestBody BookCategoryUpdateDTO bookCategoryUpdateDTO) {
        log.info("更新图书类别，参数：{}", bookCategoryUpdateDTO);
        BookCategoryResponseDTO bookCategoryResponseDTO = bookCategoryService.updateCategory(bookCategoryUpdateDTO);
        return Result.success(bookCategoryResponseDTO);
    }

    /**
     * 删除图书类别，根据ID列表
     * @param ids
     * @return
     */
    @DeleteMapping("/{ids}")
    @Operation(summary = "删除图书类别")
    public Result<Boolean> deleteCategory(@PathVariable List<Integer> ids) {
        log.info("删除图书类别，参数：{}", ids);
        boolean b = bookCategoryService.deleteCategory(ids);
        return Result.success(b);
    }
}
