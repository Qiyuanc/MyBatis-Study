import com.qiyuan.dao.UserMapper;
import com.qiyuan.entity.User;
import com.qiyuan.utils.MyBatisUtils;
import org.apache.ibatis.session.SqlSession;
import org.junit.jupiter.api.Test;

/**
 * @ClassName MyTest
 * @Description TODO
 * @Author Qiyuan
 * @Date 2021/8/19 18:23
 * @Version 1.0
 **/
public class MyTest {

    @Test
    public void queryUserById(){
        SqlSession sqlSession = MyBatisUtils.getSqlSession();
        UserMapper mapper = sqlSession.getMapper(UserMapper.class);
        // 查询 id 为 1 的用户
        User user = mapper.queryUserById(1);
        System.out.println(user);
        System.out.println("---------------");
        // 修改一下 2 号用户信息
        mapper.updateUser(new User(2,"qiyuanc2","0723"));
        System.out.println("---------------");
        // 再查询一次 1 号用户
        User user1 = mapper.queryUserById(1);
        System.out.println(user1);
        System.out.println(user==user1);
        sqlSession.close();
    }

    @Test
    public void queryUserById2(){
        // 创建两个 sqlSession 对应两个会话
        SqlSession sqlSession1 = MyBatisUtils.getSqlSession();
        SqlSession sqlSession2 = MyBatisUtils.getSqlSession();
        // 用不同的 sqlSession 实例化接口
        UserMapper mapper1 = sqlSession1.getMapper(UserMapper.class);
        UserMapper mapper2 = sqlSession2.getMapper(UserMapper.class);

        // sqlSession1 查询 id 为 1 的用户
        User user1 = mapper1.queryUserById(1);
        System.out.println(user1);
        // 二级缓存是事务性的！若此处不关闭 1 则其中的一级缓存不会更新到二级缓存中
        sqlSession1.close();

        System.out.println("---------------");

        // sqlSession2 查询 id 为 1 的用户
        User user2 = mapper2.queryUserById(1);
        System.out.println(user2);
        sqlSession2.close();

        // 再比较一下
        System.out.println(user1==user2);
    }
}
