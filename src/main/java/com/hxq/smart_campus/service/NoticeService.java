package com.hxq.smart_campus.service;

import com.github.pagehelper.PageInfo;
import com.hxq.smart_campus.entity.dto.NoticeCreateDTO;
import com.hxq.smart_campus.entity.dto.NoticeResponseDTO;
import com.hxq.smart_campus.entity.dto.NoticeUpdateDTO;
import com.hxq.smart_campus.entity.vo.MyNoticeVO;
import com.hxq.smart_campus.entity.vo.NoticeDetailVO;
import com.hxq.smart_campus.entity.vo.NoticeListVO;

public interface NoticeService {
    /**
     * 发布通知
     * @param noticeCreateDTO
     * @return
     */
    NoticeResponseDTO insertNotice(NoticeCreateDTO noticeCreateDTO);

    /**
     * 更新通知
     * @param id
     * @param noticeUpdateDTO
     * @return
     */
    NoticeResponseDTO updateNotice(Long id, NoticeUpdateDTO noticeUpdateDTO);

    /**
     * 撤回通知
     * @param id
     * @return
     */
    Boolean withdrawNotice(Long id);

    /**
     * 删除通知
     * @param id
     * @return
     */
    Boolean deleteNotice(Long id);

    /**
     * 获取通知列表
     * @param pageNum
     * @param pageSize
     * @param title
     * @param type
     * @param status
     * @return
     */
    PageInfo<NoticeListVO> getNoticeList(Integer pageNum, Integer pageSize, String title, String type, String status);

    /**
     * 获取通知详情
     * @param id
     * @return
     */
    NoticeDetailVO getNoticeDetail(Long id);

    /**
     * 获取我的通知列表
     * @param pageNum
     * @param pageSize
     * @return
     */
    PageInfo<MyNoticeVO> getMyNoticeList(Integer pageNum, Integer pageSize);

    /**
     * 获取未读通知数
     * @return
     */
    Long getUnreadCount();
}
