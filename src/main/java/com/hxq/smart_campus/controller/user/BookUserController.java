package com.hxq.smart_campus.controller.user;

import com.github.pagehelper.PageInfo;
import com.hxq.smart_campus.entity.vo.BookDetailVO;
import com.hxq.smart_campus.entity.vo.BookListVO;
import com.hxq.smart_campus.result.Result;
import com.hxq.smart_campus.service.BookService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 学生端-图书浏览
 */
@RestController
@RequestMapping("/api/books/user")
@Slf4j
@Tag(name = "学生-图书浏览")
@RequiredArgsConstructor
public class BookUserController {
    private final BookService bookService;

    @GetMapping
    @Operation(summary = "获取图书列表")
    public Result<PageInfo<BookListVO>> getBookList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId
    ) {
        log.info("学生获取图书列表: pageNum={}, pageSize={}, keyword={}, categoryId={}", pageNum, pageSize, keyword, categoryId);
        PageInfo<BookListVO> pageInfo = bookService.getBookList(pageNum, pageSize, null, keyword, null, 
                categoryId != null ? categoryId.intValue() : null, null);
        return Result.success(pageInfo);
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取图书详情")
    public Result<BookDetailVO> getBookDetail(@PathVariable Long id) {
        log.info("学生获取图书详情: id={}", id);
        BookDetailVO detail = bookService.getBookDetail(id);
        return Result.success(detail);
    }
}