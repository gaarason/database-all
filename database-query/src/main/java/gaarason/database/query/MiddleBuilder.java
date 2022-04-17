package gaarason.database.query;

import gaarason.database.appointment.AggregatesType;
import gaarason.database.appointment.SqlType;
import gaarason.database.config.ConversionConfig;
import gaarason.database.contract.connection.GaarasonDataSource;
import gaarason.database.contract.eloquent.Builder;
import gaarason.database.contract.eloquent.Model;
import gaarason.database.contract.eloquent.Record;
import gaarason.database.contract.eloquent.RecordList;
import gaarason.database.contract.function.GenerateSqlPartFunctionalInterface;
import gaarason.database.contract.query.Grammar;
import gaarason.database.exception.EntityNotFoundException;
import gaarason.database.exception.InsertNotSuccessException;
import gaarason.database.exception.SQLRuntimeException;
import gaarason.database.lang.Nullable;
import gaarason.database.provider.ContainerProvider;
import gaarason.database.provider.ModelShadowProvider;
import gaarason.database.util.FormatUtils;
import gaarason.database.util.MapUtils;
import gaarason.database.util.ObjectUtils;
import gaarason.database.util.StringUtils;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.*;

/**
 * 中间查询构造器
 * @param <T>
 * @param <K>
 * @author xt
 */
public abstract class MiddleBuilder<T extends Serializable, K extends Serializable> extends BaseBuilder<T, K> {

    protected MiddleBuilder(GaarasonDataSource gaarasonDataSource, Model<T, K> model, Grammar grammar) {
        super(gaarasonDataSource, model, grammar);
    }

    @Override
    public <R> R aggregate(AggregatesType op, String column) {
        String alias = StringUtils.getRandomString(6);
        Builder<T, K> builder = this;
        if (grammar.hasGroup()) {
            if (!grammar.hasSelect()) {
                this.selectRaw(grammar.getGroup());
            }
            builder = model.newQuery().from(alias + "sub", this.toSql(SqlType.SELECT));
        }

        Map<String, Object> resMap = builder.selectFunction(op.toString(), column, alias).firstOrFail().toMap();
        return ObjectUtils.typeCast(resMap.get(alias));
    }

    @Override
    public Long count() {
        return count("*");
    }

    @Override
    public Long count(String column) {
        return aggregate(AggregatesType.count, column);
    }

    @Override
    public String max(String column) {
        Object aggregate = aggregate(AggregatesType.max, column);
        return String.valueOf(aggregate);
    }

    @Override
    public String min(String column) {
        Object aggregate = aggregate(AggregatesType.min, column);
        return String.valueOf(aggregate);
    }

    @Override
    public BigDecimal avg(String column) {
        return aggregate(AggregatesType.avg, column);
    }

    @Override
    public BigDecimal sum(String column) {
        return aggregate(AggregatesType.sum, column);
    }

    @Override
    public Builder<T, K> forceIndex(String indexName) {
        grammar.pushForceIndex(FormatUtils.column(indexName));
        return this;
    }

    @Override
    public Builder<T, K> ignoreIndex(String indexName) {
        grammar.pushIgnoreIndex(FormatUtils.column(indexName));
        return this;
    }

    @Override
    public Builder<T, K> fromRaw(String sqlPart) {
        grammar.pushFrom(sqlPart);
        return this;
    }

    @Override
    public Builder<T, K> from(String table) {
        return fromRaw(FormatUtils.column(table));
    }

    @Override
    public Builder<T, K> from(String alias, GenerateSqlPartFunctionalInterface<T, K> closure) {
        return fromRaw(generateSql(closure) + alias);
    }

    @Override
    public Builder<T, K> from(String alias, String sql) {
        return fromRaw(FormatUtils.bracket(sql) + alias);
    }

    @Override
    public String toSql(SqlType sqlType) {
        String sql = grammar.generateSql(sqlType);
        List<String> parameterList = grammar.getParameterList(sqlType);
        return String.format(sql.replace(" ? ", "\"%s\""), parameterList.toArray());
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
        Set<String> columnNameSet = ModelShadowProvider.columnNameSet(entity, true);
        // 获取entity所有有效字段的值
        List<Object> valueList = ModelShadowProvider.valueList(entity, columnNameSet);
        // 字段加入grammar
        select(columnNameSet);
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
        select(columnNameSet);
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
        String sql = grammar.generateSql(SqlType.INSERT);
        List<String> parameterList = grammar.getParameterList(SqlType.INSERT);
        return executeGetId(sql, parameterList);
    }

    @Override
    public K insertGetId(T entity) throws SQLRuntimeException {
        // 获取entity所有有效sql字段
        Set<String> columnNameSet = ModelShadowProvider.columnNameSet(entity, true);
        // 获取entity所有有效字段的值
        List<Object> valueList = ModelShadowProvider.valueList(entity, columnNameSet);
        // 字段加入grammar
        select(columnNameSet);
        // 字段的值加入grammar
        value(valueList);
        // 执行, 并获取主键id
        K primaryId = insertGetId();
        // 赋值主键
        ModelShadowProvider.setPrimaryKeyValue(entity, primaryId);
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
        select(columnNameSet);
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
    public K insertGetIdOrFailMapStyle(Map<String, Object> entityMap) throws SQLRuntimeException, InsertNotSuccessException {
        K id = insertGetIdMapStyle(entityMap);
        if (id == null) {
            throw new InsertNotSuccessException();
        }
        return id;
    }

    @Override
    public List<K> insertGetIds() throws SQLRuntimeException {
        // sql 组装
        String sql = grammar.generateSql(SqlType.INSERT);
        List<String> parameterList = grammar.getParameterList(SqlType.INSERT);
        return executeGetIds(sql, parameterList);
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
        Map<String, Object> stringStringMap = ModelShadowProvider.columnValueMap(entity, false);

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
        Set<String> columnNameSet = ModelShadowProvider.columnNameSet(entityList.get(0), true);
        List<List<Object>> valueListList = new ArrayList<>();
        for (T entity : entityList) {
            // 获取entity所有有效字段的值
            List<Object> valueList = ModelShadowProvider.valueList(entity, columnNameSet);
            valueListList.add(valueList);
        }
        // 字段加入grammar
        select(columnNameSet);
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
        select(columnNameSet);
        // 字段的值加入grammar
        valueList(valueListList);
    }

    /**
     * 格式化参数类型,到绑定参数
     * @param value 参数
     * @return 参数占位符?
     */
    protected String formatValue(@Nullable Object value) {
        return FormatUtils.value(ContainerProvider.getBean(ConversionConfig.class).castNullable(value, String.class), grammar);
    }

    /**
     * 格式化参数类型,到绑定参数
     * @param value 参数
     * @return 参数占位符?
     */
    protected String formatData(@Nullable Object value) {
        return FormatUtils.data(ContainerProvider.getBean(ConversionConfig.class).castNullable(value, String.class), grammar);
    }

    /**
     * 格式化参数类型,到绑定参数
     * @param valueList 参数
     * @return 参数占位符?
     */
    protected String formatValue(Collection<?> valueList) {
        return FormatUtils.value(valueList, grammar);
    }

    /**
     * 返回数据库方法的拼接字符
     * @param functionName 方法名
     * @param parameter    参数字符串
     * @param alias        别名
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
    protected abstract String column(String something);
}
