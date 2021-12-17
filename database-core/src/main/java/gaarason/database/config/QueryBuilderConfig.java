package gaarason.database.config;

import gaarason.database.contract.connection.GaarasonDataSource;
import gaarason.database.contract.eloquent.Builder;
import gaarason.database.contract.eloquent.Model;
import gaarason.database.contract.query.Grammar;
import gaarason.database.provider.ModelShadowProvider;
import gaarason.database.query.MsSqlBuilder;
import gaarason.database.query.MySqlBuilder;
import gaarason.database.query.grammars.MsSqlGrammar;
import gaarason.database.query.grammars.MySqlGrammar;

import java.io.Serializable;

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
     * 获取查询构造器
     * @param gaarasonDataSource 数据源
     * @param model              数据模型
     * @param <T>                实体类型
     * @param <K>                主键类型
     * @return 查询构造器
     */
    <T extends Serializable, K extends Serializable> Builder<T, K> newBuilder(GaarasonDataSource gaarasonDataSource, Model<T, K> model);

    /**
     * 获取语法构造器
     * @param entityClass 实体类
     * @param <T>         实体类型
     * @return 语法构造器
     */
    <T extends Serializable> Grammar newGrammar(Class<T> entityClass);


    interface Mysql extends QueryBuilderConfig {

        /**
         * 根据数据库类型返回符号
         * @return 符号
         */
        @Override
        default String getValueSymbol() {
            return "'";
        }

        /**
         * 当前是否支持
         * @param databaseProductName 数据源中的数据连接中的数据库名称
         * @return 是否支持
         */
        @Override
        default boolean support(String databaseProductName) {
            return "mysql".equals(databaseProductName);
        }

        /**
         * 获取查询构造器
         * @param gaarasonDataSource 数据源
         * @param model              数据模型
         * @param <T>                实体类型了
         * @param <K>                主键类型
         * @return 查询构造器
         */
        @Override
        default <T extends Serializable, K extends Serializable> Builder<T, K> newBuilder(GaarasonDataSource gaarasonDataSource,
            Model<T, K> model) {

            return new MySqlBuilder<>(gaarasonDataSource, model, newGrammar(model.getEntityClass()));
        }

        /**
         * 获取语法构造器
         * @param entityClass 实体类
         * @param <T>         实体类型
         * @return 语法构造器
         */
        @Override
        default <T extends Serializable> Grammar newGrammar(Class<T> entityClass) {
            return new MySqlGrammar(ModelShadowProvider.getByEntityClass(entityClass).getTableName());
        }
    }


    interface Mssql extends QueryBuilderConfig {

        /**
         * 根据数据库类型返回符号
         * @return 符号
         */
        @Override
        default String getValueSymbol() {
            return "'";
        }

        /**
         * 当前是否支持
         * @param databaseProductName 数据源中的数据连接中的数据库名称
         * @return 是否支持
         */
        @Override
        default boolean support(String databaseProductName) {
            return "microsoft sql server".equals(databaseProductName) || "mssql".equals(databaseProductName);
        }

        /**
         * 获取查询构造器
         * @param gaarasonDataSource 数据源
         * @param model              数据模型
         * @param <T>                实体类型了
         * @param <K>                主键类型
         * @return 查询构造器
         */
        @Override
        default <T extends Serializable, K extends Serializable> Builder<T, K> newBuilder(GaarasonDataSource gaarasonDataSource,
            Model<T, K> model) {
            return new MsSqlBuilder<>(gaarasonDataSource, model, newGrammar(model.getEntityClass()));
        }

        /**
         * 获取语法构造器
         * @param entityClass 实体类
         * @param <T>         实体类型
         * @return 语法构造器
         */
        @Override
        default <T extends Serializable> Grammar newGrammar(Class<T> entityClass) {
            return new MsSqlGrammar(ModelShadowProvider.getByEntityClass(entityClass).getTableName());
        }
    }
}
