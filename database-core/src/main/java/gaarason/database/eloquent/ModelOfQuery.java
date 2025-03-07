package gaarason.database.eloquent;

import gaarason.database.appointment.EntityUseType;
import gaarason.database.appointment.JDBCValueWrapper;
import gaarason.database.config.ConversionConfig;
import gaarason.database.contract.connection.GaarasonDataSource;
import gaarason.database.contract.eloquent.Builder;
import gaarason.database.contract.eloquent.Record;
import gaarason.database.contract.eloquent.RecordList;
import gaarason.database.contract.function.ExecSqlWithinConnectionFunctionalInterface;
import gaarason.database.contract.model.Query;
import gaarason.database.core.Container;
import gaarason.database.exception.EntityNotFoundException;
import gaarason.database.exception.PrimaryKeyNotFoundException;
import gaarason.database.exception.PrimaryKeyTypeNotSupportException;
import gaarason.database.exception.SQLRuntimeException;
import gaarason.database.lang.Nullable;
import gaarason.database.support.FieldMember;
import gaarason.database.support.PrimaryKeyMember;
import gaarason.database.support.RecordFactory;
import gaarason.database.util.EntityUtils;
import gaarason.database.util.ObjectUtils;
import gaarason.database.util.StringUtils;

import java.sql.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * 数据模型对象
 * @author xt
 */
abstract class ModelOfQuery<T, K> extends ModelOfSoftDelete<T, K> implements Query<T, K> {

    /**
     * 全局查询作用域
     * @param builder 查询构造器
     * @return 查询构造器
     */
    protected <B extends Builder<B, T, K>> B apply(B builder) {
        return builder;
    }

    /**
     * 原始查询构造器
     * @return 原始查询构造器
     */
    protected <B extends Builder<B, T, K>> B theBuilder() {
        GaarasonDataSource gaarasonDataSource = getGaarasonDataSource();
        Builder<?, T, K> builder = gaarasonDataSource.getQueryBuilder().newBuilder(gaarasonDataSource, this);
        return apply(ObjectUtils.typeCast(builder));
    }

    @Override
    public <B extends Builder<B, T, K>> B newQuery() {
        B builder = theBuilder();
        if (softDeleting()) {
            scopeSoftDelete(builder);
        }
        return builder;
    }

    @Override
    public <B extends Builder<B, T, K>> B withTrashed() {
        B builder = theBuilder();
        scopeSoftDeleteWithTrashed(builder);
        return builder;
    }

    @Override
    public <B extends Builder<B, T, K>> B onlyTrashed() {
        B builder = theBuilder();
        scopeSoftDeleteOnlyTrashed(builder);
        return builder;
    }

    @Override
    public Record<T, K> newRecord() {
        return new RecordBean<>(this);
    }

    @Override
    public RecordList<T, K> findAll(String... column) throws SQLRuntimeException {
        return newQuery().select(column).get();
    }

    @Override
    public RecordList<T, K> findMany(Collection<Object> ids) throws SQLRuntimeException {
        return newQuery().select(getEntityClass()).whereIn(getPrimaryKeyColumnName(), ids).get();
    }

    @Override
    public RecordList<T, K> findMany(Object... ids) throws SQLRuntimeException {
        return newQuery().select(getEntityClass())
            .whereIn(getPrimaryKeyColumnName(), new HashSet<>(Arrays.asList(ids)))
            .get();
    }

    @Override
    public Record<T, K> findOrFail(@Nullable Object id) throws EntityNotFoundException, SQLRuntimeException {
        return newQuery().select(getEntityClass()).where(getPrimaryKeyColumnName(), id).firstOrFail();
    }

    @Override
    @Nullable
    public Record<T, K> find(@Nullable Object id) {
        return newQuery().select(getEntityClass()).where(getPrimaryKeyColumnName(), id).first();
    }

    @Override
    public Record<T, K> findOrNew(T entity) {
        // 查询是否存在满足条件的一条记录
        final Record<T, K> first = newQuery().select(getEntityClass()).where(entity).first();
        if (first != null) {
            return first;
        }
        // 新增此记录
        final Record<T, K> tkRecord = this.newRecord();
        tkRecord.getEntity(entity);
        return tkRecord;
    }

    @Override
    public Record<T, K> findByPrimaryKeyOrNew(T entity) {
        // 获取 entity 中的主键的值

        final Object primaryKeyValue = getModelShadow().parseAnyEntityWithCache(entity.getClass())
            .getPrimaryKeyMemberOrFail()
            .getFieldMember()
            .fieldFillGetOrFail(entity, EntityUseType.CONDITION);

        // 查询是否存在满足条件的一条记录
        final Record<T, K> first = find(primaryKeyValue);
        if (first != null) {
            return first;
        }
        // 新增此记录
        final Record<T, K> tkRecord = this.newRecord();
        tkRecord.getEntity(entity);
        return tkRecord;
    }

    @Override
    public Record<T, K> findOrCreate(T entity) {
        final Record<T, K> theRecord = findOrNew(entity);
        // 如果未持久化
        if (!theRecord.isHasBind()) {
            // 进行持久化
            theRecord.save();
        }
        return theRecord;
    }

    @Override
    public Record<T, K> findByPrimaryKeyOrCreate(T entity) {
        final Record<T, K> theRecord = findByPrimaryKeyOrNew(entity);
        // 如果未持久化
        if (!theRecord.isHasBind()) {
            // 进行持久化
            theRecord.save();
        }
        return theRecord;
    }

    @Override
    public Record<T, K> findOrNew(T conditionEntity, T complementEntity) {
        // 查询是否存在满足条件的一条记录
        final Record<T, K> first = newQuery().select(getEntityClass()).where(conditionEntity).first();
        if (first != null) {
            return first;
        }
        // 新增此记录
        final Record<T, K> tkRecord = this.newRecord();
        // 合并属性
        final T entityMerge = EntityUtils.entityMerge(conditionEntity, complementEntity);
        tkRecord.getEntity(entityMerge);
        return tkRecord;
    }

    @Override
    public Record<T, K> findOrCreate(T conditionEntity, T complementEntity) {
        final Record<T, K> theRecord = findOrNew(conditionEntity, complementEntity);
        // 如果未持久化
        if (!theRecord.isHasBind()) {
            // 进行持久化
            theRecord.save();
        }
        return theRecord;
    }

    @Override
    public Record<T, K> create(T entity) {
        final Record<T, K> theRecord = newRecord();
        theRecord.getEntity(entity);
        theRecord.save();
        return theRecord;
    }

    @Override
    public Record<T, K> update(T entity) {
        final Record<T, K> theRecord = newRecord();
        theRecord.fillEntity(entity).saveByPrimaryKey();
        return theRecord;
    }

    @Override
    public Record<T, K> updateByPrimaryKeyOrCreate(T entity) {
        // 获取 entity 中的主键的值
        final Object primaryKeyValue = getModelShadow().getPrimaryKeyValue(entity, EntityUseType.CONDITION);
        final Record<T, K> first = find(primaryKeyValue);
        final Record<T, K> theRecord = first != null ? first : this.newRecord();
        theRecord.fillEntity(entity);
        theRecord.save();
        return theRecord;
    }

    @Override
    public Record<T, K> updateOrCreate(T conditionEntity, T complementEntity) {
        // 查询是否存在满足条件的一条记录
        final Record<T, K> first = newQuery().select(getEntityClass()).where(conditionEntity).first();
        if (first != null) {
            // 更新
            first.fillEntity(complementEntity);
            first.save();
            return first;
        } else {
            // 新增
            final Record<T, K> tkRecord = this.newRecord();
            tkRecord.getEntity(conditionEntity);
            tkRecord.fillEntity(complementEntity);
            tkRecord.save();
            return tkRecord;
        }
    }

    @Override
    public String getPrimaryKeyColumnName() throws PrimaryKeyNotFoundException {
        PrimaryKeyMember<K> primaryKeyMember = getModelMember().getEntityMember().getPrimaryKeyMember();
        if (null == primaryKeyMember) {
            throw new PrimaryKeyNotFoundException();
        }
        return primaryKeyMember.getFieldMember().getColumnName();
    }

    @Override
    public Class<K> getPrimaryKeyClass() {
        return getModelMember().getPrimaryKeyClass();
    }

    @Override
    public String getTableName() {
        return getModelMember().getEntityMember().getTableName();
    }

    @Override
    public Class<T> getEntityClass() {
        return getModelMember().getEntityClass();
    }

    @Override
    public Container getContainer() {
        return getGaarasonDataSource().getContainer();
    }

    @Override
    public Record<T, K> nativeQueryOrFail(String sql, @Nullable Collection<?> parameters)
        throws SQLRuntimeException, EntityNotFoundException {
        Record<T, K> record = nativeQuery(sql, parameters);
        if (ObjectUtils.isEmpty(record)) {
            throw new EntityNotFoundException(sql);
        }
        return record;
    }

    @Override
    @Nullable
    public Record<T, K> nativeQuery(String sql, @Nullable Collection<?> parameters)
        throws SQLRuntimeException, EntityNotFoundException {
        RecordList<T, K> records = nativeQueryList(sql, parameters);
        return ObjectUtils.isEmpty(records) ? null : records.get(0);
    }

    @Override
    public RecordList<T, K> nativeQueryList(String sql, @Nullable Collection<?> parameters) throws SQLRuntimeException {
        return doSomethingInConnection(preparedStatement -> {
            ResultSet resultSet = preparedStatement.executeQuery();
            return RecordFactory.newRecordList(getSelf(), resultSet, sql);
        }, sql, parameters, false);
    }

    @Override
    public int nativeExecute(String sql, @Nullable Collection<?> parameters) throws SQLRuntimeException {
        return doSomethingInConnection(PreparedStatement::executeUpdate, sql, parameters, true);
    }

    @Override
    public List<K> nativeExecuteGetIds(String sql, @Nullable Collection<?> parameters) throws SQLRuntimeException {
        return doSomethingInConnection(preparedStatement -> {
            List<K> ids = new ArrayList<>();
            // 执行
            preparedStatement.executeUpdate();
            // 执行成功
            ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
            while (generatedKeys.next()) {
                ids.add(getGeneratedKeys(generatedKeys));
            }
            generatedKeys.close();
            return ids;
        }, sql, parameters, true);
    }

    @Override
    @Nullable
    public K nativeExecuteGetId(String sql, @Nullable Collection<?> parameters) throws SQLRuntimeException {
        List<K> list = nativeExecuteGetIds(sql, parameters);
        return ObjectUtils.isEmpty(list) ? null : list.get(0);
    }

    @Override
    public CompletableFuture<Record<T, K>> nativeQueryOrFailAsync(String sql, @Nullable Collection<?> parameters)
        throws SQLRuntimeException, EntityNotFoundException {
        CompletableFuture<Record<T, K>> recordCompletableFuture = nativeQueryAsync(sql, parameters);
        return recordCompletableFuture.thenApply(record -> {
            if (ObjectUtils.isEmpty(record)) {
                throw new EntityNotFoundException(sql);
            }
            return record;
        });
    }

    @Override
    public CompletableFuture<Record<T, K>> nativeQueryAsync(String sql, @Nullable Collection<?> parameters)
        throws SQLRuntimeException, EntityNotFoundException {
        CompletableFuture<RecordList<T, K>> recordListCompletableFuture = nativeQueryListAsync(sql, parameters);
        return recordListCompletableFuture.thenApply(records -> ObjectUtils.isEmpty(records) ? null : records.get(0));
    }

    @Override
    public CompletableFuture<RecordList<T, K>> nativeQueryListAsync(String sql, @Nullable Collection<?> parameters)
        throws SQLRuntimeException {
        return doSomethingInConnectionAsync(preparedStatement -> {
            ResultSet resultSet = preparedStatement.executeQuery();
            return RecordFactory.newRecordList(getSelf(), resultSet, sql);
        }, sql, parameters, false);
    }

    @Override
    public CompletableFuture<Integer> nativeExecuteAsync(String sql, @Nullable Collection<?> parameters)
        throws SQLRuntimeException {
        return doSomethingInConnectionAsync(PreparedStatement::executeUpdate, sql, parameters, true);
    }

    @Override
    public CompletableFuture<List<K>> nativeExecuteGetIdsAsync(String sql, @Nullable Collection<?> parameters)
        throws SQLRuntimeException {
        return doSomethingInConnectionAsync(preparedStatement -> {
            List<K> ids = new ArrayList<>();
            // 执行
            preparedStatement.executeUpdate();
            // 执行成功
            ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
            while (generatedKeys.next()) {
                ids.add(getGeneratedKeys(generatedKeys));
            }
            generatedKeys.close();
            return ids;
        }, sql, parameters, true);
    }

    @Override
    public CompletableFuture<K> nativeExecuteGetIdAsync(String sql, @Nullable Collection<?> parameters)
        throws SQLRuntimeException {
        CompletableFuture<List<K>> listCompletableFuture = nativeExecuteGetIdsAsync(sql, parameters);
        return listCompletableFuture.thenApply(res -> ObjectUtils.isEmpty(res) ? null : res.get(0));
    }

    /**
     * 在连接中执行
     * @param closure 闭包
     * @param sql 带占位符的sql
     * @param parameters sql的参数
     * @param isWrite 是否写(主)链接
     * @param <U> 响应类型
     * @return 响应
     * @throws SQLRuntimeException 数据库异常
     */
    protected <U> U doSomethingInConnection(ExecSqlWithinConnectionFunctionalInterface<U> closure, String sql,
        @Nullable Collection<?> parameters, boolean isWrite) throws SQLRuntimeException {

        Collection<?> localParameters = parameters == null ? Collections.EMPTY_LIST : parameters;
        GaarasonDataSource gaarasonDataSource = getGaarasonDataSource();
        // 获取连接
        Connection connection = gaarasonDataSource.getLocalConnection(isWrite);

        try {
            // 参数准备
            PreparedStatement preparedStatement = executeSql(connection, sql, localParameters);
            // 执行
            return closure.execute(preparedStatement);
        } catch (EntityNotFoundException e) {
            throw new EntityNotFoundException(
                String.format(StringUtils.replace(sql, " ? ", "\"%s\""), localParameters.toArray()));
        } catch (Throwable e) {
            throw new SQLRuntimeException(sql, localParameters, e.getMessage(),
                gaarasonDataSource.getQueryBuilder().getValueSymbol(), e);
        } finally {
            // 关闭连接
            gaarasonDataSource.localConnectionClose(connection);
        }
    }

    /**
     * 在连接中异步执行
     * @param closure 闭包
     * @param sql 带占位符的sql
     * @param parameters sql的参数
     * @param isWrite 是否写(主)链接
     * @param <U> 响应类型
     * @return 响应
     * @throws SQLRuntimeException 数据库异常
     */
    protected <U> CompletableFuture<U> doSomethingInConnectionAsync(
        ExecSqlWithinConnectionFunctionalInterface<U> closure, String sql, @Nullable Collection<?> parameters,
        boolean isWrite) throws SQLRuntimeException {

        GaarasonDataSource gaarasonDataSource = getGaarasonDataSource();

        // 当前是否在事务中
        boolean inTransaction = gaarasonDataSource.isLocalThreadInTransaction();

        if (inTransaction) {
            // 事务中使用同步执行
            U value = doSomethingInConnection(closure, sql, parameters, isWrite);
            return CompletableFuture.completedFuture(value);
        } else {
            // 非事务中使用异步执行
            return CompletableFuture.supplyAsync(() -> doSomethingInConnection(closure, sql, parameters, isWrite),
                getExecutorService());
        }
    }

    /**
     * 获取主键
     * @param generatedKeys 数据集
     * @return 主键
     * @throws SQLException 数据库异常
     * @throws PrimaryKeyTypeNotSupportException 不支持的主键类型
     */
    @Nullable
    private K getGeneratedKeys(ResultSet generatedKeys) throws SQLException, PrimaryKeyTypeNotSupportException {
        PrimaryKeyMember<K> primaryKeyMember = getModelMember().getEntityMember().getPrimaryKeyMember();
        // *尽量* 使用同类型赋值
        Class<K> primaryKeyClass = getSelf().getPrimaryKeyClass();
        // 数据库获取
        Object value = generatedKeys.getObject(1);
        // 值获取
        if (!ObjectUtils.isNull(primaryKeyMember)) {
            FieldMember<K> fieldMember = primaryKeyMember.getFieldMember();
            // 按照字段类型进行转化
            return fieldMember.deserialize(value);
        } else {
            // 按照字段类型进行转化
            return getContainer().getBean(ConversionConfig.class).castNullable(value, primaryKeyClass);
        }
    }

    /**
     * 执行sql
     * @param connection 数据库连接
     * @param sql 查询语句
     * @param parameterList 参数绑定
     * @return 预执行对象
     * @throws SQLException sql错误
     */
    protected PreparedStatement executeSql(Connection connection, String sql, Collection<?> parameterList)
        throws SQLException {
        // 日志记录
        getSelf().log(sql, parameterList);
        // 预执行 ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY
        PreparedStatement preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        // 参数绑定
        int i = 1;
        for (Object parameter : parameterList) {
            setParameter(preparedStatement, i++, parameter);
        }
        // 返回预执行对象
        return preparedStatement;
    }

    /**
     * 单个参数绑定
     * @param preparedStatement 预执行对象
     * @param index 参数索引
     * @param parameter 参数对象
     */
    protected static void setParameter(PreparedStatement preparedStatement, int index, Object parameter)
        throws SQLException {
        if (parameter instanceof JDBCValueWrapper) {
            // 精确类型
            preparedStatement.setObject(index, ((JDBCValueWrapper) parameter).getValue(),
                ((JDBCValueWrapper) parameter).getType());
        } else {
            // 全凭运气
            preparedStatement.setObject(index, parameter);
        }
    }

}
