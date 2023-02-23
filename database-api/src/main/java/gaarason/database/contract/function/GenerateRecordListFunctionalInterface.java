package gaarason.database.contract.function;

import gaarason.database.contract.eloquent.RecordList;
import gaarason.database.lang.Nullable;

/**
 * @author xt
 */
@FunctionalInterface
public interface GenerateRecordListFunctionalInterface {

    /**
     * RecordList关联关系
     * @return Record 查询结果集
     */
    RecordList<?, ?> execute();
}
