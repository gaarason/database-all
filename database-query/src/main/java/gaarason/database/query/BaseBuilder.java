package gaarason.database.query;

import gaarason.database.appointment.FinalVariable;
import gaarason.database.appointment.Paginate;
import gaarason.database.appointment.ParameterAndType;
import gaarason.database.appointment.SqlType;
import gaarason.database.config.ConversionConfig;
import gaarason.database.contract.connection.GaarasonDataSource;
import gaarason.database.contract.eloquent.Builder;
import gaarason.database.contract.eloquent.Model;
import gaarason.database.contract.eloquent.Record;
import gaarason.database.contract.eloquent.RecordList;
import gaarason.database.contract.function.*;
import gaarason.database.contract.query.Grammar;
import gaarason.database.core.Container;
import gaarason.database.exception.*;
import gaarason.database.lang.Nullable;
import gaarason.database.provider.ModelShadowProvider;
import gaarason.database.support.RecordFactory;
import gaarason.database.util.ExceptionUtils;
import gaarason.database.util.ObjectUtils;

import java.io.Serializable;
import java.sql.*;
import java.util.*;

/**
 * 基础查询构造器(sql生成器)
 * @param <T>
 * @param <K>
 * @author xt
 */
public abstract class BaseBuilder<T extends Serializable, K extends Serializable> implements Builder<T, K> {

    /**
     * 数据库连接
     */
    protected final GaarasonDataSource gaarasonDataSource;

    /**
     * 数据模型
     */
    protected final Model<T, K> model;

    /**
     * 容器
     */
    protected final Container container;

    /**
     * Model信息大全
     */
    protected final ModelShadowProvider modelShadowProvider;

    /**
     * 类型转化
     */
    protected final ConversionConfig conversion;

    /**
     * 数据实体类
     */
    final Class<T> entityClass;

    /**
     * sql生成器
     */
    Grammar grammar;

    protected BaseBuilder(GaarasonDataSource gaarasonDataSource, Model<T, K> model, Grammar grammar) {
        this.gaarasonDataSource = gaarasonDataSource;
        this.container = gaarasonDataSource.getContainer();
        this.modelShadowProvider = container.getBean(ModelShadowProvider.class);
        this.conversion = container.getBean(ConversionConfig.class);
        this.model = model;
        this.entityClass = model.getEntityClass();
        this.grammar = grammar;
    }

    @Override
    public Grammar getGrammar() {
        return grammar;
    }

    @Override
    public void setGrammar(Grammar grammar) {
        this.grammar = grammar;
    }

    /**
     * 得到一个全新的查询构造器
     * @return 查询构造器
     */
    @Override
    public Builder<T, K> getNewSelf() {
        return model.newQuery();
    }

    @Override
    public Builder<T, K> getSelf() {
        return this;
    }

    @Override
    public String lambda2FieldName(ColumnFunctionalInterface<T> column) {
        return modelShadowProvider.getFieldNameByLambdaWithCache(column);
    }

    @Override
    public String lambda2ColumnName(ColumnFunctionalInterface<T> column) {
        return modelShadowProvider.getColumnNameByLambdaWithCache(column);
    }

    /**
     * 克隆当前查询构造器
     * @return 查询构造器
     * @throws CloneNotSupportedRuntimeException 克隆异常
     */
    @Override
    @SuppressWarnings("unchecked")
    public Builder<T, K> clone() throws CloneNotSupportedRuntimeException {
        try {
            // 浅拷贝
            Builder<T, K> builder = (Builder<T, K>) super.clone();
            // 深拷贝
            builder.setGrammar(grammar.deepCopy());
            return builder;
        } catch (CloneNotSupportedException e) {
            throw new CloneNotSupportedRuntimeException(e.getMessage(), e);
        }
    }

    /**
     * 渴求式关联
     * @param fieldName 所关联的Model(当前模块的属性名)
     * @return 关联的Model的查询构造器
     */
    @Override
    public Builder<T, K> with(String fieldName) {
        return with(fieldName, builder -> builder, theRecord -> theRecord);
    }

    /**
     * 渴求式关联
     * @param fieldName 所关联的Model(当前模块的属性名)
     * @param builderClosure 所关联的Model的查询构造器约束
     * @return 关联的Model的查询构造器
     */
    @Override
    public Builder<T, K> with(String fieldName, GenerateSqlPartFunctionalInterface<?, ?> builderClosure) {
        return with(fieldName, builderClosure, theRecord -> theRecord);
    }

    /**
     * 渴求式关联
     * @param fieldName 所关联的Model(当前模块的属性名)
     * @param builderClosure 所关联的Model的查询构造器约束
     * @param recordClosure 所关联的Model的再一级关联
     * @return 关联的Model的查询构造器
     */
    @Override
    public Builder<T, K> with(String fieldName, GenerateSqlPartFunctionalInterface<?, ?> builderClosure,
        RelationshipRecordWithFunctionalInterface recordClosure) {
        grammar.pushWith(fieldName, builderClosure, recordClosure);
        return this;
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
     * 数据库事物开启
     * @throws SQLRuntimeException 数据库异常
     */
    @Override
    public void begin() throws SQLRuntimeException {
        gaarasonDataSource.begin();
    }

    /**
     * 数据库事物提交
     * @throws SQLRuntimeException 数据库异常
     */
    @Override
    public void commit() throws SQLRuntimeException {
        gaarasonDataSource.commit();
    }

    /**
     * 数据库事物回滚
     * @throws SQLRuntimeException 数据库异常
     */
    @Override
    public void rollBack() throws SQLRuntimeException {
        gaarasonDataSource.rollBack();
    }

    @Override
    public <V> V transaction(TransactionFunctionalInterface<V> closure) {
        return transaction(closure, FinalVariable.DEFAULT_CAUSED_BY_DEADLOCK_RETRY_COUNT);
    }

    @Override
    public <V> V transaction(TransactionFunctionalInterface<V> closure, int maxAttempts) {
        for (int currentAttempt = 0; currentAttempt <= maxAttempts; currentAttempt++) {
            begin();
            try {
                V result = closure.execute();
                commit();
                return result;
            } catch (Throwable e) {
                rollBack();
                if (currentAttempt >= maxAttempts || !ExceptionUtils.causedByDeadlock(e)) {
                    throw e;
                }
            }
        }
        throw new AbnormalParameterException("The max attempts should not be less than 0.");
    }

    @Override
    public void transaction(Runnable closure) {
        transaction(closure, FinalVariable.DEFAULT_CAUSED_BY_DEADLOCK_RETRY_COUNT);
    }

    @Override
    public void transaction(Runnable closure, int maxAttempts) {
        for (int currentAttempt = 0; currentAttempt <= maxAttempts; currentAttempt++) {
            begin();
            try {
                closure.run();
                commit();
                return;
            } catch (Throwable e) {
                rollBack();
                if (currentAttempt >= maxAttempts || !ExceptionUtils.causedByDeadlock(e)) {
                    throw e;
                }
            }
        }
        throw new AbnormalParameterException("The max attempts should not be less than 0.");
    }

    /**
     * 恢复软删除模型
     * @return 受影响的行数
     * @throws SQLRuntimeException sql异常
     */
    @Override
    public int restore() throws SQLRuntimeException {
        return model.restore(this);
    }

    /**
     * 删除
     * @return 受影响的行数
     * @throws SQLRuntimeException sql异常
     */
    @Override
    public int delete() throws SQLRuntimeException {
        return model.delete(this);
    }

    @Override
    public int forceDelete() throws SQLRuntimeException {
        return updateSql(SqlType.DELETE);
    }

    @Nullable
    @Override
    public Record<T, K> query(String sql, @Nullable Collection<?> parameters) throws SQLRuntimeException {
        try {
            return queryOrFail(sql, parameters);
        } catch (EntityNotFoundException e) {
            return null;
        }
    }

    @Nullable
    @Override
    public Record<T, K> query(String sql, Object... parameters) throws SQLRuntimeException {
        return query(sql, Arrays.asList(parameters));
    }

    @Override
    public Record<T, K> queryOrFail(String sql, @Nullable Collection<?> parameters)
        throws SQLRuntimeException, EntityNotFoundException {
        return doSomethingInConnection(preparedStatement -> {
            ResultSet resultSet = preparedStatement.executeQuery();
            return RecordFactory.newRecord(model, resultSet, sql);
        }, sql, parameters, false);
    }

    @Override
    public Record<T, K> queryOrFail(String sql, Object... parameters)
        throws SQLRuntimeException, EntityNotFoundException {
        return queryOrFail(sql, Arrays.asList(parameters));
    }

    @Override
    public RecordList<T, K> queryList(String sql, @Nullable Collection<?> parameters) throws SQLRuntimeException {
        return doSomethingInConnection(preparedStatement -> {
            ResultSet resultSet = preparedStatement.executeQuery();
            return RecordFactory.newRecordList(model, resultSet, sql);
        }, sql, parameters, false);
    }

    @Override
    public RecordList<T, K> queryList(String sql, Object... parameters) throws SQLRuntimeException {
        return queryList(sql, Arrays.asList(parameters));
    }

    @Override
    public int execute(String sql, @Nullable Collection<?> parameters) throws SQLRuntimeException {
        return doSomethingInConnection(PreparedStatement::executeUpdate, sql, parameters, true);
    }

    @Override
    public int execute(String sql, Object... parameters) throws SQLRuntimeException {
        return execute(sql, Arrays.asList(parameters));
    }

    @Override
    public List<K> executeGetIds(String sql, @Nullable Collection<?> parameters) throws SQLRuntimeException {
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
    public List<K> executeGetIds(String sql, Object... parameters) throws SQLRuntimeException {
        return executeGetIds(sql, Arrays.asList(parameters));
    }

    @Override
    @Nullable
    public K executeGetId(String sql, @Nullable Collection<?> parameters) throws SQLRuntimeException {
        return doSomethingInConnection(preparedStatement -> {
            // 执行
            int affectedRows = preparedStatement.executeUpdate();
            // 执行成功
            if (affectedRows > 0) {
                // 获取键
                ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
                generatedKeys.next();
                K key = getGeneratedKeys(generatedKeys); //得到第一个键值
                generatedKeys.close();
                return key;
            }
            return null;
        }, sql, parameters, true);
    }

    @Override
    @Nullable
    public K executeGetId(String sql, Object... parameters) throws SQLRuntimeException {
        return executeGetId(sql, Arrays.asList(parameters));
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
        // 获取连接
        Connection connection = gaarasonDataSource.getLocalConnection(isWrite);
        try {
            // 参数准备
            PreparedStatement preparedStatement = executeSql(connection, sql, localParameters);
            // 执行
            return closure.execute(preparedStatement);
        } catch (EntityNotFoundException e) {
            throw new EntityNotFoundException(String.format(sql.replace(" ? ", "\"%s\""), localParameters.toArray()));
        } catch (Throwable e) {
            throw new SQLRuntimeException(sql, localParameters, e.getMessage(),
                gaarasonDataSource.getQueryBuilder().getValueSymbol(), e);
        } finally {
            // 关闭连接
            gaarasonDataSource.localConnectionClose(connection);
        }
    }

    /**
     * 获取主键
     * @param generatedKeys 数据集
     * @return 主键
     * @throws SQLException 数据库异常
     * @throws PrimaryKeyTypeNotSupportException 不支持的主键类型
     */
    private K getGeneratedKeys(ResultSet generatedKeys) throws SQLException, PrimaryKeyTypeNotSupportException {
        Class<K> primaryKeyClass = model.getPrimaryKeyClass();
        if (Byte.class.equals(primaryKeyClass) || byte.class.equals(primaryKeyClass)) {
            return ObjectUtils.typeCast(generatedKeys.getByte(1));
        } else if (Integer.class.equals(primaryKeyClass) || int.class.equals(primaryKeyClass)) {
            return ObjectUtils.typeCast(generatedKeys.getInt(1));
        } else if (Long.class.equals(primaryKeyClass) || long.class.equals(primaryKeyClass)) {
            return ObjectUtils.typeCast(generatedKeys.getLong(1));
        } else if (String.class.equals(primaryKeyClass) || Object.class.equals(primaryKeyClass) ||
            Serializable.class.equals(primaryKeyClass)) {
            return ObjectUtils.typeCast(generatedKeys.getString(1));
        }
        throw new PrimaryKeyTypeNotSupportException(
            "Primary key type [" + primaryKeyClass + "] not support get " + "generated keys yet.");
    }

    /**
     * 执行sql, 处理jdbc结果集, 返回收集器
     * @return 收集器
     * @throws SQLRuntimeException 数据库异常
     * @throws EntityNotFoundException 查询结果为空
     */
    Record<T, K> querySql() throws SQLRuntimeException, EntityNotFoundException {
        // sql组装执行
        Grammar.SQLPartInfo sqlPartInfo = grammar.generateSql(SqlType.SELECT);

        String sql = sqlPartInfo.getSqlString();
        Collection<Object> parameterList = sqlPartInfo.getParameters();

        Map<String, Object[]> columnMap = grammar.pullWith();
        Record<T, K> theRecord = queryOrFail(sql, parameterList);
        for (Map.Entry<String, Object[]> stringEntry : columnMap.entrySet()) {
            Object[] value = stringEntry.getValue();
            theRecord.with(stringEntry.getKey(), (GenerateSqlPartFunctionalInterface) value[0],
                (RelationshipRecordWithFunctionalInterface) value[1]);
        }
        return theRecord;
    }

    /**
     * 执行sql, 处理jdbc结果集, 返回收集器
     * @return 收集器
     * @throws SQLRuntimeException 数据库异常
     * @throws EntityNotFoundException 查询结果为空
     */
    RecordList<T, K> querySqlList() throws SQLRuntimeException, EntityNotFoundException {
        // sql组装执行
        Grammar.SQLPartInfo sqlPartInfo = grammar.generateSql(SqlType.SELECT);
        String sql = sqlPartInfo.getSqlString();
        Collection<Object> parameterList = sqlPartInfo.getParameters();
        Map<String, Object[]> columnMap = grammar.pullWith();
        RecordList<T, K> records = queryList(sql, parameterList);
        for (Map.Entry<String, Object[]> stringEntry : columnMap.entrySet()) {
            records.with(stringEntry.getKey(), (GenerateSqlPartFunctionalInterface<?, ?>) stringEntry.getValue()[0],
                (RelationshipRecordWithFunctionalInterface) stringEntry.getValue()[1]);
        }
        return records;
    }

    @Override
    public void dealChunk(int num, ChunkFunctionalInterface<T, K> chunkFunctionalInterface) throws SQLRuntimeException {
        int offset = 0;
        boolean flag;
        do {
            Builder<T, K> cloneBuilder = clone();
            cloneBuilder.limit(offset, num);
            Grammar.SQLPartInfo sqlPartInfo = cloneBuilder.getGrammar().generateSql(SqlType.SELECT);
            String sql = sqlPartInfo.getSqlString();
            Collection<Object> parameterList = sqlPartInfo.getParameters();

            Map<String, Object[]> columnMap = grammar.pullWith();
            RecordList<T, K> records = queryList(sql, parameterList);
            for (Map.Entry<String, Object[]> stringEntry : columnMap.entrySet()) {
                records.with(stringEntry.getKey(), (GenerateSqlPartFunctionalInterface<?, ?>) stringEntry.getValue()[0],
                    (RelationshipRecordWithFunctionalInterface) stringEntry.getValue()[1]);
            }

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

            Grammar.SQLPartInfo sqlPartInfo = cloneBuilder.getGrammar().generateSql(SqlType.SELECT);
            String sql = sqlPartInfo.getSqlString();
            Collection<Object> parameterList = sqlPartInfo.getParameters();

            Map<String, Object[]> columnMap = grammar.pullWith();
            RecordList<T, K> records = queryList(sql, parameterList);
            for (Map.Entry<String, Object[]> stringEntry : columnMap.entrySet()) {
                records.with(stringEntry.getKey(), (GenerateSqlPartFunctionalInterface<?, ?>) stringEntry.getValue()[0],
                    (RelationshipRecordWithFunctionalInterface) stringEntry.getValue()[1]);
            }

            if (!records.isEmpty()) {
                columnValue = records.last().getMetadataMap().get(column).getValue();
            }

            flag = !records.isEmpty() && chunkFunctionalInterface.execute(records) && (records.size() == num);
        } while (flag);
    }

    /**
     * 执行sql, 返回收影响的行数
     * @return 影响的行数
     * @throws SQLRuntimeException 数据库异常
     */
    int updateSql(SqlType sqlType) throws SQLRuntimeException {
        if (sqlType != SqlType.INSERT && grammar.isEmpty(Grammar.SQLPartType.WHERE)) {
            throw new ConfirmOperationException(
                "You made a risky operation without where conditions, use where(1) " + "for sure");
        }
        // sql组装执行
        Grammar.SQLPartInfo sqlPartInfo = grammar.generateSql(sqlType);
        String sql = sqlPartInfo.getSqlString();
        Collection<Object> parameters = sqlPartInfo.getParameters();
        return execute(sql, parameters);
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
        model.log(sql, parameterList);
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
    protected void setParameter(PreparedStatement preparedStatement, int index, Object parameter) throws SQLException {
        if (parameter instanceof ParameterAndType) {
            // 精确类型
            preparedStatement.setObject(index, ((ParameterAndType) parameter).getValue(),
                ((ParameterAndType) parameter).getType());
        } else {
            // 全凭运气
            preparedStatement.setObject(index, parameter);
        }
    }

    @Override
    @Nullable
    public String conversionToString(@Nullable Object value) {
        return conversion.castNullable(value, String.class);
    }

    @Override
    public int conversionToInt(@Nullable Object value) {
        Integer integer = conversion.castNullable(value, int.class);
        return integer == null ? 0 : integer;
    }

}
