package gaarason.database.connection;

import gaarason.database.lang.Nullable;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.function.Supplier;

/**
 * 数据源组路由上下文
 * <p>
 * 基于 ThreadLocal 的栈式实现, 支持嵌套切换并自动恢复前值.
 * 不依赖 Spring, 可在任意 Java 环境中使用.
 * @author xt
 */
public final class GaarasonDataSourceContext {

    private static final ThreadLocal<Deque<String>> CONTEXT = ThreadLocal.withInitial(ArrayDeque::new);

    private GaarasonDataSourceContext() {
    }

    /**
     * 设置当前线程使用的数据源组(入栈)
     * @param groupKey 数据源组名
     */
    public static void set(String groupKey) {
        CONTEXT.get().push(groupKey);
    }

    /**
     * 获取当前线程使用的数据源组
     * @return 当前组名, 未设置时返回 null(使用默认组)
     */
    @Nullable
    public static String get() {
        Deque<String> stack = CONTEXT.get();
        return stack.isEmpty() ? null : stack.peek();
    }

    /**
     * 清除当前层级的数据源组设置(出栈), 恢复到上一层
     */
    public static void clear() {
        Deque<String> stack = CONTEXT.get();
        if (!stack.isEmpty()) {
            stack.pop();
        }
        if (stack.isEmpty()) {
            CONTEXT.remove();
        }
    }

    /**
     * 在指定数据源组内执行逻辑, 执行完毕后自动恢复
     * @param groupKey 数据源组名
     * @param supplier 业务逻辑
     * @param <T> 返回值类型
     * @return 业务逻辑返回值
     */
    public static <T> T execute(String groupKey, Supplier<T> supplier) {
        set(groupKey);
        try {
            return supplier.get();
        } finally {
            clear();
        }
    }

    /**
     * 在指定数据源组内执行逻辑(无返回值), 执行完毕后自动恢复
     * @param groupKey 数据源组名
     * @param runnable 业务逻辑
     */
    public static void execute(String groupKey, Runnable runnable) {
        set(groupKey);
        try {
            runnable.run();
        } finally {
            clear();
        }
    }
}
