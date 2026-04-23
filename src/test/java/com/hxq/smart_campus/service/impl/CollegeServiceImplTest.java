package com.hxq.smart_campus.service.impl;

import com.hxq.smart_campus.entity.vo.CollegeDetailVO;
import com.hxq.smart_campus.mapper.CollegeMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static com.hxq.smart_campus.constant.MessageConstant.COLLEGE_STATUS_DISABLED;
import static com.hxq.smart_campus.constant.MessageConstant.COLLEGE_STATUS_ENABLED;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CollegeServiceImplTest {

    @Mock
    private CollegeMapper collegeMapper;

    @InjectMocks
    private CollegeServiceImpl collegeService;

    @Test
    void testDeleteCollegeWithRelatedMajors() {
        // 准备测试数据
        Long collegeId = 1L;
        List<Long> ids = Collections.singletonList(collegeId);

        // 模拟学院详情
        CollegeDetailVO collegeDetailVO = new CollegeDetailVO();
        collegeDetailVO.setId(collegeId);
        collegeDetailVO.setStatus(COLLEGE_STATUS_DISABLED);

        // 模拟关联专业数量
        when(collegeMapper.getCollegeDetail(collegeId)).thenReturn(collegeDetailVO);
        when(collegeMapper.countRelatedMajors(collegeId)).thenReturn(1); // 存在关联专业

        // 验证删除操作被拒绝
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            collegeService.deleteCollege(ids);
        });
        assertEquals("该学院下存在专业，无法执行操作", exception.getMessage());

        // 验证方法调用
        verify(collegeMapper, times(1)).getCollegeDetail(collegeId);
        verify(collegeMapper, times(1)).countRelatedMajors(collegeId);
        verify(collegeMapper, never()).deleteBatch(ids);
    }

    @Test
    void testDeleteCollegeWithoutRelatedMajors() {
        // 准备测试数据
        Long collegeId = 1L;
        List<Long> ids = Collections.singletonList(collegeId);

        // 模拟学院详情
        CollegeDetailVO collegeDetailVO = new CollegeDetailVO();
        collegeDetailVO.setId(collegeId);
        collegeDetailVO.setStatus(COLLEGE_STATUS_DISABLED);

        // 模拟关联专业数量
        when(collegeMapper.getCollegeDetail(collegeId)).thenReturn(collegeDetailVO);
        when(collegeMapper.countRelatedMajors(collegeId)).thenReturn(0); // 不存在关联专业
        when(collegeMapper.deleteBatch(ids)).thenReturn(1);

        // 验证删除操作成功
        boolean result = collegeService.deleteCollege(ids);
        assertTrue(result);

        // 验证方法调用
        verify(collegeMapper, times(1)).getCollegeDetail(collegeId);
        verify(collegeMapper, times(1)).countRelatedMajors(collegeId);
        verify(collegeMapper, times(1)).deleteBatch(ids);
    }

    @Test
    void testUpdateStatusWithRelatedMajors() {
        // 准备测试数据
        Long collegeId = 1L;

        // 模拟学院详情
        CollegeDetailVO collegeDetailVO = new CollegeDetailVO();
        collegeDetailVO.setId(collegeId);
        collegeDetailVO.setStatus(COLLEGE_STATUS_ENABLED);

        // 模拟关联专业数量
        when(collegeMapper.getCollegeDetail(collegeId)).thenReturn(collegeDetailVO);
        when(collegeMapper.countRelatedMajors(collegeId)).thenReturn(1); // 存在关联专业

        // 验证状态更新操作被拒绝
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            collegeService.updateStatus(collegeId);
        });
        assertEquals("该学院下存在专业，无法执行操作", exception.getMessage());

        // 验证方法调用
        verify(collegeMapper, times(1)).getCollegeDetail(collegeId);
        verify(collegeMapper, times(1)).countRelatedMajors(collegeId);
        verify(collegeMapper, never()).updateStatus(anyLong(), anyString());
    }

    @Test
    void testUpdateStatusWithoutRelatedMajors() {
        // 准备测试数据
        Long collegeId = 1L;

        // 模拟学院详情
        CollegeDetailVO collegeDetailVO = new CollegeDetailVO();
        collegeDetailVO.setId(collegeId);
        collegeDetailVO.setStatus(COLLEGE_STATUS_ENABLED);

        // 模拟关联专业数量
        when(collegeMapper.getCollegeDetail(collegeId)).thenReturn(collegeDetailVO);
        when(collegeMapper.countRelatedMajors(collegeId)).thenReturn(0); // 不存在关联专业
        when(collegeMapper.updateStatus(collegeId, COLLEGE_STATUS_DISABLED)).thenReturn(1);

        // 验证状态更新操作成功
        boolean result = collegeService.updateStatus(collegeId);
        assertTrue(result);

        // 验证方法调用
        verify(collegeMapper, times(1)).getCollegeDetail(collegeId);
        verify(collegeMapper, times(1)).countRelatedMajors(collegeId);
        verify(collegeMapper, times(1)).updateStatus(collegeId, COLLEGE_STATUS_DISABLED);
    }
}
