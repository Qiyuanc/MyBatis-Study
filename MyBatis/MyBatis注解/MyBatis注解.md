## MyBatis注解

本节新建 MyBatis-05 项目学习 MyBatis 注解。注解看起来的作用好像就是简化代码，那为什么要使用注解呢？这就得从面向接口编程讲起。

### 1.  面向接口编程

#### 1.1 什么叫面向接口编程

在一个面向对象的系统中，系统的各种功能是由许许多多的不同对象协作完成的。在这种情况下，各个对象内部是如何实现自己的,对系统设计人员来讲就不那么重要了；而各个对象之间的协作关系则成为系统设计的关键。

**采用面向接口编程的根本原因：解耦！**

#### 1.2 关于接口的理解

接口从更深层次的理解，应是定义（规范，约束）与实现（名实分离的原则）的分离，接口的本身反映了系统设计人员对系统的抽象理解。

接口应有两类：第一类是对一个个体的抽象，它可对应为一个抽象体( abstract class )；第二类是对一个个体某一方面的抽象，即形成一个抽象面（ interface ）。一个抽象体有可能有多个抽象面。

#### 1.3 三个面向的区别

- 面向对象：考虑问题时，以对象为单位，要考虑实例化对象的属性和方法　

- 面向过程：考虑问题时，以一个具体流程（事务过程）为单位，考虑它的实现

- 面向接口：本质上与面对过程和面对对象不是一个问题，更多的是考虑它整体的一个架构

### 2. 使用注解开发

因为使用注解，且注解是写在 Java 代码中的，所以只需要实体类即可，对应的 Mapper.xml 和其在 mybatis-config.xml 里的资源引用（参考 MyBatis配置 6.映射器）都不需要了。

在 UserMapper 中添加查询所有用户方法并使用注解

```java
public interface UserMapper {
    // 查询所有用户 注解实现
    @Select("select * from user")
    public List<User> getUser();
}
```

由于没有 UserMapper.xml 了，所以在 mybatis-config 中不能使用类路径的资源引用方式映射接口了

```xml
<!--不能用这种方式了-->
<mapper resource="com/qiyuan/dao/UserMapper.xml"/>
```

要改为使用映射器接口实现类的完全限定类名实现映射

```xml
<!--用这种方式才行-->
<mapper class="com.qiyuan.dao.UserMapper"/>
```

其实用起来和之前是没有区别的，写个测试类测试

```java
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
}
```

获得的结果

```java
User{id=1, name='祈鸢', password='null'}
User{id=2, name='Qiyuanc', password='null'}
User{id=3, name='風栖祈鸢', password='null'}
User{id=5, name='祈鸢bbb', password='null'}
```

可以发现，出现问题了。之前数据库字段和实体类属性对应不上的时候，可以在 Mapper.xml 中配置 resultMap 实现结果映射，但现在没有 Mapper.xml 了怎么办呢？

> 使用注解来映射简单语句会使代码显得更加简洁，但对于稍微复杂一点的语句，Java 注解不仅力不从心，还会让你本就复杂的 SQL 语句更加混乱不堪。 因此，如果你需要做一些很复杂的操作，最好用 XML 来映射语句。

或许注解在其他地方可以大显身手，但在 MyBatis 这里，官方建议，还是使用 xml 来配置吧。

**注解本质上是用反射机制实现的，底层用到了代理模式的动态代理。**目前对这两个概念还不是很理解，有空还是要去看看。

### 3. MyBatis执行流程

涉及到 MyBatis 源码的分析，目前来说有点深入看不懂···不过这个流程还是大致看明白了

![image-20210816160453132](F:\TyporaMD\MyBatis\MyBatis注解\image-20210816160453132.png)

### 4. 注解CRUD

**注意**：之前使用配置文件 Mapper.xml 时，要将配置文件注册到映射器中，现在虽然没有配置文件，但要将接口注册到映射器中！虽然上面说过了这里还要再强调一次！

#### 4.1 自动提交事务

通过设置 sqlSessionFactory.openSession() 的参数，可以让 MyBatis 自动提交事务

```java
// 源码
public SqlSession openSession(boolean autoCommit) {
    return this.openSessionFromDataSource(this.configuration.getDefaultExecutorType(), (TransactionIsolationLevel)null, autoCommit);
}
```

#### 4.2 查询

这里还要注意 @param 注解的使用，用于在有多个参数时，使 SQL 语句取出的参数与正确参数对应，即 #{ } 中的参数与 @param( ) 中的参数是对应的

```java
public interface UserMapper {
	...
    // 方法有多个参数，要使用 @Param 使参数名对应
    @Select("select * from user where id = #{id}")
    public User getUserById(@Param("id") int id);
}
```

测试方法这里就省略了，和之前没有区别。

#### 4.3 增加

这里有多个参数，使用之前的直接传一个对象的方式（能放在一个对象中为什么要分开呢），此时 #{ } 中的变量名要与对象中的属性一致

```java
public interface UserMapper {
	...
    // 不用 @param 直接传一个对象 也会自动取对象中的属性
    @Insert("insert into user(id,name,pwd) value(#{id},#{name},#{password})")
    public int addUser(User user);
}
```

设置了自动提交事务，这里就不用提交事务了

```java
public class UserMapperTest {
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
}
```

除了这个区别（开不开自动提交事务与是否使用注解无关）和之前的也是一样。

#### 4.4 修改

修改的注解

```java
public interface UserMapper {
    ...
    // 修改
    @Update("update user set name = #{name},pwd = #{password} where id = #{id}")
    public int updateUser(User user);
}
```

测试方法省略。

#### 4.5 删除

删除的注解

```java
public interface UserMapper {
    ...
    // 删除
    @Delete("delete from user where id = #{id}")
    public int deleteUser(@Param("id") int id);
}
```

没什么好说的，太简单惹。

### 5. 总结

了解了一下在 MyBatis 中如何使用注解，主要是记得要把接口在映射器中注册一下，在方法上写注解就完事了。

还有关于 @param 的使用

- 基本类型或 String 类型的参数，需要加上
- 引用类型不需要加（如 User 类）
- 如果只有一个基本类型，会自动匹配，但还是建议加上！
- 在 SQL 中引用的就是 @param 设置的属性名！

就这么多内容，关键还是要去看一下 Java 反射，以后再来研究 MyBatis 源码🙄。