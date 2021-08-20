package com.qiyuan.dao;

import com.qiyuan.entity.User;
import org.apache.ibatis.annotations.Param;

public interface UserMapper {
    // 根据 id 查询用户，最好把注解加上（规范）
    User queryUserById(@Param("id") int id);
    // 修改用户，更新了数据库
    int updateUser(User user);
}
