package gaarason.database.contract.record;

import gaarason.database.contract.function.ColumnFunctionalInterface;
import gaarason.database.contract.support.LambdaStyle;
import gaarason.database.exception.EntityAttributeInvalidException;
import gaarason.database.lang.Nullable;

import java.io.Serializable;

/**
 * ORM 操作
 * @author xt
 */
public interface OperationLambda<T extends Serializable, K extends Serializable> extends Operation<T, K>, LambdaStyle<T, K> {

    /**
     * 指定属性是否有发生改变
     * @param fieldName 实体属性名表达式
     * @return bool
     */
    default boolean isDirty(ColumnFunctionalInterface<T> fieldName) {
        return isDirty(lambda2FieldName(fieldName));
    }

    /**
     * 指定属性是否没有发生改变
     * @param fieldName 实体属性名
     * @return bool
     */
    default boolean isClean(ColumnFunctionalInterface<T> fieldName) {
        return isClean(lambda2FieldName(fieldName));
    }

    /**
     * 返回模型原生属性的值
     * @param fieldName 实体属性名
     * @return 实体对象
     * @throws EntityAttributeInvalidException 无效的属性(实体声明中没有的属性)
     */
    @Nullable
    default Object getOriginal(ColumnFunctionalInterface<T> fieldName) throws EntityAttributeInvalidException {
        return getOriginal(lambda2FieldName(fieldName));
    }

}
