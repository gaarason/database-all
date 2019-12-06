package gaarason.database.generator.element;

import lombok.Data;

import java.util.List;

@Data
public class JavaElement {

    private List<String>   javaDocLines;
    private JavaVisibility visibility;
    private boolean        isStatic;
    private boolean        isFinal;
    private List<String>   annotations;

}
