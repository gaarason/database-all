package gaarason.database.contract.builder;

import gaarason.database.contract.eloquent.Builder;
import gaarason.database.contract.function.ColumnFunctionalInterface;
import gaarason.database.contract.support.LambdaStyle;

/**
 * 支持
 * @author xt
 */
public interface SupportLambda<B extends Builder<B, T, K>, T, K> extends Support<B, T, K>, LambdaStyle, Cloneable{

    /**
     * 给列名增肌别名
     * @param column 列名 eg: name
     * @return eg: `table_12376541`.`name`
     */
    default <F> String columnAlias(ColumnFunctionalInterface<T, F> column) {
        return columnAlias(lambda2ColumnName(column));
    }

}
