package gaarason.database.util;

import gaarason.database.contract.connection.GaarasonDataSource;
import gaarason.database.core.lang.Nullable;
import gaarason.database.support.DBColumn;
import lombok.SneakyThrows;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class DatabaseInfoUtil {

    /**
     * 获取表名集合
     * @param gaarasonDataSource 数据源
     * @param tableNamePattern   表名过滤表达式(null 表示全部获取)
     * @return 表名集合
     */
    @SneakyThrows
    public static Set<String> tableNames(GaarasonDataSource gaarasonDataSource, @Nullable String tableNamePattern) {
        Set<String> tableNames = new HashSet<>();
        try (Connection connection = gaarasonDataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            //获取表名的结果集
            ResultSet rs = metaData.getTables(connection.getSchema(), metaData.getUserName(), tableNamePattern,
                new String[]{"TABLE"});
            while (rs.next()) {
                String tableName = rs.getString("TABLE_NAME");
                tableNames.add(tableName);
            }
            return tableNames;
        }
    }

    /**
     * 获取表字段信息
     * @param gaarasonDataSource 数据源
     * @param tableNamePattern   表名过滤表达式(null 表示全部获取)
     * @return 表字段信息MAP
     */
    @SneakyThrows
    public static ConcurrentHashMap<String, DBColumn> getDBColumnsWIthTable(GaarasonDataSource gaarasonDataSource,
        @Nullable String tableNamePattern) {
        try (Connection connection = gaarasonDataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            ResultSet rs = metaData.getColumns(connection.getCatalog(), null, tableNamePattern, null);
            ConcurrentHashMap<String, DBColumn> dbColumnConcurrentHashMap = new ConcurrentHashMap<>();
            while (rs.next()) {
                DBColumn dbColumn = new DBColumn(rs);
                dbColumnConcurrentHashMap.put(dbColumn.getColumnName(), dbColumn);
            }
            return dbColumnConcurrentHashMap;
        }
    }
}
