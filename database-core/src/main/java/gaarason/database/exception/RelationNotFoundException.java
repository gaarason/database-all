package gaarason.database.exception;

import gaarason.database.exception.base.BaseException;

/**
 * 当在record上使用with方法标记要使用的关联关系字段时,字段检测不通过抛出
 * @author xt
 */
public class RelationNotFoundException extends BaseException {

    public RelationNotFoundException(String message) {
        super(message);
    }

}
