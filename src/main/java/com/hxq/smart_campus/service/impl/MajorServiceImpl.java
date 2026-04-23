package com.hxq.smart_campus.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.hxq.smart_campus.constant.MessageConstant;
import com.hxq.smart_campus.entity.dto.MajorCreateDTO;
import com.hxq.smart_campus.entity.dto.MajorResponseDTO;
import com.hxq.smart_campus.entity.dto.MajorUpdateDTO;
import com.hxq.smart_campus.entity.vo.CollegeDetailVO;
import com.hxq.smart_campus.entity.vo.MajorDetailVO;
import com.hxq.smart_campus.entity.vo.MajorListVO;
import com.hxq.smart_campus.mapper.MajorMapper;
import com.hxq.smart_campus.service.MajorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 专业管理服务实现类
 *
 * @author XiongQi He
 * @since 2026-04-23
 */
@RequiredArgsConstructor
@Service
@Slf4j
public class MajorServiceImpl implements MajorService {
    private final MajorMapper majorMapper;
    private static final String MAJOR_STATUS_DISABLED = MessageConstant.MAJOR_STATUS_DISABLED;
    private static final String MAJOR_STATUS_ENABLED = MessageConstant.MAJOR_STATUS_ENABLED;

    /**
     * 查询专业列表
     * @param pageNum
     * @param pageSize
     * @param collegeId
     * @param majorName
     * @param status
     * @return
     */
    @Override
    public PageInfo<MajorListVO> getMajorList(Integer pageNum, Integer pageSize, Long collegeId, String majorName, String status) {
        log.info("查询专业列表，参数：pageNum={}, pageSize={}, collegeId={}, majorName={}, status={}",
                pageNum, pageSize, collegeId, majorName, status);

        // 参数校验
        if (pageNum == null || pageNum < 1) {
            pageNum = 1;
            log.info("页码参数无效，设置为默认值：{}", pageNum);
        }
        if (pageSize == null || pageSize < 1 || pageSize > 100) {
            pageSize = 10;
            log.info("每页数量参数无效，设置为默认值：{}", pageSize);
        }

        PageHelper.startPage(pageNum, pageSize);
        List<MajorListVO> list = majorMapper.selectMajorList(
                collegeId, majorName, status);
        PageInfo<MajorListVO> pageInfo = new PageInfo<>(list);

        log.info("查询专业列表成功，总记录数：{}，总页数：{}", pageInfo.getTotal(), pageInfo.getPages());
        return pageInfo;
    }
    /**
     * 插入专业
     * @param majorCreateDTO
     * @return
     */
    @Override
    public MajorResponseDTO insertMajor(MajorCreateDTO majorCreateDTO) {
        // 参数校验
        if (majorCreateDTO == null) {
            log.warn("插入专业失败：参数为空");
            throw new IllegalArgumentException("参数不能为空");
        }
        int result = majorMapper.insert(majorCreateDTO);
        if (result <= 0) {
            log.warn("插入专业失败：插入失败");
            throw new IllegalArgumentException("插入失败");
        }
        // 获取插入的主ID
        Long newMajorId = majorMapper.getLastInsertId();
        MajorDetailVO majorDetailVO = majorMapper.getMajorDetail(newMajorId);
        return convertToResponseDTO(majorDetailVO);

    }
    /**
     * 更新专业
     * @param majorUpdateDTO
     * @return
     */
    @Override
    public MajorResponseDTO updateMajor(MajorUpdateDTO majorUpdateDTO) {
        // 参数校验
        if (majorUpdateDTO == null) {
            log.warn("更新专业失败：参数为空");
            throw new IllegalArgumentException("参数不能为空");
        }
        int result = majorMapper.update(majorUpdateDTO);
        if (result <= 0) {
            log.warn("更新专业失败：更新失败");
            throw new IllegalArgumentException("更新失败");
        }
        MajorDetailVO majorDetailVO = majorMapper.getMajorDetail(majorUpdateDTO.getId());
        return convertToResponseDTO(majorDetailVO);
    }
    /**
     * 批量删除专业
     * @param ids 专业ID列表
     * @return
     */
    @Override
    public boolean deleteMajorByIds(List<Long> ids) {
        log.info("批量删除专业，ID列表：{}", ids);

        // 检查ID列表是否为空
        if (ids == null || ids.isEmpty()) {
            log.warn("批量删除专业失败：ID列表为空");
            throw new IllegalArgumentException("ID列表不能为空");
        }

        for (Long id : ids) {
            // 判断专业是否存在以及专业的状态
            MajorDetailVO majorDetailVO = majorMapper.getMajorDetail(id);
            // 判断专业状态是否为正常状态，不是正常状态的不能删除
            if (majorDetailVO == null) {
                log.warn("删除专业失败：专业不存在，ID={}", id);
                throw new IllegalArgumentException("专业不存在或者专业状态启用，不能删除启用状态的专业");
            }
            if (!majorDetailVO.getStatus().equals(MAJOR_STATUS_DISABLED)) {
                throw new IllegalArgumentException("专业删除参数，专业状态异常，只能删除正常状态的专业");
            }
            // 检查专业是否有关联的班级
            int relatedClassesCount = majorMapper.countRelatedClasses(id);
            if (relatedClassesCount > 0) {
                log.warn("删除专业失败：专业下存在班级，ID={}", id);
                throw new IllegalArgumentException("该专业下存在班级，无法执行操作");
            }
        }

        // 执行删除操作
        int result = majorMapper.deleteBatch(ids);
        if (result <= 0) {
            log.error("批量删除专业失败：数据库操作失败");
            throw new RuntimeException("批量删除专业失败");
        }

        log.info("批量删除专业成功，删除数量：{}", result);
        return true;
    }
    /**
     * 查询专业详情
     * @param id 专业ID
     * @return
     */
    @Override
    public MajorDetailVO getMajorDetail(Long id) {
        log.info("获取专业详情，专业ID：{}", id);

        // 参数校验
        if (id == null) {
            log.warn("获取专业详情失败：ID为空");
            throw new IllegalArgumentException("ID不能为空");
        }

        MajorDetailVO majorDetailVO = majorMapper.getMajorDetail(id);
        if (majorDetailVO == null) {
            log.warn("获取专业详情失败：专业不存在，ID={}", id);
            throw new RuntimeException("专业不存在");
        }
        return majorDetailVO;
    }
    /**
     * 更新专业状态
     * @param id 专业ID
     * @return
     */
    @Override
    public boolean updateMajorStatus(Long id) {
        MajorDetailVO majorDetailVO = majorMapper.getMajorDetail(id);
        if (majorDetailVO == null) {
            log.warn("更新专业状态失败：专业不存在，ID={}", id);
            throw new RuntimeException("专业不存在");
        }
        // 检查专业是否有关联的班级
        int relatedClassesCount = majorMapper.countRelatedClasses(id);
        if (relatedClassesCount > 0) {
            log.warn("更新专业状态失败：专业下存在班级，ID={}", id);
            throw new IllegalArgumentException("该专业下存在班级，无法执行操作");
        }
        String currentStatus = majorDetailVO.getStatus();
        String newStatus;
        if (MessageConstant.MAJOR_STATUS_DISABLED.equals(currentStatus)) {
            newStatus = MessageConstant.MAJOR_STATUS_ENABLED;
        } else {
            newStatus = MessageConstant.MAJOR_STATUS_DISABLED;
        }
        int result = majorMapper.updateStatus(id, newStatus);
        if (result <= 0) {
            log.warn("更新专业状态失败：更新失败");
            throw new IllegalArgumentException("更新失败");
        }
        return true;
    }

    private static final java.time.format.DateTimeFormatter DATE_TIME_FORMATTER = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 转换专业详情VO为专业响应DTO
     * @param majorDetailVO 专业详情VO
     * @return 专业响应DTO
     */
    private MajorResponseDTO convertToResponseDTO(MajorDetailVO majorDetailVO) {
        if (majorDetailVO == null) {
            return null;
        }

        MajorResponseDTO majorResponseDTO = new MajorResponseDTO();
        majorResponseDTO.setId(majorDetailVO.getId());
        majorResponseDTO.setCollegeId(majorDetailVO.getCollegeId());
        majorResponseDTO.setCollegeName(majorDetailVO.getCollegeName());
        majorResponseDTO.setMajorCode(majorDetailVO.getMajorCode());
        majorResponseDTO.setMajorName(majorDetailVO.getMajorName());
        majorResponseDTO.setStudyYears(majorDetailVO.getStudyYears());
        majorResponseDTO.setStatus(majorDetailVO.getStatus());

        // 解析时间字符串为LocalDateTime
        try {
            if (majorDetailVO.getCreateTime() != null && !majorDetailVO.getCreateTime().isEmpty()) {
                majorResponseDTO.setCreateTime(LocalDateTime.parse(majorDetailVO.getCreateTime(), DATE_TIME_FORMATTER));
            }
            if (majorDetailVO.getUpdateTime() != null && !majorDetailVO.getUpdateTime().isEmpty()) {
                majorResponseDTO.setUpdateTime(LocalDateTime.parse(majorDetailVO.getUpdateTime(), DATE_TIME_FORMATTER));
            }
        } catch (Exception e) {
            log.error("时间格式转换失败：{}", e.getMessage());
        }

        return majorResponseDTO;
    }
}
