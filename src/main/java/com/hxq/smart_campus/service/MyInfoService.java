package com.hxq.smart_campus.service;

import com.hxq.smart_campus.entity.info.MyInfo;

public interface MyInfoService {
    /**
     * 获取当前用户信息
     * @param id 用户ID
     * @return
     */
    MyInfo getMyInfo();
}
