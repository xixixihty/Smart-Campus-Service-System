package com.hxq.smart_campus.service.impl;

import com.hxq.smart_campus.constant.MessageConstant;
import com.hxq.smart_campus.entity.vo.MajorDetailVO;
import com.hxq.smart_campus.mapper.MajorMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MajorServiceImplTest {

    @Mock
    private MajorMapper majorMapper;

    @InjectMocks
    private MajorServiceImpl majorService;

    @Test
    void testDeleteMajorWithRelatedClasses() {
        // 准备测试数据
        Long majorId = 1L;
        List<Long> ids = Collections.singletonList(majorId);

        // 模拟专业详情
        MajorDetailVO majorDetailVO = new MajorDetailVO();
        majorDetailVO.setId(majorId);
        majorDetailVO.setStatus(MessageConstant.MAJOR_STATUS_DISABLED);

        // 模拟关联班级数量
        when(majorMapper.getMajorDetail(majorId)).thenReturn(majorDetailVO);
        when(majorMapper.countRelatedClasses(majorId)).thenReturn(1); // 存在关联班级

        // 验证删除操作被拒绝
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            majorService.deleteMajorByIds(ids);
        });
        assertEquals("该专业下存在班级，无法执行操作", exception.getMessage());

        // 验证方法调用
        verify(majorMapper, times(1)).getMajorDetail(majorId);
        verify(majorMapper, times(1)).countRelatedClasses(majorId);
        verify(majorMapper, never()).deleteBatch(ids);
    }

    @Test
    void testDeleteMajorWithoutRelatedClasses() {
        // 准备测试数据
        Long majorId = 1L;
        List<Long> ids = Collections.singletonList(majorId);

        // 模拟专业详情
        MajorDetailVO majorDetailVO = new MajorDetailVO();
        majorDetailVO.setId(majorId);
        majorDetailVO.setStatus(MessageConstant.MAJOR_STATUS_DISABLED);

        // 模拟关联班级数量
        when(majorMapper.getMajorDetail(majorId)).thenReturn(majorDetailVO);
        when(majorMapper.countRelatedClasses(majorId)).thenReturn(0); // 不存在关联班级
        when(majorMapper.deleteBatch(ids)).thenReturn(1);

        // 验证删除操作成功
        boolean result = majorService.deleteMajorByIds(ids);
        assertTrue(result);

        // 验证方法调用
        verify(majorMapper, times(1)).getMajorDetail(majorId);
        verify(majorMapper, times(1)).countRelatedClasses(majorId);
        verify(majorMapper, times(1)).deleteBatch(ids);
    }

    @Test
    void testUpdateMajorStatusWithRelatedClasses() {
        // 准备测试数据
        Long majorId = 1L;

        // 模拟专业详情
        MajorDetailVO majorDetailVO = new MajorDetailVO();
        majorDetailVO.setId(majorId);
        majorDetailVO.setStatus(MessageConstant.MAJOR_STATUS_ENABLED);

        // 模拟关联班级数量
        when(majorMapper.getMajorDetail(majorId)).thenReturn(majorDetailVO);
        when(majorMapper.countRelatedClasses(majorId)).thenReturn(1); // 存在关联班级

        // 验证状态更新操作被拒绝
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            majorService.updateMajorStatus(majorId);
        });
        assertEquals("该专业下存在班级，无法执行操作", exception.getMessage());

        // 验证方法调用
        verify(majorMapper, times(1)).getMajorDetail(majorId);
        verify(majorMapper, times(1)).countRelatedClasses(majorId);
        verify(majorMapper, never()).updateStatus(anyLong(), anyString());
    }

    @Test
    void testUpdateMajorStatusWithoutRelatedClasses() {
        // 准备测试数据
        Long majorId = 1L;

        // 模拟专业详情
        MajorDetailVO majorDetailVO = new MajorDetailVO();
        majorDetailVO.setId(majorId);
        majorDetailVO.setStatus(MessageConstant.MAJOR_STATUS_ENABLED);

        // 模拟关联班级数量
        when(majorMapper.getMajorDetail(majorId)).thenReturn(majorDetailVO);
        when(majorMapper.countRelatedClasses(majorId)).thenReturn(0); // 不存在关联班级
        when(majorMapper.updateStatus(majorId, MessageConstant.MAJOR_STATUS_DISABLED)).thenReturn(1);

        // 验证状态更新操作成功
        boolean result = majorService.updateMajorStatus(majorId);
        assertTrue(result);

        // 验证方法调用
        verify(majorMapper, times(1)).getMajorDetail(majorId);
        verify(majorMapper, times(1)).countRelatedClasses(majorId);
        verify(majorMapper, times(1)).updateStatus(majorId, MessageConstant.MAJOR_STATUS_DISABLED);
    }
}
