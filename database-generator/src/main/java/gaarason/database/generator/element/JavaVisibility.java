package gaarason.database.generator.element;

public enum JavaVisibility {
    PUBLIC("public "),
    PRIVATE("private "),
    PROTECTED("protected "),
    DEFAULT("");

    private String value;

    JavaVisibility(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }
}

