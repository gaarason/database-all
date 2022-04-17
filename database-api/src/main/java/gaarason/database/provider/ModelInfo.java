package gaarason.database.provider;

import gaarason.database.contract.eloquent.Model;
import gaarason.database.contract.support.IdGenerator;
import gaarason.database.lang.Nullable;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 格式化后的Model信息
 */
public class ModelInfo<T extends Serializable, K extends Serializable> {

    /**
     * model类
     */
    protected Class<? extends Model<T, K>> modelClass;

    /**
     * model对象
     */
    protected Model<T, K> model;

    /**
     * entity类
     */
    protected Class<T> entityClass;

    /**
     * 主键信息是否存在
     */
    protected boolean primaryKeyDefinition;

    /**
     * 主键列名(并非一定是实体的属性名)
     */
    @Nullable
    protected String primaryKeyColumnName;

    /**
     * 主键名(实体的属性名)
     */
    @Nullable
    protected String primaryKeyName;

    /**
     * 主键自动生成
     */
    @Nullable
    protected IdGenerator<K> primaryKeyIdGenerator;

    /**
     * 主键自增
     */
    @Nullable
    protected Boolean primaryKeyIncrement;

    /**
     * 主键信息
     */
    @Nullable
    protected FieldInfo primaryKeyFieldInfo;

    /**
     * 主键类型 (通过model上的第2个泛型类型得到)
     */
    protected Class<K> primaryKeyClass;

    /**
     * 数据库表名
     */
    protected String tableName;

    /**
     * `属性名`对应的`普通`字段数组
     */
    protected Map<String, FieldInfo> javaFieldMap = new LinkedHashMap<>();

    /**
     * `数据库字段`名对应的`普通`字段数组
     */
    protected Map<String, FieldInfo> columnFieldMap = new LinkedHashMap<>();

    /**
     * `属性名`名对应的`普通`字段数组, 可新增的字段
     */
    protected Map<String, FieldInfo> javaFieldInsertMap = new LinkedHashMap<>();

    /**
     * `属性名`名对应的`普通`字段数组, 可更新的字段
     */
    protected Map<String, FieldInfo> javaFieldUpdateMap = new LinkedHashMap<>();

    /**
     * `属性名`对应的`关系`字段数组
     */
    protected Map<String, RelationFieldInfo> relationFieldMap = new LinkedHashMap<>();

    public Class<? extends Model<T, K>> getModelClass() {
        return modelClass;
    }

    public void setModelClass(Class<? extends Model<T, K>> modelClass) {
        this.modelClass = modelClass;
    }

    public Model<T, K> getModel() {
        return model;
    }

    public void setModel(Model<T, K> model) {
        this.model = model;
    }

    public Class<T> getEntityClass() {
        return entityClass;
    }

    public void setEntityClass(Class<T> entityClass) {
        this.entityClass = entityClass;
    }

    public boolean isPrimaryKeyDefinition() {
        return primaryKeyDefinition;
    }

    public void setPrimaryKeyDefinition(boolean primaryKeyDefinition) {
        this.primaryKeyDefinition = primaryKeyDefinition;
    }

    @Nullable
    public String getPrimaryKeyColumnName() {
        return primaryKeyColumnName;
    }

    public void setPrimaryKeyColumnName(String primaryKeyColumnName) {
        this.primaryKeyColumnName = primaryKeyColumnName;
    }

    @Nullable
    public String getPrimaryKeyName() {
        return primaryKeyName;
    }

    public void setPrimaryKeyName(String primaryKeyName) {
        this.primaryKeyName = primaryKeyName;
    }

    @Nullable
    public IdGenerator<K> getPrimaryKeyIdGenerator() {
        return primaryKeyIdGenerator;
    }

    public void setPrimaryKeyIdGenerator(IdGenerator<K> primaryKeyIdGenerator) {
        this.primaryKeyIdGenerator = primaryKeyIdGenerator;
    }

    @Nullable
    public Boolean getPrimaryKeyIncrement() {
        return primaryKeyIncrement;
    }

    public void setPrimaryKeyIncrement(Boolean primaryKeyIncrement) {
        this.primaryKeyIncrement = primaryKeyIncrement;
    }

    @Nullable
    public FieldInfo getPrimaryKeyFieldInfo() {
        return primaryKeyFieldInfo;
    }

    public void setPrimaryKeyFieldInfo(FieldInfo primaryKeyFieldInfo) {
        this.primaryKeyFieldInfo = primaryKeyFieldInfo;
    }

    public Class<K> getPrimaryKeyClass() {
        return primaryKeyClass;
    }

    public void setPrimaryKeyClass(Class<K> primaryKeyClass) {
        this.primaryKeyClass = primaryKeyClass;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public Map<String, FieldInfo> getJavaFieldMap() {
        return javaFieldMap;
    }

    public void setJavaFieldMap(Map<String, FieldInfo> javaFieldMap) {
        this.javaFieldMap = javaFieldMap;
    }

    public Map<String, FieldInfo> getColumnFieldMap() {
        return columnFieldMap;
    }

    public void setColumnFieldMap(Map<String, FieldInfo> columnFieldMap) {
        this.columnFieldMap = columnFieldMap;
    }

    public Map<String, FieldInfo> getJavaFieldInsertMap() {
        return javaFieldInsertMap;
    }

    public void setJavaFieldInsertMap(Map<String, FieldInfo> javaFieldInsertMap) {
        this.javaFieldInsertMap = javaFieldInsertMap;
    }

    public Map<String, FieldInfo> getJavaFieldUpdateMap() {
        return javaFieldUpdateMap;
    }

    public void setJavaFieldUpdateMap(Map<String, FieldInfo> javaFieldUpdateMap) {
        this.javaFieldUpdateMap = javaFieldUpdateMap;
    }

    public Map<String, RelationFieldInfo> getRelationFieldMap() {
        return relationFieldMap;
    }

    public void setRelationFieldMap(Map<String, RelationFieldInfo> relationFieldMap) {
        this.relationFieldMap = relationFieldMap;
    }

    @Override
    public String toString() {
        return "ModelInfo{" +
            "modelClass=" + modelClass +
            ", model=" + model +
            ", entityClass=" + entityClass +
            ", primaryKeyDefinition=" + primaryKeyDefinition +
            ", primaryKeyColumnName='" + primaryKeyColumnName + '\'' +
            ", primaryKeyName='" + primaryKeyName + '\'' +
            ", primaryKeyIdGenerator=" + primaryKeyIdGenerator +
            ", primaryKeyIncrement=" + primaryKeyIncrement +
            ", primaryKeyFieldInfo=" + primaryKeyFieldInfo +
            ", primaryKeyClass=" + primaryKeyClass +
            ", tableName='" + tableName + '\'' +
            ", javaFieldMap=" + javaFieldMap +
            ", columnFieldMap=" + columnFieldMap +
            ", javaFieldInsertMap=" + javaFieldInsertMap +
            ", javaFieldUpdateMap=" + javaFieldUpdateMap +
            ", relationFieldMap=" + relationFieldMap +
            '}';
    }
}
