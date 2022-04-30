package gaarason.database.contract.query;

import gaarason.database.appointment.SqlType;
import gaarason.database.contract.function.GenerateSqlPartFunctionalInterface;
import gaarason.database.contract.function.RelationshipRecordWithFunctionalInterface;
import gaarason.database.exception.CloneNotSupportedRuntimeException;
import gaarason.database.exception.GrammarException;

import java.util.List;
import java.util.Map;

/**
 * 语法
 * @author xt
 */
public interface Grammar {

    /**
     * 加入 select 语法片段
     * @param something 片段
     */
    void pushSelect(String something);

    /**
     * 加入 data 语法片段
     * @param something 片段
     */
    void pushData(String something);

    /**
     * 加入 from 语法片段
     * @param something 片段
     */
    void pushFrom(String something);

    /**
     * 加入 force index 语法片段
     * @param indexName 索引列名
     */
    void pushForceIndex(String indexName);

    /**
     * 加入 ignore select 语法片段
     * @param indexName 索引列名
     */
    void pushIgnoreIndex(String indexName);

    /**
     * 加入 order by 语法片段
     * @param something 片段
     */
    void pushOrderBy(String something);

    /**
     * 加入 limit 语法片段
     * @param something 片段
     */
    void pushLimit(String something);

    /**
     * 加入 group 语法片段
     * @param something 片段
     */
    void pushGroup(String something);

    /**
     * 加入 column 语法片段
     * @param something 片段
     */
    void pushColumn(String something);

    /**
     * 加入 join 语法片段
     * @param something 片段
     */
    void pushJoin(String something);

    /**
     * 加入 where 语法片段
     * @param something    片段
     * @param relationship 连接关系
     */
    void pushWhere(String something, String relationship);

    /**
     * 加入 having 语法片段
     * @param something    片段
     * @param relationship 连接关系
     */
    void pushHaving(String something, String relationship);

    /**
     * 加入 value 语法片段
     * @param something 片段
     */
    void pushValue(String something);

    /**
     * 加入 lock 语法片段
     * @param something 片段
     */
    void pushLock(String something);

    /**
     * 加入 union 语法片段
     * @param something 片段
     * @param unionType 连接关系
     */
    void pushUnion(String something, String unionType);

    /**
     * 加入 Where Parameter
     * @param value 实际参数
     */
    void pushWhereParameter(String value);

    /**
     * 加入 Having Parameter
     * @param value 实际参数
     */
    void pushHavingParameter(String value);

    /**
     * 加入 Data Parameter
     * @param value 实际参数
     */
    void pushDataParameter(String value);

    /**
     * 获取 参数列表 ( 含 data 与 where 与 having)
     * @param sqlType sql类型
     * @return 参数列表
     */
    List<String> getAllParameterList(SqlType sqlType);

    /**
     * 将自身所有的参数列表, 复制最佳到目标
     * @param targetGrammar 目标
     */
    void copyAllParameterTo(Grammar targetGrammar);

    /**
     * 是否存在 select
     * @return 是否存在
     */
    boolean hasSelect();

    /**
     * 是否存在 where
     * @return 是否存在
     */
    boolean hasWhere();

    /**
     * 是否存在 group
     * @return 是否存在
     */
    boolean hasGroup();

    /**
     * 获取 group 片段
     * @return group 片段
     * @throws GrammarException group为null
     */
    String getGroup() throws GrammarException;

    /**
     * 是否存在 order by
     * @return 是否存在
     */
    boolean hasOrderBy();

    /**
     * 按照类型生成sql
     * @param sqlType 类型
     * @return sql
     */
    String generateSql(SqlType sqlType);

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
    Grammar deepCopy() throws CloneNotSupportedRuntimeException;

    /**
     * 记录with信息
     * @param column         所关联的Model(当前模块的属性名)
     * @param builderClosure 所关联的Model的查询构造器约束
     * @param recordClosure  所关联的Model的再一级关联
     */
    void pushWith(String column, GenerateSqlPartFunctionalInterface<?, ?> builderClosure,
        RelationshipRecordWithFunctionalInterface recordClosure);

    /**
     * 拉取with信息
     * @return map
     */
    Map<String, Object[]> pullWith();
}
