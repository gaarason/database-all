package gaarason.database.test.parent;

import gaarason.database.contract.connection.GaarasonDataSource;
import gaarason.database.contract.eloquent.Record;
import gaarason.database.contract.eloquent.RecordList;
import gaarason.database.test.models.normal.StudentEventModel;
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

    @Override
    protected GaarasonDataSource getGaarasonDataSource() {
        return studentModel.getGaarasonDataSource();
    }
    @Override
    protected List<TABLE> getInitTables() {
        return Arrays.asList(TABLE.student);
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
}
