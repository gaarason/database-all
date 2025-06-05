package gaarason.database.query;

import gaarason.database.appointment.EntityUseType;
import gaarason.database.appointment.Paginate;
import gaarason.database.contract.eloquent.Builder;
import gaarason.database.contract.eloquent.Record;
import gaarason.database.contract.eloquent.RecordList;
import gaarason.database.contract.function.ChunkFunctionalInterface;
import gaarason.database.contract.query.Grammar;
import gaarason.database.exception.*;
import gaarason.database.lang.Nullable;
import gaarason.database.support.EntityMember;
import gaarason.database.util.FormatUtils;
import gaarason.database.util.ObjectUtils;

import java.util.*;

/**
 * 中间查询构造器
 * @param <T>
 * @param <K>
 * @author xt
 */
abstract class ExecuteLevel3Builder<B extends Builder<B, T, K>, T, K>  extends ExecuteLevel2Builder<B, T, K> {

    @Override
    @Nullable
    public Record<T, K> find(@Nullable Object id) throws SQLRuntimeException {
        return where(model.getPrimaryKeyColumnName(), id).first();
    }

    @Override
    public Record<T, K> findOrFail(@Nullable Object id) throws EntityNotFoundException, SQLRuntimeException {
        return where(model.getPrimaryKeyColumnName(), id).firstOrFail();
    }

    @Override
    @Nullable
    public Record<T, K> first() throws SQLRuntimeException {
        try {
            return firstOrFail();
        } catch (EntityNotFoundException e) {
            return null;
        }
    }

    @Override
    public int upsert(String... columns) throws SQLRuntimeException {
        return upsert(Arrays.asList(columns));
    }

    @Override
    public K insertGetIdOrFail() throws SQLRuntimeException, InsertNotSuccessException {
        K id = insertGetId();
        if (id == null) {
            throw new InsertNotSuccessException();
        }
        return id;
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
            Builder<B, T, K> cloneBuilder = clone();
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
            Builder<B, T, K> cloneBuilder = clone();
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
     * @param firstElement 数据实体对象列表 中的第一个元素
     * @param entityCollection 数据实体对象列表
     */
    protected B beforeBatchInsertEntityStyle(Object firstElement, Collection<?> entityCollection) {
        List<Map<String, Object>> mapList = new LinkedList<>();
        EntityMember<Object, Object> entityMember = modelShadowProvider.parseAnyEntityWithCache(firstElement);
        for (Object entity : entityCollection) {
            // 根据实体上的注解, 进行填充,判定,回填,以及序列化
            // 可能出现map结构不一致的情况 (根据实体上的注解, 有不合法的属性, 就不会被加入到map), 那么调用类似 inset into table k1,k2 values((v11,v12),(v21,v22)) 批量插入就会失败
            mapList.add(entityMember.toFillMap(entity, EntityUseType.INSERT, false));
        }
        // 转入mapList处理
        return beforeBatchInsertMapStyle(mapList);
    }

    /**
     * 批量插入数据, entityMapList处理
     * @param entityMapCollection 数据实体map列表
     */
    protected B beforeBatchInsertMapStyle(Collection<Map<String, Object>> entityMapCollection) {
        if (ObjectUtils.isEmpty(entityMapCollection)) {
            return getSelf();
        }
        // 获取entity所有有效字段
        Set<String> columnNameSet = entityMapCollection.stream()
                .findFirst()
                .orElseThrow(AbnormalParameterException::new)
                .keySet();
        // 值
        for (Map<String, Object> map : entityMapCollection) {
            //  字段的值加入grammar
            value(map.values());
        }
        // 字段加入grammar
        return column(columnNameSet);
    }

    @Override
    public String supportBackQuote(String something) {
        return FormatUtils.backQuote(something, getGrammar().symbol());
    }

    @Override
    public String tableAlias(String table) {
        table = table + " as " + alias();
        return supportBackQuote(table);
    }

    @Override
    public String columnAlias(String column) {
        // 兼容入参包含 表名的情况 eg : table.column
        int lastIndex = column.lastIndexOf('.');
        String result = column.substring(lastIndex + 1);
        column = alias() + "." + result;
        return supportBackQuote(column);
    }

    @Override
    public B lastRaw(String sqlPart) {
        return lastRaw(sqlPart, null);
    }

    @Override
    public B lastRaw(String sqlPart, @Nullable Collection<Object> parameters) {
        grammar.addSmartSeparator(Grammar.SQLPartType.LAST, sqlPart, parameters, " ");
        return getSelf();
    }
}
