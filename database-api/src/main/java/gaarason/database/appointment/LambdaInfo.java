package gaarason.database.appointment;

/**
 * 表达式信息
 * 将 Stu::getName 解析为此对象, 那么 fieldName = name, entityCLass = Stu
 */
public class LambdaInfo<T> {
    private final String fieldName;
    private final Class<T> entityCLass;

    public LambdaInfo(String fieldName, Class<T> entityCLass) {
        this.fieldName = fieldName;
        this.entityCLass = entityCLass;
    }

    public String getFieldName() {
        return fieldName;
    }

    public Class<T> getEntityCLass() {
        return entityCLass;
    }

}