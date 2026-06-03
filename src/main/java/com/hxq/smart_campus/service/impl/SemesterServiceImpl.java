package com.hxq.smart_campus.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.hxq.smart_campus.entity.dto.SemesterCreateDTO;
import com.hxq.smart_campus.entity.dto.SemesterResponseDTO;
import com.hxq.smart_campus.entity.dto.SemesterUpdateDTO;
import com.hxq.smart_campus.entity.vo.SemesterDetailVO;
import com.hxq.smart_campus.entity.vo.SemesterListVO;
import com.hxq.smart_campus.mapper.SemesterMapper;
import com.hxq.smart_campus.service.SemesterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static com.hxq.smart_campus.constant.MessageConstant.*;

@RequiredArgsConstructor
@Slf4j
@Service
public class SemesterServiceImpl implements SemesterService {
    private final SemesterMapper semesterMapper;

    /**
     * 当前学期缓存，TTL=60秒，避免短时间内重复查询数据库
     */
    private static final class CacheEntry {
        final SemesterDetailVO value;
        final LocalDateTime expireAt;
        CacheEntry(SemesterDetailVO value, Duration ttl) {
            this.value = value;
            this.expireAt = LocalDateTime.now().plus(ttl);
        }
        boolean isExpired() {
            return LocalDateTime.now().isAfter(expireAt);
        }
    }

    private final ConcurrentHashMap<String, CacheEntry> semesterCache = new ConcurrentHashMap<>();
    private static final String CURRENT_SEMESTER_KEY = "current";
    private static final Duration CACHE_TTL = Duration.ofSeconds(60);




    /**
     * 分页查询学期列表
     * @param pageNum
     * @param pageSize
     * @param name
     * @param status
     * @return
     */
    @Override
    public PageInfo<SemesterListVO> getSemesterList(Integer pageNum, Integer pageSize, String name, String status) {
        PageHelper.startPage(pageNum, pageSize);
        List<SemesterListVO> semesterList = semesterMapper.getSemesterList(name, status);
        return new PageInfo<>(semesterList);
    }
    /**
     * 获取学期详情
     * @param id
     * @return
     */
    @Override
    public SemesterDetailVO getSemesterDetail(Long id) {
        return semesterMapper.getSemesterDetail(id);
    }

    /**
     * 创建学期
     * @param semesterCreateDTO
     * @return
     */
    @Override
    public SemesterResponseDTO insertSemester(SemesterCreateDTO semesterCreateDTO) {
        if (semesterCreateDTO == null || semesterCreateDTO.getName() == null) {
            throw new IllegalArgumentException("参数不能为空！");
        }
        if (semesterCreateDTO.getStartDate() == null || semesterCreateDTO.getEndDate() == null) {
            throw new IllegalArgumentException("参数不能为空！");
        }
        int result = semesterMapper.insertSemester(semesterCreateDTO);
        if (result <= 0) {
            throw new RuntimeException("创建学期失败！");
        }
        Long id = semesterMapper.getLastInsertId();
        return convertToResponseDTO( semesterMapper.getSemesterDetail(id));
    }
    /**
     * 更新学期
     * @param semesterUpdateDTO
     * @return
     */
    @Override
    public SemesterResponseDTO updateSemester(SemesterUpdateDTO semesterUpdateDTO) {
        if (semesterUpdateDTO == null || semesterUpdateDTO.getId() == null) {
            throw new IllegalArgumentException("参数不能为空！");
        }
        int result = semesterMapper.updateSemester(semesterUpdateDTO);
        if (result <= 0) {
            throw new RuntimeException("更新学期失败！");
        }
        return convertToResponseDTO(semesterMapper.getSemesterDetail(semesterUpdateDTO.getId()));
    }
    /**
     * 删除学期
     * @param ids
     * @return
     */
    @Override
    public boolean deleteSemester(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new IllegalArgumentException("参数不能为空！");
        }
        for (Long id : ids) {
            SemesterDetailVO semesterDetailVO = semesterMapper.getSemesterDetail(id);
            if (semesterDetailVO == null) {
                throw new RuntimeException("学期不存在！");
            }
            if (semesterDetailVO.getIsCurrent().equals(IS_CURRENT_SEMESTER_YES)) {
                throw new RuntimeException("当前学期不能删除！");
            }
        }
        int result = semesterMapper.deleteBatch(ids);
        if (result <= 0) {
            throw new RuntimeException("删除失败！");
        }
        return true;
    }

    /**
     * 获取当前学期
     * @return
     */
    @Override
    public SemesterDetailVO getCurrentSemester() {
        CacheEntry entry = semesterCache.get(CURRENT_SEMESTER_KEY);
        if (entry != null && !entry.isExpired()) {
            return entry.value;
        }
        SemesterDetailVO current = semesterMapper.getCurrentSemester();
        if (current != null) {
            semesterCache.put(CURRENT_SEMESTER_KEY, new CacheEntry(current, CACHE_TTL));
        }
        return current;
    }

    /**
     * 根据名称获取学期详情
     * @param semesterName 学期名称
     * @return 学期详情VO
     */
    @Override
    public SemesterDetailVO getSemesterDetailByName(String semesterName) {
        if (semesterName == null || semesterName.isEmpty()) {
            throw new IllegalArgumentException("学期名称不能为空！");
        }
        return semesterMapper.getSemesterDetailByName(semesterName);
    }

    /**
     * 设置当前学期
     * @param id 学期ID
     * @return 是否设置成功
     */
    @Override
    public boolean setCurrentSemester(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("参数不能为空！");
        }
        
        // 1. 查询要设置的学期
        SemesterDetailVO targetSemester = semesterMapper.getSemesterDetail(id);
        if (targetSemester == null) {
            throw new RuntimeException("学期不存在！");
        }
        
        // 2. 查询当前学期
        SemesterDetailVO currentSemester = semesterMapper.getCurrentSemester();
        
        // 3. 检查是否已经是当前学期
        if (currentSemester != null && currentSemester.getId().equals(id)) {
            throw new RuntimeException("该学期已经是当前学期，无需重复设置！");
        }
        
        // 4. 开始事务操作
        try {
            // 4.1 将所有学期设置为非当前学期（使用批量更新提高性能）
            semesterMapper.updateAllSemestersToNonCurrent();
            
            // 4.2 将目标学期设置为当前学期
            int result = semesterMapper.updateIsCurrentById(id, true);
            if (result <= 0) {
                throw new RuntimeException("设置当前学期失败！");
            }

            // 清除缓存
            semesterCache.remove(CURRENT_SEMESTER_KEY);

            return true;
        } catch (Exception e) {
            log.error("设置当前学期失败", e);
            throw new RuntimeException("设置当前学期失败：" + e.getMessage());
        }
    }

    /**
     * 转换学期详情为响应DTO
     * @param semesterDetailVO 学期详情VO
     * @return 学期响应DTO
     */
    private SemesterResponseDTO convertToResponseDTO(SemesterDetailVO semesterDetailVO) {
        if (semesterDetailVO == null) {
            return null;
        }
        SemesterResponseDTO semesterResponseDTO = new SemesterResponseDTO();
        semesterResponseDTO.setId(semesterDetailVO.getId());
        semesterResponseDTO.setName(semesterDetailVO.getName());
        semesterResponseDTO.setStartDate(semesterDetailVO.getStartDate());
        semesterResponseDTO.setEndDate(semesterDetailVO.getEndDate());
        semesterResponseDTO.setIsCurrent(semesterDetailVO.getIsCurrent());
        semesterResponseDTO.setStatus(semesterDetailVO.getStatus());
        try {
            if (semesterDetailVO.getCreateTime() != null && !semesterDetailVO.getCreateTime().isEmpty()) {
                semesterResponseDTO.setCreateTime(LocalDateTime.parse(semesterDetailVO.getCreateTime(), DATE_TIME_FORMATTER));
            }
            if (semesterDetailVO.getUpdateTime() != null && !semesterDetailVO.getUpdateTime().isEmpty()) {
                semesterResponseDTO.setUpdateTime(LocalDateTime.parse(semesterDetailVO.getUpdateTime(), DATE_TIME_FORMATTER));
            }
        } catch (Exception e) {
            log.error("转换学期详情为响应DTO失败", e);
        }

        return semesterResponseDTO;
    }
}
