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
import com.hxq.smart_campus.entity.vo.MyCourseSelectionVO;
import com.hxq.smart_campus.entity.vo.StudentCourseVO;
import com.hxq.smart_campus.entity.vo.StudentDashboardVO;
import com.hxq.smart_campus.entity.vo.TimetableVO;
import com.hxq.smart_campus.exception.CourseScheduleException;
import com.hxq.smart_campus.mapper.CourseScheduleMapper;
import com.hxq.smart_campus.mapper.CourseSelectionMapper;
import com.hxq.smart_campus.service.CourseScheduleService;
import com.hxq.smart_campus.service.SemesterService;
import com.hxq.smart_campus.utils.SecurityUtils;
import com.hxq.smart_campus.utils.TimeConflictUtils;
import com.hxq.smart_campus.utils.WeekRangeUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.hxq.smart_campus.constant.MessageConstant.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class CourseScheduleServiceImpl implements CourseScheduleService {
    private final CourseScheduleMapper courseScheduleMapper;
    private final CourseSelectionMapper courseSelectionMapper;
    private final SemesterService semesterService;
    private final String CONFLICT_CHECK_TEACHER = "TEACHER";
    private final String CONFLICT_CHECK_CLASSROOM = "CLASSROOM";
    private final String CONFLICT_CHECK_CLASS = "CLASS";

    /**
     * 插入排课
     * @param courseScheduleCreateDTO
     * @return
     */
    @Override
    @Transactional
    public CourseScheduleResponseDTO insertCourseSchedule(CourseScheduleCreateDTO courseScheduleCreateDTO) {
        log.info("开始创建排课: {}", courseScheduleCreateDTO);
        
        if (courseScheduleCreateDTO == null) {
            log.error("创建排课失败: 参数不能为空");
            throw new IllegalArgumentException("参数不能为空");
        }
        
        validateCourseSchedule(courseScheduleCreateDTO.getWeekDay(), courseScheduleCreateDTO.getStartSection(), 
                              courseScheduleCreateDTO.getEndSection(), courseScheduleCreateDTO.getWeekRange());
        
        ConflictCheckDTO conflictCheckDTO = buildConflictCheckDTO(courseScheduleCreateDTO);
        ConflictCheckResultVO conflictResult = conflictCheck(conflictCheckDTO, null);
        if (Boolean.TRUE.equals(conflictResult.getHasConflict())) {
            List<String> conflictReasons = conflictResult.getConflicts().stream()
                    .map(ConflictCheckResultVO.ConflictDetailVO::getConflictReason)
                    .collect(Collectors.toList());
            log.warn("创建排课失败: 检测到{}个冲突, reasons={}", conflictResult.getConflicts().size(), conflictReasons);
            throw CourseScheduleException.conflict(conflictResult.getMessage());
        }
        
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

            // 插入班级关联到 junction table
            courseScheduleMapper.insertCourseScheduleClasses(id, courseScheduleCreateDTO.getClassIds());

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
        
        CourseScheduleDetailVO existingRecord = courseScheduleMapper.getCourseScheduleDetail(courseScheduleUpdateDTO.getId());
        if (existingRecord == null) {
            throw CourseScheduleException.notFound(courseScheduleUpdateDTO.getId());
        }
        
        boolean timeChanged = courseScheduleUpdateDTO.getWeekDay() != null
                || courseScheduleUpdateDTO.getStartSection() != null
                || courseScheduleUpdateDTO.getEndSection() != null
                || courseScheduleUpdateDTO.getWeekRange() != null;
        boolean resourceChanged = courseScheduleUpdateDTO.getTeacherId() != null
                || courseScheduleUpdateDTO.getClassroomId() != null
                || courseScheduleUpdateDTO.getClassIds() != null;
        
        if (timeChanged || resourceChanged) {
            ConflictCheckDTO conflictCheckDTO = buildConflictCheckDTO(courseScheduleUpdateDTO, existingRecord);
            ConflictCheckResultVO conflictResult = conflictCheck(conflictCheckDTO, existingRecord.getId());
            if (Boolean.TRUE.equals(conflictResult.getHasConflict())) {
                List<String> conflictReasons = conflictResult.getConflicts().stream()
                        .map(ConflictCheckResultVO.ConflictDetailVO::getConflictReason)
                        .collect(Collectors.toList());
                log.warn("更新排课失败: 检测到{}个冲突, reasons={}", conflictResult.getConflicts().size(), conflictReasons);
                throw CourseScheduleException.conflict(conflictResult.getMessage());
            }
        }
        
        try {
            int result = courseScheduleMapper.updateCourseSchedule(courseScheduleUpdateDTO);
            if (result <= 0) {
                log.error("更新排课失败: 数据库更新失败");
                throw CourseScheduleException.updateFailed("数据库更新失败");
            }

            // 如果班级列表有变更，更新关联表
            if (courseScheduleUpdateDTO.getClassIds() != null) {
                courseScheduleMapper.deleteCourseScheduleClasses(courseScheduleUpdateDTO.getId());
                courseScheduleMapper.insertCourseScheduleClasses(courseScheduleUpdateDTO.getId(), courseScheduleUpdateDTO.getClassIds());
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
     * @param weekNum
     * @return
     */
    @Override
    @Transactional(readOnly = true)
    public PageInfo<CourseScheduleListVO> getCourseScheduleList(Integer pageNum, Integer pageSize, Long semesterId, Long courseId, Long teacherId, Long classroomId, Integer weekDay, Integer weekNum) {
        PageHelper.startPage(pageNum, pageSize);
        List<CourseScheduleListVO> courseScheduleListVO = courseScheduleMapper.getCourseScheduleList(semesterId, courseId, teacherId, classroomId, weekDay, weekNum);
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
        return conflictCheck(conflictCheckDTO, null);
    }

    private ConflictCheckResultVO conflictCheck(ConflictCheckDTO conflictCheckDTO, Long excludeId) {
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
            
            List<CourseScheduleDetailVO> teacherConflicts = courseScheduleMapper.checkTeacherConflict(
                    semesterId, teacherId, weekDay, startSection, endSection, weekRange, excludeId);
            teacherConflicts = filterByWeekRangeOverlap(teacherConflicts, weekRange);
            
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
            classroomConflicts = filterByWeekRangeOverlap(classroomConflicts, weekRange);
            
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
            classConflicts = filterByWeekRangeOverlap(classConflicts, weekRange);
            
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
    @Transactional(readOnly = true)
    public List<TimetableVO> queryTimetable(Long semesterId, Long userId, String userType) {
        log.info("查询课表: semesterId={}, userId={}, userType={}", semesterId, userId, userType);
        // 从登陆信息中判断用户的类型
        String currentUserType = SecurityUtils.getCurrentUserType();
        try {
            List<Long> classIds = null;
            if (currentUserType == null || currentUserType.isEmpty()) {
                log.info("查询所有的课表");
            } else if (USER_TYPE_TEACHER.equals(currentUserType)) {
                log.info("查询教师课表: userId={}", userId);
                userType = USER_TYPE_TEACHER;
            } else if (USER_TYPE_STUDENT.equals(currentUserType)) {
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
            timetableVOList = filterTimetableByCurrentWeek(timetableVOList, semesterId);
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

    @Override
    public List<StudentCourseVO> getStudentAllCourses(Long semesterId, Long studentId) {
        log.info("查询学生全部课程: semesterId={}, studentId={}", semesterId, studentId);

        if (semesterId == null) {
            var currentSemester = semesterService.getCurrentSemester();
            if (currentSemester == null) {
                log.warn("当前学期未设置，无法查询课程");
                return new ArrayList<>();
            }
            semesterId = currentSemester.getId();
            log.info("自动填充当前学期ID: {}", semesterId);
        }

        List<StudentCourseVO> result = new ArrayList<>();

        List<Long> classIds = getClassIdsByStudentId(studentId);
        if (classIds != null && !classIds.isEmpty()) {
            List<StudentCourseVO> scheduleCourses = courseScheduleMapper.getStudentCourseSchedules(semesterId, classIds);
            if (scheduleCourses != null) {
                String semesterName = getSemesterName(semesterId);
                for (StudentCourseVO course : scheduleCourses) {
                    course.setSemesterName(semesterName);
                }
                result.addAll(scheduleCourses);
            }
        }

        List<MyCourseSelectionVO> selections = courseSelectionMapper.getMyCourseSelectionList(studentId, semesterId, "已选");
        if (selections != null) {
            for (MyCourseSelectionVO sel : selections) {
                boolean exists = result.stream().anyMatch(c -> c.getCourseId() != null && c.getCourseId().equals(sel.getCourseId()));
                if (exists) {
                    result.stream()
                            .filter(c -> c.getCourseId() != null && c.getCourseId().equals(sel.getCourseId()))
                            .findFirst()
                            .ifPresent(c -> {
                                c.setScore(sel.getScore());
                                c.setScorePoint(sel.getScorePoint());
                            });
                } else {
                    StudentCourseVO vo = new StudentCourseVO();
                    vo.setCourseId(sel.getCourseId());
                    vo.setCourseName(sel.getCourseName());
                    vo.setCredit(sel.getCredit());
                    vo.setSemesterName(sel.getSemesterName());
                    vo.setScore(sel.getScore());
                    vo.setScorePoint(sel.getScorePoint());
                    result.add(vo);
                }
            }
        }

        log.info("查询到{}条全部课程", result.size());
        return result;
    }

    private List<TimetableVO> filterTimetableByCurrentWeek(List<TimetableVO> timetableList, Long semesterId) {
        if (timetableList == null || timetableList.isEmpty() || semesterId == null) {
            return timetableList;
        }
        try {
            var semester = semesterService.getSemesterDetail(semesterId);
            if (semester == null || semester.getStartDate() == null) {
                return timetableList;
            }
            int currentWeek = calculateCurrentWeek(semester.getStartDate());
            if (currentWeek <= 0) {
                return timetableList;
            }
            return timetableList.stream()
                    .filter(t -> t.getWeekRange() != null && isWeekInRange(currentWeek, t.getWeekRange()))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.warn("按当前周过滤课表失败，返回全部课表: semesterId={}", semesterId, e);
            return timetableList;
        }
    }

    private int calculateCurrentWeek(java.time.LocalDate semesterStartDate) {
        java.time.LocalDate today = java.time.LocalDate.now();
        long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(semesterStartDate, today);
        return (int) (daysBetween / 7) + 1;
    }

    private boolean isWeekInRange(int week, String weekRange) {
        if (weekRange == null || weekRange.isEmpty()) {
            return true;
        }
        try {
            // 去除末尾的"周"字，兼容"1-16周"格式
            String cleaned = WeekRangeUtils.cleanWeekRange(weekRange);
            String[] ranges = cleaned.split(",");
            for (String range : ranges) {
                range = range.trim();
                if (range.contains("-")) {
                    String[] parts = range.split("-");
                    int start = Integer.parseInt(parts[0].trim());
                    int end = Integer.parseInt(parts[1].trim());
                    if (week >= start && week <= end) {
                        return true;
                    }
                } else {
                    if (week == Integer.parseInt(range)) {
                        return true;
                    }
                }
            }
        } catch (NumberFormatException e) {
            log.warn("解析周次范围失败: {}", weekRange);
            return true;
        }
        return false;
    }

    private String getSemesterName(Long semesterId) {
        try {
            var semester = semesterService.getSemesterDetail(semesterId);
            return semester != null ? semester.getName() : null;
        } catch (Exception e) {
            log.warn("获取学期名称失败: semesterId={}", semesterId);
            return null;
        }
    }

    private ConflictCheckDTO buildConflictCheckDTO(CourseScheduleCreateDTO dto) {
        ConflictCheckDTO checkDTO = new ConflictCheckDTO();
        checkDTO.setSemesterId(dto.getSemesterId());
        checkDTO.setTeacherId(dto.getTeacherId());
        checkDTO.setClassroomId(dto.getClassroomId());
        checkDTO.setClassIds(dto.getClassIds());
        checkDTO.setWeekDay(dto.getWeekDay());
        checkDTO.setStartSection(dto.getStartSection());
        checkDTO.setEndSection(dto.getEndSection());
        checkDTO.setWeekRange(dto.getWeekRange());
        return checkDTO;
    }

    private ConflictCheckDTO buildConflictCheckDTO(CourseScheduleUpdateDTO dto, CourseScheduleDetailVO existing) {
        ConflictCheckDTO checkDTO = new ConflictCheckDTO();
        checkDTO.setSemesterId(existing.getSemesterId());
        checkDTO.setTeacherId(dto.getTeacherId() != null ? dto.getTeacherId() : existing.getTeacherId());
        checkDTO.setClassroomId(dto.getClassroomId() != null ? dto.getClassroomId() : existing.getClassroomId());
        checkDTO.setClassIds(dto.getClassIds() != null ? dto.getClassIds() : existing.getClassIds());
        checkDTO.setWeekDay(dto.getWeekDay() != null ? dto.getWeekDay() : existing.getWeekDay());
        checkDTO.setStartSection(dto.getStartSection() != null ? dto.getStartSection() : existing.getStartSection());
        checkDTO.setEndSection(dto.getEndSection() != null ? dto.getEndSection() : existing.getEndSection());
        checkDTO.setWeekRange(dto.getWeekRange() != null ? dto.getWeekRange() : existing.getWeekRange());
        return checkDTO;
    }

    private List<CourseScheduleDetailVO> filterByWeekRangeOverlap(List<CourseScheduleDetailVO> conflicts, String targetWeekRange) {
        if (conflicts == null || conflicts.isEmpty() || targetWeekRange == null) {
            return conflicts;
        }
        return conflicts.stream()
                .filter(c -> WeekRangeUtils.hasWeekRangeOverlap(targetWeekRange, c.getWeekRange()))
                .collect(Collectors.toList());
    }

    @Override
    public StudentDashboardVO getStudentDashboardStats(Long studentId) {
        log.info("获取学生工作台统计数据: studentId={}", studentId);
        StudentDashboardVO vo = new StudentDashboardVO();

        // 已选课程数
        Integer courseCount = courseScheduleMapper.countStudentCourses(studentId);
        vo.setCourseCount(courseCount != null ? courseCount : 0);

        // 平均成绩
        java.math.BigDecimal avgScore = courseScheduleMapper.getStudentAvgScore(studentId);
        vo.setAvgScore(avgScore);

        // 待审批请假数
        Integer pendingLeaveCount = courseScheduleMapper.countStudentLeaves(studentId);
        vo.setPendingLeaveCount(pendingLeaveCount != null ? pendingLeaveCount : 0);

        // 今日课程数
        int todayWeekDay = java.time.LocalDate.now().getDayOfWeek().getValue(); // 1=Monday, 7=Sunday
        Integer todayCourseCount = courseScheduleMapper.countTodayCourses(studentId, todayWeekDay);
        vo.setTodayCourseCount(todayCourseCount != null ? todayCourseCount : 0);

        log.info("学生工作台统计数据: courseCount={}, avgScore={}, pendingLeaveCount={}, todayCourseCount={}",
                vo.getCourseCount(), vo.getAvgScore(), vo.getPendingLeaveCount(), vo.getTodayCourseCount());
        return vo;
    }
}
