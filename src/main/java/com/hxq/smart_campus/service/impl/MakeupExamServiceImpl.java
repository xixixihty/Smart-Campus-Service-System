package com.hxq.smart_campus.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.hxq.smart_campus.entity.dto.MakeupExamCreateDTO;
import com.hxq.smart_campus.entity.dto.MakeupExamResponseDTO;
import com.hxq.smart_campus.entity.dto.MakeupExamScoreDTO;
import com.hxq.smart_campus.entity.dto.MakeupExamUpdateDTO;
import com.hxq.smart_campus.entity.vo.MakeupExamDetailVO;
import com.hxq.smart_campus.entity.vo.MakeupExamListVO;
import com.hxq.smart_campus.entity.vo.MyMakeupExamVO;
import com.hxq.smart_campus.mapper.MakeupExamMapper;
import com.hxq.smart_campus.mapper.ScoreEntryMapper;
import com.hxq.smart_campus.mapper.StudentMapper;
import com.hxq.smart_campus.service.MakeupExamService;
import com.hxq.smart_campus.util.SecurityUtils;
import java.util.Collections;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static com.hxq.smart_campus.constant.MessageConstant.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class MakeupExamServiceImpl implements MakeupExamService {
    private final MakeupExamMapper makeupExamMapper;
    private final StudentMapper studentMapper;
    private final ScoreEntryMapper scoreEntryMapper;
    /**
     * 新增补考安排
     * @param makeupExamCreateDTO
     * @return
     */
    @Override
    public MakeupExamResponseDTO insertMakeupExam(MakeupExamCreateDTO makeupExamCreateDTO) {
        // 校验参数
        if (makeupExamCreateDTO == null) {
            throw new IllegalArgumentException("参数不能为空");
        }
        if (makeupExamCreateDTO.getScoreEntryId() == null) {
            throw new IllegalArgumentException("成绩记录ID不能为空");
        }
        if (makeupExamCreateDTO.getExamDate() == null) {
            throw new IllegalArgumentException("补考日期不能为空");
        }
        if (makeupExamCreateDTO.getLocation() == null || makeupExamCreateDTO.getLocation().trim().isEmpty()) {
            throw new IllegalArgumentException("补考地点不能为空");
        }
        int result = makeupExamMapper.insertMakeupExam(makeupExamCreateDTO);
        // 校验结果
        if (result <= 0) {
            throw new RuntimeException("新增补考安排失败");
        }
        // 获取新增的ID
        Long newMakeupExamId = makeupExamMapper.getLastInsertId();
        MakeupExamDetailVO makeupExamDetailVO = makeupExamMapper.getMakeupExamDetail(newMakeupExamId);
        if (makeupExamDetailVO == null) {
            throw new RuntimeException("新增补考安排失败");
        }
        return convertToMakeupExamResponseDTO(makeupExamDetailVO);
       }

    /**
     * 更新补考安排
     * @param makeupExamUpdateDTO
     * @return
     */
    @Override
    public MakeupExamResponseDTO updateMakeupExam(MakeupExamUpdateDTO makeupExamUpdateDTO) {
        // 校验参数
        if (makeupExamUpdateDTO == null) {
            throw new IllegalArgumentException("参数不能为空");
        }
        if (makeupExamUpdateDTO.getId() == null) {
            throw new IllegalArgumentException("补考ID不能为空");
        }
        int result = makeupExamMapper.updateMakeupExam(makeupExamUpdateDTO);
        if (result <= 0) {
            throw new RuntimeException("更新补考安排失败");
        }
        return convertToMakeupExamResponseDTO(makeupExamMapper.getMakeupExamDetail(makeupExamUpdateDTO.getId()));
    }

    /**
     * 删除补考安排
     * @param id
     * @return
     */
    @Override
    public boolean deleteMakeupExam(Long id) {
        // 校验参数
        if (id == null) {
            throw new IllegalArgumentException("参数不能为空");
        }
        MakeupExamDetailVO makeupExamDetailVO = makeupExamMapper.getMakeupExamDetail(id);
        // 校验补考安排是否存在
        if (makeupExamDetailVO == null) {
            throw new RuntimeException("补考安排不存在");
        }
        // 判断状态
        if (!MAKEUP_EXAM_STATUS_CANCELED.equals(makeupExamDetailVO.getStatus())) {
            throw new RuntimeException("补考安排状态不是取消，不能删除");
        }
        int result = makeupExamMapper.deleteMakeupExam(id);
        if (result <= 0) {
            throw new RuntimeException("删除补考安排失败");
        }
        return true;
    }



    /**
     * 获取补考安排列表
     * @param pageNum
     * @param pageSize
     * @param scoreEntryId
     * @param status
     * @return
     */
    @Override
    public PageInfo<MakeupExamListVO> getMakeupExamList(Integer pageNum, Integer pageSize, Long scoreEntryId, String status) {
        PageHelper.startPage(pageNum, pageSize);
        List<MakeupExamListVO> makeupExamListVOList = makeupExamMapper.getMakeupExamList(scoreEntryId, status);
        return new PageInfo<>(makeupExamListVOList);
    }

    /**
     * 获取补考安排详情
     * @param id
     * @return
     */
    @Override
    public MakeupExamDetailVO getMakeupExamDetail(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("补考ID不能为空");
        }
        return makeupExamMapper.getMakeupExamDetail(id);
    }

    /**
     * 获取我的补考安排列表
     * @param status
     * @return
     */
    @Override
    public List<MyMakeupExamVO> getMyMakeupExamList(String status) {
        // 从登录信息中获取用户ID
        Long userId = SecurityUtils.getCurrentUserId();
        // 校验用户ID
        if (userId == null) {
            throw new IllegalArgumentException("用户未登录，无法查询到补考信息！");
        }
        List<MyMakeupExamVO> myMakeupExamVOList = makeupExamMapper.getMyMakeupExamList(userId, status);
        if (myMakeupExamVOList == null) {
            return Collections.emptyList();
        }
        return myMakeupExamVOList;
    }

    /**
     * 新增补考安排成绩
     * @param makeupExamScoreDTO
     * @return
     */
    @Override
    @Transactional
    public boolean insertMakeupExamScore(MakeupExamScoreDTO makeupExamScoreDTO) {
        // 校验参数
        if (makeupExamScoreDTO == null) {
            throw new IllegalArgumentException("参数不能为空");
        }
        if (makeupExamScoreDTO.getId() == null) {
            throw new IllegalArgumentException("补考ID不能为空");
        }
        if (makeupExamScoreDTO.getMakeupScore() == null) {
            throw new IllegalArgumentException("补考成绩不能为空");
        }
        int result = makeupExamMapper.insertMakeupExamScore(makeupExamScoreDTO);
        if (result <= 0) {
            throw new RuntimeException("新增补考安排成绩失败");
        }
        // 修改考试状态
        int r = makeupExamMapper.updateMakeupExamStatus(makeupExamScoreDTO.getId(), MAKEUP_EXAM_STATUS_PASSED);
        if (r <= 0) {
            throw new RuntimeException("修改考试状态失败");
        }
        return true;
    }

    /**
     * 转换补考安排详情为响应DTO
     * @param makeupExamDetailVO
     * @return
     */
    private MakeupExamResponseDTO convertToMakeupExamResponseDTO(MakeupExamDetailVO makeupExamDetailVO) {
        if (makeupExamDetailVO == null) {
            throw new IllegalArgumentException("参数不能为空");
        }
        MakeupExamResponseDTO makeupExamResponseDTO = new MakeupExamResponseDTO();
        makeupExamResponseDTO.setId(makeupExamDetailVO.getId());
        makeupExamResponseDTO.setScoreEntryId(makeupExamDetailVO.getScoreEntryId());
        makeupExamResponseDTO.setStudentId(makeupExamDetailVO.getStudentId());
        makeupExamResponseDTO.setStudentName(makeupExamDetailVO.getStudentName());
        makeupExamResponseDTO.setStudentNo(makeupExamDetailVO.getStudentNo());
        makeupExamResponseDTO.setCourseId(makeupExamDetailVO.getCourseId());
        makeupExamResponseDTO.setCourseName(makeupExamDetailVO.getCourseName());
        makeupExamResponseDTO.setExamDate(makeupExamDetailVO.getExamDate());
        makeupExamResponseDTO.setLocation(makeupExamDetailVO.getLocation());
        makeupExamResponseDTO.setStatus(makeupExamDetailVO.getStatus());
        try {
            if (makeupExamDetailVO.getCreateTime() != null && !makeupExamDetailVO.getCreateTime().isEmpty()) {
                makeupExamResponseDTO.setCreateTime(LocalDateTime.parse(makeupExamDetailVO.getCreateTime(), DATE_TIME_FORMATTER));
            }
            if (makeupExamDetailVO.getUpdateTime() != null && !makeupExamDetailVO.getUpdateTime().isEmpty()) {
                makeupExamResponseDTO.setUpdateTime(LocalDateTime.parse(makeupExamDetailVO.getUpdateTime(), DATE_TIME_FORMATTER));
            }
        } catch (Exception e) {
            throw new RuntimeException("时间格式错误", e);
        }
        return makeupExamResponseDTO;
    }
}