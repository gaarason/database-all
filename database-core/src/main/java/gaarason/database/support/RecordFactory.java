package gaarason.database.support;

import gaarason.database.config.ConversionConfig;
import gaarason.database.contract.eloquent.Model;
import gaarason.database.contract.eloquent.Record;
import gaarason.database.contract.eloquent.RecordList;
import gaarason.database.core.Container;
import gaarason.database.eloquent.RecordBean;
import gaarason.database.eloquent.RecordListBean;
import gaarason.database.exception.EntityNotFoundException;
import gaarason.database.provider.ModelShadowProvider;
import gaarason.database.util.ObjectUtils;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 结果集生成
 * @author xt
 */
public class RecordFactory {

    private RecordFactory() {

    }

    /**
     * 单个结果集(来源 : 数据库查询结果)
     * @param model Model
     * @param resultSet jdbc结果
     * @param sql 执行的sql
     * @param <T> 实体类型
     * @param <K> 实体主键类型
     * @return 批量结果集(全新)
     * @throws SQLException 数据库异常
     * @throws EntityNotFoundException 数据为空
     */
    public static <T, K> Record<T, K> newRecord(Model<T, K> model,
        ResultSet resultSet, String sql) throws SQLException, EntityNotFoundException {
        if (!resultSet.next()) {
            throw new EntityNotFoundException(sql);
        }
        final ResultSetMetaData resultSetMetaData = resultSet.getMetaData();

        Map<String, Object> columnValueMap = JDBCResultToMap(model, resultSetMetaData, resultSet);
        return new RecordBean<>(model, columnValueMap, sql);
    }

    /**
     * 批量结果集(来源 : 数据库查询结果)
     * @param model Model
     * @param resultSet jdbc结果
     * @param sql 执行的sql
     * @param <T> 实体类型
     * @param <K> 实体主键类型
     * @return 批量结果集(全新)
     * @throws SQLException 数据库异常
     */
    public static <T, K> RecordList<T, K> newRecordList(Model<T, K> model,
        ResultSet resultSet, String sql) throws SQLException {

        RecordList<T, K> recordList = new RecordListBean<>(sql, model.getGaarasonDataSource().getContainer());
        // 总的数据源
        final ResultSetMetaData resultSetMetaData = resultSet.getMetaData();

        while (resultSet.next()) {
            // 拆分的数据源
            Map<String, Object> columnValueMap = JDBCResultToMap(model, resultSetMetaData, resultSet);
            recordList.add(new RecordBean<>(model, columnValueMap, sql));
        }

        return recordList;
    }

    /**
     * 单体结果集列表,转化为 批量结果集
     * 仅 ToObject 构造方法中使用
     * 保持 record 对象地址一致
     * @param container 容器
     * @param records 单体结果列表
     * @param <T> 实体类型
     * @param <K> 实体主键类型
     * @return 批量结果集(RecordList全新, Record为引用地址)
     */
    public static <T, K> RecordList<T, K> newRecordList(Container container,
        List<Record<T, K>> records) {
        String sql = !records.isEmpty() ? records.get(0).getOriginalSql() : "";
        RecordList<T, K> recordList = new RecordListBean<>(sql, container);
        // 此处不应使用, deepCopyRecord
        recordList.addAll(records);
        return recordList;
    }

    /**
     * 单体结果集列表,转化为 批量结果集
     * 仅 ToObject 构造方法中使用
     * 保持 theRecord 对象地址一致
     * @param theRecord 单体结果集
     * @param <T> 实体类型
     * @param <K> 实体主键类型
     * @return 批量结果集(RecordList全新, Record为引用地址)
     */
    public static <T, K> RecordList<T, K> newRecordList(
        Record<T, K> theRecord) {
        List<Record<T, K>> records = new ArrayList<>();
        records.add(theRecord);
        return newRecordList(theRecord.getModel().getGaarasonDataSource().getContainer(), records);
    }

    /**
     * 空的批量结果集
     * @param container 容器
     * @param <T> 实体类型
     * @param <K> 实体主键类型
     * @return 批量结果集(RecordList全新, Record为引用地址)
     */
    public static <T, K> RecordList<T, K> newRecordList(Container container) {
        return new RecordListBean<>("", container);
    }

    /**
     * copy产生新的recordList
     * @param originalRecordList 原结果集
     * @param <T> 实体类型
     * @param <K> 实体主键类型
     * @return 批量结果集
     */
    public static <T, K> RecordList<T, K> copyRecordList(
        RecordList<T, K> originalRecordList) {
        RecordList<T, K> recordList = new RecordListBean<>(originalRecordList.getOriginalSql(),
            originalRecordList.getContainer());
        for (Record<T, K> originalRecord : originalRecordList) {
            Model<T, K> model = originalRecord.getModel();
            Map<String, Object> metadataMap = copy(originalRecord.getMetadataMap());
            String originalSql = originalRecord.getOriginalSql();
            recordList.add(new RecordBean<>(model, metadataMap, originalSql));
        }
        // 使用引用
        recordList.setCacheMap(copy(originalRecordList.getCacheMap()));
        return recordList;
    }

    /**
     * jdbc结果集转化为 HashMap
     * @param model 数据模型
     * @param resultSetMetaData 源数据
     * @param resultSet 结果集
     * @return 通用map
     * @throws SQLException 数据库异常
     */
    public static <T, K> HashMap<String, Object> JDBCResultToMap(
        Model<T, K> model, ResultSetMetaData resultSetMetaData, ResultSet resultSet) throws SQLException {
        HashMap<String, Object> map = new HashMap<>();
        return (HashMap<String, Object>) JDBCResultToMap(model, map, resultSetMetaData, resultSet);

    }

    /**
     * jdbc结果集转化为通用map
     * @param model 数据模型
     * @param map map类型
     * @param resultSetMetaData 源数据
     * @param resultSet 结果集
     * @return 通用map
     * @throws SQLException 数据库异常
     */
    protected static <T, K> Map<String, Object> JDBCResultToMap(
        Model<T, K> model, Map<String, Object> map, ResultSetMetaData resultSetMetaData, ResultSet resultSet)
        throws SQLException {

        // 字段信息
        Map<String, FieldMember<?>> columnFieldMap = model.getContainer()
            .getBean(ModelShadowProvider.class)
            .get(model)
            .getEntityMember().getColumnFieldMap();

        final int columnCountMoreOne = resultSetMetaData.getColumnCount() + 1;
        for (int i = 1; i < columnCountMoreOne; i++) {
            // 列名
            String columnName = resultSetMetaData.getColumnLabel(i);

            FieldMember<?> fieldMember = columnFieldMap.get(columnName);

            Object value;
            // 值获取
            if (!ObjectUtils.isNull(fieldMember)) {
                value = fieldMember.getFieldConversion()
                    .acquisition(fieldMember.getField(), resultSet, columnName);

            } else {
                // *尽量* 使用同类型赋值
                value = model.getContainer()
                    .getBean(ConversionConfig.class)
                    .getValueFromJdbcResultSet(null, resultSet, columnName);
            }

            map.put(columnName, value);
        }
        return map;
    }

    /**
     * 通过序列化对普通对象进行递归copy
     * @param original 源对象
     * @param <T> 对象所属的类
     * @return 全新的对象
     */
    private static <T> T copy(T original) {
        // 未使用拷贝, 因为 MetaData 不会有改动, 所以使用引用性能会较好
//        return ObjectUtil.deepCopy(original);
        return original;
    }
}
