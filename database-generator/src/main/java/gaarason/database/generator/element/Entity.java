package gaarason.database.generator.element;

import gaarason.database.generator.element.field.Field;
import lombok.Data;

import java.util.List;

@Data
public class Entity {

    List<Field> fieldList;


}
