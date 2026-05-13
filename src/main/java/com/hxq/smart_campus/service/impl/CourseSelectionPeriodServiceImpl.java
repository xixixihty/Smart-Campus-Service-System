package com.hxq.smart_campus.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.hxq.smart_campus.constant.RedisConstant;
import com.hxq.smart_campus.entity.dto.CourseSelectionPeriodCreateDTO;
import com.hxq.smart_campus.entity.dto.CourseSelectionPeriodResponseDTO;
import com.hxq.smart_campus.entity.dto.CourseSelectionPeriodUpdateDTO;
import com.hxq.smart_campus.entity.pojo.Course;
import com.hxq.smart_campus.entity.pojo.CourseSelectionPeriod;
import com.hxq.smart_campus.entity.vo.CourseSelectionPeriodDetailVO;
import com.hxq.smart_campus.entity.vo.CourseSelectionPeriodListVO;
import com.hxq.smart_campus.entity.vo.SelectionTimeRedisVO;
import com.hxq.smart_campus.entity.vo.SemesterDetailVO;
import com.hxq.smart_campus.mapper.CourseSelectionPeriodMapper;
import com.hxq.smart_campus.service.CourseSelectionPeriodService;
import com.hxq.smart_campus.service.SemesterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.hxq.smart_campus.constant.MessageConstant.DATE_TIME_FORMATTER;
import static com.hxq.smart_campus.constant.RedisConstant.COURSE_SELECTION_KEY_PERIOD;



@Service
@Slf4j
@RequiredArgsConstructor
public class CourseSelectionPeriodServiceImpl implements CourseSelectionPeriodService {
    // 选课时间段Mapper
    private final CourseSelectionPeriodMapper courseSelectionPeriodMapper;
    // 学期服务
    private final SemesterService semesterService;
    // redis 模板类
    private final RedisTemplate redisTemplate;

    /**
     * 创建选课时间段
     * @param courseSelectionPeriodCreateDTO
     * @return
     */
    @Override
    public CourseSelectionPeriodResponseDTO insertCourseSelectionPeriod(CourseSelectionPeriodCreateDTO courseSelectionPeriodCreateDTO) {
        if (courseSelectionPeriodCreateDTO == null) {
            throw new IllegalArgumentException("参数不能为空");
        }
        
        if (courseSelectionPeriodCreateDTO.getSemesterId() == null && courseSelectionPeriodCreateDTO.getSemesterName() != null && !courseSelectionPeriodCreateDTO.getSemesterName().isEmpty()) {
            SemesterDetailVO semesterDetailVO = semesterService.getSemesterDetailByName(courseSelectionPeriodCreateDTO.getSemesterName());
            if (semesterDetailVO == null) {
                throw new IllegalArgumentException("学期不存在: " + courseSelectionPeriodCreateDTO.getSemesterName());
            }
            courseSelectionPeriodCreateDTO.setSemesterId(semesterDetailVO.getId());
        }
        
        int result = courseSelectionPeriodMapper.insertCourseSelectionPeriod(courseSelectionPeriodCreateDTO);
        if (result <= 0) {
            throw new RuntimeException("创建失败");
        }
        Long newCourseSelectionPeriodId = courseSelectionPeriodMapper.getLastInsertId();
        CourseSelectionPeriodDetailVO courseSelectionPeriodDetailVO = courseSelectionPeriodMapper.getCourseSelectionPeriodById(newCourseSelectionPeriodId);
        return convertToCourseSelectionPeriodResponseDTO(courseSelectionPeriodDetailVO);

    }

    /**
     * 更新选课时间段
     * @param courseSelectionPeriodUpdateDTO
     * @return
     */
    @Override
    public CourseSelectionPeriodResponseDTO updateCourseSelectionPeriod(CourseSelectionPeriodUpdateDTO courseSelectionPeriodUpdateDTO) {
        if (courseSelectionPeriodUpdateDTO == null) {
            throw new IllegalArgumentException("参数不能为空");
        }
        SemesterDetailVO semesterDetailVO = semesterService.getCurrentSemester();
        if (semesterDetailVO == null) {
            throw new IllegalArgumentException("当前学期不存在");
        }
        // 根据当前学期获取选课时间段
        CourseSelectionPeriodDetailVO currentSemesterCourseSelectionPeriod = courseSelectionPeriodMapper.getCourseSelectionPeriodBySemesterId(semesterDetailVO.getId());
        if (currentSemesterCourseSelectionPeriod == null) {
            throw new IllegalArgumentException("当前学期的选课时间段不存在");
        }
        // 判断修改的选课时间是不是当前学期的选课时间
        if (courseSelectionPeriodUpdateDTO.getId().equals(currentSemesterCourseSelectionPeriod.getId())) {
            log.info("修改的选课时间是当前学期的选课时间");
            // 需要删除Redis缓存
            redisTemplate.delete(COURSE_SELECTION_KEY_PERIOD + semesterDetailVO.getId());
        }
        int result = courseSelectionPeriodMapper.updateCourseSelectionPeriod(courseSelectionPeriodUpdateDTO);
        if (result <= 0) {
            throw new RuntimeException("更新失败");
        }
        CourseSelectionPeriodDetailVO courseSelectionPeriodDetailVO = courseSelectionPeriodMapper.getCourseSelectionPeriodById(courseSelectionPeriodUpdateDTO.getId());
        if (courseSelectionPeriodDetailVO == null) {
            throw new RuntimeException("更新失败");
        }
        // 转换为RedisVO
        SelectionTimeRedisVO selectionTimeRedisVO = convertToSelectionTimeRedisVO(courseSelectionPeriodDetailVO);
        // 将更新之后的数据保存到Redis中
        redisTemplate.opsForValue().set(COURSE_SELECTION_KEY_PERIOD + semesterDetailVO.getId(), selectionTimeRedisVO, 1, TimeUnit.DAYS);
        return convertToCourseSelectionPeriodResponseDTO(courseSelectionPeriodDetailVO);

    }
    /**
     * 删除选课时间段
     * @param ids
     * @return
     */
    @Override
    public boolean deleteCourseSelectionPeriod(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new IllegalArgumentException("参数不能为空");
        }
        // 判断删除的选课时间段是不是当前学期的选课时间段
        SemesterDetailVO semesterDetailVO = semesterService.getCurrentSemester();
        if (semesterDetailVO == null) {
            throw new IllegalArgumentException("当前学期不存在");
        }
        for (Long id : ids) {
            CourseSelectionPeriodDetailVO courseSelectionPeriodDetailVO = courseSelectionPeriodMapper.getCourseSelectionPeriodById(id);
            if (courseSelectionPeriodDetailVO == null) {
                throw new IllegalArgumentException("选课时间段不存在，ID=" + id);
            }
            if (courseSelectionPeriodDetailVO.getSemesterId().equals(semesterDetailVO.getId())) {
                log.info("删除的选课时间是当前学期的选课时间，ID={}", id);
                throw new RuntimeException("当前学期的选课时间段不能删除");
            }
        }
        int result = courseSelectionPeriodMapper.deleteBatch(ids);
        if (result <= 0) {
            throw new RuntimeException("删除失败");
        }
        return true;
    }

    /**
     * 获取选课时间段列表
     * @param pageNum
     * @param pageSize
     * @param semesterId
     * @return
     */
    @Override
    public PageInfo<CourseSelectionPeriodListVO> getCourseSelectionPeriodList(Integer pageNum, Integer pageSize, Long semesterId) {
        PageHelper.startPage(pageNum, pageSize);
        List<CourseSelectionPeriodListVO> courseSelectionPeriodListVOList = courseSelectionPeriodMapper.getCourseSelectionPeriodList(semesterId);
        return PageInfo.of(courseSelectionPeriodListVOList);
    }

    /**
     * 获取选课时间段详情
     * @param id
     * @return
     */
    @Override
    public CourseSelectionPeriodDetailVO getCourseSelectionPeriodDetail(Long id) {
        return courseSelectionPeriodMapper.getCourseSelectionPeriodById(id);
    }
    /**
     * 获取当前学期的选课时间
     * @return 选课时间段详情VO
     */
    @Override
    public SelectionTimeRedisVO getCurrentSemesterCourseSelectionPeriod() {
        SemesterDetailVO semesterDetailVO = semesterService.getCurrentSemester();
        if (semesterDetailVO == null) {
            throw new IllegalArgumentException("当前学期不存在");
        }

        String cacheKey = COURSE_SELECTION_KEY_PERIOD + semesterDetailVO.getId();
        SelectionTimeRedisVO selectionTimeRedisVO = (SelectionTimeRedisVO) redisTemplate.opsForValue().get(cacheKey);
        if (selectionTimeRedisVO != null) {
            return selectionTimeRedisVO;
        }

        CourseSelectionPeriodDetailVO currentSemesterCourseSelectionPeriod = courseSelectionPeriodMapper.getCourseSelectionPeriodBySemesterId(semesterDetailVO.getId());
        if (currentSemesterCourseSelectionPeriod == null) {
            throw new IllegalArgumentException("当前学期的选课时间段不存在");
        }

        selectionTimeRedisVO = convertToSelectionTimeRedisVO(currentSemesterCourseSelectionPeriod);
        redisTemplate.opsForValue().set(cacheKey, selectionTimeRedisVO, 30, TimeUnit.DAYS);
        return selectionTimeRedisVO;
    }


    /**
     * 转换选课时间段详情VO为响应DTO
     * @param courseSelectionPeriodDetailVO
     * @return
     */
    private CourseSelectionPeriodResponseDTO convertToCourseSelectionPeriodResponseDTO(CourseSelectionPeriodDetailVO courseSelectionPeriodDetailVO) {
        if (courseSelectionPeriodDetailVO == null) {
            throw new IllegalArgumentException("参数不能为空");
        }
        CourseSelectionPeriodResponseDTO courseSelectionPeriodResponseDTO = new CourseSelectionPeriodResponseDTO();
        courseSelectionPeriodResponseDTO.setId(courseSelectionPeriodDetailVO.getId());
        courseSelectionPeriodResponseDTO.setSemesterId(courseSelectionPeriodDetailVO.getSemesterId());
        courseSelectionPeriodResponseDTO.setSemesterName(courseSelectionPeriodDetailVO.getSemesterName());
        courseSelectionPeriodResponseDTO.setStartTime(courseSelectionPeriodDetailVO.getStartTime());
        courseSelectionPeriodResponseDTO.setEndTime(courseSelectionPeriodDetailVO.getEndTime());
        try {
            if (courseSelectionPeriodDetailVO.getCreateTime() != null && !courseSelectionPeriodDetailVO.getCreateTime().isEmpty())  {
                courseSelectionPeriodResponseDTO.setCreateTime(LocalDateTime.parse(courseSelectionPeriodDetailVO.getCreateTime(), DATE_TIME_FORMATTER));
            }
            if (courseSelectionPeriodDetailVO.getUpdateTime() != null && !courseSelectionPeriodDetailVO.getUpdateTime().isEmpty()) {
                courseSelectionPeriodResponseDTO.setUpdateTime(LocalDateTime.parse(courseSelectionPeriodDetailVO.getUpdateTime(), DATE_TIME_FORMATTER));
            }
        } catch (Exception e) {
            log.error("转换选课时间段DTO失败", e);
        }
        return courseSelectionPeriodResponseDTO;
    }

    /**
     * 转换选课时间段详情VO为RedisVO
     * @param courseSelectionPeriodDetailVO
     * @return
     */
    private SelectionTimeRedisVO convertToSelectionTimeRedisVO(CourseSelectionPeriodDetailVO courseSelectionPeriodDetailVO) {
        if (courseSelectionPeriodDetailVO == null) {
            throw new IllegalArgumentException("参数不能为空");
        }
        SelectionTimeRedisVO selectionTimeRedisVO = new SelectionTimeRedisVO();
        selectionTimeRedisVO.setStartTime(courseSelectionPeriodDetailVO.getStartTime());
        selectionTimeRedisVO.setEndTime(courseSelectionPeriodDetailVO.getEndTime());
        return selectionTimeRedisVO;

    }
}
