package gaarason.database.contract.support;

import gaarason.database.contract.function.ColumnFunctionalInterface;

import java.io.Serializable;
import java.util.Collection;
import java.util.stream.Collectors;

/**
 * 表达式风格
 * @param <T>
 * @param <K>
 */
public interface LambdaStyle<T extends Serializable, K extends Serializable> {

    /**
     * 通过 表达式 推断属性名
     * @param column Lambda表达式
     * @return 列名
     */
    String lambda2FieldName(ColumnFunctionalInterface<T> column);

    /**
     * 通过 表达式 推断列名
     * @param column Lambda表达式
     * @return 列名
     */
    String lambda2ColumnName(ColumnFunctionalInterface<T> column);

    /**
     * 通过 表达式 推断列名
     * @param columns Lambda表达式集合
     * @return 列名集合
     */
    default Collection<String> lambda2ColumnName(Collection<ColumnFunctionalInterface<T>> columns) {
        return columns.stream().map(this::lambda2ColumnName).collect(Collectors.toList());
    }
}
