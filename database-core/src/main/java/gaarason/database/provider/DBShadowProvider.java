package gaarason.database.provider;

import gaarason.database.contract.connection.GaarasonDataSource;
import gaarason.database.support.DBColumn;
import gaarason.database.util.DatabaseInfoUtils;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 数据库信息大全
 */
final public class DBShadowProvider {

    final protected static Map<GaarasonDataSource, ConcurrentHashMap<String, LinkedHashMap<String, DBColumn>>> info = new ConcurrentHashMap<>();

    public static LinkedHashMap<String, DBColumn> getTable(GaarasonDataSource gaarasonDataSource, String table) {
        ConcurrentHashMap<String, LinkedHashMap<String, DBColumn>> manyTableInfoMap = info.get(gaarasonDataSource);
        if (manyTableInfoMap == null) {
            synchronized (gaarasonDataSource) {
                manyTableInfoMap = info.get(gaarasonDataSource);
                if (manyTableInfoMap == null) {
                    // 获取表信息
                    manyTableInfoMap = new ConcurrentHashMap<>();
                    // 进入缓存
                    info.put(gaarasonDataSource, manyTableInfoMap);
                }
            }
        }
        return getTableFromGaarasonDataSource(gaarasonDataSource, manyTableInfoMap, table);
    }

    protected static LinkedHashMap<String, DBColumn> getTableFromGaarasonDataSource(GaarasonDataSource gaarasonDataSource,
        ConcurrentHashMap<String, LinkedHashMap<String, DBColumn>> manyTableInfoMap, String table) {
        LinkedHashMap<String, DBColumn> tableInfoMap = manyTableInfoMap.get(table);
        if (tableInfoMap == null) {
            String key = gaarasonDataSource.hashCode() + table;
            synchronized (key.intern()) {
                tableInfoMap = manyTableInfoMap.get(table);
                if (tableInfoMap == null) {
                    tableInfoMap = DatabaseInfoUtils.getDBColumnsWIthTable(gaarasonDataSource, table);
                    manyTableInfoMap.put(table, tableInfoMap);
                }
            }
        }
        return tableInfoMap;
    }

}
