package gaarason.database.generator.element.field;

import gaarason.database.generator.element.JavaClassification;
import gaarason.database.generator.element.JavaElement;
import gaarason.database.generator.element.JavaVisibility;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class Field extends JavaElement {

    /**
     * varchar(20)
     */
    private String dataType;

    /**
     * int(1) unsigned
     */
    private String columnType;

    /**
     * 属性名
     */
    private String name;

    /**
     * 字段名/列名
     */
    private String columnName;

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
     * 是否主键
     */
    private Boolean primary = false;

    /**
     * 是否自增
     */
    private Boolean increment = false;

    /**
     * java中的类型
     * eg:Date
     */
    private String javaClassTypeString;

    /**
     * java数据类型分类
     */
    private JavaClassification javaClassification;

    /**
     * 最小值
     * 当
     */
    private long min = 0;

    /**
     * 最大值
     */
    private long max = 0;

    /**
     * 缩进
     * @return 缩进所需的空格符或者制表符
     */
    private static String indentation() {
        return "    ";
    }

    /**
     * @return eg:private String name;
     */
    public String toFieldName() {
        return indentation() + JavaVisibility.PRIVATE.getValue() + javaClassTypeString + " " + name + ";\n\n";
    }

    /**
     * @return eg:@Primary()
     */
    public String toAnnotationDatabasePrimary() {
        return primary ? indentation() + "@Primary(" +
                (!increment ? "increment = " + increment : "") +
                ")\n" : "";
    }

    /**
     * @return eg:@Column(name = "name", length = 20L, comment = "姓名")
     */
    public String toAnnotationDatabaseColumn() {
        return indentation() + "@Column(" +

                "name = \"" + columnName + "\"" +
                (unique ? ", unique = " + unique : "") +
                (unsigned ? ", unsigned = " + unsigned : "") +
                (nullable ? ", nullable = " + nullable : "") +
                (!insertable ? ", insertable = " + insertable : "") +
                (!updatable ? ", updatable = " + updatable : "") +
                (length != null && length != 255 ? ", length = " + length + "L" : "") +
                (!"".equals(comment) ? ", comment = \"" + comment + "\"" : "") +

                ")\n";
    }

    /**
     * @return eg:@ApiModelProperty(value = "消息id", example = "39e0f74f-93fd-224c-0078-988542600fd3", required = true)
     */
    public String toAnnotationSwaggerAnnotationsApiModelProperty() {
        // 字段没有注释的情况下, 使用字段名
        String value = "".equals(comment) ? columnName : comment;

        return indentation() + "@ApiModelProperty(" +

                "value = \"" + value + "\"" +
                (defaultValue != null ? ", example = \"" + defaultValue + "\"" : "") +
                (isRequired() ? ", required = true" : "") +

                ")\n";
    }

    /**
     * javax.validation.constraints.@Max
     * javax.validation.constraints.@Min
     * org.hibernate.validator.constraints.@Length
     * @return eg:@Length(min = 0, max = 50, message = "合同的首期缴费日期[firstPayDate]长度需要在0和50之间")
     */
    public String toAnnotationOrgHibernateValidatorConstraintValidator() {
        // 字段没有注释的情况下, 使用字段名
        String describe = "".equals(comment) ? columnName : comment;

        switch (javaClassification) {
            case NUMERIC:
                return indentation() + "@Max(value = " + max + "L, " +
                        "message = \"" + describe + "[" + columnName + "]需要小于等于" + max + "\"" +
                        ")\n" +
                        indentation() + "@Min(value = " + min + "L, " +
                        "message = \"" + describe + "[" + columnName + "]需要大于等于" + min + "\"" +
                        ")\n";
            case STRING:
                if (max == 0)
                    return "";
                else
                    return indentation() + "@Length(" +
                            "min = " + min + ", " +
                            "max = " + max + ", " +
                            "message = \"" + describe + "[" + columnName + "]长度需要在" + min + "和" + max + "之间" + "\"" +
                            ")\n";
            default:
                return "";
        }
    }

    /**
     * 字段是否必填
     * @return 是否必填
     */
    private boolean isRequired() {
        // 列不可为null, 且没有默认值, 则 require = true
        return (!nullable) && (defaultValue == null);
    }

}
