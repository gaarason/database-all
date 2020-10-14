package gaarason.database.eloquent.appointment;

import gaarason.database.contract.connection.GaarasonDataSource;
import gaarason.database.contract.eloquent.Builder;
import gaarason.database.contract.eloquent.Model;
import gaarason.database.exception.BuilderNewInstanceException;
import gaarason.database.query.MsSqlBuilder;
import gaarason.database.query.MySqlBuilder;

import java.util.HashMap;
import java.util.Map;

public enum DatabaseType {
    MYSQL("mysql"),
    MSSQL("mssql");

    private final String databaseProductName;

    private static final Map<String, DatabaseType> databaseProductNameLookup = new HashMap<>();

    static {
        for (DatabaseType type : DatabaseType.values()) {
            databaseProductNameLookup.put(type.databaseProductName, type);
        }
    }

    DatabaseType(String databaseProductName) {
        this.databaseProductName = databaseProductName;
    }

    /**
     * 根据数据库名称(String), 找到对应的 enum
     * @param databaseProductName 数据库名称
     * @return DatabaseType
     */
    public static DatabaseType forDatabaseProductName(String databaseProductName) {
        return databaseProductNameLookup.get(databaseProductName);
    }

    public <T, K> Builder<T, K> getBuilderByDatabaseType(GaarasonDataSource gaarasonDataSource, Model<T, K> model, Class<T> entityClass) {
        switch (this) {
            case MYSQL:
                return new MySqlBuilder<>(gaarasonDataSource, model, entityClass);
            case MSSQL:
                return new MsSqlBuilder<>(gaarasonDataSource, model, entityClass);
            default:
                throw new BuilderNewInstanceException();
        }
    }

}
