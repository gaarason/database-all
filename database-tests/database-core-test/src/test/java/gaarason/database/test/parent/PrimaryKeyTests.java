package gaarason.database.test.parent;

import gaarason.database.contract.connection.GaarasonDataSource;
import gaarason.database.contract.eloquent.Record;
import gaarason.database.test.models.normal.PrimaryKeyTestModel;
import gaarason.database.test.parent.base.BaseTests;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

@Slf4j
@FixMethodOrder(MethodSorters.JVM)
abstract public class PrimaryKeyTests extends BaseTests {

    protected static PrimaryKeyTestModel primaryKeyTestModel = new PrimaryKeyTestModel();

    protected GaarasonDataSource getGaarasonDataSource(){
        return primaryKeyTestModel.getGaarasonDataSource();
    }

    @Test
    public void test() {
        final Record<PrimaryKeyTestModel.Entity, Integer> record0 = primaryKeyTestModel.newRecord();
        record0.save();
        Assert.assertEquals(200, record0.getEntity().getId().intValue());

        final Record<PrimaryKeyTestModel.Entity, Integer> record1 = primaryKeyTestModel.newRecord();
        record1.save();
        Assert.assertEquals(201, record1.getEntity().getId().intValue());

        final Record<PrimaryKeyTestModel.Entity, Integer> record2 = primaryKeyTestModel.newRecord();
        record2.save();
        Assert.assertEquals(202, record2.getEntity().getId().intValue());

        final Record<PrimaryKeyTestModel.Entity, Integer> record3 = primaryKeyTestModel.newRecord();
        record3.save();
        Assert.assertEquals(203, record3.getEntity().getId().intValue());
    }


}
