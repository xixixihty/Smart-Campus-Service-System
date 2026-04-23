package com.hxq.smart_campus.service.impl;

import com.hxq.smart_campus.constant.MessageConstant;
import com.hxq.smart_campus.entity.vo.ClassDetailVO;
import com.hxq.smart_campus.mapper.ClassMapper;
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
class ClassServiceImplTest {

    @Mock
    private ClassMapper classMapper;

    @InjectMocks
    private ClassServiceImpl classService;

    @Test
    void testDeleteClassWithRelatedStudents() {
        // 准备测试数据
        Long classId = 1L;
        List<Long> ids = Collections.singletonList(classId);

        // 模拟班级详情
        ClassDetailVO classDetailVO = new ClassDetailVO();
        classDetailVO.setId(classId);
        classDetailVO.setStatus(MessageConstant.CLASS_STATUS_GRADUATED);

        // 模拟关联学生数量
        when(classMapper.getClassDetail(classId)).thenReturn(classDetailVO);
        when(classMapper.countRelatedStudents(classId)).thenReturn(1); // 存在关联学生

        // 验证删除操作被拒绝
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            classService.deleteClass(ids);
        });
        assertEquals("该班级下存在学生，无法执行操作", exception.getMessage());

        // 验证方法调用
        verify(classMapper, times(1)).getClassDetail(classId);
        verify(classMapper, times(1)).countRelatedStudents(classId);
        verify(classMapper, never()).deleteBatch(ids);
    }

    @Test
    void testDeleteClassWithoutRelatedStudents() {
        // 准备测试数据
        Long classId = 1L;
        List<Long> ids = Collections.singletonList(classId);

        // 模拟班级详情
        ClassDetailVO classDetailVO = new ClassDetailVO();
        classDetailVO.setId(classId);
        classDetailVO.setStatus(MessageConstant.CLASS_STATUS_GRADUATED);

        // 模拟关联学生数量
        when(classMapper.getClassDetail(classId)).thenReturn(classDetailVO);
        when(classMapper.countRelatedStudents(classId)).thenReturn(0); // 不存在关联学生
        when(classMapper.deleteBatch(ids)).thenReturn(1);

        // 验证删除操作成功
        boolean result = classService.deleteClass(ids);
        assertTrue(result);

        // 验证方法调用
        verify(classMapper, times(1)).getClassDetail(classId);
        verify(classMapper, times(1)).countRelatedStudents(classId);
        verify(classMapper, times(1)).deleteBatch(ids);
    }

    @Test
    void testUpdateStatusWithRelatedStudents() {
        // 准备测试数据
        Long classId = 1L;

        // 模拟班级详情
        ClassDetailVO classDetailVO = new ClassDetailVO();
        classDetailVO.setId(classId);
        classDetailVO.setStatus(MessageConstant.CLASS_STATUS_ENABLED);

        // 模拟关联学生数量
        when(classMapper.getClassDetail(classId)).thenReturn(classDetailVO);
        when(classMapper.countRelatedStudents(classId)).thenReturn(1); // 存在关联学生

        // 验证状态更新操作被拒绝
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            classService.updateStatus(classId);
        });
        assertEquals("该班级下存在学生，无法执行操作", exception.getMessage());

        // 验证方法调用
        verify(classMapper, times(1)).getClassDetail(classId);
        verify(classMapper, times(1)).countRelatedStudents(classId);
        verify(classMapper, never()).updateStatus(anyLong(), anyString());
    }

    @Test
    void testUpdateStatusWithoutRelatedStudents() {
        // 准备测试数据
        Long classId = 1L;

        // 模拟班级详情
        ClassDetailVO classDetailVO = new ClassDetailVO();
        classDetailVO.setId(classId);
        classDetailVO.setStatus(MessageConstant.CLASS_STATUS_ENABLED);

        // 模拟关联学生数量
        when(classMapper.getClassDetail(classId)).thenReturn(classDetailVO);
        when(classMapper.countRelatedStudents(classId)).thenReturn(0); // 不存在关联学生
        when(classMapper.updateStatus(classId, MessageConstant.CLASS_STATUS_GRADUATED)).thenReturn(1);

        // 验证状态更新操作成功
        boolean result = classService.updateStatus(classId);
        assertTrue(result);

        // 验证方法调用
        verify(classMapper, times(1)).getClassDetail(classId);
        verify(classMapper, times(1)).countRelatedStudents(classId);
        verify(classMapper, times(1)).updateStatus(classId, MessageConstant.CLASS_STATUS_GRADUATED);
    }
}
