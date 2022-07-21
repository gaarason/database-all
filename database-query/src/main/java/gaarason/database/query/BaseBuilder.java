package gaarason.database.query;

import gaarason.database.appointment.FinalVariable;
import gaarason.database.config.ConversionConfig;
import gaarason.database.contract.connection.GaarasonDataSource;
import gaarason.database.contract.eloquent.Builder;
import gaarason.database.contract.eloquent.Model;
import gaarason.database.contract.function.ColumnFunctionalInterface;
import gaarason.database.contract.function.GenerateSqlPartFunctionalInterface;
import gaarason.database.contract.function.RelationshipRecordWithFunctionalInterface;
import gaarason.database.contract.function.TransactionFunctionalInterface;
import gaarason.database.contract.query.Grammar;
import gaarason.database.core.Container;
import gaarason.database.exception.AbnormalParameterException;
import gaarason.database.exception.CloneNotSupportedRuntimeException;
import gaarason.database.exception.SQLRuntimeException;
import gaarason.database.lang.Nullable;
import gaarason.database.provider.ModelShadowProvider;
import gaarason.database.util.ExceptionUtils;

/**
 * 基础查询构造器(sql生成器)
 * @param <T>
 * @param <K>
 * @author xt
 */
public abstract class BaseBuilder<T, K> implements Builder<T, K> {

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
        return modelShadowProvider.parseFieldNameByLambdaWithCache(column);
    }

    @Override
    public String lambda2ColumnName(ColumnFunctionalInterface<T> column) {
        return modelShadowProvider.parseColumnNameByLambdaWithCache(column);
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
