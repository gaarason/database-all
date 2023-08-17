package gaarason.database.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Map工具类
 */
public final class MapUtils {

    private MapUtils() {

    }

    /**
     * map的值转化为list
     * 保证map遍历时的顺序
     * @param map map
     * @param <T> 值类型
     * @return list
     */
    public static <T> List<T> mapValueToList(Map<?, T> map) {
        List<T> res = new ArrayList<>(map.size());
        for (Map.Entry<?, T> entry : map.entrySet()) {
            res.add(entry.getValue());
        }
        return res;
    }
}
