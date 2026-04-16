package gaarason.database.spring.boot.starter.properties;

import gaarason.database.lang.Nullable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 多数据源分组配置属性
 * <p>
 * 配置前缀: {@code gaarason.database.datasource}
 * <pre>{@code
 * gaarason:
 *   database:
 *     datasource:
 *       default-group: master
 *       groups:
 *         master:
 *           master:
 *             - url: jdbc:mysql://master1/db
 *               username: root
 *               password: root
 *           slave:
 *             - url: jdbc:mysql://slave1/db
 *               username: root
 *               password: root
 *         order:
 *           master:
 *             - url: jdbc:mysql://order1/order_db
 *               username: root
 *               password: root
 * }</pre>
 * @author xt
 */
public class GaarasonDataSourceProperties implements Serializable {

    public static final String PREFIX = "gaarason.database.datasource";

    private static final long serialVersionUID = 1L;

    /**
     * 默认数据源组名, 缺省 "master"
     */
    private String defaultGroup = "master";

    /**
     * 数据源分组映射: 组名 -> 组配置
     */
    private Map<String, DataSourceGroupProperties> groups = new LinkedHashMap<>();

    public String getDefaultGroup() {
        return defaultGroup;
    }

    public void setDefaultGroup(String defaultGroup) {
        this.defaultGroup = defaultGroup;
    }

    public Map<String, DataSourceGroupProperties> getGroups() {
        return groups;
    }

    public void setGroups(Map<String, DataSourceGroupProperties> groups) {
        this.groups = groups;
    }

    /**
     * 单个数据源组的配置
     */
    public static class DataSourceGroupProperties implements Serializable {

        private static final long serialVersionUID = 1L;

        /**
         * DataSource 类型全限定类名(如 com.alibaba.druid.pool.DruidDataSource), 不指定时由框架自动检测
         */
        @Nullable
        private String type;

        /**
         * 主数据源列表(写)
         */
        private List<DataSourceNodeProperties> master = new ArrayList<>();

        /**
         * 从数据源列表(读), 不配置时读请求回退到主库
         */
        private List<DataSourceNodeProperties> slave = new ArrayList<>();

        @Nullable
        public String getType() {
            return type;
        }

        public void setType(@Nullable String type) {
            this.type = type;
        }

        public List<DataSourceNodeProperties> getMaster() {
            return master;
        }

        public void setMaster(List<DataSourceNodeProperties> master) {
            this.master = master;
        }

        public List<DataSourceNodeProperties> getSlave() {
            return slave;
        }

        public void setSlave(List<DataSourceNodeProperties> slave) {
            this.slave = slave;
        }
    }

    /**
     * 单个数据源节点配置
     */
    public static class DataSourceNodeProperties implements Serializable {

        private static final long serialVersionUID = 1L;

        /**
         * JDBC URL
         */
        @Nullable
        private String url;

        /**
         * 数据库用户名
         */
        @Nullable
        private String username;

        /**
         * 数据库密码
         */
        @Nullable
        private String password;

        /**
         * JDBC 驱动类名
         */
        @Nullable
        private String driverClassName;

        /**
         * 扩展属性(连接池参数等)
         */
        private Map<String, String> properties = new LinkedHashMap<>();

        @Nullable
        public String getUrl() {
            return url;
        }

        public void setUrl(@Nullable String url) {
            this.url = url;
        }

        @Nullable
        public String getUsername() {
            return username;
        }

        public void setUsername(@Nullable String username) {
            this.username = username;
        }

        @Nullable
        public String getPassword() {
            return password;
        }

        public void setPassword(@Nullable String password) {
            this.password = password;
        }

        @Nullable
        public String getDriverClassName() {
            return driverClassName;
        }

        public void setDriverClassName(@Nullable String driverClassName) {
            this.driverClassName = driverClassName;
        }

        public Map<String, String> getProperties() {
            return properties;
        }

        public void setProperties(Map<String, String> properties) {
            this.properties = properties;
        }
    }
}
