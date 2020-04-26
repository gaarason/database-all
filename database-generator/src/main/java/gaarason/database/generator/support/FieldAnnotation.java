package gaarason.database.generator.support;

import lombok.Setter;

/**
 * 字段注解生成
 */
@Setter
public class FieldAnnotation {

    /**
     * 字段名/列名
     */
    private String name;

    /**
     * 是否唯一
     */
    private Boolean unique = false;

    /**
     * 是否,仅为正数
     */
    private Boolean unsigned = false;

    /**
     * 字段可否为 null
     */
    private Boolean nullable = false;

    /**
     * 字段默认值
     */
    private String defaultValue = null;

    /**
     * 是否允许新增时赋值
     */
    private Boolean insertable = true;

    /**
     * 是否允许更新时赋值
     */
    private Boolean updatable = true;

    /**
     * 字段长度
     */
    private Long length;

    /**
     * 注释
     */
    private String comment = "";

    /**
     * 自增
     */
    private Boolean increment;

    /**
     * 是否主键
     */
    private Boolean primary;

//
//    /**
//     * @return
//     */
//    public String toPrimary() {
//        return primary ? "@Primary(" +
//            (!increment ? "increment = " + increment : "") +
//            ")\n" : "";
//    }
//
//    /**
//     * @return
//     */
//    public String toDatabaseColumn() {
//        return "@Column(" +
//
//            "name = \"" + name + "\"" +
//            (unique ? ", unique = " + unique : "") +
//            (unsigned ? ", unsigned = " + unsigned : "") +
//            (nullable ? ", nullable = " + nullable : "") +
//            (!insertable ? ", insertable = " + insertable : "") +
//            (!updatable ? ", updatable = " + updatable : "") +
//            (length != null && length != 255 ? ", length = " + length + "L" : "") +
//            (!"".equals(comment) ? ", comment = \"" + comment + "\"" : "") +
//
//            ")";
//    }
//
//    /**
//     * @return eg:@ApiModelProperty(value = "消息id", example = "39e0f74f-93fd-224c-0078-988542600fd3", required = true)
//     */
//    public String toSwaggerAnnotationsApiModelProperty() {
//        // 字段没有注释的情况下, 使用字段名
//        String value = "".equals(comment) ? name : comment;
//        // 列不可为null, 且没有默认值, 则 require = true
//        boolean require = (!nullable) && (defaultValue == null);
//
//        return "@ApiModelProperty(" +
//
//            "value = \"" + value + "\"" +
//            (defaultValue != null ? ", example = \"" + defaultValue + "\"" : "") +
//            (require ? ", required = true" : "") +
//
//            ")";
//    }
//
//    /**
//     * @return eg:@ApiModelProperty(value = "消息id", example = "39e0f74f-93fd-224c-0078-988542600fd3", required = true)
//     */
//    public String toOrgHibernateValidatorConstraintValidator() {
//        // 字段没有注释的情况下, 使用字段名
//        String value = "".equals(comment) ? name : comment;
//        // 列不可为null, 且没有默认值, 则 require = true
//        boolean require = (!nullable) && (defaultValue == null);
//
//        return "@ApiModelProperty(" +
//
//            "value = \"" + value + "\"" +
//            (defaultValue != null ? ", example = \"" + defaultValue + "\"" : "") +
//            (require ? ", required = true" : "") +
//
//            ")";
//    }
}
