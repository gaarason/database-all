package gaarason.database.test.utils;

import gaarason.database.DatabaseType;
import gaarason.database.test.MysqlAsyncTests;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * 数据库类型
 * @author xt
 */
public class DatabaseTypeUtil {

    private static DatabaseType databaseType = DatabaseType.MYSQL;

    private DatabaseTypeUtil() {

    }

    public static DatabaseType getDatabaseType() {
        return databaseType;
    }

    public static String setDatabaseTypeToMysql() throws IOException {
        databaseType = DatabaseType.MYSQL;
        String sqlFilename = MysqlAsyncTests.class.getClassLoader().getResource("sql/mysql.sql").getFile();
        return readToString(sqlFilename);
    }

    public static String setDatabaseTypeToMssql() throws IOException {
        databaseType = DatabaseType.MSSQL;
        String sqlFilename = MysqlAsyncTests.class.getClassLoader().getResource("sql/mssql.sql").getFile();
        return readToString(sqlFilename);
    }

    public static String readToString(String fileName) throws IOException {
        String encoding = "UTF-8";
        File file = new File(fileName);
        file.setReadable(true);
        Long fileLength = file.length();
        byte[] fileContent = new byte[fileLength.intValue()];
        FileInputStream in = new FileInputStream(file);
        in.read(fileContent);
        in.close();
        return new String(fileContent, encoding);
    }
}
