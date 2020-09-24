package cn.sucrelt.aoputils;

/**
 * @description: 用于记录日志的方法
 * @author: sucre
 * @date: 2020/09/24
 * @time: 10:34
 */
public class Logger {
    /**
     * 用于打印日志，计划让其在切入点方法执行之前执行
     */
    public void printLog() {
        System.out.println("Logger类开始记录日志...");
    }
}
