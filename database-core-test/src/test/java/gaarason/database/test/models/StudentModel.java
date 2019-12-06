package gaarason.database.test.models;

import gaarason.database.eloquent.Column;
import gaarason.database.eloquent.Primary;
import gaarason.database.eloquent.Table;
import gaarason.database.test.models.base.MasterSlaveModel;
import lombok.Data;

import java.util.Date;

public class StudentModel extends MasterSlaveModel<StudentModel.Entity> {

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

        @Column(name = "created_at")
        private Date createdAt;

        @Column(name = "updated_at")
        private Date updatedAt;
    }
}
