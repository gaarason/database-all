package gaarason.database.test;

import gaarason.database.connections.GaarasonDataSourceProvider;
import gaarason.database.eloquent.Record;
import gaarason.database.test.models.PeopleModel;
import gaarason.database.test.parent.BaseTests;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Slf4j
@FixMethodOrder(MethodSorters.JVM)
public class IncrementTypeTests extends BaseTests {

    private static PeopleModel peopleModel = new PeopleModel();

    protected List<DataSource> getDataSourceList() {
        GaarasonDataSourceProvider gaarasonDataSourceProvider = peopleModel.getGaarasonDataSource();
        return gaarasonDataSourceProvider.getMasterDataSourceList();
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
        Long aLong = peopleModel.newQuery().executeGetId("insert into `people` values ()", new ArrayList<>());
        Assert.assertNotNull(aLong);
        Assert.assertEquals(20, aLong.intValue());

        List<String> vList = new ArrayList<>();
        vList.add("aaaccc");
        Long       qwww      = peopleModel.newQuery().select("name").value(vList).insertGetId();;
        Assert.assertNotNull(qwww);
        Assert.assertEquals(21, qwww.intValue());

        Long       dd      = peopleModel.newQuery().value(new ArrayList<>()).insertGetId();;
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
        int        insert = peopleModel.newQuery().insert(entityList);
        List<Long> longs  = peopleModel.newQuery().insertGetIds(entityList);
        Assert.assertEquals(9901, insert);
        Assert.assertEquals(9901, longs.size());
        System.out.println(longs);
    }


}
