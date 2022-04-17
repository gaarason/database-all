package gaarason.database.provider;

import gaarason.database.annotation.Column;
import gaarason.database.lang.Nullable;

import java.lang.reflect.Field;

public class FieldInfo {

    /**
     * Field (已经 设置属性是可访问)
     */
    protected Field field;

    /**
     * 属性名
     */
    protected String name;

    /**
     * 字段名
     */
    protected String columnName;

    /**
     * 可新增
     */
    protected boolean insertable = true;

    /**
     * 可更新
     */
    protected boolean updatable = true;

    /**
     * 可 null
     */
    protected boolean nullable;

    /**
     * java中的字段类型
     */
    protected Class<?> javaType;

    /**
     * 默认值
     */
    protected Object defaultValue;

    /**
     * column 注解
     */
    @Nullable
    protected Column column;

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

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public boolean isInsertable() {
        return insertable;
    }

    public void setInsertable(boolean insertable) {
        this.insertable = insertable;
    }

    public boolean isUpdatable() {
        return updatable;
    }

    public void setUpdatable(boolean updatable) {
        this.updatable = updatable;
    }

    public boolean isNullable() {
        return nullable;
    }

    public void setNullable(boolean nullable) {
        this.nullable = nullable;
    }

    public Class<?> getJavaType() {
        return javaType;
    }

    public void setJavaType(Class<?> javaType) {
        this.javaType = javaType;
    }

    public Object getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(Object defaultValue) {
        this.defaultValue = defaultValue;
    }

    public Column getColumn() {
        return column;
    }

    public void setColumn(Column column) {
        this.column = column;
    }

    @Override
    public String toString() {
        return "FieldInfo{" +
            "field=" + field +
            ", name='" + name + '\'' +
            ", columnName='" + columnName + '\'' +
            ", insertable=" + insertable +
            ", updatable=" + updatable +
            ", nullable=" + nullable +
            ", javaType=" + javaType +
            ", defaultValue=" + defaultValue +
            ", column=" + column +
            '}';
    }
}
