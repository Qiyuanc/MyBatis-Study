## MyBatis一对多处理

本节新建 MyBatis-07 项目学习 MyBatis 中的一对多处理。

### 1. 搭建测试环境

本节使用的数据库与上一节的数据库相同，配置文件啥的都是直接复制粘贴，但是实体类 Student 和 Teacher 要改变一下，对应的接口和配置文件内容也先删干净

```java
@Data
public class Teacher {
    private int id;
    private String name;
    // 一个老师对应多个学生
    private List<Student> students;
}
```

```java
@Data
public class Student {
    private int id;
    private String name;
    // 学生关联老师
    private int tid;
}
```

实体类 Teacher 对应的 TeacherMapper 接口，只有一个根据 id 查询老师信息的方法

```java
public interface TeacherMapper {
    // 获取指定 id 老师的信息 包含其所有的学生
    public Teacher getTeacher(int id);
}
```

在 TeacherMapper.xml 中给这个方法实现一下，只有一个参数，#{ } 随便取

```xml
<mapper namespace="com.qiyuan.dao.TeacherMapper">

    <select id="getTeacher" resultType="Teacher">
        select * from teacher where id = #{id}
    </select>

</mapper>
```

测试方法获取 id 为1的老师的信息，这里省略，结果

```java
Teacher(id=1, name=祈鸢, students=null)
```

还是那个道理，没做结果集映射，students 肯定是空的啦。 

### 2. 按照查询嵌套处理

和上节处理一对多类似，不过换成 collection 标签，这里还要注意 javaType 和 ofType 的区别（ javaType 好像可以省略？）

```xml
<mapper namespace="com.qiyuan.dao.TeacherMapper">

    <!--查询对应的老师-->
    <select id="getTeacher" resultMap="TeacherStudent">
        select * from teacher where id = #{id}
    </select>

    <!--查出来就是个 Teacher 对象-->
    <resultMap id="TeacherStudent" type="Teacher">
        <!-- 一样的属性就不写了 -->
        <!--复杂的属性，需要单独处理；集合 collection-->
        <!-- property 属性名 即 List<Student> students -->
        <!-- javaType 指的是 java 类型，如此处是 ArrayList，ofType 指的是对应的泛型 -->
        <!--这里的 type 是 Teacher 所以 column 中的 id 自然也是指 Teacher.id-->
        <collection property="students" javaType="ArrayList" ofType="Student" column="id" select="getStudentByTeacherId"/>
    </resultMap>

    <!--子查询语句，怎么获得 id 呢？通过上面的 column 传进来-->
    <select id="getStudentByTeacherId" resultType="Student">
        select * from student where tid = #{id}
    </select>

</mapper>
```

执行测试方法，得到的结果

```java
Teacher(id=0, name=祈鸢, 
        students=[Student(id=1, name=小明, tid=1), 
                  Student(id=2, name=小红, tid=1), 
                  Student(id=3, name=小张, tid=1), 
                  Student(id=4, name=小李, tid=1), 
                  Student(id=5, name=小王, tid=1)])
```

这里的 id=0 好像是因为 id 执行了子查询，被映射到 Student 里了···我也不太确定，后面再说吧。所以这里为了能正确映射，要添加

```xml
	...
    <resultMap id="TeacherStudent" type="Teacher">
        <!-- 一样的属性就不写了 -->
        <result property="id" column="id"/>
    	...
```

把查询到的 id 映射到老师的 id 中就正常了，还是少偷懒吧。

### 3. 按照结果嵌套处理

也是和之前一样，在接口中添加方法

```java
public interface TeacherMapper {
	...
    // 获取指定 id 老师的信息 包含其所有的学生，按照结果嵌套处理
    public Teacher getTeacher2(int id);
}
```

再写这个方法使用的 SQL 语句和结果集映射

```xml
<mapper namespace="com.qiyuan.dao.StudentMapper">
    
    ...
    
    <!--按照结果嵌套处理-->
    <select id="getTeacher2" resultMap="TeacherStudent2">
        select s.id as sid, s.name as sname, t.id as tid, t.name as tname
        from student s, teacher t
        where s.tid = t.id and t.id = #{id}
    </select>

    <resultMap id="TeacherStudent2" type="Teacher">
        <!--这里也不能偷懒！否则报错
        Expected one result (or null) to be returned by selectOne() -->
        
        <result property="id" column="tid"/>
        <result property="name" column="tname"/>
        <!-- ofType 属性的泛型 -->
        <collection property="students" ofType="Student">
            <!--对应学生的属性-->
            <result property="id" column="sid"/>
            <result property="name" column="sname"/>
            <result property="tid" column="tid"/>
        </collection>
    </resultMap>
    
</mapper>
```

查询到的结果，正确了

```java
Teacher(id=1, name=祈鸢, 
        students=[Student(id=1, name=小明, tid=1), 
                  Student(id=2, name=小红, tid=1), 
                  Student(id=3, name=小张, tid=1), 
                  Student(id=4, name=小李, tid=1), 
                  Student(id=5, name=小王, tid=1)])
```

写 SQL 语句的时候麻烦了一点，但写映射的时候很简单···这里又因为偷懒没写 result，报错找了好一会。虽然目前不太清楚原因，**但 result 该写的还是要写**！

### 4. 总结

两节把多对一和一对多的写法了解了一下

- 多对一，用 association，关联一个
- 一对多，用 collection，集合多个

还有 Type 的区别

- javaType：属性在 java 中的类型，如 Student、Teacher 这种类型，或者 ArrayList 这种带泛型的容器类型
- ofType：当 javaType 为 ArrayList 这种带泛型的容器类型时，就要写 ofType，即其中的泛型

还有几个比较重要的知识点

- Mysql 引擎
- InnoDB 底层原理
- 索引、索引优化

本节没搞明白的就是 result 这东西到底什么时候可以省略不写😵。

> 为什么相同可不写，不同必须写？
>
> 因为用了java反射技术，如果列名和实体类字段名不同，则反射不成功。