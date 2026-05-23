package com.hxq.smart_campus.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.hxq.smart_campus.entity.dto.NoticeCreateDTO;
import com.hxq.smart_campus.entity.dto.NoticeResponseDTO;
import com.hxq.smart_campus.entity.dto.NoticeUpdateDTO;
import com.hxq.smart_campus.entity.vo.*;
import com.hxq.smart_campus.exception.BusinessException;
import com.hxq.smart_campus.mapper.*;
import com.hxq.smart_campus.service.NoticeService;
import com.hxq.smart_campus.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.hxq.smart_campus.constant.MessageConstant.*;
import static com.hxq.smart_campus.constant.RedisConstant.MY_NOTICE_KEY_PREFIX;
import static com.hxq.smart_campus.constant.RedisConstant.NOTICE_KEY_PREFIX;

@Service
@Slf4j
@RequiredArgsConstructor
public class NoticeServiceImpl implements NoticeService {
    private final NoticeMapper noticeMapper;
    private final StudentMapper studentMapper;
    private final TeacherMapper teacherMapper;
    private final MajorMapper majorMapper;
    private final CollegeMapper collegeMapper;
    private final ClassMapper classMapper;

    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 发布通知
     * @param noticeCreateDTO
     * @return
     */
    @Override
    public NoticeResponseDTO insertNotice(NoticeCreateDTO noticeCreateDTO) {
        if (noticeCreateDTO == null) {
            throw new BusinessException("参数错误！");
        }
        Long publisherId = SecurityUtils.getCurrentUserId();
        noticeCreateDTO.setPublisherId(publisherId);

        resolveTargetName(noticeCreateDTO);

        int result = noticeMapper.insertNotice(noticeCreateDTO);
        if (result == 0) {
            throw new BusinessException("发布失败！");
        }

        Long id = noticeCreateDTO.getId();
        if (id == null || id <= 0) {
            throw new BusinessException("发布失败");
        }

        evictNoticeCache();

        NoticeDetailVO noticeDetailVO = noticeMapper.getNoticeDetail(id);
        if (noticeDetailVO == null) {
            throw new BusinessException("发布失败");
        }
        return convertToResponseDTO(noticeDetailVO);
    }

    /**
     * 更新通知
     * @param id
     * @param noticeUpdateDTO
     * @return
     */
    @Override
    public NoticeResponseDTO updateNotice(Long id, NoticeUpdateDTO noticeUpdateDTO) {
        if (noticeUpdateDTO == null) {
            throw new BusinessException("参数错误！");
        }
        NoticeDetailVO notice = noticeMapper.getNoticeDetail(id);
        if (notice == null) {
            throw new BusinessException("通知不存在！");
        }
        if (!notice.getPublisherId().equals(SecurityUtils.getCurrentUserId())) {
            throw new BusinessException("无权修改他人发布的通知");
        }
        if (!NOTICE_STATUS_PUBLISHED.equals(notice.getStatus())) {
            throw new BusinessException("只能修改已发布的通知");
        }

        noticeUpdateDTO.setId(id);
        resolveTargetName(noticeUpdateDTO);

        int result = noticeMapper.updateNotice(noticeUpdateDTO);
        if (result == 0) {
            throw new BusinessException("修改失败！");
        }

        evictNoticeCache();

        NoticeDetailVO noticeDetailVO = noticeMapper.getNoticeDetail(id);
        if (noticeDetailVO == null) {
            throw new BusinessException("修改失败");
        }
        return convertToResponseDTO(noticeDetailVO);
    }

    /**
     * 撤回通知
     * @param id
     * @return
     */
    @Override
    public Boolean withdrawNotice(Long id) {
        if (id == null) {
            throw new BusinessException("参数错误！");
        }
        NoticeDetailVO notice = noticeMapper.getNoticeDetail(id);
        if (notice == null) {
            throw new BusinessException("通知不存在！");
        }
        if (!notice.getPublisherId().equals(SecurityUtils.getCurrentUserId())) {
            throw new BusinessException("无权撤回他人发布的通知");
        }
        if (!NOTICE_STATUS_PUBLISHED.equals(notice.getStatus())) {
            throw new BusinessException("该通知已撤回");
        }
        int result = noticeMapper.withdrawNotice(id);
        if (result == 0) {
            throw new BusinessException("撤回失败！");
        }

        evictNoticeCache();

        return true;
    }

    /**
     * 删除通知
     * @param id
     * @return
     */
    @Override
    public Boolean deleteNotice(Long id) {
        if (id == null) {
            throw new BusinessException("参数错误！");
        }
        NoticeDetailVO notice = noticeMapper.getNoticeDetail(id);
        if (notice == null) {
            throw new BusinessException("通知不存在！");
        }
        if (!notice.getPublisherId().equals(SecurityUtils.getCurrentUserId())) {
            throw new BusinessException("无权删除他人发布的通知");
        }
        int result = noticeMapper.deleteNotice(id);
        if (result == 0) {
            throw new BusinessException("删除失败！");
        }

        evictNoticeCache();

        return true;
    }

    /**
     * 获取通知列表
     * @param pageNum
     * @param pageSize
     * @param title
     * @param targetType
     * @param status
     * @return
     */
    @Override
    public PageInfo<NoticeListVO> getNoticeList(Integer pageNum, Integer pageSize, String title, String targetType, String status) {
        String cacheKey = NOTICE_KEY_PREFIX + "list:" +
                (title != null ? title : "all") + ":" +
                (targetType != null ? targetType : "all") + ":" +
                (status != null ? status : "all");

        List<NoticeListVO> noticeListVOList = (List<NoticeListVO>) redisTemplate.opsForValue().get(cacheKey);
        if (noticeListVOList != null && !noticeListVOList.isEmpty()) {
            int total = noticeListVOList.size();
            int fromIndex = (pageNum - 1) * pageSize;
            if (fromIndex >= total) {
                return new PageInfo<>(new ArrayList<>());
            }
            int toIndex = Math.min(fromIndex + pageSize, total);
            List<NoticeListVO> pageList = noticeListVOList.subList(fromIndex, toIndex);

            PageInfo<NoticeListVO> pageInfo = new PageInfo<>(pageList);
            pageInfo.setTotal(total);
            pageInfo.setPageNum(pageNum);
            pageInfo.setPageSize(pageSize);
            pageInfo.setPages((total + pageSize - 1) / pageSize);
            return pageInfo;
        }

        PageHelper.startPage(pageNum, pageSize);
        noticeListVOList = noticeMapper.getNoticeList(title, targetType, status);
        PageInfo<NoticeListVO> pageInfo = new PageInfo<>(noticeListVOList);

        if (noticeListVOList != null && !noticeListVOList.isEmpty() && pageNum == 1) {
            redisTemplate.opsForValue().set(cacheKey, noticeListVOList, 5, TimeUnit.MINUTES);
        }

        return pageInfo;
    }

    /**
     * 获取通知详情
     * @param id
     * @return
     */
    @Override
    public NoticeDetailVO getNoticeDetail(Long id) {
        if (id == null) {
            throw new BusinessException("参数错误！");
        }
        NoticeDetailVO noticeDetailVO = noticeMapper.getNoticeDetail(id);
        if (noticeDetailVO == null) {
            throw new BusinessException("通知不存在");
        }
        return noticeDetailVO;
    }

    /**
     * 获取我的通知列表
     * @param pageNum
     * @param pageSize
     * @return
     */
    @Override
    public PageInfo<MyNoticeVO> getMyNoticeList(Integer pageNum, Integer pageSize) {
        String userType = SecurityUtils.getCurrentUserType();
        if (userType == null) {
            throw new BusinessException("请用户先登陆");
        }

        String userIdentifier;
        String className = null;
        String majorName = null;
        String collegeName = null;

        if (USER_TYPE_STUDENT.equals(userType)) {
            log.info("查询学生通知: userId={}", SecurityUtils.getCurrentUserId());
            Long studentId = SecurityUtils.getCurrentUserId();
            if (studentId == null) {
                throw new BusinessException("请用户先登陆");
            }
            StudentDetailVO studentDetailVO = studentMapper.getStudentDetail(studentId);
            if (studentDetailVO == null) {
                throw new BusinessException("学生不存在");
            }
            MajorDetailVO majorDetailVO = majorMapper.getMajorIdByClassId(studentDetailVO.getClassId());
            if (majorDetailVO == null) {
                throw new BusinessException("专业不存在");
            }
            userIdentifier = studentDetailVO.getStudentNo();
            className = studentDetailVO.getClassName();
            majorName = majorDetailVO.getMajorName();
            collegeName = majorDetailVO.getCollegeName();
        } else if (USER_TYPE_TEACHER.equals(userType)) {
            log.info("查询教师通知: userId={}", SecurityUtils.getCurrentUserId());
            TeacherDetailVO teacherDetailVO = teacherMapper.getTeacherDetail(SecurityUtils.getCurrentUserId());
            if (teacherDetailVO == null) {
                throw new BusinessException("教师不存在");
            }
            userIdentifier = teacherDetailVO.getTeacherNo();
            collegeName = teacherDetailVO.getCollegeName();
        } else {
            return new PageInfo<>(new ArrayList<>());
        }

        String cacheKey = MY_NOTICE_KEY_PREFIX + userIdentifier;
        List<MyNoticeVO> myNoticeVOList = (List<MyNoticeVO>) redisTemplate.opsForValue().get(cacheKey);
        if (myNoticeVOList != null && !myNoticeVOList.isEmpty()) {
            int total = myNoticeVOList.size();
            int fromIndex = (pageNum - 1) * pageSize;
            if (fromIndex >= total) {
                return new PageInfo<>(new ArrayList<>());
            }
            int toIndex = Math.min(fromIndex + pageSize, total);
            List<MyNoticeVO> pageList = myNoticeVOList.subList(fromIndex, toIndex);

            PageInfo<MyNoticeVO> pageInfo = new PageInfo<>(pageList);
            pageInfo.setTotal(total);
            pageInfo.setPageNum(pageNum);
            pageInfo.setPageSize(pageSize);
            pageInfo.setPages((total + pageSize - 1) / pageSize);
            return pageInfo;
        }

        PageHelper.startPage(pageNum, pageSize);
        myNoticeVOList = noticeMapper.getMyNoticeList(className, majorName, collegeName);
        PageInfo<MyNoticeVO> pageInfo = new PageInfo<>(myNoticeVOList);

        if (myNoticeVOList != null && !myNoticeVOList.isEmpty()) {
            redisTemplate.opsForValue().set(cacheKey, myNoticeVOList, 1, TimeUnit.DAYS);
        }

        return pageInfo;
    }

    /**
     * 解析发布范围名称
     * @param dto
     */
    private void resolveTargetName(NoticeCreateDTO dto) {
        String targetType = dto.getTargetType();
        Long targetId = dto.getTargetId();

        if ("全部".equals(targetType)) {
            dto.setTargetId(null);
            dto.setTargetName("全部");
            return;
        }

        if (targetId == null) {
            throw new BusinessException("请指定发布范围");
        }

        switch (targetType) {
            case "学院":
                CollegeDetailVO college = collegeMapper.getCollegeDetail(targetId);
                if (college == null) {
                    throw new BusinessException("学院不存在");
                }
                dto.setTargetName(college.getCollegeName());
                break;
            case "专业":
                MajorDetailVO major = majorMapper.getMajorDetail(targetId);
                if (major == null) {
                    throw new BusinessException("专业不存在");
                }
                dto.setTargetName(major.getMajorName());
                break;
            case "班级":
                ClassDetailVO clazz = classMapper.getClassDetail(targetId);
                if (clazz == null) {
                    throw new BusinessException("班级不存在");
                }
                dto.setTargetName(clazz.getClassName());
                break;
            default:
                throw new BusinessException("无效的发布范围类型: " + targetType);
        }
    }

    /**
     * 解析更新发布范围名称
     * @param dto
     */
    private void resolveTargetName(NoticeUpdateDTO dto) {
        String targetType = dto.getTargetType();
        Long targetId = dto.getTargetId();

        if (targetType == null) {
            return;
        }

        if ("全部".equals(targetType)) {
            dto.setTargetId(null);
            dto.setTargetName("全部");
            return;
        }

        if (targetId == null) {
            throw new BusinessException("请指定发布范围");
        }

        switch (targetType) {
            case "学院":
                CollegeDetailVO college = collegeMapper.getCollegeDetail(targetId);
                if (college == null) {
                    throw new BusinessException("学院不存在");
                }
                dto.setTargetName(college.getCollegeName());
                break;
            case "专业":
                MajorDetailVO major = majorMapper.getMajorDetail(targetId);
                if (major == null) {
                    throw new BusinessException("专业不存在");
                }
                dto.setTargetName(major.getMajorName());
                break;
            case "班级":
                ClassDetailVO clazz = classMapper.getClassDetail(targetId);
                if (clazz == null) {
                    throw new BusinessException("班级不存在");
                }
                dto.setTargetName(clazz.getClassName());
                break;
            default:
                throw new BusinessException("无效的发布范围类型: " + targetType);
        }
    }

    /**
     * 清除通知缓存
     */
    private void evictNoticeCache() {
        Set<String> noticeKeys = redisTemplate.keys(NOTICE_KEY_PREFIX + "*");
        if (noticeKeys != null && !noticeKeys.isEmpty()) {
            redisTemplate.delete(noticeKeys);
        }
        Set<String> myNoticeKeys = redisTemplate.keys(MY_NOTICE_KEY_PREFIX + "*");
        if (myNoticeKeys != null && !myNoticeKeys.isEmpty()) {
            redisTemplate.delete(myNoticeKeys);
        }
        log.info("通知缓存已清除");
    }

    /**
     * 转换通知详情VO为通知响应DTO
     * @param noticeDetailVO
     * @return
     */
    private NoticeResponseDTO convertToResponseDTO(NoticeDetailVO noticeDetailVO) {
        NoticeResponseDTO noticeResponseDTO = new NoticeResponseDTO();
        noticeResponseDTO.setId(noticeDetailVO.getId());
        noticeResponseDTO.setTitle(noticeDetailVO.getTitle());
        noticeResponseDTO.setContent(noticeDetailVO.getContent());
        noticeResponseDTO.setPublisherId(noticeDetailVO.getPublisherId());
        noticeResponseDTO.setPublisherName(noticeDetailVO.getPublisherName());
        noticeResponseDTO.setPublishTime(noticeDetailVO.getPublishTime());
        noticeResponseDTO.setTargetType(noticeDetailVO.getTargetType());
        noticeResponseDTO.setTargetId(noticeDetailVO.getTargetId());
        noticeResponseDTO.setTargetName(noticeDetailVO.getTargetName());
        noticeResponseDTO.setStatus(noticeDetailVO.getStatus());
        try {
            if (noticeDetailVO.getCreateTime() != null && !noticeDetailVO.getCreateTime().isEmpty()) {
                noticeResponseDTO.setCreateTime(LocalDateTime.parse(noticeDetailVO.getCreateTime(), DATE_TIME_FORMATTER));
            }
            if (noticeDetailVO.getUpdateTime() != null && !noticeDetailVO.getUpdateTime().isEmpty()) {
                noticeResponseDTO.setUpdateTime(LocalDateTime.parse(noticeDetailVO.getUpdateTime(), DATE_TIME_FORMATTER));
            }
        } catch (Exception e) {
            throw new BusinessException("TIME_FORMAT_ERROR：", "时间解析错误", e);
        }
        return noticeResponseDTO;
    }
}
