package gaarason.database.generator.element.field;

import gaarason.database.appointment.MysqlNumericRange;
import gaarason.database.contract.support.FieldConversion;
import gaarason.database.contract.support.FieldFill;
import gaarason.database.contract.support.FieldStrategy;
import gaarason.database.generator.element.JavaClassification;
import gaarason.database.generator.element.JavaVisibility;
import gaarason.database.generator.element.base.BaseElement;
import gaarason.database.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.regex.Matcher;

/**
 * mysql 数据库字段信息分析
 * @author xt
 */
public class MysqlFieldGenerator extends BaseFieldGenerator {

    public static final String TABLE_CATALOG = "TABLE_CATALOG";

    public static final String IS_NULLABLE = "IS_NULLABLE";

    public static final String TABLE_NAME = "TABLE_NAME";

    public static final String TABLE_SCHEMA = "TABLE_SCHEMA";

    public static final String THE_EXTRA = "EXTRA";

    public static final String COLUMN_NAME = "COLUMN_NAME";

    public static final String COLUMN_KEY = "COLUMN_KEY";

    public static final String CHARACTER_OCTET_LENGTH = "CHARACTER_OCTET_LENGTH";

    public static final String NUMERIC_PRECISION = "NUMERIC_PRECISION";

    public static final String THE_PRIVILEGES = "PRIVILEGES";

    public static final String COLUMN_COMMENT = "COLUMN_COMMENT";

    public static final String DATETIME_PRECISION = "DATETIME_PRECISION";

    public static final String COLLATION_NAME = "COLLATION_NAME";

    public static final String NUMERIC_SCALE = "NUMERIC_SCALE";

    public static final String COLUMN_TYPE = "COLUMN_TYPE";

    public static final String ORDINAL_POSITION = "ORDINAL_POSITION";

    public static final String CHARACTER_MAXIMUM_LENGTH = "CHARACTER_MAXIMUM_LENGTH";

    public static final String DATA_TYPE = "DATA_TYPE";

    public static final String CHARACTER_SET_NAME = "CHARACTER_SET_NAME";

    public static final String COLUMN_DEFAULT = "COLUMN_DEFAULT";

    /**
     * TABLE_CATALOG
     * def
     */
    private String tableCatalog;

    /**
     * IS_NULLABLE
     * NO
     */
    private String isNullable;

    /**
     * TABLE_NAME
     * student
     */
    private String tableName;

    /**
     * TABLE_SCHEMA
     * test_master_0
     */
    private String tableSchema;

    /**
     * EXTRA
     * auto_increment
     */
    private String extra;

    /**
     * 字段名称 COLUMN_NAME
     * id
     */
    private String columnName;

    /**
     * 索引类型 COLUMN_KEY
     * PRI
     */
    private String columnKey;

    /**
     * CHARACTER_OCTET_LENGTH
     * null
     */
    private String characterOctetLength;

    /**
     * NUMERIC_PRECISION
     * 10
     */
    private String numericPrecision;

    /**
     * PRIVILEGES
     * select,insert,update,references
     */
    private String privileges;

    /**
     * 字段注释 COLUMN_COMMENT
     */
    private String columnComment;

    /**
     * DATETIME_PRECISION
     * null
     */
    private String datetimePrecision;

    /**
     * COLLATION_NAME
     * utf8mb4_general_ci
     */
    private String collationName;

    /**
     * NUMERIC_SCALE
     * null
     */
    private String numericScale;

    /**
     * COLUMN_TYPE
     * varchar(12)
     */
    private String columnType;

    /**
     * ORDINAL_POSITION
     * 1
     */
    private String ordinalPosition;

    /**
     * CHARACTER_MAXIMUM_LENGTH
     * 12
     */
    private String characterMaximumLength;

    /**
     * DATA_TYPE
     * varchar
     */
    private String dataType;

    /**
     * CHARACTER_SET_NAME
     * utf8mb4
     */
    private String characterSetName;

    /**
     * COLUMN_DEFAULT
     * 默认值
     */
    private String columnDefault;

    /**
     * 查询时，指定不查询的列
     */
    private String[] columnDisSelectable = {};

    /**
     * 字段, 填充方式
     */
    private Map<String , Class<? extends FieldFill>> columnFill = new HashMap<>();
    /**
     * 字段, 使用策略
     */
    private Map<String , Class<? extends FieldStrategy>> columnStrategy = new HashMap<>();
    /**
     * 字段, 新增使用策略
     */
    private Map<String , Class<? extends FieldStrategy>> columnInsertStrategy = new HashMap<>();
    /**
     * 字段, 更新使用策略
     */
    private Map<String , Class<? extends FieldStrategy>> columnUpdateStrategy = new HashMap<>();
    /**
     * 字段, 条件使用策略
     */
    private Map<String , Class<? extends FieldStrategy>> columnConditionStrategy = new HashMap<>();

    /**
     * 字段, 序列化与反序列化方式
     */
    private Map<String , Class<? extends FieldConversion>> columnConversion = new HashMap<>();

    /**
     * 生成Field
     * @return Field
     */
    @Override
    public Field toField(BaseElement element) {
        Field field = new Field(element);
        field.setPrimary("PRI".equals(columnKey));
        field.setIncrement(extra != null && extra.contains("auto_increment"));
        field.setDataType(dataType);
        field.setColumnType(columnType);
        field.setName(nameConverter(StringUtils.lineToHump(columnName)));
        field.setColumnName(columnName);
        field.setUnique("UNI".equals(columnKey));
        field.setUnsigned(columnType.contains("unsigned"));
        field.setNullable("YES".equals(isNullable));
        field.setDefaultValue(safeCharactersToReplace(columnDefault));
        field.setLength(characterMaximumLength != null ? Long.valueOf(characterMaximumLength) : null);
        field.setComment(safeCharactersToReplace(columnComment));
        field.setJavaDocLines(new ArrayList<>());
        field.setVisibility(JavaVisibility.PRIVATE);

        if(Arrays.asList(columnDisSelectable).contains(columnName)){
            field.setColumnDisSelectable(true);
        }

        if(columnFill.containsKey(columnName)){
            field.setColumnFill(columnFill.get(columnName));
        }
        if(columnStrategy.containsKey(columnName)){
            field.setColumnStrategy(columnStrategy.get(columnName));
        }
        if(columnInsertStrategy.containsKey(columnName)){
            field.setColumnInsertStrategy(columnInsertStrategy.get(columnName));
        }
        if(columnUpdateStrategy.containsKey(columnName)){
            field.setColumnUpdateStrategy(columnUpdateStrategy.get(columnName));
        }
        if(columnConditionStrategy.containsKey(columnName)){
            field.setColumnConditionStrategy(columnConditionStrategy.get(columnName));
        }
        if(columnConversion.containsKey(columnName)){
            field.setColumnConversion(columnConversion.get(columnName));
        }

        // 数据类型
        switch (dataType.toLowerCase(Locale.ENGLISH)) {
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
            case "timestamp":
            case "datetime":
            case "year":
                field.setJavaClassTypeString((LocalDateTime.class));
                field.setJavaClassification(JavaClassification.DATE);
                break;
            case "date":
                field.setJavaClassTypeString((LocalDate.class));
                field.setJavaClassification(JavaClassification.DATE);
                break;
            case "time":
                field.setJavaClassTypeString((LocalTime.class));
                field.setJavaClassification(JavaClassification.DATE);
                break;
            case "blob":
                field.setJavaClassTypeString(Byte[].class);
                field.setJavaClassification(JavaClassification.OTHER);
                break;
            case "bit":
                field.setJavaClassTypeString((Boolean.class));
                field.setJavaClassification(JavaClassification.BOOLEAN);
                break;
            case "char":
            case "varchar":
                field.setMax(Long.parseLong(characterMaximumLength));
                field.setJavaClassTypeString((String.class));
                field.setMin(0);
                field.setJavaClassification(JavaClassification.STRING);
                break;
            case "text":
            default:
                field.setJavaClassTypeString((String.class));
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
            Integer tinyintLength = Integer.valueOf(matcher.group(1));
            if (tinyintLength.equals(1)) {
                field.setJavaClassTypeString((Boolean.class));
                field.setJavaClassification(JavaClassification.BOOLEAN);
                return;
            }
        }
        // 普通 tinyint
        fieldNumericAssignment(field, MysqlNumericRange.TINYINT_UNSIGNED, MysqlNumericRange.TINYINT);
    }

    /**
     * 数字字符类型赋值
     * @param field 字段信息
     * @param somethingUnsigned 类型(unsigned)
     * @param something 类型
     */
    protected void fieldNumericAssignment(Field field, MysqlNumericRange somethingUnsigned,
        MysqlNumericRange something) {
        MysqlNumericRange numericRange = columnType.contains("unsigned") ? somethingUnsigned : something;
        field.setMax(numericRange.getMax());
        field.setMin(numericRange.getMin());
        field.setJavaClassTypeString((numericRange.getJavaClassType()));
        field.setJavaClassification(JavaClassification.NUMERIC);
    }

    public String getTableCatalog() {
        return tableCatalog;
    }

    public void setTableCatalog(String tableCatalog) {
        this.tableCatalog = tableCatalog;
    }

    public String getIsNullable() {
        return isNullable;
    }

    public void setIsNullable(String isNullable) {
        this.isNullable = isNullable;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getTableSchema() {
        return tableSchema;
    }

    public void setTableSchema(String tableSchema) {
        this.tableSchema = tableSchema;
    }

    public String getExtra() {
        return extra;
    }

    public void setExtra(String extra) {
        this.extra = extra;
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public String getColumnKey() {
        return columnKey;
    }

    public void setColumnKey(String columnKey) {
        this.columnKey = columnKey;
    }

    public String getCharacterOctetLength() {
        return characterOctetLength;
    }

    public void setCharacterOctetLength(String characterOctetLength) {
        this.characterOctetLength = characterOctetLength;
    }

    public String getNumericPrecision() {
        return numericPrecision;
    }

    public void setNumericPrecision(String numericPrecision) {
        this.numericPrecision = numericPrecision;
    }

    public String getPrivileges() {
        return privileges;
    }

    public void setPrivileges(String privileges) {
        this.privileges = privileges;
    }

    public String getColumnComment() {
        return columnComment;
    }

    public void setColumnComment(String columnComment) {
        this.columnComment = columnComment;
    }

    public String getDatetimePrecision() {
        return datetimePrecision;
    }

    public void setDatetimePrecision(String datetimePrecision) {
        this.datetimePrecision = datetimePrecision;
    }

    public String getCollationName() {
        return collationName;
    }

    public void setCollationName(String collationName) {
        this.collationName = collationName;
    }

    public String getNumericScale() {
        return numericScale;
    }

    public void setNumericScale(String numericScale) {
        this.numericScale = numericScale;
    }

    public String getColumnType() {
        return columnType;
    }

    public void setColumnType(String columnType) {
        this.columnType = columnType;
    }

    public String getOrdinalPosition() {
        return ordinalPosition;
    }

    public void setOrdinalPosition(String ordinalPosition) {
        this.ordinalPosition = ordinalPosition;
    }

    public String getCharacterMaximumLength() {
        return characterMaximumLength;
    }

    public void setCharacterMaximumLength(String characterMaximumLength) {
        this.characterMaximumLength = characterMaximumLength;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public String getCharacterSetName() {
        return characterSetName;
    }

    public void setCharacterSetName(String characterSetName) {
        this.characterSetName = characterSetName;
    }

    public String getColumnDefault() {
        return columnDefault;
    }

    public void setColumnDefault(String columnDefault) {
        this.columnDefault = columnDefault;
    }
    public String[] getColumnDisSelectable() {
        return columnDisSelectable;
    }

    public void setColumnDisSelectable(String[] columnDisSelectable) {
        this.columnDisSelectable = columnDisSelectable;
    }

    public Map<String, Class<? extends FieldFill>> getColumnFill() {
        return columnFill;
    }

    public void setColumnFill(
        Map<String, Class<? extends FieldFill>> columnFill) {
        this.columnFill = columnFill;
    }

    public Map<String, Class<? extends FieldStrategy>> getColumnStrategy() {
        return columnStrategy;
    }

    public void setColumnStrategy(
        Map<String, Class<? extends FieldStrategy>> columnStrategy) {
        this.columnStrategy = columnStrategy;
    }

    public Map<String, Class<? extends FieldStrategy>> getColumnInsertStrategy() {
        return columnInsertStrategy;
    }

    public void setColumnInsertStrategy(
        Map<String, Class<? extends FieldStrategy>> columnInsertStrategy) {
        this.columnInsertStrategy = columnInsertStrategy;
    }

    public Map<String, Class<? extends FieldStrategy>> getColumnUpdateStrategy() {
        return columnUpdateStrategy;
    }

    public void setColumnUpdateStrategy(
        Map<String, Class<? extends FieldStrategy>> columnUpdateStrategy) {
        this.columnUpdateStrategy = columnUpdateStrategy;
    }

    public Map<String, Class<? extends FieldStrategy>> getColumnConditionStrategy() {
        return columnConditionStrategy;
    }

    public void setColumnConditionStrategy(
        Map<String, Class<? extends FieldStrategy>> columnConditionStrategy) {
        this.columnConditionStrategy = columnConditionStrategy;
    }

    public Map<String, Class<? extends FieldConversion>> getColumnConversion() {
        return columnConversion;
    }

    public void setColumnConversion(
        Map<String, Class<? extends FieldConversion>> columnColumnConversion) {
        this.columnConversion = columnColumnConversion;
    }
}
