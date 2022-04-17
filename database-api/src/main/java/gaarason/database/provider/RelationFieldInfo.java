package gaarason.database.provider;

import gaarason.database.contract.eloquent.relation.RelationSubQuery;

import java.lang.reflect.Field;

/**
 * 字段信息
 */
public class RelationFieldInfo {

    /**
     * 是否是集合
     */
    protected volatile boolean collection;

    /**
     * Field
     */
    protected Field field;

    /**
     * 属性名
     */
    protected String name;

    /**
     * java中的字段类型
     */
    protected Class<?> javaType;

    /**
     * java中的字段类型
     * 当本类型是非集合时, 此处等价于 javaType
     * 当本类型是集合时, 为集合中的泛型(不支持MAP等多泛型的)
     */
    protected Class<?> javaRealType;

    /**
     * 关联关系注解
     */
    protected RelationSubQuery relationSubQuery;

    public boolean isCollection() {
        return collection;
    }

    public void setCollection(boolean collection) {
        this.collection = collection;
    }

    public Field getField() {
        return field;
    }

    public void setField(Field field) {
        this.field = field;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Class<?> getJavaType() {
        return javaType;
    }

    public void setJavaType(Class<?> javaType) {
        this.javaType = javaType;
    }

    public Class<?> getJavaRealType() {
        return javaRealType;
    }

    public void setJavaRealType(Class<?> javaRealType) {
        this.javaRealType = javaRealType;
    }

    public RelationSubQuery getRelationSubQuery() {
        return relationSubQuery;
    }

    public void setRelationSubQuery(RelationSubQuery relationSubQuery) {
        this.relationSubQuery = relationSubQuery;
    }

    @Override
    public String toString() {
        return "RelationFieldInfo{" +
            "collection=" + collection +
            ", field=" + field +
            ", name='" + name + '\'' +
            ", javaType=" + javaType +
            ", javaRealType=" + javaRealType +
            ", relationSubQuery=" + relationSubQuery +
            '}';
    }
}