package gaarason.database.query.grammars;

import gaarason.database.contract.query.Grammar;
import gaarason.database.exception.CloneNotSupportedRuntimeException;

/**
 * mysql 语法分析基类
 * @author xt
 */
public class MySqlGrammar extends BaseGrammar {

    public MySqlGrammar(String tableName) {
        super(tableName);
    }
}
