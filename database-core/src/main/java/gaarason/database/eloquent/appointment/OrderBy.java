package gaarason.database.eloquent.appointment;

import lombok.Getter;

public enum OrderBy {

    ASC("asc"), DESC("desc");

    @Getter
    private final String operation;

    OrderBy(String operation) {
        this.operation = operation;
    }

}
