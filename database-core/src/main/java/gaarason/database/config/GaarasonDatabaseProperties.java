package gaarason.database.config;

import gaarason.database.logging.Log;
import gaarason.database.logging.LogFactory;
import gaarason.database.util.ObjectUtils;

import java.io.Serializable;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * GaarasonDatabaseProperties
 * @author xt
 */
public class GaarasonDatabaseProperties implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final Log LOGGER = LogFactory.getLog(DefaultAutoConfiguration.class);

    public static final String PREFIX = "gaarason.database";

    /**
     * 包扫描
     */
    protected Scan scan = new Scan();

    /**
     * 雪花算法
     */
    protected SnowFlake snowFlake = new SnowFlake();

    public Scan getScan() {
        return scan;
    }

    public void setScan(Scan scan) {
        this.scan = scan;
    }

    public SnowFlake getSnowFlake() {
        return snowFlake;
    }

    public void setSnowFlake(SnowFlake snowFlake) {
        this.snowFlake = snowFlake;
    }

    @Override
    public String toString() {
        return "GaarasonDatabaseProperties{" + "scan=" + scan + ", snowFlake=" + snowFlake + '}';
    }

    /**
     * 雪花算法
     */
    public static class SnowFlake implements Serializable {

        private static final long serialVersionUID = 1L;

        /**
         * 雪花算法 工作ID 0-1023
         */
        protected int workerId;

        /**
         * 雪花算法 数据源ID 只能是0
         */
        protected int dataId;

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

        @Override
        public String toString() {
            return "SnowFlake{" + "workerId=" + workerId + ", dataId=" + dataId + '}';
        }
    }

    /**
     * 包扫描
     */
    public static class Scan implements Serializable {

        private static final long serialVersionUID = 1L;

        /**
         * 包含的package前缀
         */
        protected List<String> packages = new LinkedList<>();

        /**
         * 排除的package前缀
         */
        protected List<String> filterExcludePackages = new LinkedList<>();

        /**
         * 包含的package表达式
         */
        protected List<String> filterIncludePatterns = new LinkedList<>();

        /**
         * 排除的package表达式
         */
        protected List<String> filterExcludePatterns = new LinkedList<>();

        public List<String> getPackages() {
            return packages;
        }

        public List<String> getFilterExcludePackages() {
            return filterExcludePackages;
        }

        public List<String> getFilterIncludePatterns() {
            return filterIncludePatterns;
        }

        public List<String> getFilterExcludePatterns() {
            return filterExcludePatterns;
        }

        public void setPackages(List<String> packages) {
            this.packages = packages;
        }

        public void setFilterExcludePackages(List<String> filterExcludePackages) {
            this.filterExcludePackages = filterExcludePackages;
        }

        public void setFilterIncludePatterns(List<String> filterIncludePatterns) {
            this.filterIncludePatterns = filterIncludePatterns;
        }

        public void setFilterExcludePatterns(List<String> filterExcludePatterns) {
            this.filterExcludePatterns = filterExcludePatterns;
        }

        @Override
        public String toString() {
            return "Scan{" + "packages=" + packages + ", filterExcludePackages=" + filterExcludePackages +
                ", filterIncludePatterns=" + filterIncludePatterns + ", filterExcludePatterns=" +
                filterExcludePatterns + '}';
        }
    }

    /**
     * 填补与验证
     * java8 与 以上版本的ClassLoader 实现上的差别, 使得当 packages 为 null 时, java8 会扫描所有包, 而其他java版本则完全不扫描
     * 因此, java8以上的版本, 必须配置本项目; java8为了更快的启动, 也应该配置本项目
     * @return GaarasonDatabaseProperties
     */
    public GaarasonDatabaseProperties fillAndVerify() {
        if (!ObjectUtils.isEmpty(this.getScan().getPackages())) {
            // 固定扫描 gaarason.database
            if(!this.getScan().getPackages().contains(GaarasonDatabaseProperties.PREFIX)){
                this.getScan().getPackages().add(GaarasonDatabaseProperties.PREFIX);
            }
        } else {
            LOGGER.warn("You should configure for the package scan, as like : " + GaarasonDatabaseProperties.PREFIX +
                ".scan.packages=you.package1,you.package2, or System.setProperty(\"gaarason.database.scan.packages\", \"you.package1,you.package2\"), or using @GaarasonDatabaseScan when spring boot is active");
        }
        LOGGER.info("Configuration is " + this);
        return this;
    }

    /**
     * 从 SystemProperties 中创建
     * 对于列表类型的数据,使用,做区分 eg: gaarason.database.scan.packages=gaarason,com.github.gaarason
     * @return GaarasonDatabaseProperties
     */
    public static GaarasonDatabaseProperties buildFromSystemProperties() {
        // 多个值时, 使用的分隔符号
        String symbol = ",";

        // 雪花算法
        GaarasonDatabaseProperties gaarasonDatabaseProperties = new GaarasonDatabaseProperties();
        gaarasonDatabaseProperties.getSnowFlake()
            .setDataId(
                Integer.parseInt(System.getProperty(GaarasonDatabaseProperties.PREFIX + ".snow-flake.worker-id", "0")));
        gaarasonDatabaseProperties.getSnowFlake()
            .setDataId(
                Integer.parseInt(System.getProperty(GaarasonDatabaseProperties.PREFIX + ".snow-flake.data-id", "0")));

        // 包扫描
        String packages = System.getProperty(GaarasonDatabaseProperties.PREFIX + ".scan.packages");

        if (packages != null) {
            gaarasonDatabaseProperties.getScan().getPackages().addAll(Arrays.asList(packages.split(symbol)));
        }
        String filterExcludePackages = System.getProperty(
            GaarasonDatabaseProperties.PREFIX + ".scan.filter-exclude-packages");
        if (filterExcludePackages != null) {
            gaarasonDatabaseProperties.getScan()
                .getFilterExcludePackages()
                .addAll(Arrays.asList(filterExcludePackages.split(symbol)));
        }
        String filterIncludePatterns = System.getProperty(
            GaarasonDatabaseProperties.PREFIX + ".scan.filter-include-patterns");
        if (filterIncludePatterns != null) {
            gaarasonDatabaseProperties.getScan()
                .getFilterIncludePatterns()
                .addAll(Arrays.asList(filterIncludePatterns.split(symbol)));
        }
        String filterExcludePatterns = System.getProperty(
            GaarasonDatabaseProperties.PREFIX + ".scan.filter-exclude-patterns");
        if (filterExcludePatterns != null) {
            gaarasonDatabaseProperties.getScan()
                .getFilterExcludePatterns()
                .addAll(Arrays.asList(filterExcludePatterns.split(symbol)));
        }

        return gaarasonDatabaseProperties.fillAndVerify();
    }

}