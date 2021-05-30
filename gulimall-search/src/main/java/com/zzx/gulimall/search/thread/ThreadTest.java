package com.zzx.gulimall.search.thread;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author zzx
 * @date 2021-05-30 12:17
 */
public class ThreadTest {

    public static ExecutorService executor = Executors.newFixedThreadPool(10);

    /*
     * 实现多线程四种方式：
     * 1) 继承Thread类
     *      new Thread01().start();
     * 2) 实现Runnable接口
     *      new Thread(new Runnable01()).start();
     * 3) 实现Callable接口 + FutureTask (可以返回结果，可以处理异常)
     *      FutureTask<Integer> futureTask = new FutureTask<>(new Callable01());
     *      new Thread(futureTask).start();
     *      System.out.println(futureTask.get());
     * 4) 线程池
     *
     * 区别：
     *      1、2不能得到返回值。3可以获取返回值
     *      1、2、3都不能控制资源
     *      4可以控制资源，性能稳定
     */

    public static void main(String[] args) throws Exception {
        System.out.println("main...start...");
        /*CompletableFuture.runAsync(() -> {
            System.out.println("当前线程: " + Thread.currentThread().getName());
            int i = 10 / 2;
            System.out.println("运行结果: " + i);
        }, executor);

        CompletableFuture<Integer> future = CompletableFuture.supplyAsync(() -> {
            System.out.println("当前线程: " + Thread.currentThread().getName());
            int i = 10 / 0;
            System.out.println("运行结果: " + i);
            return i;
        }, executor).whenComplete((res, exception) -> {
            System.out.println("异步任务成功完成了...结果是: " + res + "; 异常是: " + exception);
        }).exceptionally(throwable -> 0);*/

        CompletableFuture<Integer> future = CompletableFuture.supplyAsync(() -> {
            System.out.println("当前线程: " + Thread.currentThread().getName());
            int i = 10 / 0;
            System.out.println("运行结果: " + i);
            return i;
        }).handle((res, thr) -> {
            if (res != null) {
                return res * 3;
            }
            if (thr != null) {
                return -99;
            }
            return 0;
        });
        System.out.println("main...end..."+future.get());
        executor.shutdown();
    }

    public static class Thread01 extends Thread {
        @Override
        public void run() {
            System.out.println("当前线程: " + Thread.currentThread().getName());
            int i = 10 / 2;
            System.out.println("运行结果: " + i);
        }
    }

    public static class Runnable01 implements Runnable {
        @Override
        public void run() {
            System.out.println("当前线程: " + Thread.currentThread().getName());
            int i = 10 / 2;
            System.out.println("运行结果: " + i);
        }
    }

    public static class Callable01 implements Callable<Integer> {
        @Override
        public Integer call() throws Exception {
            System.out.println("当前线程: " + Thread.currentThread().getName());
            int i = 10 / 2;
            System.out.println("运行结果: " + i);
            return i;
        }
    }
}
