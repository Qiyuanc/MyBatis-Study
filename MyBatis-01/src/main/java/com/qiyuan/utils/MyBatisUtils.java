package com.qiyuan.utils;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.IOException;
import java.io.InputStream;

/**
 * @ClassName MyBatisUtils
 * @Description TODO
 * @Author Qiyuan
 * @Date 2021/8/11 17:17
 * @Version 1.0
 **/
public class MyBatisUtils {
    // 提升作用域
    private static SqlSessionFactory sqlSessionFactory;

    static {
        try {
            // 使用MyBatis第一步：获取SqlSessionFactory对象
            String resource = "mybatis-config.xml";
            // 要导org.apache.ibatis.io.Resources的包！ Maven犯病严重
            InputStream inputStream = Resources.getResourceAsStream(resource);
            sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 从SqlSessionFactory中获取SqlSession
    public static SqlSession getSqlSession(){
        // sqlSession 其实类似于 connection
        SqlSession sqlSession = sqlSessionFactory.openSession();
        return sqlSession;
    }
}
