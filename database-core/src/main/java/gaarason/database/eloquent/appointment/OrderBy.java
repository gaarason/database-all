package gaarason.database.eloquent.appointment;

import lombok.Getter;

/**
 * 排序类型
 * @author xt
 */
public enum OrderBy {

    /**
     * 类型
     */
    ASC("asc"), DESC("desc");

    @Getter
    private final String operation;

    OrderBy(String operation) {
        this.operation = operation;
    }

}
