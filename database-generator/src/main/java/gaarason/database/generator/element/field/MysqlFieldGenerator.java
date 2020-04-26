package gaarason.database.generator.element.field;

import gaarason.database.core.lang.Nullable;
import gaarason.database.generator.element.JavaElement;
import gaarason.database.generator.element.JavaVisibility;
import gaarason.database.generator.support.FieldAnnotation;
import gaarason.database.utils.StringUtil;
import lombok.Data;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * mysql 数据库字段信息分析
 */
@Data
public class MysqlFieldGenerator {
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
     * @param disUpdatable 不可更新的字段
     * @return Field
     */
    public Field toField(String[] disInsertable , String[] disUpdatable) {
        Field field = new Field();
        field.setPrimary("PRI".equals(columnKey));
        field.setIncrement(extra != null && extra.contains("auto_increment"));
        field.setDataType(dataType);
        field.setColumnType(columnType);
        field.setName(nameConverter(StringUtil.lineToHump(columnName)));
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


//        ArrayList<JavaElement.Annotation> annotationList = new ArrayList<>();
//
//        field.setAnnotations(annotationList);

        return field;
    }


    /**
     * 将不合法的java标识符转换
     * @param name 未验证的java标识符
     * @return 合法的java标识符
     */
    private static String nameConverter(String name) {
        return StringUtil.isJavaIdentifier(name) ? name : "a" + StringUtil.md5(name);
    }

    /**
     * 将换行符替换
     * @param str 原字符串
     * @return 换行符替换后的字符串
     */
    @Nullable
    private static String newlineCharactersToReplace(@Nullable String str) {
        return null != str ? str
            .replace("\\\r\\\n", "")
            .replace("\\r\\n", "")
            .replace("\r\n", "")
            .replace("\\\n", "")
            .replace("\\n", "")
            .replace("\n", "")
            .replace("\"", "\\\"") : null;
    }
}
