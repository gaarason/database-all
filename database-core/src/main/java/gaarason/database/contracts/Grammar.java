package gaarason.database.contracts;

import gaarason.database.eloquent.enums.SqlType;
import gaarason.database.exception.CloneNotSupportedRuntimeException;

import java.util.List;

public interface Grammar extends Cloneable {

    void pushSelect(String something);

    void pushData(String something);

    void pushFrom(String something);

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

    String generateSql(SqlType sqlType);

    void pushWhereParameter(String value);

    void pushDataParameter(String value);

    List<String> getParameterList(SqlType sqlType);

    boolean hasWhere();

    /**
     * 在统计时,需要剔除一些项目,eg: order by
     */
    void forAggregates();

    Grammar clone() throws CloneNotSupportedRuntimeException;
}
