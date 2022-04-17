package gaarason.database.exception;

import gaarason.database.exception.base.BaseException;

/**
 * 获取信息失败
 * @author xt
 */
public class GettingInformationException extends BaseException {

    public GettingInformationException(Throwable e) {
        super(e);
    }

    public GettingInformationException(String message) {
        super(message);
    }
}
