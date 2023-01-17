package gaarason.database.generator.element;

/**
 * java 类型标记
 * @author xt
 */
public enum JavaClassification {
    /**
     * 类型
     */
    NUMERIC("numeric "),
    STRING("string "),
    DATE("date "),
    BOOLEAN("boolean "),
    OTHER("");

    private final String value;

    JavaClassification(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }
}

