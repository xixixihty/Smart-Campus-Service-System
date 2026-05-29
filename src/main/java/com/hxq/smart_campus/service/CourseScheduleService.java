package com.hxq.smart_campus.service;

import com.github.pagehelper.PageInfo;
import com.hxq.smart_campus.entity.dto.ConflictCheckDTO;
import com.hxq.smart_campus.entity.dto.CourseScheduleCreateDTO;
import com.hxq.smart_campus.entity.dto.CourseScheduleResponseDTO;
import com.hxq.smart_campus.entity.dto.CourseScheduleUpdateDTO;
import com.hxq.smart_campus.entity.vo.ConflictCheckResultVO;
import com.hxq.smart_campus.entity.vo.CourseScheduleDetailVO;
import com.hxq.smart_campus.entity.vo.CourseScheduleListVO;
import com.hxq.smart_campus.entity.vo.StudentCourseVO;
import com.hxq.smart_campus.entity.vo.TimetableVO;

import java.util.List;

public interface CourseScheduleService {
    /**
     * 创建排课
     * @param courseScheduleCreateDTO
     * @return
     */
    CourseScheduleResponseDTO insertCourseSchedule(CourseScheduleCreateDTO courseScheduleCreateDTO);
    /**
     * 更新排课
     * @param courseScheduleUpdateDTO
     * @return
     */
    CourseScheduleResponseDTO updateCourseSchedule(CourseScheduleUpdateDTO courseScheduleUpdateDTO);
    /**
     * 删除排课
     * @param ids
     * @return
     */
    boolean deleteCourseSchedule(List<Long> ids);
    /**
     * 获取排课列表
     * @param pageNum
     * @param pageSize
     * @param semesterId
     * @param courseId
     * @param teacherId
     * @param classroomId
     * @param weekDay
     * @return
     */
    PageInfo<CourseScheduleListVO> getCourseScheduleList(Integer pageNum, Integer pageSize, Long semesterId, Long courseId, Long teacherId, Long classroomId, Integer weekDay);
    /**
     * 获取排课详情
     * @param id
     * @return
     */
    CourseScheduleDetailVO getCourseScheduleDetail(Long id);
    /**
     * 检查排课冲突
     * @param conflictCheckDTO
     * @return
     */
    ConflictCheckResultVO conflictCheck(ConflictCheckDTO conflictCheckDTO);

    /**
     * 查询课表
     * @param semesterId
     * @param userId
     * @param userType
     * @return
     */
    List<TimetableVO> queryTimetable(Long semesterId, Long userId, String userType);

    /**
     * 查询学生的全部课程（课表课程 + 选课课程合并去重）
     * @param semesterId 学期ID
     * @param studentId 学生ID
     * @return
     */
    List<StudentCourseVO> getStudentAllCourses(Long semesterId, Long studentId);
}
