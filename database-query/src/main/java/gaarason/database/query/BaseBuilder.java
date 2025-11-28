package gaarason.database.query;

import gaarason.database.appointment.AggregatesType;
import gaarason.database.appointment.FinalVariable;
import gaarason.database.config.ConversionConfig;
import gaarason.database.contract.connection.GaarasonDataSource;
import gaarason.database.contract.eloquent.Builder;
import gaarason.database.contract.eloquent.Model;
import gaarason.database.contract.eloquent.Record;
import gaarason.database.contract.function.BuilderAnyWrapper;
import gaarason.database.contract.function.ColumnFunctionalInterface;
import gaarason.database.contract.function.RecordWrapper;
import gaarason.database.contract.query.Grammar;
import gaarason.database.core.Container;
import gaarason.database.exception.AbnormalParameterException;
import gaarason.database.exception.CloneNotSupportedRuntimeException;
import gaarason.database.exception.SQLRuntimeException;
import gaarason.database.lang.Nullable;
import gaarason.database.provider.GodProvider;
import gaarason.database.provider.ModelShadowProvider;
import gaarason.database.support.FieldRelationMember;
import gaarason.database.support.ModelMember;
import gaarason.database.util.ClassUtils;
import gaarason.database.util.ExceptionUtils;
import gaarason.database.util.ObjectUtils;
import gaarason.database.util.StringUtils;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

/**
 * 基础查询构造器(sql生成器)
 * @param <T>
 * @param <K>
 * @author xt
 */
abstract class BaseBuilder<B extends Builder<B, T, K>, T, K> implements Builder<B, T, K> {

    /**
     * 数据库连接
     */
    protected GaarasonDataSource gaarasonDataSource;

    /**
     * 数据模型
     */
    protected Model<B, T, K> model;

    /**
     * modelMember
     */
    protected ModelMember<B, T, K> modelMember;

    /**
     * 容器
     */
    protected Container container;

    /**
     * Model信息大全
     */
    protected ModelShadowProvider modelShadowProvider;

    /**
     * 类型转化
     */
    protected ConversionConfig conversion;

    /**
     * 数据实体类
     */
    protected Class<T> entityClass;

    /**
     * sql生成器
     */
    protected Grammar grammar;

    public BaseBuilder() {

    }

    @Override
    public B initBuilder(GaarasonDataSource gaarasonDataSource, Model<B, T, K> model, Grammar grammar) {
        this.gaarasonDataSource = gaarasonDataSource;
        this.container = gaarasonDataSource.getContainer();
        this.modelShadowProvider = container.getBean(ModelShadowProvider.class);
        this.conversion = container.getBean(ConversionConfig.class);
        this.model = model;
        this.modelMember = modelShadowProvider.get(model);
        this.entityClass = model.getEntityClass();
        this.grammar = grammar;
        return getSelf();
    }

    @Override
    public Grammar getGrammar() {
        return grammar;
    }

    @Override
    public void setGrammar(Grammar grammar) {
        this.grammar = grammar;
    }

    @Override
    public void mergerGrammar(Grammar grammar) {
        this.grammar.merger(grammar);
    }

    @Override
    public B getNewSelf() {
        return model.newQuery();
    }

    @Override
    public B getNewSelfWithoutApply() {
        return model.newQueryWithoutApply();
    }

    @Override
    public B clear(Grammar.SQLPartType sqlPartType) {
        grammar.clear(sqlPartType);
        return getSelf();
    }

    @Override
    public String lambda2FieldName(ColumnFunctionalInterface<?, ?> column) {
        return modelShadowProvider.parseFieldNameByLambdaWithCache(column);
    }

    @Override
    public String lambda2ColumnName(ColumnFunctionalInterface<?, ?> column) {
        return modelShadowProvider.parseColumnNameByLambdaWithCache(column);
    }

    /**
     * 克隆当前查询构造器
     * @return 查询构造器
     * @throws CloneNotSupportedRuntimeException 克隆异常
     */
    @Override
    @SuppressWarnings("unchecked")
    public B clone() throws CloneNotSupportedRuntimeException {
        try {
            // 浅拷贝
            Builder<B, T, K> builder = (Builder<B, T, K>) super.clone();
            // 深拷贝
            builder.setGrammar(grammar.deepCopy());
            return ObjectUtils.typeCast(builder);
        } catch (CloneNotSupportedException e) {
            throw new CloneNotSupportedRuntimeException(e.getMessage(), e);
        }
    }

    /**
     * 渴求式关联
     * @param fieldNames 所关联的Model(当前模块的属性名)
     * @return 关联的Model的查询构造器
     */
    @Override
    public B with(String... fieldNames) {
        for (String fieldName : fieldNames) {
            with(fieldName, BuilderAnyWrapper.empty(), RecordWrapper.empty());
        }
        return getSelf();
    }

    @Override
    public B withAll(String... withoutFieldName) {
        Set<String> withoutFieldNameSet = new HashSet<>(Arrays.asList(withoutFieldName));
        Map<String, FieldRelationMember> map = modelMember.getEntityMember().getRelationFieldMap();
        for (FieldRelationMember member : map.values()) {
            String fieldName = member.getName();
            if (!withoutFieldNameSet.contains(fieldName)) {
                with(fieldName, BuilderAnyWrapper.empty(), RecordWrapper.empty());
            }
        }
        return getSelf();
    }


    /**
     * 渴求式关联
     * @param fieldName 所关联的Model(当前模块的属性名)
     * @param builderClosure 所关联的Model的查询构造器约束
     * @return 关联的Model的查询构造器
     */
    @Override
    public B with(String fieldName, BuilderAnyWrapper builderClosure) {
        return with(fieldName, builderClosure, RecordWrapper.empty());
    }

    /**
     * 渴求式关联
     * @param fieldName 所关联的Model(当前模块的属性名)
     * @param builderClosure 所关联的Model的查询构造器约束
     * @param recordClosure 所关联的Model的再一级关联
     * @return 关联的Model的查询构造器
     */
    @Override
    public B with(String fieldName, BuilderAnyWrapper builderClosure, RecordWrapper recordClosure) {
        grammar.pushRelation(fieldName, new Record.Relation(fieldName, builderClosure, recordClosure));
        return getSelf();
    }

    @Override
    public B withOperation(String fieldName, BuilderAnyWrapper operationBuilder, BuilderAnyWrapper customBuilder, String alisaFieldName) {
        grammar.pushRelation(alisaFieldName, new Record.Relation(fieldName, operationBuilder, customBuilder, RecordWrapper.empty()));
        return getSelf();
    }

    @Override
    public B withAggregate(AggregatesType op, String fieldName, String column,
            BuilderAnyWrapper customBuilder, @Nullable String alisaFieldName) {

        // 别名
        String alisaField = alisaFieldName != null ? alisaFieldName : StringUtils.lineToHump(fieldName + "_" + op + "_" + column);

        // 操作查询构造器
        BuilderAnyWrapper operationBuilder = builder -> builder.selectFunction(op.toString(), column, alisaField);

        return withOperation(fieldName, operationBuilder, customBuilder, alisaField);
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
    public <V> V quiet(Supplier<V> supplier) {
        return modelMember.quiet(supplier);
    }

    @Override
    public void quiet(Runnable supplier) {
        quiet(() -> {
            supplier.run();
            return true;
        });
    }

    @Override
    public <V> V transaction(Supplier<V> closure) {
        return transaction(closure, FinalVariable.DEFAULT_CAUSED_BY_DEADLOCK_RETRY_COUNT);
    }

    @Override
    public <V> CompletableFuture<V> transactionAsync(Supplier<V> closure) {
        return transactionAsync(closure, FinalVariable.DEFAULT_CAUSED_BY_DEADLOCK_RETRY_COUNT);
    }

    @Override
    public <V> V transaction(Supplier<V> closure, int maxAttempts) {
        for (int currentAttempt = 0; currentAttempt <= maxAttempts; currentAttempt++) {
            begin();
            try {
                V result = closure.get();
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
    public <V> CompletableFuture<V> transactionAsync(Supplier<V> closure, int maxAttempts) {
        return CompletableFuture.supplyAsync(() -> transaction(closure, maxAttempts), model.getExecutorService());
    }

    @Override
    public void transaction(Runnable closure) {
        transaction(closure, FinalVariable.DEFAULT_CAUSED_BY_DEADLOCK_RETRY_COUNT);
    }

    @Override
    public CompletableFuture<Boolean> transactionAsync(Runnable closure) {
        return transactionAsync(closure, FinalVariable.DEFAULT_CAUSED_BY_DEADLOCK_RETRY_COUNT);
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
    public CompletableFuture<Boolean> transactionAsync(Runnable closure, int maxAttempts) {
        return CompletableFuture.supplyAsync(() -> {
            transaction(closure, maxAttempts);
            return true;
        }, model.getExecutorService());
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

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        String identification = container.getIdentification();
        out.writeUTF(identification);
        out.writeUTF(model.getClass().getName());
        out.writeObject(grammar);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        String identification = in.readUTF();
        String modelName = in.readUTF();
        Object grammar = in.readObject();

        Container container = GodProvider.get(identification);
        Class<?> modelClass = ClassUtils.forName(modelName);
        Model<?, ?, ?> model = container.getBean(ModelShadowProvider.class)
            .getByModelClass(ObjectUtils.typeCast(modelClass))
            .getModel();
        GaarasonDataSource gaarasonDataSource = model.getGaarasonDataSource();

        initBuilder(gaarasonDataSource, ObjectUtils.typeCast(model), ObjectUtils.typeCast(grammar));
    }
}
