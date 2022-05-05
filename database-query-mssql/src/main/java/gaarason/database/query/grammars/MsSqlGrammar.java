package gaarason.database.query.grammars;

/**
 * mssql 语法分析基类
 * @author xt
 */
public class MsSqlGrammar extends BaseGrammar {

    public MsSqlGrammar(String tableName) {
        super(tableName);
    }


//    @Override
//    public String generateSql(SqlType sqlType) {
//        StringBuilder sqlBuilder = new StringBuilder();
//        switch (sqlType) {
//            case REPLACE:
//                return sqlBuilder.append("replace into ").append(dealFrom()).append(dealColumn()).append(" values").append(dealValue()).toString();
//            case INSERT:
//                return sqlBuilder.append(identityInsertOn(table)).append("insert into ").append(dealFrom()).append(dealColumn()).append(" values").append(dealValue()).toString();
//            case SELECT:
//                sqlBuilder.append("select ").append(dealSelect()).append(dealFromSelect());
//                break;
//            case UPDATE:
//                sqlBuilder.append("update ").append(dealFrom()).append(" set").append(dealData());
//                break;
//            case DELETE:
//                sqlBuilder.append("delete from ").append(dealFrom());
//                break;
//            case SUB_QUERY:
//            case SUB_QUERY_HAVING:
//                break;
//            default:
//                throw new InvalidSqlTypeException();
//        }
//
//        sqlBuilder.append(dealJoin()).append(dealWhere(sqlType)).append(dealGroup()).append(dealHaving(
//            sqlType)).append(dealOrderBy()).append(dealLimit()).append(dealLock());
//
//        if (union != null) {
//            FormatUtils.bracket(sqlBuilder);
//        }
//        sqlBuilder.append(dealUnion());
//
//        return sqlBuilder.toString();
//    }

    /**
     * 开启标识列插入显式值
     * @param table 表名
     * @return 语句
     */
    protected static String identityInsertOn(String table) {
        return "set IDENTITY_INSERT " + table + " ON ";
    }

}
