## MyBatis初见

### 1. MyBatis简介

#### 1.1 什么是MyBatis

1. MyBatis 是一款优秀的**持久层框架**，它支持自定义 SQL、存储过程以及高级映射；
2. MyBatis 免除了几乎所有的 JDBC 代码以及设置参数和获取结果集的工作；
3. MyBatis 可以通过简单的 XML 或注解来配置和映射原始类型、接口和 Java POJO为数据库中的记录。

#### 1.2 如何获得MyBatis

- Maven仓库：https://mvnrepository.com/artifact/org.mybatis/mybatis/3.5.7

```xml
<!-- https://mvnrepository.com/artifact/org.mybatis/mybatis -->
<dependency>
    <groupId>org.mybatis</groupId>
    <artifactId>mybatis</artifactId>
    <version>3.5.7</version>
</dependency>
```

- Github源码：https://github.com/mybatis/mybatis-3
- 中文文档：https://mybatis.org/mybatis-3/zh/index.html

#### 1.3 持久层

**数据持久化**：持久化是将程序数据在持久状态和瞬时状态间转换的机制。

通俗的讲，就是瞬时数据（比如内存中的数据，是不能长久保存的）持久化为持久数据（比如持久化至数据库中，能够长久保存）。

**数据持久层**：完成持久化工作的代码块，将数据持久化保存。

由于对象范例和关系范例这两大领域之间存在“阻抗不匹配”，所以把数据持久层单独作为J2EE体系的一个层提出来的原因就是能够在对象－关系数据库之间提供一个成功的企业级映射解决方案，尽最大可能弥补这两种范例之间的差异。

#### 1.4  MyBatis优点

1. 简单：本身就很小且没有任何第三方依赖，最简单安装只要两个jar文件+配置几个SQL映射文件。
2. 灵活：MyBatis不会对应用程序或者数据库的现有设计强加任何影响。 SQL写在xml里，便于统一管理和优化。通过SQL语句可以满足操作数据库的所有需求。
3. 解除SQL与程序代码的耦合：通过提供DAO层，将业务逻辑和数据访问逻辑分离，使系统的设计更清晰，更易维护，更易单元测试。SQL和代码的分离，提高了可维护性。
4. 提供映射标签，支持对象与数据库的orm字段关系映射。（？）
5. 提供对象关系映射标签，支持对象关系组建维护。（？）
6. 提供xml标签，支持编写动态SQL。（？）

### 2. 第一个MyBatis程序

流程：搭建环境 --> 导入MyBatis --> 编写代码 --> 测试

#### 2.1 搭建环境

1. 在数据库中执行 ↓ ，以创建基本的数据

```sql
CREATE DATABASE `mybatis`;

use `mybatis`;

CREATE TABLE `user`(
	`id` INT PRIMARY KEY,
	`name` VARCHAR(20) DEFAULT NULL,
	`pwd` VARCHAR(20) DEFAULT NULL
)ENGINE = INNODB DEFAULT CHARSET = utf8;

#什么时候用 ` 什么时候用 '
INSERT INTO `user`(`id`,`name`,pwd) VALUES
(1,'祈鸢','123456'),
(2,'Qiyuanc','123456'),
(3,'風栖祈鸢','07230723')
```

2. 创建新的空Maven项目MyBatis-Study，然后查看一下IDEA的设置中Maven的路径

![image-20210811153939273](F:\TyporaMD\MyBatis\MyBatis初见\image-20210811153939273.png)

果然变成Maven默认的路径了，我说之前JavaWeb的时候Maven怎么把jar包下到C盘里了...虽然也能用但肯定不太好，调整成配置好的就行。

3. 删除src目录，把这个项目作为父项目并导入依赖

   ```xml
   <!--父工程-->
   <groupId>org.example</groupId>
   <artifactId>MyBatis-Study</artifactId>
   <version>1.0-SNAPSHOT</version>
   
   <dependencies>
       <!--Mysql驱动-->
       <dependency>
           <groupId>mysql</groupId>
           <artifactId>mysql-connector-java</artifactId>
           <version>8.0.22</version>
       </dependency>
       <!--MyBatis-->
       <dependency>
           <groupId>org.mybatis</groupId>
           <artifactId>mybatis</artifactId>
           <version>3.5.7</version>
       </dependency>
       <!--junit-->
       <!--Junit单元测试-->
       <dependency>
           <groupId>org.junit.jupiter</groupId>
           <artifactId>junit-jupiter-api</artifactId>
           <version>5.7.2</version>
           <scope>test</scope>
       </dependency>
   </dependencies>
   ```

#### 2.2 编写MyBatis工具类

在MyBatis-Study父项目中创建MyBatis-01子项目，子项目有父项目导入的jar包，省去了很多麻烦。

##### 2.2.1 配置核心文件

在src/main/resourse中配置核心文件mybatis-config.xml

```xml
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE configuration
        PUBLIC "-//mybatis.org//DTD Config 3.0//EN"
        "http://mybatis.org/dtd/mybatis-3-config.dtd">
<configuration>
    <environments default="development">
        <environment id="development">
            <transactionManager type="JDBC"/>
            <dataSource type="POOLED">
                <property name="driver" value="com.mysql.jdbc.Driver"/>
                <property name="url" value="jdbc:mysql://localhost:3306/mybatis?useUnicode=true&amp;characterEncoding=utf-8&amp;useSSL=true&amp;serverTimezone=UTC"/>
                <property name="username" value="root"/>
                <property name="password" value="0723"/>
            </dataSource>
        </environment>
    </environments>
</configuration>
```

##### 2.2.2 获取SqlSessionFactory

```java
public class MyBatisUtils {
    // 提升作用域
    private static SqlSessionFactory sqlSessionFactory;
    static {
        try {
            // 使用MyBatis第一步：获取SqlSessionFactory对象
            String resource = "org/mybatis/example/mybatis-config.xml";
            // 要导org.apache.ibatis.io.Resources的包！ Maven犯病严重
            InputStream inputStream = Resources.getResourceAsStream(resource);
            sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
```

每个基于 MyBatis 的应用都是以一个 SqlSessionFactory 的实例为核心的。SqlSessionFactory 的实例可以通过 SqlSessionFactoryBuilder 获得。而 SqlSessionFactoryBuilder 则可以从 XML 配置文件或一个预先配置的 Configuration 实例来构建出 SqlSessionFactory 实例。

##### 2.2.3 获取SqlSession

```java
public class MyBatisUtils {
	...
        
    // 从SqlSessionFactory中获取SqlSession
    public static SqlSession getSqlSession(){
        // sqlSession 其实类似于 connection
        SqlSession sqlSession = sqlSessionFactory.openSession();
        return sqlSession;
    }
}
```

既然有了 SqlSessionFactory，顾名思义,可以从中获得 SqlSession 的实例。SqlSession 提供了在数据库执行 SQL 命令所需的所有方法。可以通过 SqlSession 实例来直接执行已映射的 SQL 语句。

有了MyBatisUtils工具类就可以连接上数据库了。

#### 2.3 编写User代码

##### 2.3.1 实体类User

写一个简单点的实体类用于测试，无参构造器和get/set方法在此省略

```java
public class User {
    private int id;
    private String name;
    private String pwd;
    
    ...
}
```

##### 2.3.1 Dao接口UserDao

接口中有要对数据进行的操作，后面会叫做Mapper，但其实是一个东西

```java
public interface UserDao {
    public List<User> getUserList();
}
```

##### 2.3.2 接口实现类UserMapper.xml

**重点**：与直接使用JDBC时写一个UserDaoImpl类不同，MyBatis使用Mapper配置文件（UserMapper.xml）的方式去”实现“这个接口

```xml
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE mapper
        PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
        "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<!--命名空间namespace要绑定一个对应的Dao/Mapper接口，相当于实现它-->
<mapper namespace="com.qiyuan.dao.UserDao">
    <!--select查询语句，返回类型要写全限定名-->
    <select id="getUserList" resultType="com.qiyuan.entity.User">
        select * from mybatis.user
    </select>
</mapper>
```

**命名空间**：在之前版本的 MyBatis 中，**命名空间（Namespaces）**的作用并不大，是可选的。 但现在，随着命名空间越发重要，你 必须指定命名空间。

命名空间的作用有两个，一个是利用更长的全限定名来将不同的语句隔离开来，同时也实现了上面的**接口绑定**。

#### 2.4 测试

这里使用Junit进行测试。同时在Maven项目中，写测试时，最好写到src/test/java文件下，且测试文件的包名与被测试的类最好一一对应！

如要测试src/main/java中的com.qiyuan.dao.UserDao类，最好将在src/test/java中创建com.qiyuan.dao.UserDaoTest类！

```java
public class UserDaoTest {
    @Test
    public void Test(){
        // 第一步 获取sqlSession对象，相当于connection
        SqlSession sqlSession = MyBatisUtils.getSqlSession();

        // 方式一：sqlSession.getMapper 这里应该叫做UserMapper
        UserDao mapper = sqlSession.getMapper(UserDao.class);
        // 可以调用接口中被UserMapper实例化后的方法
        List<User> userList = mapper.getUserList();

        // 方法二：sqlSession.select...() 已经不用了
        // 直接把方法路径传进去，并且要select对应的类型 one list map...
        //List<User> userList = sqlSession.selectList("com.qiyuan.dao.UserDao.getUserList");

        for (User user : userList) {
            System.out.println(user);
        }
        sqlSession.close();
    }
}
```

##### 2.4.1 Mapper.xml注册问题

此时直接运行测试，必定会报错

```java
org.apache.ibatis.binding.BindingException: Type interface com.qiyuan.dao.UserDao is not known to the MapperRegistry.
```

什么问题？MapperRegistry即Mapper注册，说明缺少了Mapper的注册信息！即每一个Mapper.xml，都要在MyBatis的核心配置文件mybatis-config.xml中注册，类似Servlet！

```xml
<!--头部信息-->
<configuration>
    <environments default="development">
		...
    </environments>
    <!--每个Mapper.xml都要在MyBatis核心配置文件中注册！类似Servlet！-->
    <mappers>
        <mapper resource="com/qiyuan/dao/UserMapper.xml"/>
    </mappers>
</configuration>
```

##### 2.4.2 Mavne配置问题

注册完Mapper后再次运行，仍然会报错，这是一个非常抽象的错

```java
java.lang.ExceptionInInitializerError //初始化错误
//...
### Cause: org.apache.ibatis.builder.BuilderException: Error parsing SQL Mapper Configuration. Cause: java.io.IOException: Could not find resource com/qiyuan/dao/UserMapper.xml
// 找不到资源文件UserMapper.xml？明明注册了！
```

回想我在CSDN发布第一篇笔记Maven学习笔记，已经过去了28天。在这篇笔记的最后，提到

> Maven由于约定大于配置，我们写的配置文件可能无法导出或生效，解决方案
>
> ```xml
> <build>
>     <resources>
>         <!--本来也能导出-->
>         <resource>
>             <directory>src/main/resources</directory>
>             <excludes>
>                 <exclude>**/*.properties</exclude>
>                 <exclude>**/*.xml</exclude>
>             </excludes>
>             <filtering>false</filtering>
>         </resource>
>         <!--让java目录下的properties和xml文件也能被导出-->
>         <resource>
>             <directory>src/main/java</directory>
>             <includes>
>                 <include>**/*.properties</include>
>                 <include>**/*.xml</include>
>             </includes>
>             <filtering>false</filtering>
>         </resource>
>     </resources>
> </build>
> ```
>
> 目前还用不到。。。

那就只能说，28天后就要用到了。因为 Maven 约定大于配置，导致 com.qiyuan.dao 下的 UserMapper.xml 无法被导出到 target 中，才会报找不到资源文件的错。

**注意**：在我这里，若把整段配置都放到 pom.xml 中，会导致 mybatis-config.xml 找不到，应该是第一段的问题，更改了默认 resources 目录的导出，把它删掉就好了。为了保险起见，在父项目和子项目的 pom.xml 中都放了一段。

### 3. 总结

使用了MyBatis之后，数据库相关操作的代码编写从实体类——Dao接口类——接口实现类变成了实体类——Dao接口类——Mapper.xml。

MyBatis 省去了接口实现类中复杂的 JDBC 操作，如创建关闭 PreparedStatement、ResultSet 对象，给实体类赋值返回等，极大地简化了持久层代码的编写。

#### 3.1 作用域和生命周期

理解不同作用域和生命周期类别是至关重要的，因为错误的使用会导致非常严重的并发问题。

- **SqlSessionFactoryBuilder**

  这个类可以被实例化、使用和丢弃，一旦创建了 SqlSessionFactory，就不再需要它了。 因此 SqlSessionFactoryBuilder 实例的最佳作用域是方法作用域（也就是局部方法变量）。

  在上面的例子中，MyBatisUtils中的静态代码块就承担了创建SqlSessionFactory的任务。

- **SqlSessionFactory**

  SqlSessionFactory 一旦被创建就应该在应用的运行期间一直存在，没有任何理由丢弃它或重新创建另一个实例。 使用 SqlSessionFactory 的最佳实践是在应用运行期间不要重复创建多次，多次重建 SqlSessionFactory 被视为一种代码“坏习惯”。因此 SqlSessionFactory 的最佳作用域是应用作用域。 有很多方法可以做到，最简单的就是使用单例模式或者静态单例模式。

- **SqlSession**

  每个线程都应该有它自己的 SqlSession 实例。SqlSession 的实例不是线程安全的，因此是不能被共享的，所以它的最佳的作用域是请求或方法作用域。 绝对不能将 SqlSession 实例的引用放在一个类的静态域，甚至一个类的实例变量也不行。 也绝不能将 SqlSession 实例的引用放在任何类型的托管作用域中，比如 Servlet 框架中的 HttpSession。 如果你现在正在使用一种 Web 框架，考虑将 SqlSession 放在一个和 HTTP 请求相似的作用域中。 换句话说，每次收到 HTTP 请求，就可以打开一个 SqlSession，返回一个响应后，就关闭它。 这个关闭操作很重要，为了确保每次都能执行关闭操作，可以使用 try-with-resource 操作。

  将上面的测试代码优化修改为

  ```java
  public class UserDaoTest {
      @Test
      public void Test(){
          // 第一步 获取sqlSession对象，相当于connection
          //SqlSession sqlSession = MyBatisUtils.getSqlSession();
  
          // 使用try-with-resources自动关闭资源
          try (SqlSession sqlSession = MyBatisUtils.getSqlSession()) {
              // 方式一：sqlSession.getMapper 这里应该叫做UserMapper
              UserDao mapper = sqlSession.getMapper(UserDao.class);
              // 可以调用接口中被UserMapper实例化后的方法
              List<User> userList = mapper.getUserList();
  
              // 方法二：sqlSession.select...() 已经不用了
              // 直接把方法路径传进去，并且要select对应的类型 one list map...
              //List<User> userList = sqlSession.selectList("com.qiyuan.dao.UserDao.getUserList");
  
              for (User user : userList) {
                  System.out.println(user);
              }
          }
      }
  }
  ```

  就可以实现 sqlSession 的自动关闭。后面学习的时候为了方便就先不用这个方式了🧐。

