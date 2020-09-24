package cn.sucrelt.aopservice;

/**
 * @description:
 * @author: sucre
 * @date: 2020/09/24
 * @time: 10:13
 */
public interface AopAccountService {
    /**
     * 模拟保存账户
     */
    void saveAccount();

    /**
     * 模拟更新账户
     *
     * @param i
     */
    void updateAccount(int i);


    /**
     * 模拟删除账户
     *
     * @return
     */
    int deleteAccount();
}
