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
    public void Test(){
        // 获取sqlSession对象 相当于connection
        SqlSession sqlSession = MyBatisUtils.getSqlSession();
        // sqlSession.getMapper
        UserMapper mapper = sqlSession.getMapper(UserMapper.class);
        // 调用接口中被UserMapper实例化后的方法
        List<User> userList = mapper.getUserList();

        for (User user : userList) {
            System.out.println(user);
        }
    }

}
