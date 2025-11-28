package gaarason.database.contract.support;

import gaarason.database.contract.function.ColumnFunctionalInterface;
import gaarason.database.lang.Nullable;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * 表达式风格
 */
public interface LambdaStyle {

    /**
     * 通过 表达式 推断属性名
     * @param column Lambda表达式
     * @return 列名
     */
    String lambda2FieldName(ColumnFunctionalInterface<?, ?> column);

    /**
     * 通过 表达式 推断属性名
     * @param columns Lambda表达式数组
     * @return 列名数组
     */
    default String[] lambda2FieldName(ColumnFunctionalInterface<?, ?>... columns) {
        String[] columnArr = new String[columns.length];
        int i = 0;
        for (ColumnFunctionalInterface<?, ?> column : columns) {
            columnArr[i++] = lambda2FieldName(column);
        }
        return columnArr;
    }


    /**
     * 通过 表达式 推断属性名
     * @param column Lambda表达式
     * @return 列名
     */
    @Nullable
    default String lambda2FieldNameNullable(@Nullable ColumnFunctionalInterface<?, ?> column) {
        return column == null ? null : lambda2FieldName(column);
    }

    /**
     * 通过 表达式 推断列名
     * @param column Lambda表达式
     * @return 列名
     */
    String lambda2ColumnName(ColumnFunctionalInterface<?, ?> column);

    /**
     * 通过 表达式 推断列名
     * @param column Lambda表达式
     * @return 列名
     */
    @Nullable
    default String lambda2ColumnNameNullable(@Nullable ColumnFunctionalInterface<?, ?> column) {
        return column == null ? null : lambda2ColumnName(column);
    }

    /**
     * 通过 表达式 推断列名
     * @param columns Lambda表达式集合
     * @return 列名集合
     */
    default Collection<String> lambda2ColumnName(Collection<ColumnFunctionalInterface<?, ?>> columns) {
        return columns.stream().map(this::lambda2ColumnName).collect(Collectors.toList());
    }

    /**
     * 通过 表达式 推断属性名
     * @param columns Lambda表达式集合
     * @return 属性集合
     */
    default Collection<String> lambda2FieldName(Collection<ColumnFunctionalInterface<?, ?>> columns) {
        return columns.stream().map(this::lambda2FieldName).collect(Collectors.toList());
    }
}
