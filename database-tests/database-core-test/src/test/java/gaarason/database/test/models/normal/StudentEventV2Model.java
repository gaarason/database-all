package gaarason.database.test.models.normal;

import gaarason.database.annotation.Column;
import gaarason.database.annotation.ObservedBy;
import gaarason.database.annotation.Primary;
import gaarason.database.annotation.Table;
import gaarason.database.contract.eloquent.Record;
import gaarason.database.contract.model.Event;
import gaarason.database.contract.support.FieldStrategy;
import gaarason.database.contract.support.ShouldHandleEventsAfterCommit;
import gaarason.database.test.config.MySqlBuilderV2;
import gaarason.database.test.models.normal.base.SingleModel;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@ObservedBy(StudentEventV2Model.StudentEvent.class)
public class StudentEventV2Model extends SingleModel<StudentEventV2Model.Entity, Integer> {


    @Data
    @Table(name = "student")
    public static class Entity implements Serializable {

        private static final long serialVersionUID = 1L;

        @Primary
        private Integer id;

        @Column(length = 20)
        private String name;

        private Byte age;

        private Byte sex;

        @Column(name = "teacher_id")
        private Integer teacherId;

        @Column(name = "created_at", insertStrategy = FieldStrategy.Never.class, updateStrategy = FieldStrategy.Never.class)
        private Date createdAt;

        @Column(name = "updated_at", insertStrategy = FieldStrategy.Never.class, updateStrategy = FieldStrategy.Never.class)
        private Date updatedAt;

        private Boolean isDeleted;

    }

    public static class StudentEvent implements Event<MySqlBuilderV2<StudentEventV2Model.Entity, Integer>, StudentEventV2Model.Entity, Integer>,
            ShouldHandleEventsAfterCommit {

        public static String RES = "";

        @Override
        public void eventRecordCreated(Record<Entity, Integer> record) {
            System.out.println(record);
        }

        @Override
        public boolean eventRecordSaving(Record<Entity, Integer> record) {
            Entity entity = record.getEntity();
            // 不让 age 66 更新成功
            return entity.age != 66;
        }

        @Override
        public void eventRecordUpdated(Record<Entity, Integer> record) {
            Entity entity = record.getEntity();

            // 记录 name
            RES = entity.name;
        }
    }
}
