package cn.sucrelt.test;

import cn.sucrelt.aopservice.AopAccountService;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @description:
 * @author: sucre
 * @date: 2020/09/24
 * @time: 15:19
 */
public class AopTest {
    public static void main(String[] args) {
        ApplicationContext applicationContext = new ClassPathXmlApplicationContext("aopbean.xml");
        AopAccountService aopAccountService = (AopAccountService) applicationContext.getBean("aopAccountService");

        aopAccountService.saveAccount();
        // aopAccountService.updateAccount(1);
        // aopAccountService.deleteAccount();

    }
}
