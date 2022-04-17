package gaarason.database.generator.element;


public class PrimaryAnnotation {
    private Boolean increment;

    @Override
    public String toString() {
        return "@Primary(" +
            (!increment ? "increment = " + increment : "") +
            ")";
    }

    public Boolean getIncrement() {
        return increment;
    }

    public void setIncrement(Boolean increment) {
        this.increment = increment;
    }
}
