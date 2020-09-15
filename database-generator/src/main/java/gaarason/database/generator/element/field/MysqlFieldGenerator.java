package gaarason.database.generator.element.field;

import gaarason.database.generator.element.JavaClassification;
import gaarason.database.generator.element.JavaVisibility;
import gaarason.database.query.range.mysql.MysqlNumericRange;
import gaarason.database.util.StringUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.regex.Matcher;

/**
 * mysql 数据库字段信息分析
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class MysqlFieldGenerator extends BaseFieldGenerator {
    /**
     * TABLE_CATALOG
     * def
     */
    private String tableCatalog;

    final public static String TABLE_CATALOG = "TABLE_CATALOG";

    /**
     * IS_NULLABLE
     * NO
     */
    private String isNullable;

    final public static String IS_NULLABLE = "IS_NULLABLE";

    /**
     * TABLE_NAME
     * student
     */
    private String tableName;

    final public static String TABLE_NAME = "TABLE_NAME";

    /**
     * TABLE_SCHEMA
     * test_master_0
     */
    private String tableSchema;

    final public static String TABLE_SCHEMA = "TABLE_SCHEMA";

    /**
     * EXTRA
     * auto_increment
     */
    private String extra;

    final public static String EXTRA = "EXTRA";

    /**
     * 字段名称 COLUMN_NAME
     * id
     */
    private String columnName;

    final public static String COLUMN_NAME = "COLUMN_NAME";

    /**
     * 索引类型 COLUMN_KEY
     * PRI
     */
    private String columnKey;

    final public static String COLUMN_KEY = "COLUMN_KEY";

    /**
     * CHARACTER_OCTET_LENGTH
     * null
     */
    private String characterOctetLength;

    final public static String CHARACTER_OCTET_LENGTH = "CHARACTER_OCTET_LENGTH";

    /**
     * NUMERIC_PRECISION
     * 10
     */
    private String numericPrecision;

    final public static String NUMERIC_PRECISION = "NUMERIC_PRECISION";

    /**
     * PRIVILEGES
     * select,insert,update,references
     */
    private String privileges;

    final public static String PRIVILEGES = "PRIVILEGES";

    /**
     * 字段注释 COLUMN_COMMENT
     */
    private String columnComment;

    final public static String COLUMN_COMMENT = "COLUMN_COMMENT";

    /**
     * DATETIME_PRECISION
     * null
     */
    private String datetimePrecision;

    final public static String DATETIME_PRECISION = "DATETIME_PRECISION";

    /**
     * COLLATION_NAME
     * utf8mb4_general_ci
     */
    private String collationName;

    final public static String COLLATION_NAME = "COLLATION_NAME";

    /**
     * NUMERIC_SCALE
     * null
     */
    private String numericScale;

    final public static String NUMERIC_SCALE = "NUMERIC_SCALE";

    /**
     * COLUMN_TYPE
     * varchar(12)
     */
    private String columnType;

    final public static String COLUMN_TYPE = "COLUMN_TYPE";

    /**
     * ORDINAL_POSITION
     * 1
     */
    private String ordinalPosition;

    final public static String ORDINAL_POSITION = "ORDINAL_POSITION";

    /**
     * CHARACTER_MAXIMUM_LENGTH
     * 12
     */
    private String characterMaximumLength;

    final public static String CHARACTER_MAXIMUM_LENGTH = "CHARACTER_MAXIMUM_LENGTH";

    /**
     * DATA_TYPE
     * varchar
     */
    private String dataType;

    final public static String DATA_TYPE = "DATA_TYPE";

    /**
     * CHARACTER_SET_NAME
     * utf8mb4
     */
    private String characterSetName;

    final public static String CHARACTER_SET_NAME = "CHARACTER_SET_NAME";

    /**
     * COLUMN_DEFAULT
     * 默认值
     */
    private String columnDefault;

    final public static String COLUMN_DEFAULT = "COLUMN_DEFAULT";

    /**
     * 生成Field
     * @param disInsertable 不可新增的字段
     * @param disUpdatable  不可更新的字段
     * @return Field
     */
    public Field toField(String[] disInsertable, String[] disUpdatable) {
        Field field = new Field();
        field.setPrimary("PRI".equals(columnKey));
        field.setIncrement(extra != null && extra.contains("auto_increment"));
        field.setDataType(dataType);
        field.setColumnType(columnType);
        field.setName(nameConverter(StringUtil.lineToHump(columnName)));
        field.setColumnName(columnName);
        field.setUnique("UNI".equals(columnKey));
        field.setUnsigned(columnType.contains("unsigned"));
        field.setNullable("YES".equals(isNullable));
        field.setInsertable(!Arrays.asList(disInsertable).contains(columnName));
        field.setUpdatable(!Arrays.asList(disUpdatable).contains(columnName));
        field.setDefaultValue(newlineCharactersToReplace(columnDefault));
        field.setLength(characterMaximumLength != null ? Long.valueOf(characterMaximumLength) : null);
        field.setComment(newlineCharactersToReplace(columnComment));
        field.setJavaDocLines(new ArrayList<>());
        field.setVisibility(JavaVisibility.PRIVATE);

        // 数据类型
        switch (dataType.toLowerCase()) {
            case "tinyint":
                dataTypeTinyint(field);
                break;
            case "smallint":
                fieldNumericAssignment(field, MysqlNumericRange.SMALLINT_UNSIGNED, MysqlNumericRange.SMALLINT);
                break;
            case "mediumint":
                fieldNumericAssignment(field, MysqlNumericRange.MEDIUMINT_UNSIGNED, MysqlNumericRange.MEDIUMINT);
                break;
            case "int":
                fieldNumericAssignment(field, MysqlNumericRange.INT_UNSIGNED, MysqlNumericRange.INT);
                break;
            case "bigint":
                fieldNumericAssignment(field, MysqlNumericRange.BIGINT_UNSIGNED, MysqlNumericRange.BIGINT);
                break;
            case "datetime":
            case "timestamp":
            case "year":
            case "date":
            case "time":
                field.setJavaClassTypeString(cutClassName(Date.class));
                field.setJavaClassification(JavaClassification.DATE);
                break;
            case "blob":
                field.setJavaClassTypeString("Byte[]");
                field.setJavaClassification(JavaClassification.OTHER);
                break;
            case "bit":
                field.setJavaClassTypeString(cutClassName(Boolean.class));
                field.setJavaClassification(JavaClassification.BOOLEAN);
                break;
            case "char":
            case "varchar":
                field.setMax(Long.parseLong(characterMaximumLength));
            case "text":
            default:
                field.setJavaClassTypeString(cutClassName(String.class));
                field.setMin(0);
                field.setJavaClassification(JavaClassification.STRING);
        }
        return field;
    }


    /**
     * tinyint分析
     * @param field 字段
     */
    protected void dataTypeTinyint(Field field) {
        Matcher matcher = tinyintPattern.matcher(columnType);
        // 特殊 tinyint(1) 表示 bool值
        if (matcher.find()) {
            Integer length = Integer.valueOf(matcher.group(1));
            if (length.equals(1)) {
                field.setJavaClassTypeString(cutClassName(Boolean.class));
                field.setJavaClassification(JavaClassification.BOOLEAN);
                return;
            }
        }
        // 普通 tinyint
        fieldNumericAssignment(field, MysqlNumericRange.TINYINT_UNSIGNED, MysqlNumericRange.TINYINT);
    }

    /**
     * 数字字符类型赋值
     * @param field             字段信息
     * @param somethingUnsigned 类型(unsigned)
     * @param something         类型
     */
    protected void fieldNumericAssignment(Field field, MysqlNumericRange somethingUnsigned,
                                          MysqlNumericRange something) {
        MysqlNumericRange numericRange = columnType.contains("unsigned") ? somethingUnsigned : something;
        field.setMax(numericRange.getMax());
        field.setMin(numericRange.getMin());
        field.setJavaClassTypeString(cutClassName(numericRange.getJavaClassType()));
        field.setJavaClassification(JavaClassification.NUMERIC);
    }

}
