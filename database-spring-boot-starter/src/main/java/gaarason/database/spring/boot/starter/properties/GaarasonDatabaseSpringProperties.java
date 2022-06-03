package gaarason.database.spring.boot.starter.properties;

import gaarason.database.config.GaarasonDatabaseProperties;
import gaarason.database.logging.Log;
import gaarason.database.logging.LogFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

/**
 * Properties
 * 因为 org.springframework.boot:spring-boot-configuration-processor 中使用 javax.lang.model.element.TypeElement.getEnclosedElements() 获取源信息
 * 所以, 这边若直接继承 GaarasonDatabaseProperties, 则不会产生配置提示信息(不影响使用); 也不想通过写 additional-spring-configuration-metadata.json 来解决这个问题
 * 所以, 直接抄一个 GaarasonDatabaseProperties, 再copy属性咯
 * @author xt
 * @see GaarasonDatabaseProperties
 */
@ConfigurationProperties(prefix = GaarasonDatabaseProperties.PREFIX)
public class GaarasonDatabaseSpringProperties implements Serializable {

    private static final Log LOGGER = LogFactory.getLog(GaarasonDatabaseSpringProperties.class);

    /** copy GaarasonDatabaseProperties start **/

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
         * 固定扫描 gaarason.database
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

    /** copy GaarasonDatabaseProperties end **/

    /**
     * 将自身合并其他来源配置后, 创建 GaarasonDatabaseProperties
     * 对于列表类型的数据,使用:做区分 eg: gaarason.database.scan.packages=gaarason:com.github.gaarason
     * @param scanByAnnotation 来自包扫描注解的配置
     * @return GaarasonDatabaseProperties
     * @see gaarason.database.spring.boot.starter.annotation.GaarasonDatabaseScan
     */
    public GaarasonDatabaseProperties buildFromThisAndWithAnnotation(
        GaarasonDatabaseSpringProperties.Scan scanByAnnotation) {
        GaarasonDatabaseProperties gaarasonDatabaseProperties = new GaarasonDatabaseProperties();

        this.getScan().getPackages().addAll(scanByAnnotation.getPackages());
        this.getScan().getFilterExcludePackages().addAll(scanByAnnotation.getFilterExcludePackages());
        this.getScan().getFilterIncludePatterns().addAll(scanByAnnotation.getFilterIncludePatterns());
        this.getScan().getFilterExcludePatterns().addAll(scanByAnnotation.getFilterExcludePatterns());


        BeanUtils.copyProperties(this.snowFlake, gaarasonDatabaseProperties.getSnowFlake());
        BeanUtils.copyProperties(this.scan, gaarasonDatabaseProperties.getScan());

        return gaarasonDatabaseProperties.fillAndVerify();
    }
}