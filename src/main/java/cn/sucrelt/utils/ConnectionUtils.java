package cn.sucrelt.utils;

import javax.sql.DataSource;
import java.sql.Connection;

/**
 * @description: 数据库连接的工具类，用于从数据源中获取一个连接，并实现和线程的绑定
 * @author: sucre
 * @date: 2020/09/23
 * @time: 10:08
 */
public class ConnectionUtils {
    private ThreadLocal<Connection> threadLocal = new ThreadLocal<Connection>();
    private DataSource dataSource;

    /**
     * 提供set方法用于注入
     *
     * @param dataSource
     */
    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * 获取当前线程上的连接
     *
     * @return
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
