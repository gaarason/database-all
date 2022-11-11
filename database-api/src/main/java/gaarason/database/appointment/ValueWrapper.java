package gaarason.database.appointment;

import gaarason.database.lang.Nullable;

/**
 * 值包装
 * 1. 避免异常堆栈造成的性能降低
 * 2. 避免返回结果的歧义性
 * @param <V> 原始结果类型
 */
public class ValueWrapper<V> {
    /**
     * 是否有效
     */
    private final boolean valid;
    /**
     * 有效值
     */
    @Nullable
    private final V value;

    public ValueWrapper(boolean valid, @Nullable V value) {
        this.valid = valid;
        this.value = value;
    }

    public boolean isValid() {
        return valid;
    }

    @Nullable
    public V getValue() {
        return value;
    }

    /**
     * 结果包装
     * 不可以为 null
     * @param <V> 原始结果类型
     */
    public static class NotNull<V> extends ValueWrapper<V> {

        public NotNull(boolean valid, @Nullable V value) {
            super(valid, value);
        }

        @Override
        public V getValue() {
            return super.getValue();
        }
    }
}
