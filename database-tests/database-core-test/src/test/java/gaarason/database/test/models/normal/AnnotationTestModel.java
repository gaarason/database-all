package gaarason.database.test.models.normal;

import gaarason.database.annotation.Column;
import gaarason.database.annotation.Primary;
import gaarason.database.annotation.Table;
import gaarason.database.contract.support.FieldConversion;
import gaarason.database.contract.support.FieldStrategy;
import gaarason.database.contract.support.IdGenerator;
import gaarason.database.lang.Nullable;
import gaarason.database.test.models.normal.base.SingleModel;
import lombok.Data;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

public class AnnotationTestModel extends SingleModel<AnnotationTestModel.PrimaryKeyEntity, Integer> {

    @Override
    protected boolean softDeleting() {
        return true;
    }

    @Data
    @Table(name = "null_test")
    public static class PrimaryKeyEntity implements Serializable {

        private static final long serialVersionUID = 1L;

        @Primary(idGenerator = CustomPrimaryKey.class)
        private Integer id;

        @Column(length = 20, nullable = true)
        private String name;

        @Column(name = "time_column", nullable = true)
        private LocalTime timeColumn;

        @Column(name = "date_column", nullable = true)
        private LocalDate dateColumn;

        @Column(name = "datetime_column", nullable = true)
        private LocalDateTime datetimeColumn;

        @Column(name = "timestamp_column", nullable = true)
        private Date timestampColumn;

        private boolean isDeleted;

    }

    public static class CustomPrimaryKey implements IdGenerator<Integer> {

        private final static AtomicInteger id = new AtomicInteger(200);

        @Override
        public Integer nextId() {
            return id.getAndIncrement();
        }
    }

    public enum Sex {
        MAN,
        WOMAN,
        OTHER;

        static class SexConversion implements FieldConversion {

            @Nullable
            @Override
            public Object serialize(@Nullable Object originalValue) {
                if (MAN.equals(originalValue)) {
                    return 1;
                } else if (WOMAN.equals(originalValue)) {
                    return 2;
                } else {
                    return 3;
                }
            }

            @Nullable
            @Override
            public Object deserialize(Field field, ResultSet resultSet, String columnName) throws SQLException {
                int sex = resultSet.getInt(columnName);
                if (sex == 1) {
                    return MAN;
                } else if (sex == 2) {
                    return WOMAN;
                } else {
                    return OTHER;
                }
            }

            @Nullable
            @Override
            public Object deserialize(Field field, @Nullable Object originalValue) {
                String str = String.valueOf(originalValue);
                if ("1".equals(str)) {
                    return MAN;
                } else if ("2".equals(str)) {
                    return WOMAN;
                } else {
                    return OTHER;
                }
            }
        }
    }

    @Data
    @Table(name = "student")
    public static class EnumEntity implements Serializable {

        private static final long serialVersionUID = 1L;

        @Primary
        private Integer id;

        @Column(length = 20)
        private String name;

        private Byte age;

        @Column(conversion = Sex.SexConversion.class)
        private Sex sex;

        @Column(name = "teacher_id")
        private Integer teacherId;

        @Column(name = "created_at", insertStrategy = FieldStrategy.Never.class, updateStrategy = FieldStrategy.Never.class)
        private Date createdAt;

        @Column(name = "updated_at", insertStrategy = FieldStrategy.Never.class, updateStrategy = FieldStrategy.Never.class)
        private Date updatedAt;
    }


}
