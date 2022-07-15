package gaarason.database.query;

import gaarason.database.appointment.EntityUseType;
import gaarason.database.appointment.SqlType;
import gaarason.database.contract.connection.GaarasonDataSource;
import gaarason.database.contract.eloquent.Model;
import gaarason.database.contract.eloquent.Record;
import gaarason.database.contract.eloquent.RecordList;
import gaarason.database.contract.query.Grammar;
import gaarason.database.exception.EntityNotFoundException;
import gaarason.database.exception.InsertNotSuccessException;
import gaarason.database.exception.SQLRuntimeException;
import gaarason.database.lang.Nullable;
import gaarason.database.util.FormatUtils;
import gaarason.database.util.MapUtils;

import java.util.*;

/**
 * 中间查询构造器
 * @param <T>
 * @param <K>
 * @author xt
 */
public abstract class MiddleBuilder<T, K> extends BaseBuilder<T, K> {

    protected MiddleBuilder(GaarasonDataSource gaarasonDataSource, Model<T, K> model, Grammar grammar) {
        super(gaarasonDataSource, model, grammar);
    }


    @Override
    public Record<T, K> find(K id) throws SQLRuntimeException {
        return where(model.getPrimaryKeyColumnName(), id.toString()).first();
    }

    @Override
    public Record<T, K> findOrFail(K id) throws EntityNotFoundException, SQLRuntimeException {
        return where(model.getPrimaryKeyColumnName(), id.toString()).firstOrFail();
    }

    @Override
    public Record<T, K> first() throws SQLRuntimeException {
        try {
            return firstOrFail();
        } catch (EntityNotFoundException e) {
            return null;
        }
    }

    @Override
    public Record<T, K> firstOrFail() throws SQLRuntimeException, EntityNotFoundException {
        limit(1);
        return querySql();
    }

    @Override
    public RecordList<T, K> get() throws SQLRuntimeException {
        return querySqlList();
    }

    @Override
    public int insert() throws SQLRuntimeException {
        return updateSql(SqlType.INSERT);
    }

    @Override
    public int insert(T entity) throws SQLRuntimeException {
        // 获取entity所有有效sql字段
        Set<String> columnNameSet = modelShadowProvider.columnNameSet(entity, EntityUseType.INSERT);
        // 获取entity所有有效字段的值
        List<Object> valueList = modelShadowProvider.valueList(entity, columnNameSet);
        // 字段加入grammar
        column(columnNameSet);
        // 字段的值加入grammar
        value(valueList);
        // 执行
        return insert();
    }

    @Override
    public int insertMapStyle(Map<String, Object> entityMap) throws SQLRuntimeException {
        // 获取map所有有效sql字段
        Set<String> columnNameSet = new LinkedHashSet<>(entityMap.keySet());
        // 获取map所有有效字段的值
        List<Object> valueList = MapUtils.mapValueToList(entityMap);
        // 字段加入grammar
        column(columnNameSet);
        // 字段的值加入grammar
        value(valueList);
        // 执行
        return insert();
    }

    @Override
    public int insert(List<T> entityList) throws SQLRuntimeException {
        // entityList处理
        beforeBatchInsert(entityList);
        // 执行
        return insert();
    }

    @Override
    public int insertMapStyle(List<Map<String, Object>> entityMapList) throws SQLRuntimeException {
        // entityList处理
        beforeBatchInsertMapStyle(entityMapList);
        // 执行
        return insert();
    }

    @Override
    public K insertGetId() throws SQLRuntimeException {
        Grammar.SQLPartInfo sqlPartInfo = grammar.generateSql(SqlType.INSERT);
        assert sqlPartInfo.getParameters() != null;
        return executeGetId(sqlPartInfo.getSqlString(), sqlPartInfo.getParameters());
    }

    @Override
    public K insertGetId(T entity) throws SQLRuntimeException {
        // 获取entity所有有效sql字段
        Set<String> columnNameSet = modelShadowProvider.columnNameSet(entity, EntityUseType.INSERT);
        // 获取entity所有有效字段的值
        List<Object> valueList = modelShadowProvider.valueList(entity, columnNameSet);
        // 字段加入grammar
        column(columnNameSet);
        // 字段的值加入grammar
        value(valueList);
        // 执行, 并获取主键id
        K primaryId = insertGetId();
        // 赋值主键
        modelShadowProvider.setPrimaryKeyValue(entity, primaryId);
        // 返回主键
        return primaryId;
    }

    @Override
    public K insertGetIdMapStyle(Map<String, Object> entityMap) throws SQLRuntimeException {
        // 获取map所有有效sql字段
        Set<String> columnNameSet = new LinkedHashSet<>(entityMap.keySet());
        // 获取map所有有效字段的值
        List<Object> valueList = MapUtils.mapValueToList(entityMap);
        // 字段加入grammar
        column(columnNameSet);
        // 字段的值加入grammar
        value(valueList);
        // 执行, 并获取主键id，返回主键
        return insertGetId();
    }

    @Override
    public K insertGetIdOrFail() throws SQLRuntimeException, InsertNotSuccessException {
        K id = insertGetId();
        if (id == null) {
            throw new InsertNotSuccessException();
        }
        return id;
    }

    @Override
    public K insertGetIdOrFail(T entity) throws SQLRuntimeException, InsertNotSuccessException {
        K id = insertGetId(entity);
        if (id == null) {
            throw new InsertNotSuccessException();
        }
        return id;
    }

    @Override
    public K insertGetIdOrFailMapStyle(Map<String, Object> entityMap)
        throws SQLRuntimeException, InsertNotSuccessException {
        K id = insertGetIdMapStyle(entityMap);
        if (id == null) {
            throw new InsertNotSuccessException();
        }
        return id;
    }

    @Override
    public List<K> insertGetIds() throws SQLRuntimeException {
        Grammar.SQLPartInfo sqlPartInfo = grammar.generateSql(SqlType.INSERT);
        assert sqlPartInfo.getParameters() != null;
        return executeGetIds(sqlPartInfo.getSqlString(), sqlPartInfo.getParameters());
    }

    @Override
    public List<K> insertGetIds(List<T> entityList) throws SQLRuntimeException {
        // entityList处理
        beforeBatchInsert(entityList);
        return insertGetIds();
    }

    @Override
    public List<K> insertGetIdsMapStyle(List<Map<String, Object>> entityMapList) throws SQLRuntimeException {
        // entityList处理
        beforeBatchInsertMapStyle(entityMapList);
        return insertGetIds();
    }

    @Override
    public int update() throws SQLRuntimeException {
        return updateSql(SqlType.UPDATE);
    }

    @Override
    public int update(T entity) throws SQLRuntimeException {
        // 获取entity所有有效字段对其值得映射
        Map<String, Object> stringStringMap = modelShadowProvider.columnValueMap(entity, EntityUseType.UPDATE);

        data(stringStringMap);
        // 执行
        return update();
    }

    @Override
    public int updateMapStyle(Map<String, Object> entityMap) throws SQLRuntimeException {
        data(entityMap);
        // 执行
        return update();
    }

    /**
     * 批量插入数据, entityList处理
     * @param entityList 数据实体对象列表
     */
    protected void beforeBatchInsert(List<T> entityList) {
        // 获取entity所有有效字段
        Set<String> columnNameSet = modelShadowProvider.columnNameSet(entityList.get(0), EntityUseType.INSERT);
        List<List<Object>> valueListList = new ArrayList<>();
        for (T entity : entityList) {
            // 获取entity所有有效字段的值
            List<Object> valueList = modelShadowProvider.valueList(entity, columnNameSet);
            valueListList.add(valueList);
        }
        // 字段加入grammar
        column(columnNameSet);
        // 字段的值加入grammar
        valueList(valueListList);
    }

    /**
     * 批量插入数据, entityMapList处理
     * @param entityMapList 数据实体map列表
     */
    protected void beforeBatchInsertMapStyle(List<Map<String, Object>> entityMapList) {
        // 获取entity所有有效字段
        Set<String> columnNameSet = new LinkedHashSet<>(entityMapList.get(0).keySet());
        List<List<Object>> valueListList = new ArrayList<>();
        for (Map<String, Object> map : entityMapList) {
            List<Object> valueList = MapUtils.mapValueToList(map);
            valueListList.add(valueList);
        }
        // 字段加入grammar
        column(columnNameSet);
        // 字段的值加入grammar
        valueList(valueListList);
    }

    /**
     * 返回数据库方法的拼接字符
     * @param functionName 方法名
     * @param parameter 参数字符串
     * @param alias 别名
     * @return 拼接后的字符
     */
    protected String function(String functionName, String parameter, @Nullable String alias) {
        return functionName + FormatUtils.bracket(parameter) + (alias == null ? "" :
            " as " + FormatUtils.quotes(alias));
    }

    /**
     * 给字段加上引号
     * @param something 字段 eg: sum(order.amount) AS sum_price
     * @return eg: sum(`order`.`amount`) AS `sum_price`
     */
    protected abstract String backQuote(String something);
}
