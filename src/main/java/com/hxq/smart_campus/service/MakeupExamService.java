package com.hxq.smart_campus.service;

import com.github.pagehelper.PageInfo;
import com.hxq.smart_campus.entity.dto.MakeupExamCreateDTO;
import com.hxq.smart_campus.entity.dto.MakeupExamResponseDTO;
import com.hxq.smart_campus.entity.dto.MakeupExamScoreDTO;
import com.hxq.smart_campus.entity.dto.MakeupExamUpdateDTO;
import com.hxq.smart_campus.entity.vo.MakeupExamDetailVO;
import com.hxq.smart_campus.entity.vo.MakeupExamListVO;
import com.hxq.smart_campus.entity.vo.MyMakeupExamVO;

import java.util.List;

public interface MakeupExamService {
    /**
     * 创建补考
     * @param makeupExamCreateDTO
     * @return
     */
    MakeupExamResponseDTO insertMakeupExam(MakeupExamCreateDTO makeupExamCreateDTO);
    /**
     * 更新补考
     * @param makeupExamUpdateDTO
     * @return
     */
    MakeupExamResponseDTO updateMakeupExam(MakeupExamUpdateDTO makeupExamUpdateDTO);
    /**
     * 删除补考
     * @param id
     * @return
     */
    boolean deleteMakeupExam(Long id);
    /**
     * 分页查询补考列表
     * @param pageNum
     * @param pageSize
     * @param scoreEntryId
     * @param status
     * @return
     */
    PageInfo<MakeupExamListVO> getMakeupExamList(Integer pageNum, Integer pageSize, Long scoreEntryId, String status);
    /**
     * 获取补考详情
     * @param id
     * @return
     */
    MakeupExamDetailVO getMakeupExamDetail(Long id);
    /**
     * 获取我的补考列表
     * @param status
     * @return
     */
    List<MyMakeupExamVO> getMyMakeupExamList(String status);

    /**
     * 录入补考成绩
     * @param makeupExamScoreDTO
     * @return
     */
    boolean insertMakeupExamScore(MakeupExamScoreDTO makeupExamScoreDTO);


}
