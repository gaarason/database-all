package gaarason.database.test.models.relation.pojo;

import gaarason.database.test.models.relation.pojo.base.BaseEntity;
import gaarason.database.eloquent.annotation.Column;
import gaarason.database.eloquent.annotation.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = true)
@Table(name = "data_type")
public class DataType extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** auto generator start **/

    final public static String NAME = "name";
    final public static String AGE = "age";
    final public static String SEX = "sex";
    final public static String SEX_2 = "sex_2";
    final public static String SUBJECT = "subject";
    final public static String CREATED_AT = "created_at";
    final public static String UPDATED_AT = "updated_at";
    final public static String CREATED_TIME = "created_time";
    final public static String UPDATED_TIME = "updated_time";
    final public static String IS_DELETED = "is_deleted";
    final public static String CHAR_CHAR = "char_char";
    final public static String INTEGER = "integer";
    final public static String NUMERIC = "numeric";
    final public static String BIGINT = "bigint";
    final public static String BINARY = "binary";
    final public static String BIT = "bit";
    final public static String BLOB = "blob";
    final public static String DATE = "date";
    final public static String DECIMAL = "decimal";
    final public static String DOUBLE_D = "double_d";
    final public static String POINT = "point";
    final public static String LINESTRING = "linestring";
    final public static String GEOMETRY = "geometry";
    final public static String TEXT = "text";

    @Column(name = "name", length = 20L, comment = "姓名")
    private String name;

    @Column(name = "age", unsigned = true, comment = "年龄")
    private Integer age;

    @Column(name = "sex", unsigned = true, comment = "性别1男2女")
    private Integer sex;

    @Column(name = "sex_2", unsigned = true, comment = "test")
    private Boolean sex2;

    @Column(name = "subject", length = 20L, comment = "科目")
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

    @Column(name = "binary", length = 0L)
    private String binary;

    @Column(name = "bit")
    private Boolean bit;

    @Column(name = "blob", length = 65535L)
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

    @Column(name = "geometry", comment = "支付结果SUCCESS—支付成功REFUND—转入退款")
    private String geometry;

    @Column(name = "text", length = 65535L, comment = "问题状态现\"场检\"查 enum(''待提交'',''待整改'',''待复验'',''已通过'',''已作废'',''已关闭'')实测实量一级问题 enum(''检查中'',''待整改'',''已整改'')实测实量二级问题 enum(''检查中'',''检查完毕'', ''已整改'')")
    private String text;


    /** auto generator end **/
}