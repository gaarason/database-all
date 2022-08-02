package gaarason.database.annotation;

import gaarason.database.contract.support.IdGenerator;

import java.lang.annotation.*;

/**
 * 主键
 * 当 idGenerator == Auto 时, 如果 increment == true, 那么使用数据库自增主键(程序不做任何事情), 如果 increment == false,
 * 那么将根据主键的Java数据类型以及@Column等信息来确定使用含雪花ID/UUID32/UUID36/Never其中的一种;
 * 当 idGenerator != Auto 时, 将使用指定的主键生成方式进行主键生成;
 * 什么时候进行主键生成 ? 在使用实体 entity 进行新增时, 如果本字段为 null, 那么进行主键生成.
 * @author xt
 */
@Documented
@Inherited
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Primary {

    /**
     * 自增主键
     * @return 自增主键
     */
    boolean increment() default true;

    /**
     * id生成策略
     * @return id生成策略
     */
    Class<? extends IdGenerator> idGenerator() default IdGenerator.Auto.class;
}
