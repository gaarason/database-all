package gaarason.database.appointment;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Locale;

/**
 * jdbc字段值对象
 * @author xt
 */
public class DBColumn implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String tableCat;

    private final String tableSchem;

    private final String tableName;

    private final String columnName;

    private final int dataType;

    private final String typeName;

    private final int columnSize;

    private final int bufferLength;

    private final int decimalDigits;

    private final int numPrecRadix;

    private final int nullable;

    private final String remarks;

    private final String columnDef;

    private final int sqlDataType;

    private final int sqlDatetimeSub;

    private final int charOctetLength;

    private final int ordinalPosition;

    //    private final String    sourceDataType;
    private final String isNullable;

    private final String isAutoincrement;


    /**
     * jdbc 数据类型
     */
    private final JdbcType jdbcType;

    /**
     * java 数据类型
     */
    private final String javaClassName;

    /**
     * 是否无符号型
     */
    private final boolean unsigned;

    public DBColumn(ResultSet rs) throws SQLException {

        tableCat = rs.getString("TABLE_CAT");
        tableSchem = rs.getString("TABLE_SCHEM");
        tableName = rs.getString("TABLE_NAME");
        columnName = rs.getString("COLUMN_NAME");
        dataType = rs.getInt("DATA_TYPE");
        typeName = rs.getString("TYPE_NAME");
        columnSize = rs.getInt("COLUMN_SIZE");
        bufferLength = rs.getInt("BUFFER_LENGTH");
        decimalDigits = rs.getInt("DECIMAL_DIGITS");
        numPrecRadix = rs.getInt("NUM_PREC_RADIX");
        nullable = rs.getInt("NULLABLE");
        remarks = rs.getString("REMARKS");
        columnDef = rs.getString("COLUMN_DEF");
        sqlDataType = rs.getInt("SQL_DATA_TYPE");
        sqlDatetimeSub = rs.getInt("SQL_DATETIME_SUB");
        charOctetLength = rs.getInt("CHAR_OCTET_LENGTH");
        ordinalPosition = rs.getInt("ORDINAL_POSITION");
//        sourceDataType = rs.getString("SOURCE_DATA_TYPE");
        isNullable = rs.getString("IS_NULLABLE");
        isAutoincrement = rs.getString("IS_AUTOINCREMENT");

        jdbcType = JdbcType.fromCode(dataType);
        unsigned = typeName.toLowerCase(Locale.ENGLISH).contains("unsigned");
        javaClassName = JdbcType.forClassName(jdbcType.TYPE_CODE);
    }

    public String getTableCat() {
        return tableCat;
    }

    public String getTableSchem() {
        return tableSchem;
    }

    public String getTableName() {
        return tableName;
    }

    public String getColumnName() {
        return columnName;
    }

    public int getDataType() {
        return dataType;
    }

    public String getTypeName() {
        return typeName;
    }

    public int getColumnSize() {
        return columnSize;
    }

    public int getBufferLength() {
        return bufferLength;
    }

    public int getDecimalDigits() {
        return decimalDigits;
    }

    public int getNumPrecRadix() {
        return numPrecRadix;
    }

    public int getNullable() {
        return nullable;
    }

    public String getRemarks() {
        return remarks;
    }

    public String getColumnDef() {
        return columnDef;
    }

    public int getSqlDataType() {
        return sqlDataType;
    }

    public int getSqlDatetimeSub() {
        return sqlDatetimeSub;
    }

    public int getCharOctetLength() {
        return charOctetLength;
    }

    public int getOrdinalPosition() {
        return ordinalPosition;
    }

    public String getIsNullable() {
        return isNullable;
    }

    public String getIsAutoincrement() {
        return isAutoincrement;
    }

    public JdbcType getJdbcType() {
        return jdbcType;
    }

    public String getJavaClassName() {
        return javaClassName;
    }

    public boolean isUnsigned() {
        return unsigned;
    }

    @Override
    public String toString() {
        return "DBColumn{" + "tableCat='" + tableCat + '\'' + ", tableSchem='" + tableSchem + '\'' + ", tableName='" +
            tableName + '\'' + ", columnName='" + columnName + '\'' + ", dataType=" + dataType + ", typeName='" +
            typeName + '\'' + ", columnSize=" + columnSize + ", bufferLength=" + bufferLength + ", decimalDigits=" +
            decimalDigits + ", numPrecRadix=" + numPrecRadix + ", nullable=" + nullable + ", remarks='" + remarks +
            '\'' + ", columnDef='" + columnDef + '\'' + ", sqlDataType=" + sqlDataType + ", sqlDatetimeSub=" +
            sqlDatetimeSub + ", charOctetLength=" + charOctetLength + ", ordinalPosition=" + ordinalPosition +
            ", isNullable='" + isNullable + '\'' + ", isAutoincrement='" + isAutoincrement + '\'' + ", jdbcType=" +
            jdbcType + ", javaClassName='" + javaClassName + '\'' + ", unsigned=" + unsigned + '}';
    }
}
