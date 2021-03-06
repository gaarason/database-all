package gaarason.database.contract.query;

import gaarason.database.contract.function.GenerateSqlPartFunctionalInterface;
import gaarason.database.contract.function.RelationshipRecordWithFunctionalInterface;
import gaarason.database.eloquent.appointment.SqlType;
import gaarason.database.exception.CloneNotSupportedRuntimeException;
import gaarason.database.util.ObjectUtil;

import java.util.List;
import java.util.Map;

public interface Grammar {

    void pushSelect(String something);

    void pushData(String something);

    void pushFrom(String something);

    void pushForceIndex(String indexName);

    void pushIgnoreIndex(String indexName);

    void pushOrderBy(String something);

    void pushLimit(String something);

    void pushGroup(String something);

    void pushColumn(String something);

    void pushJoin(String something);

    void pushWhere(String something, String relationship);

    void pushHaving(String something, String relationship);

    void pushValue(String something);

    void pushLock(String something);

    void pushUnion(String something, String unionType);

    void pushWhereParameter(String value);

    void pushDataParameter(String value);

    List<String> getParameterList(SqlType sqlType);

    boolean hasSelect();

    boolean hasWhere();

    boolean hasGroup();

    String getGroup();

    boolean hasOrderBy();

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
    default Grammar deepCopy() throws CloneNotSupportedRuntimeException {
        return ObjectUtil.deepCopy(this);
    }

    /**
     * 记录with信息
     * @param column         所关联的Model(当前模块的属性名)
     * @param builderClosure 所关联的Model的查询构造器约束
     * @param recordClosure  所关联的Model的再一级关联
     */
    void pushWith(String column, GenerateSqlPartFunctionalInterface builderClosure,
                  RelationshipRecordWithFunctionalInterface recordClosure);

    /**
     * 拉取with信息
     * @return map
     */
    Map<String, Object[]> pullWith();
}
