package com.hxq.smart_campus.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.hxq.smart_campus.entity.dto.CollegeCreateDTO;
import com.hxq.smart_campus.entity.dto.CollegeResponseDTO;
import com.hxq.smart_campus.entity.dto.CollegeUpdateDTO;
import com.hxq.smart_campus.entity.vo.CollegeDetailVO;
import com.hxq.smart_campus.entity.vo.CollegeListVO;
import com.hxq.smart_campus.mapper.CollegeMapper;
import com.hxq.smart_campus.service.CollegeService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

import static com.hxq.smart_campus.constant.MessageConstant.*;

@AllArgsConstructor
@Slf4j
@Service
public class CollegeServiceImpl implements CollegeService {

    private final CollegeMapper collegeMapper;

    /**
     * 获取学院列表
     *
     * @param pageNum       页码
     * @param pageSize      每页数量
     * @param collegeName   学院名称
     * @param status        状态
     * @return 学院列表
     */
    @Override
    public PageInfo<CollegeListVO> getCollegeList(Integer pageNum, Integer pageSize, String collegeName, String status) {
        log.info("获取学院列表，页码：{}，每页数量：{}，学院名称：{}，状态：{}", pageNum, pageSize, collegeName, status);
        
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
        List<CollegeListVO> collegeList = collegeMapper.getCollegeList(collegeName, status);
        PageInfo<CollegeListVO> pageInfo = PageInfo.of(collegeList);
        
        log.info("获取学院列表成功，总记录数：{}，总页数：{}", pageInfo.getTotal(), pageInfo.getPages());
        return pageInfo;
    }
    /**
     * 新增学院
     *
     * @param collegeCreateDTO 学院创建DTO
     * @return 学院响应DTO
     */
    @Override
    public CollegeResponseDTO createCollege(CollegeCreateDTO collegeCreateDTO) {
        log.info("新增学院：{}", collegeCreateDTO);
        
        // 参数校验
        if (collegeCreateDTO == null) {
            log.warn("新增学院失败：参数为空");
            throw new IllegalArgumentException("参数不能为空");
        }
        if (collegeCreateDTO.getCollegeCode() == null || collegeCreateDTO.getCollegeCode().trim().isEmpty()) {
            log.warn("新增学院失败：学院代码不能为空");
            throw new IllegalArgumentException("学院代码不能为空");
        }
        if (collegeCreateDTO.getCollegeName() == null || collegeCreateDTO.getCollegeName().trim().isEmpty()) {
            log.warn("新增学院失败：学院名称不能为空");
            throw new IllegalArgumentException("学院名称不能为空");
        }
        
        // 执行新增操作
        int result = collegeMapper.insert(collegeCreateDTO);
        if (result <= 0) {
            // 出现异常
            log.error("新增学院失败：数据库操作失败");
            throw new RuntimeException("新增学院失败");
        }
        
        // 获取新增学院的ID
        Long newCollegeId = collegeMapper.getLastInsertId();
        log.info("新增学院成功，ID：{}", newCollegeId);
        
        // 新增成功后，查询新增学院的详情
        CollegeDetailVO collegeDetailVO = collegeMapper.getCollegeDetail(newCollegeId);
        return convertToResponseDTO(collegeDetailVO);
    }
    /**
     * 更新学院
     *
     * @param collegeUpdateDTO 更新学院参数
     * @return 更新学院响应
     */
    @Override
    public CollegeResponseDTO updateCollege(CollegeUpdateDTO collegeUpdateDTO) {
        log.info("更新学院：{}", collegeUpdateDTO);
        
        // 参数校验
        if (collegeUpdateDTO == null) {
            log.warn("更新学院失败：参数为空");
            throw new IllegalArgumentException("参数不能为空");
        }
        if (collegeUpdateDTO.getId() == null) {
            log.warn("更新学院失败：ID为空");
            throw new IllegalArgumentException("ID不能为空");
        }
        
        // 检查学院是否存在
        CollegeDetailVO existingCollege = collegeMapper.getCollegeDetail(collegeUpdateDTO.getId());
        if (existingCollege == null) {
            log.warn("更新学院失败：学院不存在，ID={}", collegeUpdateDTO.getId());
            throw new RuntimeException("学院不存在");
        }
        
        // 执行更新操作
        int result = collegeMapper.update(collegeUpdateDTO);
        if (result <= 0) {
            // 出现异常
            log.error("更新学院失败：数据库操作失败");
            throw new RuntimeException("更新学院失败");
        }
        
        log.info("更新学院成功，ID：{}", collegeUpdateDTO.getId());
        
        // 更新成功后，查询更新学院的详情
        CollegeDetailVO collegeDetailVO = collegeMapper.getCollegeDetail(collegeUpdateDTO.getId());
        return convertToResponseDTO(collegeDetailVO);
    }
    /**
     * 删除学院
     *
     * @param ids 学院ID列表
     * @return 删除结果
     */
    @Override
    public boolean deleteCollege(List<Long> ids) {
        log.info("删除学院，ID列表：{}", ids);
        
        // 参数校验
        if (ids == null || ids.isEmpty()) {
            log.warn("删除学院失败：ID列表为空");
            throw new IllegalArgumentException("ID列表不能为空");
        }
        
        // 遍历ids，检查是否存在该学院以及学院的状态
        for (Long id : ids) {
            CollegeDetailVO collegeDetailVO = collegeMapper.getCollegeDetail(id);
            if (collegeDetailVO == null) {
                // 学院不存在
                log.warn("删除学院失败：学院不存在，ID={}", id);
                throw new RuntimeException("学院不存在");
            }
            // 检查学院的状态是否为禁用
            if (COLLEGE_STATUS_DISABLED.equals(collegeDetailVO.getStatus())) {
                // 学院状态异常
                log.warn("删除学院失败：学院状态异常，ID={}, 状态={}", id, collegeDetailVO.getStatus());
                throw new RuntimeException("学院状态异常，不能删除");
            }
        }
        
        int result = collegeMapper.deleteBatch(ids);
        if (result <= 0) {
            // 出现异常
            log.error("删除学院失败：数据库操作失败");
            throw new RuntimeException("删除学院失败");
        }
        
        log.info("删除学院成功，删除数量：{}", result);
        // 删除成功后，返回true
        return true;
    }
    /**
     * 更新学院状态
     *
     * @param id 学院ID
     * @return 更新结果
     */
    @Override
    public boolean updateStatus(Long id) {
        log.info("更新学院状态，ID：{}", id);
        
        // 参数校验
        if (id == null) {
            log.warn("更新学院状态失败：ID为空");
            throw new IllegalArgumentException("ID不能为空");
        }
        
        // 检查学院是否存在
        CollegeDetailVO collegeDetailVO = collegeMapper.getCollegeDetail(id);
        if (collegeDetailVO == null) {
            // 学院不存在
            log.warn("更新学院状态失败：学院不存在，ID={}", id);
            throw new RuntimeException("学院不存在");
        }
        
        // 获取学院的旧状态
        String oldStatus = collegeDetailVO.getStatus();
        String newStatus;
        // 检查学院的状态是否为禁用
        if (COLLEGE_STATUS_DISABLED.equals(collegeDetailVO.getStatus())) {
            // 禁用状态，更新为启用
            newStatus = COLLEGE_STATUS_ENABLED;
        } else {
            // 启用状态，更新为禁用
            newStatus = COLLEGE_STATUS_DISABLED;
        }
        
        log.info("学院状态更新，ID：{}，旧状态：{}，新状态：{}", id, oldStatus, newStatus);
        
        // 更新学院状态
        int result = collegeMapper.updateStatus(id, newStatus);
        if (result <= 0) {
            // 出现异常
            log.error("更新学院状态失败：数据库操作失败");
            throw new RuntimeException("更新学院状态失败");
        }
        
        log.info("更新学院状态成功，ID：{}", id);
        // 更新成功后，返回true
        return true;
    }
    /**
     * 获取学院详情
     *
     * @param id 学院ID
     * @return 学院详情
     */
    @Override
    public CollegeDetailVO getCollegeDetail(Long id) {
        if (id == null) {
            log.warn("获取学院详情失败：ID为空");
            throw new IllegalArgumentException("ID不能为空");
        }
        CollegeDetailVO collegeDetailVO = collegeMapper.getCollegeDetail(id);
        if (collegeDetailVO == null) {
            log.warn("获取学院详情失败：学院不存在，ID={}", id);
            throw new RuntimeException("学院不存在");
        }
        return collegeDetailVO;
    }

    /**
     * 转换学院详情为响应DTO
     *
     * @param collegeDetailVO 学院详情
     * @return 学院响应DTO
     */
    private CollegeResponseDTO convertToResponseDTO(CollegeDetailVO collegeDetailVO) {
        CollegeResponseDTO responseDTO = new CollegeResponseDTO();
        responseDTO.setId(collegeDetailVO.getId());
        responseDTO.setCollegeCode(collegeDetailVO.getCollegeCode());
        responseDTO.setCollegeName(collegeDetailVO.getCollegeName());
        responseDTO.setDean(collegeDetailVO.getDean());
        responseDTO.setContactPhone(collegeDetailVO.getContactPhone());
        responseDTO.setStatus(collegeDetailVO.getStatus());
        try {
            // 使用正确的时间格式解析
            java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            responseDTO.setCreateTime(LocalDateTime.parse(collegeDetailVO.getCreateTime(), formatter));
            responseDTO.setUpdateTime(LocalDateTime.parse(collegeDetailVO.getUpdateTime(), formatter));
        } catch (Exception e) {
            // 处理异常
            e.printStackTrace();
        }
        return responseDTO;
    }
}
