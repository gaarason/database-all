package gaarason.database.support;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 线程池工厂
 */
public class NamedThreadFactory implements ThreadFactory {

    private final AtomicInteger threadNo = new AtomicInteger(1);
    private final String nameStr;

    /**
     * 构造
     * @param poolName 线程池名称
     */
    public NamedThreadFactory(String poolName) {
        nameStr = poolName + "-";
    }

    @Override
    public Thread newThread(Runnable r) {
        String threadName = nameStr + threadNo.getAndIncrement();
        Thread newThread = new Thread(r, threadName);
        newThread.setDaemon(true);
        if (newThread.getPriority() != Thread.NORM_PRIORITY) {
            newThread.setPriority(Thread.NORM_PRIORITY);
        }
        return newThread;
    }

}
