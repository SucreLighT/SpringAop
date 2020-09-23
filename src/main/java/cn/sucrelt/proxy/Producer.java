package cn.sucrelt.proxy;

/**
 * @description: 生产厂家
 * @author: sucre
 * @date: 2020/09/23
 * @time: 14:16
 */

public class Producer implements InterfaceProducer{

    /**
     * 销售
     * @param money
     */
    public void saleProduct(float money) {
        System.out.println("销售产品，并拿到钱" + money);
    }

    /**
     * 售后
     * @param money
     */
    public void afterService(float money) {
        System.out.println("提供售后服务，并拿到钱" + money);
    }
}
