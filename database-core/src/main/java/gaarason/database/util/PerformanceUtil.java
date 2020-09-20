package gaarason.database.util;

import gaarason.database.eloquent.appointment.FinalVariable;

import java.util.Collection;
import java.util.stream.Stream;

public class PerformanceUtil {

    /**
     * 获取 steam
     * @param collection 集合
     * @param <V>        泛型
     * @return steam
     */
    public static <V> Stream<V> steam(Collection<V> collection) {
        return FinalVariable.defaultParallelStream ? collection.parallelStream() : collection.stream();
    }
}
