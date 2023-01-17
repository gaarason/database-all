package gaarason.database.generator.appointment;

public enum Style {
    NORMAL(0), ENTITY(1), ALL(2);
    public final int code;

    Style(int code) {
        this.code = code;
    }
}