package gaarason.database.test.models;

import gaarason.database.eloquent.annotations.Column;
import gaarason.database.eloquent.annotations.Primary;
import gaarason.database.eloquent.annotations.Table;
import gaarason.database.test.models.base.Single2Model;
import lombok.Data;

import java.util.Date;

public class Student2Model extends Single2Model<Student2Model.Entity, Integer> {

    final public static String id        = "id";

    final public static String name      = "name";

    final public static String age       = "age";

    final public static String sex       = "sex";

    final public static String teacherId = "teacher_id";

    final public static String isDeleted = "is_deleted";

    final public static String createdAt = "created_at";

    final public static String updatedAt = "updated_at";

    @Data
    @Table(name = "student")
    public static class Entity {
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
