package gaarason.database.support;

import gaarason.database.annotation.ObservedBy;
import gaarason.database.appointment.EventType;
import gaarason.database.contract.eloquent.Builder;
import gaarason.database.contract.eloquent.Model;
import gaarason.database.contract.eloquent.Record;
import gaarason.database.contract.eloquent.RecordList;
import gaarason.database.contract.function.EventQueryEdFunctionalInterface;
import gaarason.database.contract.function.EventQueryIngFunctionalInterface;
import gaarason.database.contract.function.EventRecordEdFunctionalInterface;
import gaarason.database.contract.function.EventRecordIngFunctionalInterface;
import gaarason.database.contract.model.Event;
import gaarason.database.contract.model.base.RecordEvent;
import gaarason.database.contract.support.ShouldHandleEventsAfterCommit;
import gaarason.database.core.Container;
import gaarason.database.eloquent.RecordBean;
import gaarason.database.exception.AbnormalParameterException;
import gaarason.database.exception.InvalidPrimaryKeyTypeException;
import gaarason.database.lang.Nullable;
import gaarason.database.provider.ModelInstanceProvider;
import gaarason.database.util.ObjectUtils;

import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;

/**
 * 格式化后的Model信息
 */
public class ModelMember<B extends Builder<B, T, K>, T, K> extends Container.SimpleKeeper implements Serializable {

    /**
     * 记录当前线程, 当期是否需要触发事件
     */
    private static final ThreadLocal<Boolean> CALL_EVENT_FLAG = ThreadLocal.withInitial(() -> true);

    /**
     * model 类型
     */
    private final Class<? extends Model<B, T, K>> modelClass;

    /**
     * entity 类型
     */
    private final Class<T> entityClass;

    /**
     * 主键类型
     */
    private final Class<K> primaryKeyClass;

    /**
     * entity 信息
     */
    private final EntityMember<T, K> entityMember;

    /**
     * model对象
     */
    private final Model<B, T, K> model;

    /**
     * 事件触发列表
     */
    private final List<Event<B, T, K>> eventProcessors;

    public ModelMember(Container container, Class<? extends Model<B, T, K>> modelClass) {
        super(container);
        this.modelClass = modelClass;
        this.entityClass = ObjectUtils.getGenerics(modelClass, -2);
        this.primaryKeyClass = ObjectUtils.getGenerics(modelClass, -1);
        this.entityMember = new EntityMember<>(container, entityClass);

        // 一个简单的检测, 可以避免大量的问题
        typeCheck();

        // 获取模型对象 (是否是单例, 仅取决于Model实例化工厂), 但是缓存之后就是单例的了~
        this.model = container.getBean(ModelInstanceProvider.class).getModel(modelClass);
        this.eventProcessors = dealEvents();
    }

    /**
     * 触发Record的ing事件
     * @param eventType 事件类型
     * @param record 结果集
     * @return 是否继续
     */
    public boolean triggerRecordIngEvents(EventType.RecordIng eventType, Record<T, K> record) {
        switch (eventType) {
            case eventRecordCreating:
                return dealEventRecordIng(eventProcessor -> eventProcessor.eventRecordCreating(record));
            case eventRecordSaving:
                return dealEventRecordIng(eventProcessor -> eventProcessor.eventRecordSaving(record));
            case eventRecordUpdating:
                return dealEventRecordIng(eventProcessor -> eventProcessor.eventRecordUpdating(record));
            case eventRecordDeleting:
                return dealEventRecordIng(eventProcessor -> eventProcessor.eventRecordDeleting(record));
            case eventRecordForceDeleting:
                return dealEventRecordIng(eventProcessor -> eventProcessor.eventRecordForceDeleting(record));
            case eventRecordRestoring:
                return dealEventRecordIng(eventProcessor -> eventProcessor.eventRecordRestoring(record));
        }
        throw new AbnormalParameterException(eventType.name());
    }

    /**
     * 触发Record的ed事件
     * @param eventType 事件类型
     * @param record 结果集
     */
    public void triggerRecordEdEvents(EventType.RecordEd eventType, Record<T, K> record) {
        switch (eventType) {
            case eventRecordCreated:
                dealEventRecordEd(RecordEvent::eventRecordCreated, record);
                return;
            case eventRecordUpdated:
                dealEventRecordEd(RecordEvent::eventRecordUpdated, record);
                return;
            case eventRecordRetrieved:
                dealEventRecordEd(RecordEvent::eventRecordRetrieved, record);
                return;
            case eventRecordDeleted:
                dealEventRecordEd(RecordEvent::eventRecordDeleted, record);
                return;
            case eventRecordForceDeleted:
                dealEventRecordEd(RecordEvent::eventRecordForceDeleted, record);
                return;
            case eventRecordRestored:
                dealEventRecordEd(RecordEvent::eventRecordRestored, record);
                return;
            case eventRecordSaved:
                dealEventRecordEd(RecordEvent::eventRecordSaved, record);
                return;
        }
        throw new AbnormalParameterException(eventType.name());
    }


    /**
     * 触发Query的ing事件
     * @param eventType 事件类型
     * @param builder 查询构造器
     */
    public void triggerQueryIngEvents(EventType.QueryIng eventType, Builder<B, T, K> builder) {
        switch (eventType) {
            case eventQueryCreating:
                dealEventQueryIng(eventProcessor -> eventProcessor.eventQueryCreating(builder));
                return;
            case eventQueryUpdating:
                dealEventQueryIng(eventProcessor -> eventProcessor.eventQueryUpdating(builder));
                return;
            case eventQueryDeleting:
                dealEventQueryIng(eventProcessor -> eventProcessor.eventQueryDeleting(builder));
                return;
            case eventQueryForceDeleting:
                dealEventQueryIng(eventProcessor -> eventProcessor.eventQueryForceDeleting(builder));
                return;
            case eventQueryRestoring:
                dealEventQueryIng(eventProcessor -> eventProcessor.eventQueryRestoring(builder));
                return;
            case eventQueryRetrieving:
                dealEventQueryIng(eventProcessor -> eventProcessor.eventQueryRetrieving(builder));
                return;
        }
        throw new AbnormalParameterException(eventType.name());
    }

    /**
     * 触发Query的ed事件
     * @param eventType 事件类型
     * @param builder 查询构造器
     * @param rows 受影响的行数
     */
    public void triggerQueryEdEvents(EventType.QueryEd eventType, Builder<B, T, K> builder, int rows) {
        switch (eventType) {
            case eventQueryCreated:
                dealEventQueryEd((eventProcessor, b, r, rs) -> eventProcessor.eventQueryCreated(b, rows), builder, null, null);
                return;
            case eventQueryUpdated:
                dealEventQueryEd((eventProcessor, b, r, rs) -> eventProcessor.eventQueryUpdated(b, rows), builder, null, null);
                return;
            case eventQueryDeleted:
                dealEventQueryEd((eventProcessor, b, r, rs) -> eventProcessor.eventQueryDeleted(b, rows), builder, null, null);
                return;
            case eventQueryForceDeleted:
                dealEventQueryEd((eventProcessor, b, r, rs) -> eventProcessor.eventQueryForceDeleted(b, rows), builder, null, null);
                return;
            case eventQueryRestored:
                dealEventQueryEd((eventProcessor, b, r, rs) -> eventProcessor.eventQueryRestored(b, rows), builder, null, null);
                return;
        }
        throw new AbnormalParameterException(eventType.name());
    }

    /**
     * 触发Query的ed事件
     * @param eventType 事件类型
     * @param builder 查询构造器
     * @param primaryKeyValue 主键值
     */
    public void triggerQueryEdEvents(EventType.QueryEd eventType, Builder<B, T, K> builder, @Nullable K primaryKeyValue) {
        if (eventType == EventType.QueryEd.eventQueryCreated) {
            dealEventQueryEd((eventProcessor, b, r, rs) -> eventProcessor.eventQueryCreated(b, primaryKeyValue), builder, null, null);
            return;
        }
        throw new AbnormalParameterException(eventType.name());
    }

    /**
     * 触发Query的ed事件
     * @param eventType 事件类型
     * @param builder 查询构造器
     * @param primaryKeyValues 主键值列表
     */
    public void triggerQueryEdEvents(EventType.QueryEd eventType, Builder<B, T, K> builder, List<K> primaryKeyValues) {
        if (eventType == EventType.QueryEd.eventQueryCreated) {
            dealEventQueryEd((eventProcessor, b, r, rs) -> eventProcessor.eventQueryCreated(b, primaryKeyValues), builder, null, null);
            return;
        }
        throw new AbnormalParameterException(eventType.name());
    }

    /**
     * 触发Query的ed事件
     * @param eventType 事件类型
     * @param builder 查询构造器
     * @param record 查询结果集
     */
    public void triggerQueryEdEvents(EventType.QueryEd eventType, Builder<B, T, K> builder, Record<T, K> record) {
        if (eventType == EventType.QueryEd.eventQueryRetrieved) {
            dealEventQueryEd((eventProcessor, b, r, rs) -> eventProcessor.eventQueryRetrieved(b, r), builder, record, null);
            return;
        }
        throw new AbnormalParameterException(eventType.name());
    }

    /**
     * 触发Query的ed事件
     * @param eventType 事件类型
     * @param builder 查询构造器
     * @param records 查询结果集集合
     */
    public void triggerQueryEdEvents(EventType.QueryEd eventType, Builder<B, T, K> builder, RecordList<T, K> records) {
        if (eventType == EventType.QueryEd.eventQueryRetrieved) {
            dealEventQueryEd((eventProcessor, b, r, rs) -> eventProcessor.eventQueryRetrieved(b, rs), builder, null, records);
            return;
        }
        throw new AbnormalParameterException(eventType.name());
    }

    /**
     * 一个简单的检测, 可以避免大量的问题
     */
    private void typeCheck() {
        PrimaryKeyMember<K> primaryKeyMember = entityMember.getPrimaryKeyMember();

        // 主键类型检测( 实体上的主键的类型是否与模型上的泛型一致)
        if (primaryKeyMember != null &&
            !primaryKeyMember.getFieldMember().getField().getType().equals(primaryKeyClass)) {
            throw new InvalidPrimaryKeyTypeException(
                "The primary key type [" + primaryKeyMember.getFieldMember().getField().getType() +
                    "] of the entity does not match with the generic [" + primaryKeyClass + "]");
        }
    }

    /**
     * 处理注解 @ObservedBy, 得到`事件触发列表`
     * @return 事件触发列表
     */
    private List<Event<B, T, K>> dealEvents() {
        // 获取类定义上的注解, 会找寻其父类上的注解
        ObservedBy annotationsObservedBy = modelClass.getAnnotation(ObservedBy.class);

        // 不存在注解时, 即等价于 @ObservedBy(自身)
        if (ObjectUtils.isEmpty(annotationsObservedBy)) {
            return Collections.singletonList(model);
        }

        // 注解中指定的事件处理器`类型`列表
        Class<? extends Event<?, ?, ?>>[] eventClassArray = annotationsObservedBy.value();

        // 注解中指定的事件处理器`实例`列表
        LinkedList<Event<B, T, K>> eventInstanceList = new LinkedList<>();

        // 逐个实例化 - 单例
        for (Class<? extends Event<?, ?, ?>> event : eventClassArray) {
            eventInstanceList.add(ObjectUtils.typeCast(container.getBean(event)));
        }
        return eventInstanceList;
    }

    /**
     * 是否跳过事件
     * @return 跳过事件
     */
    private static boolean shouldNotCallEvent() {
        return !CALL_EVENT_FLAG.get();
    }

    /**
     * 不触发事件的情况下, 执行目标业务逻辑
     * @param supplier 业务逻辑
     * @return 响应
     * @param <T> 响应类型
     */
    public static <T> T quiet(Supplier<T> supplier) {
        // 初始状态记录
        Boolean originalState = CALL_EVENT_FLAG.get();
        // 设置静默
        CALL_EVENT_FLAG.set(false);
        try {
            return supplier.get();
        } finally {
            // 还原
            CALL_EVENT_FLAG.set(originalState);
        }
    }

    /**
     * 触发Record的ing事件
     * @param supplier 具体事件
     */
    protected boolean dealEventRecordIng(EventRecordIngFunctionalInterface<B, T, K> supplier) {
        // 是否触发事件
        if (shouldNotCallEvent()) {
            // 直接通过
            return true;
        }
        for (Event<B, T, K> eventProcessor : eventProcessors) {
            if (!supplier.execute(eventProcessor)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 触发Record的ed事件
     * @param supplier 具体事件
     * @param record 结果集
     */
    protected void dealEventRecordEd(EventRecordEdFunctionalInterface<T, K> supplier, Record<T, K> record) {
        // 是否触发事件
        if (shouldNotCallEvent()) {
            return;
        }
        // 是否仍在事务中
        boolean inTransaction = model.getGaarasonDataSource().isLocalThreadInTransaction();

        for (Event<?, T, K> eventProcessor : eventProcessors) {
            // 事务中 且 要求事务提交后再触发事件
            if (inTransaction && eventProcessor instanceof ShouldHandleEventsAfterCommit) {
                // 快照参数
                Record<T, K> recordCopy = new RecordBean<>(record);
                // 加入"待触发事件队列"
                model.getGaarasonDataSource().addEvent(() -> {
                    // 调用
                    supplier.execute(eventProcessor, recordCopy);

                });
            }
            // 直接触发
            else {
                supplier.execute(eventProcessor, record);
            }
        }
    }

    /**
     * 触发Query的ing事件
     * @param supplier 具体事件
     */
    protected void dealEventQueryIng(EventQueryIngFunctionalInterface<B, T, K> supplier) {
        // 是否触发事件
        if (shouldNotCallEvent()) {
            return;
        }
        for (Event<B, T, K> eventProcessor : eventProcessors) {
            supplier.execute(eventProcessor);
        }
    }

    /**
     * 触发Query的ed事件
     * @param supplier 具体事件
     * @param builder 查询构造器
     * @param record 结果集
     * @param records 结果集集合
     */
    protected void dealEventQueryEd(EventQueryEdFunctionalInterface<B, T, K> supplier, Builder<B, T, K> builder,
            @Nullable Record<T, K> record, @Nullable RecordList<T, K> records) {
        // 是否触发事件
        if (shouldNotCallEvent()) {
            return;
        }

        // 是否仍在事务中
        boolean inTransaction = model.getGaarasonDataSource().isLocalThreadInTransaction();

        for (Event<B, T, K> eventProcessor : eventProcessors) {
            // 事务中 且 要求事务提交后再触发事件
            if (inTransaction && eventProcessor instanceof ShouldHandleEventsAfterCommit) {
                // 快照参数
                B builderCopy = builder.clone();
                Record<T, K> recordCopy = record != null ? new RecordBean<>(record) : null;
                RecordList<T, K> recordListCopy = records != null ? RecordFactory.copyRecordList(records) : null;

                // 加入"待触发事件队列"
                model.getGaarasonDataSource().addEvent(() -> {
                    // 调用
                    supplier.execute(eventProcessor, builderCopy, recordCopy, recordListCopy);

                });
            }
            // 直接触发
            else {
                supplier.execute(eventProcessor, builder, record, records);
            }
        }
    }

    // ---------------------------- simple getter ---------------------------- //

    public Class<? extends Model<?, T, K>> getModelClass() {
        return modelClass;
    }

    public Class<T> getEntityClass() {
        return entityClass;
    }

    public EntityMember<T, K> getEntityMember() {
        return entityMember;
    }

    public Class<K> getPrimaryKeyClass() {
        return primaryKeyClass;
    }

    public Model<?, T, K> getModel() {
        return model;
    }

    public List<Event<B, T, K>> getEventProcessors() {
        return eventProcessors;
    }
}
