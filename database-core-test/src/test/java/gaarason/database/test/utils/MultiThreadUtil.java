package gaarason.database.test.utils;

import java.util.concurrent.CountDownLatch;

/**
 * 并发测试
 */
public class MultiThreadUtil {

    /**
     * 在多线程下执行某个闭包, 打印所消耗时间
     * @param threadNumber 并发线程数量
     * @param cycleNumber  每个线程顺序执行次数数量
     * @param runnable     业务闭包
     */
    public static void run(int threadNumber, int cycleNumber, Runnable runnable) {
        CountDownLatch countDownLatch = new CountDownLatch(threadNumber);
        long           startTime      = System.currentTimeMillis();//记录开始时间
        for (int i = 0; i < threadNumber; i++) {
            new Thread(() -> {
                try {
                    for (int t = 0; t < cycleNumber; t++) {
                        runnable.run();
                    }
                } finally {
                    countDownLatch.countDown();
                }
            }).start();
        }
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        long  endTime = System.currentTimeMillis();//记录结束时间
        float excTime = (float) (endTime - startTime) / 1000;
        System.out.println(
            threadNumber + " 个线程同时顺序执行目标逻辑 " + cycleNumber + " 次, 共计 " + threadNumber * cycleNumber + " 次, 消耗时间：" + excTime + "s");
    }
}
