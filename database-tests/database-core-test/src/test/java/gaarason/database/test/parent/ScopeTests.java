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
