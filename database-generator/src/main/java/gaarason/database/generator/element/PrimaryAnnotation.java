package gaarason.database.generator.element;

import lombok.Data;

@Data
public class PrimaryAnnotation {
    private Boolean increment;

    @Override
    public String toString() {
        return "@Primary(" +
                (!increment ? "increment = " + increment : "") +
                ")";
    }
}
