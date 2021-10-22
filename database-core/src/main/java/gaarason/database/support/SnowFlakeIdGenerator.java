package gaarason.database.support;

import gaarason.database.contract.support.IdGenerator;
import gaarason.database.exception.SnowFlakeIdGeneratorException;

/**
 * 雪花id工具类
 * @author xt
 */
public class SnowFlakeIdGenerator implements IdGenerator.SnowFlakesID {

    /**
     * 初始时间截 (2020-02-02)
     */
    public static final long INITIAL_TIME_STAMP = 1580572800000L;

    /**
     * 序列在id中占的位数
     */
    public static final long SEQUENCE_BITS = 13L;

    /**
     * 机器id所占的位数
     */
    public static final long WORKER_ID_BITS = 10L;

    /**
     * 数据标识id所占的位数(全部使用机器id区分)
     */
    public static final long DATA_CENTER_ID_BITS = 0L;

    /**
     * 支持的最大机器id，当WORKER_ID_BITS=5时，结果是31 (这个移位算法可以很快的计算出几位二进制数所能表示的最大十进制数)
     */
    public static final long MAX_WORKER_ID = ~(-1L << WORKER_ID_BITS);

    /**
     * 支持的最大数据标识id，当DATA_CENTER_ID_BITS=5，时结果是31
     */
    public static final long MAX_DATA_CENTER_ID = ~(-1L << DATA_CENTER_ID_BITS);

    /**
     * 机器ID的偏移量(13)
     */
    public static final long WORKER_ID_OFFSET = SEQUENCE_BITS;

    /**
     * 数据中心ID的偏移量(13+10)
     */
    public static final long DATA_CENTER_ID_OFFSET = SEQUENCE_BITS + WORKER_ID_BITS;

    /**
     * 时间截的偏移量(13+10+0)
     */
    public static final long TIMESTAMP_OFFSET = SEQUENCE_BITS + WORKER_ID_BITS + DATA_CENTER_ID_BITS;

    /**
     * 生成序列的掩码，这里为 2的SEQUENCE_BITS次方-1 = 8191
     */
    public static final long SEQUENCE_MASK = ~(-1L << SEQUENCE_BITS);

    /**
     * 最大时钟回拨等待时间(ms)
     */
    public static final long MAX_BACKWARD_MS = 10;

    /**
     * 工作节点ID(0~1023)
     */
    private final long workerId;

    /**
     * 数据中心ID(0~0)
     */
    private final long dataCenterId;

    /**
     * 毫秒内序列(0~8191)
     */
    private long sequence;

    /**
     * 上次生成ID的时间截
     */
    private long lastTimestamp = -1L;

    /**
     * 构造函数
     * @param workerId     工作ID (0~1023)
     * @param dataCenterId 数据中心ID (0)
     */
    public SnowFlakeIdGenerator(long workerId, long dataCenterId) {
        if (workerId > MAX_WORKER_ID || workerId < 0) {
            throw new IllegalArgumentException(String.format("WorkerID 不能大于 %d 或小于 0", MAX_WORKER_ID));
        }
        if (dataCenterId > MAX_DATA_CENTER_ID || dataCenterId < 0) {
            throw new IllegalArgumentException(String.format("DataCenterID 不能大于 %d 或小于 0", MAX_DATA_CENTER_ID));
        }
        this.workerId = workerId;
        this.dataCenterId = dataCenterId;
        informationReport();
    }

    private void informationReport() {
        long effectiveAge =
            ((1L << (64 - 1 - SEQUENCE_BITS - WORKER_ID_BITS - DATA_CENTER_ID_BITS)) - (getSystemCurrentTimeMillis() - INITIAL_TIME_STAMP)) / (1000L * 3600 * 24 * 365);
        System.out.println(
            "雪花算法信息 : 尚可使用 " + effectiveAge + " 年, 当前 workerID " + workerId + " (最大 : " + MAX_WORKER_ID + ") , 当前 dataCenterID " + dataCenterId);
    }

    /**
     * 获取当前毫秒时间
     * @return 当前时间戳
     */
    private static long getSystemCurrentTimeMillis() {
        return System.currentTimeMillis();
    }

    /**
     * 获得下一个ID (用同步锁保证线程安全,在高并发场景同步锁性能和资源消耗优于CAS；并且测试中单线程即使用CAS也没有提升性能)
     * @return SnowflakeId
     */
    @Override
    public synchronized Long nextId() {
        long currentTimestamp = getSystemCurrentTimeMillis();
        // 如果当前时间小于上一次ID生成的时间戳，说明系统时钟回退过这个时候应当抛出异常
        if (currentTimestamp < lastTimestamp) {
            // 如果时钟回拨在可接受范围内, 等待即可
            long offset = lastTimestamp - currentTimestamp;
            if (offset <= MAX_BACKWARD_MS) {
                // 堵塞到lastTimestamp之后让其追上
                currentTimestamp = tilNextMillis(lastTimestamp);
                // 当前仍然时间小于上一次ID生成的时间戳,抛异常并上报
                if (currentTimestamp < lastTimestamp) {
                    throw new SnowFlakeIdGeneratorException(
                        "当前时间 " + currentTimestamp + " 等待后仍然小于上一次记录的时间戳 " + lastTimestamp + " !");
                }
            } else {
                throw new SnowFlakeIdGeneratorException("当前时间 " + currentTimestamp + " 小于上一次记录的时间戳 " + lastTimestamp + " !");
            }
        }

        // 如果是同一时间生成的，则进行毫秒内序列
        if (lastTimestamp == currentTimestamp) {
            sequence = (sequence + 1) & SEQUENCE_MASK;
            // sequence等于0说明毫秒内序列已经增长到最大值
            if (sequence == 0) {
                // 阻塞到下一个毫秒,获得新的时间戳
                currentTimestamp = tilNextMillis(lastTimestamp);
            }
        }
        // 时间戳改变，毫秒内序列重置
        else {
            sequence = 0L;
        }
        // 上次生成ID的时间截
        lastTimestamp = currentTimestamp;
        // 移位并通过或运算拼到一起组成64位的ID
        return ((currentTimestamp - INITIAL_TIME_STAMP) << TIMESTAMP_OFFSET)
            | (dataCenterId << DATA_CENTER_ID_OFFSET)
            | (workerId << WORKER_ID_OFFSET)
            | sequence;
    }

    /**
     * 阻塞到下一个毫秒(大于lastTimestamp)，直到获得新的时间戳
     * @param lastTimestamp 上次生成ID的时间截
     * @return 当前时间戳(大于上次生成ID的时间截)
     */
    private static long tilNextMillis(long lastTimestamp) {
        long timestamp = System.currentTimeMillis();
        while (timestamp <= lastTimestamp) {
            timestamp = System.currentTimeMillis();
        }
        return timestamp;
    }
}
