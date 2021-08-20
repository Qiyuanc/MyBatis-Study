package com.qiyuan.dao;

import com.qiyuan.entity.User;

import java.util.List;

public interface UserMapper {
    // 查询全部用户
    public List<User> getUserList();
    // 根须ID查询用户
    public User getUserById(int id);
    // 新增用户
    public int addUser(User user);
    // 删除用户，根据id删
    public int deleteUser(int id);
    // 修改用户信息
    public int updateUser(User user);
}
