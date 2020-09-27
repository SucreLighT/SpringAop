# Spring AOP

AOP：全称是Aspect Oriented Programming，即：面向切面编程。简单的说就是把程序重复的代码抽取出来，在需要执行的时候，使用**动态代理**的技术，在不修改源码的基础上，对已有方法进行增强。

+ 作用： 在程序运行期间，不修改源码对已有方法进行增强。 
+ 优势： 减少重复代码，提高开发效率，维护方便。
+ 实现技术：动态代理



## 问题分析

### 问题

在之前的案例中，事务是被自动控制的，即使用了connection对象的setAutoCommit(true) 方法控制事务，如果都执行一条sql语句是没有问题的，但是如果业务方法一次要执行多条sql语句，这种方式就无法实现功能了。

例如：实现账户的转账功能，需要查询转出账户-查询转入账户-转出账户减钱-转入账户加钱，当其中环节出现异常时，如在3-4之间，则转出账户减钱完成，而转入账户不会加钱。因为每次执行的持久层方法都是独立事务，导致无法实现事务控制，事务的一致性被破坏。

```java
public void transfer(String sourceName, String targetName, Float money) {
        System.out.println("transfer....");
        //2.1根据名称查询转出账户
        Account source = accountDao.findAccountByName(sourceName);
        //2.2根据名称查询转入账户
        Account target = accountDao.findAccountByName(targetName);
        //2.3转出账户减钱
        source.setMoney(source.getMoney() - money);
        //2.4转入账户加钱
        target.setMoney(target.getMoney() + money);
        //2.5更新转出账户
        accountDao.updateAccount(source);
        int i = 1 / 0;
        //2.6更新转入账户
        accountDao.updateAccount(target);
}
```

代码分析：在2.5之后由于出现了除数为0的异常，程序执行会中断，但2.5的事务将会提交到数据库中而2.6的事务将不会执行。



### 解决的思路

使用一个connection执行所有的操作。使用TreadLocal对象把Connection和当前线程绑定，从而使得一个线程中只有一个控制事务的对象，在业务层来控制事务的提交和回滚。

1. 数据库连接的工具类，实现从数据源中获取连接，并将连接和当前线程绑定

   ```java
   public class ConnectionUtils {
       private ThreadLocal<Connection> threadLocal = new ThreadLocal<Connection>();
       private DataSource dataSource;
       /**
        * 提供set方法用于注入
        */
       public void setDataSource(DataSource dataSource) {
           this.dataSource = dataSource;
       }
       /**
        * 获取当前线程上的连接
        */
       public Connection getThreadConnection() {
           try {
               //1.先从ThreadLocal上获取
               Connection conn = threadLocal.get();
               //2.判断当前线程上是否有连接
               if (conn == null) {
                   //3.从数据源中获取一个连接，并且存入ThreadLocal中
                   conn = dataSource.getConnection();
                   threadLocal.set(conn);
               }
               //4.返回当前线程上的连接
               return conn;
           } catch (Exception e) {
               throw new RuntimeException(e);
           }
       }
       /**
        * 把连接和线程解绑
        */
       public void removeConnection() {
           threadLocal.remove();
       }
   }
   ```

2. 事务管理的工具类，包括事务的开启，提交，回滚以及关闭连接，在业务层被调用用于管理事务。

   ```java
   public class TransactionManager {
       private ConnectionUtils connectionUtils;
   
       public void setConnectionUtils(ConnectionUtils connectionUtils) {
           this.connectionUtils = connectionUtils;
       }
   
       public void beginTransaction() {
           try {
               connectionUtils.getThreadConnection().setAutoCommit(false);
           } catch (SQLException e) {
               e.printStackTrace();
           }
       }
   
       public void commit() {
           try {
               connectionUtils.getThreadConnection().commit();
           } catch (SQLException e) {
               e.printStackTrace();
           }
       }
   
       public void rollback() {
           try {
               connectionUtils.getThreadConnection().rollback();
           } catch (SQLException e) {
               e.printStackTrace();
           }
       }
   
       public void release() {
           try {
               connectionUtils.getThreadConnection().close();
               connectionUtils.removeConnection();;
           } catch (SQLException e) {
               e.printStackTrace();
           }
       }
   }
   ```

3. 在业务层Service中调用事务管理

   ```java
   public void transfer(String sourceName, String targetName, Float money) {
       try {
           //1.开启事务
           transactionManager.beginTransaction();
           //2.执行操作
           System.out.println("transfer....");
           //2.1根据名称查询转出账户
           Account source = accountDao.findAccountByName(sourceName);
           //2.2根据名称查询转入账户
           Account target = accountDao.findAccountByName(targetName);
           //2.3转出账户减钱
           source.setMoney(source.getMoney() - money);
           //2.4转入账户加钱
           target.setMoney(target.getMoney() + money);
           //2.5更新转出账户
           accountDao.updateAccount(source);
           int i = 1 / 0;
           //2.6更新转入账户
           accountDao.updateAccount(target);
           //3.提交事务
           transactionManager.commit();
       } catch (Exception e) {
           //4.回滚事务
           transactionManager.rollback();
           e.printStackTrace();
       } finally {
           //5.释放连接
           transactionManager.release();
       }
   }
   ```

4. 在bean.xml中对新增类的成员变量进行注入

   ```xml
   <!-- 配置Service -->
   <bean id="accountService" class="cn.sucrelt.service.impl.AccountServiceImpl">
       <!-- 注入dao -->
       <property name="accountDao" ref="accountDao"/>
       <!--注入事务管理器-->
       <property name="transactionManager" ref="transactionManager"/>
   </bean>
   <!--配置Dao对象-->
   <bean id="accountDao" class="cn.sucrelt.dao.impl.AccountDaoImpl">
       <!-- 注入QueryRunner -->
       <property name="runner" ref="runner"/>
       <!--注入ConnectionUtils-->
       <property name="connectionUtils" ref="connectionUtils"/>
   </bean>
   <!--配置QueryRunner-->
   <bean id="runner" class="org.apache.commons.dbutils.QueryRunner" scope="prototype"/>
   <!--配置Connection工具类-->
   <bean id="connectionUtils" class="cn.sucrelt.utils.ConnectionUtils">
       <!--注入数据源-->
       <property name="dataSource" ref="dataSource"/>
   </bean>
   <!--配置事务管理器-->
   <bean id="transactionManager" class="cn.sucrelt.utils.TransactionManager">
       <property name="connectionUtils" ref="connectionUtils"/>
   </bean>
   <!-- 配置数据源 -->
   <bean id="dataSource" class="com.mchange.v2.c3p0.ComboPooledDataSource">
       <!--连接数据库的必备信息-->
       <property name="driverClass" value="com.mysql.jdbc.Driver"/>
       <property name="jdbcUrl"
                 value="jdbc:mysql://localhost:3306/springioc?characterEncoding=UTF-8&amp;serverTimezone=GMT"/>
       <property name="user" value="root"/>
       <property name="password" value="123456"/>
   </bean>
   ```



通过对业务层改造，在业务层实现事务控制，但是由于添加了事务控制，需要增加建立连接相关的ConnectionUtils类和事物管理相关的TransactionManager类，从而导致业务层方法变得臃肿，spring配置中的配置和注入变得复杂，同时充斥着很多重复代码，并且业务层方法和事务控制方法耦合了。 此时提交，回滚，释放资源中任何一个方法名变更，都需要修改业务层的代码，而且还只是一个业务层实现类，而实际的项目中这种业务层实现类可能有十几个甚至几十个。



## 动态代理

### 动态代理

+ 特点：字节码随用随创建，随用随加载，区别于静态代理（装饰者模式）必须写好一个类来代理。

+ 作用：不修改源码的基础上对方法进行增强。

+ 分类：基于接口；基于子类。

  

### 动态代理的方式

#### 1.基于接口的动态代理

1. 使用Proxy类中的newProxyInstance方法
2. 创建代理对象的**要求：被代理类中至少实现了一个接口**，如果没有则不能使用。
3. newProxyInstance的参数：
   + ClassLoader：类加载器，用于加载代理对象的字节码，和被代理对象使用相同的类加载器，代理哪个类xxx，就写xxx.getClass().getClassLoader()。
   + Class[]：字节码数组，用于让代理对象和被代理对象有相同的方法，即让二者实现相同的接口，代理哪个类xxx。就写xxx.getClass().getInterfaces()。
   + InvocationHandler：用于提供增强的代码，即如何实现代理，一般是写一个该接口的实现类，通常是匿名内部类。

4. 匿名内部类InvocationHandler中包含方法`public Object invoke(Object proxy, Method method, Object[] args)`
   + 代理对象执行任何方法时都会经过该方法，进行方法的拦截并增强
   + proxy：代理对象的引用
   + method：当前执行的方法
   + args：当前执行方法的参数列表
   + return：返回值与当前执行的方法的返回值相同

5. 实例

   生产厂家的接口

   ```java
   public interface InterfaceProducer {
       /**
        * 销售
        */
       public void saleProduct(float money);
   
       /**
        * 售后
        */
       public void afterService(float money);
   }
   ```

   生产厂家的类

   ```java
   public class Producer implements InterfaceProducer{
       /**
        * 销售
        */
       public void saleProduct(float money) {
           System.out.println("销售产品，并拿到钱" + money);
       }
       /**
        * 售后
        */
       public void afterService(float money) {
           System.out.println("提供售后服务，并拿到钱" + money);
       }
   }
   ```

   模拟消费者的类

   ```java
   public class Client {
       public static void main(String[] args) {
           //生产厂家
           final Producer producer = new Producer();
           //使用动态代理获取代理商对象
           InterfaceProducer proxyProducer = (InterfaceProducer) Proxy.newProxyInstance(producer.getClass().getClassLoader(),
                   producer.getClass().getInterfaces(),
                   new InvocationHandler() {
                       public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                           //提供增强的代码
   						//代理商获取2成的利润，剩余8成给厂家
                           Object returnValue = null;
                           Float money = (Float) args[0];
   
                           if ("saleProduct".equals(method.getName())) {
                               returnValue = method.invoke(producer, money * 0.8f);
                           }
                           return returnValue;
                       }
                   });
           // producer.saleProduct(1000f);
           //由代理商销售产品
           proxyProducer.saleProduct(1000f);
       }
   }
   ```



#### 2.基于子类的动态代理

1. 使用第三方库cglib中的Enhancer类中的create方法。

2. 创建代理对象的**要求：被代理类不能是最终类（final）**。

3. create的参数：

   + Class：被代理对象的字节码
   + Callback：用于实现增强，一般使用该接口（Callback）的子接口实现类MethodInterceptor

4. MethodInterceptor中包含方法`public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy)`

   + 代理对象执行任何方法时都会经过该方法，进行方法的拦截并增强，类似于上面的invoke方法
   + obj：代理对象的引用
   + method：当前执行的方法
   + args：当前执行方法的参数列表
   + proxy：当前执行方法的代理对象，一般用不到
   + return：返回值与当前执行的方法的返回值相同

5. 实例

   Producer类中不再需要实现接口

   ```java
   public class Producer{
   
       /**
        * 销售
        * @param money
        */
       public void saleProduct(float money) {
           System.out.println("销售产品，并拿到钱" + money);
       }
   
       /**
        * 售后
        * @param money
        */
       public void afterService(float money) {
           System.out.println("提供售后服务，并拿到钱" + money);
       }
   }
   ```

   模拟消费者的类

   ```java
   public class Client {
       public static void main(String[] args) {
           final Producer producer = new Producer();
   
           Producer cglibProducer = (Producer) Enhancer.create(producer.getClass(), new MethodInterceptor() {
               public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
                   //提供增强的代码
                   Object returnValue = null;
                   Float money = (Float) args[0];
   
                   if ("saleProduct".equals(method.getName())) {
                       returnValue = method.invoke(producer, money * 0.8f);
                   }
                   return returnValue;
               }
           });
           cglibProducer.saleProduct(1000f);
       }
   }
   ```



## 使用动态代理实现业务层事务控制

1. 创建一个动态代理类，将事务控制移交到该类中管理，对accountService类进行代理。该类中定义`getAccountService`方法用于获取一个代理对象，accountService中的任何方法执行，都会经过该其中的invoke方法进行增强。

   ```java
   public class ProxyAccountService {
       private AccountService accountService;
       private TransactionManager transactionManager;
       public void setTransactionManager(TransactionManager transactionManager) {
           this.transactionManager = transactionManager;
       }
       public final void setAccountService(AccountService accountService) {
           this.accountService = accountService;
       }
   
       public AccountService getAccountService() {
           return (AccountService) Proxy.newProxyInstance(accountService.getClass().getClassLoader(),
                   accountService.getClass().getInterfaces(),
                   new InvocationHandler() {
                       public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
   
                           Object rtValue = null;
                           try {
                               //1.开启事务
                               transactionManager.beginTransaction();
                               //2.执行操作
                               rtValue = method.invoke(accountService, args);
                               //3.提交事务
                               transactionManager.commit();
                               //4.返回结果
                               return rtValue;
                           } catch (Exception e) {
                               //5.回滚操作
                               transactionManager.rollback();
                               throw new RuntimeException(e);
                           } finally {
                               //6.释放连接
                               transactionManager.release();
                           }
                       }
                   });
       }
   }
   ```

2. 原本的AccountService类中移除事务控制的代码

   ```java
   public class AccountServiceImpl implements AccountService {
       private AccountDao accountDao;
       public void setAccountDao(AccountDao accountDao) {
           this.accountDao = accountDao;
       }
   
       public List<Account> findAllAccount() {
           List<Account> accounts = accountDao.findAllAccount();
           return accounts;
       }
   
       public Account findAccountById(Integer accountId) {
           return accountDao.findAccountById(accountId);
       }
   
       public void saveAccount(Account account) {
           accountDao.saveAccount(account);
       }
   
       public void updateAccount(Account account) {
           accountDao.updateAccount(account);
       }
   
       public void deleteAccount(Integer acccountId) {
           accountDao.deleteAccount(acccountId);
       }
   
       public void transfer(String sourceName, String targetName, Float money) {
           System.out.println("transfer....");
           Account source = accountDao.findAccountByName(sourceName);
           Account target = accountDao.findAccountByName(targetName);
           source.setMoney(source.getMoney() - money);
           target.setMoney(target.getMoney() + money);
           accountDao.updateAccount(source);
           int i = 1 / 0;
           accountDao.updateAccount(target);
       }
   }
   ```

3. 注入数据需要对新增的代理类进行注入，同时在测试方法中由于存在两个AccountService类型的bean，需要使用 `@Qualifier("proxyAccountService")`指定为代理的类。

   `proxyAccountService`由`getAccountService`方法产生，该方法中通过动态代理实现了`AccountService`接口。

   `accountService`本身就是`AccountService`接口的实现类。

   所以二者在spring容器中的类型都是`AccountService`，在注入数据时要加以区分。

   ```xml
   <bean id="proxyAccountService" factory-bean="proxyAccount" factory-method="getAccountService"/>
   <bean id="proxyAccount" class="cn.sucrelt.service.impl.ProxyAccountService">
       <property name="accountService" ref="accountService"/>
       <property name="transactionManager" ref="transactionManager"/>
   </bean>
   ```

   ```java
   public class AccountServiceTest {
   
       @Autowired
       @Qualifier("proxyAccountService")
       private AccountService accountService;
   }
   ```



## AOP相关概念

1. Joinpoint(连接点)：所谓连接点是指那些被**拦截到的点**。在spring中,这些点指的是方法，因为spring只支持方法类型的连接点。 
2. Pointcut(切入点)：所谓切入点是指我们要对哪些Joinpoint进行拦截的定义，实际上就是**被增强的方法**。连接点不一定是切入点，因为不一定做了方法的增强。
3. Advice(通知/增强)：所谓通知是指拦截到Joinpoint之后所要做的事情。即在特定的切入点上**执行的增强处理**。
   + 通知的类型：前置通知，后置通知，异常通知，最终通知，环绕通知。 

4. Introduction(引介)：引介是一种特殊的通知。在不修改类代码的前提下，Introduction可以在运行期为类动态地添加一些方法或Field。
5. Target(目标对象)：被代理的目标对象。
6. Weaving(织入)：是指把增强的功能加入到目标对象来创建新的代理对象的过程。spring采用**动态代理织入**，而AspectJ采用编译期织入和类装载期织入。 
7. Proxy(代理)：一个类被AOP织入增强后，就产生一个**结果代理类**。 
8. Aspect(切面)：是切入点和通知（引介）的结合。



## 基于xml的AOP

### 1.bean.xml中的配置

1. `aop:config`：用于声明开始aop的配置。
2. `aop:aspect` ：用于配置切面。 
   + id属性：给切面提供一个唯一标识。 
   + ref属性：引用配置好的通知类bean的id，即包含增强方法的类的bean的ID。

3. 在`aop:aspect`标签内部使用对应标签来**配置通知**的类型。

   1. `aop:before`：表示前置通知，在切入点方法执行前执行。
      + method属性：指定类中作为前置通知的方法。
      + ponitcut-ref属性：用于指定切入点的表达式的引用 。
      + poinitcut属性：用于指定切入点表达式，含义是指对业务层中哪些方法进行增强。

   2. `aop:after-returning`：表示后置通知，在切入点方法正常执行后执行。
      + method属性，ponitcut-ref属性，poinitcut属性。
      + 切入点方法正常执行之后。**它和异常通知只能有一个执行**。

   3. `aop:after-throwing`：表示异常通知，在切入点方法执行发生异常时执行。
      + method属性，ponitcut-ref属性，poinitcut属性。
   4. `aop:after`：表示最终通知，无论切入点方法是否正常执行，最终通知都在最后执行。
      + method属性，ponitcut-ref属性，poinitcut属性。
      + 无论切入点方法执行时是否有异常，它都会在其后面执行。

   5. `aop:pointcut`：用于定义切入点表达式。
      + id属性：指定表达式的唯一标识。
      + expression属性：指定表达式内容。
      + 该标签可以放在`aop:aspect`标签外面，此时**所有切面都可使用**。


### 2.切入点表达式

	1. 表达式执行：`execution(表达式)`
 	2. 切入点表达式语法：`execution([修饰符] 返回值类型 包名.类名.方法名(参数))`
 	3. 全匹配方式：`public void cn.sucrelt.service.impl.AccountServiceImpl.saveAccount(cn.sucrelt.domain.Account)`
 	4. 访问修饰符可以省略，返回值用*表示任意返回值*
 	5. *包名用*表示任意包，用..表示当前包及其子包
 	6. 参数列表使用*表示任意参数，但不能是空参；使用..表示有无参数均可*
 	7. 全通配方式：`* *..*.*(..)`
 	8. 通常情况下，我们都是对业务层的方法进行增强，所以**切入点表达式**都是切到业务层实现类：`execution(* cn.sucrelt.service.impl.*.*(..))`

### 3.案例

Logger类中用于模拟几种类型的通知

```java
public class Logger {
    /**
     * 前置通知
     */
    public void beforePrintLog() {
        System.out.println("前置通知beforePrintLog开始记录日志...");
    }
    /**
     * 后置通知
     */
    public void afterReturningPrintLog() {
        System.out.println("后置通知afterReturningPrintLog开始记录日志...");
    }
    /**
     * 异常通知
     */
    public void afterThrowingPrintLog() {
        System.out.println("异常通知afterThrowingPrintLog开始记录日志...");
    }
    /**
     * 最终通知
     */
    public void afterPrintLog() {
        System.out.println("最终通知afterPrintLog开始记录日志...");
    }
}
```

在bean.xml中配置Logger类以及切面

```xml
<!--配置Logger类-->
<bean id="logger" class="cn.sucrelt.aoputils.Logger"/>
<!--配置AOP-->
<!--配置切面-->
<aop:config>
    <aop:aspect id="logAdvice" ref="logger">
        <!--配置切入点表达式-->
        <aop:pointcut id="pt1" expression="execution(* cn.sucrelt.aopservice.impl.*.*(..))"/>

        <aop:before method="beforePrintLog" pointcut-ref="pt1"/>
        <aop:after-returning method="afterReturningPrintLog" pointcut-ref="pt1"/>
        <aop:after-throwing method="afterThrowingPrintLog" pointcut-ref="pt1"/>
        <aop:after method="afterPrintLog" pointcut-ref="pt1"/>
        <aop:around method="aroundPrintLog" pointcut-ref="pt1"/>
    </aop:aspect>
</aop:config>
```

执行业务层方法saveAccount结果

```java
前置通知beforePrintLog开始记录日志...
执行了保存
后置通知afterReturningPrintLog开始记录日志...
最终通知afterPrintLog开始记录日志...
```

### 4.环绕通知

1. 问题：当配置环绕通知后，执行业务层方法时，环绕通知方法执行，但原有的切入点方法没有执行，对于比动态代理中，存在invoke方法用于明确的执行切入点方法。

2. 解决：Spring框架中提供了一个接口ProceedingJoinPoint，其中有一个proceed()方法，用于明确调用切入点方法，该接口可以作为环绕通知的方法参数。

3. 通常情况下，环绕通知都是独立使用的，相当于动态代理中的invoke方法

   ```java
   /**
    * 环绕通知
    */
   public Object aroundPrintLog(ProceedingJoinPoint pjp) {
       Object rtValue = null;
       try {
           Object[] args = pjp.getArgs();
           System.out.println("环绕通知aroundPrintLog开始记录日志...前置");
           rtValue = pjp.proceed(args);
           System.out.println("环绕通知aroundPrintLog开始记录日志...后置");
           return rtValue;
       } catch (Throwable throwable) {
           System.out.println("环绕通知aroundPrintLog开始记录日志...异常");
           throw new RuntimeException(throwable);
       }finally {
           System.out.println("环绕通知aroundPrintLog开始记录日志...最终");
       }
   }
   ```

   bean.xml

   ```xml
   <aop:config>
           <aop:aspect id="logAdvice" ref="logger">
               <!--配置切入点表达式-->
               <aop:pointcut id="pt1" expression="execution(* cn.sucrelt.aopservice.impl.*.*(..))"/>
               <aop:around method="aroundPrintLog" pointcut-ref="pt1"/>
           </aop:aspect>
       </aop:config>
   ```



## 基于注解的AOP

1. 通知类Logger使用注解@Component("logAdvice")配置，同时在其上方使用@Aspect声明该类为一个切面。

2. 在类中增强的通知方法上使用相应的注解进行配置说明，括号中的参数为切入点表达式。

   + @Before("pt1()")前置通知
   + @AfterReturning后置通知
   + @AfterThrowing异常通知
   + @After最终通知
   + @Around环绕通知

3. 在bean.xml文件中使用`<aop:aspectj-autoproxy/>`开启对注解的支持。

4. 配置切入点表达式

   ```java
   @Pointcut("(* cn.sucrelt.aopservice.impl.*.*(..))") 
   private void pt1() {}
   ```

   