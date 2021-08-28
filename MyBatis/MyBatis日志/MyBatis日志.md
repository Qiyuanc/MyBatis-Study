## MyBatis日志

这节使用新项目 MyBatis-04（同 MyBatis-03 ），研究一下 MyBatis 日志的配置使用。

### 1. 日志工厂

以前直接使用 JDBC 时，如果一个数据库操作出现了问题，一般只能使用 sout 和 debug 来发现问题；现在 MyBatis 日志就是最好的帮手！

要在 MyBatis 中使用日志，需要在 mybatis-config.xml 中进行配置

| 设置名  | 描述                                                  | 有效值                                                       | 默认   |
| :------ | :---------------------------------------------------- | :----------------------------------------------------------- | :----- |
| logImpl | 指定 MyBatis 所用日志的具体实现，未指定时将自动查找。 | SLF4J \| LOG4J \| LOG4J2 \| JDK_LOGGING \| COMMONS_LOGGING \| STDOUT_LOGGING \| NO_LOGGING | 未设置 |

其中，有几个比较重要的日志格式

- STDOUT_LOGGING：标准日志
- LOG4J：即 Java 日志（ Log for Java），下面要用到的日志格式
- LOG4J2：即 LOG4J 的升级版
- SLF4J：即简单日志门面( Simple Logging Facade for Java )，不是具体的日志解决方案，它只服务于各种各样的日志系统，以后会了解

此处先使用标准日志了解一下日志的作用，在 mybatis-config.xml 中配置

```xml
<settings>
    <!--标准的日志工厂实现-->
    <setting name="logImpl" value="STDOUT_LOGGING"/>
</settings>
```

注意：这里的配置要完全遵守格式，大小写不能错，也不能多加空格，否则会报错！

配置完日志后再运行根据 ID 查询的 Test 方法，控制台输出的日志内容为

```java
// 日志初始化 加载 StdOutImpl 适配器
Logging initialized using 'class org.apache.ibatis.logging.stdout.StdOutImpl' adapter.
// ...
// 打开 JDBC 连接 说明 MyBatis 的本质还是 JDBC 只不过封装起来了
Opening JDBC Connection
Loading class `com.mysql.jdbc.Driver'. // ...
// 获取了一个连接！
Created connection 1263668904.
// 设置事务开启
Setting autocommit to false on JDBC Connection [com.mysql.cj.jdbc.ConnectionImpl@4b520ea8]
// 执行的 SQL 语句 看的很清楚！
==>  Preparing: select * from mybatis.user where id = ?
// 参数
==> Parameters: 1(Integer)
// 执行结果 列名 行 总数
<==    Columns: id, name, pwd
<==        Row: 1, 祈鸢, 123456
<==      Total: 1
// 测试的输出
User{id=1, name='祈鸢', password='123456'}
// 关闭事务
Resetting autocommit to true on JDBC Connection [com.mysql.cj.jdbc.ConnectionImpl@4b520ea8]
// 关闭 JDBC 连接
Closing JDBC Connection [com.mysql.cj.jdbc.ConnectionImpl@4b520ea8]
// 将获取的连接返回到 连接池 pool 中！ 此处可以帮助理解连接池
Returned connection 1263668904 to pool.

```

从这里例子就可以看出日志的强大了，底裤都看穿了😆。

### 2. Log4j

#### 2.1 什么是Log4j

- Log4j 是 Apache 的一个开源项目，通过使用 Log4j ，可以控制日志信息输送的目的地是控制台、文件、GUI 组件、甚至是套接口服务器、NT 的事件记录器、UNIX Syslog 守护进程等
- 也可以控制每一条日志的输出格式
- 通过定义每一条日志信息的级别，能够更加细致地控制日志的生成过程
- 这些都可以通过一个配置文件来灵活地进行配置，而不需要修改应用的代码

#### 2.2 使用Log4j

在 Maven 中导入 log4j 的 jar 包

```xml
<!-- log4j 日志依赖 -->
<dependency>
    <groupId>log4j</groupId>
    <artifactId>log4j</artifactId>
    <version>1.2.17</version>
</dependency>
```

在资源文件夹 resources 下创建 log4j 的配置文件 log4j.properties

```properties
#将等级为DEBUG的日志信息输出到console和file这两个目的地，console和file的定义在下面的代码
log4j.rootLogger=DEBUG,console,file

#控制台输出的相关设置
log4j.appender.console = org.apache.log4j.ConsoleAppender
log4j.appender.console.Target = System.out
log4j.appender.console.Threshold=DEBUG
log4j.appender.console.layout = org.apache.log4j.PatternLayout
log4j.appender.console.layout.ConversionPattern=[%c]-%m%n

#文件输出的相关设置
log4j.appender.file = org.apache.log4j.RollingFileAppender
log4j.appender.file.File=./log/qiyuan.log
log4j.appender.file.MaxFileSize=10mb
log4j.appender.file.Threshold=DEBUG
log4j.appender.file.layout=org.apache.log4j.PatternLayout
log4j.appender.file.layout.ConversionPattern=[%p][%d{yy-MM-dd}][%c]%m%n

#日志输出级别
log4j.logger.org.mybatis=DEBUG
log4j.logger.java.sql=DEBUG
log4j.logger.java.sql.Statement=DEBUG
log4j.logger.java.sql.ResultSet=DEBUG
log4j.logger.java.sql.PreparedStatement=DEBUG
```

在 mybatis-config.xml 中使用 log4j 作为日志格式

```xml
<settings>
    <!--使用 log4j 注意不能打错-->
    <setting name="logImpl" value="LOG4J"/>
</settings>
```

运行测试方法，看看控制台的输出

```java
// ... 乱七八糟的东西还有乱码
[org.apache.ibatis.transaction.jdbc.JdbcTransaction]-Opening JDBC Connection
Loading class `com.mysql.jdbc.Driver'. This is deprecated. The new driver class is `com.mysql.cj.jdbc.Driver'. The driver is automatically registered via the SPI and manual loading of the driver class is generally unnecessary.
[org.apache.ibatis.datasource.pooled.PooledDataSource]-Created connection 605052357.
[org.apache.ibatis.transaction.jdbc.JdbcTransaction]-Setting autocommit to false on JDBC Connection [com.mysql.cj.jdbc.ConnectionImpl@24105dc5]
[com.qiyuan.dao.UserMapper.getUserById]-==>  Preparing: select * from mybatis.user where id = ?
[com.qiyuan.dao.UserMapper.getUserById]-==> Parameters: 1(Integer)
[com.qiyuan.dao.UserMapper.getUserById]-<==      Total: 1
User{id=1, name='祈鸢', password='123456'}
[org.apache.ibatis.transaction.jdbc.JdbcTransaction]-Resetting autocommit to true on JDBC Connection [com.mysql.cj.jdbc.ConnectionImpl@24105dc5]
[org.apache.ibatis.transaction.jdbc.JdbcTransaction]-Closing JDBC Connection [com.mysql.cj.jdbc.ConnectionImpl@24105dc5]
[org.apache.ibatis.datasource.pooled.PooledDataSource]-Returned connection 605052357 to pool.

```

可以看到和之前的方式其实差别不大，但多了一些前缀，更加详细。同时因为在日志的配置中配置了文件输出日志，所以可以看到 MyBatis-04 / log / qiyuan.log 的日志文件，内容和控制台一样。

**发现问题**：若在 mybatis-config.xml 中使用了扫描包的方式，如 typeAlias 和 mapper，输出的日志文件上会有一个问号且无法打开。

**解决方法**：百度的都说就不用包扫描了，这么干确实可以，但还是有点逆天。

#### 2.3 Log4j日志级别

Log4j 有三个级别，分别是 info、debug、error，用于输出不同级别的信息

创建一个 TestLog4j 测试一下

```java
public class UserMapperTest {
    // 要经常使用，提升作用域
    static Logger logger = Logger.getLogger(UserMapperTest.class);

    @Test
    public void TestLog4j(){
        // 输出的参数类型是 Object
        // 用于输出普通信息，如 SQL 语句
        logger.info("info：info:这是一条 info 信息");
        // 用于 debug 某处是否有问题
        logger.debug("debug：这是一条 debug 信息");
        // 类似 try-catch 获取错误信息
        logger.error("error：这是一条 error 信息");
    }
}
```

有了 logger 对象后，就可以用它来写日志了，作用相当于之前的 sout；运行测试

控制台输出，这里看不出很明显的信息

```java
[com.qiyuan.dao.UserMapperTest]-info：info:这是一条 info 信息
[com.qiyuan.dao.UserMapperTest]-debug：这是一条 debug 信息
[com.qiyuan.dao.UserMapperTest]-error：这是一条 error 信息
```

查看 qiyuan.log 日志文件

```java
[INFO][21-08-14][com.qiyuan.dao.UserMapperTest]info：info:这是一条 info 信息
[DEBUG][21-08-14][com.qiyuan.dao.UserMapperTest]debug：这是一条 debug 信息
[ERROR][21-08-14][com.qiyuan.dao.UserMapperTest]error：这是一条 error 信息
```

可以看到有很明显的不同级别的前缀和输出时间，可以很方便的定位什么时间出了什么样的问题。这也就是日志的作用吧。

### 3. 总结

本节主要是学习怎么用日志和日志中一些配置的作用，而且只了解了一下 log4j，还有很多其他日志没看过。

以后建议 sout 操作直接改成日志输出，多看两眼日志总不是坏事👀。
