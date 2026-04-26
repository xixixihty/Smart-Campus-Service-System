package com.hxq.smart_campus.mapper;

import com.hxq.smart_campus.entity.dto.BookCategoryCreateDTO;
import com.hxq.smart_campus.entity.dto.BookCategoryUpdateDTO;
import com.hxq.smart_campus.entity.vo.BookCategoryDetailVO;
import com.hxq.smart_campus.entity.vo.BookCategoryListVO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface BookCategoryMapper {
    /**
     * 获取所有分类
     * @param categoryName
     * @return
     */
    List<BookCategoryListVO> getCategoryList(String categoryName);
    /**
     * 获取分类详情
     * @param id
     * @return
     */
    BookCategoryDetailVO getCategoryDetail(Integer id);

    /**
     *
     * @param bookCategoryCreateDTO
     * @return
     */
    @Insert("insert into book_category(category_name) values(#{categoryName})")
    int insertCategory(BookCategoryCreateDTO bookCategoryCreateDTO);
    /**
     * 获取最新插入的ID
     * @return
     */
    @Insert("select last_insert_id()")
    Integer getLastInsertId();
    /**
     * 更新分类
     * @param bookCategoryUpdateDTO
     * @return
     */
    @Update("update book_category set category_name = #{categoryName} where id = #{id}")
    int updateCategory(BookCategoryUpdateDTO bookCategoryUpdateDTO);
    /**
     * 批量删除分类
     * @param ids
     * @return
     */
    int deleteCategoryBatch(@Param("ids") List<Integer> ids);
}
