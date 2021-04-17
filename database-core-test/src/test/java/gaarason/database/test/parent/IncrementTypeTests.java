package gaarason.database.test.parent;

import gaarason.database.contract.connection.GaarasonDataSource;
import gaarason.database.contract.eloquent.Record;
import gaarason.database.provider.DBShadowProvider;
import gaarason.database.support.Column;
import gaarason.database.support.DBColumn;
import gaarason.database.test.models.normal.PeopleModel;
import gaarason.database.test.parent.base.BaseTests;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import javax.sql.DataSource;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@FixMethodOrder(MethodSorters.JVM)
abstract public class IncrementTypeTests extends BaseTests {

    protected static PeopleModel peopleModel = new PeopleModel();

    protected List<DataSource> getDataSourceList() {
        GaarasonDataSource gaarasonDataSourceWrapper = peopleModel.getGaarasonDataSource();
        return gaarasonDataSourceWrapper.getMasterDataSourceList();
    }

    @Test
    public void ss(){
        GaarasonDataSource gaarasonDataSource = peopleModel.getGaarasonDataSource();
        Map<String, DBColumn> student = DBShadowProvider.getTable(gaarasonDataSource, "data_type");

        Set<String> strings = student.keySet();

        for (String string : strings) {
            System.out.println(string);
            System.out.println(student.get(string));
            System.out.println();

        }

    }

    @Test
    public void 新增_返回自增id() {
        PeopleModel.Entity entity = new PeopleModel.Entity();
        entity.setName("姓名");
        entity.setAge(Byte.valueOf("13"));
        entity.setSex(Byte.valueOf("1"));
        entity.setTeacherId(0);
        entity.setCreatedAt(new Date(1312312312));
        entity.setUpdatedAt(new Date(1312312312));
        System.out.println("原对象 " + entity);
        Long newId = peopleModel.newQuery().insertGetIdOrFail(entity);
        Assert.assertEquals(20L, newId.longValue());
        System.out.println("新对象 " + entity);

        Record<PeopleModel.Entity, Long> entityLongRecord = peopleModel.find(20L);
        System.out.println(entityLongRecord);
    }


    @Test
    public void 新增_空插入_返回自增id() {
        Long aLong = peopleModel.newQuery().executeGetId("insert into people values ()", new ArrayList<>());
        Assert.assertNotNull(aLong);
        Assert.assertEquals(20, aLong.intValue());

        List<String> vList = new ArrayList<>();
        vList.add("aaaccc");
        Long qwww = peopleModel.newQuery().select("name").value(vList).insertGetId();
        ;
        Assert.assertNotNull(qwww);
        Assert.assertEquals(21, qwww.intValue());

        Long dd = peopleModel.newQuery().value(new ArrayList<>()).insertGetId();
        ;
        Assert.assertNotNull(dd);
        Assert.assertEquals(22, dd.intValue());


        Long id = peopleModel.newQuery().insertGetIdOrFail();
        Assert.assertEquals(23, id.intValue());
    }

    @Test
    public void 新增_返回自增idOrFail() {
        PeopleModel.Entity entity = new PeopleModel.Entity();
        entity.setName("姓名");
        entity.setAge(Byte.valueOf("13"));
        entity.setSex(Byte.valueOf("1"));
        entity.setTeacherId(0);
        entity.setCreatedAt(new Date(1312312312));
        entity.setUpdatedAt(new Date(1312312312));
        System.out.println("原对象 " + entity);
        Long newId = peopleModel.newQuery().insertGetIdOrFail(entity);
        Assert.assertEquals(20L, newId.longValue());
        System.out.println("新对象 " + entity);

        Record<PeopleModel.Entity, Long> entityLongRecord = peopleModel.find(20L);
        System.out.println(entityLongRecord);
    }

    @Test
    public void 批量新增_返回自增ids() {
        List<PeopleModel.Entity> entityList = new ArrayList<>();
        for (int i = 99; i < 10000; i++) {
            PeopleModel.Entity entity = new PeopleModel.Entity();
            entity.setName("姓名");
            entity.setAge(Byte.valueOf("13"));
            entity.setSex(Byte.valueOf("1"));
            entity.setTeacherId(0);
            entity.setCreatedAt(new Date(1312312312));
            entity.setUpdatedAt(new Date(1312312312));
            entityList.add(entity);
        }
        int insert = peopleModel.newQuery().insert(entityList);
        List<Long> longs = peopleModel.newQuery().insertGetIds(entityList);
        Assert.assertEquals(9901, insert);
        Assert.assertEquals(9901, longs.size());
        System.out.println(longs);
    }

    @Test
    public void 雪花算法id_ORM新增且没有赋值给主键才会生效() {
        Record<PeopleModel.Entity, Long> entityLongRecord = peopleModel.newRecord();
        PeopleModel.Entity entity = entityLongRecord.getEntity();
        entity.setName("姓名");
        entity.setAge(Byte.valueOf("13"));
        entity.setSex(Byte.valueOf("1"));
        entity.setTeacherId(0);
        entity.setCreatedAt(new Date(1312312312));
        entity.setUpdatedAt(new Date(1312312312));

        entityLongRecord.save();
        System.out.println(entity);
        Assert.assertTrue(entity.getId() - 170936861320019968L > 0);

        PeopleModel.Entity entity1 = peopleModel.findOrFail(entity.getId()).toObject();
        System.out.println(entity1);
        Assert.assertNotNull(entity1);
        Assert.assertTrue(entity1.getId() - 170936861320019968L > 0);
    }


}
