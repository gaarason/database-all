package gaarason.database.support;

import gaarason.database.core.Container;
import gaarason.database.util.ObjectUtils;

import java.io.Serializable;

/**
 * 数据库主键信息
 */
public class PrimaryKeyMember<K> extends Container.SimpleKeeper implements Serializable {

    /**
     * 数据库字段信息
     */
    private final FieldMember<K> fieldMember;

    public PrimaryKeyMember(Container container, FieldMember<?> fieldMember) {
        super(container);
        this.fieldMember = ObjectUtils.typeCast(fieldMember);
    }

    // ---------------------------- simple getter ---------------------------- //

    public FieldMember<K> getFieldMember() {
        return fieldMember;
    }

}

