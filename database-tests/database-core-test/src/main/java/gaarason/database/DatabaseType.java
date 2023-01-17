package gaarason.database;

import gaarason.database.exception.TypeNotSupportedException;

import java.util.HashMap;
import java.util.Map;

/**
 * 数据库类型
 * @author xt
 */
public enum DatabaseType {

    /**
     * mysql
     */
    MYSQL("mysql"),
    /**
     * mssql
     */
    MSSQL("mssql"),
    /**
     * mssql
     */
    MSSQL_V2("microsoft sql server");

    private static final Map<String, DatabaseType> DATABASE_PRODUCT_NAME_LOOKUP = new HashMap<>();

    static {
        for (DatabaseType type : DatabaseType.values()) {
            DATABASE_PRODUCT_NAME_LOOKUP.put(type.databaseProductName, type);
        }
    }

    private final String databaseProductName;

    DatabaseType(String databaseProductName) {
        this.databaseProductName = databaseProductName;
    }

    /**
     * 根据数据库名称(String), 找到对应的 enum
     * @param databaseProductName 数据库名称
     * @return DatabaseType
     */
    public static DatabaseType forDatabaseProductName(String databaseProductName) {
        DatabaseType databaseType = DATABASE_PRODUCT_NAME_LOOKUP.get(databaseProductName);
        if (databaseType == null) {
            throw new TypeNotSupportedException(
                "Database product name [" + databaseProductName + "] not supported yet.");
        }
        return databaseType;
    }

    /**
     * 根据数据库类型返回符号
     * @return 符号
     */
    public String getValueSymbol() {
        switch (this) {
            case MYSQL:
                return "\"";
            case MSSQL:
            case MSSQL_V2:
                return "'";
            default:
                throw new TypeNotSupportedException("Database type not supported.");
        }
    }

}
