package com.hxq.smart_campus.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.hxq.smart_campus.entity.dto.ConflictCheckDTO;
import com.hxq.smart_campus.entity.dto.CourseScheduleCreateDTO;
import com.hxq.smart_campus.entity.dto.CourseScheduleResponseDTO;
import com.hxq.smart_campus.entity.dto.CourseScheduleUpdateDTO;
import com.hxq.smart_campus.entity.vo.ConflictCheckResultVO;
import com.hxq.smart_campus.entity.vo.CourseScheduleDetailVO;
import com.hxq.smart_campus.entity.vo.CourseScheduleListVO;
import com.hxq.smart_campus.entity.vo.TimetableVO;
import com.hxq.smart_campus.exception.CourseScheduleException;
import com.hxq.smart_campus.mapper.CourseScheduleMapper;
import com.hxq.smart_campus.service.CourseScheduleService;
import com.hxq.smart_campus.utils.TimeConflictUtils;
import com.hxq.smart_campus.utils.WeekRangeUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static com.hxq.smart_campus.constant.MessageConstant.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class CourseScheduleServiceImpl implements CourseScheduleService {
    private final CourseScheduleMapper courseScheduleMapper;
    private final String CONFLICT_CHECK_TEACHER = "TEACHER";
    private final String CONFLICT_CHECK_CLASSROOM = "CLASSROOM";
    private final String CONFLICT_CHECK_CLASS = "CLASS";

    /**
     * 插入排课
     * @param courseScheduleCreateDTO
     * @return
     */
    @Override
    public CourseScheduleResponseDTO insertCourseSchedule(CourseScheduleCreateDTO courseScheduleCreateDTO) {
        log.info("开始创建排课: {}", courseScheduleCreateDTO);
        
        if (courseScheduleCreateDTO == null) {
            log.error("创建排课失败: 参数不能为空");
            throw new IllegalArgumentException("参数不能为空");
        }
        
        validateCourseSchedule(courseScheduleCreateDTO.getWeekDay(), courseScheduleCreateDTO.getStartSection(), 
                              courseScheduleCreateDTO.getEndSection(), courseScheduleCreateDTO.getWeekRange());
        
        try {
            int result = courseScheduleMapper.insertCourseSchedule(courseScheduleCreateDTO);
            if (result <= 0) {
                log.error("创建排课失败: 数据库插入失败");
                throw CourseScheduleException.createFailed("数据库插入失败");
            }
            
            Long id = courseScheduleMapper.getLastInsertId();
            CourseScheduleDetailVO courseScheduleDetailVO = courseScheduleMapper.getCourseScheduleDetail(id);
            
            if (courseScheduleDetailVO == null) {
                log.error("创建排课失败: 无法查询到创建的排课记录");
                throw CourseScheduleException.createFailed("无法查询到创建的排课记录");
            }
            
            CourseScheduleResponseDTO responseDTO = convertToResponseDTO(courseScheduleDetailVO);
            log.info("创建排课成功: id={}", id);
            
            return responseDTO;
            
        } catch (CourseScheduleException e) {
            throw e;
        } catch (Exception e) {
            log.error("创建排课失败: 系统异常", e);
            throw CourseScheduleException.createFailed("系统异常: " + e.getMessage());
        }
    }
    
    /**
     * 验证排课参数
     * @param weekDay 星期几
     * @param startSection 开始节次
     * @param endSection 结束节次
     * @param weekRange 周次范围
     */
    private void validateCourseSchedule(Integer weekDay, Integer startSection, Integer endSection, String weekRange) {
        if (!TimeConflictUtils.isValidWeekDay(weekDay)) {
            throw new IllegalArgumentException("星期几必须在1-7之间");
        }
        
        if (!TimeConflictUtils.isValidSectionRange(startSection, endSection)) {
            throw new IllegalArgumentException("节次范围不正确");
        }
        
        if (!WeekRangeUtils.isValidWeekRange(weekRange)) {
            throw new IllegalArgumentException("周次范围格式不正确，应为如：1-10,12-18");
        }
    }
    /**
     * 更新排课
     * @param courseScheduleUpdateDTO
     * @return
     */
    @Override
    public CourseScheduleResponseDTO updateCourseSchedule(CourseScheduleUpdateDTO courseScheduleUpdateDTO) {
        log.info("开始更新排课: {}", courseScheduleUpdateDTO);
        
        if (courseScheduleUpdateDTO == null) {
            log.error("更新排课失败: 参数不能为空");
            throw new IllegalArgumentException("参数不能为空");
        }
        
        try {
            int result = courseScheduleMapper.updateCourseSchedule(courseScheduleUpdateDTO);
            if (result <= 0) {
                log.error("更新排课失败: 数据库更新失败");
                throw CourseScheduleException.updateFailed("数据库更新失败");
            }
            
            CourseScheduleDetailVO detailVO = courseScheduleMapper.getCourseScheduleDetail(courseScheduleUpdateDTO.getId());
            if (detailVO == null) {
                log.error("更新排课失败: 无法查询到更新后的排课记录");
                throw CourseScheduleException.updateFailed("无法查询到更新后的排课记录");
            }
            
            CourseScheduleResponseDTO responseDTO = convertToResponseDTO(detailVO);
            log.info("更新排课成功: id={}", courseScheduleUpdateDTO.getId());
            
            return responseDTO;
            
        } catch (CourseScheduleException e) {
            throw e;
        } catch (Exception e) {
            log.error("更新排课失败: 系统异常", e);
            throw CourseScheduleException.updateFailed("系统异常: " + e.getMessage());
        }
    }

    /**
     * 删除排课
     * @param ids
     * @return
     */
    @Override
    public boolean deleteCourseSchedule(List<Long> ids) {
        log.info("开始删除排课: ids={}", ids);
        
        if (ids == null || ids.isEmpty()) {
            log.error("删除排课失败: 参数不能为空");
            throw new IllegalArgumentException("参数不能为空");
        }
        
        try {
            for (Long id : ids) {
                CourseScheduleDetailVO courseScheduleDetailVO = courseScheduleMapper.getCourseScheduleDetail(id);
                if (courseScheduleDetailVO == null) {
                    log.error("删除排课失败: 排课不存在, id={}", id);
                    throw CourseScheduleException.notFound(id);
                }
            }
            
            int result = courseScheduleMapper.deleteCourseScheduleBatch(ids);
            if (result <= 0) {
                log.error("删除排课失败: 数据库删除失败");
                throw CourseScheduleException.deleteFailed("数据库删除失败");
            }
            
            log.info("删除排课成功: 删除了{}条记录", result);
            return true;
            
        } catch (CourseScheduleException e) {
            throw e;
        } catch (Exception e) {
            log.error("删除排课失败: 系统异常", e);
            throw CourseScheduleException.deleteFailed("系统异常: " + e.getMessage());
        }
    }
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
    @Override
    public PageInfo<CourseScheduleListVO> getCourseScheduleList(Integer pageNum, Integer pageSize, Long semesterId, Long courseId, Long teacherId, Long classroomId, Integer weekDay) {
        PageHelper.startPage(pageNum, pageSize);
        List<CourseScheduleListVO> courseScheduleListVO = courseScheduleMapper.getCourseScheduleList(semesterId, courseId, teacherId, classroomId, weekDay);
        return new PageInfo<>(courseScheduleListVO);
    }

    /**
     * 获取排课详情
     * @param id
     * @return
     */
    @Override
    public CourseScheduleDetailVO getCourseScheduleDetail(Long id) {
        return courseScheduleMapper.getCourseScheduleDetail(id);
    }
    /**
     * 检查排课冲突
     * @param conflictCheckDTO
     * @return
     */
    @Override
    public ConflictCheckResultVO conflictCheck(ConflictCheckDTO conflictCheckDTO) {
        log.info("检查排课冲突: {}", conflictCheckDTO);
        
        ConflictCheckResultVO result = new ConflictCheckResultVO();
        List<ConflictCheckResultVO.ConflictDetailVO> conflicts = new ArrayList<>();
        
        try {
            Long semesterId = conflictCheckDTO.getSemesterId();
            Long teacherId = conflictCheckDTO.getTeacherId();
            Long classroomId = conflictCheckDTO.getClassroomId();
            List<Long> classIds = conflictCheckDTO.getClassIds();
            Integer weekDay = conflictCheckDTO.getWeekDay();
            Integer startSection = conflictCheckDTO.getStartSection();
            Integer endSection = conflictCheckDTO.getEndSection();
            String weekRange = conflictCheckDTO.getWeekRange();
            
            Long excludeId = null;
            
            List<CourseScheduleDetailVO> teacherConflicts = courseScheduleMapper.checkTeacherConflict(
                    semesterId, teacherId, weekDay, startSection, endSection, weekRange, excludeId);
            
            if (teacherConflicts != null && !teacherConflicts.isEmpty()) {
                for (CourseScheduleDetailVO conflict : teacherConflicts) {
                    ConflictCheckResultVO.ConflictDetailVO detail = new ConflictCheckResultVO.ConflictDetailVO();
                    detail.setConflictType(CONFLICT_CHECK_TEACHER);
                    detail.setConflictReason(String.format("教师[%s]在周%d第%d-%d节已有排课[%s]", 
                            conflict.getTeacherName(), weekDay, startSection, endSection, conflict.getCourseName()));
                    detail.setConflictSchedule(conflict);
                    conflicts.add(detail);
                }
            }
            
            List<CourseScheduleDetailVO> classroomConflicts = courseScheduleMapper.checkClassroomConflict(
                    semesterId, classroomId, weekDay, startSection, endSection, weekRange, excludeId);
            
            if (classroomConflicts != null && !classroomConflicts.isEmpty()) {
                for (CourseScheduleDetailVO conflict : classroomConflicts) {
                    ConflictCheckResultVO.ConflictDetailVO detail = new ConflictCheckResultVO.ConflictDetailVO();
                    detail.setConflictType(CONFLICT_CHECK_CLASSROOM);
                    detail.setConflictReason(String.format("教室[%s]在周%d第%d-%d节已被占用", 
                            conflict.getClassroomName(), weekDay, startSection, endSection));
                    detail.setConflictSchedule(conflict);
                    conflicts.add(detail);
                }
            }
            
            List<CourseScheduleDetailVO> classConflicts = courseScheduleMapper.checkClassConflict(
                    semesterId, classIds, weekDay, startSection, endSection, weekRange, excludeId);
            
            if (classConflicts != null && !classConflicts.isEmpty()) {
                for (CourseScheduleDetailVO conflict : classConflicts) {
                    ConflictCheckResultVO.ConflictDetailVO detail = new ConflictCheckResultVO.ConflictDetailVO();
                    detail.setConflictType(CONFLICT_CHECK_CLASS);
                    detail.setConflictReason(String.format("班级在周%d第%d-%d节已有排课[%s]", 
                            weekDay, startSection, endSection, conflict.getCourseName()));
                    detail.setConflictSchedule(conflict);
                    conflicts.add(detail);
                }
            }
            
            result.setHasConflict(!conflicts.isEmpty());
            result.setConflicts(conflicts);
            
            if (!conflicts.isEmpty()) {
                result.setMessage(String.format("检测到%d个冲突，请调整排课信息", conflicts.size()));
            } else {
                result.setMessage("未检测到冲突，可以排课");
            }
            
            log.info("冲突检查完成，结果: {}", result);
            
        } catch (Exception e) {
            log.error("冲突检查失败", e);
            throw new RuntimeException("冲突检查失败: " + e.getMessage());
        }
        
        return result;
    }
    /**
     * 查询课表
     * @param semesterId
     * @param userId
     * @param userType
     * @return
     */
    @Override
    public List<TimetableVO> queryTimetable(Long semesterId, Long userId, String userType) {
        log.info("查询课表: semesterId={}, userId={}, userType={}", semesterId, userId, userType);
        
        try {
            List<Long> classIds = null;
            
            if (userType == null || userType.isEmpty()) {
                log.info("查询所有的课表");
            } else if (USER_TYPE_TEACHER.equals(userType)) {
                log.info("查询教师课表: userId={}", userId);
                userType = USER_TYPE_TEACHER;
            } else if (USER_TYPE_STUDENT.equals(userType)) {
                log.info("查询学生课表: userId={}", userId);
                userType = USER_TYPE_STUDENT;
                
                if (userId != null) {
                    classIds = getClassIdsByStudentId(userId);
                    if (classIds == null || classIds.isEmpty()) {
                        log.warn("学生未关联班级: userId={}", userId);
                        return new ArrayList<>();
                    }
                }
            }
            
            List<TimetableVO> timetableVOList = courseScheduleMapper.queryTimetable(semesterId, userId, userType, classIds);
            log.info("查询到{}条课表记录", timetableVOList != null ? timetableVOList.size() : 0);
            
            return timetableVOList;
            
        } catch (Exception e) {
            log.error("查询课表失败", e);
            throw new RuntimeException("查询课表失败: " + e.getMessage());
        }
    }
    
    /**
     * 根据学生ID获取班级ID列表
     * @param studentId 学生ID
     * @return 班级ID列表
     */
    private List<Long> getClassIdsByStudentId(Long studentId) {
        try {
            return courseScheduleMapper.getClassIdsByStudentId(studentId);
        } catch (Exception e) {
            log.error("获取学生班级信息失败: studentId={}", studentId, e);
            return null;
        }
    }

    /**
     * 转换排课详情为响应DTO
     * @param courseScheduleDetailVO
     * @return
     */
    private CourseScheduleResponseDTO convertToResponseDTO(CourseScheduleDetailVO courseScheduleDetailVO) {
        if (courseScheduleDetailVO == null) {
            throw new IllegalArgumentException("参数不能为空");
        }
        CourseScheduleResponseDTO responseDTO = new CourseScheduleResponseDTO();
        responseDTO.setId(courseScheduleDetailVO.getId());
        responseDTO.setSemesterId(courseScheduleDetailVO.getSemesterId());
        responseDTO.setSemesterName(courseScheduleDetailVO.getSemesterName());
        responseDTO.setCourseId(courseScheduleDetailVO.getCourseId());
        responseDTO.setCourseName(courseScheduleDetailVO.getCourseName());
        responseDTO.setTeacherId(courseScheduleDetailVO.getTeacherId());
        responseDTO.setTeacherName(courseScheduleDetailVO.getTeacherName());
        responseDTO.setClassroomId(courseScheduleDetailVO.getClassroomId());

        responseDTO.setClassroomName(courseScheduleDetailVO.getClassroomName());
        responseDTO.setClassIds(courseScheduleDetailVO.getClassIds());
        responseDTO.setClassNames(courseScheduleDetailVO.getClassNames());
        responseDTO.setWeekDay(courseScheduleDetailVO.getWeekDay());
        responseDTO.setStartSection(courseScheduleDetailVO.getStartSection());
        responseDTO.setEndSection(courseScheduleDetailVO.getEndSection());
        responseDTO.setWeekRange(courseScheduleDetailVO.getWeekRange());
        try {
            if (courseScheduleDetailVO.getCreateTime() != null && !courseScheduleDetailVO.getCreateTime().isEmpty()) {
                responseDTO.setCreateTime(LocalDateTime.parse(courseScheduleDetailVO.getCreateTime(), DATE_TIME_FORMATTER));
            }
            if (courseScheduleDetailVO.getUpdateTime() != null && !courseScheduleDetailVO.getUpdateTime().isEmpty()) {
                responseDTO.setUpdateTime(LocalDateTime.parse(courseScheduleDetailVO.getUpdateTime(), DATE_TIME_FORMATTER));
            }
        } catch (Exception e) {
            log.error("转换排课详情为响应DTO失败！", e);
            throw new RuntimeException("转换排课详情为响应DTO失败！");
        }
        return responseDTO;
    }
}
