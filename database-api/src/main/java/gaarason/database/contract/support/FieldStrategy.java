package gaarason.database.contract.support;

import gaarason.database.lang.Nullable;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

/**
 * 字段的使用策略
 */
public interface FieldStrategy {

    /**
     * 是否使用
     * @param originalValue 字段的原始值
     * @return 是否使用
     */
    boolean enable(@Nullable Object originalValue);

    /**
     * 从不使用
     */
    class Never implements FieldStrategy {
        @Override
        public boolean enable(@Nullable Object originalValue) {
            return false;
        }
    }

    /**
     * 总是使用
     */
    class Always implements FieldStrategy {
        @Override
        public boolean enable(@Nullable Object originalValue) {
            return true;
        }
    }

    /**
     * 非NULL则使用
     */
    class NotNull implements FieldStrategy {
        @Override
        public boolean enable(@Nullable Object originalValue) {
            if (originalValue == null) {
                return false;
            } else if (originalValue instanceof Optional) {
                return ((Optional<?>) originalValue).isPresent();
            }
            return true;
        }
    }

    /**
     * 非EMPTY则使用
     * 对于字符类型, 等价于 s !=null && s != ""
     * 对于非集合类型, 等价于 NOT_NULL
     * 对于集合类型, 等价于 !s.isEmpty()
     */
    class NotEmpty implements FieldStrategy {
        @Override
        public boolean enable(@Nullable Object originalValue) {
            if (originalValue == null) {
                return false;
            } else if (originalValue instanceof Optional) {
                return ((Optional<?>) originalValue).isPresent();
            } else if (originalValue instanceof CharSequence) {
                return ((CharSequence) originalValue).length() != 0;
            } else if (originalValue.getClass().isArray()) {
                return Array.getLength(originalValue) != 0;
            } else if (originalValue instanceof Collection) {
                return !((Collection<?>) originalValue).isEmpty();
            } else {
                return !(originalValue instanceof Map && ((Map<?, ?>) originalValue).isEmpty());
            }
        }
    }

    /**
     * 跟随默认值
     */
    class Default extends NotNull {
    }
}
