package gaarason.database.support;

import gaarason.database.core.Container;

import java.io.Serializable;

/**
 * 数据库主键信息
 */
public class PrimaryKeyMember extends Container.SimpleKeeper implements Serializable {

    /**
     * 数据库字段信息
     */
    private final FieldMember fieldMember;

    public PrimaryKeyMember(Container container, FieldMember fieldMember) {
        super(container);
        this.fieldMember = fieldMember;
    }

    // ---------------------------- simple getter ---------------------------- //

    public FieldMember getFieldMember() {
        return fieldMember;
    }

}

