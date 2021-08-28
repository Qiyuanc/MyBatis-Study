## MyBatis结果集映射

本节新建 MyBatis-03项目，研究如何解决属性名和字段名不一致的问题。

### 1. 正常情况

将 MyBatis-02项目的内容复制进来，并且在 UserMapper 接口和 UserMapper.xml 配置中只保留根据 ID 查询用户方法。此时 User 实体类，UserMapper 接口，UserMapper.xml 配置内容为

```java
public class User {
    private int id;
    private String name;
    private String pwd;
	...
}
```

```java
public interface UserMapper {
    // 根据ID查询用户
    public User getUserById(int id);
}
```

```xml
<mapper namespace="com.qiyuan.dao.UserMapper">
    <!--根据ID查询用户-->
    <select id="getUserById" parameterType="int" resultType="com.qiyuan.entity.User">
        select * from mybatis.user where id = #{id}
    </select>
</mapper>
```

此时实体类的属性与数据库字段是一一对应的，执行测试方法也能正常查询到结果

```java
public class UserMapperTest {
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
}
```

```java
User{id=1, name='祈鸢', pwd='123456'}
```

### 2. 不一致情况

再将 User 实体类中的属性改名（ get/set 方法和 toString 方法也改），使其与数据库字段不对应，再执行测试方法，此时查询到的不对应的字段结果为 null

```java
public class User {
    private int id;
    private String name;
    // 数据库字段为 pwd 
    private String password;
	...
}
```

```java
User{id=1, name='祈鸢', password='null'}
```

出现这个问题的原因很简单，SQL 语句

```sql
select * from mybatis.user where id = #{id}
```

该语句完整的情况应该是

```sql
# 数据库字段
select id, name, pwd from mybatis.user where id = #{id}
```

在查询到 id, name, pwd 后，类型处理器会将其映射到返回类型的属性中（ set 方法）。若属性和字段名不一致，则无法正确映射，于是结果就是 null。

### 3. 解决方法

#### 3.1 起别名

> 如果列名和属性名不能匹配上，可以在 SELECT 语句中设置列别名（这是一个基本的 SQL 特性）来完成匹配。

既然是 SQL 语句执行时的问题，修改 SQL 语句也可以解决，如将上面的语句改为

```sql
select id, name, pwd as password from mybatis.user where id = #{id}
```

就可以将 pwd 映射到 password 上，这是最暴力也是最笨的方法，一般都不会这么干。

#### 3.2 结果映射 resultMap

> `resultMap` 元素是 MyBatis 中最重要最强大的元素。它可以让你从 90% 的 JDBC `ResultSets` 数据提取代码中解放出来，并在一些情形下允许你进行一些 JDBC 不支持的操作。
>
> ResultMap 的设计思想是，对简单的语句做到零配置，对于复杂一点的语句，只需要描述语句之间的关系就行了。

```xml
<mapper namespace="com.qiyuan.dao.UserMapper">
    <!--将结果集 UserMap 映射为 实体类 User-->
    <resultMap id="UserMap" type="User">
        <!-- colum 数据库字段 property 实体类属性-->
        <!--相同的其实可以不写-->
        <result column="id" property="id"/>
        <result column="name" property="name"/>
        <result column="pwd" property="password"/>
    </resultMap>
    <!--根据ID查询用户，只有一个参数可以不写参数类型-->
    <!--使用结果集映射 UserMap-->
    <select id="getUserById" resultMap="UserMap">
        select * from mybatis.user where id = #{id}
    </select>
</mapper>
```

通过将 SQL 语句的返回类型设置为 resultMap，再对 resultMap 进行配置（colum 为数据库字段，property 为实体类属性），就可以将 SQL 查询出来的结果映射为 type="User"。

### 4. 总结

这样看来可能并不能发现 resultMap 真正的作用，如在上面例子中我为什么不直接把数据库字段与实体类属性一一对应呢？

> 如果这个世界总是这么简单就好了。

在联合多张表查询的场景下，查询到的内容可能会非常复杂。就像实体类的属性可能也是个实体类一样——数据库某张表的某个字段在另一张表中可能是个主键，也能查询到许多信息。这种情况才能体现  resultMap 的强大。

不过这节就先介绍一下啦，我也不知道为什么突然断开了，但迟早是要讲到的😕。