package com.qiyuan.dao;

import com.qiyuan.entity.User;
import com.qiyuan.utils.MyBatisUtils;
import org.apache.ibatis.session.SqlSession;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @ClassName UserDaoTest
 * @Description TODO
 * @Author Qiyuan
 * @Date 2021/8/11 21:20
 * @Version 1.0
 **/
public class UserMapperTest {
    @Test
    public void Test(){
        // 第一步 获取sqlSession对象，相当于connection
        //SqlSession sqlSession = MyBatisUtils.getSqlSession();

        // 使用try-with-resources自动关闭资源
        try (SqlSession sqlSession = MyBatisUtils.getSqlSession()) {
            // 方式一：sqlSession.getMapper 这里应该叫做UserMapper
            UserMapper mapper = sqlSession.getMapper(UserMapper.class);
            // 可以调用接口中被UserMapper实例化后的方法
            List<User> userList = mapper.getUserList();

            // 方法二：sqlSession.select...() 已经不用了
            // 直接把方法路径传进去，并且要select对应的类型 one list map...
            //List<User> userList = sqlSession.selectList("com.qiyuan.dao.UserDao.getUserList");

            for (User user : userList) {
                System.out.println(user);
            }
        }
    }

    @Test
    public void getUserById(){
        // 获取sqlSession
        SqlSession sqlSession = MyBatisUtils.getSqlSession();
        // 加载/实现接口
        UserMapper mapper = sqlSession.getMapper(UserMapper.class);
        // 调用接口中的方法
        User user = mapper.getUserById(1);
        System.out.println(user);
        // 关闭sqlSession
        sqlSession.close();
    }

    @Test
    public void getUserLike(){
        SqlSession sqlSession = MyBatisUtils.getSqlSession();
        UserMapper mapper = sqlSession.getMapper(UserMapper.class);

        List<User> userList = mapper.getUserLike("%祈%");
        for (User user : userList) {
            System.out.println(user);
        }
        sqlSession.close();
    }

    // 增删改需要提交事务了！
    @Test
    public void addUser(){
        // 1
        SqlSession sqlSession = MyBatisUtils.getSqlSession();
        // 2
        UserMapper mapper = sqlSession.getMapper(UserMapper.class);
        // 调用接口中的方法
        mapper.addUser(new User(4,"祈鸢ccc","123456"));
        // 提交事务！不提交修改无效！
        sqlSession.commit();
        // 3
        sqlSession.close();
        // 三句话完全不用变！
    }

    @Test
    public void addUser2(){
        // 获取连接部分
        SqlSession sqlSession = MyBatisUtils.getSqlSession();
        UserMapper mapper = sqlSession.getMapper(UserMapper.class);

        // 配置参数部分
        Map<String, Object> map = new HashMap<>();
        map.put("userid",5);
        map.put("userName","祈鸢bbb");
        map.put("password","123123");

        // 调用方法部分
        mapper.addUser2(map);
        sqlSession.commit();

        // 关闭连接部分
        sqlSession.close();
    }

    @Test
    public void deleteUser(){
        // 首先写三步走！
        SqlSession sqlSession = MyBatisUtils.getSqlSession();
        UserMapper mapper = sqlSession.getMapper(UserMapper.class);
        // 调用方法，修改用户信息
        mapper.deleteUser(4);
        // 提交事务不要忘
        sqlSession.commit();
        sqlSession.close();
    }

    @Test
    public void updateUser(){
        // 首先写三步走！
        SqlSession sqlSession = MyBatisUtils.getSqlSession();
        UserMapper mapper = sqlSession.getMapper(UserMapper.class);
        // 调用方法，修改用户信息
        mapper.updateUser(new User(4,"qiyuanbbb","123123"));
        // 提交事务不要忘
        sqlSession.commit();
        sqlSession.close();
    }
}
