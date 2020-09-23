package cn.sucrelt.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * @description: 模拟一个消费者
 * @author: sucre
 * @date: 2020/09/23
 * @time: 14:21
 */
public class Client {
    public static void main(String[] args) {
        final Producer producer = new Producer();
        InterfaceProducer proxyProducer = (InterfaceProducer) Proxy.newProxyInstance(producer.getClass().getClassLoader(),
                producer.getClass().getInterfaces(),
                new InvocationHandler() {
                    /**
                     * 执行被代理对象的任何方法都会经过该方法，即该方法会有一个拦截的功能
                     * @param proxy 代理对象的引用
                     * @param method 当前执行的方法
                     * @param args 当前执行方法的参数
                     * @return 和被代理对象的方法有相同的返回值
                     * @throws Throwable
                     */
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        //提供增强的代码

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
