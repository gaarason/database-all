package gaarason.database.test.parent;

import gaarason.database.appointment.Paginate;
import gaarason.database.contract.connection.GaarasonDataSource;
import gaarason.database.contract.eloquent.Record;
import gaarason.database.contract.eloquent.RecordList;
import gaarason.database.test.models.normal.StudentEventModel;
import gaarason.database.test.models.normal.StudentEventV2Model;
import gaarason.database.test.models.normal.StudentSoftDeleteModel;
import gaarason.database.test.parent.base.BaseTests;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.util.Arrays;
import java.util.List;

@Slf4j
@FixMethodOrder(MethodSorters.JVM)
abstract public class ScopeTests extends BaseTests {

    protected static StudentSoftDeleteModel studentModel = new StudentSoftDeleteModel();

    protected static StudentEventModel studentEventModel = new StudentEventModel();

    // 注解用法
    protected static StudentEventV2Model studentEventV2Model = new StudentEventV2Model();

    @Override
    protected GaarasonDataSource getGaarasonDataSource() {
        return studentModel.getGaarasonDataSource();
    }
    @Override
    protected List<TABLE> getInitTables() {
        return Arrays.asList(TABLE.student);
    }

    @Test
    public void 软删除_group() {
        Paginate<StudentSoftDeleteModel.Entity> paginate = studentModel.newQuery().select("sex").group("sex").orderBy("sex").paginate(1, 15);
        Assert.assertEquals(2, paginate.getItemList().size());
        Assert.assertEquals(1, paginate.getItemList().get(0).getSex().intValue());
        Assert.assertEquals(2, paginate.getItemList().get(1).getSex().intValue());
    }

    @Test
    public void 软删除与恢复() {
        int id = studentModel.newQuery().where("id", "5").delete();
        Assert.assertEquals(id, 1);

        Record<StudentSoftDeleteModel.Entity, Integer> record = studentModel.withTrashed().where("id", "5").first();
        Assert.assertNotNull(record);
        Assert.assertTrue(record.toObject().isDeleted());

        RecordList<StudentSoftDeleteModel.Entity, Integer> records = studentModel.onlyTrashed().get();
        Assert.assertEquals(records.size(), 1);

        int restore = studentModel.onlyTrashed().restore();
        Assert.assertEquals(restore, 1);

        Record<StudentSoftDeleteModel.Entity, Integer> record1 = studentModel.findOrFail(5);
        Assert.assertFalse(record1.toObject().isDeleted());
    }

    @Test
    public void 软删除_updateOrCreate() {
        // 软删除 id=5 (小卡卡, 11)
        int deleted = studentModel.newQuery().where("id", "5").delete();
        Assert.assertEquals(deleted, 1);

        // 链上 withTrashed(), 且实体中显式给出 isDeleted=true 作为条件 => 命中已删除行
        // 但不会自动恢复(更新依然只作用于未删除的行), 也不会实际更新已删除行的数据
        StudentSoftDeleteModel.Entity condition1 = new StudentSoftDeleteModel.Entity();
        condition1.setDeleted(true);
        StudentSoftDeleteModel.Entity complement1 = new StudentSoftDeleteModel.Entity();
        complement1.setAge(Byte.valueOf("88"));

        Record<StudentSoftDeleteModel.Entity, Integer> theRecord = studentModel.withTrashed().where("id", "5")
            .updateOrCreate(condition1, complement1);
        Assert.assertEquals(Integer.valueOf(5), theRecord.toObject().getId());

        // 软删除状态保持, 数据未实际变化
        StudentSoftDeleteModel.Entity after1 = studentModel.withTrashed().where("id", "5").firstOrFail().toObject();
        Assert.assertTrue(after1.isDeleted());
        Assert.assertEquals(Byte.valueOf("11"), after1.getAge());

        // ----------------
        // 未显式给出 isDeleted 时, 基础类型的 false 同样会进入查询条件(is_deleted=0)
        // => 即使 withTrashed() 也无法查到已删除行, 所以走新建(新行新id, 不影响已删除行)
        StudentSoftDeleteModel.Entity condition2 = new StudentSoftDeleteModel.Entity();
        StudentSoftDeleteModel.Entity complement2 = new StudentSoftDeleteModel.Entity();
        complement2.setAge(Byte.valueOf("99"));

        Record<StudentSoftDeleteModel.Entity, Integer> theRecord2 = studentModel.withTrashed().where("id", "5")
            .updateOrCreate(condition2, complement2);
        Assert.assertNotEquals(Integer.valueOf(5), theRecord2.toObject().getId());
        Assert.assertEquals(Byte.valueOf("99"), theRecord2.toObject().getAge());

        // 原 id=5 行依然为软删除状态
        Assert.assertTrue(studentModel.withTrashed().where("id", "5").firstOrFail().toObject().isDeleted());
    }

    @Test
    public void 软删除_updateOrCreateAutoRestore() {
        // 软删除 id=5 (小卡卡, 11)
        int deleted = studentModel.newQuery().where("id", "5").delete();
        Assert.assertEquals(deleted, 1);

        // ---------------- 以 isDeleted=true 作为条件, 命中已软删除的行 => 恢复 + 更新(一条sql完成)
        StudentSoftDeleteModel.Entity condition1 = new StudentSoftDeleteModel.Entity();
        condition1.setDeleted(true);
        StudentSoftDeleteModel.Entity complement1 = new StudentSoftDeleteModel.Entity();
        complement1.setAge(Byte.valueOf("88"));

        Record<StudentSoftDeleteModel.Entity, Integer> theRecord = studentModel.withTrashed().where("id", "5")
            .updateOrCreateAutoRestore(condition1, complement1);
        Assert.assertEquals(Integer.valueOf(5), theRecord.toObject().getId());

        // 恢复成功, 数据已更新(未携带的属性保持数据库中的值)
        StudentSoftDeleteModel.Entity after1 = studentModel.newQuery().where("id", "5").firstOrFail().toObject();
        Assert.assertFalse(after1.isDeleted());
        Assert.assertEquals(Byte.valueOf("88"), after1.getAge());
        Assert.assertEquals("小卡卡", after1.getName());

        // ---------------- 未显式给出 isDeleted 时, 基础类型的 false 同样会进入条件(is_deleted=0)
        // 当前行未被软删除, 所以能命中 => 仅更新
        StudentSoftDeleteModel.Entity condition2 = new StudentSoftDeleteModel.Entity();
        StudentSoftDeleteModel.Entity complement2 = new StudentSoftDeleteModel.Entity();
        complement2.setAge(Byte.valueOf("55"));

        Record<StudentSoftDeleteModel.Entity, Integer> theRecord2 = studentModel.withTrashed().where("id", "5")
            .updateOrCreateAutoRestore(condition2, complement2);
        Assert.assertEquals(Integer.valueOf(5), theRecord2.toObject().getId());
        Assert.assertEquals(Byte.valueOf("55"),
            studentModel.newQuery().where("id", "5").firstOrFail().toObject().getAge());

        // ---------------- 行已被软删除时, 基础类型的 false 无法命中 => 新增(新行新id, 不影响已删除行)
        int deleted2 = studentModel.newQuery().where("id", "5").delete();
        Assert.assertEquals(deleted2, 1);
        StudentSoftDeleteModel.Entity condition3 = new StudentSoftDeleteModel.Entity();
        StudentSoftDeleteModel.Entity complement3 = new StudentSoftDeleteModel.Entity();
        complement3.setAge(Byte.valueOf("99"));

        Record<StudentSoftDeleteModel.Entity, Integer> theRecord3 = studentModel.withTrashed().where("id", "5")
            .updateOrCreateAutoRestore(condition3, complement3);
        Assert.assertNotEquals(Integer.valueOf(5), theRecord3.toObject().getId());
        Assert.assertEquals(Byte.valueOf("99"), theRecord3.toObject().getAge());

        // 原 id=5 行依然为软删除状态
        Assert.assertTrue(studentModel.withTrashed().where("id", "5").firstOrFail().toObject().isDeleted());
    }

    @Test
    public void 硬删除() {
        int id = studentModel.newQuery().where("id", "5").forceDelete();
        Assert.assertEquals(id, 1);

        Record<StudentSoftDeleteModel.Entity, Integer> record = studentModel.withTrashed().where("id", "5").first();
        Assert.assertNull(record);

        RecordList<StudentSoftDeleteModel.Entity, Integer> records = studentModel.onlyTrashed().get();
        Assert.assertEquals(records.size(), 0);
    }

    @Test
    public void event_新增() {
        Record<StudentEventModel.Entity, Integer> record = studentEventModel.newRecord();
        StudentEventModel.Entity entity = record.getEntity();
        entity.setName("张test");
        record.save();
    }
    @Test
    public void event_查询_修改() {
        Record<StudentEventModel.Entity, Integer> record = studentEventModel.findOrFail(1);
        StudentEventModel.Entity entity = record.getEntity();
        entity.setName("张test");
        record.save();
    }
    @Test
    public void event_查询_删除() {
        Record<StudentEventModel.Entity, Integer> record = studentEventModel.findOrFail(1);
        System.out.println(record);
        record.delete();
        System.out.println(record);
    }

    @Test
    public void event_Quietly () {
        Record<StudentEventV2Model.Entity, Integer> record = studentEventV2Model.newRecord();
        StudentEventV2Model.Entity entity = record.getEntity();
        entity.setName("test");
        entity.setAge((byte) 66);
        // 事件中会拒绝 age 66 的插入
        Assert.assertFalse(record.save());
        // 事件中会拒绝 age 66 的插入, 不触发事件, 自然就成功啦
        Assert.assertTrue(record.saveQuietly());

        boolean  b = studentEventV2Model.newQuery().quiet(() -> {
            return record.save();
        });
        // 事件中会拒绝 age 66 的插入, 不触发事件, 自然就成功啦
        Assert.assertTrue(b);
    }

    @Test
    public void event_顺序 () {
        Record<StudentEventV2Model.Entity, Integer> record = studentEventV2Model.find(1);
        StudentEventV2Model.Entity entity = record.getEntity();
        entity.setName("test11");
        entity.setAge((byte) 66);
        // 事件中会拒绝 age 66 的插入
        Assert.assertFalse(record.save());
        Assert.assertNotEquals("test11", StudentEventV2Model.StudentEvent.RES);
        // 事件中会拒绝 age 66 的插入, 不触发事件, 自然就成功啦
        Assert.assertTrue(record.saveQuietly());
    }

    @Test
    public void event_顺序_事务后执行 () {
        Record<StudentEventV2Model.Entity, Integer> record = studentEventV2Model.find(1);
        StudentEventV2Model.Entity entity = record.getEntity();
        entity.setName("test112");
        entity.setAge((byte) 99);

        studentEventV2Model.newQuery().transaction(() -> {
            Assert.assertTrue(record.save());
            Assert.assertNotEquals("test112", StudentEventV2Model.StudentEvent.RES);
        });
        // 事务结束后, 才会执行事件
        Assert.assertEquals("test112", StudentEventV2Model.StudentEvent.RES);

    }
}
