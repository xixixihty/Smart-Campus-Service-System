package com.hxq.smart_campus.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.hxq.smart_campus.entity.dto.*;
import com.hxq.smart_campus.entity.pojo.MakeupExamStudent;
import com.hxq.smart_campus.entity.pojo.ScoreAuditLog;
import com.hxq.smart_campus.entity.vo.*;
import com.hxq.smart_campus.mapper.MakeupExamSessionMapper;
import com.hxq.smart_campus.mapper.MakeupExamStudentMapper;
import com.hxq.smart_campus.mapper.ScoreAuditLogMapper;
import com.hxq.smart_campus.mapper.ScoreEntryMapper;
import com.hxq.smart_campus.service.MakeupExamSessionService;
import com.hxq.smart_campus.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.hxq.smart_campus.constant.MessageConstant.*;

/**
 * 补考场次服务实现
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class MakeupExamSessionServiceImpl implements MakeupExamSessionService {

    private final MakeupExamSessionMapper sessionMapper;
    private final MakeupExamStudentMapper studentMapper;
    private final ScoreEntryMapper scoreEntryMapper;
    private final ScoreAuditLogMapper auditLogMapper;

    @Override
    public List<CandidateGroupVO> scanCandidates(Long courseId, Long semesterId) {
        if (semesterId == null) {
            throw new IllegalArgumentException("学期ID不能为空");
        }
        List<MakeupExamCandidateVO> candidates = studentMapper.scanCandidates(courseId, semesterId);
        if (candidates == null || candidates.isEmpty()) {
            return Collections.emptyList();
        }
        // 按课程分组并返回
        return groupByCourse(candidates);
    }

    private List<CandidateGroupVO> groupByCourse(List<MakeupExamCandidateVO> candidates) {
        // 通过 score_entry_id 查询对应的 course_id 进行分组
        Map<String, CandidateGroupVO> groupMap = new LinkedHashMap<>();
        for (MakeupExamCandidateVO c : candidates) {
            // 使用 scoreEntryId 查询课程信息
            ScoreEntryDetailVO detail = scoreEntryMapper.getScoreDetail(c.getScoreEntryId());
            if (detail == null) continue;

            String key = detail.getCourseId() + "_" + detail.getCourseName();
            CandidateGroupVO group = groupMap.computeIfAbsent(key, k -> {
                CandidateGroupVO g = new CandidateGroupVO();
                g.setCourseId(detail.getCourseId());
                g.setCourseName(detail.getCourseName());
                g.setCandidates(new ArrayList<>());
                return g;
            });
            group.getCandidates().add(c);
            group.setCandidateCount(group.getCandidates().size());
        }
        return new ArrayList<>(groupMap.values());
    }

    @Override
    @Transactional
    public MakeupExamSessionDetailVO createSession(MakeupExamSessionCreateDTO dto) {
        validateCreateDTO(dto);

        // 校验同课程是否已有进行中补考
        int activeCount = sessionMapper.countActiveByCourseAndSemester(dto.getCourseId(), dto.getSemesterId());
        if (activeCount > 0) {
            throw new IllegalStateException("该课程已有进行中的补考场次");
        }

        // 校验教室冲突
        List<MakeupExamSessionListVO> conflicts = sessionMapper.checkClassroomConflict(
                dto.getLocation(), dto.getExamDate(),
                dto.getExamStartTime(), dto.getExamEndTime(), null);
        if (!conflicts.isEmpty()) {
            throw new IllegalStateException("该教室此时段已被占用（场次ID: " + conflicts.get(0).getId() + "）");
        }

        // 插入场次
        int result = sessionMapper.insert(dto);
        if (result <= 0) {
            throw new RuntimeException("创建补考场次失败");
        }
        Long sessionId = sessionMapper.getLastInsertId();

        // 自动分配学生
        allocateStudents(sessionId, dto.getCourseId(), dto.getSemesterId(), dto.getMaxCapacity());

        // 返回场次详情
        return getSessionDetail(sessionId);
    }

    private void allocateStudents(Long sessionId, Long courseId, Long semesterId, int maxCapacity) {
        List<MakeupExamCandidateVO> candidates = studentMapper.scanCandidates(courseId, semesterId);
        if (candidates == null || candidates.isEmpty()) {
            // 没有待补考学生，场次创建但不分配
            return;
        }

        // 按分数升序排列，取前 maxCapacity 人
        List<MakeupExamCandidateVO> allocated = candidates.stream()
                .sorted(Comparator.comparing(c -> c.getTotalScore() != null ? c.getTotalScore() : BigDecimal.ZERO))
                .limit(maxCapacity)
                .collect(Collectors.toList());

        // 批量插入学生关联
        List<MakeupExamStudent> students = new ArrayList<>();
        for (MakeupExamCandidateVO c : allocated) {
            MakeupExamStudent student = new MakeupExamStudent();
            student.setSessionId(sessionId);
            student.setStudentId(c.getStudentId());
            student.setScoreEntryId(c.getScoreEntryId());
            student.setStudentStatus("待通知");
            students.add(student);
        }

        if (!students.isEmpty()) {
            studentMapper.batchInsert(students);

            // 更新 score_entry.makeup_exam_id 指向新场次
            for (MakeupExamStudent student : students) {
                updateScoreEntryMakeupExamId(student.getScoreEntryId(), sessionId);
            }
        }

        // 更新 enrolled_count
        sessionMapper.updateEnrolledCount(sessionId, students.size());
        // 更新状态为已安排
        sessionMapper.updateStatus(sessionId, SESSION_STATUS_ARRANGED);
    }

    private void updateScoreEntryMakeupExamId(Long scoreEntryId, Long sessionId) {
        scoreEntryMapper.updateMakeupExamId(scoreEntryId, sessionId);
    }

    @Override
    public MakeupExamSessionDetailVO updateSession(MakeupExamSessionUpdateDTO dto) {
        if (dto == null || dto.getId() == null) {
            throw new IllegalArgumentException("场次ID不能为空");
        }

        MakeupExamSessionDetailVO existing = sessionMapper.selectDetailById(dto.getId());
        if (existing == null) {
            throw new RuntimeException("补考场次不存在");
        }

        // 校验容量不能小于已分配人数
        if (dto.getMaxCapacity() != null && dto.getMaxCapacity() < existing.getEnrolledCount()) {
            throw new IllegalArgumentException("容量不能小于已分配人数（当前已分配 " + existing.getEnrolledCount() + " 人）");
        }

        // 校验教室冲突（排除自身）
        if (dto.getLocation() != null || dto.getExamDate() != null
                || dto.getExamStartTime() != null || dto.getExamEndTime() != null) {
            String location = dto.getLocation() != null ? dto.getLocation() : existing.getLocation();
            LocalDate examDate = dto.getExamDate() != null ? dto.getExamDate() : existing.getExamDate();
            LocalTime startTime = dto.getExamStartTime() != null ? dto.getExamStartTime() : existing.getExamStartTime();
            LocalTime endTime = dto.getExamEndTime() != null ? dto.getExamEndTime() : existing.getExamEndTime();

            List<MakeupExamSessionListVO> conflicts = sessionMapper.checkClassroomConflict(
                    location, examDate, startTime, endTime, dto.getId());
            if (!conflicts.isEmpty()) {
                throw new IllegalStateException("该教室此时段已被占用（场次ID: " + conflicts.get(0).getId() + "）");
            }
        }

        int result = sessionMapper.update(dto);
        if (result <= 0) {
            throw new RuntimeException("更新补考场次失败");
        }
        return getSessionDetail(dto.getId());
    }

    @Override
    public boolean deleteSession(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("场次ID不能为空");
        }
        MakeupExamSessionDetailVO session = sessionMapper.selectDetailById(id);
        if (session == null) {
            throw new RuntimeException("补考场次不存在");
        }
        if (!SESSION_STATUS_PENDING.equals(session.getStatus())) {
            throw new IllegalStateException("仅待安排状态的场次可删除，当前状态：" + session.getStatus());
        }
        int result = sessionMapper.deleteById(id);
        return result > 0;
    }

    @Override
    @Transactional
    public boolean cancelSession(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("场次ID不能为空");
        }
        MakeupExamSessionDetailVO session = sessionMapper.selectDetailById(id);
        if (session == null) {
            throw new RuntimeException("补考场次不存在");
        }
        if (!SESSION_STATUS_PENDING.equals(session.getStatus())
                && !SESSION_STATUS_ARRANGED.equals(session.getStatus())) {
            throw new IllegalStateException("当前状态不允许取消，状态：" + session.getStatus());
        }
        int result = sessionMapper.updateStatus(id, SESSION_STATUS_CANCELED);
        return result > 0;
    }

    @Override
    public PageInfo<MakeupExamSessionListVO> getSessionList(Integer pageNum, Integer pageSize,
                                                            Long courseId, Long semesterId,
                                                            String status, String examDate) {
        PageHelper.startPage(pageNum, pageSize);
        LocalDate date = null;
        if (examDate != null && !examDate.isEmpty()) {
            date = LocalDate.parse(examDate);
        }
        List<MakeupExamSessionListVO> list = sessionMapper.selectList(courseId, semesterId, status, date);
        return new PageInfo<>(list);
    }

    @Override
    public MakeupExamSessionDetailVO getSessionDetail(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("场次ID不能为空");
        }
        MakeupExamSessionDetailVO detail = sessionMapper.selectDetailById(id);
        if (detail == null) {
            throw new RuntimeException("补考场次不存在");
        }
        // 查询学生列表
        List<MakeupExamStudentVO> students = studentMapper.selectBySessionId(id);
        detail.setStudents(students != null ? students : Collections.emptyList());
        return detail;
    }

    @Override
    @Transactional
    public void addStudentToSession(Long sessionId, Long studentId) {
        // 校验
        MakeupExamSessionDetailVO session = sessionMapper.selectDetailById(sessionId);
        if (session == null) {
            throw new RuntimeException("补考场次不存在");
        }
        if (session.getEnrolledCount() >= session.getMaxCapacity()) {
            throw new IllegalStateException("场次人数已满，无法继续添加");
        }

        // 检查学生是否已在补考资格列表中
        List<MakeupExamCandidateVO> candidates = studentMapper.scanCandidates(
                session.getCourseId(), session.getSemesterId());
        MakeupExamCandidateVO target = candidates.stream()
                .filter(c -> c.getStudentId().equals(studentId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("该学生不在补考资格列表中"));

        // 检查学生时间冲突
        List<MyMakeupSessionVO> conflicts = studentMapper.checkStudentTimeConflict(
                studentId, session.getExamDate(),
                session.getExamStartTime(), session.getExamEndTime());
        if (!conflicts.isEmpty()) {
            throw new IllegalStateException("该学生此时段已有其他补考安排（场次ID: " + conflicts.get(0).getSessionId() + "）");
        }

        // 插入关联
        MakeupExamStudent student = new MakeupExamStudent();
        student.setSessionId(sessionId);
        student.setStudentId(studentId);
        student.setScoreEntryId(target.getScoreEntryId());
        student.setStudentStatus("待通知");
        studentMapper.batchInsert(Collections.singletonList(student));

        // 更新 enrolled_count
        sessionMapper.updateEnrolledCount(sessionId, session.getEnrolledCount() + 1);
    }

    @Override
    @Transactional
    public void removeStudentFromSession(Long sessionId, Long studentId) {
        MakeupExamSessionDetailVO session = sessionMapper.selectDetailById(sessionId);
        if (session == null) {
            throw new RuntimeException("补考场次不存在");
        }
        if (!SESSION_STATUS_PENDING.equals(session.getStatus())
                && !SESSION_STATUS_ARRANGED.equals(session.getStatus())) {
            throw new IllegalStateException("当前状态不允许移除学生");
        }

        int result = studentMapper.deleteBySessionAndStudent(sessionId, studentId);
        if (result > 0) {
            sessionMapper.updateEnrolledCount(sessionId, session.getEnrolledCount() - 1);
        }
    }

    @Override
    @Transactional
    public void batchInputScore(BatchMakeupScoreDTO dto) {
        if (dto == null || dto.getSessionId() == null) {
            throw new IllegalArgumentException("场次ID不能为空");
        }
        if (dto.getScores() == null || dto.getScores().isEmpty()) {
            throw new IllegalArgumentException("成绩列表不能为空");
        }

        MakeupExamSessionDetailVO session = sessionMapper.selectDetailById(dto.getSessionId());
        if (session == null) {
            throw new RuntimeException("补考场次不存在");
        }
        if (!SESSION_STATUS_NOTIFIED.equals(session.getStatus())
                && !SESSION_STATUS_FINISHED.equals(session.getStatus())) {
            throw new IllegalStateException("当前场次状态不允许录入成绩");
        }

        for (BatchMakeupScoreDTO.ScoreItem item : dto.getScores()) {
            processSingleScore(dto.getSessionId(), item.getStudentId(), item.getScore());
        }

        // 检查是否全部录入完成
        int unscoredCount = studentMapper.countUnscored(dto.getSessionId());
        if (unscoredCount == 0) {
            sessionMapper.updateStatus(dto.getSessionId(), SESSION_STATUS_FINISHED);
        }
    }

    @Override
    @Transactional
    public void inputSingleScore(MakeupScoreInputDTO dto) {
        if (dto == null || dto.getSessionId() == null || dto.getStudentId() == null) {
            throw new IllegalArgumentException("参数不能为空");
        }
        if (dto.getScore() == null) {
            throw new IllegalArgumentException("补考成绩不能为空");
        }

        MakeupExamSessionDetailVO session = sessionMapper.selectDetailById(dto.getSessionId());
        if (session == null) {
            throw new RuntimeException("补考场次不存在");
        }
        if (!SESSION_STATUS_NOTIFIED.equals(session.getStatus())
                && !SESSION_STATUS_FINISHED.equals(session.getStatus())) {
            throw new IllegalStateException("当前场次状态不允许录入成绩");
        }

        processSingleScore(dto.getSessionId(), dto.getStudentId(), dto.getScore());

        // 检查是否全部录入完成
        int unscoredCount = studentMapper.countUnscored(dto.getSessionId());
        if (unscoredCount == 0) {
            sessionMapper.updateStatus(dto.getSessionId(), SESSION_STATUS_FINISHED);
        }
    }

    private void processSingleScore(Long sessionId, Long studentId, BigDecimal score) {
        // 查找学生关联记录
        List<MakeupExamStudentVO> students = studentMapper.selectBySessionId(sessionId);
        MakeupExamStudentVO target = students.stream()
                .filter(s -> s.getStudentId().equals(studentId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("该学生不属于此场次"));

        BigDecimal scoreValue = score;
        String status = scoreValue.compareTo(MAKEUP_PASS_LINE) >= 0
                ? STUDENT_STATUS_PASSED : STUDENT_STATUS_FAILED;

        // 更新 makeup_exam_student
        studentMapper.updateScore(sessionId, studentId, scoreValue, status);

        // 同步更新 score_entry
        ScoreEntryUpdateDTO updateDTO = new ScoreEntryUpdateDTO();
        updateDTO.setId(target.getScoreEntryId());
        updateDTO.setMakeupScore(scoreValue);
        String examStatus = scoreValue.compareTo(MAKEUP_PASS_LINE) >= 0
                ? EXAM_STATUS_MAKEUP_PASSED : EXAM_STATUS_MAKEUP_FAILED;
        updateDTO.setExamStatus(examStatus);
        scoreEntryMapper.updateScore(updateDTO);

        // 记录审计日志
        ScoreAuditLog auditLog = new ScoreAuditLog();
        auditLog.setScoreEntryId(target.getScoreEntryId());
        auditLog.setOperatorId(SecurityUtils.getCurrentUserId());
        auditLog.setOperatorType("TEACHER");
        auditLog.setOperation("补考成绩录入");
        auditLog.setBeforeData("{\"makeupScore\": " + (target.getMakeupScore() != null ? target.getMakeupScore() : "null") + "}");
        auditLog.setAfterData("{\"makeupScore\": " + scoreValue + ", \"status\": \"" + status + "\"}");
        auditLogMapper.insert(auditLog);
    }

    @Override
    public List<MyMakeupSessionVO> getMySessions(String status) {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            throw new IllegalArgumentException("用户未登录");
        }
        List<MyMakeupSessionVO> list = studentMapper.selectMySessions(userId, status);
        return list != null ? list : Collections.emptyList();
    }

    private void validateCreateDTO(MakeupExamSessionCreateDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException("参数不能为空");
        }
        if (dto.getCourseId() == null) {
            throw new IllegalArgumentException("课程ID不能为空");
        }
        if (dto.getSemesterId() == null) {
            throw new IllegalArgumentException("学期ID不能为空");
        }
        if (dto.getExamDate() == null) {
            throw new IllegalArgumentException("考试日期不能为空");
        }
        if (dto.getExamStartTime() == null || dto.getExamEndTime() == null) {
            throw new IllegalArgumentException("考试时间不能为空");
        }
        if (dto.getExamEndTime().isBefore(dto.getExamStartTime()) || dto.getExamEndTime().equals(dto.getExamStartTime())) {
            throw new IllegalArgumentException("考试结束时间必须晚于开始时间");
        }
        if (dto.getLocation() == null || dto.getLocation().trim().isEmpty()) {
            throw new IllegalArgumentException("考试地点不能为空");
        }
        if (dto.getMaxCapacity() == null || dto.getMaxCapacity() < 1) {
            dto.setMaxCapacity(60);
        }
    }
}