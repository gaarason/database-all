package gaarason.database.generator.element;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigInteger;
import java.sql.Time;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@EqualsAndHashCode(callSuper = true)
@Data
public class Field extends JavaElement {

    final private static Pattern tinyintPattern = Pattern.compile("tinyint\\((\\d)\\)");

    private String dataType;

    private String columnType;

    private boolean isVolatile;

    private String name;

    private String initializationString;

    private boolean isTransient;

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
                return cutClassName(Date.class);
            case "year":
            case "date":
                return cutClassName(java.sql.Date.class);
            case "time":
                return cutClassName(Time.class);
            case "blob":
                return "Byte[]";
            case "char":
            case "varchar":
            case "text":
                return cutClassName(String.class);
            case "bit":
                return cutClassName(Boolean.class);
            default:
                return cutClassName(String.class);
        }

    }

    private String dataTypeTinyint() {
        Matcher matcher = tinyintPattern.matcher(columnType);
        if (matcher.find()) {
            Integer length = new Integer(matcher.group(1));
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

    public String toString() {
        return "private " + getJavaType() + " " + name + ";";
    }
}
