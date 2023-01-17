package gaarason.database.query;

import gaarason.database.appointment.EntityUseType;
import gaarason.database.appointment.Paginate;
import gaarason.database.contract.eloquent.Builder;
import gaarason.database.contract.eloquent.Record;
import gaarason.database.contract.eloquent.RecordList;
import gaarason.database.contract.function.ChunkFunctionalInterface;
import gaarason.database.exception.CloneNotSupportedRuntimeException;
import gaarason.database.exception.EntityNotFoundException;
import gaarason.database.exception.InsertNotSuccessException;
import gaarason.database.exception.SQLRuntimeException;
import gaarason.database.lang.Nullable;
import gaarason.database.util.FormatUtils;
import gaarason.database.util.MapUtils;
import gaarason.database.util.ObjectUtils;

import java.util.*;

/**
 * 中间查询构造器
 * @param <T>
 * @param <K>
 * @author xt
 */
public abstract class ExecuteLevel3Builder<T, K> extends ExecuteLevel2Builder<T, K> {

    @Override
    public Record<T, K> find(@Nullable Object id) throws SQLRuntimeException {
        return where(model.getPrimaryKeyColumnName(), id).first();
    }

    @Override
    public Record<T, K> findOrFail(@Nullable Object id) throws EntityNotFoundException, SQLRuntimeException {
        return where(model.getPrimaryKeyColumnName(), id).firstOrFail();
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
    public int insert(@Nullable Object anyEntity) throws SQLRuntimeException {
        Map<String, Object> simpleMap = modelShadowProvider.entityToMap(anyEntity, EntityUseType.INSERT);
        return insertMapStyle(simpleMap);
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
    public int insert(List<?> entityList) throws SQLRuntimeException {
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
    public K insertGetId(Object anyEntity) throws SQLRuntimeException {
        Map<String, Object> simpleMap = modelShadowProvider.entityToMap(anyEntity, EntityUseType.INSERT);
        // 返回主键
        return insertGetIdMapStyle(simpleMap);
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
    public K insertGetIdOrFail(Object anyEntity) throws SQLRuntimeException, InsertNotSuccessException {
        K id = insertGetId(anyEntity);
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
    public List<K> insertGetIds(List<?> anyEntityList) throws SQLRuntimeException {
        // entityList处理
        beforeBatchInsert(anyEntityList);
        return insertGetIds();
    }

    @Override
    public List<K> insertGetIdsMapStyle(List<Map<String, Object>> entityMapList) throws SQLRuntimeException {
        // entityList处理
        beforeBatchInsertMapStyle(entityMapList);
        return insertGetIds();
    }

    @Override
    public int update(Object anyEntity) throws SQLRuntimeException {
        // 获取entity所有有效字段对其值得映射
        Map<String, Object> stringStringMap = modelShadowProvider.entityToMap(anyEntity,
            EntityUseType.UPDATE);

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


    @Nullable
    @Override
    public Record<T, K> query(String sql, Object... parameters) throws SQLRuntimeException {
        return query(sql, Arrays.asList(parameters));
    }

    @Override
    public Record<T, K> queryOrFail(String sql, Object... parameters)
        throws SQLRuntimeException, EntityNotFoundException {
        return queryOrFail(sql, Arrays.asList(parameters));
    }

    @Override
    public RecordList<T, K> queryList(String sql, Object... parameters) throws SQLRuntimeException {
        return queryList(sql, Arrays.asList(parameters));
    }

    @Override
    public void dealChunk(int num, ChunkFunctionalInterface<T, K> chunkFunctionalInterface) throws SQLRuntimeException {
        int offset = 0;
        boolean flag;
        do {
            Builder<T, K> cloneBuilder = clone();
            cloneBuilder.limit(offset, num);
            RecordList<T, K> records = cloneBuilder.get();
            flag = !records.isEmpty() && chunkFunctionalInterface.execute(records) && (records.size() == num);
            offset += num;
        } while (flag);
    }

    @Override
    public void dealChunk(int num, String column, ChunkFunctionalInterface<T, K> chunkFunctionalInterface)
        throws SQLRuntimeException {
        boolean flag;
        Object columnValue = null;
        do {
            Builder<T, K> cloneBuilder = clone();
            cloneBuilder.whereIgnoreNull(column, ">", columnValue)
                .firstOrderBy(builder -> builder.orderBy(column))
                .limit(num);
            RecordList<T, K> records = cloneBuilder.get();
            if (!records.isEmpty()) {
                columnValue = records.last().getMetadataMap().get(column);
            }
            flag = !records.isEmpty() && chunkFunctionalInterface.execute(records) && (records.size() == num);
        } while (flag);
    }

    @Override
    public int execute(String sql, Object... parameters) throws SQLRuntimeException {
        return execute(sql, Arrays.asList(parameters));
    }

    @Override
    public List<K> executeGetIds(String sql, Object... parameters) throws SQLRuntimeException {
        return executeGetIds(sql, Arrays.asList(parameters));
    }

    @Override
    @Nullable
    public K executeGetId(String sql, Object... parameters) throws SQLRuntimeException {
        return executeGetId(sql, Arrays.asList(parameters));
    }


    /**
     * 带总数的分页
     * @param currentPage 当前页
     * @param perPage 每页数量
     * @return 分页对象
     * @throws SQLRuntimeException 数据库异常
     * @throws CloneNotSupportedRuntimeException 克隆异常
     */
    @Override
    public Paginate<T> paginate(int currentPage, int perPage)
        throws SQLRuntimeException, CloneNotSupportedRuntimeException {
        Long count = clone().count("*");
        List<T> list = limit((currentPage - 1) * perPage, perPage).get().toObjectList();
        return new Paginate<>(list, currentPage, perPage, count.intValue());
    }

    /**
     * 带总数的分页
     * @param currentPage 当前页
     * @param perPage 每页数量
     * @return 分页对象
     * @throws SQLRuntimeException 数据库异常
     * @throws CloneNotSupportedRuntimeException 克隆异常
     */
    @Override
    public Paginate<Map<String, Object>> paginateMapStyle(int currentPage, int perPage)
        throws SQLRuntimeException, CloneNotSupportedRuntimeException {
        Long count = clone().count("*");
        List<Map<String, Object>> list = limit((currentPage - 1) * perPage, perPage).get().toMapList();
        return new Paginate<>(list, currentPage, perPage, count.intValue());
    }

    /**
     * 不带总数的分页
     * @param currentPage 当前页
     * @param perPage 每页数量
     * @return 分页对象
     * @throws SQLRuntimeException 数据库异常
     */
    @Override
    public Paginate<T> simplePaginate(int currentPage, int perPage) throws SQLRuntimeException {
        List<T> list = limit((currentPage - 1) * perPage, perPage).get().toObjectList();
        return new Paginate<>(list, currentPage, perPage);
    }

    /**
     * 不带总数的分页
     * @param currentPage 当前页
     * @param perPage 每页数量
     * @return 分页对象
     * @throws SQLRuntimeException 数据库异常
     */
    @Override
    public Paginate<Map<String, Object>> simplePaginateMapStyle(int currentPage, int perPage)
        throws SQLRuntimeException {
        List<Map<String, Object>> list = limit((currentPage - 1) * perPage, perPage).get().toMapList();
        return new Paginate<>(list, currentPage, perPage);
    }

    /**
     * 批量插入数据, entityList处理
     * @param entityList 数据实体对象列表
     */
    protected void beforeBatchInsert(List<?> entityList) {
        List<Map<String, Object>> mapList = new LinkedList<>();
        for (Object entity : entityList) {
            mapList.add(modelShadowProvider.entityToMap(entity, EntityUseType.INSERT));
        }
        // 转入mapList处理
        beforeBatchInsertMapStyle(mapList);
    }

    /**
     * 批量插入数据, entityMapList处理
     * @param entityMapList 数据实体map列表
     */
    protected void beforeBatchInsertMapStyle(List<Map<String, Object>> entityMapList) {
        if (ObjectUtils.isEmpty(entityMapList)) {
            return;
        }
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
    protected static String function(String functionName, String parameter, @Nullable String alias) {
        return functionName + FormatUtils.bracket(parameter) +
            (alias == null ? "" : " as " + FormatUtils.quotes(alias));
    }

    /**
     * 给字段加上引号
     * @param something 字段 eg: sum(order.amount) AS sum_price
     * @return eg: sum(`order`.`amount`) AS `sum_price`
     */
    protected abstract String backQuote(String something);
}
