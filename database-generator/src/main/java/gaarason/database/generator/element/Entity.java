package gaarason.database.generator.element;

import gaarason.database.generator.element.field.Field;

import java.util.List;

public class Entity {

    List<Field> fieldList;


    public List<Field> getFieldList() {
        return fieldList;
    }

    public void setFieldList(List<Field> fieldList) {
        this.fieldList = fieldList;
    }
}
