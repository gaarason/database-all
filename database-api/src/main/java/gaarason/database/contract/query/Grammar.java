package gaarason.database.contract.query;

import gaarason.database.appointment.SqlType;
import gaarason.database.contract.eloquent.Record;
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
     * 引号
     * @return 反引号 or 引号, 一般根据数据库锁支持的来
     */
    String symbol();

    /**
     * 是否使用别名
     * @param isUse 使用别名
     */
    void useAlias(boolean isUse);

    /**
     * 同步别名
     * @param alias 别名
     */
    void alias(Alias alias);

    /**
     * 获取别名
     * @return 当前别名
     */
    Alias alias();

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
     * 清空指定sql片段类型
     * @param sqlPartType SQL片段类型
     */
    void clear(SQLPartType sqlPartType);

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
     * 记录关联关系信息
     * @param targetFieldName 目标属性(当前模块的属性名)
     * @param relation 关联关系信息
     */
    void pushRelation(String targetFieldName, Record.Relation relation);


    /**
     * 拉取关联关系信息
     * @return 目标属性 -> 关联关系信息
     */
    Map<String, Record.Relation> pullRelation();

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

        public SQLPartInfo(String sqlString) {
            this(sqlString, null);
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
