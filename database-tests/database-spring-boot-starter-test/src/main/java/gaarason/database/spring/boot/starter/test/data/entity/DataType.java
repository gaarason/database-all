package gaarason.database.spring.boot.starter.test.data.entity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import gaarason.database.annotation.Column;
import gaarason.database.annotation.Primary;
import gaarason.database.annotation.Table;
import gaarason.database.contract.support.FieldConversion;
import gaarason.database.contract.support.FieldStrategy;
import gaarason.database.lang.Nullable;
import gaarason.database.spring.boot.starter.test.data.model.base.BaseModel;
import lombok.Data;
import org.springframework.stereotype.Repository;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

@Data
@Table(name = "data_type")
public class DataType implements Serializable {

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

    @Column(name = "created_at", insertStrategy = FieldStrategy.Never.class, updateStrategy = FieldStrategy.Never.class, comment = "新增时间")
    private Date createdAt;

    @Column(name = "updated_at", insertStrategy = FieldStrategy.Never.class, updateStrategy = FieldStrategy.Never.class, comment = "更新时间")
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

    @Column(name = "text", length = 65535, conversion = JsonFieldConversion.class)
    private Info text;

    /** auto generator end **/

    @Data
    public static class Info {
        private String school;
        private String country;
    }


    public static class JsonFieldConversion implements FieldConversion {

        public final static ObjectMapper MAPPER = new ObjectMapper();

        @Nullable
        @Override
        public Object serialize(Field field, @Nullable Object originalValue) {
            if (originalValue == null) {
                return null;
            }
            try {
                return MAPPER.writeValueAsString(originalValue);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }

        @Nullable
        @Override
        public Object deserialize(Field field, ResultSet resultSet, String columnName) throws SQLException {
            String valueOfDB = resultSet.getString(columnName);
            if (valueOfDB == null || valueOfDB.equals("")) {
                return valueOfDB;
            }
            try {
                return MAPPER.readValue(valueOfDB, field.getType());
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }

        @Nullable
        @Override
        public Object deserialize(Field field, @Nullable Object originalValue) {
            if (originalValue == null || "".equals(originalValue)) {
                return null;
            }
            try {
                return MAPPER.readValue(MAPPER.writeValueAsString(originalValue), field.getType());
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Repository
    public static class Model extends BaseModel<DataType, Long> {

    }
}