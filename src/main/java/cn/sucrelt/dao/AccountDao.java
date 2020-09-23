package cn.sucrelt.dao;

import cn.sucrelt.domain.Account;

import java.util.List;

/**
 * @description:
 * @author: sucre
 * @date: 2020/09/23
 * @time: 09:18
 */
public interface AccountDao {

    /**
     * 查询所有
     *
     * @return
     */
    List<Account> findAllAccount();

    /**
     * @param accountId
     * @return
     */

    Account findAccountById(Integer accountId);

    /**
     * 保存
     *
     * @param account
     */
    void saveAccount(Account account);

    /**
     * 更新
     *
     * @param account
     */
    void updateAccount(Account account);

    /**
     * 删除
     *
     * @param acccountId
     */
    void deleteAccount(Integer acccountId);

    /**
     * 根据名称查询账户
     *
     * @param accountName
     * @return 如果有唯一的一个结果就返回，如果没有结果就返回null
     * 如果结果集超过一个就抛异常
     */
    Account findAccountByName(String accountName);
}
