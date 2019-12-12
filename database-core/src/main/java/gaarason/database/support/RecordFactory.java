package gaarason.database.support;

import gaarason.database.eloquent.Model;
import gaarason.database.eloquent.Record;
import gaarason.database.eloquent.RecordList;
import gaarason.database.exception.EntityNotFoundException;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class RecordFactory {

    public static <T> RecordList<T> newRecordList(Class<T> entityClass, Model<T> model, ResultSet resultSet)
        throws SQLException {
        RecordList<T>           recordList        = new RecordList<>();
        final ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
        while (resultSet.next()) {
            recordList.add(new Record<>(entityClass, model, JDBCResultToMap(resultSetMetaData, resultSet)));
        }
        return recordList;
    }

    public static <T> Record<T> newRecord(Class<T> entityClass, Model<T> model, ResultSet resultSet)
        throws SQLException, EntityNotFoundException {
        if (!resultSet.next()) {
            throw new EntityNotFoundException();
        }
        final ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
        return new Record<>(entityClass, model, JDBCResultToMap(resultSetMetaData, resultSet));
    }

    private static Map<String, Column> JDBCResultToMap(ResultSetMetaData resultSetMetaData, ResultSet resultSet)
        throws SQLException {
        Map<String, Column> map                = new HashMap<>();
        final int           columnCountMoreOne = resultSetMetaData.getColumnCount() + 1;
        for (int i = 1; i < columnCountMoreOne; i++) {
            Column column = new Column();
            column.setName(resultSetMetaData.getColumnLabel(i));
            column.setValue(resultSet.getObject(column.getName()));
            column.setType(resultSetMetaData.getColumnType(i));
            column.setTypeName(resultSetMetaData.getColumnTypeName(i));
            column.setCount(resultSetMetaData.getColumnCount());
            column.setCatalogName(resultSetMetaData.getCatalogName(i));
            column.setClassName(resultSetMetaData.getColumnClassName(i));
            column.setDisplaySize(resultSetMetaData.getColumnDisplaySize(i));
            column.setColumnName(resultSetMetaData.getColumnName(i));
            column.setSchemaName(resultSetMetaData.getSchemaName(i));
            column.setPrecision(resultSetMetaData.getPrecision(i));
            column.setScale(resultSetMetaData.getScale(i));
            column.setTableName(resultSetMetaData.getTableName(i));
            column.setAutoIncrement(resultSetMetaData.isAutoIncrement(i));
            column.setCaseSensitive(resultSetMetaData.isCaseSensitive(i));
            column.setSearchable(resultSetMetaData.isSearchable(i));
            column.setCurrency(resultSetMetaData.isCurrency(i));
            column.setNullable(resultSetMetaData.isNullable(i) == ResultSetMetaData.columnNullable);
            column.setSigned(resultSetMetaData.isSigned(i));
            column.setReadOnly(resultSetMetaData.isReadOnly(i));
            column.setWritable(resultSetMetaData.isWritable(i));
            column.setDefinitelyWritable(resultSetMetaData.isDefinitelyWritable(i));
            map.put(column.getName(), column);
        }
        return map;
    }
}
