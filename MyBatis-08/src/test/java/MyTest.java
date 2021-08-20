import com.qiyuan.dao.BlogMapper;
import com.qiyuan.entity.Blog;
import com.qiyuan.utils.IDUtils;
import com.qiyuan.utils.MyBatisUtils;
import org.apache.ibatis.session.SqlSession;
import org.junit.jupiter.api.Test;

import java.util.*;

/**
 * @ClassName MyTest
 * @Description TODO
 * @Author Qiyuan
 * @Date 2021/8/17 23:42
 * @Version 1.0
 **/
public class MyTest {
    @Test
    public void addBlogTest() {
        SqlSession sqlSession = MyBatisUtils.getSqlSession();
        BlogMapper mapper = sqlSession.getMapper(BlogMapper.class);
        Blog blog = new Blog();
        blog.setId(IDUtils.getId());
        blog.setTitle("Mybatis");
        blog.setAuthor("祈鸢");
        blog.setCreateTime(new Date());
        blog.setViews(1000);

        mapper.addBlog(blog);

        blog.setId(IDUtils.getId());
        blog.setTitle("Java");
        mapper.addBlog(blog);

        blog.setId(IDUtils.getId());
        blog.setTitle("Spring");
        mapper.addBlog(blog);

        blog.setId(IDUtils.getId());
        blog.setTitle("微服务");
        mapper.addBlog(blog);

        // 创建 SqlSession 时设置了打开事务提交，就不用 commit 了
        sqlSession.close();
    }

    @Test
    public void queryBlogIf(){
        SqlSession sqlSession = MyBatisUtils.getSqlSession();
        BlogMapper mapper = sqlSession.getMapper(BlogMapper.class);
        Map<String, String> map = new HashMap<String, String>();
        // 参数放进去
        //map.put("title","MyBatis");
        map.put("author","祈鸢ccc");

        List<Blog> blogs = mapper.queryBlogIf(map);
        for (Blog blog : blogs) {
            System.out.println(blog);
        }
        sqlSession.close();
    }

    @Test
    public void queryBlogChoose(){
        SqlSession sqlSession = MyBatisUtils.getSqlSession();
        BlogMapper mapper = sqlSession.getMapper(BlogMapper.class);
        Map<String, String> map = new HashMap<String, String>();
        // 参数放进去
        // map.put("title","MyBatis");
        // map.put("author","祈鸢");
        List<Blog> blogs = mapper.queryBlogChoose(map);
        for (Blog blog : blogs) {
            System.out.println(blog);
        }
        sqlSession.close();
    }

    @Test
    public void updateBlog(){
        SqlSession sqlSession = MyBatisUtils.getSqlSession();
        BlogMapper mapper = sqlSession.getMapper(BlogMapper.class);
        Map<String, String> map = new HashMap<String, String>();
        // 参数放进去，UUID 有点长。。。
        map.put("id","17ada90375174f63969fa69256019db8");
        map.put("title","MyBatisAfterUpdateTwice");
        // map.put("author","祈鸢ccc");

        System.out.println(mapper.updateBlog(map));
        sqlSession.close();
    }

    @Test
    public void queryBlogForeach(){
        SqlSession sqlSession = MyBatisUtils.getSqlSession();
        BlogMapper mapper = sqlSession.getMapper(BlogMapper.class);
        // 用 HashMap 存乱七八糟的东西
        HashMap map = new HashMap();
        // SQL 需要的 ids，这里是个 List，也可以是 Set Map
        ArrayList<Integer> ids = new ArrayList<Integer>();

        // 放要遍历的东西
        ids.add(1);
        ids.add(2);
        ids.add(3);
        map.put("ids",ids);

        List<Blog> blogs = mapper.queryBlogForeach(map);
        for (Blog blog : blogs) {
            System.out.println(blog);
        }
        sqlSession.close();
    }
}
