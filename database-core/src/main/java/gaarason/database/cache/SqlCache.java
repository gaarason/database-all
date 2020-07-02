package gaarason.database.cache;

import java.util.HashMap;
import java.util.Map;

public class SqlCache<R> {

    private Map<String, R> sqlCache = new HashMap<>();

    public synchronized R putOne(String sql, R rowInfo) {
        return sqlCache.put(sql, rowInfo);
    }

    public synchronized R getOne(String sql) {
        return sqlCache.get(sql);
    }
}
