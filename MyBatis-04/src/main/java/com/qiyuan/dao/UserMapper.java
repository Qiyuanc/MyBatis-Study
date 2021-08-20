package com.qiyuan.dao;

import com.qiyuan.entity.User;

import java.util.List;
import java.util.Map;

public interface UserMapper {
    // 根据ID查询用户
    public User getUserById(int id);
    // 分页查询用户
    public List<User> getUserByLimit(Map<String,Object> map);
    // RowBounds分页 不用传页面的参数
    public List<User> getUserByRowBounds();
}
