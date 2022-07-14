package gaarason.database.support;

import gaarason.database.annotation.Primary;
import gaarason.database.contract.support.IdGenerator;
import gaarason.database.core.Container;

import java.io.Serializable;

/**
 * 数据库主键信息
 */
public class PrimaryKeyMember extends Container.SimpleKeeper implements Serializable {

    /**
     * Field (已经 设置属性是可访问)
     */
    private final FieldMember fieldMember;

    /**
     * 数据库列信息
     * @see Primary
     */
    private final Primary primary;

    /**
     * id生成器
     */
    private final IdGenerator<?> idGenerator;

    public PrimaryKeyMember(Container container, FieldMember fieldMember) {
        super(container);
        this.fieldMember = fieldMember;
        this.primary = fieldMember.getField().getAnnotation(Primary.class);
        this.idGenerator = dealIdGenerator();
    }

    /**
     * 主键auto生成器选择
     * @return 主键生成器
     */
    private IdGenerator<?> dealIdGenerator() {
        Class<?> keyJavaType = fieldMember.getField().getType();
        if (primary.idGenerator().isAssignableFrom(IdGenerator.Auto.class)) {
            if (keyJavaType == Long.class || keyJavaType == long.class) {
                return container.getBean(IdGenerator.SnowFlakesID.class);
            } else if (keyJavaType == String.class) {
                if (fieldMember.getColumn().length() >= 36) {
                    return container.getBean(IdGenerator.UUID36.class);
                } else {
                    return container.getBean(IdGenerator.UUID32.class);
                }
            } else {
                return container.getBean(IdGenerator.Never.class);
            }
        } else {
            return container.getBean(primary.idGenerator());
        }
    }

    // ---------------------------- simple getter ---------------------------- //

    public FieldMember getFieldMember() {
        return fieldMember;
    }

    public Primary getPrimary() {
        return primary;
    }

    public IdGenerator<?> getIdGenerator() {
        return idGenerator;
    }

}

