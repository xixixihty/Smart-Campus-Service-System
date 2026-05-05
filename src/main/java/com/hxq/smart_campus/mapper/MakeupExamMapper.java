package com.hxq.smart_campus.mapper;

import com.hxq.smart_campus.entity.dto.MakeupExamCreateDTO;
import com.hxq.smart_campus.entity.dto.MakeupExamScoreDTO;
import com.hxq.smart_campus.entity.dto.MakeupExamUpdateDTO;
import com.hxq.smart_campus.entity.vo.MakeupExamDetailVO;
import com.hxq.smart_campus.entity.vo.MakeupExamListVO;
import com.hxq.smart_campus.entity.vo.MyMakeupExamVO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MakeupExamMapper {
    /**
     * 新增补考安排
     * @param makeupExamCreateDTO
     * @return
     */
    int insertMakeupExam(MakeupExamCreateDTO makeupExamCreateDTO);
    /**
     * 获取新增补考安排的ID
     * @return
     */
    @Select("select LAST_INSERT_ID()")
    Long getLastInsertId();
    /**
     * 获取补考安排详情
     * @param newMakeupExamId
     * @return
     */
    MakeupExamDetailVO getMakeupExamDetail(Long newMakeupExamId);

    /**
     * 更新补考安排
     * @param makeupExamUpdateDTO
     * @return
     */
    int updateMakeupExam(MakeupExamUpdateDTO makeupExamUpdateDTO);

    /**
     * 删除补考安排
     * @param id
     * @return
     */
    @Delete("delete from makeup_exam where id = #{id}")
    int deleteMakeupExam(Long id);

    /**
     * 获取补考安排列表
     * @param scoreEntryId
     * @param status
     * @return
     */
    List<MakeupExamListVO> getMakeupExamList(@Param("scoreEntryId") Long scoreEntryId,
                                             @Param("status") String status);

    /**
     * 获取我的补考安排列表
     * @param userId
     * @param status
     * @return
     */
    List<MyMakeupExamVO> getMyMakeupExamList(@Param("userId") Long userId,
                                             @Param("status") String status);
    /**
     * 新增补考安排成绩
     * @param makeupExamScoreDTO
     * @return
     */
    int insertMakeupExamScore(MakeupExamScoreDTO makeupExamScoreDTO);

    /**
     * 修改补考安排状态
     * @param id
     * @param makeupExamStatusPassed
     * @return
     */
    @Update("update makeup_exam set status = #{makeupExamStatusPassed} where id = #{id}")
    int updateMakeupExamStatus(@Param("id") Long id,
                               @Param("makeupExamStatusPassed") String makeupExamStatusPassed);
}
