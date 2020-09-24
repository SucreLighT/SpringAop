package cn.sucrelt.cglib;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;

/**
 * @description: 模拟一个消费者
 * @author: sucre
 * @date: 2020/09/23
 * @time: 14:21
 */
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
