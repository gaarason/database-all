package gaarason.database.appointment;

import java.io.Serializable;

/**
 * 字段的使用策略
 */
public enum FieldStrategy implements Serializable {
    /**
     * 从不使用
     */
    NEVER,
    /**
     * 总是使用
     */
    ALWAYS,
    /**
     * 非NULL则使用
     */
    NOT_NULL,
    /**
     * 非EMPTY则使用
     * 对于字符类型, 等价于 s !=null && s != ""
     * 对于非集合类型, 等价于 NOT_NULL
     * 对于集合类型, 等价于 !s.isEmpty()
     */
    NOT_EMPTY,
    /**
     * 跟随默认值(NOT_NULL)
     * (缺省值)
     */
    DEFAULT
}
