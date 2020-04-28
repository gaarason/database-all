package gaarason.database.generator.element;

public enum JavaClassification {
    NUMERIC("numeric "),
    STRING("string "),
    DATE("date "),
    BOOLEAN("boolean "),
    OTHER("");

    private String value;

    JavaClassification(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }
}

