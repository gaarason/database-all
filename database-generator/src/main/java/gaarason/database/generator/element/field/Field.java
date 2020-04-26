package gaarason.database.generator.element.field;

import gaarason.database.generator.element.JavaElement;
import gaarason.database.generator.support.FieldAnnotation;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigInteger;
import java.sql.Time;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Data
@EqualsAndHashCode(callSuper = true)
public class Field extends JavaElement {

    final private static Pattern tinyintPattern = Pattern.compile("tinyint\\((\\d)\\)");

    /**
     *
     */
    private String dataType;

    /**
     *
     */
    private String columnType;

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
     * 是否主键
     */
    private Boolean primary = false;

    /**
     * 是否自增
     */
    private Boolean increment = false;

    /**
     * @return eg:private String name;
     */
    public String toFieldName() {
        return "private " + getJavaType() + " " + name + ";";
    }

    /**
     * @return eg:@Primary()
     */
    public String toAnnotationDatabasePrimary() {
        return "@Primary(" +
            (!increment ? "increment = " + increment : "") +
            ")";
    }

    /**
     * @return eg:@Column(name = "name", length = 20L, comment = "姓名")
     */
    public String toAnnotationDatabaseColumn() {
        return "@Column(" +

            "name = \"" + name + "\"" +
            (unique ? ", unique = " + unique : "") +
            (unsigned ? ", unsigned = " + unsigned : "") +
            (nullable ? ", nullable = " + nullable : "") +
            (!insertable ? ", insertable = " + insertable : "") +
            (!updatable ? ", updatable = " + updatable : "") +
            (length != null && length != 255 ? ", length = " + length + "L" : "") +
            (!"".equals(comment) ? ", comment = \"" + comment + "\"" : "") +

            ")";
    }

    /**
     * @return eg:@ApiModelProperty(value = "消息id", example = "39e0f74f-93fd-224c-0078-988542600fd3", required = true)
     */
    public String toAnnotationSwaggerAnnotationsApiModelProperty() {
        // 字段没有注释的情况下, 使用字段名
        String value = "".equals(comment) ? name : comment;
        // 列不可为null, 且没有默认值, 则 require = true
        boolean require = (!nullable) && (defaultValue == null);

        return "@ApiModelProperty(" +

            "value = \"" + value + "\"" +
            (defaultValue != null ? ", example = \"" + defaultValue + "\"" : "") +
            (require ? ", required = true" : "") +

            ")";
    }

    /**
     * @return eg:@ApiModelProperty(value = "消息id", example = "39e0f74f-93fd-224c-0078-988542600fd3", required = true)
     */
//    public String toAnnotationOrgHibernateValidatorConstraintValidator() {
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

    private String getJavaType() {
        switch (dataType.toLowerCase()) {
            case "tinyint":
                return dataTypeTinyint();
            case "smallint":
            case "mediumint":
                return cutClassName(Integer.class);
            case "int":
                return dataTypeInt();
            case "bigint":
                return dataTypeBigint();
            case "datetime":
            case "timestamp":
            case "year":
            case "date":
            case "time":
                return cutClassName(Date.class);
            case "blob":
                return "Byte[]";
            case "bit":
                return cutClassName(Boolean.class);
            case "char":
            case "varchar":
            case "text":
            default:
                return cutClassName(String.class);
        }

    }

    private String dataTypeTinyint() {
        Matcher matcher = tinyintPattern.matcher(columnType);
        if (matcher.find()) {
            Integer length = Integer.valueOf(matcher.group(1));
            if (length.equals(1)) {
                return cutClassName(Boolean.class);
            }
        }
        return columnType.contains("unsigned") ? cutClassName(Integer.class) : cutClassName(Byte.class);
    }

    private String dataTypeInt() {
        return columnType.contains("unsigned") ? cutClassName(Long.class) : cutClassName(Integer.class);
    }

    private String dataTypeBigint() {
        return columnType.contains("unsigned") ? cutClassName(BigInteger.class) : cutClassName(Long.class);

    }

    private static String cutClassName(Class classType) {
        String   className = classType.toString();
        String[] split     = className.split("\\.");
        return split[split.length - 1];
    }
}
