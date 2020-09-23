package cn.sucrelt.service.impl;

import cn.sucrelt.dao.AccountDao;
import cn.sucrelt.domain.Account;
import cn.sucrelt.service.AccountService;
import cn.sucrelt.utils.TransactionManager;

import java.util.List;

/**
 * @description:
 * @author: sucre
 * @date: 2020/09/23
 * @time: 09:16
 */
public class AccountServiceImpl implements AccountService {

    private AccountDao accountDao;
    // private TransactionManager transactionManager;
    //
    // public void setTransactionManager(TransactionManager transactionManager) {
    //     this.transactionManager = transactionManager;
    // }

    public void setAccountDao(AccountDao accountDao) {
        this.accountDao = accountDao;
    }

    public List<Account> findAllAccount() {
        // try {
        //     //1.开启事务
        //     transactionManager.beginTransaction();
        //     //2.执行操作
        List<Account> accounts = accountDao.findAllAccount();
        // //3.提交事务
        // transactionManager.commit();
        // //4.返回结果
        return accounts;
        // } catch (Exception e) {
        //     //5.回滚事务
        //     transactionManager.rollback();
        //     throw new RuntimeException(e);
        // } finally {
        //     //6.释放连接
        //     transactionManager.release();
        // }
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
        // int i = 1 / 0;
        //2.6更新转入账户
        accountDao.updateAccount(target);
    }
}
