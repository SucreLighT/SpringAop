package cn.sucrelt.proxy;

/**
 * @description:
 * @author: sucre
 * @date: 2020/09/23
 * @time: 14:20
 */
public interface InterfaceProducer {

    /**
     * 销售
     *
     * @param money
     */
    public void saleProduct(float money);

    /**
     * 售后
     *
     * @param money
     */
    public void afterService(float money);
}
