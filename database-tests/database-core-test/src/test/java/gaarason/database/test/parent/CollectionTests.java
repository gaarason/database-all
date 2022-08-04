package gaarason.database.test.parent;

import gaarason.database.contract.connection.GaarasonDataSource;
import gaarason.database.contract.eloquent.Record;
import gaarason.database.contract.eloquent.RecordList;
import gaarason.database.exception.AbnormalParameterException;
import gaarason.database.test.models.normal.StudentModel;
import gaarason.database.test.parent.base.BaseTests;
import gaarason.database.util.ConverterUtils;
import gaarason.database.util.ObjectUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.math.BigDecimal;
import java.util.*;

@Slf4j
@FixMethodOrder(MethodSorters.JVM)
abstract public class CollectionTests extends BaseTests {

    protected static StudentModel studentModel = new StudentModel();

    protected static RecordList<StudentModel.Entity, Integer> records;

    protected GaarasonDataSource getGaarasonDataSource() {
        return studentModel.getGaarasonDataSource();
    }
    @Override
    protected List<TABLE> getInitTables() {
        return Arrays.asList(TABLE.student);
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
    public void chunkToMap() {
        Assert.assertEquals(10, records.size());
        final List<List<Map<String, Object>>> lists = records.chunkToMap(4);
        Assert.assertEquals(3, lists.size());
        Assert.assertEquals(4, lists.get(0).size());
        Assert.assertEquals(4, lists.get(1).size());
        Assert.assertEquals(2, lists.get(2).size());
    }

    @Test
    public void contains() {
        Assert.assertFalse(records.contains("name", "sssssssssss"));
        Assert.assertTrue(records.contains("name", "小卡卡"));

        // 存在主键值为10的元素
        boolean contains = records.contains((index, e) -> Objects.equals(e.getOriginalPrimaryKeyValue(), 10));
        Assert.assertTrue(contains);
    }

    @Test
    public void count() {
        Assert.assertEquals(10, records.count());
    }

    @Test
    public void countBy() {
        // 每个年龄多少人
        Map<Byte, Integer> ageMap = ObjectUtils.typeCast(records.countBy("age"));
        Assert.assertEquals(5, ageMap.size());
        Assert.assertEquals(2, ageMap.get(Byte.valueOf("16")).intValue());
        Assert.assertEquals(3, ageMap.get(Byte.valueOf("11")).intValue());
        Assert.assertEquals(3, ageMap.get(Byte.valueOf("17")).intValue());
        Assert.assertNotEquals(3, ageMap.get(Byte.valueOf("15")).intValue());
        Assert.assertEquals(1, ageMap.get(Byte.valueOf("15")).intValue());

        // 每个性别多少人
        Map<Byte, Integer> sexMap = records.countBy((index, e) -> e.getEntity().getSex());
        Assert.assertEquals(2, sexMap.size());
        Assert.assertEquals(6, sexMap.get(Byte.valueOf("1")).intValue());
        Assert.assertEquals(4, sexMap.get(Byte.valueOf("2")).intValue());
    }

    @Test
    public void every() {
        final boolean a = records.every((index, e) -> e.getMetadataMap().get("age").getValue() != null);
        Assert.assertTrue(a);

        // 每个人的年龄都比1大吗
        final boolean b = records.every(
            (index, e) -> ConverterUtils.cast(e.getMetadataMap().get("age").getValue(), Byte.class) >
                Byte.parseByte("1"));
        Assert.assertTrue(b);

        final boolean c = records.every(
            (index, e) -> ConverterUtils.cast(e.getMetadataMap().get("age").getValue(), Byte.class) >
                Byte.parseByte("12"));
        Assert.assertFalse(c);
    }

    @Test
    public void filter() {
        final int filter1 = records.filter();
        Assert.assertEquals(0, filter1);

        // 保留 sex = 1 的数据
        final int filter2 = records.filter(
            (index, e) -> (Byte.valueOf("1")).equals(e.getMetadataMap().get("sex").getValue()));
        Assert.assertEquals(4, filter2);
        Assert.assertEquals(6, records.size());
    }

    @Test
    public void filter_2() {
        final Record<StudentModel.Entity, Integer> integerRecord = records.get(9);
        integerRecord.getMetadataMap().get("teacher_id").setValue(null);

        final int num = records.filter("teacherId");
        Assert.assertEquals(1, num);
        Assert.assertEquals(9, records.size());
        for (Record<StudentModel.Entity, Integer> record : records) {
            Assert.assertFalse(ObjectUtils.isEmpty(record.toObject().getTeacherId()));
        }
    }

    @Test
    public void reject() {
        // 移除 sex = 1 的数据
        final int reject = records.reject(
            (index, e) -> (Byte.valueOf("1")).equals(e.getMetadataMap().get("sex").getValue()));
        Assert.assertEquals(6, reject);
        Assert.assertEquals(4, records.size());
    }

    @Test
    public void first() {
        final Record<StudentModel.Entity, Integer> first = records.first();
        assert first != null;
        Assert.assertEquals(1, first.toObject().getId().intValue());

        // 返回第一个id大于4的元素
        final Record<StudentModel.Entity, Integer> record = records.first((index, e) -> e.toObject().getId() > 4);
        assert record != null;
        Assert.assertEquals(5, record.toObject().getId().intValue());
    }

    @Test
    public void groupBy() {
        // 按性别分组
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
        // 按照年龄取模的值进行分组
        final Map<Integer, List<Record<StudentModel.Entity, Integer>>> sexMap = records.groupBy(
            (index, e) -> e.toObject().getAge() % 3);
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

        final Map<Integer, Record<StudentModel.Entity, Integer>> keyByTeacherId = records.keyBy(
            (index, e) -> e.toObject().getTeacherId());
        Assert.assertEquals(5, keyByTeacherId.size());
        for (Map.Entry<Integer, Record<StudentModel.Entity, Integer>> entry : keyByTeacherId.entrySet()) {
            Assert.assertEquals(entry.getKey(), entry.getValue().toObject().getTeacherId());
        }
    }

    @Test
    public void last() {
        Assert.assertEquals(records.last(), records.get(9));

        // 返回最后一个teacherID=2的元素
        final Record<StudentModel.Entity, Integer> last = records.last(
            (index, e) -> e.toObject().getTeacherId().equals(2));
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
    public void mapWithKeys() {
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
    public void pluck() {
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
    public void pop() {
        final Record<StudentModel.Entity, Integer> record1 = records.pop();
        Assert.assertEquals(1, record1.toObject().getId().intValue());
        final Record<StudentModel.Entity, Integer> record2 = records.pop();
        Assert.assertEquals(2, record2.toObject().getId().intValue());
        final Record<StudentModel.Entity, Integer> record3 = records.pop();
        Assert.assertEquals(3, record3.toObject().getId().intValue());

        Assert.assertEquals(7, records.size());
    }

    @Test
    public void push() {
        final Record<StudentModel.Entity, Integer> element = records.get(0);
        // 等价 add(0, element)
        records.push(element);
        Assert.assertEquals(11, records.size());
        final Record<StudentModel.Entity, Integer> record1 = records.get(0);
        Assert.assertSame(element, record1);
    }

    @Test
    public void put() {
        final Record<StudentModel.Entity, Integer> record = records.get(0);
        Assert.assertEquals(10, records.size());
        Assert.assertThrows(IndexOutOfBoundsException.class, () -> {
            records.put(10, record);
        });
        Assert.assertEquals(10, records.size());

        records.put(6, record);
        Assert.assertEquals(10, records.size());

        Assert.assertSame(record, records.get(6));
    }

    @Test
    public void pull() {
        final Record<StudentModel.Entity, Integer> record1 = records.pull(6);
        Assert.assertEquals(9, records.size());
        Assert.assertEquals(7, record1.toObject().getId().intValue());

        final Record<StudentModel.Entity, Integer> record2 = records.pull(6);
        Assert.assertEquals(8, records.size());
        Assert.assertEquals(8, record2.toObject().getId().intValue());
    }

    @Test
    public void random() {
        final Record<StudentModel.Entity, Integer> random1 = records.random();
        Assert.assertNotNull(random1);

        final List<Record<StudentModel.Entity, Integer>> records1 = CollectionTests.records.random(10);
        Assert.assertEquals(10, records1.size());

        Set<Integer> unSet = new HashSet<>();
        for (Record<StudentModel.Entity, Integer> entityIntegerRecord : records1) {
            unSet.add(entityIntegerRecord.toObject().getId());
        }
        Assert.assertEquals(10, unSet.size());

        //---------------
        records.clear();
        records.add(random1);

        final Record<StudentModel.Entity, Integer> random2 = records.random();
        Assert.assertSame(random1, random2);

        final List<Record<StudentModel.Entity, Integer>> records2 = records.random(1);
        Assert.assertEquals(1, records2.size());

        //---------------
        records.clear();
        final Record<StudentModel.Entity, Integer> random3 = records.random();
        Assert.assertNull(random3);

        Assert.assertThrows(AbnormalParameterException.class, () -> {
            records.random(1);
        });
    }

    @Test
    public void reverse() {
        final List<Record<StudentModel.Entity, Integer>> recordList = records.reverse();
        Assert.assertEquals(10, recordList.size());
        for (int i = 0; i < 10; i++) {
            final Integer id = recordList.get(i).toObject().getId();
            Assert.assertEquals(10 - i, id.intValue());
        }
    }

    @Test
    public void sortBy() {
        // 按年龄小到大排序
        List<Record<StudentModel.Entity, Integer>> sortByAge = records.sortBy("age");
        Assert.assertEquals(10, sortByAge.size());
        Assert.assertEquals(6, sortByAge.get(0).toObject().getAge().intValue());
        Assert.assertEquals(11, sortByAge.get(1).toObject().getAge().intValue());
        Assert.assertEquals(11, sortByAge.get(2).toObject().getAge().intValue());
        Assert.assertEquals(11, sortByAge.get(3).toObject().getAge().intValue());
        Assert.assertEquals(15, sortByAge.get(4).toObject().getAge().intValue());
        Assert.assertEquals(16, sortByAge.get(5).toObject().getAge().intValue());
        Assert.assertEquals(16, sortByAge.get(6).toObject().getAge().intValue());

        List<Record<StudentModel.Entity, Integer>> sortByDescAge = records.sortByDesc("age");
        Assert.assertEquals(10, sortByDescAge.size());
        Assert.assertEquals(17, sortByDescAge.get(0).toObject().getAge().intValue());
        Assert.assertEquals(17, sortByDescAge.get(1).toObject().getAge().intValue());
        Assert.assertEquals(17, sortByDescAge.get(2).toObject().getAge().intValue());
        Assert.assertEquals(16, sortByDescAge.get(3).toObject().getAge().intValue());
        Assert.assertEquals(16, sortByDescAge.get(4).toObject().getAge().intValue());
        Assert.assertEquals(15, sortByDescAge.get(5).toObject().getAge().intValue());
        Assert.assertEquals(11, sortByDescAge.get(6).toObject().getAge().intValue());
    }

    @Test
    public void splice() {
        List<Record<StudentModel.Entity, Integer>> records1 = records.splice(8);
        Assert.assertEquals(2, records1.size());
        Assert.assertEquals(9, records1.get(0).toObject().getId().intValue());
        Assert.assertEquals(10, records1.get(1).toObject().getId().intValue());

        List<Record<StudentModel.Entity, Integer>> records2 = records.splice(1, 3);
        Assert.assertEquals(3, records2.size());
        Assert.assertEquals(2, records2.get(0).toObject().getId().intValue());
        Assert.assertEquals(3, records2.get(1).toObject().getId().intValue());
        Assert.assertEquals(4, records2.get(2).toObject().getId().intValue());
    }

    @Test
    public void take() {
        List<Record<StudentModel.Entity, Integer>> records1 = records.take(8);
        Assert.assertEquals(8, records1.size());
        Assert.assertEquals(1, records1.get(0).toObject().getId().intValue());
        Assert.assertEquals(2, records1.get(1).toObject().getId().intValue());
        Assert.assertEquals(3, records1.get(2).toObject().getId().intValue());
        Assert.assertEquals(4, records1.get(3).toObject().getId().intValue());
        Assert.assertEquals(5, records1.get(4).toObject().getId().intValue());
        Assert.assertEquals(6, records1.get(5).toObject().getId().intValue());
        Assert.assertEquals(7, records1.get(6).toObject().getId().intValue());
        Assert.assertEquals(8, records1.get(7).toObject().getId().intValue());
    }

    @Test
    public void take_2() {
        List<Record<StudentModel.Entity, Integer>> records2 = records.take(-3);
        Assert.assertEquals(3, records2.size());
        Assert.assertEquals(8, records2.get(0).toObject().getId().intValue());
        Assert.assertEquals(9, records2.get(1).toObject().getId().intValue());
        Assert.assertEquals(10, records2.get(2).toObject().getId().intValue());
    }

    @Test
    public void unique() {
        // 不同性别的各返回一人
        List<Record<StudentModel.Entity, Integer>> records2 = records.unique("sex");
        Assert.assertEquals(2, records2.size());
        Assert.assertEquals(1, records2.get(0).toObject().getId().intValue());
        Assert.assertEquals(3, records2.get(1).toObject().getId().intValue());
    }

}
