package cn.sucrelt.service.impl;

import cn.sucrelt.service.AccountService;
import cn.sucrelt.utils.TransactionManager;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * @description:
 * @author: sucre
 * @date: 2020/09/23
 * @time: 15:56
 */
public class ProxyAccountService {
    private AccountService accountService;
    private TransactionManager transactionManager;

    public void setTransactionManager(TransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }
    public final void setAccountService(AccountService accountService) {
        this.accountService = accountService;
    }

    /**
     * 获取Service代理对象
     *
     * @return
     */
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
