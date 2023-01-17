package gaarason.database.appointment;

import java.io.Serializable;
import java.sql.Types;
import java.util.HashMap;
import java.util.Map;

/**
 * jdbc类型
 * @author xt
 */
public enum JdbcType implements Serializable {

    /**
     * 类型
     */
    ARRAY(Types.ARRAY),
    BIT(Types.BIT),
    TINYINT(Types.TINYINT),
    SMALLINT(Types.SMALLINT),
    INTEGER(Types.INTEGER),
    BIGINT(Types.BIGINT),
    FLOAT(Types.FLOAT),
    REAL(Types.REAL),
    DOUBLE(Types.DOUBLE),
    NUMERIC(Types.NUMERIC),
    DECIMAL(Types.DECIMAL),
    CHAR(Types.CHAR),
    VARCHAR(Types.VARCHAR),
    LONG_VARCHAR(Types.LONGVARCHAR),
    DATE(Types.DATE),
    TIME(Types.TIME),
    TIMESTAMP(Types.TIMESTAMP),
    BINARY(Types.BINARY),
    VARBINARY(Types.VARBINARY),
    LONG_VARBINARY(Types.LONGVARBINARY),
    NULL(Types.NULL),
    OTHER(Types.OTHER),
    BLOB(Types.BLOB),
    CLOB(Types.CLOB),
    BOOLEAN(Types.BOOLEAN),
    CURSOR(-10), // Oracle
    UNDEFINED(Integer.MIN_VALUE + 1000),
    NVARCHAR(Types.NVARCHAR), // JDK6
    NCHAR(Types.NCHAR), // JDK6
    NCLOB(Types.NCLOB), // JDK6
    STRUCT(Types.STRUCT),
    JAVA_OBJECT(Types.JAVA_OBJECT),
    DISTINCT(Types.DISTINCT),
    REF(Types.REF),
    DATA_LINK(Types.DATALINK),
    ROW_ID(Types.ROWID), // JDK6
    LONG_NVARCHAR(Types.LONGNVARCHAR), // JDK6
    SQL_XML(Types.SQLXML), // JDK6
    DATETIME_OFFSET(-155), // SQL Server 2008
    TIME_WITH_TIMEZONE(Types.TIME_WITH_TIMEZONE), // JDBC 4.2 JDK8
    TIMESTAMP_WITH_TIMEZONE(Types.TIMESTAMP_WITH_TIMEZONE); // JDBC 4.2 JDK8

    private static final Map<Integer, JdbcType> codeLookup = new HashMap<>();

    static {
        for (JdbcType type : JdbcType.values()) {
            codeLookup.put(type.TYPE_CODE, type);
        }
    }

    public final int TYPE_CODE;

    JdbcType(int code) {
        this.TYPE_CODE = code;
    }

    /**
     * @param code JdbcType
     * @return JdbcType
     */
    public static JdbcType fromCode(int code) {
        return codeLookup.get(code);
    }

    /**
     * @param code JdbcType
     * @return java 类型名称
     */
    public static String forClassName(int code) {
        String className = String.class.getName();
        switch (code) {
            case Types.NUMERIC:
            case Types.DECIMAL:
                className = java.math.BigDecimal.class.getName();
                break;

            case Types.BIT:
                className = java.lang.Boolean.class.getName();
                break;

            case Types.TINYINT:
                className = java.lang.Byte.class.getName();
                break;

            case Types.SMALLINT:
                className = java.lang.Short.class.getName();
                break;

            case Types.INTEGER:
                className = java.lang.Integer.class.getName();
                break;

            case Types.BIGINT:
                className = java.lang.Long.class.getName();
                break;

            case Types.REAL:
                className = java.lang.Float.class.getName();
                break;

            case Types.FLOAT:
            case Types.DOUBLE:
                className = java.lang.Double.class.getName();
                break;

            case Types.BINARY:
            case Types.VARBINARY:
            case Types.LONGVARBINARY:
                className = "Byte[]";
                break;

            case Types.DATE:
                className = java.sql.Date.class.getName();
                break;

            case Types.TIME:
                className = java.sql.Time.class.getName();
                break;

            case Types.TIMESTAMP:
                className = java.sql.Timestamp.class.getName();
                break;

            case Types.BLOB:
                className = java.sql.Blob.class.getName();
                break;

            case Types.CLOB:
                className = java.sql.Clob.class.getName();
                break;
        }

        return className;
    }
}
