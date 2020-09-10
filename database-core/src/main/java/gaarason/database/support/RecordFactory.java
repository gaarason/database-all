package gaarason.database.support;

import gaarason.database.eloquent.Model;
import gaarason.database.eloquent.Record;
import gaarason.database.eloquent.RecordList;
import gaarason.database.exception.EntityNotFoundException;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 结果集生成
 */
public class RecordFactory {

    /**
     * 单个结果集(来源 : 数据库查询结果)
     * @param entityClass 实体类型
     * @param model       Model
     * @param resultSet   jdbc结果
     * @param sql         执行的sql
     * @param <T>         实体类型
     * @param <K>         实体主键类型
     * @return 批量结果集(全新)
     * @throws SQLException            数据库异常
     * @throws EntityNotFoundException 数据为空
     */
    public static <T, K> Record<T, K> newRecord(Class<T> entityClass, Model<T, K> model, ResultSet resultSet,
                                                String sql)
        throws SQLException, EntityNotFoundException {
        if (!resultSet.next()) {
            throw new EntityNotFoundException(sql);
        }
        final ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
        Map<String, Column>     stringColumnMap   = JDBCResultToMap(resultSetMetaData, resultSet);
        return new Record<>(entityClass, model, stringColumnMap, sql);
    }

    /**
     * 批量结果集(来源 : 数据库查询结果)
     * @param entityClass 实体类型
     * @param model       Model
     * @param resultSet   jdbc结果
     * @param sql         执行的sql
     * @param <T>         实体类型
     * @param <K>         实体主键类型
     * @return 批量结果集(全新)
     * @throws SQLException 数据库异常
     */
    public static <T, K> RecordList<T, K> newRecordList(Class<T> entityClass, Model<T, K> model, ResultSet resultSet,
                                                        String sql)
        throws SQLException {
        RecordList<T, K> recordList = new RecordList<>();
        // 总的数据源
        final ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
        while (resultSet.next()) {
            // 拆分的数据源
            Map<String, Column> stringColumnMap = JDBCResultToMap(resultSetMetaData, resultSet);
            recordList.add(new Record<>(entityClass, model, stringColumnMap, sql));
            recordList.getOriginalMetadataMapList().add(stringColumnMap);
        }
        // 设置原始sql
        recordList.setOriginalSql(sql);
        return recordList;
    }

    /**
     * 单体结果集列表,转化为 批量结果集
     * 仅 ToObject 构造方法中使用
     * 保持 record 对象地址一致
     * @param records 单体结果列表
     * @param <T>     实体类型
     * @param <K>     实体主键类型
     * @return 批量结果集(RecordList全新, Record为引用地址)
     */
    public static <T, K> RecordList<T, K> newRecordList(List<Record<T, K>> records) {
        RecordList<T, K> recordList = new RecordList<>();
        String sql = records.size() > 0 ? records.get(0).getOriginalSql() : "";
        for (Record<T, K> record : records) {
            // 此处不应使用, deepCopyRecord
            recordList.add(record);
            recordList.getOriginalMetadataMapList().add(copy(record.getMetadataMap()));
        }
        recordList.setOriginalSql(sql);
        return recordList;
    }

    /**
     * copy产生新的record, 其中model使用原record中的引用
     * @param originalRecord 原结果集
     * @param <T>            实体类型
     * @param <K>            实体主键类型
     * @return 单个结果集
     */
    public static <T, K> Record<T, K> copyRecord(Record<T, K> originalRecord) {
        Model<T, K>         model       = originalRecord.getModel();
        Class<T>            entityClass = model.getEntityClass();
        Map<String, Column> metadataMap = copy(originalRecord.getMetadataMap());
        String              originalSql = originalRecord.getOriginalSql();
        return new Record<>(entityClass, model, metadataMap, originalSql);
    }

    /**
     * copy产生新的recordList
     * @param originalRecordList 原结果集
     * @param <T>                实体类型
     * @param <K>                实体主键类型
     * @return 批量结果集
     */
    public static <T, K> RecordList<T, K> copyRecordList(RecordList<T, K> originalRecordList) {
        RecordList<T, K> recordList = new RecordList<>();
        for (Record<T, K> originalRecord : originalRecordList) {
            Model<T, K>         model       = originalRecord.getModel();
            Class<T>            entityClass = model.getEntityClass();
            Map<String, Column> metadataMap = copy(originalRecord.getMetadataMap());
            String              originalSql = originalRecord.getOriginalSql();
            recordList.add(new Record<>(entityClass, model, metadataMap, originalSql));
        }
        // 使用引用
        recordList.getOriginalMetadataMapList().addAll(copy(originalRecordList.getOriginalMetadataMapList()));
        recordList.setOriginalSql(originalRecordList.getOriginalSql());
        recordList.setCacheMap(copy(originalRecordList.getCacheMap()));
        return recordList;
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

    /**
     * 通过序列化对普通对象进行递归copy
     * @param original 源对象
     * @param <T>      对象所属的类
     * @return 全新的对象
     */
    private static <T> T copy(T original) {
        // 未使用拷贝, 因为 MetaData 不会有改动, 所以使用引用性能会较好
//        return ObjectUtil.deepCopy(original);
        return original;
    }
}
