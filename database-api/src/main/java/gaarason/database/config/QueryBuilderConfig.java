package gaarason.database.config;

import gaarason.database.contract.connection.GaarasonDataSource;
import gaarason.database.contract.eloquent.Builder;
import gaarason.database.contract.eloquent.Model;

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
     * 获取全新的查询构造器
     * @param gaarasonDataSource 数据源
     * @param model 数据模型
     * @param <T> 实体类型
     * @param <K> 主键类型
     * @return 查询构造器
     */
    <T, K> Builder<T, K> newBuilder(GaarasonDataSource gaarasonDataSource, Model<T, K> model);

}
