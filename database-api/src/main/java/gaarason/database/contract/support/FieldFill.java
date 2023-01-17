package gaarason.database.contract.support;

import gaarason.database.lang.Nullable;

import java.lang.reflect.Field;

/**
 * 字段填充
 */
public interface FieldFill {

    /**
     * 插入时
     * @param entity 实体对象
     * @param field 属性字段, 应仅用于读取信息, 而非进行改动与赋值
     * @param originalValue 原始值
     * @return 填充的值
     */
    @Nullable
    <W> W inserting(Object entity, Field field, @Nullable W originalValue);

    /**
     * 更新时
     * @param entity 实体对象
     * @param field 属性字段, 应仅用于读取信息, 而非进行改动与赋值
     * @param originalValue 原始值
     * @return 填充的值
     */
    @Nullable
    <W> W updating(Object entity, Field field, @Nullable W originalValue);

    /**
     * 作为条件时
     * @param entity 实体对象
     * @param field 属性字段, 应仅用于读取信息, 而非进行改动与赋值
     * @param originalValue 原始值
     * @return 填充的值
     */
    @Nullable
    <W> W condition(Object entity, Field field, @Nullable W originalValue);

    /**
     * 默认实现
     */
    class NotFill implements FieldFill {

        @Nullable
        @Override
        public <W> W inserting(Object entity, Field field, @Nullable W originalValue) {
            return originalValue;
        }

        @Nullable
        @Override
        public <W> W updating(Object entity, Field field, @Nullable W originalValue) {
            return originalValue;
        }

        @Nullable
        @Override
        public <W> W condition(Object entity, Field field, @Nullable W originalValue) {
            return originalValue;
        }
    }
}
