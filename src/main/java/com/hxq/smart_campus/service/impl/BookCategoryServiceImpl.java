package com.hxq.smart_campus.service.impl;


import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.hxq.smart_campus.entity.dto.BookCategoryCreateDTO;
import com.hxq.smart_campus.entity.dto.BookCategoryResponseDTO;
import com.hxq.smart_campus.entity.dto.BookCategoryUpdateDTO;
import com.hxq.smart_campus.entity.vo.BookCategoryDetailVO;
import com.hxq.smart_campus.entity.vo.BookCategoryListVO;
import com.hxq.smart_campus.entity.vo.BookListVO;
import com.hxq.smart_campus.mapper.BookCategoryMapper;
import com.hxq.smart_campus.mapper.BookMapper;
import com.hxq.smart_campus.service.BookCategoryService;
import com.hxq.smart_campus.service.BookService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;

import static com.hxq.smart_campus.constant.MessageConstant.DATE_TIME_FORMATTER;

@RequiredArgsConstructor
@Service
@Slf4j
public class BookCategoryServiceImpl implements BookCategoryService {
    private final BookCategoryMapper bookCategoryMapper;
    private final BookMapper bookMapper;

    /**
     * 获取分类列表
     * @param pageNum
     * @param pageSize
     * @param categoryName
     * @return
     */
    @Override
    public PageInfo<BookCategoryListVO> getCategoryList(Integer pageNum, Integer pageSize, String categoryName) {
        PageHelper.startPage(pageNum, pageSize);
        List<BookCategoryListVO> bookListVo = bookCategoryMapper.getCategoryList(categoryName);
        return new PageInfo<>(bookListVo);
    }

    /**
     * 获取分类详情
     * @param id
     * @return
     */
    @Override
    public BookCategoryDetailVO getCategoryDetail(Integer id) {
        return bookCategoryMapper.getCategoryDetail(id);
    }

    /**
     * 新增分类
     * @param bookCategoryCreateDTO
     * @return
     */
    @Override
    public BookCategoryResponseDTO insertCategory(BookCategoryCreateDTO bookCategoryCreateDTO) {
        if (bookCategoryCreateDTO == null) {
            throw new IllegalArgumentException("参数不能为空");
        }
        if (bookCategoryCreateDTO.getCategoryName() == null || bookCategoryCreateDTO.getCategoryName().trim().isEmpty()) {
            throw new IllegalArgumentException("分类名不能为空");
        }
        int result = bookCategoryMapper.insertCategory(bookCategoryCreateDTO);
        if (result <= 0) {
            throw new IllegalArgumentException("分类插入失败");
        }
        Integer id = bookCategoryMapper.getLastInsertId();
        BookCategoryDetailVO bookCategoryDetailVO = bookCategoryMapper.getCategoryDetail(id);
        return convertToBookCategoryResponseDTO(bookCategoryDetailVO);
    }

    /**
     * 更新分类
     * @param bookCategoryUpdateDTO
     * @return
     */
    @Override
    public BookCategoryResponseDTO updateCategory(BookCategoryUpdateDTO bookCategoryUpdateDTO) {
        if (bookCategoryUpdateDTO == null || bookCategoryUpdateDTO.getId() == null) {
            throw new IllegalArgumentException("参数不能为空");
        }
        int result = bookCategoryMapper.updateCategory(bookCategoryUpdateDTO);
        if (result <= 0) {
            throw new RuntimeException("更新失败");
        }
        BookCategoryDetailVO bookCategoryDetailVO = bookCategoryMapper.getCategoryDetail(bookCategoryUpdateDTO.getId());
        return convertToBookCategoryResponseDTO(bookCategoryDetailVO);
    }

    /**
     * 删除分类
     * @param ids
     * @return
     */
    @Override
    public boolean deleteCategory(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new IllegalArgumentException("参数不能为空");
        }
        // 检查分类下是否有书籍
        for (Integer id : ids) {
            BookCategoryDetailVO bookCategoryDetailVO = bookCategoryMapper.getCategoryDetail(id);
            if (bookCategoryDetailVO == null) {
                throw new IllegalArgumentException("分类不存在");
            }
            // 检查分类下是否有书籍
            List<BookListVO> bookListVo = bookMapper.getBookList(bookCategoryDetailVO.getCategoryName(), null, null, bookCategoryDetailVO.getId(), null);
            if (bookListVo != null && !bookListVo.isEmpty()) {
                throw new IllegalArgumentException("分类下有书籍，不能删除");
            }
        }
        int result = bookCategoryMapper.deleteCategoryBatch(ids);
        if (result <= 0) {
            throw new RuntimeException("删除失败");
        }
        return true; // 所有分类下都没有书籍，可以删除
    }

    /**
     * 转换分类详情VO为分类详情DTO
     * @param bookCategoryDetailVO
     * @return
     */
    private BookCategoryResponseDTO convertToBookCategoryResponseDTO(BookCategoryDetailVO bookCategoryDetailVO) {
        if (bookCategoryDetailVO == null){
            throw new IllegalArgumentException("参数不能为空");
        }
        BookCategoryResponseDTO bookCategoryResponseDTO = new BookCategoryResponseDTO();
        bookCategoryResponseDTO.setId(bookCategoryDetailVO.getId());
        bookCategoryResponseDTO.setCategoryName(bookCategoryDetailVO.getCategoryName());
        try {
            if (bookCategoryDetailVO.getCreateTime() != null && !bookCategoryDetailVO.getCreateTime().isEmpty()) {
                bookCategoryResponseDTO.setCreateTime(LocalDateTime.parse(bookCategoryDetailVO.getCreateTime(), DATE_TIME_FORMATTER));
            }
            if (bookCategoryDetailVO.getUpdateTime() != null && !bookCategoryDetailVO.getUpdateTime().isEmpty()) {
                bookCategoryResponseDTO.setUpdateTime(LocalDateTime.parse(bookCategoryDetailVO.getUpdateTime(), DATE_TIME_FORMATTER));
            }
        } catch (Exception e) {
            log.error("日期转换错误", e);
        }
        return bookCategoryResponseDTO;
    }
}
