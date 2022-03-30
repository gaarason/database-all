package gaarason.database.exception;

import gaarason.database.exception.base.BaseException;

/**
 * 不合法的table
 * @author xt
 */
public class TableInvalidException extends BaseException {

    public TableInvalidException(String tableName){
        super("Table name[" + tableName + "] have no information in the Shadow.");
    }

}
