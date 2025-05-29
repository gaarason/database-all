package gaarason.database.test.models.normal;

import gaarason.database.annotation.Column;
import gaarason.database.annotation.Primary;
import gaarason.database.annotation.Table;
import gaarason.database.contract.eloquent.Builder;
import gaarason.database.contract.eloquent.Record;
import gaarason.database.contract.support.FieldStrategy;
import gaarason.database.test.config.MySqlBuilderV2;
import gaarason.database.test.models.normal.base.SingleModel;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

public class StudentORMModel extends SingleModel<StudentORMModel.Entity, Integer> {

    @Override
    protected boolean softDeleting() {
        return true;
    }

    @Override
    public boolean eventRecordUpdating(Record<Entity, Integer> record) {
        if (record.getEntity().getId() == 9) {
            System.out.println("正要修改id为9的数据, 但是拒绝");
            return false;
        }
        return true;
    }

    @Override
    public void eventRecordUpdated(Record<Entity, Integer> record) {
        Entity entity = record.getEntity();
        super.eventRecordUpdated(record);
    }

    @Override
    public void eventQueryRetrieved(Builder<MySqlBuilderV2<Entity, Integer>, Entity, Integer> builder,
            Record<Entity, Integer> record) {
        System.out.println("已经从数据库中查询到数据");
    }

    public StudentORMModel.Entity getById(String id) {
        return newQuery().where("id", id).firstOrFail().toObject();
    }

    public String getNameById(String id) {
        return newQuery().where("id", id).select("name").firstOrFail().toObject().getName();
    }

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
}
