package com.qiyuan.dao;

import com.qiyuan.entity.User;
import com.qiyuan.utils.MyBatisUtils;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.session.SqlSession;
import org.apache.log4j.Logger;
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

    // 要经常使用，提升作用域
    static Logger logger = Logger.getLogger(UserMapperTest.class);

    @Test
    public void Test(){
        // 获取sqlSession对象 相当于connection
        SqlSession sqlSession = MyBatisUtils.getSqlSession();
        // sqlSession.getMapper
        UserMapper mapper = sqlSession.getMapper(UserMapper.class);
        // 调用接口中被UserMapper实例化后的方法
        User user = mapper.getUserById(1);
        System.out.println(user);

        sqlSession.close();
    }

    @Test
    public void TestLog4j(){
        // 输出的参数类型是 Object
        // 用于输出普通信息，如 SQL 语句
        logger.info("info：info:这是一条 info 信息");
        // 用于 debug 某处是否有问题
        logger.debug("debug：这是一条 debug 信息");
        // 类似 try-catch 获取错误信息
        logger.error("error：这是一条 error 信息");
    }

    @Test
    public void getUserByLimit(){
        SqlSession sqlSession = MyBatisUtils.getSqlSession();
        UserMapper mapper = sqlSession.getMapper(UserMapper.class);
        Map<String, Object> map = new HashMap<String, Object>();
        // map 的 key 要和 SQL 中使用的名字相同！
        map.put("name","%祈%");
        map.put("startIndex",Integer.valueOf(0));
        map.put("pageSize",Integer.valueOf(2));
        List<User> userList = mapper.getUserByLimit(map);
        for (User user : userList) {
            System.out.println(user);
        }
        sqlSession.close();
    }

    @Test
    public void getUserByRowBounds(){
        SqlSession sqlSession = MyBatisUtils.getSqlSession();
        // 从第1条记录开始取2条记录
        RowBounds rowBounds = new RowBounds(1, 2);
        // sqlSession实例化接口的第二种方式 好麻烦
        List<User> userList = sqlSession.selectList("com.qiyuan.dao.UserMapper.getUserByRowBounds",null,rowBounds);
        for (User user : userList) {
            System.out.println(user);
        }
        sqlSession.close();
    }
}
