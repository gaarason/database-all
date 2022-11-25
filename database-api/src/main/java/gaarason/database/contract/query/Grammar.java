package gaarason.database.contract.query;

import gaarason.database.appointment.SqlType;
import gaarason.database.contract.function.GenerateSqlPartFunctionalInterface;
import gaarason.database.contract.function.RelationshipRecordWithFunctionalInterface;
import gaarason.database.exception.CloneNotSupportedRuntimeException;
import gaarason.database.lang.Nullable;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;

/**
 * 语法
 * @author xt
 */
public interface Grammar {

    /**
     * 返回替换参数后的字符, 并填充到"绑定参数集合"
     * @param value 参数 eg: 1
     * @param parameters 绑定参数集合, 引用地址
     * @return 替换后的参数  ?
     */
    String replaceValueAndFillParameters(@Nullable Object value, Collection<Object> parameters);

    /**
     * 返回替换参数后的字符, 并填充到"绑定参数集合"
     * @param values 参数集合 eg:  1 , 2, 3
     * @param parameters 绑定参数集合, 引用地址
     * @return 替换后的参数  ? , ? , ?
     */
    String replaceValuesAndFillParameters(Collection<?> values, Collection<Object> parameters, String separator);

    /**
     * 加入sql片段(片段尾部), 自动处理首个的情况
     * @param sqlPartType SQL片段类型
     * @param sqlPartString SQL片段
     * @param parameters 绑定参数集合
     */
    void addSmartSeparator(SQLPartType sqlPartType, String sqlPartString, @Nullable Collection<Object> parameters);

    /**
     * 加入sql片段(片段首部), 自动处理首个的情况
     * @param sqlPartType SQL片段类型
     * @param sqlPartString SQL片段
     * @param parameters 绑定参数集合
     */
    void addFirstSmartSeparator(SQLPartType sqlPartType, String sqlPartString, @Nullable Collection<Object> parameters);

    /**
     * 加入sql片段(片段尾部), 自动处理首个的情况
     * @param sqlPartType SQL片段类型
     * @param sqlPartString SQL片段
     * @param parameters 绑定参数集合
     * @param separator 分割符号
     */
    void addSmartSeparator(SQLPartType sqlPartType, String sqlPartString, @Nullable Collection<Object> parameters,
        String separator);

    /**
     * 加入sql片段(片段首部), 自动处理首个的情况
     * @param sqlPartType SQL片段类型
     * @param sqlPartString SQL片段
     * @param parameters 绑定参数集合
     * @param separator 分割符号
     */
    void addFirstSmartSeparator(SQLPartType sqlPartType, String sqlPartString, @Nullable Collection<Object> parameters,
        String separator);

    /**
     * 加入sql片段(片段尾部)
     * @param sqlPartType SQL片段类型
     * @param sqlPartString SQL片段
     * @param parameters 绑定参数集合
     */
    void add(SQLPartType sqlPartType, String sqlPartString, @Nullable Collection<Object> parameters);

    /**
     * 加入sql片段(片段首部)
     * @param sqlPartType SQL片段类型
     * @param sqlPartString SQL片段
     * @param parameters 绑定参数集合
     */
    void addFirst(SQLPartType sqlPartType, String sqlPartString, @Nullable Collection<Object> parameters);

    /**
     * 设置sql片段
     * @param sqlPartType SQL片段类型
     * @param sqlPartString SQL片段
     * @param parameters 绑定参数集合
     */
    void set(SQLPartType sqlPartType, String sqlPartString, @Nullable Collection<Object> parameters);

    /**
     * SQL片段类型 是否为空
     * @param sqlPartType SQL片段类型
     */
    boolean isEmpty(SQLPartType sqlPartType);

    /**
     * 获取指定的片段
     * @param sqlPartType SQL片段类型
     * @return SQL片段信息
     */
    SQLPartInfo get(SQLPartType sqlPartType);

    /**
     * 连接sql片段
     * @param sqlType SQL类型
     * @param sqlPartType SQL片段类型
     * @param sqlBuilder SQL 语句构造器
     * @param allParameters 绑定参数 收集集合
     */
    void concatenate(SqlType sqlType, SQLPartType sqlPartType, StringBuilder sqlBuilder,
        Collection<Object> allParameters);

    /**
     * 按照类型生成sql
     * @param sqlType 类型
     * @return sql
     */
    SQLPartInfo generateSql(SqlType sqlType);

    /**
     * 深度copy
     * @return 和当前属性值一样的全新对象
     * @throws CloneNotSupportedRuntimeException 克隆异常
     */
    Grammar deepCopy() throws CloneNotSupportedRuntimeException;

    /**
     * 将目标grammar合并到自身
     * @param grammar 目标grammar
     */
    void merger(Grammar grammar);

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
    enum SQLPartType implements Serializable {
        SELECT("select "), COLUMN(""), DATA(" set "), VALUE(" values "), FROM(" from "), TABLE(""),
        FORCE_INDEX(" force index "), IGNORE_INDEX(" ignore index "), ORDER(" order by "), LIMIT(" limit "),
        GROUP(" group by "), JOIN(""), WHERE(" where "), HAVING(" having "), LOCK(""), UNION("");

        private static final long serialVersionUID = 1L;

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
    class SQLPartInfo implements Serializable {

        private static final long serialVersionUID = 1L;

        private final String sqlString;

        @Nullable
        private final Collection<Object> parameters;

        public SQLPartInfo(String sqlString, @Nullable Collection<Object> parameters) {
            this.sqlString = sqlString;
            this.parameters = parameters;
        }

        public String getSqlString() {
            return sqlString;
        }

        @Nullable
        public Collection<Object> getParameters() {
            return parameters;
        }
    }
}
