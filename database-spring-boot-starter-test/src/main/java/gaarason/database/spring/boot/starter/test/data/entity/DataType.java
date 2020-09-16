package gaarason.database.spring.boot.starter.test.data.entity;

import gaarason.database.eloquent.annotation.Column;
import gaarason.database.eloquent.annotation.Primary;
import gaarason.database.eloquent.annotation.Table;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@Table(name = "data_type")
public class DataType implements Serializable {

    /**
     * auto generator start
     **/

    final public static String ID           = "id";

    final public static String NAME         = "name";

    final public static String AGE          = "age";

    final public static String SEX          = "sex";

    final public static String SUBJECT      = "subject";

    final public static String CREATED_AT   = "created_at";

    final public static String UPDATED_AT   = "updated_at";

    final public static String CREATED_TIME = "created_time";

    final public static String UPDATED_TIME = "updated_time";

    final public static String IS_DELETED   = "is_deleted";

    final public static String CHAR_CHAR    = "char_char";

    final public static String INTEGER      = "integer";

    final public static String NUMERIC      = "numeric";

    final public static String BIGINT       = "bigint";

    final public static String BINARY       = "binary";

    final public static String BIT          = "bit";

    final public static String BLOB         = "blob";

    final public static String DATE         = "date";

    final public static String DECIMAL      = "decimal";

    final public static String DOUBLE_D     = "double_d";

    final public static String POINT        = "point";

    final public static String LINESTRING   = "linestring";

    final public static String GEOMETRY     = "geometry";

    final public static String TEXT         = "text";

    @Primary()
    @Column(name = "id", unsigned = true)
    private Long id;

    @Column(name = "name", length = 20, comment = "姓名")
    private String name;

    @Column(name = "age", unsigned = true, comment = "年龄")
    private Integer age;

    @Column(name = "sex", unsigned = true, comment = "性别1男2女")
    private Integer sex;

    @Column(name = "subject", length = 20, comment = "科目")
    private String subject;

    @Column(name = "created_at", insertable = false, updatable = false, comment = "新增时间")
    private Date createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false, comment = "更新时间")
    private Date updatedAt;

    @Column(name = "created_time")
    private Date createdTime;

    @Column(name = "updated_time")
    private Date updatedTime;

    @Column(name = "is_deleted")
    private Boolean isDeleted;

    @Column(name = "char_char")
    private String charChar;

    @Column(name = "integer")
    private Integer integer;

    @Column(name = "numeric")
    private String numeric;

    @Column(name = "bigint")
    private Long bigint;

    @Column(name = "binary", length = 0)
    private String binary;

    @Column(name = "bit")
    private Boolean bit;

    @Column(name = "blob", length = 65535)
    private Byte[] blob;

    @Column(name = "date")
    private Date date;

    @Column(name = "decimal")
    private String decimal;

    @Column(name = "double_d")
    private String doubleD;

    @Column(name = "point")
    private String point;

    @Column(name = "linestring")
    private String linestring;

    @Column(name = "geometry")
    private String geometry;

    @Column(name = "text", length = 65535)
    private String text;

    /** auto generator end **/
}