package cn.sucrelt.utils;

import java.sql.SQLException;

/**
 * @description: 和事务管理相关的工具类，包括开启事务，提交事务，回滚事务和释放连接
 * @author: sucre
 * @date: 2020/09/23
 * @time: 10:17
 */
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
