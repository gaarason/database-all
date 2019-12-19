package gaarason.database.eloquent.enums;

import lombok.Getter;

public enum JoinType {
    LEFT("left"),
    RIGHT("right"),
    INNER("inner"),
    NATURAL("natural"),
    NATURA("natura"),
    NATURE("nature");

    @Getter
    private final String operation;

    JoinType(String operation) {
        this.operation = operation;
    }

}
