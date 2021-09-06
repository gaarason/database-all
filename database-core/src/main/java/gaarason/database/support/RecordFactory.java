package gaarason.database.support;

import gaarason.database.contract.eloquent.Model;
import gaarason.database.contract.eloquent.Record;
import gaarason.database.contract.eloquent.RecordList;
import gaarason.database.eloquent.RecordBean;
import gaarason.database.eloquent.RecordListBean;
import gaarason.database.exception.EntityNotFoundException;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 结果集生成
 * @author xt
 */
public class RecordFactory {

    private RecordFactory(){

    }

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
    public static <T extends Serializable, K extends Serializable> Record<T, K> newRecord(Class<T> entityClass, Model<T, K> model, ResultSet resultSet,
                                                String sql)
        throws SQLException, EntityNotFoundException {
        if (!resultSet.next()) {
            throw new EntityNotFoundException(sql);
        }
        final ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
        Map<String, Column> stringColumnMap = JDBCResultToMap(resultSetMetaData, resultSet);
        return new RecordBean<>(entityClass, model, stringColumnMap, sql);
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
    public static <T extends Serializable, K extends Serializable> RecordList<T, K> newRecordList(Class<T> entityClass, Model<T, K> model, ResultSet resultSet,
                                                        String sql) throws SQLException {
        RecordList<T, K> recordList = new RecordListBean<>(sql);
        // 总的数据源
        final ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
        while (resultSet.next()) {
            // 拆分的数据源
            Map<String, Column> stringColumnMap = JDBCResultToMap(resultSetMetaData, resultSet);
            recordList.add(new RecordBean<>(entityClass, model, stringColumnMap, sql));
            recordList.getOriginalMetadataMapList().add(stringColumnMap);
        }
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
    public static <T extends Serializable, K extends Serializable> RecordList<T, K> newRecordList(List<Record<T, K>> records) {
        String sql = records.size() > 0 ? records.get(0).getOriginalSql() : "";
        RecordList<T, K> recordList = new RecordListBean<>(sql);
        for (Record<T, K> record : records) {
            // 此处不应使用, deepCopyRecord
            recordList.add(record);
            recordList.getOriginalMetadataMapList().add(copy(record.getMetadataMap()));
        }
        return recordList;
    }

    /**
     * 单体结果集列表,转化为 批量结果集
     * 仅 ToObject 构造方法中使用
     * 保持 record 对象地址一致
     * @param record 单体结果集
     * @param <T>    实体类型
     * @param <K>    实体主键类型
     * @return 批量结果集(RecordList全新, Record为引用地址)
     */
    public static <T extends Serializable, K extends Serializable> RecordList<T, K> newRecordList(Record<T, K> record) {
        List<Record<T, K>> records = new ArrayList<>();
        records.add(record);
        return newRecordList(records);
    }

    /**
     * 空的批量结果集
     * @param <T> 实体类型
     * @param <K> 实体主键类型
     * @return 批量结果集(RecordList全新, Record为引用地址)
     */
    public static <T extends Serializable, K extends Serializable> RecordList<T, K> newRecordList() {
        return new RecordListBean<>("");
    }

    /**
     * copy产生新的record, 其中model使用原record中的引用
     * @param originalRecord 原结果集
     * @param <T>            实体类型
     * @param <K>            实体主键类型
     * @return 单个结果集
     */
    public static <T extends Serializable, K extends Serializable> Record<T, K> copyRecord(Record<T, K> originalRecord) {
        Model<T, K> model = originalRecord.getModel();
        Class<T> entityClass = model.getEntityClass();
        Map<String, Column> metadataMap = copy(originalRecord.getMetadataMap());
        String originalSql = originalRecord.getOriginalSql();
        return new RecordBean<>(entityClass, model, metadataMap, originalSql);
    }

    /**
     * copy产生新的recordList
     * @param originalRecordList 原结果集
     * @param <T>                实体类型
     * @param <K>                实体主键类型
     * @return 批量结果集
     */
    public static <T extends Serializable, K extends Serializable> RecordList<T, K> copyRecordList(RecordList<T, K> originalRecordList) {
        RecordList<T, K> recordList = new RecordListBean<>(originalRecordList.getOriginalSql());
        for (Record<T, K> originalRecord : originalRecordList) {
            Model<T, K> model = originalRecord.getModel();
            Class<T> entityClass = model.getEntityClass();
            Map<String, Column> metadataMap = copy(originalRecord.getMetadataMap());
            String originalSql = originalRecord.getOriginalSql();
            recordList.add(new RecordBean<>(entityClass, model, metadataMap, originalSql));
        }
        // 使用引用
        recordList.getOriginalMetadataMapList().addAll(copy(originalRecordList.getOriginalMetadataMapList()));
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
    public static HashMap<String, Column> JDBCResultToMap(ResultSetMetaData resultSetMetaData, ResultSet resultSet)
        throws SQLException {
        HashMap<String, Column> map = new HashMap<>();
        return (HashMap<String, Column>) JDBCResultToMap(map, resultSetMetaData, resultSet);

    }

    /**
     * jdbc结果集转化为通用map
     * @param resultSetMetaData 源数据
     * @param resultSet         结果集
     * @return 通用map
     * @throws SQLException 数据库异常
     */
    public static ConcurrentHashMap<String, Column> JDBCResultToConcurrentHashMap(ResultSetMetaData resultSetMetaData, ResultSet resultSet)
        throws SQLException {
        ConcurrentHashMap<String, Column> map = new ConcurrentHashMap<>();
        return (ConcurrentHashMap<String, Column>) JDBCResultToMap(map, resultSetMetaData, resultSet);
    }

    /**
     * jdbc结果集转化为通用map
     * @param map               map类型
     * @param resultSetMetaData 源数据
     * @param resultSet         结果集
     * @return 通用map
     * @throws SQLException 数据库异常
     */
    protected static Map<String, Column> JDBCResultToMap(Map<String, Column> map, ResultSetMetaData resultSetMetaData,
                                                         ResultSet resultSet) throws SQLException {
        final int columnCountMoreOne = resultSetMetaData.getColumnCount() + 1;
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
