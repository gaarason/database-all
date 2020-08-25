package gaarason.database.test;

import gaarason.database.connections.ProxyDataSource;
import gaarason.database.eloquent.Record;
import gaarason.database.eloquent.RecordList;
import gaarason.database.test.models.StudentSoftDeleteModel;
import gaarason.database.test.parent.BaseTests;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import javax.sql.DataSource;
import java.util.List;

@Slf4j
@FixMethodOrder(MethodSorters.JVM)
public class ScopeTests extends BaseTests {
//    @Rule
//    public ExpectedException thrown = ExpectedException.none();

    private static StudentSoftDeleteModel studentModel = new StudentSoftDeleteModel();

    protected List<DataSource> getDataSourceList() {
        ProxyDataSource proxyDataSource = studentModel.getProxyDataSource();
        return proxyDataSource.getMasterDataSourceList();
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
}
