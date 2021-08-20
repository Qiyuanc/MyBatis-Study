package com.qiyuan.dao;

import com.qiyuan.entity.User;

public interface UserMapper {
    // 根据ID查询用户
    public User getUserById(int id);
}
