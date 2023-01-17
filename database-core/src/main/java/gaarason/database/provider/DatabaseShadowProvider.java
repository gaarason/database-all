package gaarason.database.provider;

import gaarason.database.appointment.DBColumn;
import gaarason.database.contract.connection.GaarasonDataSource;
import gaarason.database.util.DatabaseInfoUtils;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 数据库信息大全
 * @author xt
 */
public final class DatabaseShadowProvider {

    /**
     * 表信息
     */
    private static final Map<GaarasonDataSource, ConcurrentHashMap<String, LinkedHashMap<String, DBColumn>>> INFO = new ConcurrentHashMap<>();

    private DatabaseShadowProvider() {

    }

    /**
     * 获取表的信息
     * @param gaarasonDataSource 数据源
     * @param table 表名
     * @return 表信息
     */
    public static Map<String, DBColumn> getTable(GaarasonDataSource gaarasonDataSource, String table) {
        ConcurrentHashMap<String, LinkedHashMap<String, DBColumn>> manyTableInfoMap = INFO.get(gaarasonDataSource);
        if (manyTableInfoMap == null) {
            synchronized (gaarasonDataSource) {
                manyTableInfoMap = INFO.get(gaarasonDataSource);
                if (manyTableInfoMap == null) {
                    // 获取表信息
                    manyTableInfoMap = new ConcurrentHashMap<>(16);
                    // 进入缓存
                    INFO.put(gaarasonDataSource, manyTableInfoMap);
                }
            }
        }
        return getTableFromGaarasonDataSource(gaarasonDataSource, manyTableInfoMap, table);
    }

    /**
     * 获取表的信息
     * @param gaarasonDataSource 数据源
     * @param manyTableInfoMap 许多的表信息
     * @param table 表名
     * @return 表信息
     */
    private static Map<String, DBColumn> getTableFromGaarasonDataSource(GaarasonDataSource gaarasonDataSource,
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
