package gaarason.database.appointment;

/**
 * 连接类型
 * @author xt
 */
public enum JoinType {

    /**
     * 类型
     */
    LEFT("left"),
    RIGHT("right"),
    INNER("inner"),
    NATURAL("natural"),
    NATURA("natura"),
    NATURE("nature");

    private final String operation;

    JoinType(String operation) {
        this.operation = operation;
    }

    public String getOperation(){
        return operation;
    }
}
