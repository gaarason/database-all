package gaarason.database.spring.boot.starter.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.io.Serializable;

/**
 * Properties
 * @author xt
 */
@Data
@ConfigurationProperties(prefix = "gaarason.database")
public class GaarasonDatabaseProperties implements Serializable {

    /**
     * 雪花算法
     */
    private SnowFlake snowFlake = new SnowFlake();

    @Data
    public static class SnowFlake implements Serializable {

        /**
         * 雪花算法 工作ID
         */
        private int workerId;

        /**
         * 雪花算法 数据源ID
         */
        private int dataId;
    }
}