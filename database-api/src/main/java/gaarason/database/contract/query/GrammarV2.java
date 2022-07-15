package gaarason.database.contract.query;

import gaarason.database.appointment.SqlType;
import gaarason.database.contract.function.GenerateSqlPartFunctionalInterface;
import gaarason.database.contract.function.RelationshipRecordWithFunctionalInterface;
import gaarason.database.exception.CloneNotSupportedRuntimeException;

import java.util.Collection;
import java.util.Map;

/**
 * 语法
 * @author xt
 */
public interface GrammarV2 {

    /**
     * 加入sql片段, 自动处理首个的情况
     * @param sqlPartType SQL片段类型
     * @param sqlPartString SQL片段
     * @param parameters 绑定参数集合
     */
    void addSmartSeparator(SQLPartType sqlPartType, String sqlPartString, Collection<String> parameters);

    /**
     * 加入sql片段, 自动处理首个的情况
     * @param sqlPartType SQL片段类型
     * @param sqlPartString SQL片段
     * @param parameters 绑定参数集合
     * @param separator 分割符号
     */
    void addSmartSeparator(SQLPartType sqlPartType, String sqlPartString, Collection<String> parameters,
        String separator);

//    void addSelect(String sqlPartString, Collection<String> parameters);
//
//    void addColumn(String sqlPartString, Collection<String> parameters);
//
//    void addWhere(String sqlPartString, Collection<String> parameters, String relationship);
//
//    void addHaving(String sqlPartString, Collection<String> parameters, String relationship);

    /**
     * 加入sql片段
     * @param sqlPartType SQL片段类型
     * @param sqlPartString SQL片段
     * @param parameters 绑定参数集合
     */
    void add(SQLPartType sqlPartType, String sqlPartString, Collection<String> parameters);

    /**
     * 设置sql片段
     * @param sqlPartType SQL片段类型
     * @param sqlPartString SQL片段
     * @param parameters 绑定参数集合
     */
    void set(SQLPartType sqlPartType, String sqlPartString, Collection<String> parameters);

    /**
     * SQL片段类型 是否为空
     * @param sqlPartType SQL片段类型
     */
    boolean isEmpty(SQLPartType sqlPartType);

    /**
     * 连接sql片段
     * @param sqlType SQL类型
     * @param sqlPartType SQL片段类型
     * @param sqlBuilder SQL 语句构造器
     * @param allParameters 绑定参数 收集集合
     * @return
     */
    void concatenate(SqlType sqlType, SQLPartType sqlPartType, StringBuilder sqlBuilder,
        Collection<String> allParameters);

    /**
     * 按照类型生成sql
     * @param sqlType 类型
     * @return sql
     */
    SQLPartInfo generateSql(SqlType sqlType);

    /**
     * 在统计时,需要剔除一些项目,eg: order by , select
     * count(*)
     */
    void forAggregates();

    /**
     * 深度copy
     * @return 和当前属性值一样的全新对象
     * @throws CloneNotSupportedRuntimeException 克隆异常
     */
    GrammarV2 deepCopy() throws CloneNotSupportedRuntimeException;

    /**
     * 记录with信息
     * @param column 所关联的Model(当前模块的属性名)
     * @param builderClosure 所关联的Model的查询构造器约束
     * @param recordClosure 所关联的Model的再一级关联
     */
    void pushWith(String column, GenerateSqlPartFunctionalInterface<?, ?> builderClosure,
        RelationshipRecordWithFunctionalInterface recordClosure);

    /**
     * 拉取with信息
     * @return map
     */
    Map<String, Object[]> pullWith();

    /**
     * SQL片段类型
     */
    enum SQLPartType {
        SELECT("select "), COLUMN(""), DATA(" set "), VALUE(" values "), FROM(" from "),
        FORCE_INDEX(" force index "), IGNORE_INDEX(" ignore index "), ORDER(" order by "),
        LIMIT(" limit "), GROUP(" group by "), JOIN(""), WHERE(" where "),
        HAVING(" having "), LOCK(""), UNION("");

        private final String keyword;

        SQLPartType(String keyword) {
            this.keyword = keyword;
        }

        public String getKeyword() {
            return keyword;
        }
    }

    /**
     * SQL片段信息
     */
    class SQLPartInfo {
        private final String sqlString;
        private final Collection<String> parameters;

        public SQLPartInfo(String sqlString, Collection<String> parameters) {
            this.sqlString = sqlString;
            this.parameters = parameters;
        }

        public String getSqlString() {
            return sqlString;
        }

        public Collection<String> getParameters() {
            return parameters;
        }
    }
}
