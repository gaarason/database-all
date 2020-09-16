package gaarason.database.eloquent.appointment;

import gaarason.database.eloquent.annotation.BelongsTo;
import gaarason.database.eloquent.annotation.BelongsToMany;
import gaarason.database.eloquent.annotation.HasOneOrMany;

import java.lang.annotation.Annotation;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * 全局可用的常量
 */
public class FinalVariable {

    /**
     * 实体中普通属性支持的包装类型
     */
    public final static List<Class<?>> allowFieldTypes = Arrays.asList(Boolean.class, Byte.class, Character.class, Short.class,
            Integer.class, Long.class, Float.class, Double.class, BigInteger.class, Date.class, String.class);

    /**
     * 关联关系声明注解
     */
    public final static List<Class<? extends Annotation>> relationAnnotations = Arrays.asList(HasOneOrMany.class,
            BelongsTo.class, BelongsToMany.class);


}
