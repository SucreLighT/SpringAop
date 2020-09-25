package cn.sucrelt.aoputils;

/**
 * @description: 用于记录日志的方法
 * @author: sucre
 * @date: 2020/09/24
 * @time: 10:34
 */
public class Logger {
    /**
     * 前置通知
     */
    public void beforePrintLog() {
        System.out.println("前置通知beforePrintLog开始记录日志...");
    }

    /**
     * 后置通知
     */
    public void afterReturningPrintLog() {
        System.out.println("后置通知afterReturningPrintLog开始记录日志...");
    }

    /**
     * 异常通知
     */
    public void afterThrowingPrintLog() {
        System.out.println("异常通知afterThrowingPrintLog开始记录日志...");
    }

    /**
     * 最终通知
     */
    public void afterPrintLog() {
        System.out.println("最终通知afterPrintLog开始记录日志...");
    }

    /**
     * 环绕通知
     */
    public void aroundPrintLog() {
        System.out.println("环绕通知aroundPrintLog开始记录日志...");
    }
}
