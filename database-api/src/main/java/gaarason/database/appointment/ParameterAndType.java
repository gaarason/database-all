package gaarason.database.appointment;

import java.sql.JDBCType;

/**
 * 包含JDBC类型的参数引用
 */
public class ParameterAndType {

    Object value;

    JDBCType type;

    public ParameterAndType(Object value, JDBCType type) {
        this.value = value;
        this.type = type;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public JDBCType getType() {
        return type;
    }

    public void setType(JDBCType type) {
        this.type = type;
    }
}
