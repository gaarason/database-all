package gaarason.database.generator.util;

import gaarason.database.contract.connection.GaarasonDataSource;
import gaarason.database.core.lang.Nullable;
import gaarason.database.support.Column;
import gaarason.database.support.RecordFactory;
import lombok.SneakyThrows;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.*;

/**
 * 数据库信息工具
 * @author xt
 */
public class DatabaseInfoUtil {

    private DatabaseInfoUtil() {

    }

    /**
     * 获取数据库名称
     * @param gaarasonDataSource 数据源
     * @return 数据库名称
     */
    public static String databaseName(GaarasonDataSource gaarasonDataSource) {

        return "";
    }

    /**
     * 获取表名
     * @param gaarasonDataSource 数据源
     * @param databaseName       数据库名(null 表示全部获取)
     * @param userName           用户名
     * @param tableNamePattern   表名过滤表达式(null 表示全部获取)
     * @return 表名集合
     */
    @SneakyThrows
    public static Set<String> tableNames(GaarasonDataSource gaarasonDataSource, String databaseName, String userName,
        @Nullable String tableNamePattern) {
        Set<String> tableNames = new HashSet<>();
        try (Connection connection = gaarasonDataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            //获取表名的结果集
            ResultSet rs = metaData.getTables(databaseName, userName, tableNamePattern, new String[]{"TABLE"});
            while (rs.next()) {
                String tableName = rs.getString("TABLE_NAME");
                tableNames.add(tableName);
            }
            return tableNames;
        }
    }

    @SneakyThrows
    public static List<Map<String, Column>> columns(GaarasonDataSource gaarasonDataSource, String databaseName, String userName,
        @Nullable String tableNamePattern) {
        List<Map<String, Column>> columns = new ArrayList<>();
        try (Connection connection = gaarasonDataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();

            //获取表名的结果集
            ResultSet rs = metaData.getColumns(databaseName, userName, tableNamePattern, null);
            while (rs.next()) {

                ResultSetMetaData resultSetMetaData = rs.getMetaData();

                Map<String, Column> stringColumnMap = RecordFactory.JDBCResultToMap(resultSetMetaData, rs);

//                Map<String, Object> info = new HashMap<>();
//                for (String label : FinalVariable.metaDataLabel) {
//                    Object object;
//                    if("DATA_TYPE".equals(label)){
//                        SQLStructDataType.Field object1 = (SQLStructDataType.Field) rs.getObject(label);
//                        object  = object1;
//                    }else{
//                        object = rs.getObject(label);
//                    }
//                    info.put(label, object);
//                }
                columns.add(stringColumnMap);
            }
            return columns;
        }
    }
}
