package gaarason.database.test.utils;

import gaarason.database.eloquent.appointment.DatabaseType;

public class DatabaseTypeUtil {

    private static DatabaseType databaseType = DatabaseType.MYSQL;


    public static DatabaseType getDatabaseType() {
        return databaseType;
    }


    public static void setDatabaseTypeToMysql() {
        databaseType = DatabaseType.MYSQL;
    }


    public static void setDatabaseTypeToMssql() {
        databaseType = DatabaseType.MSSQL;
    }

}
