package gaarason.database.eloquent;

import gaarason.database.contract.eloquent.Model;
import gaarason.database.contract.eloquent.Record;
import gaarason.database.contract.eloquent.RecordList;
import gaarason.database.contract.eloquent.extra.Bind;
import gaarason.database.contract.function.GenerateSqlPartFunctionalInterface;
import gaarason.database.contract.function.RelationshipRecordWithFunctionalInterface;
import gaarason.database.core.lang.Nullable;
import gaarason.database.eloquent.record.BindBean;
import gaarason.database.exception.PrimaryKeyNotFoundException;
import gaarason.database.exception.RelationNotFoundException;
import gaarason.database.provider.ModelShadowProvider;
import gaarason.database.support.Column;
import gaarason.database.support.RelationGetSupport;
import gaarason.database.util.EntityUtils;
import gaarason.database.util.ObjectUtils;
import gaarason.database.util.StringUtils;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * 结果集对象
 * @author xt
 */
public class RecordBean<T extends Serializable, K extends Serializable> implements Record<T, K> {

    /**
     * 数据模型
     */
    protected final transient Model<T, K> model;

    /**
     * 数据实体类
     */
    protected final Class<T> entityClass;

    /**
     * 本表元数据
     * <数据库字段名 -> 字段信息>
     */
    protected transient Map<String, Column> metadataMap;

    /**
     * 原Sql
     */
    protected String originalSql = "";

    /**
     * 数据实体
     */
    protected transient T entity;

    /**
     * 是否已经绑定具体的数据
     */
    protected boolean hasBind;

    /**
     * 关联关系(Builder)
     */
    protected HashMap<String, GenerateSqlPartFunctionalInterface<T, K>> relationBuilderMap = new HashMap<>();

    /**
     * 关联关系(Record)
     */
    protected HashMap<String, RelationshipRecordWithFunctionalInterface> relationRecordMap = new HashMap<>();

    /**
     * 主键值
     */
    @Nullable
    protected K originalPrimaryKeyValue;

    /**
     * 根据查询结果集生成
     * @param entityClass     数据实体类
     * @param model           数据模型
     * @param stringColumnMap 元数据
     */
    public RecordBean(Class<T> entityClass, Model<T, K> model, Map<String, Column> stringColumnMap,
        String originalSql) {
        this.entityClass = entityClass;
        this.model = model;
        this.originalSql = originalSql;
        init(stringColumnMap);
    }

    /**
     * 凭空生成
     * @param entityClass 数据实体类
     * @param model       数据模型
     */
    public RecordBean(Class<T> entityClass, Model<T, K> model) {
        this.entityClass = entityClass;
        this.model = model;
        init(new HashMap<>());
    }


    @Override
    public Map<String, Column> getMetadataMap() {
        return metadataMap;
    }

    @Override
    public Model<T, K> getModel() {
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
    @Nullable
    public K getOriginalPrimaryKeyValue() {
        return originalPrimaryKeyValue;
    }

    @Override
    public void setOriginalPrimaryKeyValue(K value) {
        originalPrimaryKeyValue = value;
    }

    @Override
    public Map<String, GenerateSqlPartFunctionalInterface<T, K>> getRelationBuilderMap() {
        return relationBuilderMap;
    }

    @Override
    public void setRelationBuilderMap(HashMap<String, GenerateSqlPartFunctionalInterface<T, K>> relationBuilderMap) {
        this.relationBuilderMap = relationBuilderMap;
    }

    @Override
    public Map<String, RelationshipRecordWithFunctionalInterface> getRelationRecordMap() {
        return relationRecordMap;
    }

    @Override
    public void setRelationRecordMap(HashMap<String, RelationshipRecordWithFunctionalInterface> relationRecordMap) {
        this.relationRecordMap = relationRecordMap;
    }

    /**
     * 初始化数据
     * @param stringColumnMap 元数据
     */
    protected void init(Map<String, Column> stringColumnMap) {
        this.metadataMap = stringColumnMap;
        entity = toObjectWithoutRelationship();
        if (!stringColumnMap.isEmpty()) {
            hasBind = true;
            // 通知
            model.retrieved(this);
        } else {
            hasBind = false;
        }
    }

    /**
     * 将元数据map转化为普通map
     * @return 普通map
     */
    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>(16);
        for (Column value : metadataMap.values()) {
            map.put(value.getName(), value.getValue());
        }
        return map;
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
        RelationGetSupport<T, K> tkRelationGetSupport = new RelationGetSupport<>(this, true);
        return tkRelationGetSupport.toObject();

    }

    /**
     * 转化为对象
     * @return 对象
     */
    @Override
    public T toObject(Map<String, RecordList<?, ?>> cacheRelationRecordList) {
        RelationGetSupport<T, K> tkRelationGetSupport = new RelationGetSupport<>(this, true);
        return tkRelationGetSupport.toObject(cacheRelationRecordList);

    }


    /**
     * 元数据转实体对象
     * @return 实体对象
     */
    @Override
    public T toObjectWithoutRelationship() {
        RelationGetSupport<T, K> tkRelationGetSupport = new RelationGetSupport<>(this, false);
        return tkRelationGetSupport.toObject();
    }

    @Override
    public <V> V toObject(Class<V> clazz) {
        return EntityUtils.entityAssignment(this.metadataMap, clazz);
    }

    @Override
    public Record<T, K> withClear() {
        relationBuilderMap.clear();
        relationRecordMap.clear();
        return this;
    }

    @Override
    public Record<T, K> with(String column) {
        return with(column, builder -> builder);
    }

    @Override
    public Record<T, K> with(String column, GenerateSqlPartFunctionalInterface<T, K> builderClosure) {
        return with(column, builderClosure, theRecord -> theRecord);
    }

    @Override
    public Record<T, K> with(String column, GenerateSqlPartFunctionalInterface<T, K> builderClosure,
        RelationshipRecordWithFunctionalInterface recordClosure) {
        // 效验参数
        if (!ObjectUtils.checkProperties(entityClass, column)) {
            throw new RelationNotFoundException(entityClass + " 不存在关联属性 : " + column);
        }

        String[] columnArr = column.split("\\.");
        // 快捷类型
        if (columnArr.length > 1) {
            String lastLevelColumn = columnArr[columnArr.length - 1];
            String otherLevelColumn = StringUtils.rtrim(column, "." + lastLevelColumn);
            return with(otherLevelColumn, builder -> builder,
                thrRecord -> thrRecord.with(lastLevelColumn, ObjectUtils.typeCast(builderClosure), recordClosure));
        }

        relationBuilderMap.put(column, builderClosure);
        relationRecordMap.put(column, recordClosure);
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
        // aop阻止
        if (!model.saving(this)) {
            return false;
        }
        // 执行
        boolean success = hasBind ? update() : insert();
        // aop通知
        model.saved(this);
        // 响应
        return success;
    }

    /**
     * 删除
     * deleting -> deleted
     * @return 执行成功
     */
    @Override
    public boolean delete() {
        // 主键未知
        if (originalPrimaryKeyValue == null) {
            throw new PrimaryKeyNotFoundException();
        }
        // aop阻止
        if (!model.deleting(this)) {
            return false;
        }
        // 执行
        boolean success = model.newQuery()
            .where(model.getPrimaryKeyColumnName(), originalPrimaryKeyValue.toString())
            .delete() > 0;
        // 成功删除后后,刷新自身属性
        if (success) {
            this.metadataMap = new HashMap<>();
            entity = toObjectWithoutRelationship();
            hasBind = false;
            // 通知
            model.deleted(this);
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
        if (!model.restoring(this)) {
            return false;
        }
        // 执行
        boolean success = model.onlyTrashed()
            .where(model.getPrimaryKeyColumnName(), originalPrimaryKeyValue.toString())
            .restore() > 0;
        // 成功恢复后,刷新自身属性
        if (success && refresh) {
            refresh();
        }
        // 响应
        return success;
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
        Map<String, Column> theMetadataMap = model.withTrashed()
            .where(model.getPrimaryKeyColumnName(), originalPrimaryKeyValue.toString())
            .firstOrFail().getMetadataMap();

        // 刷新自身属性
        return refresh(theMetadataMap);
    }

    @Override
    public Record<T, K> refresh(Map<String, Column> metadataMap) {
        init(metadataMap);
        return this;
    }

    /**
     * 新增
     * 事件使用 creating created
     * @return 执行成功
     */
    protected boolean insert() {
        // aop阻止
        if (!model.creating(this)) {
            return false;
        }
        // 主键处理
        primaryKeyAutoDeal(entity);
        // 执行
        boolean success = model.newQuery().insertGetId(entity) != null;
        // 成功插入后,刷新自身属性
        if (success) {
            selfUpdate(entity, true);
            // 通知
            model.created(this);
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
        // 阻止
        if (!model.updating(this)) {
            return false;
        }
        // 执行
        boolean success = model.newQuery()
            .where(model.getPrimaryKeyColumnName(), originalPrimaryKeyValue.toString())
            .update(entity) > 0;
        // 成功更新后,刷新自身属性
        if (success) {
            selfUpdate(entity, false);
            // aop通知
            model.updated(this);
        }
        // 响应
        return success;
    }

    /**
     * 主键自动处理
     * @param entity 实体
     */
    protected void primaryKeyAutoDeal(T entity) {
        if (!model.isPrimaryKeyDefinition()) {
            return;
        }
        ModelShadowProvider.FieldInfo fieldInfo = model.getPrimaryKeyFieldInfo();
        if (ModelShadowProvider.fieldGet(fieldInfo, entity) == null) {
            K k = model.getPrimaryKeyIdGenerator().nextId();
            // 生成后赋值
            ModelShadowProvider.setPrimaryKeyValue(entity, k);
        }
    }

    /**
     * 更新自身数据
     * @param entity     新的实体
     * @param insertType 是否新增
     */
    protected void selfUpdate(T entity, boolean insertType) {
        // 更新元数据
        selfUpdateMetadataMap(entity, insertType);
        // 更新相关对象
        this.entity = toObjectWithoutRelationship();
    }

    /**
     * 更新元数据
     * @param entity     数据实体
     * @param insertType 是否为更新操作
     */
    protected void selfUpdateMetadataMap(T entity, boolean insertType) {
        // 模型信息
        ModelShadowProvider.ModelInfo<T, Serializable> modelInfo = ModelShadowProvider.getByEntityClass(entityClass);
        // 字段信息集合
        Map<String, ModelShadowProvider.FieldInfo> fieldInfoMap = insertType ? modelInfo.getJavaFieldInsertMap() : modelInfo.getJavaFieldUpdateMap();

        for (Map.Entry<String, ModelShadowProvider.FieldInfo> entry : fieldInfoMap.entrySet()) {
            // 字段信息
            ModelShadowProvider.FieldInfo fieldInfo = entry.getValue();
            // 获取值
            Object value = ModelShadowProvider.fieldGet(fieldInfo, entity);
            // 声明不可 null, 值仍然为null, 说明值无效
            if (!fieldInfo.isNullable() && value == null) {
                continue;
            }

            // 数据库列名
            String columnName = fieldInfo.getColumnName();
            if (insertType) {
                Column column = new Column();
                column.setValue(value);
                column.setName(columnName);
                metadataMap.put(columnName, column);
            } else {
                metadataMap.get(columnName).setValue(value);
            }
        }
        hasBind = true;
    }

    /**
     * 结果集序列化
     * @return 结果集序列化
     */
    @Override
    public String toString(){
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(getClass().getName()).append('@').append(Integer.toHexString(hashCode()));
        stringBuilder.append('{');
        for (Map.Entry<String, Column> entry : getMetadataMap().entrySet()) {
            stringBuilder.append(entry.getKey()).append("=\"").append(entry.getValue().getValue()).append("\", ");
        }
        return StringUtils.rtrim(StringUtils.rtrim(stringBuilder.toString(), " "), ",") + "}";
    }

}
