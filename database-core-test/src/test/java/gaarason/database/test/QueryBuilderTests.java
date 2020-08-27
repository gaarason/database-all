package gaarason.database.test;

import gaarason.database.connections.ProxyDataSource;
import gaarason.database.eloquent.Paginate;
import gaarason.database.eloquent.Record;
import gaarason.database.eloquent.RecordList;
import gaarason.database.eloquent.enums.OrderBy;
import gaarason.database.exception.AggregatesNotSupportedGroupException;
import gaarason.database.exception.ConfirmOperationException;
import gaarason.database.exception.EntityNotFoundException;
import gaarason.database.exception.NestedTransactionException;
import gaarason.database.query.Builder;
import gaarason.database.test.models.StudentModel;
import gaarason.database.test.parent.BaseTests;
import gaarason.database.test.utils.MultiThreadUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import javax.sql.DataSource;
import java.text.SimpleDateFormat;
import java.util.*;

@Slf4j
@FixMethodOrder(MethodSorters.JVM)
public class QueryBuilderTests extends BaseTests {

    private static StudentModel studentModel = new StudentModel();

    protected List<DataSource> getDataSourceList() {
        ProxyDataSource proxyDataSource = studentModel.getDataSource();
        return proxyDataSource.getMasterDataSourceList();
    }

    @Test
    public void 新增_多线程_循环_非entity方式() throws InterruptedException {
        // 原本数据量
        Long beforeCount = studentModel.newQuery().count("id");
        Assert.assertEquals(10L, beforeCount.longValue());

        // 插入多次
        MultiThreadUtil.run(10, 10, () -> {
            List<String> columnNameList = new ArrayList<>();
            columnNameList.add("name");
            columnNameList.add("age");
            columnNameList.add("sex");
            List<String> valueList = new ArrayList<>();
            valueList.add("testNAme134");
            valueList.add("11");
            valueList.add("1");

            int insert = studentModel.newQuery().select(columnNameList).value(valueList).insert();
            Assert.assertEquals(insert, 1);
            StudentModel.Entity entityFirst = studentModel.newQuery()
                .where("name", "testNAme134")
                .orderBy("id", OrderBy.DESC)
                .firstOrFail()
                .toObject();
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            System.out.println(entityFirst);
            Assert.assertNotNull(entityFirst);
            Assert.assertEquals(11, entityFirst.getAge().intValue());
            Assert.assertEquals("testNAme134", entityFirst.getName());
            Assert.assertEquals(0, entityFirst.getTeacherId().intValue());
        });

        // 现在数据量
        Long afterCount = studentModel.newQuery().count("id");
        Assert.assertEquals(110L, afterCount.longValue());
    }

    @Test
    public void 新增_单条记录() {
        StudentModel.Entity entity = new StudentModel.Entity();
        entity.setId(99);
        entity.setName("姓名");
        entity.setAge(Byte.valueOf("13"));
        entity.setSex(Byte.valueOf("1"));
        entity.setTeacherId(0);
        entity.setCreatedAt(new Date(1312312312));
        entity.setUpdatedAt(new Date(1312312312));
        int insert = studentModel.newQuery().insert(entity);
        Assert.assertEquals(insert, 1);

        StudentModel.Entity entityFirst = studentModel.newQuery().where("id", "99").firstOrFail().toObject();
        SimpleDateFormat    formatter   = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Assert.assertNotNull(entityFirst);
        Assert.assertEquals(entity.getId(), entityFirst.getId());
        Assert.assertEquals(entity.getAge(), entityFirst.getAge());
        Assert.assertEquals(entity.getName(), entityFirst.getName());
        Assert.assertEquals(entity.getTeacherId(), entityFirst.getTeacherId());
        // 这两个字段在entity中标记为不可更新
        Assert.assertNotEquals(formatter.format(entity.getCreatedAt()), formatter.format(entityFirst.getCreatedAt()));
        Assert.assertNotEquals(formatter.format(entity.getUpdatedAt()), formatter.format(entityFirst.getUpdatedAt()));
    }


    @Test
    public void 新增_单条记录_并获取数据库自增id() {
        StudentModel.Entity entity = new StudentModel.Entity();
//        entity.setId(99);
        entity.setName("姓名");
        entity.setAge(Byte.valueOf("13"));
        entity.setSex(Byte.valueOf("1"));
        entity.setTeacherId(0);
        entity.setCreatedAt(new Date(1312312312));
        entity.setUpdatedAt(new Date(1312312312));
        Object insert = studentModel.newQuery().insertGetId(entity);
        Assert.assertEquals(insert, 20);

        StudentModel.Entity entityFirst = studentModel.newQuery().where("id", "20").firstOrFail().toObject();
        SimpleDateFormat    formatter   = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Assert.assertNotNull(entityFirst);
        Assert.assertEquals(entity.getId(), entityFirst.getId());
        Assert.assertEquals(entity.getAge(), entityFirst.getAge());
        Assert.assertEquals(entity.getName(), entityFirst.getName());
        Assert.assertEquals(entity.getTeacherId(), entityFirst.getTeacherId());
        // 这两个字段在entity中标记为不可更新
        Assert.assertNotEquals(formatter.format(entity.getCreatedAt()), formatter.format(entityFirst.getCreatedAt()));
        Assert.assertNotEquals(formatter.format(entity.getUpdatedAt()), formatter.format(entityFirst.getUpdatedAt()));
    }

    @Test
    public void 新增_使用list单次新增多条记录() {
        List<StudentModel.Entity> entityList = new ArrayList<>();
        for (int i = 99; i < 10000; i++) {
            StudentModel.Entity entity = new StudentModel.Entity();
            entity.setName("姓名");
            entity.setAge(Byte.valueOf("13"));
            entity.setSex(Byte.valueOf("1"));
            entity.setTeacherId(i * 3);
            entity.setCreatedAt(new Date());
            entity.setUpdatedAt(new Date());
            entityList.add(entity);
        }
        int insert = studentModel.newQuery().insert(entityList);
        Assert.assertEquals(insert, 9901);

        RecordList<StudentModel.Entity, Integer> records = studentModel.newQuery()
            .whereBetween("id", "300", "350")
            .orderBy("id", OrderBy.DESC)
            .get();
        Assert.assertEquals(records.size(), 51);
    }

    @Test
    public void 更新_普通更新() {
        int update = studentModel.newQuery().data("name", "xxcc").where("id", "3").update();
        Assert.assertEquals(update, 1);

        StudentModel.Entity entity = studentModel.newQuery().where("id", "3").firstOrFail().toObject();
        Assert.assertEquals(entity.getId().intValue(), 3);
        Assert.assertEquals(entity.getName(), "xxcc");

        int update2 = studentModel.newQuery().data("name", "vvv").where("id", ">", "3").update();
        Assert.assertEquals(update2, 7);
        RecordList<StudentModel.Entity, Integer> records = studentModel.newQuery().whereRaw("id>3").get();
        Assert.assertEquals(records.size(), 7);

        for (Record<StudentModel.Entity, Integer> record : records) {
            Assert.assertEquals(record.getEntity().getName(), "vvv");
        }
    }

    @Test
    public void 更新_字段自增自减() {
        int update = studentModel.newQuery().dataDecrement("age", 2).whereRaw("id=4").update();
        Assert.assertEquals(update, 1);
        StudentModel.Entity entity = studentModel.newQuery().where("id", "4").firstOrFail().toObject();
        Assert.assertEquals(entity.getId().intValue(), 4);
        Assert.assertEquals(entity.getAge(), Byte.valueOf("9"));

        int update2 = studentModel.newQuery().dataIncrement("age", 4).whereRaw("id=4").update();
        Assert.assertEquals(update2, 1);
        StudentModel.Entity entity2 = studentModel.newQuery().where("id", "4").firstOrFail().toObject();
        Assert.assertEquals(entity2.getId().intValue(), 4);
        Assert.assertEquals(entity2.getAge(), Byte.valueOf("13"));

    }

    @Test
    public void 更新_通过MAP更新() {
        Map<String, String> map = new HashMap<>();
        map.put("name", "gggg");
        map.put("age", "7");

        int update = studentModel.newQuery().data(map).where("id", "3").update();
        Assert.assertEquals(update, 1);
        StudentModel.Entity entity = studentModel.newQuery().where("id", "3").firstOrFail().toObject();
        Assert.assertEquals(entity.getId().intValue(), 3);
        Assert.assertEquals(entity.getName(), "gggg");
        Assert.assertEquals(entity.getAge(), Byte.valueOf("7"));

        Assert.assertThrows(ConfirmOperationException.class, () -> {
            studentModel.newQuery().data("name", "ee").update();
        });
    }

    @Test
    public void 更新_通过entity更新() {
        StudentModel.Entity entity1 = new StudentModel.Entity();
        entity1.setAge(Byte.valueOf("7"));
        entity1.setName("ggg");
        int update = studentModel.newQuery().where("id", "3").update(entity1);
        Assert.assertEquals(update, 1);
        StudentModel.Entity entity = studentModel.newQuery().where("id", "3").firstOrFail().toObject();
        Assert.assertEquals(entity.getId().intValue(), 3);
        Assert.assertEquals(entity.getName(), "ggg");
        Assert.assertEquals(entity.getAge(), Byte.valueOf("7"));
        Assert.assertEquals(entity.getSex(), Byte.valueOf("1"));

        Assert.assertThrows(ConfirmOperationException.class, () -> {
            studentModel.newQuery().update(entity1);
        });
    }

    @Test
    public void 删除_硬() {
        int id = studentModel.newQuery().where("id", "3").forceDelete();
        Assert.assertEquals(id, 1);

        Record<StudentModel.Entity, Integer> id1 = studentModel.newQuery().where("id", "3").first();
        Assert.assertNull(id1);

        Assert.assertThrows(ConfirmOperationException.class, () -> {
            studentModel.newQuery().delete();
        });
    }

    @Test
    public void 查询_单条记录() {
        Record<StudentModel.Entity, Integer> RecordFirst1 =
            studentModel.newQuery().select("name").select("id").first();
        log.info("RecordFirst1 : {}", RecordFirst1);
        Assert.assertNotNull(RecordFirst1);
        StudentModel.Entity first1 = RecordFirst1.toObject();
        Assert.assertEquals(first1.getId(), new Integer(1));
        Assert.assertEquals(first1.getId().intValue(), 1);
        Assert.assertEquals(first1.getName(), "小明");
        Assert.assertNull(first1.getAge());
        Assert.assertNull(first1.getTeacherId());
        Assert.assertNull(first1.getCreatedAt());
        Assert.assertNull(first1.getUpdatedAt());

        Record<StudentModel.Entity, Integer> RecordFirst2 = studentModel.newQuery().select("name", "id",
            "created_at").first();
        Assert.assertNotNull(RecordFirst2);
        StudentModel.Entity first2 = RecordFirst2.toObject();
        Assert.assertEquals(first2.getId(), new Integer(1));
        Assert.assertEquals(first2.getId().intValue(), 1);
        Assert.assertEquals(first2.getName(), "小明");
        Assert.assertNull(first2.getAge());
        Assert.assertNull(first2.getTeacherId());
        Assert.assertEquals(first2.getCreatedAt().toString(), "2009-03-14 17:15:23.0");
        Assert.assertNull(first2.getUpdatedAt());

        Assert.assertNotNull(first1);
        Assert.assertEquals(first1.getId(), new Integer(1));
        Assert.assertEquals(first1.getId().intValue(), 1);
        Assert.assertEquals(first1.getName(), "小明");
        Assert.assertNull(first1.getAge());
        Assert.assertNull(first1.getTeacherId());
        Assert.assertNull(first1.getCreatedAt());
        Assert.assertNull(first1.getUpdatedAt());

        Record<StudentModel.Entity, Integer> first3 =
            studentModel.newQuery().select("name", "id").where("id", "not found").first();
        Assert.assertNull(first3);

        Record<StudentModel.Entity, Integer> RecordFirst5 = studentModel.newQuery().first();
        System.out.println(RecordFirst5);
        Assert.assertNotNull(RecordFirst5);
        StudentModel.Entity first5 = RecordFirst5.toObject();
        Assert.assertEquals(first5.getId(), new Integer(1));
        Assert.assertEquals(first5.getId().intValue(), 1);
        Assert.assertEquals(first5.getName(), "小明");
        Assert.assertEquals(first5.getAge().intValue(), 6);
        Assert.assertEquals(first5.getTeacherId().intValue(), 6);
        Assert.assertEquals(first5.getCreatedAt().toString(), "2009-03-14 17:15:23.0");
        Assert.assertEquals(first5.getUpdatedAt().toString(), "2010-04-24 22:11:03.0");

        Assert.assertThrows(EntityNotFoundException.class, () -> {
            studentModel.newQuery().select("name", "id").where("id", "not found").firstOrFail();
        });
    }

    @Test
    public void 查询_多条记录() throws InterruptedException {
        MultiThreadUtil.run(10, 10, () -> {
            List<StudentModel.Entity> entities1 = studentModel.newQuery()
                .select("name")
                .select("id")
                .get()
                .toObjectList();

            Assert.assertEquals(entities1.size(), 10);

            List<StudentModel.Entity> entities2 = studentModel.newQuery().get().toObjectList();
            StudentModel.Entity       entity2   = entities2.get(0);
            System.out.println(entity2);
            Assert.assertNotNull(entity2);
            Assert.assertEquals(entity2.getId(), new Integer(1));
            Assert.assertEquals(entity2.getId().intValue(), 1);
            Assert.assertEquals(entity2.getName(), "小明");
            Assert.assertEquals(entity2.getAge().intValue(), 6);
            Assert.assertEquals(entity2.getTeacherId().intValue(), 6);
            Assert.assertEquals(entity2.getCreatedAt().toString(), "2009-03-14 17:15:23.0");
            Assert.assertEquals(entity2.getUpdatedAt().toString(), "2010-04-24 22:11:03.0");
        });
    }

    @Test
    public void 查询_多条记录_非分块() throws InterruptedException {
        Runtime r = Runtime.getRuntime();
        r.gc();
        long startMem = r.totalMemory(); // 开始时内存
        System.out.println("开始时内存: " + startMem);
        // 数据库数据有限,此处模拟大数据
        新增_多线程_循环_非entity方式();
        System.out.println("插入数据后的内存: " + r.totalMemory());
        Builder<StudentModel.Entity, Integer> queryBuilder = studentModel.newQuery();
        for (int i = 0; i < 100; i++) {
            queryBuilder.unionAll((builder -> builder));
        }
        System.out.println("构造sql后的内存: " + r.totalMemory());
        RecordList<StudentModel.Entity, Integer> records = queryBuilder.get();
        System.out.println("执行sql后的内存: " + r.totalMemory());
        int size = records.size();
        System.out.println("查询结果数量 : " + size);
        StringBuilder temp = new StringBuilder();
        for (Record<StudentModel.Entity, Integer> record : records) {
            // do something
            temp.append(record.toSearch());
        }
        System.out.println(temp.toString());
        long orz = r.totalMemory() - startMem; // 剩余内存 现在
        System.out.println("最后的内存: " + r.totalMemory());
        System.out.println("执行消耗的内存差: " + orz);
    }

    @Test
    public void 查询_多条记录_分块() throws InterruptedException {
        Runtime r = Runtime.getRuntime();
        r.gc();
        long startMem = r.totalMemory(); // 开始时内存
        System.out.println("开始时内存: " + startMem);
        // 数据库数据有限,此处模拟大数据
        新增_多线程_循环_非entity方式();
        System.out.println("插入数据后的内存: " + r.totalMemory());
        Builder<StudentModel.Entity, Integer> queryBuilder = studentModel.newQuery();
        for (int i = 0; i < 100; i++) {
            queryBuilder.unionAll((builder -> builder));
        }
        System.out.println("构造sql后的内存: " + r.totalMemory());
        StringBuilder temp = new StringBuilder();
        queryBuilder.dealChunk(2000, records -> {
            // do something
            for (Record<StudentModel.Entity, Integer> record : records) {
                // do something
                temp.append(record.toSearch());
            }
            return true;
        });
        System.out.println("执行sql后的内存: " + r.totalMemory());
        System.out.println(temp.toString());

        long orz = r.totalMemory() - startMem; // 剩余内存 现在
        System.out.println("最后的内存: " + r.totalMemory());
        System.out.println("执行消耗的内存差: " + orz);
    }

    @Test
    public void 查询_调用mysql中的其他函数() {
        Record<StudentModel.Entity, Integer> entityRecord = studentModel.newQuery()
            .selectFunction("concat_ws", "\"-\",`name`,`id`", "newKey")
            .first();
        Assert.assertNotNull(entityRecord);
        StudentModel.Entity entity          = entityRecord.toObject();
        Map<String, Object> stringObjectMap = entityRecord.toMap();
        Assert.assertNull(entity.getId());
        Assert.assertNull(entity.getName());
        Assert.assertNull(entity.getAge());
        Assert.assertNull(entity.getTeacherId());
        Assert.assertEquals(stringObjectMap.get("newKey"), "小明-1");
    }

    // todo check
    @Test
    public void 查询_聚合函数() {
        Long count0 = studentModel.newQuery().where("sex", "1").count("id");
        Assert.assertEquals(count0.intValue(), 6);

        Long count = studentModel.newQuery().where("sex", "1").count("age");
        Assert.assertEquals(count.intValue(), 6);

//        String max0 = studentModel.newQuery().select("age").where("sex", "1").max("id");
//        Assert.assertEquals(max0, "10");

        String max1 = studentModel.newQuery().where("sex", "1").max("id");
        Assert.assertEquals(max1, "10");

        String min = studentModel.newQuery().where("sex", "1").min("id");
        Assert.assertEquals(min, "3");

        String avg = studentModel.newQuery().where("sex", "1").avg("id");
        Assert.assertEquals(avg, "7.1667");

        String sum = studentModel.newQuery().where("sex", "2").sum("id");
        Assert.assertEquals(sum, "12");
    }

    // todo check
    @Test
    public void 查询_聚合函数_带group() {
        Long count0 = studentModel.newQuery().where("sex", "1").count("*");
        Assert.assertEquals(count0.intValue(), 6);


        Assert.assertThrows(AggregatesNotSupportedGroupException.class, () -> {
            Long count01 = studentModel.newQuery().where("sex", "1").group("sex").count("id");
        });

//        Long count02 = studentModel.newQuery().where("sex", "1").group("teacher_id").count("id");
//        Assert.assertEquals(count02.intValue(), 5);
//
//        Long count1 = studentModel.newQuery().select("id").where("sex", "1").group("id", "age").count("id");
//        Assert.assertEquals(count1.intValue(), 1);

        Long count = studentModel.newQuery().where("sex", "1").count("age");
        Assert.assertEquals(count.intValue(), 6);

//        String max0 = studentModel.newQuery().select("age").where("sex", "1").group("age").max("id");
//        Assert.assertEquals(max0, "10");

        String max1 = studentModel.newQuery().where("sex", "1").max("id");
        Assert.assertEquals(max1, "10");

        String min = studentModel.newQuery().where("sex", "1").min("id");
        Assert.assertEquals(min, "3");

        String avg = studentModel.newQuery().where("sex", "1").avg("id");
        Assert.assertEquals(avg, "7.1667");

        String sum = studentModel.newQuery().where("sex", "2").sum("id");
        Assert.assertEquals(sum, "12");
    }

    @Test
    public void 条件_字段之间比较() {
        Record<StudentModel.Entity, Integer> entityRecord = studentModel.newQuery()
            .whereColumn("id", ">", "sex")
            .first();
        Assert.assertNotNull(entityRecord);
        System.out.println(entityRecord);
        StudentModel.Entity first = entityRecord.toObject();
        Assert.assertEquals(first.getId(), new Integer(3));
        Assert.assertEquals(first.getId().intValue(), 3);
        Assert.assertEquals(first.getName(), "小腾");
        Assert.assertEquals(first.getAge().intValue(), 16);
        Assert.assertEquals(first.getTeacherId().intValue(), 6);
        Assert.assertEquals(first.getCreatedAt().toString(), "2009-03-14 15:11:23.0");
        Assert.assertEquals(first.getUpdatedAt().toString(), "2010-04-24 22:11:03.0");

        Record<StudentModel.Entity, Integer> entityRecord2 =
            studentModel.newQuery().whereColumn("id", "sex").first();
        Assert.assertNotNull(entityRecord2);
        System.out.println(entityRecord2);
        StudentModel.Entity first2 = entityRecord2.toObject();
        Assert.assertEquals(first2.getId(), new Integer(2));
        Assert.assertEquals(first2.getId().intValue(), 2);
        Assert.assertEquals(first2.getName(), "小张");
        Assert.assertEquals(first2.getAge().intValue(), 11);
        Assert.assertEquals(first2.getTeacherId().intValue(), 6);
        Assert.assertEquals(first2.getCreatedAt().toString(), "2009-03-14 15:15:23.0");
        Assert.assertEquals(first2.getUpdatedAt().toString(), "2010-04-24 22:11:03.0");
    }

    @Test
    public void 条件_普通条件() {
        Record<StudentModel.Entity, Integer> entityRecord = studentModel.newQuery().where("id", ">", "2").first();
        Assert.assertNotNull(entityRecord);
        System.out.println(entityRecord);
        StudentModel.Entity first = entityRecord.toObject();
        Assert.assertEquals(first.getId(), new Integer(3));
        Assert.assertEquals(first.getId().intValue(), 3);
        Assert.assertEquals(first.getName(), "小腾");
        Assert.assertEquals(first.getAge().intValue(), 16);
        Assert.assertEquals(first.getTeacherId().intValue(), 6);
        Assert.assertEquals(first.getCreatedAt().toString(), "2009-03-14 15:11:23.0");
        Assert.assertEquals(first.getUpdatedAt().toString(), "2010-04-24 22:11:03.0");


        List<StudentModel.Entity> entityList1 = studentModel.newQuery()
            .where("id", ">", "2")
            .get()
            .toObjectList();
        Assert.assertEquals(entityList1.size(), 8);

        List<StudentModel.Entity> entityList2 = studentModel.newQuery().where("id", ">", "2")
            .where("id", "<", "7").get().toObjectList();
        Assert.assertEquals(entityList2.size(), 4);

        StudentModel.Entity entity2 = studentModel.newQuery().where("id", "4").firstOrFail().toObject();
        Assert.assertEquals(entity2.getId().intValue(), 4);

        StudentModel.Entity entity1 = studentModel.newQuery()
            .where("created_at", ">=", "2009-03-15 22:15:23.0")
            .firstOrFail().toObject();
        Assert.assertEquals(entity1.getId().intValue(), 9);

        StudentModel.Entity entity3 = studentModel.newQuery()
            .where("created_at", ">=", "2009-03-15 22:15:23")
            .firstOrFail().toObject();
        Assert.assertEquals(entity3.getId().intValue(), 9);

    }

    @Test
    public void 条件_Between() {
        List<StudentModel.Entity> entityList1 = studentModel.newQuery()
            .whereBetween("id", "3", "5")
            .get()
            .toObjectList();
        Assert.assertEquals(entityList1.size(), 3);

        List<StudentModel.Entity> entityList2 = studentModel.newQuery()
            .whereBetween("id", "3", "5")
            .whereNotBetween(
                "id", "3", "4")
            .get()
            .toObjectList();
        Assert.assertEquals(entityList2.size(), 1);
    }

    @Test
    public void 条件_whereIn() {
        List<Object> idList = new ArrayList<>();
        idList.add("4");
        idList.add("5");
        idList.add("6");
        idList.add("7");
        List<StudentModel.Entity> entityList1 = studentModel.newQuery()
            .whereIn("id", idList)
            .get()
            .toObjectList();
        Assert.assertEquals(entityList1.size(), 4);

        List<Object> idList2 = new ArrayList<>();
        idList2.add("10");
        idList2.add("9");
        idList2.add("7");

        List<StudentModel.Entity> entityList2 = studentModel.newQuery().whereIn("id", idList).whereNotIn("id",
            idList2).get().toObjectList();
        Assert.assertEquals(entityList2.size(), 3);
    }

    @Test
    public void 条件_whereIn_Array() {
        List<StudentModel.Entity> entityList1 = studentModel.newQuery()
            .whereIn("id", "4", "5", "6", "7")
            .get()
            .toObjectList();
        Assert.assertEquals(entityList1.size(), 4);

        List<StudentModel.Entity> entityList2 = studentModel.newQuery()
            .whereIn("id", "4", "5", "6", "7")
            .whereNotIn("id",
                "10", "9", "7")
            .get()
            .toObjectList();
        Assert.assertEquals(entityList2.size(), 3);
    }

    @Test
    public void 条件_whereIn_closure() {
        List<StudentModel.Entity> entityList1 = studentModel.newQuery().whereIn("id",
            builder -> builder.select("id").where("age", ">=", "11")
        ).andWhere(
            builder -> builder.whereNotIn("sex",
                builder1 -> builder1.select("sex").where("sex", "1")
            )
        ).get().toObjectList();
        Assert.assertEquals(entityList1.size(), 3);
    }

    @Test
    public void 条件_whereNull() {
        List<StudentModel.Entity> entityList1 = studentModel.newQuery().whereNotNull("id").get().toObjectList();
        Assert.assertEquals(entityList1.size(), 10);

        List<StudentModel.Entity> entityList2 = studentModel.newQuery().whereNull("age").get().toObjectList();
        Assert.assertEquals(entityList2.size(), 0);
    }

    @Test
    public void 条件_orWhere() {
        List<StudentModel.Entity> entityList1 = studentModel.newQuery().where("id", "3").orWhere(
            (builder) -> builder.whereRaw("id=4")
        ).get().toObjectList();
        Assert.assertEquals(entityList1.size(), 2);

        List<StudentModel.Entity> entityList2 = studentModel.newQuery().where("id", "3").orWhere(
            (builder) -> builder.whereBetween("id", "4", "10").where("age", ">", "11")
        ).get().toObjectList();
        Assert.assertEquals(entityList2.size(), 6);
    }

    @Test
    public void 条件_andWhere() {
        List<StudentModel.Entity> entityList1 = studentModel.newQuery().where("id", "3").andWhere(
            (builder) -> builder.whereRaw("id=4")
        ).get().toObjectList();
        Assert.assertEquals(entityList1.size(), 0);

        List<StudentModel.Entity> entityList2 = studentModel.newQuery().where("id", "7").andWhere(
            (builder) -> builder.whereBetween("id", "4", "10").where("age", ">", "11")
        ).get().toObjectList();
        Assert.assertEquals(entityList2.size(), 1);
        Assert.assertEquals(entityList2.get(0).getId().intValue(), 7);
    }

    @Test
    public void 条件_andWhere与orWhere无线嵌套() {
        List<StudentModel.Entity> entityList1 = studentModel.newQuery().where("id", "3").orWhere(
            builder -> builder.where("age", ">", "11").where("id", "7").andWhere(
                builder2 -> builder2.whereBetween("id", "4", "10").where("age", ">", "11")
            )
        ).from("student").select("id", "name").get().toObjectList();
        Assert.assertEquals(entityList1.size(), 2);
    }

    @Test
    public void 条件_子查询_闭包() {
        List<Object> ins = new ArrayList<>();
        ins.add("1");
        ins.add("2");
        ins.add("3");
        RecordList<StudentModel.Entity, Integer> records = studentModel.newQuery()
            .where("age", "!=", "99")
            .whereSubQuery("id", "in", builder -> builder.select("id").whereIn("id", ins))
            .get();
        Assert.assertEquals(records.size(), 3);
    }

    @Test
    public void 条件_子查询_字符串() {
        RecordList<StudentModel.Entity, Integer> records = studentModel.newQuery()
            .where("age", "!=", "99")
            .whereSubQuery("id", "in", "select id from student where id = 3")
            .get();
        Assert.assertEquals(records.size(), 1);
    }

    @Test
    public void 随机获取() throws InterruptedException {
        // 数据库数据有限,此处模拟大数据
        for (int i = 0; i < 10; i++)
            新增_使用list单次新增多条记录();
        System.out.println("总数据量 : " + studentModel.newQuery().count("id"));

        long l1 = System.currentTimeMillis();
        studentModel.newQuery().where("sex", "1").orderBy("RAND()").limit(5).get().toObjectList();
        System.out.println("RAND()耗时 : " + (System.currentTimeMillis() - l1));
        long l2 = System.currentTimeMillis();
        studentModel.newQuery().where("sex", "1").inRandomOrder("id").limit(5).get().toObjectList();
        System.out.println("inRandomOrder()耗时 : " + (System.currentTimeMillis() - l2));
    }

    @Test
    public void 条件_exists() {
        // EXISTS用于检查子查询是否至少会返回一行数据，该子查询实际上并不返回任何数据，而是返回值True或False
        // EXISTS 指定一个子查询，检测 行 的存在。

        List<StudentModel.Entity> entityList = studentModel.newQuery()
            .select("id", "name", "age")
            .whereBetween("id", "1", "2")
            .whereExists(
                builder -> builder.select("id", "name", "age").whereBetween("id", "2", "3")
            )
            .whereExists(
                builder -> builder.select("id", "name", "age").whereBetween("id", "1", "4")
            )
            .get().toObjectList();
        Assert.assertEquals(entityList.size(), 2);

        List<StudentModel.Entity> entityList2 = studentModel.newQuery()
            .select("id", "name", "age")
            .whereBetween("id", "1", "2")
            .whereExists(
                builder -> builder.select("id", "name", "age").whereBetween("id", "2", "3")
            )
            .whereNotExists(
                builder -> builder.select("id", "name", "age").whereBetween("id", "2", "4")
            )
            .get().toObjectList();
        Assert.assertEquals(entityList2.size(), 0);
    }

    @Test
    public void GROUP() {
        List<String> groupList = new ArrayList<>();
        groupList.add("id");
        groupList.add("age");
        List<StudentModel.Entity> entities = studentModel.newQuery()
            .select("id", "age")
            .where("id", "&", "1")
            .orderBy("id", OrderBy.DESC)
            .group("sex", "id", "age")
            .group(groupList)
            .get()
            .toObjectList();
        System.out.println(entities);
        Assert.assertEquals(entities.size(), 5);
        Assert.assertEquals(entities.get(0).getId().intValue(), 9);
        Assert.assertEquals(entities.get(1).getId().intValue(), 7);

        // 严格模式 todo
//        Assert.assertThrows(SQLRuntimeException.class, () -> {
//            List<StudentModel.Entity> entities1 = studentModel.newQuery()
//                .select("id", "name", "age")
//                .where("id", "&", "1")
//                .orderBy("id", OrderBy.DESC)
//                .group("sex", "age")
//                .group("id")
//                .get()
//                .toObjectList();
//        });
    }


    @Test
    public void 筛选_字段之间比较() {
        RecordList<StudentModel.Entity, Integer> records = studentModel.newQuery()
            .havingColumn("age", ">", "sex").group("age", "sex").select("age", "sex")
            .get();
        Assert.assertEquals(records.size(), 5);
        System.out.println(records);
        StudentModel.Entity first = records.get(0).toObject();
        Assert.assertEquals(first.getAge().intValue(), 6);
        Assert.assertEquals(first.getSex().intValue(), 2);

        RecordList<StudentModel.Entity, Integer> records2 = studentModel.newQuery()
            .havingColumn("age", "<", "sex").group("age", "sex").select("age", "sex")
            .get();
        Assert.assertTrue(records2.isEmpty());
    }

    @Test
    public void 筛选_having() {
        Record<StudentModel.Entity, Integer> entityRecord =
            studentModel.newQuery().select("id").group("id").where("id", "<", "3").having("id", ">=", "2").first();
        Assert.assertNotNull(entityRecord);
        System.out.println(entityRecord);
        StudentModel.Entity first = entityRecord.toObject();
        Assert.assertEquals(first.getId(), new Integer(2));
    }

    @Test
    public void 筛选_havingBetween() {
        List<StudentModel.Entity> entityList1 = studentModel.newQuery().group("id")
            .havingBetween("id", "3", "5").select("id")
            .get()
            .toObjectList();
        System.out.println(entityList1);
        Assert.assertEquals(entityList1.size(), 3);

        List<StudentModel.Entity> entityList2 = studentModel.newQuery()
            .havingBetween("id", "3", "5")
            .havingNotBetween(
                "id", "3", "4").select("id").group("id")
            .get()
            .toObjectList();
        System.out.println(entityList2);
        Assert.assertEquals(entityList2.size(), 1);
    }

    @Test
    public void 筛选_havingIn() {
        List<Object> idList = new ArrayList<>();
        idList.add("4");
        idList.add("5");
        idList.add("6");
        idList.add("7");
        List<StudentModel.Entity> entityList1 = studentModel.newQuery()
            .havingIn("id", idList).group("id").select("id")
            .get()
            .toObjectList();
        System.out.println(entityList1);
        Assert.assertEquals(entityList1.size(), 4);

        List<Object> idList2 = new ArrayList<>();
        idList2.add("10");
        idList2.add("9");
        idList2.add("7");

        List<StudentModel.Entity> entityList2 = studentModel.newQuery()
            .havingIn("id", idList)
            .group("id")
            .select("id")
            .havingNotIn("id",
                idList2)
            .get()
            .toObjectList();
        System.out.println(entityList2);
        Assert.assertEquals(entityList2.size(), 3);
    }

    @Test
    public void 条件_havingIn_Array() {
        List<StudentModel.Entity> entityList1 = studentModel.newQuery()
            .havingIn("id", "4", "5", "6", "7").group("id").select("id")
            .get()
            .toObjectList();
        System.out.println(entityList1);
        Assert.assertEquals(entityList1.size(), 4);

        List<StudentModel.Entity> entityList2 = studentModel.newQuery()
            .havingIn("id", "4", "5", "6", "7").group("id").select("id")
            .havingNotIn("id",
                "10", "9", "7")
            .get()
            .toObjectList();
        System.out.println(entityList2);
        Assert.assertEquals(entityList2.size(), 3);
    }

    @Test
    public void 筛选_havingIn_closure() {
        List<StudentModel.Entity> entityList1 = studentModel.newQuery().havingIn("id",
            builder -> builder.select("id").where("age", ">=", "11")
        ).andHaving(
            builder -> builder.havingNotIn("sex",
                builder1 -> builder1.select("sex").where("sex", "1")
            )
        ).group("id", "sex").select("id").get().toObjectList();
        Assert.assertEquals(entityList1.size(), 3);
    }

    @Test
    public void 筛选_havingNull() {
        List<StudentModel.Entity> entityList1 =
            studentModel.newQuery().group("id").select("id").havingNotNull("id").get().toObjectList();
        Assert.assertEquals(entityList1.size(), 10);

        List<StudentModel.Entity> entityList2 = studentModel.newQuery()
            .group("id")
            .select("id")
            .havingNull("id")
            .get()
            .toObjectList();
        Assert.assertEquals(entityList2.size(), 0);
    }

    @Test
    public void 筛选_orHaving() {
        List<StudentModel.Entity> entityList1 =
            studentModel.newQuery().select("id").group("id").having("id", ">", "3").orHaving(
                (builder) -> builder.havingRaw("id=4")
            ).get().toObjectList();
        Assert.assertEquals(entityList1.size(), 7);

        List<StudentModel.Entity> entityList3 =
            studentModel.newQuery().select("id").group("id").having("id", ">", "3").orHaving(
                (builder) -> builder.having("id", "4")
            ).get().toObjectList();
        Assert.assertEquals(entityList3.size(), 7);

        List<StudentModel.Entity> entityList2 =
            studentModel.newQuery().select("id").group("id", "age").having("id", "3").orHaving(
                (builder) -> builder.havingBetween("id", "4", "10").having("age", ">", "11")
            ).get().toObjectList();
        Assert.assertEquals(entityList2.size(), 6);
    }

    @Test
    public void 筛选_andHaving() {
        List<StudentModel.Entity> entityList1 = studentModel.newQuery()
            .select("id")
            .group("id")
            .having("id", "3")
            .andHaving(
                (builder) -> builder.havingRaw("id=4")
            )
            .get()
            .toObjectList();
        Assert.assertEquals(entityList1.size(), 0);

        List<StudentModel.Entity> entityList2 = studentModel.newQuery().select("id").group("id", "age").having(
            "id", "7").andHaving(
            (builder) -> builder.havingBetween("id", "4", "10").having("age", ">", "11")
        ).get().toObjectList();
        Assert.assertEquals(entityList2.size(), 1);
        Assert.assertEquals(entityList2.get(0).getId().intValue(), 7);
    }

    @Test
    public void 筛选_andHaving与orHaving无线嵌套() {
        List<StudentModel.Entity> entityList1 =
            studentModel.newQuery().select("id").group("id", "age", "name").having(
                "id", "3").orHaving(
                (builder) -> builder.having("age", ">", "11").having("id", "7").andHaving(
                    (builder2) -> builder2.havingBetween("id", "4", "10").having("age", ">", "11")
                )
            ).from("student").select("id", "name").get().toObjectList();
        Assert.assertEquals(entityList1.size(), 2);
    }

    @Test
    public void 筛选_exists() {
        // EXISTS用于检查子查询是否至少会返回一行数据，该子查询实际上并不返回任何数据，而是返回值True或False
        // EXISTS 指定一个子查询，检测 行 的存在。

        List<StudentModel.Entity> entityList = studentModel.newQuery()
            .select("id", "name", "age")
            .group("id", "name", "age")
            .havingBetween("id", "1", "2")
            .havingExists(
                builder -> builder.select("id", "name", "age").whereBetween("id", "2", "3")
            )
            .havingExists(
                builder -> builder.select("id", "name", "age").whereBetween("id", "1", "4")
            )
            .get().toObjectList();
        Assert.assertEquals(entityList.size(), 2);

        List<StudentModel.Entity> entityList2 = studentModel.newQuery()
            .select("id", "name", "age")
            .group("id", "name", "age")
            .havingBetween("id", "1", "2")
            .havingExists(
                builder -> builder.select("id", "name", "age").whereBetween("id", "2", "3")
            )
            .havingNotExists(
                builder -> builder.select("id", "name", "age").whereBetween("id", "2", "4")
            )
            .get().toObjectList();
        Assert.assertEquals(entityList2.size(), 0);
    }

    @Test
    public void join() {
        RecordList<StudentModel.Entity, Integer> student_as_t = studentModel.newQuery()
            .select("student.*", "t.age as age2")
            .join("student as t", "student.id", "=", "t.age")
            .get();
        System.out.println(student_as_t.toMapList());
    }

    @Test
    public void 排序() {
        StudentModel.Entity first = studentModel.newQuery().orderBy("id", OrderBy.DESC).firstOrFail().toObject();
        Assert.assertNotNull(first);
        Assert.assertEquals(first.getId().intValue(), 10);

        StudentModel.Entity first2 = studentModel.newQuery()
            .where("id", "<>", "10")
            .orderBy("id", OrderBy.DESC)
            .firstOrFail().toObject();
        Assert.assertNotNull(first2);
        Assert.assertEquals(first2.getId().intValue(), 9);

        StudentModel.Entity first3 = studentModel.newQuery()
            .where("id", "<>", "10")
            .orderBy("age", OrderBy.DESC)
            .orderBy("id", OrderBy.ASC)
            .firstOrFail().toObject();
        Assert.assertNotNull(first3);
        Assert.assertEquals(first3.getId().intValue(), 7);
    }

    @Test
    public void union() {
        Record<StudentModel.Entity, Integer> record = studentModel.newQuery()
            .union((builder -> builder.where("id", "2")))
            .firstOrFail();
        System.out.println(record);

    }

    @Test
    public void unionAll() {
        Record<StudentModel.Entity, Integer> record = studentModel.newQuery()
            .unionAll((builder -> builder.where("id", "2")))
            .union((builder -> builder.where("id", "7")))
            .firstOrFail();
        System.out.println(record);

    }

    @Test
    public void 偏移量() {
        List<StudentModel.Entity> entityList1 =
            studentModel.newQuery().orderBy("id", OrderBy.DESC).limit(2, 3).get().toObjectList();
        Assert.assertNotNull(entityList1);
        Assert.assertEquals(entityList1.size(), 3);

        List<StudentModel.Entity> entityList2 =
            studentModel.newQuery().orderBy("id", OrderBy.DESC).limit(8, 3).get().toObjectList();
        Assert.assertNotNull(entityList2);
        Assert.assertEquals(entityList2.size(), 2);
    }

    @Test
    public void 事物_普通() {
        studentModel.newQuery().begin();
        studentModel.newQuery().where("id", "1").data("name", "dddddd").update();
        StudentModel.Entity entity = studentModel.newQuery().where("id", "1").firstOrFail().toObject();
        Assert.assertEquals(entity.getName(), "dddddd");
        studentModel.newQuery().rollBack();

        StudentModel.Entity entity2 = studentModel.newQuery().where("id", "1").firstOrFail().toObject();
        Assert.assertNotEquals(entity2.getName(), "dddddd");
    }

    @Test
    public void 事物_闭包() {
        Assert.assertThrows(RuntimeException.class, () -> {
            studentModel.newQuery().transaction(() -> {
                studentModel.newQuery().where("id", "1").data("name", "dddddd").update();
                StudentModel.Entity entity = studentModel.newQuery().where("id", "1").firstOrFail().toObject();
                Assert.assertEquals(entity.getName(), "dddddd");
                throw new RuntimeException("ssss");
            }, 3, true);
        });

        StudentModel.Entity entity = studentModel.newQuery().where("id", "1").firstOrFail().toObject();
        Assert.assertNotEquals(entity.getName(), "dddddd");
    }

    @Test
    public void 事物_单个数据连接不可嵌套事物() {
        Assert.assertThrows(NestedTransactionException.class, () -> {
            // 1层事物
            studentModel.newQuery().transaction(() -> {
                // 2层事物
                studentModel.newQuery().transaction(() -> {
                    // 3层事物
                    studentModel.newQuery().transaction(() -> {
                        try {
                            // 4层事物
                            studentModel.newQuery().transaction(() -> {
                                studentModel.newQuery().where("id", "1").data("name", "dddddd").update();
                                StudentModel.Entity entity = studentModel.newQuery()
                                    .where("id", "1")
                                    .firstOrFail()
                                    .toObject();
                                Assert.assertEquals(entity.getName(), "dddddd");
                                throw new RuntimeException("业务上抛了个异常");
                            }, 1, true);
                        } catch (RuntimeException e) {
                        }
                    }, 1, true);
                }, 1, true);
            }, 3, true);
        });
    }

    @Test
    public void 事物_lock_in_share_mode() {
        studentModel.newQuery().transaction(() -> {
            studentModel.newQuery().where("id", "3").sharedLock().get();
        }, 3, true);
    }

    @Test
    public void 事物_for_update() {
        studentModel.newQuery().transaction(() -> {
            studentModel.newQuery().where("id", "3").lockForUpdate().get();
        }, 3, true);
    }

    @Test
    public void 安全_更新操作需要确认() {
        int update = studentModel.newQuery().data("name", "xxcc").whereRaw("1").update();
        Assert.assertEquals(update, 10);

        List<StudentModel.Entity> entities = studentModel.newQuery().get().toObjectList();
        Assert.assertEquals(entities.get(0).getName(), entities.get(2).getName());

        Assert.assertThrows(ConfirmOperationException.class, () -> {
            studentModel.newQuery().data("name", "xxcc").update();
        });
    }

    @Test
    public void 安全_SQL注入() {
        String 用户非法输入 = "小明\' and 0<>(select count(*) from student) and \'1";

        Assert.assertThrows(EntityNotFoundException.class, () -> {
            studentModel.newQuery().where("name", 用户非法输入).firstOrFail();
        });
    }

//    @Test
//    public void 便捷_model方法定义() {
//        StudentModel.Entity byId = studentModel.getById("3");
//        Assert.assertEquals(byId.getName(), "小腾");
//
//        String nameById = studentModel.getNameById("4");
//        Assert.assertEquals(nameById, "小云");
//    }

    @Test
    public void 分页_快速分页() {
        Paginate<StudentModel.Entity> paginate =
            studentModel.newQuery().orderBy(StudentModel.id).simplePaginate(1, 3);
        System.out.println(paginate);
        Assert.assertEquals(paginate.getCurrentPage(), 1);
        Assert.assertNotNull(paginate.getFrom());
        Assert.assertNotNull(paginate.getTo());
        Assert.assertEquals(paginate.getFrom().intValue(), 1);
        Assert.assertEquals(paginate.getTo().intValue(), 3);
        Assert.assertNull(paginate.getLastPage());
        Assert.assertNull(paginate.getTotal());


        Paginate<StudentModel.Entity> paginate2 = studentModel.newQuery()
            .where("sex", "1")
            .orWhere((builder -> builder.where("sex", "2")))
            .orderBy("id")
            .simplePaginate(2, 3);
        System.out.println(paginate2);
        Assert.assertEquals(paginate2.getCurrentPage(), 2);
        Assert.assertNotNull(paginate2.getFrom());
        Assert.assertNotNull(paginate2.getTo());
        Assert.assertEquals(paginate2.getFrom().intValue(), 4);
        Assert.assertEquals(paginate2.getTo().intValue(), 6);
        Assert.assertNull(paginate2.getLastPage());
        Assert.assertNull(paginate2.getTotal());

        Paginate<StudentModel.Entity> paginate3 = studentModel.newQuery()
            .where("sex", "1")
            .orWhere((builder -> builder.where("sex", "2")))
            .orderBy("id")
            .simplePaginate(3, 3);
        System.out.println(paginate3);
        Assert.assertEquals(paginate3.getCurrentPage(), 3);
        Assert.assertNotNull(paginate3.getFrom());
        Assert.assertNotNull(paginate3.getTo());
        Assert.assertEquals(paginate3.getFrom().intValue(), 7);
        Assert.assertEquals(paginate3.getTo().intValue(), 9);
        Assert.assertNull(paginate3.getLastPage());
        Assert.assertNull(paginate3.getTotal());


        Paginate<StudentModel.Entity> paginate4 = studentModel.newQuery()
            .orderBy("id")
            .where("sex", "1")
            .orWhere((builder -> builder.where("sex", "2")))
            .simplePaginate(4, 3);
        System.out.println(paginate4);
        Assert.assertEquals(paginate4.getCurrentPage(), 4);
        Assert.assertNotNull(paginate4.getFrom());
        Assert.assertNotNull(paginate4.getTo());
        Assert.assertEquals(paginate4.getFrom().intValue(), 10);
        Assert.assertEquals(paginate4.getTo().intValue(), 10);
        Assert.assertNull(paginate4.getLastPage());
        Assert.assertNull(paginate4.getTotal());


        Paginate<StudentModel.Entity> paginate5 = studentModel.newQuery()
            .where("sex", "1")
            .orWhere((builder -> builder.where("sex", "2")))
            .orderBy("id")
            .simplePaginate(5, 3);
        System.out.println(paginate5);
        Assert.assertEquals(paginate5.getCurrentPage(), 5);
        Assert.assertNull(paginate5.getFrom());
        Assert.assertNull(paginate5.getTo());
//        Assert.assertEquals(paginate5.getFrom().intValue(), 13);
//        Assert.assertEquals(paginate5.getTo().intValue(), 10);
        Assert.assertNull(paginate5.getLastPage());
        Assert.assertNull(paginate5.getTotal());

    }

    @Test
    public void 分页_通用分页() {
        Paginate<StudentModel.Entity> paginate =
            studentModel.newQuery().orderBy("id").paginate(1,
                4);
        System.out.println(paginate);
        Assert.assertEquals(paginate.getCurrentPage(), 1);
        Assert.assertNotNull(paginate.getFrom());
        Assert.assertNotNull(paginate.getTo());
        Assert.assertEquals(paginate.getFrom().intValue(), 1);
        Assert.assertEquals(paginate.getTo().intValue(), 4);
        Assert.assertNotNull(paginate.getLastPage());
        Assert.assertNotNull(paginate.getTotal());
        Assert.assertEquals(paginate.getLastPage().intValue(), 3);
        Assert.assertEquals(paginate.getTotal().intValue(), 10);


        Paginate<StudentModel.Entity> paginate2 =
            studentModel.newQuery()
                .andWhere((builder -> builder.where("sex", "1")))
                .orWhere((builder -> builder.where("sex", "2")))
                .orderBy("id")
                .paginate(2, 4);
        System.out.println(paginate2);
        Assert.assertEquals(paginate2.getCurrentPage(), 2);
        Assert.assertNotNull(paginate2.getFrom());
        Assert.assertNotNull(paginate2.getTo());
        Assert.assertEquals(paginate2.getFrom().intValue(), 5);
        Assert.assertEquals(paginate2.getTo().intValue(), 8);
        Assert.assertNotNull(paginate2.getLastPage());
        Assert.assertNotNull(paginate2.getTotal());
        Assert.assertEquals(paginate2.getLastPage().intValue(), 3);
        Assert.assertEquals(paginate2.getTotal().intValue(), 10);


//        Paginate<StudentModel.Entity> paginate3 =
//            studentModel.newQuery()
//                .select("id", "name")
//                .orderBy("id")
//                .where("sex", "1")
//                .orWhere((builder -> builder.where(
//                    "sex",
//                    "2")))
//                .group("id", "name")
//                .paginate(3, 4);
//        System.out.println(paginate3);
//        Assert.assertEquals(paginate3.getCurrentPage(), 3);
//        Assert.assertNotNull(paginate3.getFrom());
//        Assert.assertNotNull(paginate3.getTo());
//        Assert.assertEquals(paginate3.getFrom().intValue(), 9);
//        Assert.assertEquals(paginate3.getTo().intValue(), 10);
//        Assert.assertNotNull(paginate3.getLastPage());
//        Assert.assertNotNull(paginate3.getTotal());
//        Assert.assertEquals(paginate3.getLastPage().intValue(), 1);
//        Assert.assertEquals(paginate3.getTotal().intValue(), 1);

        // 防止过界
        Paginate<StudentModel.Entity> paginate4 = studentModel.newQuery()
            .orderBy("id")
            .where("sex", "1")
            .orWhere((builder -> builder.where("sex", "2")))
            .paginate(4, 4);
        System.out.println(paginate4);
        Assert.assertEquals(paginate4.getCurrentPage(), 4);
        Assert.assertNull(paginate4.getFrom());
        Assert.assertNull(paginate4.getTo());
//        Assert.assertEquals(paginate4.getFrom().intValue(), 9);
//        Assert.assertEquals(paginate4.getTo().intValue(), 10);
        Assert.assertNotNull(paginate4.getLastPage());
        Assert.assertNotNull(paginate4.getTotal());
        Assert.assertEquals(paginate4.getLastPage().intValue(), 3);
        Assert.assertEquals(paginate4.getTotal().intValue(), 10);
    }

    @Test
    public void 原生() {
        Record<StudentModel.Entity, Integer> record = studentModel.newQuery()
            .query("select * from student where id=1", new ArrayList<>());
        Assert.assertNotNull(record);
        Assert.assertEquals(record.toObject().getId().intValue(), 1);

        List<String> e = new ArrayList<>();
        e.add("2");
        RecordList<StudentModel.Entity, Integer> records = studentModel.newQuery()
            .queryList("select * from student where sex=?", e);
        Assert.assertEquals(records.size(), 4);
        Assert.assertEquals(records.get(0).toObject().getId().intValue(), 1);

        List<String> e2 = new ArrayList<>();
        e2.add("134");
        e2.add("testNAme");
        e2.add("11");
        e2.add("1");
        int execute = studentModel.newQuery()
            .execute("insert into `student`(`id`,`name`,`age`,`sex`) values( ? , ? , ? , ? )", e2);
        Assert.assertEquals(execute, 1);

        Record<StudentModel.Entity, Integer> query = studentModel.newQuery()
            .query("select * from student where sex=12", new ArrayList<>());
        Assert.assertNull(query);

        Assert.assertThrows(EntityNotFoundException.class, () -> {
            studentModel.newQuery().queryOrFail("select * from student where sex=12", new ArrayList<>());
        });
    }

}
