package com.hxq.smart_campus.controller.admin;

import com.github.pagehelper.PageInfo;
import com.hxq.smart_campus.entity.dto.BookCreateDTO;
import com.hxq.smart_campus.entity.dto.BookResponseDTO;
import com.hxq.smart_campus.entity.dto.BookUpdateDTO;
import com.hxq.smart_campus.entity.vo.BookDetailVO;
import com.hxq.smart_campus.entity.vo.BookListVO;
import com.hxq.smart_campus.result.Result;
import com.hxq.smart_campus.service.BookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/books/admin")
@Slf4j
@Tag(name = "图书管理")
@RequiredArgsConstructor
public class BookController {
    private final BookService bookService;

    /**
     * 获取图书列表
     * @param pageNum
     * @param pageSize
     * @param categoryName
     * @param title
     * @param author
     * @param categoryId
     * @param status
     * @return
     */
    @GetMapping
    @Operation(summary = "获取图书列表")
    public Result<PageInfo<BookListVO>> getBookList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String categoryName,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String author,
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false) String status
    ) {
        log.info("获取图书列表: pageNum={}, pageSize={}, categoryName={}, title={}, author={}, categoryId={}, status={}",
                pageNum, pageSize, categoryName, title, author, categoryId, status);
        PageInfo<BookListVO> pageInfo = bookService.getBookList(pageNum, pageSize, categoryName, title, author, categoryId, status);
        return Result.success(pageInfo);
    }
    /**
     * 获取图书详情
     *
     * @param id 图书ID
     * @return
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取图书详情")
    public Result<BookDetailVO> getBookDetail(@PathVariable Long id) {
        log.info("获取图书详情，参数：bookId={}", id);
        BookDetailVO bookDetailVO = bookService.getBookDetail(id);
        return Result.success(bookDetailVO);
    }

    /**
     * 新增图书
     * @param bookCreateDTO
     * @return
     */
    @PostMapping
    @Operation(summary = "新增图书")
    public Result<BookResponseDTO> insertBook(@RequestBody BookCreateDTO bookCreateDTO) {
        log.info("新增图书，参数：{}", bookCreateDTO);
        BookResponseDTO bookResponseDTO = bookService.insertBook(bookCreateDTO);
        return Result.success(bookResponseDTO);
    }

    /**
    *更新图书
     * @param bookUpdateDTO
     * @return
     */
    @PutMapping
    @Operation(summary = "更新图书")
    public Result<BookResponseDTO> updateBook(@RequestBody BookUpdateDTO bookUpdateDTO) {
        log.info("更新图书，参数：{}", bookUpdateDTO);
        BookResponseDTO bookResponseDTO = bookService.updateBook(bookUpdateDTO);
        return Result.success(bookResponseDTO);
    }

    /**
     * 删除图书
     * @param ids
     * @return
     */
    @DeleteMapping("/{ids}")
    @Operation(summary = "删除图书")
    public Result<Boolean> deleteBook(@PathVariable List<Long> ids) {
        log.info("删除图书，参数：{}", ids);
        boolean b = bookService.deleteBook(ids);
        return Result.success(b);
    }

    /**
     * 更新图书状态
     * @param id
     * @param status
     * @return
     */
    @PutMapping("/{id}/status")
    @Operation(summary = "更新图书状态")
    public Result<Boolean> updateBookStatus(@PathVariable Long id, @RequestParam String status) {
        log.info("更新图书状态，参数：id={}, status={}", id, status);
        boolean b = bookService.updateBookStatus(id, status);
        return Result.success(b);
    }
}
