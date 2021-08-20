package com.qiyuan.dao;

import com.qiyuan.entity.User;
import org.apache.ibatis.annotations.*;

import java.util.List;

public interface UserMapper {
    // 查询所有用户 注解实现
    @Select("select * from user")
    public List<User> getUser();

    // 方法有多个参数，要使用 @Param 使参数名对应
    @Select("select * from user where id = #{id}")
    public User getUserById(@Param("id") int id);

    // 不用 @param 直接传一个对象 也会自动取对象中的属性
    @Insert("insert into user(id,name,pwd) value(#{id},#{name},#{password})")
    public int addUser(User user);

    // 修改
    @Update("update user set name = #{name},pwd = #{password} where id = #{id}")
    public int updateUser(User user);

    // 删除
    @Delete("delete from user where id = #{id}")
    public int deleteUser(@Param("id") int id);
}
