package gaarason.database.test.parent;

import gaarason.database.contract.connection.GaarasonDataSource;
import gaarason.database.contract.eloquent.Record;
import gaarason.database.contract.eloquent.RecordList;
import gaarason.database.test.models.normal.NullTestModel;
import gaarason.database.test.models.normal.StudentModel;
import gaarason.database.test.parent.base.BaseTests;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Slf4j
@FixMethodOrder(MethodSorters.JVM)
abstract public class CollectionTests extends BaseTests {

    protected static StudentModel studentModel = new StudentModel();

    protected static RecordList<StudentModel.Entity, Integer> records;

    protected List<DataSource> getDataSourceList() {
        GaarasonDataSource gaarasonDataSourceWrapper = studentModel.getGaarasonDataSource();
        return gaarasonDataSourceWrapper.getMasterDataSourceList();
    }

    @Override
    protected void otherAfter() {
        records = studentModel.newQuery().get();
        log.debug("records重新初始化完成");
    }
    @Test
    public void all(){
        final List<Record<StudentModel.Entity, Integer>> all = records.all();
        int index = 0;
        Assert.assertFalse(all.isEmpty());
        for (Record<StudentModel.Entity, Integer> record : records) {
            Assert.assertEquals(record, all.get(index++));
        }
    }

    @Test
    public void avg(){
        final BigDecimal decimal = records.avg("age");
        Assert.assertEquals(new BigDecimal("13.7"), decimal);
    }

    @Test
    public void sum(){
        final BigDecimal decimal = records.sum("age");
        Assert.assertEquals(137, decimal.intValue());
    }

    @Test
    public void max() {
        final int age = records.max("age").intValue();
        Assert.assertEquals(17, age);
    }
}
