## MyBatis分页

分页操作是为了减少数据的处理量，本节继续使用 MyBatis-04 项目研究一下怎么方便的实现分页。

### 1. Limit实现分页

在之前的 SMBMS 项目中，就是使用数据库的关键字 limit 实现的分页，接口和 SQL 语句如下

```java
public interface UserMapper {
	...
    // 分页查询用户
    public List<User> getUserByLimit(Map<String,Integer> map);
}
```

```xml
<!--在 UserMapper.xml 中-->
<select id="getUserByLimit" parameterType="map" resultMap="UserMap">
    select * from mybatis.user limit #{startIndex},#{pageSize}
</select>
```

再写一个测试方法执行查询

```java
public class UserMapperTest {
    @Test
    public void getUserByLimit(){
        SqlSession sqlSession = MyBatisUtils.getSqlSession();
        UserMapper mapper = sqlSession.getMapper(UserMapper.class);
        Map<String, Integer> map = new HashMap<String, Integer>();
        // map 的 key 要和 SQL 中使用的名字相同！
        map.put("startIndex",Integer.valueOf(0));
        map.put("pageSize",Integer.valueOf(3));
        List<User> userList = mapper.getUserByLimit(map);
        for (User user : userList) {
            System.out.println(user);
        }
        sqlSession.close();
    }
}
```

查询的结果就是从数据库数据第0条（ startIndex ）记录开始的3条（ pageSize ）记录。

#### 1.1 注意

在写代码的时候，我无意中将 #{ } 写为了 ${ }，仍能执行正确的查询，但日志显示

```java
[com.qiyuan.dao.UserMapper.getUserByLimit]-==>  Preparing: select * from mybatis.user limit 0,3
[com.qiyuan.dao.UserMapper.getUserByLimit]-==> Parameters: 
[com.qiyuan.dao.UserMapper.getUserByLimit]-<==      Total: 3
```

即 SQL 语句没有了占位符 ?，直接就被补充了，参数 parameters 也为空。好奇之下发现了这个问题，正常的日志应为

```java
[com.qiyuan.dao.UserMapper.getUserByLimit]-==>  Preparing: select * from mybatis.user limit ?,?
[com.qiyuan.dao.UserMapper.getUserByLimit]-==> Parameters: 0(Integer), 3(Integer)
[com.qiyuan.dao.UserMapper.getUserByLimit]-<==      Total: 3
```

看来日志还真好使，以后注意一下这个问题吧。

#### 1.2 扩展

出于对 map 这个东西的好奇，我尝试将模糊查询和分页结合起来。模糊查询需要 name 参数，类型为 String，而分页的参数为 Integer 类型，我尝试用 Map<String, Object> map 把它们结合起来

修改方法中的参数类型和 SQL 语句

```java
public interface UserMapper {
	...
    // 分页查询用户
    public List<User> getUserByLimit(Map<String,Object> map);
}
```

```xml
<select id="getUserByLimit" parameterType="map" resultMap="UserMap">
    select * from mybatis.user where name like #{name} limit #{startIndex},#{pageSize}
</select>
```

同时上面的测试方法中 map 的类型也要修改，再增加一个 key 为 name 的参数

```java
public class UserMapperTest {
    @Test
    public void getUserByLimit(){
        ...
		Map<String, Object> map = new HashMap<String, Object>();
        map.put("name","%祈%");
        map.put("startIndex",Integer.valueOf(0));
        map.put("pageSize",Integer.valueOf(3));
        ...
    }
}
```

执行结果

```java
[com.qiyuan.dao.UserMapper.getUserByLimit]-==>  Preparing: select * from mybatis.user where name like ? limit ?,?
[com.qiyuan.dao.UserMapper.getUserByLimit]-==> Parameters: %祈%(String), 0(Integer), 3(Integer)
[com.qiyuan.dao.UserMapper.getUserByLimit]-<==      Total: 3
User{id=1, name='祈鸢', password='123456'}
User{id=3, name='風栖祈鸢', password='07230723'}
User{id=5, name='祈鸢bbb', password='123123'}
```

可以看到并没有什么问题嗷，挺好的。

### 2. RowBounds实现分页

使用 limit 是在 SQL 层面完成分页，而这个使用 RowBounds 的方法是在 Java 代码层面完成分页。

添加接口方法和对应配置如下

```java
public interface UserMapper {
	...
    // RowBounds分页 不用传页面的参数
    public List<User> getUserByRowBounds();
}
```

```xml
<!--RowBounds分页-->
<select id="getUserByRowBounds" resultMap="UserMap">
    select * from mybatis.user
</select>
```

可以看到目前和直接查询所有用户没有区别，因为分页在 Java 代码中实现。

写一个测试方法实现

```java
public class UserMapperTest {
	@Test
    public void getUserByRowBounds(){
        SqlSession sqlSession = MyBatisUtils.getSqlSession();
        // 从第1条记录开始取2条记录
        RowBounds rowBounds = new RowBounds(1, 2);
        // sqlSession实例化接口的第二种方式
        List<User> userList = sqlSession.selectList("com.qiyuan.dao.getUserByRowBounds");
        for (User user : userList) {
            System.out.println(user);
        }
        sqlSession.close();
    }
}
```

查询的结果

```java
[com.qiyuan.dao.UserMapper.getUserByRowBounds]-==>  Preparing: select * from mybatis.user
[com.qiyuan.dao.UserMapper.getUserByRowBounds]-==> Parameters: 
User{id=2, name='Qiyuanc', password='123456'}
User{id=3, name='風栖祈鸢', password='07230723'}
```

可见这种方式也能实现分页，了解即可。

### 3. 总结

分页其实除了手写 limit 和 RowBounds 之外，也能使用分页插件。

如 MyBatis PageHelper 插件（ https://pagehelper.github.io ）就可以帮助分页。不过其底层实现也不过是 limit 和 RowBounds，只是封装的好而已。项目不大的时候还是要自己多写点东西🧐。