## MyBatis动态SQL

**什么是动态 SQL？**

**动态 SQL 就是根据不同的条件生成不同的 SQL 语句。**

> 动态 SQL 是 MyBatis 的强大特性之一。如果你使用过 JDBC 或其它类似的框架，你应该能理解根据不同条件拼接 SQL 语句有多痛苦，例如拼接时要确保不能忘记添加必要的空格，还要注意去掉列表最后一个列名的逗号。利用动态 SQL，可以彻底摆脱这种痛苦。

如官方文档所说，之前 SMBMS 项目手写 SQL 还要考虑怎么拼接确实很难过，所以现在新建 MyBatis-08 项目，学习一下动态 SQL。

### 1. 搭建环境

1. 在数据库中创建 blog 表，后面再插入数据

   ```sql
   CREATE TABLE `blog`(
   `id` VARCHAR(50) NOT NULL COMMENT 博客id,
   `title` VARCHAR(100) NOT NULL COMMENT 博客标题,
   `author` VARCHAR(30) NOT NULL COMMENT 博客作者,
   `create_time` DATETIME NOT NULL COMMENT 创建时间,
   `views` INT(30) NOT NULL COMMENT 浏览量
   )ENGINE=INNODB DEFAULT CHARSET=utf8
   ```

2. 创建新项目 MyBatis-08，导包

3. 编写配置文件（和之前的一样）

4. 编写实体类

   ```java
   @Data
   public class Blog {
       // 数据库中是 varchar 型的
       private String id;
       private String title;
       private String author;
       // 使用 util 下的 Date
       private Date createTime;
       private int views;
   }
   ```

   这里的 id 是 String 类型的，再写一个 IDUtils 工具类来获取随机的 id。如果用 int 型自增的 id，如在 id 到6的时候将5删掉，则下一个 id 仍是7，除非插入5否则5这个 id 就不会存在，这涉及到 INNODB 底层的原理。

   ```java
   public class IDUtils {
       public static String getId(){
           // 通过 java.util.UUID 获取随机 uid，将其中的 "-" 删掉
           return UUID.randomUUID().toString().replace("-","");
       }
   
       @Test
       public void Test(){
           System.out.println(IDUtils.getId());
       }
   }
   ```

   除此之外，Blog 的属性 createTime 与在数据库中的字段为 create_time，无法对应上，可以选择设置 mapUnderscoreToCamelCase 使它们自动对应

   > settings
   >
   > mapUnderscoreToCamelCase
   >
   > 是否开启驼峰命名自动映射，即从经典数据库列名 A_COLUMN 映射到经典 Java 属性名 aColumn。
   >
   > 可选：true | false
   >
   > 默认：False

   此时设置为

   ```xml
   <!-- mybatis-config.xml 中 -->
   <settings>
       <setting name="logImpl" value="STDOUT_LOGGING"/>
       <setting name="mapUnderscoreToCamelCase" value="true"/>
   </settings>
   ```

5. 编写实体类对应的 Mapper 接口和 Mapper.xml

   ```java
   public interface BlogMapper {
       // 插入一条博客信息
       int addBlog(Blog blog);
   }
   ```

   ```xml
   <mapper namespace="com.qiyuan.dao.BlogMapper">
   
       <!--已经设置了扫描 entity 下的别名-->
       <insert id="addBlog" parameterType="blog">
           insert into blog(id, title, author, create_time, views)
           values (#{id}, #{title}, #{author}, #{createTime}, #{views})
       </insert>
   
   </mapper>
   ```

   这里接口和其配置文件都在 com.qiyuan.dao 下，注册时使用类注册

   ```xml
   <!-- mybatis-config.xml 中 -->
   <mappers>
       <mapper class="com.qiyuan.dao.BlogMapper"/>
   </mappers>
   ```

   这样即注册了 Mapper.xml，也能使用注解（虽然这里没用）

6. 在测试方法中测试同时添加数据

   ```java
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
   }
   ```

   SqlSessionFactory 创建 SqlSession 时设置了打开事务提交，这里就不用 commit 了。

   执行完后数据库就有数据了

   | id                               | title   | author | create_time         | views |
   | -------------------------------- | ------- | ------ | ------------------- | ----- |
   | 17ada90375174f63969fa69256019db8 | Mybatis | 祈鸢   | 2021-08-17 15:46:32 | 1000  |
   | 8372493a5ed7471baeebedb310383d8c | Java    | 祈鸢   | 2021-08-17 15:46:32 | 1000  |
   | adbee140554e4b099d52bfa6e51d938c | Spring  | 祈鸢   | 2021-08-17 15:46:32 | 1000  |
   | bc3e2ec132074016aa176c6b133ca43e | 微服务  | 祈鸢   | 2021-08-17 15:46:32 | 1000  |

   使用 java 代码生成数据库文件也是一种思路（或者用 sql 文件生成）。

### 2. if 标签

有这样一个场景：根据输入的标题和作者查询博客，如果输入了标题，就按标题查询；输入了作者，就按作者查询；都输入了就都查询。在之前 SMBMS 项目也有这种场景，当时是可选输入的是用户名和用户职责。当时实现的时候是在 Dao 层判断参数是否为空，然后用 Stirng 的方法把 SQL 语句拼接起来，非常麻烦。现在动态 SQL 中的 if 标签就是用来干这个的。

先在接口中创建一个方法，查询博客，参数为 map，方便传不同的参数（好像又不太规范）

```java
public interface BlogMapper {
	...
    // 根据条件查询博客
    List<Blog> queryBlogIf(Map map);
}
```

在 BlogMapper.xml 中配置这个方法，使用了 if 标签

   ```xml
   <!--使用 map，从 map 中取对应的值-->
   <select id="queryBlogIf" parameterType="map" resultType="Blog">
       select * from blog where 1=1
       <if test="title != null">
           and title = #{title}
       </if>
       <if test="author != null">
           and author = #{author}
       </if>
   </select>
   ```

其中 where 1=1 的作用是确保两个 if 都判断为否时，SQL 语句仍是正常的，否则会变成

```sql
select * from blog where 
```

这样执行肯定是会报错的。当然现在还没到 where 标签，后面就用 where 标签干这个活了。

执行一下测试方法，先不传参数

```java
@Test
public void queryBlog(){
    SqlSession sqlSession = MyBatisUtils.getSqlSession();
    BlogMapper mapper = sqlSession.getMapper(BlogMapper.class);
    Map<String, String> map = new HashMap<String, String>();
    // 不放参数到 map 中，相当于两个 if 都不成功
    List<Blog> blogs = mapper.queryBlog(map);
    for (Blog blog : blogs) {
        System.out.println(blog);
    }
    sqlSession.close();
}
```

日志输出执行的 SQL，还有查询结果

```java
==>  Preparing: select * from blog where 1=1
==> Parameters: 
// UUID 乱七八糟的
Blog(id=17ada90375174f63969fa69256019db8, title=Mybatis, author=祈鸢, createTime=Tue Aug 17 23:46:32 CST 2021, views=1000)
Blog(id=8372493a5ed7471baeebedb310383d8c, title=Java, author=祈鸢, createTime=Tue Aug 17 23:46:32 CST 2021, views=1000)
Blog(id=adbee140554e4b099d52bfa6e51d938c, title=Spring, author=祈鸢, createTime=Tue Aug 17 23:46:32 CST 2021, views=1000)
Blog(id=bc3e2ec132074016aa176c6b133ca43e, title=微服务, author=祈鸢, createTime=Tue Aug 17 23:46:32 CST 2021, views=1000)
```

非常好，没有什么问题。再把参数（ title = Java 和 author = 祈鸢 ）放到 map 中

```java
	Map<String, String> map = new HashMap<String, String>();
	// 参数放进去
	map.put("title","MyBatis");
	map.put("author","祈鸢");
```

再查询一下，结果为

```java
==>  Preparing: select * from blog where 1=1 and title = ? and author = ?
==> Parameters: MyBatis(String), 祈鸢(String)
    
Blog(id=17ada90375174f63969fa69256019db8, title=Mybatis, author=祈鸢, createTime=Tue Aug 17 23:46:32 CST 2021, views=1000)
```

完美！if 标签就是这么简单，轻易做到了 String 拼接搞半天的事。

### 3. where 标签

> *where* 元素只会在子元素返回任何内容的情况下才插入 “WHERE” 子句。而且，若子句的开头为 “AND” 或 “OR”，*where* 元素也会将它们去除。

用 where 标签解决一下上面说的问题，而且 where 标签会是一个特别常用的标签，用法如

```xml
<select id="queryBlog" parameterType="map" resultType="Blog">
    select * from blog
    <where>
        <if test="title != null">
            title = #{title}
        </if>
        <if test="author != null">
            and author = #{author}
        </if>
    </where>
</select>
```

其中 if 标签仍会进行判断，如果有至少一个 if 有效，即需要追加条件，where 标签会自动先将 where 标签追加上去；若没有 if 有效，则 SQL 语句中也不会有 where

不放参数

```java
==>  Preparing: select * from blog
==> Parameters: 
```

放参数 title 和 author

```java
==>  Preparing: select * from blog WHERE title = ? and author = ?
==> Parameters: MyBatis(String), 祈鸢(String)
```

还有一个要注意的点，where 还会自动去掉多余的 and 或 or（可在 trim 标签中配置），如不传入 title 而只传入 author

```java
==>  Preparing: select * from blog WHERE author = ?
==> Parameters: 祈鸢(String)
```

可以看到 author 前的 and 也被很聪明地去掉了。

### 4. choose 标签

> 有时候，我们不想使用所有的条件，而只是想从多个条件中选择一个使用。针对这种情况，MyBatis 提供了 choose 元素，它有点像 Java 中的 switch 语句。

**类似于 java 中的 switch**，在众多条件中只选一个。其中 when 相当于 case，otherwise 相当于 default，懂得都懂没什么好说的。

和上面一样，创建一个使用 choose 的接口方法，参数也是 map

```java
public interface BlogMapper {
    ...
    // 根据条件查询博客，选其一
    List<Blog> queryBlogChoose(Map map);
}
```

对应的 SQL 语句

```xml
<select id="queryBlogChoose" parameterType="map" resultType="Blog">
    select * from blog
    <where>
        <choose>
            <when test="title != null">
                title = #{title}
            </when>
            <when test="author != null">
                author = #{author}
            </when>
            <otherwise>
                views = #{views}
            </otherwise>
        </choose>
    </where>
</select>
```

乍一看这层级关系好像有点乱，其实就是加个 where 和 switch 的关系。

同时，与 switch 相同，when 是从上至下判断的，若有一个成功则 choose 结束。即同时传入 title 和 author，也只相当于根据 title 查询，实践一下

```java
==>  Preparing: select * from blog WHERE title = ?
==> Parameters: MyBatis(String)
    
Blog(id=17ada90375174f63969fa69256019db8, title=Mybatis, author=祈鸢, createTime=Tue Aug 17 23:46:32 CST 2021, views=1000)
```

同时还有个 otherwise 相当于 default，在这里如果没传 title 和 author，则会根据 views 查询，若连 views 也不传，则结果为

```java
==>  Preparing: select * from blog WHERE views = ?
==> Parameters: null
```

结果也是什么也没有啦，传 views 的这里就不测试了，没什么问题。

### 5. set 标签

> *set* 元素会动态地在行首插入 SET 关键字，并会删掉额外的逗号（这些逗号是在使用条件语句给列赋值时引入的）。

假设有一个需求：根据传入的参数不同去修改数据库对应的列，如传入了 id 和 title（ id 当然是必传的，不然怎么知道改谁啊）就去修改对应 id 的 title，传了 id、title 和 author，就修改对应 id 的 title 和 author。

这就需要用到 if 来判断某个参数是否存在了，存在的时候就追加其对应的 set 语句，如

```java
public interface BlogMapper {
	...
    // 更新博客信息
    int updateBlog(Map map);
}
```

```xml
<update id="updateBlog" parameterType="map">
    update blog set
    <if test="title != null">
        /*此处得有逗号！*/
        title = #{title},
    </if>
    <if test="author != null">
        author = #{author}
    </if>
    where id = #{id}
</update>
```

注意其中追加的 title 后有个逗号，如果没有这个逗号，title 和 author 都存在的时候 SQL 语句就不对了；但有了这个逗号，只有 title 的时候 SQL 语句也不对了。这就需要用到 set 标签了。

**set 标签其实就相当于 UPDATE 版的 where 标签**。where 标签用于在查询时添加 where 以及去掉多余的 AND 和 OR，而 set 标签用于在更新时添加 set 以及去掉多余的逗号。

使用了 set 修改一下 SQL 语句

```xml
<update id="updateBlog" parameterType="map">
    update blog
    <set>
        <if test="title != null">
            /*有逗号也没事*/
            title = #{title},
        </if>
        <if test="author != null">
            author = #{author}
        </if>
    </set>
    where id = #{id}
</update>
```

再写个测试方法，执行一下

```java
@Test
public void updateBlog(){
    SqlSession sqlSession = MyBatisUtils.getSqlSession();
    BlogMapper mapper = sqlSession.getMapper(BlogMapper.class);
    Map<String, String> map = new HashMap<String, String>();
    // 参数放进去，UUID 有点长。。。
    map.put("id","17ada90375174f63969fa69256019db8");
    map.put("title","MyBatisAfterUpdate");
    map.put("author","祈鸢ccc");

    System.out.println(mapper.updateBlog(map));
    sqlSession.close();
}
```

执行的日志

```java
==>  Preparing: update blog SET title = ?, author = ? where id = ?
==> Parameters: MyBatisAfterUpdate(String), 祈鸢ccc(String), 17ada90375174f63969fa69256019db8(String)
<==    Updates: 1
```

再用查询的方法，查询 “祈鸢ccc” 看一下

```java
Blog(id=17ada90375174f63969fa69256019db8, title=MyBatisAfterUpdate, author=祈鸢ccc, createTime=Tue Aug 17 23:46:32 CST 2021, views=1000)
```

这样就修改成功了嗷，再试试只修改 title 的

```java
==>  Preparing: update blog SET title = ? where id = ?
==> Parameters: MyBatisAfterUpdateTwice(String), 17ada90375174f63969fa69256019db8(String)
<==    Updates: 1
```

SQL 语句没有问题，title 后的逗号被去掉了！就不再查询一次了，已经 Updates: 1 了。

不过如果什么修改的参数都不传，还是会报错的，因为 update blog 后直接接上了 where，懂得都懂吧。

### 6. trim 标签

其实 where 标签和 set 标签的本质都是 trim 标签，此处引用官方文档

> 如果 *where* 元素与你期望的不太一样，你也可以通过自定义 trim 元素来定制 *where* 元素的功能。比如，和 *where* 元素等价的自定义 trim 元素为
>
> ```xml
> <trim prefix="WHERE" prefixOverrides="AND |OR ">
>   ...
> </trim>
> ```
>
> 与 *set* 元素等价的自定义 *trim* 元素
>
> ```xml
> <trim prefix="SET" suffixOverrides=",">
>   ...
> </trim>
> ```

其中 prefix 为前缀，trim 会插入 perfix 属性中指定的内容，移除 prefixOverrides（前缀覆盖）属性中指定的内容；还有 suffix 为后缀，suffixOverrides 为后缀覆盖。

### 7. sql 标签

sql 标签是一个和 select、insert 等标签同级的标签，可以将重复使用的片段提取出来并赋予一个 id，在需要用到这个片段的地方引用 id 即可

如上面的片段

```xml
<if test="title != null">
    title = #{title},
</if>
<if test="author != null">
    author = #{author}
</if>
```

就经常用到，把它提取出来

```xml
<sql id="if-title-author">
    <if test="title != null">
        title = #{title}
    </if>
    <if test="author != null">
        and author = #{author}
    </if>
</sql>
```

在用到的时候，用 include 引用即可

```xml
<select id="queryBlogIf" parameterType="map" resultType="Blog">
    select * from blog
    <where>
        <include refid="if-title-author"/>
    </where>
</select>
```

不过这种方法有点降低程序的可读性，看起来有点难受。

**注意**：提取出来的内容不要包含 where，包含 if 标签就行了。

### 8. foreach 标签

> *foreach* 元素的功能非常强大，它允许你指定一个集合，声明可以在元素体内使用的集合项（item）和索引（index）变量。它也允许你指定开头与结尾的字符串以及集合项迭代之间的分隔符。这个元素也不会错误地添加多余的分隔符，看它多智能！
>
> **提示** 你可以将任何可迭代对象（如 List、Set 等）、Map 对象或者数组对象作为集合参数传递给 *foreach*。当使用可迭代对象或者数组时，index 是当前迭代的序号，item 的值是本次迭代获取到的元素。当使用 Map 对象（或者 Map.Entry 对象的集合）时，index 是键，item 是值。

foreach 标签用于对集合进行遍历，有点不太好理解，看一下例子。

在接口中创建一个查询1、2、3号博客的方法（之前用的是 UUID，这里为了方便遍历，手动把 UUID 改成普通的 id 了😓）

```java
public interface BlogMapper {
	...
    // 查询 1 2 3 号博客的记录
    List<Blog> queryBlogForeach(Map map);
}
```

查询1、2、3号博客的 SQL 语句为

```sql
select * from blog where id in (1,2,3)
```

现在的目的就是拼接出这个 SQL 语句，在 BlogMapper.xml 中配置

```xml
<!--传递的参数是 map，其中可以有一个集合
    传进来的集合（collection）叫 ids
    其中的元素（item）叫 id -->
<!--目的：拼接出 select * from blog where id in (1,2,3) 这个语句-->
<select id="queryBlogForeach" parameterType="map" resultType="Blog">
    select * from blog
    <where>
        <foreach collection="ids" item="id"
                 open="id in (" close=")" separator=",">
            id = #{id}
        </foreach>
    </where>
</select>
```

测试方法

```java
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
```

查询的结果

```java
==>  Preparing: select * from blog WHERE id in ( id = ? , id = ? , id = ? )
==> Parameters: 1(String), 2(String), 3(String)
<==    Columns: id, title, author, create_time, views
<==        Row: 1, MyBatisAfterUpdateTwice, 祈鸢ccc, 2021-08-17 15:46:32, 1000
<==      Total: 1
Blog(id=1, title=MyBatisAfterUpdateTwice, author=祈鸢ccc, createTime=Tue Aug 17 23:46:32 CST 2021, views=1000)
```

我也不知道为什么这里只查出来了一行···数据库执行这条语句也确实只有一行

**突然明白了**，上面放在 foreach 里的是 id = #{ id }，这个会被放到括号里，直接换成 #{ id } ！

```xml
<select id="queryBlogForeach" parameterType="map" resultType="Blog">
    select * from blog
    <where>
        <foreach collection="ids" item="id"
                 open="id in (" close=")" separator=",">
            #{id}
        </foreach>
    </where>
</select>
```

再执行一下测试方法

```java
==>  Preparing: select * from blog WHERE id in ( ? , ? , ? )
==> Parameters: 1(Integer), 2(Integer), 3(Integer)
<==    Columns: id, title, author, create_time, views
<==        Row: 1, MyBatisAfterUpdateTwice, 祈鸢ccc, 2021-08-17 15:46:32, 1000
<==        Row: 2, Java, 祈鸢, 2021-08-17 15:46:32, 1000
<==        Row: 3, Spring, 祈鸢, 2021-08-17 15:46:32, 1000
<==      Total: 3
Blog(id=1, title=MyBatisAfterUpdateTwice, author=祈鸢ccc, createTime=Tue Aug 17 23:46:32 CST 2021, views=1000)
Blog(id=2, title=Java, author=祈鸢, createTime=Tue Aug 17 23:46:32 CST 2021, views=1000)
Blog(id=3, title=Spring, author=祈鸢, createTime=Tue Aug 17 23:46:32 CST 2021, views=1000)
```

这次对了！**看来 foreach 中放的自己写的内容（如 id = #{ id } 和 #{ id } ）就是被遍历放的东西**！关键就看日志输出的 SQL 语句对不对就完了！

**还有问题**：测试方法中 List 中放的 id 是 Integer 类型的元素，数据库中的 id 是 varchar 型的，这居然能对应上？

### 9. 总结

**所谓动态 SQL，本质还是 SQL 语句，只是在 SQL 层面执行了逻辑代码进行增删。**

**使用动态 SQL，只要保证拼接出来的 SQL 的正确性，按照 SQL 的格式去排列组合就可以了！**

搞了这么久终于看完动态 SQL 了，这东西还是用的多就熟练了，忘了的时候再回来看看吧😵。

