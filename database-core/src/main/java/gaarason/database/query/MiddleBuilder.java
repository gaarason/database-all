package gaarason.database.query;

import gaarason.database.contract.connection.GaarasonDataSource;
import gaarason.database.contract.eloquent.Builder;
import gaarason.database.contract.eloquent.Model;
import gaarason.database.contract.eloquent.Record;
import gaarason.database.contract.eloquent.RecordList;
import gaarason.database.contract.function.GenerateSqlPartFunctionalInterface;
import gaarason.database.eloquent.appointment.AggregatesType;
import gaarason.database.eloquent.appointment.SqlType;
import gaarason.database.exception.EntityNotFoundException;
import gaarason.database.exception.InsertNotSuccessException;
import gaarason.database.exception.SQLRuntimeException;
import gaarason.database.provider.ModelShadowProvider;
import gaarason.database.util.FormatUtil;
import gaarason.database.util.ObjectUtil;
import gaarason.database.util.StringUtil;

import java.math.BigDecimal;
import java.util.*;

abstract public class MiddleBuilder<T, K> extends BaseBuilder<T, K> {

    public MiddleBuilder(GaarasonDataSource gaarasonDataSource, Model<T, K> model, Class<T> entityClass) {
        super(gaarasonDataSource, model, entityClass);
    }

    @Override
    public <R> R aggregate(AggregatesType op, String column) {
        String alias = StringUtil.getRandomString(6);
        Builder<T, K> builder = this;
        if(grammar.hasGroup()){
            if(!grammar.hasSelect()){
                this.selectRaw(grammar.getGroup());
            }
            builder = model.newQuery().from(alias + "sub", this.toSql(SqlType.SELECT));
        }

        Map<String, Object> resMap = builder.selectFunction(op.toString(), column, alias).firstOrFail().toMap();
        return ObjectUtil.typeCast(resMap.get(alias));
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
    public Builder<T, K> forceIndex(String indexName){
        grammar.pushForceIndex(FormatUtil.column(indexName));
        return this;
    }

    @Override
    public Builder<T, K> ignoreIndex(String indexName){
        grammar.pushIgnoreIndex(FormatUtil.column(indexName));
        return this;
    }

    @Override
    public Builder<T, K> from(String table) {
        grammar.pushFrom(FormatUtil.column(table));
        return this;
    }

    @Override
    public Builder<T, K> from(String alias, GenerateSqlPartFunctionalInterface closure) {
        grammar.pushFrom(FormatUtil.bracket(generateSql(closure)) + alias);
        return this;
    }

    @Override
    public Builder<T, K> from(String alias, String sql) {
        grammar.pushFrom(FormatUtil.bracket(sql) + alias);
        return this;
    }

    /**
     * 格式化参数类型,到绑定参数
     * @param value 参数
     * @return 参数占位符?
     */
    protected String formatValue(String value) {
        return FormatUtil.value(value, grammar);
    }

    /**
     * 格式化参数类型,到绑定参数
     * @param value 参数
     * @return 参数占位符?
     */
    protected String formatData(String value) {
        return FormatUtil.data(value, grammar);
    }

    /**
     * 格式化参数类型,到绑定参数
     * @param valueList 参数
     * @return 参数占位符?
     */
    protected String formatValue(Collection<?> valueList) {
        return FormatUtil.value(valueList, grammar);
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
        List<String> columnNameList = ModelShadowProvider.columnNameList(entity, true);
        // 获取entity所有有效字段的值
        List<String> valueList = ModelShadowProvider.valueList(entity, columnNameList);
        // 字段加入grammar
        select(columnNameList);
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
    public K insertGetId() throws SQLRuntimeException {
        String sql = grammar.generateSql(SqlType.INSERT);
        List<String> parameterList = grammar.getParameterList(SqlType.INSERT);
        return executeGetId(sql, parameterList);
    }

    @Override
    public K insertGetId(T entity) throws SQLRuntimeException {
        // 获取entity所有有效sql字段
        List<String> columnNameList = ModelShadowProvider.columnNameList(entity, true);
        // 获取entity所有有效字段的值
        List<String> valueList = ModelShadowProvider.valueList(entity, columnNameList);
        // 字段加入grammar
        select(columnNameList);
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

    /**
     * 批量插入数据, entityList处理
     * @param entityList 数据实体对象列表
     */
    protected void beforeBatchInsert(List<T> entityList) {
        // 获取entity所有有效字段
        List<String> columnNameList = ModelShadowProvider.columnNameList(entityList.get(0), true);
        List<List<String>> valueListList = new ArrayList<>();
        for (T entity : entityList) {
            // 获取entity所有有效字段的值
            List<String> valueList = ModelShadowProvider.valueList(entity, columnNameList);
            valueListList.add(valueList);
        }
        // 字段加入grammar
        select(columnNameList);
        // 字段的值加入grammar
        valueList(valueListList);
    }

    @Override
    public int update() throws SQLRuntimeException {
        return updateSql(SqlType.UPDATE);
    }

    @Override
    public int update(T entity) throws SQLRuntimeException {
        // 获取entity所有有效字段对其值得映射
        Map<String, String> stringStringMap = ModelShadowProvider.columnValueMap(entity, false);

        data(stringStringMap);
        // 执行
        return update();
    }

    /**
     * 给字段加上引号
     * @param something 字段 eg: sum(order.amount) AS sum_price
     * @return eg: sum(`order`.`amount`) AS `sum_price`
     */
    protected static String column(String something) {
        return FormatUtil.backQuote(something, "`");
    }
}
