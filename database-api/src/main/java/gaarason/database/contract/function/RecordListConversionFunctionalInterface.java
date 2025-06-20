package gaarason.database.contract.function;

import gaarason.database.contract.eloquent.RecordList;

import java.util.List;

/**
 * 结果集集合转化
 * @author xt
 */
@FunctionalInterface
public interface RecordListConversionFunctionalInterface<T, K, V> {

    /**
     * 结果集转化
     * @param tkRecord 结果集集合
     * @return 转化后的结果
     */
    List<V> execute(RecordList<T, K> tkRecord);
}
