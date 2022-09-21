package gaarason.database.eloquent.relation;

import gaarason.database.config.ConversionConfig;
import gaarason.database.contract.eloquent.Model;
import gaarason.database.contract.eloquent.RecordList;
import gaarason.database.contract.eloquent.relation.RelationSubQuery;
import gaarason.database.core.Container;
import gaarason.database.eloquent.RecordListBean;
import gaarason.database.exception.PrimaryKeyNotFoundException;
import gaarason.database.provider.ModelShadowProvider;
import gaarason.database.support.FieldMember;
import gaarason.database.support.ModelMember;
import gaarason.database.support.PrimaryKeyMember;
import gaarason.database.util.ObjectUtils;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.*;

/**
 * 关联关系
 * @author xt
 */
public abstract class BaseRelationSubQuery implements RelationSubQuery {

    /**
     * model 信息
     */
    protected final ModelShadowProvider modelShadowProvider;

    /**
     * 当前模型
     */
    protected final Model<?, ?> localModel;

    protected BaseRelationSubQuery(ModelShadowProvider modelShadowProvider, Model<?, ?> model) {
        this.modelShadowProvider = modelShadowProvider;
        this.localModel = model;
    }

    /**
     * 将数据源中的某一列,转化为可以使用 where in 查询的 set
     * @param columnValueMapList 数据源
     * @param column 目标列
     * @return 目标列的集合
     */
    protected static Set<Object> getColumnInMapList(List<Map<String, Object>> columnValueMapList, String column) {
        Set<Object> result = new HashSet<>();
        for (Map<String, Object> stringColumnMap : columnValueMapList) {
            result.add(stringColumnMap.get(column));
        }
        return result;
    }

    @Override
    public RecordList<?, ?> dealBatchPrepare(String sql1) {
        return new RecordListBean<>(getContainer());
    }

    /**
     * 容器
     * @return 容器
     */
    protected abstract Container getContainer();

    /**
     * 集合兼容处理
     * 1. 用于解决AbstractList不实现removeAll的情况; 2. 产生全新对象; 3. 将集合类数据的类型,转化成model的主键类型
     * @param mayBeInstanceOfAbstractList 原集合
     * @param model 模型对象
     * @return 集合
     */
    protected Collection<Object> compatibleCollection(Collection<Object> mayBeInstanceOfAbstractList,
        Model<?, ?> model) {
        final Class<?> javaType = model.getPrimaryKeyClass();
        HashSet<Object> tempHashSet = new HashSet<>();
        for (Object old : mayBeInstanceOfAbstractList) {
            tempHashSet.add(getContainer().getBean(ConversionConfig.class).cast(old, javaType));
        }
        return tempHashSet;
    }

    /**
     * 通过targetRecords, 获取目标表的之间集合
     * @param targetRecords 目标结果集合
     * @return 目标表的主键集合
     */
    protected List<Object> getTargetRecordPrimaryKeyIds(RecordList<?, ?> targetRecords) {
        // 应该目标表的主键列表
        return targetRecords.toList(
            recordTemp -> recordTemp.getMetadataMap().get(recordTemp.getModel().getPrimaryKeyColumnName()));
    }

    /**
     * 获取 model 实例
     * @param field 属性信息
     * @return Model实例
     */
    protected Model<?, ?> getModelInstance(Field field) {
        Class<? extends Serializable> clazz = ObjectUtils.isCollection(field.getType()) ?
            ObjectUtils.getGenerics((ParameterizedType) field.getGenericType(), 0) :
            ObjectUtils.typeCast(field.getType());
        return modelShadowProvider.getByEntityClass(clazz).getModel();
    }

    /**
     * 获取model主键列名
     * @param model Model实例
     * @return 主键列名
     */
    protected String getPrimaryKeyColumnName(Model<?, ?> model) {
        PrimaryKeyMember primaryKeyMember = modelShadowProvider.get(model).getEntityMember().getPrimaryKeyMember();
        if (null == primaryKeyMember) {
            throw new PrimaryKeyNotFoundException();
        }
        return primaryKeyMember.getFieldMember().getColumnName();
    }

    /**
     * 获取 model 实例
     * @param modelClass Model类
     * @return Model实例
     */
    protected Model<?, ?> getModelInstance(Class<? extends Model<?, ?>> modelClass) {
        return modelShadowProvider.getByModelClass(ObjectUtils.typeCast(modelClass)).getModel();
    }

    /**
     * 将满足条件的对象筛选并返回
     * @param relationshipObjectList 待筛选的对象列表
     * @param columnName 对象的属性的名
     * @param fieldTargetValue 对象的属性的目标值
     * @return 对象列表
     */
    protected List<Object> findObjList(List<?> relationshipObjectList, String columnName, Object fieldTargetValue) {
        List<Object> objectList = new ArrayList<>();
        if (!relationshipObjectList.isEmpty()) {
            // 模型信息
            ModelMember<?, ?> modelMember = modelShadowProvider.getByEntityClass(
                relationshipObjectList.get(0).getClass());

            // 字段信息
            FieldMember fieldMember = modelMember.getEntityMember().getColumnFieldMap().get(columnName);

            for (Object o : relationshipObjectList) {
                // 值
                Object fieldValue = fieldMember.fieldGet(o);
                // 满足则加入
                if (fieldTargetValue.equals(fieldValue)) {
                    objectList.add(o);
                }
            }
        }
        return objectList;
    }
}
