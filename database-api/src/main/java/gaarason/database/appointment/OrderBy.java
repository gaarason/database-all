package gaarason.database.appointment;

/**
 * 排序类型
 * @author xt
 */
public enum OrderBy {

    /**
     * 类型
     */
    ASC("asc"), DESC("desc");

    private final String operation;

    OrderBy(String operation) {
        this.operation = operation;
    }

    public String getOperation() {
        return operation;
    }

}
