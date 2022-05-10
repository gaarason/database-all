package gaarason.database.contract.function;

import java.io.Serializable;
import java.util.function.Function;

/**
 * 使用 Lambda 风格的列名
 * @author xt
 */
@FunctionalInterface
public interface ColumnFunctionalInterface<T extends Serializable> extends Function<T, Object>,Serializable {


}
