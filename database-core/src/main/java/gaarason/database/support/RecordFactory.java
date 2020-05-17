package gaarason.database.support;

import gaarason.database.core.lang.Nullable;
import gaarason.database.eloquent.Model;
import gaarason.database.eloquent.Record;
import gaarason.database.eloquent.RecordList;
import gaarason.database.exception.EntityNotFoundException;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.*;

/**
 * 结果集生成
 */
public class RecordFactory {

    /**
     * 批量结果集
     * @param entityClass 实体类型
     * @param model       Model
     * @param resultSet   jdbc结果
     * @param <T>         实体类型
     * @param <K>         实体主键类型
     * @return 批量结果集
     * @throws SQLException 数据库异常
     */
    public static <T, K> RecordList<T, K> newRecordList(Class<T> entityClass, Model<T, K> model, ResultSet resultSet)
        throws SQLException {
        RecordList<T, K> recordList = new RecordList<>();
        // 总的数据源
        List<Map<String, Column>> metadataMapList   = new ArrayList<>();
        final ResultSetMetaData   resultSetMetaData = resultSet.getMetaData();
        while (resultSet.next()) {
            // 拆分的数据源
            Map<String, Column> stringColumnMap = JDBCResultToMap(resultSetMetaData, resultSet);
            metadataMapList.add(stringColumnMap);
            recordList.add(new Record<>(entityClass, model, stringColumnMap));
        }
        // 设置数据源
        recordList.setOriginalMetadataMapList(metadataMapList);
        return recordList;
    }

    /**
     * 筛选批量结果集
     * @param originalRecordList 原本批量结果集
     * @param column             字段
     * @param value              值
     * @param <T>                实体类型
     * @param <K>                实体主键类型
     * @return 批量结果集
     */
    public static <T, K> RecordList<T, K> filterRecordList(RecordList<T, K> originalRecordList, String column,
                                                           String value) {
        RecordList<T, K> recordList = new RecordList<>();
        for (Record<T, K> originalRecord : originalRecordList) {
            // 一对一关系
            String targetValue = originalRecord.getMetadataMap().get(column).getValue().toString();
            // 一对多关系
            Set<String> relationIds = originalRecord.getMetadataMap().get(column).getRelationIds();
            // 满足其一则加入
            if ((!relationIds.isEmpty() && relationIds.contains(value)) || value.equals(targetValue))
                recordList.add(originalRecord);
        }
        return recordList;
    }

    /**
     * 筛选单个结果集
     * @param originalRecordList 原本批量结果集
     * @param column             字段
     * @param value              值
     * @param <T>                实体类型
     * @param <K>                实体主键类型
     * @return 批量结果集
     */
    @Nullable
    public static <T, K> Record<T, K> filterRecord(RecordList<T, K> originalRecordList, String column,
                                                   String value) {
        for (Record<T, K> originalRecord : originalRecordList) {
            if (value.equals(originalRecord.getMetadataMap().get(column).getValue().toString())) {
                return originalRecord;
            }
        }
        return null;
    }

    /**
     * 单个结果集
     * @param entityClass 实体类型
     * @param model       Model
     * @param resultSet   jdbc结果
     * @param <T>         实体类型
     * @param <K>         实体主键类型
     * @return 批量结果集
     * @throws SQLException            数据库异常
     * @throws EntityNotFoundException 数据为空
     */
    public static <T, K> Record<T, K> newRecord(Class<T> entityClass, Model<T, K> model, ResultSet resultSet)
        throws SQLException, EntityNotFoundException {
        if (!resultSet.next()) {
            throw new EntityNotFoundException();
        }
        final ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
        Map<String, Column>     stringColumnMap   = JDBCResultToMap(resultSetMetaData, resultSet);
        return new Record<>(entityClass, model, stringColumnMap);
    }

    /**
     * jdbc结果集转化为通用map
     * @param resultSetMetaData 源数据
     * @param resultSet         结果集
     * @return 通用map
     * @throws SQLException 数据库异常
     */
    private static Map<String, Column> JDBCResultToMap(ResultSetMetaData resultSetMetaData, ResultSet resultSet)
        throws SQLException {
        Map<String, Column> map                = new HashMap<>();
        final int           columnCountMoreOne = resultSetMetaData.getColumnCount() + 1;
        for (int i = 1; i < columnCountMoreOne; i++) {
            Column column = new Column();
            column.setName(resultSetMetaData.getColumnLabel(i));
            column.setValue(resultSet.getObject(column.getName()));
            column.setType(resultSetMetaData.getColumnType(i));
            column.setTypeName(resultSetMetaData.getColumnTypeName(i));
            column.setCount(resultSetMetaData.getColumnCount());
            column.setCatalogName(resultSetMetaData.getCatalogName(i));
            column.setClassName(resultSetMetaData.getColumnClassName(i));
            column.setDisplaySize(resultSetMetaData.getColumnDisplaySize(i));
            column.setColumnName(resultSetMetaData.getColumnName(i));
            column.setSchemaName(resultSetMetaData.getSchemaName(i));
            column.setPrecision(resultSetMetaData.getPrecision(i));
            column.setScale(resultSetMetaData.getScale(i));
            column.setTableName(resultSetMetaData.getTableName(i));
            column.setAutoIncrement(resultSetMetaData.isAutoIncrement(i));
            column.setCaseSensitive(resultSetMetaData.isCaseSensitive(i));
            column.setSearchable(resultSetMetaData.isSearchable(i));
            column.setCurrency(resultSetMetaData.isCurrency(i));
            column.setNullable(resultSetMetaData.isNullable(i) == ResultSetMetaData.columnNullable);
            column.setSigned(resultSetMetaData.isSigned(i));
            column.setReadOnly(resultSetMetaData.isReadOnly(i));
            column.setWritable(resultSetMetaData.isWritable(i));
            column.setDefinitelyWritable(resultSetMetaData.isDefinitelyWritable(i));
            map.put(column.getName(), column);
        }
        return map;
    }
}
