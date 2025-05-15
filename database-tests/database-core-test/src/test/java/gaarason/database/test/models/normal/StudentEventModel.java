package gaarason.database.test.models.normal;

import gaarason.database.annotation.Column;
import gaarason.database.annotation.Primary;
import gaarason.database.annotation.Table;
import gaarason.database.contract.eloquent.Builder;
import gaarason.database.contract.eloquent.Record;
import gaarason.database.contract.eloquent.RecordList;
import gaarason.database.contract.support.FieldStrategy;
import gaarason.database.test.config.MySqlBuilderV2;
import gaarason.database.test.models.normal.base.SingleModel;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

public class StudentEventModel extends SingleModel<StudentEventModel.Entity, Integer> {

    @Override
    protected boolean softDeleting() {
        return true;
    }


    // ------------- record event ----------- //
    @Override
    public void eventRecordRetrieved(Record<Entity, Integer> record) {
        printCurrentMethodName();
    }

    @Override
    public boolean eventRecordSaving(Record<Entity, Integer> record) {
        printCurrentMethodName();
        return true;
    }

    @Override
    public boolean eventRecordCreating(Record<Entity, Integer> record) {
        printCurrentMethodName();
        return true;
    }


    @Override
    public boolean eventRecordUpdating(Record<Entity, Integer> record) {
        printCurrentMethodName();
        return true;
    }

    @Override
    public void eventRecordCreated(Record<Entity, Integer> record) {
        printCurrentMethodName();
    }

    @Override
    public void eventRecordUpdated(Record<Entity, Integer> record) {
        printCurrentMethodName();
    }

    @Override
    public void eventRecordSaved(Record<Entity, Integer> record) {
        printCurrentMethodName();
    }

    @Override
    public boolean eventRecordDeleting(Record<Entity, Integer> record) {
        printCurrentMethodName();
        return true;
    }

    @Override
    public void eventRecordDeleted(Record<Entity, Integer> record) {
        printCurrentMethodName();
    }

    @Override
    public boolean eventRecordRestoring(Record<Entity, Integer> record) {
        printCurrentMethodName();
        return super.eventRecordRestoring(record);
    }

    @Override
    public void eventRecordRestored(Record<Entity, Integer> record) {
        printCurrentMethodName();
    }

    // ------------- query event ----------- //


    @Override
    public void eventQueryRetrieving(Builder<MySqlBuilderV2<Entity, Integer>, Entity, Integer> builder) {
        printCurrentMethodName();
    }

    @Override
    public void eventQueryRetrieved(Builder<MySqlBuilderV2<Entity, Integer>, Entity, Integer> builder, RecordList<Entity, Integer> records) {
        printCurrentMethodName();
    }

    @Override
    public void eventQueryRetrieved(Builder<MySqlBuilderV2<Entity, Integer>, Entity, Integer> builder, Record<Entity, Integer> tkRecord) {
        printCurrentMethodName();
    }

    @Override
    public void eventQueryCreating(Builder<MySqlBuilderV2<Entity, Integer>, Entity, Integer> builder) {
        printCurrentMethodName();
    }

    @Override
    public void eventQueryCreated(Builder<MySqlBuilderV2<Entity, Integer>, Entity, Integer> builder, List<Integer> primaryKeyValues) {
        super.eventQueryCreated(builder, primaryKeyValues);
        printCurrentMethodName();
    }

    @Override
    public void eventQueryCreated(Builder<MySqlBuilderV2<Entity, Integer>, Entity, Integer> builder, Integer primaryKeyValue) {
        super.eventQueryCreated(builder, primaryKeyValue);
        printCurrentMethodName();
    }

    @Override
    public void eventQueryCreated(Builder<MySqlBuilderV2<Entity, Integer>, Entity, Integer> builder, int rows) {
        printCurrentMethodName();
        printCurrentMethodName();
    }

    @Override
    public void eventQueryUpdating(Builder<MySqlBuilderV2<Entity, Integer>, Entity, Integer> builder) {
        super.eventQueryUpdating(builder);
        printCurrentMethodName();
    }

    @Override
    public void eventQueryUpdated(Builder<MySqlBuilderV2<Entity, Integer>, Entity, Integer> builder, int rows) {
        super.eventQueryUpdated(builder, rows);
        printCurrentMethodName();
    }

    @Override
    public void eventQueryRestoring(Builder<MySqlBuilderV2<Entity, Integer>, Entity, Integer> builder) {
        super.eventQueryRestoring(builder);
        printCurrentMethodName();
    }

    @Override
    public void eventQueryRestored(Builder<MySqlBuilderV2<Entity, Integer>, Entity, Integer> builder, int rows) {
        super.eventQueryRestored(builder, rows);
        printCurrentMethodName();
    }

    @Override
    public void eventQueryDeleting(Builder<MySqlBuilderV2<Entity, Integer>, Entity, Integer> builder) {
        super.eventQueryDeleting(builder);
        printCurrentMethodName();
    }

    @Override
    public void eventQueryDeleted(Builder<MySqlBuilderV2<Entity, Integer>, Entity, Integer> builder, int rows) {
        super.eventQueryDeleted(builder, rows);
        printCurrentMethodName();
    }

    private static void printCurrentMethodName() {
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        if (stackTraceElements.length > 2) {
            System.out.println("当前方法名：" + stackTraceElements[2].getMethodName());
        } else {
            System.out.println("获取当前方法名失败");
        }
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
