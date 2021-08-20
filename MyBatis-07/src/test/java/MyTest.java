import com.qiyuan.dao.TeacherMapper;
import com.qiyuan.entity.Teacher;
import com.qiyuan.utils.MyBatisUtils;
import org.apache.ibatis.session.SqlSession;
import org.junit.jupiter.api.Test;

/**
 * @ClassName Test
 * @Description TODO
 * @Author Qiyuan
 * @Date 2021/8/16 23:25
 * @Version 1.0
 **/
public class MyTest {

    @Test
    public void getTeacher(){
        SqlSession sqlSession = MyBatisUtils.getSqlSession();
        TeacherMapper mapper = sqlSession.getMapper(TeacherMapper.class);
        Teacher teacher = mapper.getTeacher(1);
        System.out.println(teacher);
        sqlSession.close();
    }

    @Test
    public void getTeacher2(){
        SqlSession sqlSession = MyBatisUtils.getSqlSession();
        TeacherMapper mapper = sqlSession.getMapper(TeacherMapper.class);
        Teacher teacher = mapper.getTeacher2(1);
        System.out.println(teacher);
        sqlSession.close();
    }

}
