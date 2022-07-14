package gaarason.database.appointment;


import gaarason.database.lang.Nullable;

/**
 * jdbc字段值对象
 * @author xt
 */
public class Column {

    private static final long serialVersionUID = 1L;

    /**
     * 字段名
     */
    private String name;

    /**
     * 字段值, 大多数情况下是可序列化的类型
     * 但是也可能是 Blob 以及 Clob 等
     */
    @Nullable
    private Object value;

    /**
     * 字段类型 java.sql.Types
     * @see java.sql.Types
     */
    private int type;

    private String typeName;

    private int count;

    /**
     * designated column's table's catalog name.
     */
    private String catalogName;

    private String className;

    private int displaySize;

    /**
     * 原列名
     */
    private String columnName;

    private String schemaName;

    private int precision;

    private int scale;

    private String tableName;

    /**
     * 是否自增
     */
    private boolean autoIncrement;

    private boolean caseSensitive;

    private boolean searchable;

    private boolean currency;

    private boolean nullable;

    private boolean signed;

    private boolean readOnly;

    private boolean writable;

    private boolean definitelyWritable;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Nullable
    public Object getValue() {
        return value;
    }

    public void setValue(@Nullable Object value) {
        this.value = value;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String getCatalogName() {
        return catalogName;
    }

    public void setCatalogName(String catalogName) {
        this.catalogName = catalogName;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public int getDisplaySize() {
        return displaySize;
    }

    public void setDisplaySize(int displaySize) {
        this.displaySize = displaySize;
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }

    public int getPrecision() {
        return precision;
    }

    public void setPrecision(int precision) {
        this.precision = precision;
    }

    public int getScale() {
        return scale;
    }

    public void setScale(int scale) {
        this.scale = scale;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public boolean isAutoIncrement() {
        return autoIncrement;
    }

    public void setAutoIncrement(boolean autoIncrement) {
        this.autoIncrement = autoIncrement;
    }

    public boolean isCaseSensitive() {
        return caseSensitive;
    }

    public void setCaseSensitive(boolean caseSensitive) {
        this.caseSensitive = caseSensitive;
    }

    public boolean isSearchable() {
        return searchable;
    }

    public void setSearchable(boolean searchable) {
        this.searchable = searchable;
    }

    public boolean isCurrency() {
        return currency;
    }

    public void setCurrency(boolean currency) {
        this.currency = currency;
    }

    public boolean isNullable() {
        return nullable;
    }

    public void setNullable(boolean nullable) {
        this.nullable = nullable;
    }

    public boolean isSigned() {
        return signed;
    }

    public void setSigned(boolean signed) {
        this.signed = signed;
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    public boolean isWritable() {
        return writable;
    }

    public void setWritable(boolean writable) {
        this.writable = writable;
    }

    public boolean isDefinitelyWritable() {
        return definitelyWritable;
    }

    public void setDefinitelyWritable(boolean definitelyWritable) {
        this.definitelyWritable = definitelyWritable;
    }

    @Override
    public String toString() {
        return "Column{" +
            "name='" + name + '\'' +
            ", value=" + value +
            ", type=" + type +
            ", typeName='" + typeName + '\'' +
            ", count=" + count +
            ", catalogName='" + catalogName + '\'' +
            ", className='" + className + '\'' +
            ", displaySize=" + displaySize +
            ", columnName='" + columnName + '\'' +
            ", schemaName='" + schemaName + '\'' +
            ", precision=" + precision +
            ", scale=" + scale +
            ", tableName='" + tableName + '\'' +
            ", autoIncrement=" + autoIncrement +
            ", caseSensitive=" + caseSensitive +
            ", searchable=" + searchable +
            ", currency=" + currency +
            ", nullable=" + nullable +
            ", signed=" + signed +
            ", readOnly=" + readOnly +
            ", writable=" + writable +
            ", definitelyWritable=" + definitelyWritable +
            '}';
    }
}
