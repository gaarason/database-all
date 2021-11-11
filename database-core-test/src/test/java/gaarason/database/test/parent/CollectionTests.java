package gaarason.database.test.parent;

import gaarason.database.contract.connection.GaarasonDataSource;
import gaarason.database.contract.eloquent.Record;
import gaarason.database.contract.eloquent.RecordList;
import gaarason.database.test.models.normal.StudentModel;
import gaarason.database.test.parent.base.BaseTests;
import gaarason.database.util.ConverterUtils;
import gaarason.database.util.ObjectUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
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
        records = studentModel.newQuery().orderBy("id").get();
        log.debug("records重新初始化完成");
    }

    @Test
    public void all() {
        final List<Record<StudentModel.Entity, Integer>> all = records.all();
        int index = 0;
        Assert.assertFalse(all.isEmpty());
        for (Record<StudentModel.Entity, Integer> record : records) {
            Assert.assertEquals(record, all.get(index++));
        }
    }

    @Test
    public void avg() {
        final BigDecimal decimal = records.avg("age");
        Assert.assertEquals(new BigDecimal("13.7"), decimal);
    }

    @Test
    public void sum() {
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
        List<Byte> ages = records.mode("age");
        Assert.assertFalse(ages.isEmpty());
        Assert.assertEquals(2, ages.size());
        Assert.assertTrue(ages.contains(Byte.valueOf("17")));
        Assert.assertTrue(ages.contains(Byte.valueOf("11")));
    }

    @Test
    public void median() {
        BigDecimal medianAge = records.median("age");
        Assert.assertEquals(new BigDecimal("15.5"), medianAge);
    }

    @Test
    public void chunk() {
        Assert.assertEquals(10, records.size());
        List<List<Record<StudentModel.Entity, Integer>>> lists = records.chunk(4);
        Assert.assertEquals(3, lists.size());
        Assert.assertEquals(4, lists.get(0).size());
        Assert.assertEquals(4, lists.get(1).size());
        Assert.assertEquals(2, lists.get(2).size());
    }

    @Test
    public void contains() {
        Assert.assertFalse(records.contains("name", "sssssssssss"));
        Assert.assertTrue(records.contains("name", "小卡卡"));

        boolean contains = records.contains((index, e) -> Objects.equals(e.getOriginalPrimaryKeyValue(), 10));
        Assert.assertTrue(contains);
    }

    @Test
    public void count() {
        Assert.assertEquals(10, records.count());
    }

    @Test
    public void countBy() {
        Map<Byte, Integer> ageMap = ObjectUtils.typeCast(records.countBy("age"));
        Assert.assertEquals(5, ageMap.size());
        Assert.assertEquals(2, ageMap.get(Byte.valueOf("16")).intValue());
        Assert.assertEquals(3, ageMap.get(Byte.valueOf("11")).intValue());
        Assert.assertEquals(3, ageMap.get(Byte.valueOf("17")).intValue());
        Assert.assertNotEquals(3, ageMap.get(Byte.valueOf("15")).intValue());
        Assert.assertEquals(1, ageMap.get(Byte.valueOf("15")).intValue());

        Map<Byte, Integer> sexMap = records.countBy((index, e) -> e.getEntity().getSex());
        Assert.assertEquals(2, sexMap.size());
        Assert.assertEquals(6, sexMap.get(Byte.valueOf("1")).intValue());
        Assert.assertEquals(4, sexMap.get(Byte.valueOf("2")).intValue());
    }

    @Test
    public void every() {
        final boolean a = records.every((index, e) -> e.getMetadataMap().get("age").getValue() != null);
        Assert.assertTrue(a);

        final boolean b = records.every(
            (index, e) -> ConverterUtils.cast(e.getMetadataMap().get("age").getValue(), Byte.class) > Byte.parseByte("1"));
        Assert.assertTrue(b);

        final boolean c = records.every(
            (index, e) -> ConverterUtils.cast(e.getMetadataMap().get("age").getValue(), Byte.class) > Byte.parseByte("12"));
        Assert.assertFalse(c);
    }

    @Test
    public void filter() {
        final int filter1 = records.filter();
        Assert.assertEquals(0, filter1);

        // 保留 sex = 1 的数据
        final int filter2 = records.filter((index, e) -> (Byte.valueOf("1")).equals(e.getMetadataMap().get("sex").getValue()));
        Assert.assertEquals(4, filter2);
        Assert.assertEquals(6, records.size());
    }

    @Test
    public void reject() {
        // 移除 sex = 1 的数据
        final int reject = records.reject((index, e) -> (Byte.valueOf("1")).equals(e.getMetadataMap().get("sex").getValue()));
        Assert.assertEquals(6, reject);
        Assert.assertEquals(4, records.size());
    }

    @Test
    public void first() {
        final Record<StudentModel.Entity, Integer> first = records.first();
        assert first != null;
        Assert.assertEquals(1, first.toObject().getId().intValue());

        final Record<StudentModel.Entity, Integer> record = records.first((index, e) -> e.toObject().getId() > 4);
        assert record != null;
        Assert.assertEquals(5, record.toObject().getId().intValue());
    }

    @Test
    public void groupBy() {
        final Map<Byte, List<Record<StudentModel.Entity, Integer>>> sexMap = records.groupBy("sex");
        Assert.assertEquals(2, sexMap.size());
        final List<Record<StudentModel.Entity, Integer>> records1 = sexMap.get(Byte.valueOf("1"));
        Assert.assertEquals(6, records1.size());
        for (Record<StudentModel.Entity, Integer> record1 : records1) {
            Assert.assertEquals(1, record1.toObject().getSex().intValue());
        }
        final List<Record<StudentModel.Entity, Integer>> records2 = sexMap.get(Byte.valueOf("2"));
        Assert.assertEquals(4, records2.size());
        for (Record<StudentModel.Entity, Integer> record2 : records2) {
            Assert.assertEquals(2, record2.toObject().getSex().intValue());
        }
    }

    @Test
    public void groupBy_closure() {
        final Map<Integer, List<Record<StudentModel.Entity, Integer>>> sexMap = records.groupBy((index, e) -> e.toObject().getAge() % 3);
        Assert.assertEquals(3, sexMap.size());

        final List<Record<StudentModel.Entity, Integer>> records0 = sexMap.get(0);
        Assert.assertEquals(2, records0.size());
        for (Record<StudentModel.Entity, Integer> record0 : records0) {
            Assert.assertEquals(0, record0.toObject().getAge().intValue() % 3);
        }

        final List<Record<StudentModel.Entity, Integer>> records1 = sexMap.get(1);
        Assert.assertEquals(2, records1.size());
        for (Record<StudentModel.Entity, Integer> record1 : records1) {
            Assert.assertEquals(1, record1.toObject().getAge().intValue() % 3);
        }

        final List<Record<StudentModel.Entity, Integer>> records2 = sexMap.get(2);
        Assert.assertEquals(6, records2.size());
        for (Record<StudentModel.Entity, Integer> record2 : records2) {
            Assert.assertEquals(2, record2.toObject().getAge().intValue() % 3);
        }
    }

    @Test
    public void implode() {
        final String name = records.implode("name", "##");
        Assert.assertEquals("小明##小张##小腾##小云##小卡卡##非卡##狄龙##金庸##莫西卡##象帕", name);

        final String name2 = records.implode(e -> {
            final StudentModel.Entity entity = e.toObject();
            return entity.getId() + "*" + entity.getName();
        }, "|");
        Assert.assertEquals("1*小明|2*小张|3*小腾|4*小云|5*小卡卡|6*非卡|7*狄龙|8*金庸|9*莫西卡|10*象帕", name2);
    }

    @Test
    public void keyBy() {
        final Map<String, Record<StudentModel.Entity, Integer>> keyByName = records.keyBy("name");
        Assert.assertEquals(10, keyByName.size());
        for (Map.Entry<String, Record<StudentModel.Entity, Integer>> entry : keyByName.entrySet()) {
            Assert.assertEquals(entry.getKey(), entry.getValue().toObject().getName());
        }

        final Map<Byte, Record<StudentModel.Entity, Integer>> keyBySex = records.keyBy("sex");
        Assert.assertEquals(2, keyBySex.size());
        for (Map.Entry<Byte, Record<StudentModel.Entity, Integer>> entry : keyBySex.entrySet()) {
            Assert.assertEquals(entry.getKey(), entry.getValue().toObject().getSex());
        }

        final Map<Integer, Record<StudentModel.Entity, Integer>> keyByTeacherId = records.keyBy((index, e) -> e.toObject().getTeacherId());
        Assert.assertEquals(5, keyByTeacherId.size());
        for (Map.Entry<Integer, Record<StudentModel.Entity, Integer>> entry : keyByTeacherId.entrySet()) {
            Assert.assertEquals(entry.getKey(), entry.getValue().toObject().getTeacherId());
        }
    }

    @Test
    public void last() {
        Assert.assertEquals(records.last(), records.get(9));

        final Record<StudentModel.Entity, Integer> last = records.last((index, e) -> e.toObject().getTeacherId().equals(2));
        assert last != null;
        Assert.assertEquals(8, last.toObject().getId().intValue());
    }

    @Test
    public void mapToGroups() {
        final Map<Integer, List<String>> groups = records.mapToGroups((index, e) -> {
            final StudentModel.Entity entity1 = e.toObject();
            return entity1.getSex() + entity1.getAge();
        }, (index, e) -> {
            final StudentModel.Entity entity1 = e.toObject();
            return entity1.getId() + entity1.getName();
        });
        Assert.assertEquals(5, groups.size());
        Assert.assertEquals(1, groups.get(16).size());
        Assert.assertEquals(2, groups.get(17).size());
        Assert.assertEquals(1, groups.get(8).size());
        Assert.assertEquals(3, groups.get(13).size());
    }

    @Test
    public void mapWithKeys(){
        final Map<Integer, String> map = records.mapWithKeys((index, e) -> {
            final StudentModel.Entity entity1 = e.toObject();
            return entity1.getSex() + entity1.getAge();
        }, (index, e) -> {
            final StudentModel.Entity entity1 = e.toObject();
            return entity1.getId() + entity1.getName();
        });
        Assert.assertEquals(5, map.size());
    }

    @Test
    public void pluck(){
        final List<String> names = records.pluck("name");
        Assert.assertEquals(10, names.size());
        Assert.assertEquals("小明", names.get(0));
        Assert.assertEquals("小张", names.get(1));
        Assert.assertEquals("小腾", names.get(2));
        Assert.assertEquals("小云", names.get(3));
        Assert.assertEquals("小卡卡", names.get(4));
        Assert.assertEquals("非卡", names.get(5));
        Assert.assertEquals("狄龙", names.get(6));
        Assert.assertEquals("金庸", names.get(7));
        Assert.assertEquals("莫西卡", names.get(8));
        Assert.assertEquals("象帕", names.get(9));

        final Map<Byte, String> pluck = records.pluck("name", "age");
        Assert.assertEquals(5, pluck.size());
        Assert.assertEquals("象帕", pluck.get(Byte.parseByte("15")));
        Assert.assertEquals("莫西卡", pluck.get(Byte.parseByte("17")));
        Assert.assertEquals("小卡卡", pluck.get(Byte.parseByte("11")));
        Assert.assertEquals("非卡", pluck.get(Byte.parseByte("16")));
        Assert.assertEquals("小明", pluck.get(Byte.parseByte("6")));
    }

    @Test
    public void shift() {
        final Record<StudentModel.Entity, Integer> record1 = records.shift();
        Assert.assertEquals(1, record1.toObject().getId().intValue());
        final Record<StudentModel.Entity, Integer> record2 = records.shift();
        Assert.assertEquals(2, record2.toObject().getId().intValue());
        final Record<StudentModel.Entity, Integer> record3 = records.shift();
        Assert.assertEquals(3, record3.toObject().getId().intValue());

        Assert.assertEquals(7, records.size());
    }

    @Test
    public void pop(){
        final Record<StudentModel.Entity, Integer> record1 = records.pop();
        Assert.assertEquals(10, record1.toObject().getId().intValue());
        final Record<StudentModel.Entity, Integer> record2 = records.pop();
        Assert.assertEquals(9, record2.toObject().getId().intValue());
        final Record<StudentModel.Entity, Integer> record3 = records.pop();
        Assert.assertEquals(8, record3.toObject().getId().intValue());

        Assert.assertEquals(7, records.size());
    }

    @Test
    public void prepend(){
        final Record<StudentModel.Entity, Integer> record1 = records.pop();
        Assert.assertEquals(10, record1.toObject().getId().intValue());

        records.prepend(record1);
        Assert.assertEquals(10, records.size());
        Assert.assertEquals(10, records.get(0).toObject().getId().intValue());
        Assert.assertEquals(1, records.get(1).toObject().getId().intValue());


    }
}
