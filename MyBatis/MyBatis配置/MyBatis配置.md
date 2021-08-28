## MyBatis配置

这节创建一个新项目 MyBatis-Study / MyBatis-02 进行实践。在创建过程中发现了一个问题，之前的 MyBatis-01中用到了 try-with-resource，自动在 Maven 中添加了一个插件

```xml
<plugins>
    <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-compiler-plugin</artifactId>
        <configuration>
            <source>7</source>
            <target>7</target>
        </configuration>
    </plugin>
</plugins>
```

若删除这个插件，try-with-resource 代码块就会报 ↓ 的错误

```java
// java: -source 1.5 中不支持 try-with-resources
```

看来用 try-with-resource 还要多加一步，不过后面也不用了。

### 1. 核心配置文件

分析前先让 MyBatis-02 项目能运行起来。

1. 在 resources 文件夹下创建配置文件 mybatis-config.xml
2. 编写工具类 MyBatisUtils，方便后面获取 SqlSession
3. 三步走，UserMapper --> UserMapper.xml --> Test

这样 MyBatis-02 项目就能运行起来了。

MyBatis 的配置文件 mybatis-config.xml 包含了会深深影响 MyBatis 行为的设置和属性信息。 配置文档的顶层结构如下

```java
configuration（配置）
	properties（属性）
	settings（设置）
    typeAliases（类型别名）
    typeHandlers（类型处理器）
    objectFactory（对象工厂）
    plugins（插件）
    environments（环境配置）
		environment（环境变量）
            transactionManager（事务管理器）
            dataSource（数据源）
	databaseIdProvider（数据库厂商标识）
	mappers（映射器）
```

### 2. 环境配置（environments）

> MyBatis 可以配置成适应多种环境，这种机制有助于将 SQL 映射应用于多种数据库之中，例如，开发、测试和生产环境需要有不同的配置。**不过尽管可以配置多个环境，但每个 SqlSessionFactory 实例只能选择一种环境。**如果要连接两个数据库，就需要创建两个 SqlSessionFactory 实例，即**每个数据库对应一个 SqlSessionFactory 实例**。


假设现在有两个配置的环境 development 和 test
```xml
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
    <environment id="test">
        <transactionManager type="JDBC"></transactionManager>
        <dataSource type="POOLED"></dataSource>
    </environment>
</environments>
```

为了指定创建哪种环境，只要将它作为可选的参数传递给 SqlSessionFactoryBuilder 即可

```java
// 默认环境
SqlSessionFactory factory = new SqlSessionFactoryBuilder().build(reader);
// test环境
SqlSessionFactory factory = new SqlSessionFactoryBuilder().build(reader，"test");
```

也可以通过修改配置文件中的 default 参数来切换默认环境，如上面的 default="development" 修改为 default="test"，则创建默认环境时就会使用 test 环境。

#### 2.1 事务管理器（transactionManager）

> 在 MyBatis 中有两种类型的事务管理器（也就是 type = "[ JDBC | MANAGED ]"）
>
> - JDBC – 这个配置直接使用了 JDBC 的提交和回滚设施，它依赖从数据源获得的连接来管理事务作用域。
> - MANAGED – 这个配置几乎没做什么。它从不提交或回滚一个连接，而是让容器来管理事务的整个生命周期。

如果要使用 Spring + MyBatis，则没有必要配置事务管理器，因为 Spring 模块会使用自带的管理器来覆盖前面的配置。**反正知道事务管理器有两种类型就行了**。

#### 2.2 数据源（dataSource）

> dataSource 元素使用标准的 JDBC 数据源接口来配置 JDBC 连接对象的资源。有三种内建的数据源类型（也就是 type = "[ UNPOOLED | POOLED | JNDI ]"）

其中 UNPOOLED 为不使用连接池，POOLED 为使用连接池，JNDI 不太懂，应该也用不到。

**连接池**：对于大多数应用程序，当它们正在处理通常需要数毫秒完成的事务时，仅需要能够访问 JDBC 连接的 1 个线程。当不处理事务时，这个连接就会闲置。相反，连接池允许闲置的连接被其它需要的线程使用。

**总而言之，连接池避免了创建新的连接实例时所必需的初始化和认证时间**。 这种处理方式很流行，能使并发 Web 应用快速响应请求。

### 3. 属性（properties）

> 这些属性可以在外部进行配置，并可以进行动态替换。既可以在典型的 Java 属性文件中配置这些属性，也可以在 properties 元素的子元素中设置。

目前用到的外部配置文件就是 db.properties。在 resources 目录下创建 db.properties 配置文件，注意配置文件中的 url 不需要用 amp; 进行转义

```properties
driver=com.mysql.jdbc.Driver
url=jdbc:mysql://localhost:3306/mybatis?useUnicode=true&characterEncoding=utf-8&useSSL=true&serverTimezone=UTC
username=root
password=0723
```

有了 db.properties 后，在核心配置文件 mybatis-config.xml 中引入配置文件，就可以通过 ${ } 的方式获取属性值了。**注意**：xml 配置文件的标签顺序是规定好的，不能随便写标签！

```xml
<!--引入配置文件！都在resources目录下，不需要路径！-->
<properties resource="db.properties"/>

<environments default="development">
    <environment id="development">
        <transactionManager type="JDBC"/>
        <dataSource type="POOLED">
            <property name="driver" value="${driver}"/>
            <property name="url" value="${url}"/>
            <property name="username" value="${username}"/>
            <property name="password" value="${password}"/>
        </dataSource>
    </environment>
</environments>
```

properties 标签内也能添加新的属性值，如

```xml
<properties resource="db.properties">
    <property name="username" value="root"/>
    <property name="password" value="0723"/>
</properties>
```

> 如果一个属性在不只一个地方进行了配置，那么，MyBatis 将按照下面的顺序来加载：
>
> - 首先读取在 properties 元素体内指定的属性。
> - 然后根据 properties 元素中的 resource 属性读取类路径下属性文件，或根据 url 属性指定的路径读取属性文件，并覆盖之前读取过的同名属性。
> - 最后读取作为方法参数传递的属性，并覆盖之前读取过的同名属性。
>
> 因此，通过方法参数传递的属性具有最高优先级，resource/url 属性中指定的配置文件次之，最低优先级的则是 properties 元素中指定的属性。

### 4. 类型别名（typeAliases）

> 类型别名可为 Java 类型设置一个缩写名字。 它仅用于 XML 配置，意在降低冗余的全限定类名书写。

```xml
<!--给全限定名起别名-->
<typeAliases>
    <typeAlias type="com.qiyuan.entity.User" alias="User"/>
</typeAliases>
```

起好别名后测试一下，修改 UserMapper.xml 中的全限定名为别名

```xml
<!--select查询语句，返回类型要写全限定名-->
<!--修改：可以不用全限定名辣！-->
<select id="getUserList" resultType="User">
    select * from mybatis.user
</select>
```

查询成功，起的别名正常工作！

> 也可以指定一个包名，MyBatis 会在包名下面搜索需要的 Java Bean。每一个在包中的 Java Bean，在没有注解的情况下，会使用 Bean 的首字母小写的非限定类名来作为它的别名；若有注解，则别名为其注解值。

还有第二种方式，在 typeAliases 中指定一个包名，使用某个类的别名时（首字母大小写都行），会自动搜索这个包下匹配的类

```xml
<typeAliases>
    <package name="com.qiyuan.entity"/>
</typeAliases>
```

在 UserMapper.xml 中使用

```xml
<select id="getUserList" resultType="user">
    select * from mybatis.user
</select>
```

建议使用小写，这样就知道是通过扫描包的方式匹配的了。

实体类少的时候，建议使用第一种，可以自定义别名；

实体类多的时候，建议使用第二种，如果还想自定义别名，需要使用注解 @Alias(...)。

### 5. 设置（settings）

设置的配置实在太多，这里只写几个可能比较重要的，其他的用到再到官方文档查。

| 设置名                   | 描述                                                         | 有效值                                                       | 默认值 |
| :----------------------- | :----------------------------------------------------------- | :----------------------------------------------------------- | :----- |
| cacheEnabled             | 全局性地开启或关闭所有映射器配置文件中已配置的任何缓存。     | true \| false                                                | true   |
| lazyLoadingEnabled       | 延迟加载的全局开关。当开启时，所有关联对象都会延迟加载。 特定关联关系中可通过设置 `fetchType` 属性来覆盖该项的开关状态。 | true \| false                                                | false  |
| mapUnderscoreToCamelCase | 是否开启驼峰命名自动映射，即从经典数据库列名 A_COLUMN 映射到经典 Java 属性名 aColumn。 | true \| false                                                | false  |
| logImpl                  | 指定 MyBatis 所用日志的具体实现，未指定时将自动查找。        | SLF4J \| LOG4J \| LOG4J2 \| JDK_LOGGING \| COMMONS_LOGGING \| STDOUT_LOGGING \| NO_LOGGING | 未设置 |

### 6. 映射器（mappers）

> 既然 MyBatis 的行为已经由上述元素配置完了，我们现在就要来定义 SQL 映射语句了。 但首先，我们需要告诉 MyBatis 到哪里去找到这些语句。在自动查找资源方面，Java 并没有提供一个很好的解决方案，所以最好的办法是直接告诉 MyBatis 到哪里去找映射文件。

映射器即之前所说的注册 Mapper，因为 SQL 语句写在 Mapper.xml 中，所有需要让 MyBatis 知道去哪里能找到这个文件。

**方式一**：使用相对于类路径的资源引用

即之前使用的方法，注意此处使用的是斜杠 / 而不是 点 . 

```xml
<!-- 使用相对于类路径的资源引用 -->
<mappers>
    <mapper resource="com/qiyuan/dao/UserMapper.xml"/>
</mappers>
```

**方法二**：使用映射器接口实现类的完全限定类名

```xml
<!-- 使用映射器接口实现类的完全限定类名 -->
<mappers>
    <mapper class="com.qiyuan.dao.UserMapper"/>
</mappers>
```

使用这种方法，有两个点需要注意

- 接口和它的配置文件 Mapper.xml 必须同名！
- 接口和它的配置文件 Mapper.xml 必须在同一个包下！

因为名字不同，MyBatis 无法通过接口寻找到他对应的 Mapper.xml。

**方法三**：将包内的映射器接口实现全部注册为映射器

```xml
<mappers>
    <package name="com.qiyuan.dao"/>
</mappers>
```

这种方法的注意点和方法二一样。不过使用这种方法，也可以在 resources 下创建相同路径的包，将 Mapper.xml 放在对应的包下，也能寻找到。

### 7. 作用域（Scope）和生命周期

> 理解我们之前讨论过的不同作用域和生命周期类别是至关重要的，因为错误的使用会导致非常严重的**并发问题**。

![image-20210813180259123](F:\TyporaMD\MyBatis\MyBatis配置\image-20210813180259123.png)

#### 7.1 SqlSessionFactoryBuilder

- SqlSessionFactoryBuilder 的作用就是读取配置文件，根据配置的信息创建 SqlSessionFactory。
- 一旦创建了 SqlSessionFactory，就不再需要它了。
- 因此它的最佳作用域是局部变量。

#### 7.2 SqlSessionFactory

- SqlSessionFactory 一旦被创建就应该在应用的运行期间一直存在，没有任何理由丢弃它或重新创建另一个实例。
- SqlSessionFactory 可以理解为数据库连接池，需要连接数据库的时候，从中获取一个连接。
- 所以它的最佳作用域是应用作用域，即随着程序开始而产生，随着程序结束而销毁。最简单的就是使用单例模式或者静态单例模式

#### 7.3 SqlSession

- 每个线程都应该有它自己的 SqlSession 实例，SqlSession 的实例不是线程安全的，因此是不能被共享的。
- 可以理解为从连接池获取一个连接，因此需要打开的关闭。
- 它的最佳的作用域是请求或方法作用域，在一个方法中用的时候打开，用完就赶紧关闭，否则资源会被占用。

![image-20210813182024272](F:\TyporaMD\MyBatis\MyBatis配置\image-20210813182024272.png)

执行流程：从连接池 SqlSessionFactory 中获取一个连接 SqlSession，使用连接实例化接口，获得业务对象 Mapper，再用 Mapper 去执行业务（SQL语句）。

### 8. 总结

本节主要学习一下 MyBatis 配置文件中的一些属性的作用，不难且都是比较常用的属性，后面用着用着就会熟练。但需要理解作用域和生命周期的含义，避免后面发生问题😪。