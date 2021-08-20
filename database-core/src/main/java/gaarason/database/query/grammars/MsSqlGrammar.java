package gaarason.database.query.grammars;

import gaarason.database.eloquent.appointment.SqlType;
import gaarason.database.exception.InvalidSqlTypeException;
import gaarason.database.exception.OperationNotSupportedException;
import gaarason.database.util.FormatUtils;

import java.util.List;

public class MsSqlGrammar extends BaseGrammar {

    public MsSqlGrammar(String tableName) {
        super(tableName);
    }


    @Override
    public void pushForceIndex(String indexName) {
        throw new OperationNotSupportedException();
    }

    @Override
    public void pushIgnoreIndex(String indexName) {
        throw new OperationNotSupportedException();
    }

    @Override
    public String generateSql(SqlType sqlType) {
        String sql;
        switch (sqlType) {
            case REPLACE:
                return "replace into " + dealFrom() + dealColumn() + " values" + dealValue();
            case INSERT:
                return identityInsertOn(table) + "insert into " + dealFrom() + dealColumn() + " values" + dealValue();
            case SELECT:
                sql = "select " + dealSelect() + dealFromSelect();
                break;
            case UPDATE:
                sql = "update " + dealFrom() + " set" + dealData();
                break;
            case DELETE:
                sql = "delete from " + dealFrom();
                break;
            case SUB_QUERY:
                sql = "";
                break;
            default:
                throw new InvalidSqlTypeException();
        }

        sql += dealJoin() + dealWhere(sqlType) + dealGroup() + dealHaving(
            sqlType) + dealOrderBy() + dealLimit() + dealLock();

        if (union != null) {
            sql = FormatUtils.bracket(sql);
        }

        sql += dealUnion();

        return sql;
    }

    @Override
    public List<String> getParameterList(SqlType sqlType) {
        if (sqlType != SqlType.INSERT)
            dataParameterList.addAll(whereParameterList);
        return dataParameterList;
    }

    /**
     * 开启标识列插入显式值
     * @param table 表名
     * @return 语句
     */
    protected static String identityInsertOn(String table) {
        return "set IDENTITY_INSERT " + table + " ON ";
    }

}
