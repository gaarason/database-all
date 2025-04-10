package gaarason.database.query.grammars;

/**
 * mysql 语法分析
 * @author xt
 */
public class MySqlGrammar extends BaseGrammar {

    private static final long serialVersionUID = 1L;

    public MySqlGrammar(String tableName) {
        super(tableName, "`");
    }
}
