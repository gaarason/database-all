package gaarason.database.test.models;

import gaarason.database.eloquent.annotation.Column;
import gaarason.database.eloquent.annotation.Primary;
import gaarason.database.eloquent.annotation.Table;
import gaarason.database.test.models.base.SingleModel;
import lombok.Data;

import java.util.Date;

public class Student4Model extends SingleModel<Student4Model.Entity, Integer> {

    @Data
    @Table(name = "student")
    public static class Entity {
        final public static String ID         = "id";

        final public static String NAME       = "name";

        final public static String AGE        = "age";

        final public static String SEX        = "sex";

        final public static String TEACHER_ID = "teacher_id";

        final public static String IS_DELETED = "is_deleted";

        final public static String CREATED_AT = "created_at";

        final public static String UPDATED_AT = "updated_at";

        @Primary
        private Integer id;

        @Column(length = 20)
        private String name;

        private Byte age;

        private Byte sex;

        @Column(name = "teacher_id")
        private Integer teacherId;

        @Column(name = "created_at", insertable = false, updatable = false)
        private Date createdAt;

        @Column(name = "updated_at", insertable = false, updatable = false)
        private Date updatedAt;
    }
}
