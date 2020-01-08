package gaarason.database.generator.element;

import lombok.Setter;

@Setter
public class ColumnAnnotation {

    private String name;

    private Boolean unique = false;

    private Boolean unsigned = false;

    private Boolean nullable = false;

    private Boolean insertable = true;

    private Boolean updatable = true;

    private Long length;

    private String comment = "";

    @Override
    public String toString() {
        return "@Column(" +

            "name = \"" + name + "\"" +
            (unique ? ", unique = " + unique : "") +
            (unsigned ? ", unsigned = " + unsigned : "") +
            (nullable ? ", nullable = " + nullable : "") +
            (!insertable ? ", insertable = " + insertable : "") +
            (!updatable ? ", updatable = " + updatable : "") +
            (length != null && length != 255 ? ", length = " + length : "L") +
            (!"".equals(comment) ? ", comment = \"" + comment + "\"" : "") +

            ")";
    }
}
