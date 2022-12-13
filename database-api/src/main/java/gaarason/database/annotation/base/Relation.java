package gaarason.database.annotation.base;

import gaarason.database.contract.eloquent.relation.RelationSubQuery;

import java.lang.annotation.*;

/**
 * 关联关系注解
 * 用于标注其他注解是否是关联关系注解
 * @author xt
 */
@Documented
@Inherited
@Target({ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Relation {

    /**
     * 关联关系注解的解析器
     * 该解析器, 必须存在构造函数 public constructor(Field field, ModelShadowProvider modelShadowProvider, Model<?, ?> model)
     * @return 关联关系注解的解析器
     */
    Class<? extends RelationSubQuery> value();

}
