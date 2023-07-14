package gaarason.database.generator.element.field;

import gaarason.database.annotation.Column;
import gaarason.database.annotation.Primary;
import gaarason.database.appointment.FinalVariable;
import gaarason.database.contract.support.FieldConversion;
import gaarason.database.contract.support.FieldFill;
import gaarason.database.contract.support.FieldStrategy;
import gaarason.database.generator.element.JavaClassification;
import gaarason.database.generator.element.JavaElement;
import gaarason.database.generator.element.JavaVisibility;
import gaarason.database.generator.element.base.BaseElement;
import gaarason.database.util.ObjectUtils;

/**
 * 数据库列属性
 * @author xt
 */
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
    private Boolean unique;

    /**
     * 是否,仅为正数
     */
    private Boolean unsigned;

    /**
     * 字段可否为 null
     */
    private Boolean nullable;

    /**
     * 字段默认值
     */
    private String defaultValue;

    /**
     * 查询时，指定不查询的列
     */
    private Boolean columnDisSelectable;
    /**
     * 字段, 填充方式
     */
    private Class<? extends FieldFill> columnFill;
    /**
     * 字段, 使用策略
     */
    private Class<? extends FieldStrategy> columnStrategy;
    /**
     * 字段, 新增使用策略
     */
    private Class<? extends FieldStrategy> columnInsertStrategy;
    /**
     * 字段, 更新使用策略
     */
    private Class<? extends FieldStrategy> columnUpdateStrategy;
    /**
     * 字段, 条件使用策略
     */
    private Class<? extends FieldStrategy> columnConditionStrategy;

    /**
     * 字段, 序列化与反序列化方式
     */
    private Class<? extends FieldConversion> columnConversion;

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
    private boolean primary;

    /**
     * 是否自增
     */
    private boolean increment;

    /**
     * java中的类型
     * eg:Date
     */
    private Class<?> javaClassTypeString;

    /**
     * java数据类型分类
     */
    private JavaClassification javaClassification;

    /**
     * 最小值
     */
    private long min;

    /**
     * 最大值
     */
    private long max;

    private final BaseElement element;

    public Field(BaseElement element) {
        this.element = element;
    }

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
        return indentation() + JavaVisibility.PRIVATE.getValue() + element.type2Name(javaClassTypeString) + " " + name + ";\n\n";
    }

    /**
     * @return eg:@Primary()
     */
    public String toAnnotationDatabasePrimary() {
        return primary ? indentation() + element.anno2Name(Primary.class) + "(" +
            (!increment ? "increment = " + increment : "") +
            ")\n" : "";
    }

    /**
     * @return eg:@Column(name = "name", length = 20L, comment = "姓名")
     */
    public String toAnnotationDatabaseColumn() {
        return indentation() + element.anno2Name(Column.class)+"(" +

            "name = \"" + columnName + "\"" +
            (unsigned ? ", unsigned = " + unsigned : "") +
            (nullable ? ", nullable = " + nullable : "") +
            (!ObjectUtils.isNull(columnDisSelectable) ? ", selectable = " + columnDisSelectable : "") +
            (!ObjectUtils.isNull(columnFill) ? ", fill = " + element.type2Name(columnFill) + FinalVariable.Symbol.CLASS : "") +
            (!ObjectUtils.isNull(columnStrategy) ? ", strategy = " + element.type2Name(columnStrategy)+ FinalVariable.Symbol.CLASS : "") +
            (!ObjectUtils.isNull(columnInsertStrategy) ? ", insertStrategy = " + element.type2Name(columnInsertStrategy)+ FinalVariable.Symbol.CLASS : "") +
            (!ObjectUtils.isNull(columnUpdateStrategy) ? ", updateStrategy = " + element.type2Name(columnUpdateStrategy)+ FinalVariable.Symbol.CLASS : "") +
            (!ObjectUtils.isNull(columnConditionStrategy) ? ", conditionStrategy = " + element.type2Name(columnConditionStrategy)+ FinalVariable.Symbol.CLASS : "") +
            (!ObjectUtils.isNull(columnConversion) ? ", conversion = " + element.type2Name(columnConversion)+ FinalVariable.Symbol.CLASS : "") +
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

        return indentation() + element.anno2Name("io.swagger.annotations.ApiModelProperty") + "(" +

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
                return indentation() + element.anno2Name("javax.validation.constraints.Max") + "(value = " + max + "L, " +
                    "message = \"" + describe + "[" + columnName + "]需要小于等于" + max + "\"" +
                    ")\n" +
                    indentation() + element.anno2Name("javax.validation.constraints.Min") + "(value = " + min + "L, " +
                    "message = \"" + describe + "[" + columnName + "]需要大于等于" + min + "\"" +
                    ")\n";
            case STRING:
                if (max == 0) {
                    return "";
                } else {
                    return indentation() + element.anno2Name("org.hibernate.validator.constraints.Length") + "(" +
                        "min = " + min + ", " +
                        "max = " + max + ", " +
                        "message = \"" + describe + "[" + columnName + "]长度需要在" + min + "和" + max + "之间" + "\"" +
                        ")\n";
                }
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

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public String getColumnType() {
        return columnType;
    }

    public void setColumnType(String columnType) {
        this.columnType = columnType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public boolean isUnique() {
        return unique;
    }

    public void setUnique(boolean unique) {
        this.unique = unique;
    }

    public boolean isUnsigned() {
        return unsigned;
    }

    public void setUnsigned(boolean unsigned) {
        this.unsigned = unsigned;
    }

    public boolean isNullable() {
        return nullable;
    }

    public void setNullable(boolean nullable) {
        this.nullable = nullable;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public Boolean getColumnDisSelectable() {
        return columnDisSelectable;
    }

    public void setColumnDisSelectable(Boolean columnDisSelectable) {
        this.columnDisSelectable = columnDisSelectable;
    }

    public Class<? extends FieldFill> getColumnFill() {
        return columnFill;
    }

    public void setColumnFill(Class<? extends FieldFill> columnFill) {
        this.columnFill = columnFill;
    }

    public Class<? extends FieldStrategy> getColumnStrategy() {
        return columnStrategy;
    }

    public void setColumnStrategy(Class<? extends FieldStrategy> columnStrategy) {
        this.columnStrategy = columnStrategy;
    }

    public Class<? extends FieldStrategy> getColumnInsertStrategy() {
        return columnInsertStrategy;
    }

    public void setColumnInsertStrategy(
        Class<? extends FieldStrategy> columnInsertStrategy) {
        this.columnInsertStrategy = columnInsertStrategy;
    }

    public Class<? extends FieldStrategy> getColumnUpdateStrategy() {
        return columnUpdateStrategy;
    }

    public void setColumnUpdateStrategy(
        Class<? extends FieldStrategy> columnUpdateStrategy) {
        this.columnUpdateStrategy = columnUpdateStrategy;
    }

    public Class<? extends FieldStrategy> getColumnConditionStrategy() {
        return columnConditionStrategy;
    }

    public void setColumnConditionStrategy(
        Class<? extends FieldStrategy> columnConditionStrategy) {
        this.columnConditionStrategy = columnConditionStrategy;
    }

    public Class<? extends FieldConversion> getColumnConversion() {
        return columnConversion;
    }

    public void setColumnConversion(
        Class<? extends FieldConversion> columnConversion) {
        this.columnConversion = columnConversion;
    }

    public Long getLength() {
        return length;
    }

    public void setLength(Long length) {
        this.length = length;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public boolean isPrimary() {
        return primary;
    }

    public void setPrimary(boolean primary) {
        this.primary = primary;
    }

    public boolean isIncrement() {
        return increment;
    }

    public void setIncrement(boolean increment) {
        this.increment = increment;
    }

    public Class<?> getJavaClassTypeString() {
        return javaClassTypeString;
    }

    public void setJavaClassTypeString(Class<?> javaClassTypeString) {
        this.javaClassTypeString = javaClassTypeString;
    }

    public JavaClassification getJavaClassification() {
        return javaClassification;
    }

    public void setJavaClassification(JavaClassification javaClassification) {
        this.javaClassification = javaClassification;
    }

    public long getMin() {
        return min;
    }

    public void setMin(long min) {
        this.min = min;
    }

    public long getMax() {
        return max;
    }

    public void setMax(long max) {
        this.max = max;
    }
}
