package gaarason.database.appointment;

/**
 * 表达式信息
 * 将 Stu::getName 解析为此对象, 那么 fieldName = name, entityCLass = Stu
 */
public class LambdaInfo<T> {
    private final String fieldName;
    /**
     * 不要优先使用
     * 优先使用 ModelShadowProvider 中的信息
     */
    private final String columnName;
    private final Class<T> entityCLass;

    public LambdaInfo(String fieldName, String columnName, Class<T> entityCLass) {
        this.fieldName = fieldName;
        this.columnName = columnName;
        this.entityCLass = entityCLass;
    }

    public String getFieldName() {
        return fieldName;
    }

    public String getColumnName() {
        return columnName;
    }

    public Class<T> getEntityCLass() {
        return entityCLass;
    }
}