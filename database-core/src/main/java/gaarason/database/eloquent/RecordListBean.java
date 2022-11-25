package gaarason.database.eloquent;

import gaarason.database.config.ConversionConfig;
import gaarason.database.contract.eloquent.Record;
import gaarason.database.contract.eloquent.RecordList;
import gaarason.database.contract.function.ColumnFunctionalInterface;
import gaarason.database.contract.function.FilterRecordAttributeFunctionalInterface;
import gaarason.database.contract.function.GenerateSqlPartFunctionalInterface;
import gaarason.database.contract.function.RelationshipRecordWithFunctionalInterface;
import gaarason.database.core.Container;
import gaarason.database.exception.AbnormalParameterException;
import gaarason.database.exception.NoSuchAlgorithmException;
import gaarason.database.exception.OperationNotSupportedException;
import gaarason.database.lang.Nullable;
import gaarason.database.provider.GodProvider;
import gaarason.database.provider.ModelShadowProvider;
import gaarason.database.support.EntityMember;
import gaarason.database.support.RelationGetSupport;
import gaarason.database.util.ObjectUtils;
import gaarason.database.util.StringUtils;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.*;

/**
 * 结果集集合对象
 * @param <T>
 * @param <K>
 * @author xt
 */
public class RecordListBean<T, K> extends LinkedList<Record<T, K>>
    implements RecordList<T, K> {

    /**
     * 容器
     */
    protected transient Container container;
    /**
     * Model信息
     */
    protected transient ModelShadowProvider modelShadowProvider;
    /**
     * 原始sql
     */
    protected String originalSql = "";
    /**
     * 临时缓存
     */
    protected transient HashMap<Object, Set<Object>> cacheMap = new HashMap<>();

    /**
     * 仅反序列化时使用
     */
    public RecordListBean() {

    }

    public RecordListBean(Container container) {
        initRecordListBean("", container);
    }

    public RecordListBean(String originalSql, Container container) {
        initRecordListBean(originalSql, container);
    }

    protected void initRecordListBean(String originalSql, Container container) {
        this.originalSql = originalSql;
        this.container = container;
        this.modelShadowProvider = container.getBean(ModelShadowProvider.class);
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o) && (o instanceof RecordList) &&
            originalSql.equals(((RecordList<?, ?>) o).getOriginalSql());
    }

    @Override
    public int hashCode() {
        return super.hashCode() + originalSql.hashCode();
    }

    @Override
    public List<Map<String, Object>> getOriginalMetadataMapList() {
        List<Map<String, Object>> metadataMapList = new ArrayList<>();
        for (Record<T, K> tkRecord : this) {
            metadataMapList.add(tkRecord.getMetadataMap());
        }
        return metadataMapList;
    }

    @Override
    public String getOriginalSql() {
        return originalSql;
    }

    @Override
    public HashMap<Object, Set<Object>> getCacheMap() {
        return cacheMap;
    }

    @Override
    public void setCacheMap(HashMap<Object, Set<Object>> cacheMap) {
        this.cacheMap = cacheMap;
    }

    /**
     * 转化为对象列表
     * @return 对象列表
     */
    @Override
    public List<T> toObjectList() {
        RelationGetSupport<T, K> tkRelationGetSupport = new RelationGetSupport<>(container, this, true);
        return tkRelationGetSupport.toObjectList();
    }

    @Override
    public List<T> toObjectListWithoutRelationship() {
        RelationGetSupport<T, K> tkRelationGetSupport = new RelationGetSupport<>(container, this, false);
        return tkRelationGetSupport.toObjectList();
    }

    @Override
    public List<T> toObjectList(Map<String, RecordList<?, ?>> cacheRelationRecordList) {
        RelationGetSupport<T, K> tkRelationGetSupport = new RelationGetSupport<>(container, this, true);
        return tkRelationGetSupport.toObjectList(cacheRelationRecordList);
    }

    @Override
    public <V> List<V> toObjectList(Class<V> clazz) {
        List<V> entityList = new ArrayList<>();
        for (Record<T, K> tkRecord : this) {
            entityList.add(tkRecord.toObject(clazz));
        }
        return entityList;
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
     * 转化为map list
     * @return mapList
     */
    @Override
    public List<Map<String, Object>> toMapList() {
        List<Map<String, Object>> list = new ArrayList<>();
        for (Record<T, K> theRecord : this) {
            list.add(theRecord.toMap());
        }
        return list;
    }

    @Override
    public Map<String, List<Object>> toListMap() {
        Map<String, List<Object>> map = new HashMap<>(16);
        for (Record<T, K> theRecord : this) {
            for (String column : theRecord.getMetadataMap().keySet()) {
                List<Object> list = map.computeIfAbsent(column, key -> new ArrayList<>());
                list.add(theRecord.getMetadataMap().get(column));
            }
        }
        return map;
    }

    /**
     * 过滤成list
     * @return 单个字段列表
     */
    @Override
    public <V> List<V> toList(
        FilterRecordAttributeFunctionalInterface<T, K, V> filterRecordAttributeFunctionalInterface) {
        List<V> list = new ArrayList<>();
        for (Record<T, K> theRecord : this) {
            V result = filterRecordAttributeFunctionalInterface.execute(theRecord);
            if (null == result) {
                continue;
            }
            list.add(result);
        }
        return list;
    }

    @Override
    public List<Object> toOneColumnList() {
        return toList(theRecord -> {
            Set<Map.Entry<String, Object>> entries = theRecord.getMetadataMap().entrySet();
            for (Map.Entry<String, Object> entry : entries) {
                return entry.getValue();
            }
            return null;
        });
    }

    @Override
    public RecordListBean<T, K> with(String fieldName) {
        return with(fieldName, builder -> builder);
    }

    @Override
    public RecordListBean<T, K> with(String fieldName, GenerateSqlPartFunctionalInterface<?, ?> builderClosure) {
        return with(fieldName, builderClosure, theRecord -> theRecord);
    }

    @Override
    public RecordListBean<T, K> with(String fieldName, GenerateSqlPartFunctionalInterface<?, ?> builderClosure,
        RelationshipRecordWithFunctionalInterface recordClosure) {
        String[] columnArr = fieldName.split("\\.");
        // 快捷类型
        if (columnArr.length > 1) {
            String lastLevelColumn = columnArr[columnArr.length - 1];
            String otherLevelColumn = StringUtils.rtrim(fieldName, "." + lastLevelColumn);
            return with(otherLevelColumn, builder -> builder,
                theRecord -> theRecord.with(lastLevelColumn, builderClosure, recordClosure));
        }
        for (Record<T, K> tkRecord : this) {
            // 赋值关联关系过滤
            // 保持引用
            tkRecord.getRelationBuilderMap().put(fieldName, builderClosure);
            tkRecord.getRelationRecordMap().put(fieldName, recordClosure);
        }
        return this;
    }

    @Override
    public String toString() {
        return toMapList().toString();
    }

    @Override
    @Nullable
    public <W> W elementGetValueByFieldName(Record<T, K> theRecord, String fieldName) {
        EntityMember<T, K> entityMember = modelShadowProvider.parseAnyEntityWithCache(
            theRecord.getModel().getEntityClass());

        final Object value = theRecord.getMetadataMap()
            .get(entityMember.getFieldMemberByFieldName(fieldName).getColumnName());
        return value == null ? null : ObjectUtils.typeCast(value);
    }

    @Override
    public Map<String, Object> elementToMap(Record<T, K> theRecord) throws OperationNotSupportedException {
        return theRecord.toMap();
    }

    @Override
    public ConversionConfig getConversionWorkerFromContainer() {
        return container.getBean(ConversionConfig.class);
    }

    @Override
    public boolean isEmpty(@Nullable Object obj) {
        return ObjectUtils.isEmpty(obj);
    }

    @Override
    public boolean isEmpty(@Nullable Object[] obj) {
        return ObjectUtils.isEmpty(obj);
    }

    @Override
    public boolean contains(String fieldName, @Nullable Object value) {
        for (Record<T, K> e : this) {
            if (ObjectUtils.nullSafeEquals(elementGetValueByFieldName(e, fieldName), value)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public List<Record<T, K>> random(int count) throws NoSuchAlgorithmException {
        if (count > size()) {
            throw new AbnormalParameterException(
                "The parameter count [" + count + "] of the random method should not be less than size [" + size() +
                    "]");
        }
        if (isEmpty()) {
            return new ArrayList<>();
        }
        if (size() == 1) {
            return Collections.singletonList(get(0));
        }
        final List<Record<T, K>> list = new ArrayList<>(count);
        final Set<Integer> randomSet = ObjectUtils.random(size(), count);
        for (Integer index : randomSet) {
            list.add(get(index));
        }
        return list;
    }


    @Override
    public Container getContainer() {
        return container;
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        String identification = container.getIdentification();
        out.writeUTF(identification);
        out.writeUTF(originalSql);
        out.writeObject(toArray(new Record<?, ?>[0]));
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        String identification = in.readUTF();
        String sql = in.readUTF();
        Record<?, ?>[] array = ObjectUtils.typeCast(in.readObject());

        Container container = GodProvider.get(identification);
        initRecordListBean(sql, container);
        for (Record<?, ?> record : array) {
            add(ObjectUtils.typeCast(record));
        }
    }
}
