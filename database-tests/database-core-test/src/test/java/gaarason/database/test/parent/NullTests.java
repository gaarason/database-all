package gaarason.database.test.parent;

import gaarason.database.annotation.Primary;
import gaarason.database.contract.connection.GaarasonDataSource;
import gaarason.database.contract.eloquent.Record;
import gaarason.database.contract.eloquent.RecordList;
import gaarason.database.test.models.normal.NullTestModel;
import gaarason.database.test.models.normal.NullTestWIthFillModel;
import gaarason.database.test.parent.base.BaseTests;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Slf4j
@FixMethodOrder(MethodSorters.JVM)
abstract public class NullTests extends BaseTests {

    protected static NullTestModel nullTestModel = new NullTestModel();

    protected static NullTestWIthFillModel nullTestWIthFillModel = new NullTestWIthFillModel();

    protected GaarasonDataSource getGaarasonDataSource() {
        return nullTestModel.getGaarasonDataSource();
    }
    @Override
    protected List<TABLE> getInitTables() {
        return Arrays.asList(TABLE.null_test);
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

        NullTestModel.Entity entitySon = new NullTestModel.Entity() {

            @Primary
            public Integer id;
        };
        List<NullTestModel.Entity> list2 = nullTestModel.newQuery().where(entitySon).get().toObjectList();
        Assert.assertEquals(2, list2.size());
    }

    @Test
    public void test1() {
        final RecordList<NullTestModel.Entity, Integer> records = nullTestModel.withTrashed().get();
        System.out.println(records);
        for (Record<NullTestModel.Entity, Integer> record : records) {
            System.out.println(record);
        }

        final Map<Object, Record<NullTestModel.Entity, Integer>> id = records.keyBy("id");
        System.out.println(id);

    }

    @Test
    public void fill() {
        Record<NullTestWIthFillModel.Entity, Integer> record = nullTestWIthFillModel.newRecord();

        // ORM 新增
        boolean save = record.save();
        Assert.assertTrue(save);
        NullTestWIthFillModel.Entity entity = record.getEntity();
        System.out.println(entity);
        Assert.assertNotNull(entity.getId());
        Assert.assertNotNull(entity.getTimeColumn());
        Assert.assertNull(entity.getDateColumn());

        Integer id = entity.getId();
        // query 查询验证
        NullTestWIthFillModel.Entity entity1 = nullTestWIthFillModel.findOrFail(id).toObject();
        System.out.println(entity1);
//  有小数点, 不方便断言      Assert.assertEquals(entity1.getTimeColumn(),entity.getTimeColumn());
        Assert.assertEquals(entity1.getDateColumn(),entity.getDateColumn());

        // ORM 更新
        String name = "asdaa1111";
        entity.setName(name);
        boolean save2 = record.save();
        Assert.assertTrue(save2);
        System.out.println(entity);
        Assert.assertEquals(name, entity.getName());
        Assert.assertNotNull(entity.getTimeColumn());
        Assert.assertNotNull(entity.getDateColumn());
        // query 查询验证
        NullTestWIthFillModel.Entity entity2 = nullTestWIthFillModel.findOrFail(id).toObject();
        System.out.println(entity2);
//  有小数点, 不方便断言        Assert.assertEquals(entity2.getTimeColumn(),entity.getTimeColumn());
        Assert.assertEquals(entity2.getDateColumn(),entity.getDateColumn());

    }

}
