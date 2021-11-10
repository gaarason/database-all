package gaarason.database.test.parent;

import gaarason.database.contract.connection.GaarasonDataSource;
import gaarason.database.contract.eloquent.Record;
import gaarason.database.contract.eloquent.RecordList;
import gaarason.database.test.models.normal.NullTestModel;
import gaarason.database.test.models.normal.StudentModel;
import gaarason.database.test.parent.base.BaseTests;
import gaarason.database.util.ObjectUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.*;
import org.junit.runners.MethodSorters;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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

    @Test
    public void min() {
        final int age = records.min("age").intValue();
        Assert.assertEquals(6, age);
    }

    @Test
    public void mode() {
        List<Object> ages = records.mode("age");
        Assert.assertFalse(ages.isEmpty());
        Assert.assertEquals(2, ages.size());
        List<Byte> ageByteList = ObjectUtils.typeCast(ages);
        Assert.assertTrue(ageByteList.contains(Byte.valueOf("17")));
        Assert.assertTrue(ageByteList.contains(Byte.valueOf("11")));
    }

    @Test
    public void median() {
        BigDecimal medianAge = records.median("age");
        Assert.assertEquals(new BigDecimal("15.5"), medianAge);
    }

    @Test
    public void chunk(){
        Assert.assertEquals(10, records.size());
        List<List<Record<StudentModel.Entity, Integer>>> lists = records.chunk(4);
        Assert.assertEquals(3, lists.size());
        Assert.assertEquals(4, lists.get(0).size());
        Assert.assertEquals(4, lists.get(1).size());
        Assert.assertEquals(2, lists.get(2).size());
    }

    @Test
    public void contains(){
        Assert.assertFalse(records.contains("name", "sssssssssss"));
        Assert.assertTrue(records.contains("name", "小卡卡"));

        boolean contains = records.contains((index, e) -> Objects.equals(e.getOriginalPrimaryKeyValue(), 10));
        Assert.assertTrue(contains);
    }

    @Test
    public void count(){
        Assert.assertEquals(10, records.count());
    }

    @Test
    public void countBy(){
        Map<Byte, Integer> ageMap = ObjectUtils.typeCast(records.countBy("age"));
        Assert.assertEquals(5, ageMap.size());
        Assert.assertEquals(2, ageMap.get(Byte.valueOf("16")).intValue());
        Assert.assertEquals(3, ageMap.get(Byte.valueOf("11")).intValue());
        Assert.assertEquals(3, ageMap.get(Byte.valueOf("17")).intValue());
        Assert.assertNotEquals(3, ageMap.get(Byte.valueOf("15")).intValue());
        Assert.assertEquals(1, ageMap.get(Byte.valueOf("15")).intValue());

        Map<Byte, Integer> sexMap = records.countBy((index, e) -> e.getEntity().getSex());
        Assert.assertEquals(2, ageMap.size());
        Assert.assertEquals(6, ageMap.get(Byte.valueOf("1")).intValue());
        Assert.assertEquals(4, ageMap.get(Byte.valueOf("2")).intValue());
    }
}
