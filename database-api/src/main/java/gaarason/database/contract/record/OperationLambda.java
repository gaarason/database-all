package gaarason.database.contract.record;

import gaarason.database.contract.function.ColumnFunctionalInterface;
import gaarason.database.contract.support.LambdaStyle;
import gaarason.database.exception.EntityAttributeInvalidException;
import gaarason.database.lang.Nullable;

import java.util.Arrays;

/**
 * ORM 操作
 * @author xt
 */
public interface OperationLambda<T, K> extends Operation<T, K>, LambdaStyle {

    /**
     * 指定属性是否有发生改变
     * @param fieldNames 实体属性名表达式
     * @param <F> 属性类型
     * @return bool
     */
    @SuppressWarnings("unchecked")
    default <F> boolean isDirty(ColumnFunctionalInterface<T, F>... fieldNames) {
        return isDirty(lambda2FieldName(Arrays.asList(fieldNames)));
    }

    /**
     * 指定属性是否没有发生改变
     * @param fieldNames 实体属性名
     * @param <F> 属性类型
     * @return bool
     */
    @SuppressWarnings("unchecked")
    default <F> boolean isClean(ColumnFunctionalInterface<T, F>... fieldNames) {
        return isClean(lambda2FieldName(Arrays.asList(fieldNames)));
    }

    /**
     * 初始以来, 指定的数据是否存在已提交到数据库的变化
     * 比较 getOriginal(field) 与 当前属性的已提交数据
     * @param fieldNames 实体属性名
     * @return bool
     */
    default <F> boolean wasChanged(ColumnFunctionalInterface<T, F>... fieldNames) {
        return wasChanged(lambda2FieldName(Arrays.asList(fieldNames)));
    }

    /**
     * 返回模型原生属性的值
     * @param fieldName 实体属性名
     * @param <F> 属性类型
     * @return 实体对象
     * @throws EntityAttributeInvalidException 无效的属性(实体声明中没有的属性)
     */
    @Nullable
    default <F> Object getOriginal(ColumnFunctionalInterface<T, F> fieldName) throws EntityAttributeInvalidException {
        return getOriginal(lambda2FieldName(fieldName));
    }

}
