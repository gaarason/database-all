package gaarason.database.test.parent;

import gaarason.database.contract.connection.GaarasonDataSource;
import gaarason.database.contract.eloquent.Record;
import gaarason.database.contract.eloquent.RecordList;
import gaarason.database.test.models.normal.NullTestModel;
import gaarason.database.test.models.normal.StudentSoftDeleteModel;
import gaarason.database.test.parent.base.BaseTests;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import javax.sql.DataSource;
import java.util.List;

@Slf4j
@FixMethodOrder(MethodSorters.JVM)
abstract public class NullTests extends BaseTests {

    protected static NullTestModel nullTestModel = new NullTestModel();

    protected List<DataSource> getDataSourceList() {
        GaarasonDataSource gaarasonDataSourceWrapper = nullTestModel.getGaarasonDataSource();
        return gaarasonDataSourceWrapper.getMasterDataSourceList();
    }

    @Test
    public void test() {
        Integer id = 3456;

        Long count1 = nullTestModel.newQuery().count();

        Record<NullTestModel.Entity, Integer> record = nullTestModel.newRecord();
        record.getEntity().setId(id);
        record.save();
        Long count2 = nullTestModel.newQuery().count();

        Assert.assertEquals(count1.intValue() + 1, count2.intValue());

        NullTestModel.Entity entity = nullTestModel.findOrFail(id).toObject();
        Assert.assertEquals(entity.getId(), id);
        Assert.assertNull(entity.getName());
        Assert.assertNull(entity.getDateColumn());
        Assert.assertNull(entity.getTimeColumn());
        Assert.assertNull(entity.getDatetimeColumn());
        Assert.assertNull(entity.getTimestampColumn());

        NullTestModel.Entity forQuery = new NullTestModel.Entity();
        forQuery.setId(id);
        List<NullTestModel.Entity> list = nullTestModel.newQuery().where(forQuery).get().toObjectList();
        Assert.assertEquals(list.size(), 1);
    }

}
