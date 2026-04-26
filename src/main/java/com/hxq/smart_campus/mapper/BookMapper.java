package com.hxq.smart_campus.mapper;

import com.hxq.smart_campus.entity.dto.BookCreateDTO;
import com.hxq.smart_campus.entity.dto.BookUpdateDTO;
import com.hxq.smart_campus.entity.vo.BookDetailVO;
import com.hxq.smart_campus.entity.vo.BookListVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface BookMapper{
    /**
     * 获取图书列表
     * @param categoryName
     * @param title
     * @param author
     * @param categoryId
     * @param status
     * @return
     */
    List<BookListVO> getBookList(@Param("categoryName") String categoryName,
                                 @Param("title") String title,
                                 @Param("author") String author,
                                 @Param("categoryId") Integer categoryId,
                                 @Param("status") String status);

    /**
     * 获取图书详情
     * @param id
     * @return
     */
    BookDetailVO getBookDetail(Long id);
    /**
     * 插入图书
     * @param bookCreateDTO
     * @return
     */
    int insertBook(BookCreateDTO bookCreateDTO);
    /**
     * 获取最新插入的图书ID
     * @return
     */
    @Select("SELECT LAST_INSERT_ID()")
    Long getLastInsertId();

    /**
     * 更新图书
     * @param bookUpdateDTO
     * @return
     */
    int updateBook(BookUpdateDTO bookUpdateDTO);

    /**
     * 批量删除图书
     * @param ids
     * @return
     */
    int deleteBookBatch(@Param("ids") List<Long> ids);
    
    /**
     * 更新图书状态
     * @param id 图书ID
     * @param status 状态
     * @return 更新结果
     */
    int updateBookStatus(@Param("id") Long id, @Param("status") String status);
}
