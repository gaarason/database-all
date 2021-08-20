package gaarason.database.exception;

import gaarason.database.exception.base.BaseException;

/**
 * 不支持在group中统计
 * @author xt
 */
public class AggregatesNotSupportedGroupException extends BaseException {

    public AggregatesNotSupportedGroupException(String message) {
        super(message);
    }

}
