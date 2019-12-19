package gaarason.database.test.models;

import gaarason.database.eloquent.annotations.Column;
import gaarason.database.eloquent.annotations.Primary;
import gaarason.database.eloquent.Record;
import gaarason.database.eloquent.annotations.Table;
import gaarason.database.test.models.base.SingleModel;
import lombok.Data;

import java.util.Date;

public class StudentORMModel extends SingleModel<StudentORMModel.Entity> {

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

        private boolean isDeleted;

    }

    final public static String id        = "id";

    final public static String name      = "name";

    final public static String age       = "age";

    final public static String sex       = "sex";

    final public static String teacherId = "teacher_id";

    final public static String isDeleted = "is_deleted";

    final public static String createdAt = "created_at";

    final public static String updatedAt = "updated_at";

    @Override
    protected boolean softDeleting() {
        return true;
    }

    @Override
    public boolean updating(Record<Entity> record){
        if(record.getEntity().getId() == 9){
            System.out.println("正要修改id为9的数据, 但是拒绝");
            return false;
        }
        return true;
    }

    @Override
    public void retrieved(Record<Entity> entityRecord){
        System.out.println("已经从数据库中查询到数据");
    }

    public StudentORMModel.Entity getById(String id){
        return newQuery().where("id", id).firstOrFail().toObject();
    }

    public String getNameById(String id){
        return newQuery().where("id", id).select("name").firstOrFail().toObject().getName();
    }
}
