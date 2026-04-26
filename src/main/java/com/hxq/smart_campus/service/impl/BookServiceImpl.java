package com.hxq.smart_campus.service.impl;


import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.hxq.smart_campus.entity.dto.BookCreateDTO;
import com.hxq.smart_campus.entity.dto.BookResponseDTO;
import com.hxq.smart_campus.entity.dto.BookUpdateDTO;
import com.hxq.smart_campus.entity.vo.BookDetailVO;
import com.hxq.smart_campus.entity.vo.BookListVO;
import com.hxq.smart_campus.mapper.BookMapper;
import com.hxq.smart_campus.service.BookService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

import static com.hxq.smart_campus.constant.MessageConstant.BOOK_STATUS_LOST;

@RequiredArgsConstructor
@Service
@Slf4j
public class BookServiceImpl implements BookService {
    private final BookMapper bookMapper;

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
    @Override
    public PageInfo<BookListVO> getBookList(Integer pageNum, Integer pageSize, String categoryName, String title, String author, Integer categoryId, String status) {
        PageHelper.startPage(pageNum, pageSize);
        List<BookListVO> bookList = bookMapper.getBookList(categoryName, title, author, categoryId, status);
        return new PageInfo<>(bookList);
    }
    /**
     * 获取图书详情
     * @param id
     * @return
     */
    @Override
    public BookDetailVO getBookDetail(Long id) {
        return bookMapper.getBookDetail(id);
    }

    /**
     * 插入图书
     * @param bookCreateDTO
     * @return
     */
    @Override
    public BookResponseDTO insertBook(BookCreateDTO bookCreateDTO) {
        if (bookCreateDTO == null || bookCreateDTO.getIsbn() == null) {
            throw new IllegalArgumentException("参数不能为空");
        }
        int result = bookMapper.insertBook(bookCreateDTO);
        if (result <= 0) {
            throw new RuntimeException("插入失败");
        }
        Long id = bookMapper.getLastInsertId();
        BookDetailVO bookDetailVO = bookMapper.getBookDetail(id);
        BookResponseDTO bookResponseDTO = convertToBookResponseDTO(bookDetailVO);
        bookResponseDTO.setCategoryId(bookCreateDTO.getCategoryId());
        return bookResponseDTO;
    }

    /**
     * 更新图书
     * @param bookUpdateDTO
     * @return
     */
    @Override
    public BookResponseDTO updateBook(BookUpdateDTO bookUpdateDTO) {
        if (bookUpdateDTO == null || bookUpdateDTO.getId() == null) {
            throw new IllegalArgumentException("参数不能为空");
        }
        int result = bookMapper.updateBook(bookUpdateDTO);
        if (result <= 0) {
            throw new RuntimeException("更新失败");
        }
        BookDetailVO bookDetailVO = bookMapper.getBookDetail(bookUpdateDTO.getId());
        return convertToBookResponseDTO(bookDetailVO);
    }

    /**
     * 删除图书
     * @param ids
     * @return
     */
    @Override
    public boolean deleteBook(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new IllegalArgumentException("参数不能为空");
        }
        for (Long id : ids) {
            // 判断图书的状态
            BookDetailVO bookDetailVO = bookMapper.getBookDetail(id);
            if (bookDetailVO == null) {
                throw new RuntimeException("图书不存在");
            }
            if (!bookDetailVO.getStatus().equals(BOOK_STATUS_LOST)) {
                throw new RuntimeException("图书状态正常，不能删除");
            }
        }
        int result = bookMapper.deleteBookBatch(ids);
        if (result <= 0) {
            throw new RuntimeException("删除失败");
        }
        return true;
    }

    /**
     * 更新图书状态
     * @param id 图书ID
     * @param status 状态
     * @return 是否更新成功
     */
    @Override
    public boolean updateBookStatus(Long id, String status) {
        if (id == null || status == null) {
            throw new IllegalArgumentException("参数不能为空");
        }
        // 检查图书是否存在
        BookDetailVO bookDetailVO = bookMapper.getBookDetail(id);
        if (bookDetailVO == null) {
            throw new RuntimeException("图书不存在");
        }
        int result = bookMapper.updateBookStatus(id, status);
        if (result <= 0) {
            throw new RuntimeException("更新状态失败");
        }
        return true;
    }

    /**
     * 将BookDetailVO转换为BookResponseDTO
     * @param bookDetailVO
     * @return
     */
    private BookResponseDTO convertToBookResponseDTO(BookDetailVO bookDetailVO) {
        if (bookDetailVO == null){
            throw new IllegalArgumentException("参数不能为空");
        }
        BookResponseDTO bookResponseDTO = new BookResponseDTO();
        bookResponseDTO.setId(bookDetailVO.getId());
        bookResponseDTO.setIsbn(bookDetailVO.getIsbn());
        bookResponseDTO.setTitle(bookDetailVO.getTitle());
        bookResponseDTO.setAuthor(bookDetailVO.getAuthor());
        bookResponseDTO.setPublisher(bookDetailVO.getPublisher());
        bookResponseDTO.setPublishDate(bookDetailVO.getPublishDate());
        bookResponseDTO.setCategoryName(bookDetailVO.getCategoryName());
        bookResponseDTO.setTotalCopies(bookDetailVO.getTotalCopies());
        bookResponseDTO.setAvailableCopies(bookDetailVO.getAvailableCopies());
        bookResponseDTO.setStatus(bookDetailVO.getStatus());
        bookResponseDTO.setCoverImage(bookDetailVO.getCoverImage());
        bookResponseDTO.setDescription(bookDetailVO.getDescription());
        return bookResponseDTO;
    }
}
