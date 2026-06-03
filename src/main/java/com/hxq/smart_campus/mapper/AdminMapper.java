package com.hxq.smart_campus.mapper;

import com.hxq.smart_campus.entity.vo.AdminDetailVO;
import com.hxq.smart_campus.entity.vo.AdminListVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AdminMapper {

    AdminDetailVO getAdminByAdminNo(@Param("adminNo") String adminNo);

    AdminDetailVO getAdminDetail(@Param("id") Long id);

    List<AdminListVO> getAdminList(@Param("name") String name,
                                   @Param("role") String role,
                                   @Param("adminNo") String adminNo);

    AdminDetailVO getCollegeAdminByCollegeId(@Param("collegeId") Long collegeId);
}