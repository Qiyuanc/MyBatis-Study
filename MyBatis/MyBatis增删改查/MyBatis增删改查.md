## MyBatis增删改查

这节沿用上一节的MyBatis-Study / MyBatis-01项目，同时要将其中不规范的命名换成规范的MyBatis命名。如UserDao接口要改名为UserMapper接口，同时注意要将UserMapper.xml中绑定的接口也改掉（IDEA有一键全改的功能），保证其对应了一个有效的接口，否则会出现MapperRegister错误。

```xml
<!--UserMapper.xml-->
<!--命名空间namespace要绑定一个对应的Dao/Mapper接口，相当于实现它-->
<mapper namespace="com.qiyuan.dao.UserMapper">
```

### 1. 查询 SELECT

Mapper.xml中的 select 标签就是查询语句，它有几个参数

- id：对应 namespace 中对应接口的方法名
- resultType：sql 语句执行完后的返回类型
- parameterType：参数类型，如基本类型 int 或某个类

如根据 id 查询用户，在 UserMapper 中添加方法

```java
public interface UserMapper {
	...
    // 根须ID查询用户
    public User getUserById(int id);
}
```

然后修改 UserMapper.xml 的配置（与 UserMapper 在同一目录下）

这里使用 #{ } 获取参数，参数类型配置在 parameterType 中，参数名在 #{ } 中，结合起来就是 int id 了

```xml
<!--命名空间namespace要绑定一个对应的Dao/Mapper接口，相当于实现它-->
<mapper namespace="com.qiyuan.dao.UserMapper">
    <!--select查询语句，返回类型要写全限定名-->
	...
    <!--根据ID查询用户-->
    <select id="getUserById" parameterType="int" resultType="com.qiyuan.entity.User">
        select * from mybatis.user where id = #{id}
    </select>
</mapper>
```

最后编写要调用到的方法，这里是 Test 方法

```java
public class UserMapperTest {
    
    ...
        
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
}
```

这样就查询到了，好起来了。

### 2. 增加 INSERT

首先在接口中添加方法，为什么返回 int 懂得都懂🧐

```java
public interface UserMapper {
	...
    // 新增用户
    public int addUser(User user);
}
```

在 UserMapper.xml 中配置这个方法，对象中的属性可以直接取出来，但要注意和对象中的属性名对应

IDEA自动补全查询参数：在 Settings —> SQL Dialects 中将 Global SQL Dialect 和 Project SQL Dialect 设置为 Mysql，就可以自动补全参数了。

```xml
<!--新增一个用户-->
<!--对象中的属性可以直接取出来，要注意和对象中的属性名对应！-->
<insert id="addUser" parameterType="com.qiyuan.entity.User">
    insert into mybatis.user (id, name, pwd) values (#{id},#{name},#{pwd})
</insert>
```

编写 Test 测试，不要忘记增删改操作涉及到事务，需要提交事务！

```java
public class UserMapperTest {
    ...
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
}
```

增加完成，太简单以至于要写一句话填充一下。

### 3. 删除 DELETE

在接口中添加方法

```java
public interface UserMapper {
	...
    // 删除用户，根据id删
    public int deleteUser(int id);
}
```

在 UserMapper.xml 中配置方法

```xml
<!--删除一个用户-->
<delete id="deleteUser" parameterType="int">
    delete from mybatis.user where id = #{id}
</delete>
```

写一个 Test 测试

```java
public interface UserMapper {
	...
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
}
```

删除完成。

### 4. 修改 UPDATE

在接口中添加方法

```java
public interface UserMapper {
	...
    // 修改用户信息
    public int updateUser(User user);
}
```

在 UserMapper.xml 中配置方法

```xml
<!--修改用户信息-->
<update id="updateUser" parameterType="com.qiyuan.entity.User">
    update mybatis.user set name = #{name},pwd = #{pwd} where id = #{id}
</update>
```

写一个 Test 测试

```java
public class UserMapperTest {
    ...
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
```

修改完成。

### 5. 使用Map传参数

在上面的新增用户中，传的参数是 User。也就是说，需要新增一个用户时，必须先创建一个要传的 User 对象，再将这个对象作为参数才能完成新增用户操作。当 User 的属性非常多时（说明数据库中的字段也非常多），可能是十几个或几十个，创建一个对象再给它其中的属性赋值就非常麻烦。这时就可以使用 Map 作为参数。

使用 Map 作为参数，接口中必须要有对应的方法

```java
public interface UserMapper {
	...
    // 用Map 让UserMapper.xml获取数据
    public int addUser2(Map<String,Object> map);
}
```

然后在 UserMapper.xml 中配置这个方法，参数 parameterType 为 Map，此处 #{ } 中的属性名不用在与 User 类中的属性名一一对应，而是要与传入的 map 中键值对的 key 对应（可以不对应但没必要，这里故意写乱点当例子）

```xml
<!--使用Map-->
<insert id="addUser2" parameterType="map">
    insert into mybatis.user (id, name, pwd) values (#{userid},#{userName},#{password})
</insert>
```

再写一个对应的 Test 方法来测试一下

```java
public class UserMapperTest {
    ...
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
}
```

新增用户操作成功！这里是对 INSERT 操作使用 Map 的例子，所以参数还是写完了。在 UPDATE 操作中，假设要修改密码，只需要在 Map 中 put 两个参数 id 和 pwd，直接省去了创建对象的步骤！

### 6. 模糊查询

模糊查询需要使用 like 和 % %，有两种方法可以实现，这里的例子是在 Java代码中添加 % %。

在 UserMapper 接口中添加模糊查询方法，参数为模糊查询的字段

```java
public interface UserMapper {
	...
    // 模糊查询
    public List<User> getUserLike(String likeName);
}
```

在 UserMapper.xml 中配置，这里的 SQL 不加 % %，就要在 Java 代码传进来的时候加；SELECT 记得写返回值类型！

```xml
<!--模糊查询，记得写返回值类型-->
<select id="getUserLike" resultType="com.qiyuan.entity.User">
    select * from  mybatis.user where name like #{name}
</select>
```

在测试方法中，传参数时该参数应该已经做好 % % 处理

```java
public class UserMapperTest {
    ...
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
}
```

如果不加 % % 就执行，虽然不会报错但是什么都查不出来！

模糊查询需要使用 like 和 % %，有两种方法可以做到

1. 在 Java 代码中对字符串进行 % % 处理，也就是上面例子中的方式

   ```java
   // select * from  mybatis.user where name like #{name}
   List<User> userList = mapper.getUserLike("%祈%");
   ```

2. 在 SQL 中直接添加 % %，Java 代码只要正常传字符串进行了

   ```sql
   select * from  mybatis.user where name like "%"#{name}"%"
   ```

两种方式都能实现，但第一种有 SQL 注入的风险，所以实现项目中为了安全起见还是直接定死比较好。

### 7. 总结

本节实践了 MyBatis 的增删改查操作、Map 的使用和模糊查询的两种方法。

在传参数时，要注意：

- 用对象传参数，要在 SQL 中取出对象的属性！
- 用 Map 传参数，要在 SQL 中取出 Map 的 Key！
- 只有一个基本类型参数，可以直接在 SQL 中取到（不写都行）！

模糊查询就是和软件安全相关的了，这里不再深入😶。