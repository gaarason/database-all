package gaarason.database.config;

import gaarason.database.appointment.DbType;
import gaarason.database.contract.connection.GaarasonDataSource;
import gaarason.database.contract.eloquent.Builder;
import gaarason.database.contract.eloquent.Model;
import gaarason.database.contract.query.Grammar;
import gaarason.database.exception.TypeNotSupportedException;
import gaarason.database.query.QueryBuilder;
import gaarason.database.query.grammars.*;
import gaarason.database.util.ObjectUtils;

/**
 * 通用查询构造器配置, 支持所有预置数据库方言
 * <p>
 * 通过 JDBC DatabaseMetaData.getDatabaseProductName() 自动检测数据库类型,
 * 并创建对应方言的 Grammar 实例.
 * <p>
 * 本类作为单例注册; 检测成功后通过 {@link #forProductName(String)} 返回绑定了特定
 * {@link DbType} 的新实例, 保证线程安全
 * @author xt
 */
public class DefaultQueryBuilderConfig implements QueryBuilderConfig {

    /**
     * 已绑定的数据库类型, null 表示未绑定(工厂模式)
     */
    private final DbType dbType;

    public DefaultQueryBuilderConfig() {
        this.dbType = null;
    }

    private DefaultQueryBuilderConfig(DbType dbType) {
        this.dbType = dbType;
    }

    @Override
    public String getValueSymbol() {
        return "'";
    }

    @Override
    public boolean support(String databaseProductName) {
        return DbType.fromProductName(databaseProductName) != null;
    }

    @Override
    public QueryBuilderConfig forProductName(String databaseProductName) {
        DbType detected = DbType.fromProductName(databaseProductName);
        if (detected == null) {
            throw new TypeNotSupportedException("Database product name [" + databaseProductName + "] not supported.");
        }
        return new DefaultQueryBuilderConfig(detected);
    }

    @Override
    public Grammar newGrammar(String tableName) {
        if (dbType == null) {
            throw new IllegalStateException(
                "DbType not bound. This config should be obtained via forProductName() first.");
        }
        return createGrammar(tableName, dbType);
    }

    @Override
    public <T, K> Builder<?, T, K> newBuilder(GaarasonDataSource gaarasonDataSource, Model<?, T, K> model) {
        return new QueryBuilder<T, K>().initBuilder(
            gaarasonDataSource, ObjectUtils.typeCast(model), newGrammar(model.getTableName()));
    }

    /**
     * 根据数据库类型创建对应方言的 Grammar 实例
     * @param tableName 表名
     * @param type 数据库类型
     * @return Grammar 实例
     */
    protected Grammar createGrammar(String tableName, DbType type) {
        String quote = type.getIdentifierQuote();
        switch (type.getDialectGroup()) {
            case MYSQL:
                return newMySqlGrammar(tableName, quote);
            case POSTGRESQL:
                return new PostgreSqlGrammar(tableName, quote);
            case ORACLE:
                return new OracleGrammar(tableName);
            case ORACLE_12C:
                return new Oracle12cGrammar(tableName, quote);
            case SQL_SERVER:
                return new MsSqlGrammar(tableName);
            case DB2:
                return new Db2Grammar(tableName);
            case INFORMIX:
                return new InformixGrammar(tableName);
            case FIREBIRD:
                return new FirebirdGrammar(tableName);
            default:
                return new PostgreSqlGrammar(tableName, quote);
        }
    }

    /**
     * MySQL 方言 Grammar (BaseGrammar 的默认行为即 MySQL 兼容)
     */
    private static Grammar newMySqlGrammar(String tableName, String quote) {
        return new BaseGrammar(tableName, quote) {
            private static final long serialVersionUID = 1L;
        };
    }
}
