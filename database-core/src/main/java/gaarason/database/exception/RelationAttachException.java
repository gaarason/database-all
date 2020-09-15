package gaarason.database.exception;

import gaarason.database.exception.base.BaseException;

/**
 * 关联关系属性上的注解, 不在预期内
 */
public class RelationAttachException extends BaseException {

    public RelationAttachException(String message) {
        super(message);
    }

}
