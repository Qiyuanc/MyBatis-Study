## MyBatis多对一处理

本节新建 MyBatis-06 项目学习 MyBatis 多个对象对应一个对象的处理。

多对一和一对多举例来说即多个学生对应一个老师的关系

- 对于学生来说，多个学生关联一个老师
- 对于老师来说，一个老师集合多个学生

在 MyBatis 中，可以使用 association 和 collection 处理这种关系

> association – 一个复杂类型的关联；许多结果将包装成这种类型
>
> - 嵌套结果映射 – 关联可以是 `resultMap` 元素，或是对其它结果映射的引用
>
> collection – 一个复杂类型的集合
>
> - 嵌套结果映射 – 集合可以是 `resultMap` 元素，或是对其它结果映射的引用

### 1. 搭建测试环境

#### 1.1 数据库

在数据库中执行

```sql
CREATE TABLE `teacher` (
  `id` INT(10) NOT NULL,
  `name` VARCHAR(30) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=INNODB DEFAULT CHARSET=utf8

INSERT INTO teacher(id, NAME) VALUES (1, '祈鸢');

CREATE TABLE `student` (
  `id` INT(10) NOT NULL,
  `name` VARCHAR(30) DEFAULT NULL,
  `tid` INT(10) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fktid` (`tid`),
  CONSTRAINT `fktid` FOREIGN KEY (`tid`) REFERENCES `teacher` (`id`)
) ENGINE=INNODB DEFAULT CHARSET=utf8 

INSERT INTO `student` (`id`, `name`, `tid`) VALUES (1, '小明', 1);
INSERT INTO `student` (`id`, `name`, `tid`) VALUES (2, '小红', 1);
INSERT INTO `student` (`id`, `name`, `tid`) VALUES (3, '小张', 1);
INSERT INTO `student` (`id`, `name`, `tid`) VALUES (4, '小李', 1);
INSERT INTO `student` (`id`, `name`, `tid`) VALUES (5, '小王', 1);
```

以创建测试数据，这里使用了外键（好像不建议使用）。

#### 1.2 编写代码

1. 创建  MyBatis-06 项目，导入 Lombok 依赖，补充完整资源文件 db,properties 和 mybatis-config.xml，并创建 MyBatisUtils 工具类

2. 创建实体类 Student 和 Teacher，其中学生类中维护一个对应的老师对象。

   ```java
   @Data
   public class Student {
       private int id;
       private String name;
       // 学生关联老师
       private Teacher teacher;
   }
   ```

   ```java
   @Data
   public class Teacher {
       private int id;
       private String name;
   }
   ```

3. 在 com.qiyuan.dao 下创建接口 StudentMapper 和 TeacherMapper

   ```java
   public interface StudentMapper {
       // 获取所有学生及其对应的老师信息
       public List<Student> getStudent();
   }
   ```

   ```java
   public interface TeacherMapper {
       public List<Teacher> getTeacher();
   }
   ```

4. 在资源文件夹下创建与接口对应的文件夹 com/qiyuan/dao（这里必须使用斜杠 / ！因为创建的是文件夹而不是包，使用点 . 的话会被当成一个文件夹！），在其中创建 StudentMapper.xml 和 TeacherMapper.xml

   ```xml
   <?xml version="1.0" encoding="UTF-8" ?>
   <!DOCTYPE mapper
           PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
           "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
   <mapper namespace="com.qiyuan.dao.StudentMapper">
       <select id="getStudent" resultType="Student">
           select * from mybatis.student
       </select>
   </mapper>
   ```

   ```xml
   <mapper namespace="com.qiyuan.dao.TeacherMapper">
       <select id="getTeacher" resultType="Teacher">
           select * from mybatis.teacher
       </select>
   </mapper>
   ```

5. 在 mybatis-config.xml 中注册以上两个配置文件（如果使用注解，要注册接口）

   这里用通配符 *.xml 会报错，为什么捏（还是说根本就不能用）？

   ```xml
   <mappers>
       <mapper resource="com/qiyuan/dao/StudentMapper.xml"/>
       <mapper resource="com/qiyuan/dao/TeacherMapper.xml"/>
   </mappers>
   ```

6. 写个测试方法测试一下

   这里我先写的查询所有学生的方法（方法略），直接就看到问题了

   ```java
   Student(id=1, name=小明, teacher=null)
   Student(id=2, name=小红, teacher=null)
   Student(id=3, name=小张, teacher=null)
   Student(id=4, name=小李, teacher=null)
   Student(id=5, name=小王, teacher=null)
   ```

   数据库中，学生表的字段是 tid，在实体类中（属性不可能用 tid 的，必须是一个老师对象才符合情景）属性却是 Teacher 对象，而 resultType 只能有一个类型。怎么把老师的信息放到学生里面呢？接下来也就是要解决这个问题。

### 2. 按照查询嵌套处理

现在来解决上面的问题，怎么才能查询到老师呢？一种思路是先查询所有学生，再根据学生中的 tid 去查询对应的老师，在 SQL 中就是一个子查询。

接口中的方法不用改变，要改变的是 StudentMapper.xml 中怎么用 SQL

```xml
<mapper namespace="com.qiyuan.dao.StudentMapper">

    <!--思路：1.查询所有学生信息
            2.根据查询到的学生信息查询对应的老师
            本质上：子查询                     -->
    <resultMap id="StudentTeacher" type="Student">
        <!--属性和字段本身就能对应起来的，可以不写-->
        <result property="id" column="id"/>
        <result property="name" column="name"/>
        <!--复杂的属性，需要单独处理；对象 association 集合 collection-->
        <!-- property 对象属性 colum 数据库列名-->
        <!-- javaType 查询出来的对象 类似 resultType select 子查询-->
        <association property="teacher" column="tid" javaType="Teacher" select="getTeacher"/>
    </resultMap>

    <select id="getStudent" resultMap="StudentTeacher">
        select * from mybatis.student
    </select>
    <!--根据 id 查询老师 只有一个参数不用写-->
    <select id="getTeacher" resultType="Teacher">
        select * from mybatis.teacher where id = #{tid}
    </select>

</mapper>
```

association 可以指定关联的 JavaBean 对象，property 指定哪个属性是关联的对象，column 是其在数据库中的字段，此处数据库字段 tid 与 teacher 也不一致，所以要指定一个查询通过 tid 查询到 teacher 对象。

看看查询的结果，没有问题

```java
Student(id=1, name=小明, teacher=Teacher(id=1, name=祈鸢))
Student(id=2, name=小红, teacher=Teacher(id=1, name=祈鸢))
Student(id=3, name=小张, teacher=Teacher(id=1, name=祈鸢))
Student(id=4, name=小李, teacher=Teacher(id=1, name=祈鸢))
Student(id=5, name=小王, teacher=Teacher(id=1, name=祈鸢))
```

**这种方法本质上其实是嵌套了一个子查询**，即先查学生再查对应的老师，对 Student 执行一次查询，在 Student 里再对 Teacher 执行一次查询。

### 3. 按照结果嵌套处理

如果觉得子查询不好使，还有第二种方法，即先用 SQL 语句将需要的属性、对象统一查询出来，再通过 resultMap 中 association 使查询出来的字段与属性对应

在接口中再创建一个方法

```java
public interface StudentMapper {
    ...
	 // 获取所有学生及其对应的老师信息，按照结果嵌套处理
    public List<Student> getStudent2();
}
```

再写这个方法使用的 SQL 语句和结果集映射

```xml
<mapper namespace="com.qiyuan.dao.StudentMapper">
    
    ...
    
    <!--按照结果嵌套处理-->
    <!--查询的结果 本质上还是个 Student-->
    <resultMap id="StudentTeacher2" type="Student">
        <!--属性和字段本身就能对应起来的，可以不写-->
        <result property="id" column="sid"/>
        <result property="name" column="sname"/>
        <!--对应，要查找的属性叫 teacher 它是 Teacher 类型的-->
        <association property="teacher" javaType="Teacher">
            <!--将查找出来的 tname 与 老师中的属性 name 对应-->
            <result property="name" column="tname"/>
        </association>
    </resultMap>

    <select id="getStudent2" resultMap="StudentTeacher2">
        select s.id as sid, s.name as sname, t.name as tname
        from student s, teacher t
        where s.tid = t.id
    </select>
    
</mapper>
```

测试方法中直接把 mapper 调用的方法改成 getStudent2 就行了，直接看结果

```java
Student(id=1, name=小明, teacher=Teacher(id=0, name=祈鸢))
Student(id=2, name=小红, teacher=Teacher(id=0, name=祈鸢))
Student(id=3, name=小张, teacher=Teacher(id=0, name=祈鸢))
Student(id=4, name=小李, teacher=Teacher(id=0, name=祈鸢))
Student(id=5, name=小王, teacher=Teacher(id=0, name=祈鸢))
```

老师的 id 都是0，原因很显然，这种方法是先查属性再做对应，这里一没查老师的 id 二没做对应，所以老师的 id 肯定显示不出来。

把老师的 id 也加进去

```xml
<mapper namespace="com.qiyuan.dao.StudentMapper">
    
    ...
    
    <!--按照结果嵌套处理-->
    <!--查询的结果 本质上还是个 Student-->
    <resultMap id="StudentTeacher2" type="Student">
        <!--属性和字段本身就能对应起来的，可以不写-->
        <result property="id" column="sid"/>
        <result property="name" column="sname"/>
        <!--对应，要查找的属性叫 teacher 它是 Teacher 类型的-->
        <association property="teacher" javaType="Teacher">
            <!--将查找出来的 tname 与 老师中的属性 name 对应-->
            <!-- ++++++加入 老师id 的对应--> 
            <result property="id" column="tid"/>
            <result property="name" column="tname"/>
        </association>
    </resultMap>

    <select id="getStudent2" resultMap="StudentTeacher2">
        <!-- ++++++多查一个 t.id 出来--> 
        select s.id as sid, s.name as sname, t.id as tid, t.name as tname
        from student s, teacher t
        where s.tid = t.id
    </select>
    
</mapper>
```

这样结果就是完整的了，同上面的完整结果。

### 4. 总结

总的来说，多对一的查询，有两种方法

- 按照查询嵌套处理：本质上是子查询，用当前查出来的信息再做一次查询
- 按照结果嵌套处理：本质上是联表查询，直接查出来所有信息再给它们对应上

我感觉第二种更好用，即没改变 SQL 本身，也不用写多个查询标签。

**注意**：如果在 IDEA 中设置了 SQL Dialects，则 SQL 语句中的表名前要加上数据库库名，否则会报红，不过其实不影响执行（猜测是 IDEA 无法识别到这张表，但执行的时候数据源中指定了数据库，所以能查到正确的表）。但去掉设置后 IDEA 无法给出输入提示了，如表名要纯手打。为了干净一点我后面还是去掉了。

**提醒**：在 IDEA 中使用数据库 Console，一定要对着要使用的数据库打开 Console，否则会识别不了表（在表前加上数据库名也行，不过多丑啊），痛苦😢。
