package gaarason.database.provider;

import gaarason.database.contract.connection.GaarasonDataSource;
import gaarason.database.support.Column;
import gaarason.database.support.DBColumn;
import gaarason.database.util.DatabaseInfoUtil;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 数据库信息大全
 */
final public class DBShadowProvider {

    final protected static Map<GaarasonDataSource, ConcurrentHashMap<String, ConcurrentHashMap<String, DBColumn>>> info = new ConcurrentHashMap<>();

    public static ConcurrentHashMap<String, DBColumn> getTable(GaarasonDataSource gaarasonDataSource, String table) {
        ConcurrentHashMap<String, ConcurrentHashMap<String, DBColumn>> manyTableInfoMap = info.get(gaarasonDataSource);
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

//    public static ConcurrentHashMap<String, ConcurrentHashMap<String, Column>> getGaarasonDataSource(GaarasonDataSource gaarasonDataSource) {
//        ConcurrentHashMap<String, ConcurrentHashMap<String, Column>> tableInfoMap = info.get(gaarasonDataSource);
//        if (tableInfoMap == null) {
//            synchronized (gaarasonDataSource) {
//                tableInfoMap = info.get(gaarasonDataSource);
//                if (tableInfoMap == null) {
//                    // 获取表信息
//                    tableInfoMap = new ConcurrentHashMap<>();
//
//                    // 缓存
//                    info.put(gaarasonDataSource, tableInfoMap);
//                }
//            }
//        }
//        return tableInfoMap;
//    }


    protected static ConcurrentHashMap<String, DBColumn> getTableFromGaarasonDataSource(GaarasonDataSource gaarasonDataSource,
        ConcurrentHashMap<String, ConcurrentHashMap<String, DBColumn>> manyTableInfoMap, String table) {
        ConcurrentHashMap<String, DBColumn> tableInfoMap = manyTableInfoMap.get(table);
        if (tableInfoMap == null) {
            String key = gaarasonDataSource.hashCode() + table;
            synchronized (key.intern()) {
                tableInfoMap = manyTableInfoMap.get(table);
                if (tableInfoMap == null) {
                    tableInfoMap = DatabaseInfoUtil.getDBColumnsWIthTable(gaarasonDataSource, table);
                    manyTableInfoMap.put(table, tableInfoMap);
                }
            }
        }
        return tableInfoMap;
    }

}
