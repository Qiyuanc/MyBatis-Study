package com.qiyuan.dao;

import com.qiyuan.entity.User;
import com.qiyuan.utils.MyBatisUtils;
import org.apache.ibatis.session.SqlSession;
import org.junit.jupiter.api.Test;

import java.util.List;

/**
 * @ClassName UserDaoTest
 * @Description TODO
 * @Author Qiyuan
 * @Date 2021/8/11 21:20
 * @Version 1.0
 **/
public class UserMapperTest {

    @Test
    public void getUser(){
        // 使用注解的方式 其实没区别
        SqlSession sqlSession = MyBatisUtils.getSqlSession();
        UserMapper mapper = sqlSession.getMapper(UserMapper.class);
        List<User> userList = mapper.getUser();
        for (User user : userList) {
            System.out.println(user);
        }
        sqlSession.close();
    }

    @Test
    public void getUserById(){
        SqlSession sqlSession = MyBatisUtils.getSqlSession();
        UserMapper mapper = sqlSession.getMapper(UserMapper.class);
        User user = mapper.getUserById(1);
        System.out.println(user);
        sqlSession.close();
    }

    @Test
    public void addUser(){
        SqlSession sqlSession = MyBatisUtils.getSqlSession();
        UserMapper mapper = sqlSession.getMapper(UserMapper.class);
        int i = mapper.addUser(new User(6, "qqqiyuan", "666666"));
        System.out.println(i);
        // 设置 sqlSession 自动提交事务 这里就不用提交了
        //sqlSession.commit();
        sqlSession.close();
    }

    @Test
    public void updateUser(){
        SqlSession sqlSession = MyBatisUtils.getSqlSession();
        UserMapper mapper = sqlSession.getMapper(UserMapper.class);
        int i = mapper.updateUser(new User(6, "qqqiyuan", "777777"));
        System.out.println(i);
        // 设置 sqlSession 自动提交事务 这里就不用提交了
        //sqlSession.commit();
        sqlSession.close();
    }

    @Test
    public void deleteUser(){
        SqlSession sqlSession = MyBatisUtils.getSqlSession();
        UserMapper mapper = sqlSession.getMapper(UserMapper.class);
        int i = mapper.deleteUser(6);
        System.out.println(i);
        // 设置 sqlSession 自动提交事务 这里就不用提交了
        //sqlSession.commit();
        sqlSession.close();
    }
}
