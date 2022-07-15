package gaarason.database.test.utils;

import gaarason.database.exception.base.BaseException;
import org.junit.Assert;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 并发测试
 * @author xt
 */
public class MultiThreadUtil {

    private MultiThreadUtil() {
    }

    /**
     * 在多线程下执行某个闭包, 打印所消耗时间
     * @param threadNumber 并发线程数量
     * @param cycleNumber 每个线程顺序执行次数数量
     * @param runnable 业务闭包
     */
    public static void run(int threadNumber, int cycleNumber, Runnable runnable) {
        long count = runWithoutAssert(threadNumber, cycleNumber, runnable);
        Assert.assertEquals(threadNumber * cycleNumber, count);

    }

    /**
     * 在多线程下执行某个闭包, 打印所消耗时间
     * @param threadNumber 并发线程数量
     * @param cycleNumber 每个线程顺序执行次数数量
     * @param runnable 业务闭包
     * @return 正确执行的次数
     */
    public static long runWithoutAssert(int threadNumber, int cycleNumber, Runnable runnable) {
        // 正确执行的计数器
        AtomicLong count = new AtomicLong(0);

        CountDownLatch countDownLatch = new CountDownLatch(threadNumber);
        long startTime = System.currentTimeMillis();//记录开始时间
        for (int i = 0; i < threadNumber; i++) {
            new Thread(() -> {
                try {
                    for (int t = 0; t < cycleNumber; t++) {
                        runnable.run();
                        count.addAndGet(1);
                    }
                } finally {
                    countDownLatch.countDown();
                }
            }).start();
        }
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            throw new BaseException(e.getMessage(), e);
        }
        long endTime = System.currentTimeMillis();//记录结束时间
        float excTime = (float) (endTime - startTime) / 1000;
        System.out.println(
            threadNumber + " 个线程同时顺序执行目标逻辑 " + cycleNumber + " 次, 预计执行 " + threadNumber * cycleNumber + " 次, 实际正确执行 " +
                count.longValue() + " 次, 消耗时间：" + excTime + "s");
        return count.longValue();
    }
}
