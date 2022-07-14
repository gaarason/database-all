package gaarason.database.contract.support;

import gaarason.database.lang.Nullable;

/**
 * 字段填充
 */
public interface FieldFill {

    /**
     * 插入时
     * @param entity 实体对象
     * @param originalValue 原始值
     * @return 填充的值
     */
    @Nullable
    <W> W inserting(Object entity, @Nullable W originalValue);

    /**
     * 更新时
     * @param entity 实体对象
     * @param originalValue 原始值
     * @return 填充的值
     */
    @Nullable
    <W> W updating(Object entity, @Nullable W originalValue);

    /**
     * 作为条件时
     * @param entity 实体对象
     * @param originalValue 原始值
     * @return 填充的值
     */
    @Nullable
    <W> W condition(Object entity, @Nullable W originalValue);

    /**
     * 默认实现
     */
    class DefaultFieldFill implements FieldFill {

        @Nullable
        @Override
        public <W> W inserting(Object entity, @Nullable W originalValue) {
            return originalValue;
        }

        @Nullable
        @Override
        public <W> W updating(Object entity, @Nullable W originalValue) {
            return originalValue;
        }

        @Nullable
        @Override
        public <W> W condition(Object entity, @Nullable W originalValue) {
            return originalValue;
        }
    }
}
