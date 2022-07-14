package gaarason.database.appointment;

import gaarason.database.lang.Nullable;

import java.sql.JDBCType;

/**
 * 包含JDBC类型的参数引用
 */
public class ValueWrapper {

    @Nullable
    Object value;

    JDBCType type;

    public ValueWrapper(@Nullable Object value, JDBCType type) {
        this.value = value;
        this.type = type;
    }

    @Nullable
    public Object getValue() {
        return value;
    }

    public JDBCType getType() {
        return type;
    }
}
