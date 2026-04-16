package gaarason.database.config;

import gaarason.database.contract.connection.GaarasonDataSource;
import gaarason.database.contract.eloquent.Builder;
import gaarason.database.contract.eloquent.Model;
import gaarason.database.contract.query.Grammar;

/**
 * 区分数据库类型的查询构造器
 * @author xt
 * @since 2021/12/1 11:42 上午
 */
public interface QueryBuilderConfig {

    /**
     * 根据数据库类型返回符号
     * @return 符号
     */
    String getValueSymbol();

    /**
     * 当前是否支持
     * @param databaseProductName 数据源中的数据连接中的数据库名称
     * @return 是否支持
     */
    boolean support(String databaseProductName);

    /**
     * 创建方言对应的Grammar实例
     * @param tableName 表名
     * @return Grammar实例
     */
    Grammar newGrammar(String tableName);

    /**
     * 获取全新的查询构造器
     * @param gaarasonDataSource 数据源
     * @param model 数据模型
     * @param <T> 实体类型
     * @param <K> 主键类型
     * @return 查询构造器
     */
    <T, K> Builder<?, T, K> newBuilder(GaarasonDataSource gaarasonDataSource, Model<?, T, K> model);

    /**
     * 当匹配到特定的 databaseProductName 后, 返回一个绑定了具体方言的配置实例
     * <p>
     * 默认返回自身(适用于已预知数据库类型的配置);
     * 通用配置类应覆盖此方法, 返回绑定了特定方言的新实例, 以保证线程安全
     * @param databaseProductName JDBC 产品名(小写)
     * @return 绑定了具体方言的配置实例
     */
    default QueryBuilderConfig forProductName(String databaseProductName) {
        return this;
    }

}
