package gaarason.database.spring.boot.starter.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.io.Serializable;

/**
 * Properties
 * @author xt
 */
@ConfigurationProperties(prefix = "gaarason.database")
public class GaarasonDatabaseProperties implements Serializable {

    /**
     * 雪花算法
     */
    private SnowFlake snowFlake = new SnowFlake();

    /**
     * 使用的日志驱动 slf4j, log4j, log4j2, commonsLog, jdkLog
     */
    private String logType;

    public SnowFlake getSnowFlake() {
        return snowFlake;
    }

    public void setSnowFlake(SnowFlake snowFlake) {
        this.snowFlake = snowFlake;
    }

    public String getLogType() {
        return logType;
    }

    public void setLogType(String logType) {
        this.logType = logType;
    }

    public static class SnowFlake implements Serializable {

        /**
         * 雪花算法 工作ID 0-1023
         */
        private int workerId;

        /**
         * 雪花算法 数据源ID 只能是0
         */
        private int dataId;

        public int getWorkerId() {
            return workerId;
        }

        public void setWorkerId(int workerId) {
            this.workerId = workerId;
        }

        public int getDataId() {
            return dataId;
        }

        public void setDataId(int dataId) {
            this.dataId = dataId;
        }
    }
}