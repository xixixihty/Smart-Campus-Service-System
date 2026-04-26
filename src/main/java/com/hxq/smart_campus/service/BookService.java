package com.hxq.smart_campus.service;

import com.github.pagehelper.PageInfo;
import com.hxq.smart_campus.entity.dto.BookCreateDTO;
import com.hxq.smart_campus.entity.dto.BookResponseDTO;
import com.hxq.smart_campus.entity.dto.BookUpdateDTO;
import com.hxq.smart_campus.entity.vo.BookDetailVO;
import com.hxq.smart_campus.entity.vo.BookListVO;

import java.util.List;

public interface BookService {
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
    PageInfo<BookListVO> getBookList(Integer pageNum, Integer pageSize, String categoryName, String title, String author, Integer categoryId, String status);
    /**
     * 获取图书详情
     * @param id
     * @return
     */
    BookDetailVO getBookDetail(Long id);
    /**
     * 新增图书
     * @param bookCreateDTO
     * @return
     */
    BookResponseDTO insertBook(BookCreateDTO bookCreateDTO);
    /**
     * 更新图书
     * @param bookUpdateDTO
     * @return
     */
    BookResponseDTO updateBook(BookUpdateDTO bookUpdateDTO);
    /**
     * 删除图书
     * @param ids
     * @return
     */
    boolean deleteBook(List<Long> ids);
    
    /**
     * 更新图书状态
     * @param id 图书ID
     * @param status 状态
     * @return 是否更新成功
     */
    boolean updateBookStatus(Long id, String status);
}
