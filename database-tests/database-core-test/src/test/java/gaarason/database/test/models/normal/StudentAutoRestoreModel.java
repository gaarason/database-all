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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 自动恢复(AutoRestore)测试模型: 软删除 + 查询事件记录 + 软删除可重写点标记.
 * 用于验证`updateOrCreateAutoRestore`/`updateByPrimaryKeyOrCreateAutoRestore`时:
 * 1. 软删除语义(已删除的判断/恢复动作/软删除列名)均走模型自身的可重写实现(已删除的判断读取命中记录, 无额外查询)
 * 2. 命中已软删除记录时, 按需触发 恢复 与 更新 事件
 * @author xt
 */
public class StudentAutoRestoreModel extends SingleModel<StudentAutoRestoreModel.Entity, Integer> {

    /** 查询事件的触发序列 */
    public static final List<String> QUERY_EVENT_LIST = new ArrayList<>();

    /** 软删除查询作用域(反) 被调用 */
    public static boolean ONLY_TRASHED_SCOPE_CALLED = false;

    /** 已删除的判断 被调用 */
    public static boolean SOFT_DELETED_CALLED = false;

    /** 软删除标记列名 被调用 */
    public static boolean SOFT_DELETED_COLUMN_NAME_CALLED = false;

    /** 软删除恢复实现 被调用 */
    public static boolean SOFT_DELETE_RESTORE_CALLED = false;

    public static void clear() {
        QUERY_EVENT_LIST.clear();
        ONLY_TRASHED_SCOPE_CALLED = false;
        SOFT_DELETED_CALLED = false;
        SOFT_DELETED_COLUMN_NAME_CALLED = false;
        SOFT_DELETE_RESTORE_CALLED = false;
    }

    @Override
    protected boolean softDeleting() {
        return true;
    }

    // ------------- 软删除的可重写点(记录调用) ----------- //

    @Override
    public boolean isSoftDeleted(Record<Entity, Integer> record) {
        SOFT_DELETED_CALLED = true;
        return super.isSoftDeleted(record);
    }

    @Override
    public String softDeletedColumnName() {
        SOFT_DELETED_COLUMN_NAME_CALLED = true;
        return super.softDeletedColumnName();
    }

    @Override
    protected void scopeSoftDeleteOnlyTrashed(Builder<?, Entity, Integer> builder) {
        ONLY_TRASHED_SCOPE_CALLED = true;
        super.scopeSoftDeleteOnlyTrashed(builder);
    }

    @Override
    protected int softDeleteRestore(Builder<?, Entity, Integer> builder) {
        SOFT_DELETE_RESTORE_CALLED = true;
        return super.softDeleteRestore(builder);
    }

    // ------------- query event ----------- //

    @Override
    public void eventQueryRetrieving(Builder<MySqlBuilderV2<Entity, Integer>, Entity, Integer> builder) {
        QUERY_EVENT_LIST.add("retrieving");
    }

    @Override
    public void eventQueryRetrieved(Builder<MySqlBuilderV2<Entity, Integer>, Entity, Integer> builder,
        RecordList<Entity, Integer> records) {
        QUERY_EVENT_LIST.add("retrieved");
    }

    @Override
    public void eventQueryRetrieved(Builder<MySqlBuilderV2<Entity, Integer>, Entity, Integer> builder,
        Record<Entity, Integer> record) {
        QUERY_EVENT_LIST.add("retrieved");
    }

    @Override
    public void eventQueryCreating(Builder<MySqlBuilderV2<Entity, Integer>, Entity, Integer> builder) {
        QUERY_EVENT_LIST.add("creating");
    }

    @Override
    public void eventQueryCreated(Builder<MySqlBuilderV2<Entity, Integer>, Entity, Integer> builder,
        List<Integer> primaryKeyValues) {
        QUERY_EVENT_LIST.add("created");
    }

    @Override
    public void eventQueryCreated(Builder<MySqlBuilderV2<Entity, Integer>, Entity, Integer> builder,
        Integer primaryKeyValue) {
        QUERY_EVENT_LIST.add("created");
    }

    @Override
    public void eventQueryCreated(Builder<MySqlBuilderV2<Entity, Integer>, Entity, Integer> builder, int rows) {
        QUERY_EVENT_LIST.add("created");
    }

    @Override
    public void eventQueryUpdating(Builder<MySqlBuilderV2<Entity, Integer>, Entity, Integer> builder) {
        QUERY_EVENT_LIST.add("updating");
    }

    @Override
    public void eventQueryUpdated(Builder<MySqlBuilderV2<Entity, Integer>, Entity, Integer> builder, int rows) {
        QUERY_EVENT_LIST.add("updated");
    }

    @Override
    public void eventQueryRestoring(Builder<MySqlBuilderV2<Entity, Integer>, Entity, Integer> builder) {
        QUERY_EVENT_LIST.add("restoring");
    }

    @Override
    public void eventQueryRestored(Builder<MySqlBuilderV2<Entity, Integer>, Entity, Integer> builder, int rows) {
        QUERY_EVENT_LIST.add("restored");
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
