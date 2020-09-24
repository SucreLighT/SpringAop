package cn.sucrelt.aopservice.impl;

import cn.sucrelt.aopservice.AopAccountService;

/**
 * @description: 账户的业务层实现类
 * @author: sucre
 * @date: 2020/09/24
 * @time: 10:30
 */
public class AopAccountServiceImpl implements AopAccountService {
    public void saveAccount() {
        System.out.println("执行了保存");
    }

    public void updateAccount(int i) {
        System.out.println("执行了更新" + i);
    }

    public int deleteAccount() {
        System.out.println("执行了删除");
        return 0;
    }
}
