package gaarason.database.support;

import gaarason.database.core.lang.Nullable;
import lombok.Data;

import java.io.Serializable;

/**
 * jdbc字段值对象
 */
@Data
public class Column implements Serializable {

    /**
     * 字段名
     */
    private String name;

    /**
     * 字段值
     */
//    @Nullable
    private Object value;

    /**
     * 字段类型 java.sql.Types
     */
    private int type;

    private String typeName;

    private int count;

    /**
     * designated column's table's catalog name.
     */
    private String catalogName;

    private String className;

    private int displaySize;

    /**
     * 原列名
     */
    private String columnName;

    private String schemaName;

    private int precision;

    private int scale;

    private String tableName;

    /**
     * 是否自增
     */
    private boolean autoIncrement;

    private boolean caseSensitive;

    private boolean searchable;

    private boolean currency;

    private boolean nullable;

    private boolean signed;

    private boolean readOnly;

    private boolean writable;

    private boolean definitelyWritable;

}
