package com.hxq.smart_campus.service;

import com.github.pagehelper.PageInfo;
import com.hxq.smart_campus.entity.dto.BookCategoryCreateDTO;
import com.hxq.smart_campus.entity.dto.BookCategoryResponseDTO;
import com.hxq.smart_campus.entity.dto.BookCategoryUpdateDTO;
import com.hxq.smart_campus.entity.vo.BookCategoryDetailVO;
import com.hxq.smart_campus.entity.vo.BookCategoryListVO;
import com.hxq.smart_campus.entity.vo.BookDetailVO;

import java.util.List;

public interface BookCategoryService {


    /**
     * 获取图书类别列表
     * @param pageNum
     * @param pageSize
     * @param categoryName
     * @return
     */
    PageInfo<BookCategoryListVO> getCategoryList(Integer pageNum, Integer pageSize, String categoryName);

    /**
     * 获取图书类别详情
     * @param id
     * @return
     */
    BookCategoryDetailVO getCategoryDetail(Integer id);

    /**
     *
     * @param bookCategoryCreateDTO
     * @return
     */
    BookCategoryResponseDTO insertCategory(BookCategoryCreateDTO bookCategoryCreateDTO);

    BookCategoryResponseDTO updateCategory(BookCategoryUpdateDTO bookCategoryUpdateDTO);

    boolean deleteCategory(List<Integer> ids);
}
