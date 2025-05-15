package gaarason.database.eloquent;

import gaarason.database.appointment.EntityUseType;
import gaarason.database.appointment.EventType;
import gaarason.database.appointment.ValueWrapper;
import gaarason.database.contract.eloquent.Model;
import gaarason.database.contract.eloquent.Record;
import gaarason.database.contract.eloquent.extra.Bind;
import gaarason.database.contract.function.BuilderAnyWrapper;
import gaarason.database.contract.function.ColumnFunctionalInterface;
import gaarason.database.contract.function.RecordWrapper;
import gaarason.database.core.Container;
import gaarason.database.eloquent.record.BindBean;
import gaarason.database.exception.EntityAttributeInvalidException;
import gaarason.database.exception.PrimaryKeyNotFoundException;
import gaarason.database.lang.Nullable;
import gaarason.database.provider.GodProvider;
import gaarason.database.provider.ModelShadowProvider;
import gaarason.database.support.*;
import gaarason.database.util.ClassUtils;
import gaarason.database.util.EntityUtils;
import gaarason.database.util.ObjectUtils;
import gaarason.database.util.StringUtils;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.*;

/**
 * 结果集对象
 * @author xt
 */
public class RecordBean<T, K> implements Record<T, K> {

    private static final long serialVersionUID = 1L;

    /**
     * 本表元数据
     * <数据库字段名 -> 字段信息>
     */
    protected  Map<String, Object> metadataMap = new HashMap<>(16);

    /**
     * 历史元数据
     * 不管从检索起模型是否发生了任何变化, 它都不变, 除非调用 xx 方法, 主动刷新
     * <数据库字段名 -> 字段信息>
     */
    protected  Map<String, Object> originalMetadataMap = new HashMap<>(16);

    /**
     * 数据模型
     */
    protected Model<?, T, K> model;

    /**
     * Model信息大全
     */
    protected ModelShadowProvider modelShadow;

    /**
     * 数据实体类
     */
    protected Class<T> entityClass;

    /**
     * modelMember
     */
    protected ModelMember<?, T, K> modelMember;

    /**
     * 原Sql
     */
    protected String originalSql = "";

    /**
     * 数据实体
     */
    protected T entity;

    /**
     * 是否已经绑定具体的数据
     */
    protected boolean hasBind;

    /**
     * 关联关系MAP
     * MAP< 待赋值字段 -> 关联关系信息（关联关系属性，查询构造器包装，查询结果集包装）>
     */
    protected Map<String, Relation> relationMap = new HashMap<>();

    /**
     * 主键值
     */
    @Nullable
    protected K originalPrimaryKeyValue;

    /**
     * 仅反序列化时使用
     */
    public RecordBean() {

    }

    /**
     * 根据查询结果集生成
     * @param model 数据模型
     * @param stringObjectMap 元数据
     */
    public RecordBean(Model<?, T, K> model, Map<String, Object> stringObjectMap, String originalSql) {
        initNewRecord(model, stringObjectMap, originalSql);
    }

    /**
     * 凭空生成
     * @param model 数据模型
     */
    public RecordBean(Model<?, T, K> model) {
        initRecord(model, Collections.emptyMap(), "");
    }

    /**
     * 由已存在的 Record 生成
     * @param recordBean 已经完成初始化的 Record
     */
    public RecordBean(Record<T, K> recordBean) {
        initRecord(recordBean.getModel(), recordBean.getMetadataMap(), recordBean.getOriginalSql());
    }

    protected void initRecord(Model<?, T, K> model, Map<String, Object> stringObjectMap, String originalSql) {
        this.entityClass = model.getEntityClass();
        this.model = model;
        this.modelShadow = model.getGaarasonDataSource().getContainer().getBean(ModelShadowProvider.class);
        this.modelMember = modelShadow.get(model);
        this.originalSql = originalSql;
        init(stringObjectMap);
        hasBind = !metadataMap.isEmpty();
        this.entity = toObjectWithoutRelationship();
    }

    protected void initNewRecord(Model<?, T, K> model, Map<String, Object> stringObjectMap, String originalSql) {
        initRecord(model, stringObjectMap, originalSql);
        // 通知
        modelMember.triggerRecordEdEvents(EventType.RecordEd.eventRecordRetrieved, this);
    }

    /**
     * 初始化数据
     * @param stringObjectMap 元数据
     */
    protected void init(Map<String, Object> stringObjectMap) {
        // 如果不是统同一个(引用相同)对象, 则手动赋值下
        if (metadataMap != stringObjectMap) {
            metadataMap.clear();
            metadataMap.putAll(stringObjectMap);
        }
        if (originalMetadataMap != stringObjectMap) {
            originalMetadataMap.clear();
            originalMetadataMap.putAll(stringObjectMap);
        }
    }

    @Override
    public String lambda2FieldName(ColumnFunctionalInterface<?, ?> column) {
        return modelShadow.parseFieldNameByLambdaWithCache(column);
    }

    @Override
    public String lambda2ColumnName(ColumnFunctionalInterface<?, ?> column) {
        return modelShadow.parseColumnNameByLambdaWithCache(column);
    }

    @Override
    public Map<String, Object> getMetadataMap() {
        return metadataMap;
    }

    @Override
    public boolean isHasBind() {
        return hasBind;
    }

    @Override
    public Model<?, T, K> getModel() {
        return model;
    }

    @Override
    public String getOriginalSql() {
        return originalSql;
    }

    @Override
    public T getEntity() {
        return entity;
    }

    @Override
    public T getEntity(T entity) {
        this.entity = entity;
        return this.entity;
    }

    @Override
    public Record<T, K> fillEntity(T entity) {
        // 合并属性
        EntityUtils.entityMergeReference(this.entity, entity, true);
        return this;
    }

    @Override
    @Nullable
    public K getOriginalPrimaryKeyValue() {
        return originalPrimaryKeyValue;
    }

    @Override
    public void setOriginalPrimaryKeyValue(K value) {
        originalPrimaryKeyValue = value;
    }

    @Override
    public Map<String, Relation> getRelationMap() {
        return relationMap;
    }


    /**
     * 将元数据map转化为普通map
     * @return 普通map
     */
    @Override
    public Map<String, Object> toMap() {
        return new LinkedHashMap<>(metadataMap);
    }

    /**
     * 元数据转String
     * @return eg:age=16&name=alice&sex=
     */
    @Override
    public String toSearch() {
        Map<String, Object> stringObjectMap = toMap();
        return StringUtils.mapToQuerySearch(stringObjectMap, false);
    }

    /**
     * 转化为对象
     * @return 对象
     */
    @Override
    public T toObject() {
        RelationGetSupport<T, K> tkRelationGetSupport = new RelationGetSupport<>(
            model.getGaarasonDataSource().getContainer(), this, true);
        return tkRelationGetSupport.toObject();

    }

    /**
     * 元数据转实体对象
     * @return 实体对象
     */
    @Override
    public T toObjectWithoutRelationship() {
        RelationGetSupport<T, K> tkRelationGetSupport = new RelationGetSupport<>(
            model.getGaarasonDataSource().getContainer(), this, false);
        return tkRelationGetSupport.toObject();
    }

    @Override
    public <V> V toObject(Class<V> clazz) {
        return modelShadow.entityAssignment(clazz, this);
    }

    @Override
    public Record<T, K> withClear() {
        relationMap.clear();
        return this;
    }

    @Override
    public Record<T, K> with(String fieldName) {
        return with(fieldName, builder -> builder);
    }

    @Override
    public Record<T, K> with(String fieldName, BuilderAnyWrapper builderClosure) {
        return with(fieldName, builderClosure, theRecord -> theRecord);
    }

    @Override
    public Record<T, K> with(String fieldName, BuilderAnyWrapper builderClosure,
        RecordWrapper recordClosure) {

        String[] columnArr = fieldName.split("\\.");
        // 快捷类型
        if (columnArr.length > 1) {
            String lastLevelColumn = columnArr[columnArr.length - 1];
            String otherLevelColumn = fieldName.substring(0, fieldName.length() - (lastLevelColumn.length() + 1));
            return with(otherLevelColumn, builder -> builder,
                thrRecord -> thrRecord.with(lastLevelColumn, ObjectUtils.typeCast(builderClosure), recordClosure));
        }

        relationMap.put(fieldName, new Relation(fieldName, builderClosure, recordClosure));

        return this;
    }

    @Override
    public Bind bind(String fieldName) {
        return new BindBean<>(this, fieldName);
    }

    /**
     * 新增或者更新
     * 新增情况下: saving -> creating -> created -> saved
     * 更新情况下: saving -> updating -> updated -> saved
     * @return 执行成功
     */
    @Override
    public boolean save() {
        // 事件阻止
        if (!modelMember.triggerRecordIngEvents(EventType.RecordIng.eventRecordSaving, this)) {
            return false;
        }

        // 执行
        boolean success = hasBind ? update() : insert();
        // 事件通知
        modelMember.triggerRecordEdEvents(EventType.RecordEd.eventRecordSaved, this);
        // 响应
        return success;
    }

    @Override
    public boolean saveByPrimaryKey() {
        // 事件阻止
        if (!modelMember.triggerRecordIngEvents(EventType.RecordIng.eventRecordSaving, this)) {
            return false;
        }
        boolean success = false;
        boolean flag = false;
        // 兼容主键为nul, 且有效的情况
        PrimaryKeyMember<?> primaryKeyMember = modelShadow.parseAnyEntityWithCache(entityClass).getPrimaryKeyMember();
        if (primaryKeyMember != null) {
            // 获取主键值
            ValueWrapper<?> valueWrapper = primaryKeyMember.getFieldMember()
                .fieldFillGet(entity, EntityUseType.CONDITION, false);
            if (valueWrapper.isValid()) {
                flag = true;
                success = updateByPrimaryKey(valueWrapper.getValue());
            }
        }
        if (!flag) {
            success = insert();
        }

        // 事件通知
        modelMember.triggerRecordEdEvents(EventType.RecordEd.eventRecordSaved, this);
        // 响应
        return success;
    }

    @Override
    public boolean delete() {
        // 主键未知
        if (originalPrimaryKeyValue == null) {
            throw new PrimaryKeyNotFoundException();
        }
        // 事件阻止
        if (!modelMember.triggerRecordIngEvents(EventType.RecordIng.eventRecordDeleting, this)) {
            return false;
        }
        // 执行
        boolean success =
            model.newQueryWithoutApply().where(model.getPrimaryKeyColumnName(), originalPrimaryKeyValue.toString()).delete() > 0;
        // 成功删除后后,刷新自身属性
        if (success) {
            this.metadataMap.clear();
            entity = toObjectWithoutRelationship();
            hasBind = false;
            // 通知
            modelMember.triggerRecordEdEvents(EventType.RecordEd.eventRecordDeleted, this);
        }
        // 响应
        return success;
    }

    @Override
    public boolean forceDelete() {
        // 主键未知
        if (originalPrimaryKeyValue == null) {
            throw new PrimaryKeyNotFoundException();
        }
        // 事件阻止
        if (!modelMember.triggerRecordIngEvents(EventType.RecordIng.eventRecordForceDeleting, this)) {
            return false;
        }
        // 执行
        boolean success =
                model.newQueryWithoutApply().where(model.getPrimaryKeyColumnName(), originalPrimaryKeyValue.toString()).forceDelete() > 0;
        // 成功删除后后,刷新自身属性
        if (success) {
            this.metadataMap.clear();
            entity = toObjectWithoutRelationship();
            hasBind = false;
            // 通知
            modelMember.triggerRecordEdEvents(EventType.RecordEd.eventRecordForceDeleted, this);
        }
        // 响应
        return success;
    }

    /**
     * 恢复(成功恢复后将会刷新record)
     * restoring -> restored
     * @return 执行成功
     */
    @Override
    public boolean restore() {
        return restore(true);
    }

    /**
     * 恢复
     * restoring -> restored
     * @param refresh 是否刷新自身
     * @return 执行成功
     */
    @Override
    public boolean restore(boolean refresh) {
        // 主键未知
        if (originalPrimaryKeyValue == null) {
            throw new PrimaryKeyNotFoundException();
        }
        // 阻止
        if (!modelMember.triggerRecordIngEvents(EventType.RecordIng.eventRecordRestoring, this)) {
            return false;
        }
        // 执行
        boolean success =
            model.onlyTrashed().where(model.getPrimaryKeyColumnName(), originalPrimaryKeyValue.toString()).restore() >
                0;
        // 成功恢复后,刷新自身属性
        if (success) {
            if (refresh) {
                refresh();
            }
            modelMember.triggerRecordEdEvents(EventType.RecordEd.eventRecordRestored, this);
        }
        // 响应
        return success;
    }

    @Override
    public Record<T, K> fresh() {
        // 主键未知
        if (originalPrimaryKeyValue == null) {
            throw new PrimaryKeyNotFoundException();
        }

        return model.withTrashed()
                .where(model.getPrimaryKeyColumnName(), originalPrimaryKeyValue.toString())
                .firstOrFail();
    }

    /**
     * 刷新(重新从数据库获取)
     * retrieved
     * @return 执行成功
     */
    @Override
    public Record<T, K> refresh() {
        // 主键未知
        if (originalPrimaryKeyValue == null) {
            throw new PrimaryKeyNotFoundException();
        }

        Map<String, Object> theMetadataMap = fresh().getMetadataMap();

        // 刷新自身属性
        return refresh(theMetadataMap);
    }

    @Override
    public Record<T, K> refresh(Map<String, Object> metadataMap) {
        init(metadataMap);
        this.entity = toObjectWithoutRelationship();
        if (!metadataMap.isEmpty()) {
            hasBind = true;
            // 通知
            model.eventRecordRetrieved(this);
        } else {
            hasBind = false;
        }
        return this;
    }

    @Override
    public boolean isDirty() {
        return !ObjectUtils.isEmpty(getDirtyMap());
    }

    @Override
    public Map<String, Object> getDirtyMap() {
        EntityMember<T, K> entityMember = modelShadow.parseAnyEntityWithCache(entityClass);

        Map<String, Object> theMap = new HashMap<>(16);
        Map<String, FieldMember<?>> columnFieldMap = entityMember.getColumnFieldMap();
        // 逐个比较, 注意null的处理
        for (Map.Entry<String, FieldMember<?>> entry : columnFieldMap.entrySet()) {
            FieldMember<?> fieldMember = entry.getValue();
            final String columnName = fieldMember.getColumnName();
            // 元数据中的值
            final Object valueInMetadataMap = metadataMap.get(columnName);
            // entity中的值
            final Object valueInEntity = fieldMember.fieldGet(entity);
            // 如果不相等,则加入返回对象对象
            if (!ObjectUtils.nullSafeEquals(valueInMetadataMap, valueInEntity)) {
                theMap.put(columnName, valueInEntity);
            }
        }
        return theMap;
    }

    @Override
    public boolean isDirty(String... fieldNames) {
        EntityMember<T, K> entityMember = modelShadow.parseAnyEntityWithCache(entityClass);
        // 获取所有变更属性组成的map
        final Map<String, Object> dirtyMap = getDirtyMap();
        for (String fieldName : fieldNames) {
            String columnName = entityMember.getFieldMemberByFieldName(fieldName).getColumnName();
            if(dirtyMap.containsKey(columnName)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isDirty(Collection<String> fieldNames) {
        return isDirty(fieldNames.toArray(new String[0]));
    }

    @Override
    public boolean isClean() {
        return !isDirty();
    }

    @Override
    public boolean isClean(String... fieldNames) {
        return !isDirty(fieldNames);
    }

    @Override
    public boolean isClean(Collection<String> fieldNames) {
        return !isDirty(fieldNames);
    }

    @Override
    public boolean wasChanged() {
        EntityMember<T, K> entityMember = modelShadow.parseAnyEntityWithCache(entityClass);
        Map<String, FieldMember<?>> columnFieldMap = entityMember.getColumnFieldMap();
        for (Map.Entry<String, FieldMember<?>> entry : columnFieldMap.entrySet()) {
            Object originalValue = originalMetadataMap.get(entry.getKey());
            Object value = metadataMap.get(entry.getKey());
            if(!ObjectUtils.nullSafeEquals(originalValue, value)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean wasChanged(String... fieldNames) {
        EntityMember<T, K> entityMember = modelShadow.parseAnyEntityWithCache(entityClass);
        for (String fieldName : fieldNames) {
            String columnName = entityMember.getFieldMemberByFieldName(fieldName).getColumnName();
            Object originalValue = originalMetadataMap.get(columnName);
            Object value = metadataMap.get(columnName);
            if(!ObjectUtils.nullSafeEquals(originalValue, value)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean wasChanged(Collection<String> fieldNames) {
        return wasChanged(fieldNames.toArray(new String[0]));
    }

    @Override
    public T getOriginal() {
        EntityMember<T, K> entityMember = modelMember.getEntityMember();
        return entityMember.toEntity(originalMetadataMap);
    }

    @Override
    @Nullable
    public Object getOriginal(String fieldName) throws EntityAttributeInvalidException {
        FieldMember<?> fieldMember = modelMember.getEntityMember()
            .getFieldMemberByFieldName(fieldName);

        // 从元数据中获取字段值
        return originalMetadataMap.get(fieldMember.getColumnName());
    }

    /**
     * 新增
     * 事件使用 creating created
     * @return 执行成功
     */
    protected boolean insert() {
        // 阻止
        if (!modelMember.triggerRecordIngEvents(EventType.RecordIng.eventRecordCreating, this)) {
            return false;
        }
        // entity 2 map
        Map<String, Object> entityMap = modelShadow.entityToMap(entity, EntityUseType.INSERT);
        // 执行并, 返回主键
        K primaryKeyValue = model.newQuery().value(entityMap).insertGetId();

        boolean success = primaryKeyValue != null;
        // 成功插入后,刷新自身属性
        if (success) {
            // 主键准备
            entityMap.put(model.getPrimaryKeyColumnName(), primaryKeyValue);
            // 更新自身
            selfUpdate(entityMap);
            // 新增的场景下, 对 originalMetadataMap 进行更新
            originalMetadataMap.putAll(entityMap);
            // 通知
            modelMember.triggerRecordEdEvents(EventType.RecordEd.eventRecordCreated, this);
        }
        // 响应
        return success;
    }

    /**
     * 更新
     * 事件使用 updating updated
     * @return 执行成功
     */
    protected boolean update() {
        // 主键未知
        if (originalPrimaryKeyValue == null) {
            throw new PrimaryKeyNotFoundException();
        }

        return updateByPrimaryKey(originalPrimaryKeyValue);
    }

    /**
     * 更新
     * 事件使用 updating updated
     * @param primaryKeyValue 主键值
     * @return 执行成功
     */
    protected boolean updateByPrimaryKey(@Nullable Object primaryKeyValue) {
        // 阻止
        if (!modelMember.triggerRecordIngEvents(EventType.RecordIng.eventRecordUpdating, this)) {
            return false;
        }
        // entity 2 map
        Map<String, Object> entityMap = modelShadow.entityToMap(entity, EntityUseType.UPDATE);
        // 执行
        boolean success =
            model.newQueryWithoutApply().where(model.getPrimaryKeyColumnName(), primaryKeyValue).data(entityMap).update() > 0;
        // 成功更新后,刷新自身属性
        if (success) {
            // 更新自身
            selfUpdate(entityMap);
            // aop通知
            modelMember.triggerRecordEdEvents(EventType.RecordEd.eventRecordUpdated, this);
        }
        // 响应
        return success;
    }

    /**
     * 更新自身数据
     * @param entityMap 新的实体所对应的MAP
     */
    protected void selfUpdate(Map<String, Object> entityMap) {
        // 更新元数据
        selfUpdateMetadataMap(entityMap);
        // 更新相关对象
        // 这一步操作可以剔除无效的字段
        T entity = toObjectWithoutRelationship();
        // 保持引用不变
        EntityUtils.entityMergeReference(this.entity, entity, false);
    }

    /**
     * 更新元数据
     * @param entityMap 新的实体所对应的MAP
     */
    protected void selfUpdateMetadataMap(Map<String, Object> entityMap) {
        metadataMap.clear();
        metadataMap.putAll(entityMap);
        hasBind = true;
    }

    /**
     * 结果集序列化
     * @return 结果集序列化
     */
    @Override
    public String toString() {
        return toMap().toString();
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        String identification = model.getContainer().getIdentification();
        out.writeUTF(identification);
        out.writeUTF(model.getClass().getName());
        out.writeObject(metadataMap);
        out.writeObject(originalMetadataMap);
        out.writeUTF(originalSql);
        out.writeObject(relationMap);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        String identification = in.readUTF();
        String modelName = in.readUTF();
        Object map = in.readObject();
        Object originalMap = in.readObject();
        String sql = in.readUTF();
        relationMap = ObjectUtils.typeCast(in.readObject());

        Container container = GodProvider.get(identification);
        Class<?> modelClass = ClassUtils.forName(modelName);
        Model<?, ?, ?> model = container.getBean(ModelShadowProvider.class)
            .getByModelClass(ObjectUtils.typeCast(modelClass))
            .getModel();

        initNewRecord(ObjectUtils.typeCast(model), ObjectUtils.typeCast(map), sql);
        originalMetadataMap.putAll(ObjectUtils.typeCast(originalMap));
    }
}
