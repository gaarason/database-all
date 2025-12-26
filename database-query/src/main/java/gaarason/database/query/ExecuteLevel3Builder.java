package gaarason.database.query;

import gaarason.database.appointment.*;
import gaarason.database.contract.eloquent.Builder;
import gaarason.database.contract.eloquent.Record;
import gaarason.database.contract.eloquent.RecordList;
import gaarason.database.contract.function.ChunkFunctionalInterface;
import gaarason.database.contract.function.RecordListConversionFunctionalInterface;
import gaarason.database.contract.query.Grammar;
import gaarason.database.exception.AbnormalParameterException;
import gaarason.database.exception.EntityNotFoundException;
import gaarason.database.exception.InsertNotSuccessException;
import gaarason.database.exception.SQLRuntimeException;
import gaarason.database.lang.Nullable;
import gaarason.database.support.EntityMember;
import gaarason.database.support.RecordFactory;
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
        int currentPage = 1;
        boolean flag;
        do {
            // 复制查询构造器
            Builder<B, T, K> cloneBuilder = clone();
            // 偏移分页查询
            Paginate<Record<T, K>> paginate = cloneBuilder.paginate(records -> records, currentPage, num, false);
            // 查询结果集集合
            RecordList<T, K> records = ObjectUtils.typeCast(paginate.getItemList());
            // 是否继续
            flag = !records.isEmpty() && chunkFunctionalInterface.execute(records) && (records.size() == num);
            // 页码增加
            currentPage++;
        } while (flag);
    }

    @Override
    public void dealChunk(int num, String column, ChunkFunctionalInterface<T, K> chunkFunctionalInterface)
        throws SQLRuntimeException {
        boolean flag;
        Object columnValue = null;
        do {
            // 复制查询构造器
            Builder<B, T, K> cloneBuilder = clone();
            // 游标分页查询
            CursorPaginate<Record<T, K>> cursorPaginate = cloneBuilder.cursorPaginate(records -> records,
                    column, null, columnValue, OrderBy.ASC, PageNavigation.NEXT, num, false);
            // 查询结果集集合
            RecordList<T, K> records = ObjectUtils.typeCast(cursorPaginate.getItemList());
            // 是否继续
            flag = !records.isEmpty() && chunkFunctionalInterface.execute(records) && (records.size() == num);
            // 游标移动
            columnValue = cursorPaginate.getNextIndex();
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

    @Override
    public <V> Paginate<V> paginate(RecordListConversionFunctionalInterface<T, K, V> func, int currentPage, int perPage,
            boolean hasTotal) {
        Long total = hasTotal ? clone().count() : null;
        RecordList<T, K> records = limit((currentPage - 1) * perPage, perPage).get();
        return new Paginate<>(func.execute(records), currentPage, perPage, total);
    }

    @Override
    public CursorPaginate<T> cursorPaginate(@Nullable K nextPrimaryKeyValue, int perPage) {
        return cursorPaginate(model.getPrimaryKeyColumnName(), nextPrimaryKeyValue, perPage);
    }

    @Override
    public <V> CursorPaginate<V> cursorPaginate(RecordListConversionFunctionalInterface<T, K, V> func, String indexColumn,
            @Nullable Object previousIndex, @Nullable Object nextIndex, OrderBy order, PageNavigation pageNavigation,
            int perPage, boolean hasTotal) {
        // 查询总数
        Long total = hasTotal ? clone().count():  null;
        // 如果 select 非空, 说明存在手动指定查询列, 为避免`索引列`未被手动指定查询, 在这里加上
        if (!getGrammar().isEmpty(Grammar.SQLPartType.SELECT)) {
            select(indexColumn);
        }

        // 顺序
        if (OrderBy.ASC.equals(order)) {
            // 上一页
            if (PageNavigation.PREVIOUS.equals(pageNavigation)) {
                firstOrderBy(builder -> builder.orderBy(indexColumn, OrderBy.DESC))
                        .whereIgnoreNull(indexColumn, "<", previousIndex);
            }
            // 下一页
            else {
                firstOrderBy(builder -> builder.orderBy(indexColumn, OrderBy.ASC))
                        .whereIgnoreNull(indexColumn, ">", nextIndex);
            }
        }
        // 逆序
        else {
            // 上一页
            if (PageNavigation.PREVIOUS.equals(pageNavigation)) {
                firstOrderBy(builder -> builder.orderBy(indexColumn, OrderBy.ASC))
                        .whereIgnoreNull(indexColumn, ">", previousIndex);
            }
            // 下一页
            else {
                firstOrderBy(builder -> builder.orderBy(indexColumn, OrderBy.DESC))
                        .whereIgnoreNull(indexColumn, "<", nextIndex);
            }
        }
        // 限制当前页的数量, 并查询
        RecordList<T, K> recordsLimit = limit(perPage).get();

        // 上一页的查询, 都需要进行顺序反转
        RecordList<T, K> records = PageNavigation.PREVIOUS.equals(pageNavigation)
                ? RecordFactory.newRecordList(container, recordsLimit.reverse())
                : recordsLimit;

        // 没有数据, 则游标不变
        Object previousIndexNew = records.isEmpty() ? previousIndex : records.getFirst().getMetadataMap().get(indexColumn);
        Object nextIndexNew = records.isEmpty() ? nextIndex : records.getLast().getMetadataMap().get(indexColumn);

        return new CursorPaginate<>(func.execute(records), previousIndexNew, nextIndexNew, perPage, total);
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
