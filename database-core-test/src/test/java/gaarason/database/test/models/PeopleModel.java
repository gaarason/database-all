package gaarason.database.test.models;

import gaarason.database.eloquent.annotation.Column;
import gaarason.database.eloquent.annotation.Primary;
import gaarason.database.eloquent.annotation.Table;
import gaarason.database.test.models.base.SingleModel;
import lombok.Data;

import java.util.Date;

public class PeopleModel extends SingleModel<PeopleModel.Entity, Long> {

    final public static String id        = "id";

    final public static String name      = "name";

    final public static String age       = "age";

    final public static String sex       = "sex";

    final public static String teacherId = "teacher_id";

    final public static String isDeleted = "is_deleted";

    final public static String createdAt = "created_at";

    final public static String updatedAt = "updated_at";

    @Data
    @Table(name = "people")
    public static class Entity {
        @Primary
        private Long id;

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
