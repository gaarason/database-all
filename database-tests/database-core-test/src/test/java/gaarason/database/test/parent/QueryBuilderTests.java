package gaarason.database.test.parent;

import gaarason.database.appointment.JoinType;
import gaarason.database.appointment.OrderBy;
import gaarason.database.appointment.Paginate;
import gaarason.database.contract.connection.GaarasonDataSource;
import gaarason.database.contract.eloquent.Builder;
import gaarason.database.contract.eloquent.Record;
import gaarason.database.contract.eloquent.RecordList;
import gaarason.database.exception.ConfirmOperationException;
import gaarason.database.exception.EntityNotFoundException;
import gaarason.database.test.models.normal.StudentCombination;
import gaarason.database.test.models.normal.StudentModel;
import gaarason.database.test.models.normal.StudentReversal;
import gaarason.database.test.parent.base.BaseTests;
import gaarason.database.test.utils.MultiThreadUtil;
import gaarason.database.util.LocalDateUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.ZoneOffset;
import java.util.*;

@Slf4j
@FixMethodOrder(MethodSorters.JVM)
abstract public class QueryBuilderTests extends BaseTests {

    private static final StudentModel studentModel = new StudentModel();

    private static final StudentReversal.Model studentReversalModel = new StudentReversal.Model();

    private static final StudentCombination studentCombination = new StudentCombination();

    @Override
    protected GaarasonDataSource getGaarasonDataSource() {
        return studentModel.getGaarasonDataSource();
    }

    @Override
    protected List<TABLE> getInitTables() {
        return Collections.singletonList(TABLE.student);
    }

    @Test
    public void 新增_多线程_循环_非entity方式() throws InterruptedException {
        // 原本数据量
        Long beforeCount = studentModel.newQuery().count("id");
        Assert.assertEquals(10L, beforeCount.longValue());

        // 插入多次
        MultiThreadUtil.run(10, 100, () -> {
            List<String> columnNameList = new ArrayList<>();
            columnNameList.add("name");
            columnNameList.add("age");
            columnNameList.add("sex");
            List<Object> valueList = new ArrayList<>();
            valueList.add("testNAme134");
            valueList.add("11");
            valueList.add("1");

            int insert = studentModel.newQuery().column(columnNameList).value(valueList).insert();
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
        Assert.assertEquals(1010L, afterCount.longValue());
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
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
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
    public void 新增_单条记录_mapStyle() {
        Map<String, Object> map = new HashMap<>();
        map.put("id", 99);
        map.put("name", "姓名");
        map.put("age", 13);
        map.put("sex", 1);
        map.put("teacher_id", 0);
        map.put("created_at", LocalDateUtils.SIMPLE_DATE_FORMAT.get().format(new Date(1312312312)));
        map.put("updated_at", LocalDateUtils.SIMPLE_DATE_FORMAT.get().format(new Date(1312312312)));
        int insert = studentModel.newQuery().insertMapStyle(map);
        Assert.assertEquals(insert, 1);

        StudentModel.Entity entityFirst = studentModel.newQuery().where("id", "99").firstOrFail().toObject();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Assert.assertNotNull(entityFirst);
        Assert.assertEquals(map.get("id"), entityFirst.getId());
        Assert.assertEquals(map.get("age"), entityFirst.getAge().intValue());
        Assert.assertEquals(map.get("name"), entityFirst.getName());
        Assert.assertEquals(map.get("teacher_id"), entityFirst.getTeacherId());
        // 这两个字段在entity中标记为不可更新
        // mapStyle 可不管这些，自然是直接插入啦
        Assert.assertEquals(map.get("created_at"), formatter.format(entityFirst.getCreatedAt()));
        Assert.assertEquals(map.get("updated_at"), formatter.format(entityFirst.getUpdatedAt()));
    }

    @Test
    public void 新增_单条记录_并获取数据库自增id() {
        StudentModel.Entity entity = new StudentModel.Entity();
        entity.setName("姓名");
        entity.setAge(Byte.valueOf("13"));
        entity.setSex(Byte.valueOf("1"));
        entity.setTeacherId(0);
        entity.setCreatedAt(new Date(1312312312));
        entity.setUpdatedAt(new Date(1312312312));
        Object insert = studentModel.newQuery().insertGetId(entity);
        Assert.assertEquals(insert, 20);

        StudentModel.Entity entityFirst = studentModel.newQuery().where("id", "20").firstOrFail().toObject();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
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
    public void 新增_单条记录_并获取数据库自增id_mapStyle() {
        Map<String, Object> map = new HashMap<>();
        map.put("name", "姓名");
        map.put("age", 13);
        map.put("sex", 1);
        map.put("teacher_id", 0);
        map.put("created_at", LocalDateUtils.SIMPLE_DATE_FORMAT.get().format(new Date(1312312312)));
        map.put("updated_at", LocalDateUtils.SIMPLE_DATE_FORMAT.get().format(new Date(1312312312)));
        Object insert = studentModel.newQuery().insertGetIdMapStyle(map);
        Assert.assertEquals(insert, 20);

        StudentModel.Entity entityFirst = studentModel.newQuery().where("id", "20").firstOrFail().toObject();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Assert.assertNotNull(entityFirst);

        Assert.assertEquals(20, entityFirst.getId().intValue());
        Assert.assertEquals(map.get("age"), entityFirst.getAge().intValue());
        Assert.assertEquals(map.get("name"), entityFirst.getName());
        Assert.assertEquals(map.get("teacher_id"), entityFirst.getTeacherId());
        // 这两个字段在entity中标记为不可更新
        // mapStyle 可不管这些，自然是直接插入啦
        Assert.assertEquals(map.get("created_at"), formatter.format(entityFirst.getCreatedAt()));
        Assert.assertEquals(map.get("updated_at"), formatter.format(entityFirst.getUpdatedAt()));
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
    public void 新增_使用list单次新增多条记录_mapStyle() {
        List<Map<String, Object>> entityList = new ArrayList<>();
        for (int i = 99; i < 10000; i++) {
            Map<String, Object> map = new HashMap<>();
            map.put("name", "姓名");
            map.put("age", 13);
            map.put("sex", 1);
            map.put("teacher_id", i * 3);
            map.put("created_at", LocalDateUtils.SIMPLE_DATE_FORMAT.get().format(new Date()));
            map.put("updated_at", LocalDateUtils.SIMPLE_DATE_FORMAT.get().format(new Date()));
            entityList.add(map);
        }
        int insert = studentModel.newQuery().insertMapStyle(entityList);
        Assert.assertEquals(insert, 9901);

        RecordList<StudentModel.Entity, Integer> records = studentModel.newQuery()
            .whereBetween("id", "300", "350")
            .orderBy("id", OrderBy.DESC)
            .get();
        Assert.assertEquals(records.size(), 51);
    }

    @Test
    public void 新增_使用ValueList单次新增多条记录() {
        List<String> columnNameList = new ArrayList<>();
        columnNameList.add("name");
        columnNameList.add("age");
        columnNameList.add("sex");
        List<List<Object>> valuesList = new ArrayList<>();
        for (int i = 1; i < 10; i++) {
            List<Object> valueList = new ArrayList<>();
            valueList.add("testNAme=" + i);
            valueList.add("11");
            valueList.add("1");
            valuesList.add(valueList);
        }


        List<Integer> integers = studentModel.newQuery().column(columnNameList).valueList(valuesList).insertGetIds();
        Assert.assertEquals(integers.size(), valuesList.size());

        List<StudentModel.Entity> entityList = studentModel.newQuery()
            .whereIn(studentModel.getPrimaryKeyColumnName(), integers)
            .orderBy("id", OrderBy.DESC)
            .get().toObjectList();
        System.out.println(entityList);
        Assert.assertEquals(entityList.size(), valuesList.size());

    }

    @Test
    public void 更新_普通更新_data() {
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
    public void 更新_普通更新_dataIgnoreNull() {
        int update = studentModel.newQuery()
            .dataIgnoreNull("name", null)
            .dataIgnoreNull("age", 55)
            .where("id", "3")
            .update();
        Assert.assertEquals(update, 1);

        StudentModel.Entity entity = studentModel.findOrFail(3).toObject();
        Assert.assertEquals(entity.getId().intValue(), 3);
        Assert.assertEquals(entity.getAge().intValue(), 55);
        Assert.assertNotNull(entity.getName());
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
    public void 更新_通过MAP更新_dataIgnoreNull() {
        Map<String, Object> map = new HashMap<>();
        map.put("name", "gggg");
        map.put("age", null);

        int update = studentModel.newQuery().dataIgnoreNull(map).where("id", "3").update();
        Assert.assertEquals(update, 1);
        StudentModel.Entity entity = studentModel.newQuery().where("id", "3").firstOrFail().toObject();
        Assert.assertEquals(entity.getId().intValue(), 3);
        Assert.assertEquals(entity.getName(), "gggg");
        Assert.assertNotNull(entity.getAge());

        Assert.assertThrows(ConfirmOperationException.class, () -> {
            studentModel.newQuery().data("name", "ee").update();
        });
    }

    @Test
    public void 更新_通过MAP更新() {
        Map<String, Object> map = new HashMap<>();
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
    public void 更新_通过MAP更新_2() {
        Map<String, Object> map = new HashMap<>();
        map.put("name", "gggg");
        map.put("age", "7");

        int update = studentReversalModel.newQuery().where("id", "3").updateMapStyle(map);
        Assert.assertEquals(update, 1);
        StudentReversal student = studentReversalModel.newQuery().where("id", "3").firstOrFail().toObject();
        Assert.assertEquals(student.getId().intValue(), 3);
        Assert.assertEquals(student.getName(), "gggg");
        Assert.assertEquals(student.getAge(), Byte.valueOf("7"));

        Assert.assertThrows(ConfirmOperationException.class, () -> {
            studentReversalModel.newQuery().data("name", "ee").update();
        });
    }

    @Test
    public void 更新_通过MAP更新_3() {
        Map<String, Object> map = new HashMap<>();
        map.put("name", "gggg");
        map.put("age", "7");
        int update = studentCombination.newQuery().where("id", "3").updateMapStyle(map);
        Assert.assertEquals(update, 1);
        StudentCombination student = studentCombination.newQuery().where("id", "3").firstOrFail().toObject();
        Assert.assertEquals(student.getId().intValue(), 3);
        Assert.assertEquals(student.getName(), "gggg");
        Assert.assertEquals(student.getAge(), Byte.valueOf("7"));

        Assert.assertThrows(ConfirmOperationException.class, () -> {
            studentCombination.newQuery().data("name", "ee").update();
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
        System.out.println("first2 : " + first2);
        Assert.assertEquals(first2.getId(), new Integer(1));
        Assert.assertEquals(first2.getId().intValue(), 1);
        Assert.assertEquals(first2.getName(), "小明");
        Assert.assertNull(first2.getAge());
        Assert.assertNull(first2.getTeacherId());
        Assert.assertEquals(first2.getCreatedAt().getTime() / 1000L,
            LocalDateUtils.str2LocalDateTime("2009-03-14 17:15:23.0").toEpochSecond(ZoneOffset.of("+8")));
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
        System.out.println("RecordFirst5 : " + RecordFirst5);
        Assert.assertNotNull(RecordFirst5);
        StudentModel.Entity first5 = RecordFirst5.toObject();
        Assert.assertEquals(first5.getId(), new Integer(1));
        Assert.assertEquals(first5.getId().intValue(), 1);
        Assert.assertEquals(first5.getName(), "小明");
        Assert.assertEquals(first5.getAge().intValue(), 6);
        Assert.assertEquals(first5.getTeacherId().intValue(), 6);
        Assert.assertEquals(LocalDateUtils.date2LocalDateTime(first5.getCreatedAt()),
            LocalDateUtils.str2LocalDateTime("2009-03-14 17:15:23.0"));
        Assert.assertEquals(LocalDateUtils.date2LocalDateTime(first5.getUpdatedAt()),
            LocalDateUtils.str2LocalDateTime("2010-04-24 22:11:03.0"));

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
            StudentModel.Entity entity2 = entities2.get(0);
            System.out.println(entity2);
            Assert.assertNotNull(entity2);
            Assert.assertEquals(entity2.getId(), new Integer(1));
            Assert.assertEquals(entity2.getId().intValue(), 1);
            Assert.assertEquals(entity2.getName(), "小明");
            Assert.assertEquals(entity2.getAge().intValue(), 6);
            Assert.assertEquals(entity2.getTeacherId().intValue(), 6);
            Assert.assertEquals(LocalDateUtils.date2LocalDateTime(entity2.getCreatedAt()),
                LocalDateUtils.str2LocalDateTime("2009-03-14 17:15:23.0"));
            Assert.assertEquals(LocalDateUtils.date2LocalDateTime(entity2.getUpdatedAt()),
                LocalDateUtils.str2LocalDateTime("2010-04-24 22:11:03.0"));
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
    public void 查询_多条记录_分块_兼容模式() throws InterruptedException {
        Runtime r = Runtime.getRuntime();
        r.gc();
        long startMem = r.totalMemory(); // 开始时内存
        System.out.println("开始时内存: " + startMem);
        // 数据库数据有限,此处模拟大数据
        新增_多线程_循环_非entity方式();
        System.out.println("插入数据后的内存: " + r.totalMemory());
        Builder<StudentModel.Entity, Integer> queryBuilder = studentModel.newQuery();

        System.out.println("构造sql后的内存: " + r.totalMemory());
        StringBuilder temp = new StringBuilder();
        queryBuilder.dealChunk(20, records -> {
            // do something
            for (Record<StudentModel.Entity, Integer> record : records) {
                // do something
                temp.append(record.toSearch());
            }
            return true;
        });
        System.out.println("执行sql后的内存: " + r.totalMemory());
        //System.out.println(temp.toString());

        long orz = r.totalMemory() - startMem; // 剩余内存 现在
        System.out.println("最后的内存: " + r.totalMemory());
        System.out.println("执行消耗的内存差: " + orz);
    }

    @Test
    public void 查询_多条记录_分块_性能模式() throws InterruptedException {
        Runtime r = Runtime.getRuntime();
        r.gc();
        long startMem = r.totalMemory(); // 开始时内存
        System.out.println("开始时内存: " + startMem);
        // 数据库数据有限,此处模拟大数据
        新增_多线程_循环_非entity方式();
        System.out.println("插入数据后的内存: " + r.totalMemory());
        Builder<StudentModel.Entity, Integer> queryBuilder = studentModel.newQuery();

        System.out.println("构造sql后的内存: " + r.totalMemory());
        StringBuilder temp = new StringBuilder();
        queryBuilder.dealChunk(20, StudentModel.Entity::getId, records -> {
            // do something
            for (Record<StudentModel.Entity, Integer> record : records) {
                // do something
                temp.append(record.toSearch());
            }
            return true;
        });
        System.out.println("执行sql后的内存: " + r.totalMemory());
        //System.out.println(temp.toString());

        long orz = r.totalMemory() - startMem; // 剩余内存 现在
        System.out.println("最后的内存: " + r.totalMemory());
        System.out.println("执行消耗的内存差: " + orz);
    }

    @Test
    public void 查询_多条记录_分块_为空时不再执行() {
        studentModel.newQuery().where("id", "12321").dealChunk(10, records -> {
            Assert.assertFalse(true);
            return true;
        });
    }

    @Test
    public void 查询_调用mysql中的其他函数() {
        Record<StudentModel.Entity, Integer> entityRecord = studentModel.newQuery()
            .selectFunction("concat_ws", "\"-\",`name`,`id`", "newKey")
            .first();
        Assert.assertNotNull(entityRecord);
        StudentModel.Entity entity = entityRecord.toObject();
        Map<String, Object> stringObjectMap = entityRecord.toMap();
        Assert.assertNull(entity.getId());
        Assert.assertNull(entity.getName());
        Assert.assertNull(entity.getAge());
        Assert.assertNull(entity.getTeacherId());
        Assert.assertEquals(stringObjectMap.get("newKey"), "小明-1");
    }

    @Test
    public void 查询_聚合函数() {
        // select count(*) as 'eUTIdN' from `student` where `sex`="1" limit 1
        Long count0 = studentModel.newQuery().where("sex", "1").count("id");
        Assert.assertEquals(count0.intValue(), 6);

        // select count(*) as 'eUTIdN' from `student` where `sex`="1" limit 1
        Long count1 = studentModel.newQuery().where("sex", "1").count();
        Assert.assertEquals(count1.intValue(), 6);

        // select count(age) as 'DidUua' from `student` where `sex`="1" limit 1
        Long count = studentModel.newQuery().where("sex", "1").count("age");
        Assert.assertEquals(count.intValue(), 6);

        // select max(id) as 'KUjDrZ' from `student` where `sex`="1" limit 1
        String max1 = studentModel.newQuery().where("sex", "1").max("id");
        Assert.assertEquals(max1, "10");

        // select min(id) as 'PgtEoj' from `student` where `sex`="1" limit 1
        String min = studentModel.newQuery().where("sex", "1").min("id");
        Assert.assertEquals(min, "3");

        // select avg(id) as 'DKYNYr' from `student` where `sex`="1" limit 1
        BigDecimal avg = studentModel.newQuery().where("sex", "1").avg("id");
        Assert.assertEquals(avg.toString(), "7.1667");

        // select sum(id) as 'UGvQJm' from `student` where `sex`="2" limit 1
        BigDecimal sum = studentModel.newQuery().where("sex", "2").sum("id");
        Assert.assertEquals(sum.toString(), "12");
    }

    @Test
    public void 查询_聚合函数_count_带group() {
        // 以下为手动
        // select count(*) as 'ccc' from (select `sex` from `student` group by `sex`)t
        RecordList<StudentModel.Entity, Integer> records = studentModel.newQuery()
            .selectFunction("count", "*", "ccc")
            .from("t",
                builder -> builder.group("sex").select("sex"))
            .get();
        Assert.assertEquals(records.size(), 1);
        Assert.assertEquals(records.toMapList().get(0).get("ccc").toString(), "2");

        // select count(*) as 'ccc' from (select `sex` from `student` group by `sex`)t limit 1
        Record<StudentModel.Entity, Integer> record = studentModel.newQuery()
            .selectFunction("count", "*", "ccc")
            .from("t",
                builder -> builder.group("sex").select("sex"))
            .firstOrFail();
        Assert.assertEquals(record.toMap().get("ccc").toString(), "2");

        // 以下为自动
        // select count(sex) as 'qQhLPU' from (select `sex` from `student` group by `sex`)qQhLPUsub limit 1
        Long count01 = studentModel.newQuery().group("sex").count("sex");
        Assert.assertEquals(count01.longValue(), 2);

        // select count(*) as 'GtMbMe' from (select `sex`,`age`,`name` from `student` group by `sex`,`age`,`name`)GtMbMesub limit 1
        Long count02 = studentModel.newQuery().group("sex").group("age", "name").count("*");
        Assert.assertEquals(count02.longValue(), 10);

        // select count(*) as 'oLmXhJ' from (select `sex` from `student` group by `sex`)oLmXhJsub limit 1
        Long count03 = studentModel.newQuery().group("sex").count();
        Assert.assertEquals(count03.longValue(), 2);

        // select count(*) as 'HXXFaq' from (select `sex` from `student` group by `sex`)HXXFaqsub limit 1
        Long count04 = studentModel.newQuery().group("sex").select("sex").count("*");
        Assert.assertEquals(count04.longValue(), 2);
    }

    @Test
    public void 查询_聚合函数_max_带group() {
        // 以下为手动
        // select max(sex) as 'ccc' from (select `sex` from `student` group by `sex`)t
        RecordList<StudentModel.Entity, Integer> records = studentModel.newQuery()
            .selectFunction("max", "sex", "ccc")
            .from("t",
                builder -> builder.group("sex").select("sex"))
            .get();
        Assert.assertEquals(records.size(), 1);
        Assert.assertEquals(records.toMapList().get(0).get("ccc").toString(), "2");

        // select max(sex) as 'ccc' from (select `sex` from `student` group by `sex`)t limit 1
        Record<StudentModel.Entity, Integer> record = studentModel.newQuery()
            .selectFunction("max", "sex", "ccc")
            .from("t",
                builder -> builder.group("sex").select("sex"))
            .firstOrFail();
        Assert.assertEquals(record.toMap().get("ccc").toString(), "2");

        // 以下为自动
        // select max(sex) as 'MlXcWL' from (select `sex` from `student` group by `sex`)MlXcWLsub limit 1
        String max1 = studentModel.newQuery().group("sex").max("sex");
        Assert.assertEquals(max1, "2");

        // select max(sex) as 'ZldfCz' from (select `sex`,`age`,`name` from `student` group by `sex`,`age`,`name`)ZldfCzsub limit 1
        String count02 = studentModel.newQuery().group("sex").group("age", "name").max("sex");
        Assert.assertEquals(count02, "2");

        // select max(sex) as 'uOhnwy' from (select `sex` from `student` group by `sex`)uOhnwysub limit 1
        String count03 = studentModel.newQuery().group("sex").max("sex");
        Assert.assertEquals(count03, "2");

        // select max(sex) as 'thbZAz' from (select `sex` from `student` group by `sex`)thbZAzsub limit 1
        String count04 = studentModel.newQuery().group("sex").select("sex").max("sex");
        Assert.assertEquals(count04, "2");
    }

    @Test
    public void 查询_聚合函数_min_带group() {
        // 以下为手动
        // select min(sex) as 'ccc' from (select `sex` from `student` group by `sex`)t limit 1
        Record<StudentModel.Entity, Integer> record = studentModel.newQuery()
            .selectFunction("min", "sex", "ccc")
            .from("t",
                builder -> builder.group("sex").select("sex"))
            .firstOrFail();
        Assert.assertEquals(record.toMap().get("ccc").toString(), "1");

        // 以下为自动
        // select min(sex) as 'NZpuZx' from (select `sex` from `student` group by `sex`)NZpuZxsub limit 1
        String min1 = studentModel.newQuery().group("sex").min("sex");
        Assert.assertEquals(min1, "1");

        // select min(sex) as 'YAhmzr' from (select `sex`,`age`,`name` from `student` group by `sex`,`age`,`name`)YAhmzrsub limit 1
        String min2 = studentModel.newQuery().group("sex").group("age", "name").min("sex");
        Assert.assertEquals(min2, "1");

        // select min(sex) as 'RntldM' from (select `sex` from `student` group by `sex`)RntldMsub limit 1
        String min3 = studentModel.newQuery().group("sex").min("sex");
        Assert.assertEquals(min3, "1");

        // select min(sex) as 'oUnMLS' from (select `sex` from `student` group by `sex`)oUnMLSsub limit 1
        String min4 = studentModel.newQuery().group("sex").select("sex").min("sex");
        Assert.assertEquals(min4, "1");
    }

    @Test
    public void 查询_聚合函数_avg_带group() {
        // 以下为手动
        // select avg(sex) as 'ccc' from (select `sex` from `student` group by `sex`)t limit 1
        Record<StudentModel.Entity, Integer> record = studentModel.newQuery()
            .selectFunction("avg", "sex", "ccc")
            .from("t",
                builder -> builder.group("sex").select("sex"))
            .firstOrFail();
        Assert.assertEquals(record.toMap().get("ccc").toString(), "1.5000");

        // 以下为自动
        // select avg(sex) as 'IImErp' from (select `sex` from `student` group by `sex`)IImErpsub limit 1
        BigDecimal res1 = studentModel.newQuery().group("sex").avg("sex");
        Assert.assertEquals(res1.toString(), "1.5000");

        // select avg(sex) as 'JuDitC' from (select `sex`,`age`,`name` from `student` group by `sex`,`age`,`name`)JuDitCsub limit 1
        BigDecimal res2 = studentModel.newQuery().group("sex").group("age", "name").avg("sex");
        Assert.assertEquals(res2.toString(), "1.4000");

        // select avg(sex) as 'LRxkwD' from (select `sex` from `student` group by `sex`)LRxkwDsub limit 1
        BigDecimal res3 = studentModel.newQuery().group("sex").avg("sex");
        Assert.assertEquals(res3.toString(), "1.5000");

        // select avg(sex) as 'tcRKqt' from (select `sex` from `student` group by `sex`)tcRKqtsub limit 1
        BigDecimal res4 = studentModel.newQuery().group("sex").select("sex").avg("sex");
        Assert.assertEquals(res4.toString(), "1.5000");
    }

    @Test
    public void 查询_聚合函数_sum_带group() {
        // 以下为手动
        // select sum(sex) as 'ccc' from (select `sex` from `student` group by `sex`)t limit 1
        Record<StudentModel.Entity, Integer> record = studentModel.newQuery()
            .selectFunction("sum", "sex", "ccc")
            .from("t",
                builder -> builder.group("sex").select("sex"))
            .firstOrFail();
        Assert.assertEquals(record.toMap().get("ccc").toString(), "3");

        // 以下为自动
        // select sum(sex) as 'DLfORT' from (select `sex` from `student` group by `sex`)DLfORTsub limit 1
        BigDecimal min1 = studentModel.newQuery().group("sex").sum("sex");
        Assert.assertEquals(min1.toString(), "3");

        // select sum(sex) as 'yMpOUV' from (select `sex`,`age`,`name` from `student` group by `sex`,`age`,`name`)yMpOUVsub limit 1
        BigDecimal min2 = studentModel.newQuery().group("sex").group("age", "name").sum("sex");
        Assert.assertEquals(min2.toString(), "14");

        // select sum(sex) as 'MxNqTs' from (select `sex` from `student` group by `sex`)MxNqTssub limit 1
        BigDecimal min3 = studentModel.newQuery().group("sex").sum("sex");
        Assert.assertEquals(min3.toString(), "3");

        // select sum(sex) as 'aVtVwE' from (select `sex` from `student` group by `sex`)aVtVwEsub limit 1
        BigDecimal min4 = studentModel.newQuery().group("sex").select("sex").sum("sex");
        Assert.assertEquals(min4.toString(), "3");
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
        Assert.assertNotEquals(LocalDateUtils.date2LocalDateTime(first.getCreatedAt()),
            LocalDateUtils.str2LocalDateTime("2009-03-14 15:11:29.0"));
        Assert.assertEquals(LocalDateUtils.date2LocalDateTime(first.getCreatedAt()),
            LocalDateUtils.str2LocalDateTime("2009-03-14 15:11:23.0"));
        Assert.assertEquals(LocalDateUtils.date2LocalDateTime(first.getUpdatedAt()),
            LocalDateUtils.str2LocalDateTime("2010-04-24 22:11:03.0"));

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
        Assert.assertNotEquals(LocalDateUtils.date2LocalDateTime(first2.getCreatedAt()),
            LocalDateUtils.str2LocalDateTime("2009-03-14 15:11:29.0"));
        Assert.assertEquals(LocalDateUtils.date2LocalDateTime(first2.getCreatedAt()),
            LocalDateUtils.str2LocalDateTime("2009-03-14 15:15:23.0"));
        Assert.assertEquals(LocalDateUtils.date2LocalDateTime(first2.getUpdatedAt()),
            LocalDateUtils.str2LocalDateTime("2010-04-24 22:11:03.0"));

        Record<StudentModel.Entity, Integer> entityRecord3 =
            studentModel.newQuery().whereColumn(StudentModel.Entity::getId, StudentModel.Entity::getSex).first();
        Assert.assertNotNull(entityRecord3);
        System.out.println(entityRecord3);
        StudentModel.Entity first3 = entityRecord3.toObject();
        Assert.assertEquals(first3.getId(), new Integer(2));
        Assert.assertEquals(first3.getId().intValue(), 2);
        Assert.assertEquals(first3.getName(), "小张");
        Assert.assertEquals(first3.getAge().intValue(), 11);
        Assert.assertEquals(first3.getTeacherId().intValue(), 6);
        Assert.assertNotEquals(LocalDateUtils.date2LocalDateTime(first3.getCreatedAt()),
            LocalDateUtils.str2LocalDateTime("2009-03-14 15:11:29.0"));
        Assert.assertEquals(LocalDateUtils.date2LocalDateTime(first3.getCreatedAt()),
            LocalDateUtils.str2LocalDateTime("2009-03-14 15:15:23.0"));
        Assert.assertEquals(LocalDateUtils.date2LocalDateTime(first3.getUpdatedAt()),
            LocalDateUtils.str2LocalDateTime("2010-04-24 22:11:03.0"));
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
        Assert.assertNotEquals(LocalDateUtils.date2LocalDateTime(first.getCreatedAt()),
            LocalDateUtils.str2LocalDateTime("2009-03-14 15:11:29.0"));
        Assert.assertEquals(LocalDateUtils.date2LocalDateTime(first.getCreatedAt()),
            LocalDateUtils.str2LocalDateTime("2009-03-14 15:11:23.0"));
        Assert.assertEquals(LocalDateUtils.date2LocalDateTime(first.getUpdatedAt()),
            LocalDateUtils.str2LocalDateTime("2010-04-24 22:11:03.0"));


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

        StudentModel.Entity entity4 = studentModel.newQuery()
            .where(StudentModel.Entity::getCreatedAt, ">=", "2009-03-15 22:15:23")
            .firstOrFail().toObject();
        Assert.assertEquals(entity4.getId().intValue(), 9);
    }

    @Test
    public void 条件_Between() {
        List<StudentModel.Entity> entityList0 = studentModel.newQuery()
            .whereBetweenRaw("id + age", 10, 20)
            .get()
            .toObjectList();
        Assert.assertEquals(4, entityList0.size());

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

        List<StudentModel.Entity> entityList3 = studentModel.newQuery()
            .whereBetween(StudentModel.Entity::getId, "3", "5")
            .whereNotBetween(
                StudentModel.Entity::getId, "3", "4")
            .get()
            .toObjectList();
        Assert.assertEquals(entityList3.size(), 1);
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

        List<StudentModel.Entity> entityList3 = studentModel.newQuery()
            .whereIn(StudentModel.Entity::getId, idList)
            .whereNotIn(StudentModel.Entity::getId,
                idList2)
            .get()
            .toObjectList();
        Assert.assertEquals(entityList3.size(), 3);
    }

    @Test
    public void 条件_whereInIgnoreEmpty() {
        List<Object> idList = new ArrayList<>();
        idList.add("4");
        idList.add("5");
        idList.add("6");
        idList.add("7");
        List<StudentModel.Entity> entityList1 = studentModel.newQuery()
            .whereIn("id", idList).whereInIgnoreEmpty("id", new ArrayList<>())
            .get()
            .toObjectList();
        Assert.assertEquals(entityList1.size(), 4);

        List<Object> idList2 = new ArrayList<>();
        idList2.add("10");
        idList2.add("9");
        idList2.add("7");

        List<StudentModel.Entity> entityList2 = studentModel.newQuery().whereIn("id", idList).whereNotIn("id",
            idList2).whereNotInIgnoreEmpty("id", new ArrayList<>()).get().toObjectList();
        Assert.assertEquals(entityList2.size(), 3);

        List<StudentModel.Entity> entityList3 = studentModel.newQuery()
            .whereIn(StudentModel.Entity::getId, idList)
            .whereNotIn(StudentModel.Entity::getId,
                idList2)
            .whereNotInIgnoreEmpty(StudentModel.Entity::getId, new ArrayList<>())
            .get()
            .toObjectList();
        Assert.assertEquals(entityList3.size(), 3);
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

        List<StudentModel.Entity> entityList3 = studentModel.newQuery()
            .whereIn("id", 4, 5, "6", 7)
            .whereNotIn("id",
                "10", 9, "7")
            .get()
            .toObjectList();
        Assert.assertEquals(entityList3.size(), 3);

        List<StudentModel.Entity> entityList4 = studentModel.newQuery()
            .whereIn(StudentModel.Entity::getId, 4, 5, "6", 7)
            .whereNotIn(StudentModel.Entity::getId,
                "10", 9, "7")
            .get()
            .toObjectList();
        Assert.assertEquals(entityList4.size(), 3);
    }

    @Test
    public void 条件_whereLike() {
        List<StudentModel.Entity> entityList1 = studentModel.newQuery()
            .whereLikeIgnoreNull("name", "小%")
            .get()
            .toObjectList();
        Assert.assertEquals(5, entityList1.size());

        List<StudentModel.Entity> entityList2 = studentModel.newQuery()
            .whereLikeIgnoreNull("name", "小") // 自动拼成 "%小%"
            .whereLikeIgnoreNull("name", null)
            .get()
            .toObjectList();
        Assert.assertEquals(5, entityList2.size());

        Map<String, Object> likeMap = new HashMap<>();
        likeMap.put("name", "%卡");
        Map<String, Object> likeMap2 = null;
        List<StudentModel.Entity> entityList3 = studentModel.newQuery()
            .whereLikeIgnoreNull(likeMap)
            .whereLikeIgnoreNull(likeMap2)
            .get()
            .toObjectList();
        Assert.assertEquals(3, entityList3.size());


        StudentModel.Entity student = new StudentModel.Entity();
        student.setName("%卡");
        StudentModel.Entity student2 = null;
        List<StudentModel.Entity> entityList4 = studentModel.newQuery()
            .whereLikeIgnoreNull(student)
            .whereLikeIgnoreNull(student2)
            .get()
            .toObjectList();
        Assert.assertEquals(3, entityList4.size());
    }

    @Test
    public void 筛选_havingLike() {
        List<StudentModel.Entity> entityList1 = studentModel.newQuery()
            .havingLike("name", "小%")
            .get()
            .toObjectList();
        Assert.assertEquals(5, entityList1.size());

        List<StudentModel.Entity> entityList2 = studentModel.newQuery()
            .havingLike("name", "小")// 自动拼成 "%小%"
            .havingLike("name", null)
            .get()
            .toObjectList();
        Assert.assertEquals(5, entityList2.size());

        Map<String, Object> likeMap = new HashMap<>();
        likeMap.put("name", "%卡");
        Map<String, Object> likeMap2 = null;
        List<StudentModel.Entity> entityList3 = studentModel.newQuery()
            .havingLike(likeMap)
            .havingLike(likeMap2)
            .get()
            .toObjectList();
        Assert.assertEquals(3, entityList3.size());


        StudentModel.Entity student = new StudentModel.Entity();
        student.setName("%卡");
        StudentModel.Entity student2 = null;
        List<StudentModel.Entity> entityList4 = studentModel.newQuery()
            .havingLike(student)
            .havingLike(student2)
            .get()
            .toObjectList();
        Assert.assertEquals(3, entityList4.size());
    }

    @Test
    public void 条件_whereKeywords() {

        List<StudentModel.Entity> list0 = studentModel.newQuery()
            .whereKeywords("小", StudentModel.Entity::getName, StudentModel.Entity::getAge, StudentModel.Entity::getId)
            .get()
            .toObjectList();
        Assert.assertEquals(5, list0.size());

        List<StudentModel.Entity> list = studentModel.newQuery()
            .whereKeywordsIgnoreNull("小", "name", "age", "id")
            .get()
            .toObjectList();
        Assert.assertEquals(5, list.size());

        List<StudentModel.Entity> list1 = studentModel.newQuery()
            .whereKeywordsIgnoreNull("小%", "name", "age", "id")
            .get()
            .toObjectList();
        Assert.assertEquals(5, list1.size());

        List<StudentModel.Entity> list2 = studentModel.newQuery()
            .whereKeywordsIgnoreNull(null, "name", "age", "id")
            .get()
            .toObjectList();
        Assert.assertEquals(10, list2.size());

        List<StudentModel.Entity> list3 = studentModel.newQuery()
            .whereKeywordsIgnoreNull(2, "name", "age", "id")
            .get()
            .toObjectList();
        Assert.assertEquals(1, list3.size());

        List<StudentModel.Entity> list4 = studentModel.newQuery()
            .whereKeywordsIgnoreNull(6, "name", "age", "id")
            .get()
            .toObjectList();
        Assert.assertEquals(3, list4.size());

        List<StudentModel.Entity> list5 = studentModel.newQuery()
            .whereKeywordsIgnoreNull("%6", "name", "age", "id")
            .get()
            .toObjectList();
        Assert.assertEquals(3, list5.size());

        List<StudentModel.Entity> list7 = studentModel.newQuery()
            .whereKeywordsIgnoreNull("%1", "name", "age", "id")
            .whereKeywordsIgnoreNull("%张", "name", "age", "id")
            .get()
            .toObjectList();
        Assert.assertEquals(1, list7.size());

    }

    @Test
    public void 条件_havingKeywords() {

        List<StudentModel.Entity> list0 = studentModel.newQuery()
            .havingKeywords("小", StudentModel.Entity::getName, StudentModel.Entity::getAge, StudentModel.Entity::getId)
            .get()
            .toObjectList();
        Assert.assertEquals(0, list0.size());

        List<StudentModel.Entity> list = studentModel.newQuery()
            .havingKeywords("小", "name", "age", "id")
            .get()
            .toObjectList();
        Assert.assertEquals(0, list.size());

        List<StudentModel.Entity> list1 = studentModel.newQuery()
            .havingKeywords("小%", "name", "age", "id")
            .get()
            .toObjectList();
        Assert.assertEquals(5, list1.size());

        List<StudentModel.Entity> list2 = studentModel.newQuery()
            .havingKeywords(null, "name", "age", "id")
            .get()
            .toObjectList();
        Assert.assertEquals(0, list2.size());

        List<StudentModel.Entity> list3 = studentModel.newQuery()
            .havingKeywords(2, "name", "age", "id")
            .get()
            .toObjectList();
        Assert.assertEquals(1, list3.size());

        List<StudentModel.Entity> list4 = studentModel.newQuery()
            .havingKeywords(6, "name", "age", "id")
            .get()
            .toObjectList();
        Assert.assertEquals(2, list4.size());

        List<StudentModel.Entity> list5 = studentModel.newQuery()
            .havingKeywords("%6", "name", "age", "id")
            .get()
            .toObjectList();
        Assert.assertEquals(3, list5.size());

        List<StudentModel.Entity> list6 = studentModel.newQuery()
            .havingKeywordsIgnoreNull(null, "name", "age", "id")
            .get()
            .toObjectList();
        Assert.assertEquals(10, list6.size());

        List<StudentModel.Entity> list7 = studentModel.newQuery()
            .havingKeywords("%1", "name", "age", "id")
            .havingKeywords("%张", "name", "age", "id")
            .get()
            .toObjectList();
        Assert.assertEquals(1, list7.size());

        List<StudentModel.Entity> list8 = studentModel.newQuery()
            .havingKeywordsIgnoreNull("%1", "name", "age", "id")
            .havingKeywordsIgnoreNull("%张", "name", "age", "id")
            .get()
            .toObjectList();
        Assert.assertEquals(1, list8.size());

        List<StudentModel.Entity> list9 = studentModel.newQuery()
            .havingKeywordsIgnoreNull(null, "name", "age", "id")
            .havingKeywordsIgnoreNull(null, "name", "age", "id")
            .get()
            .toObjectList();
        Assert.assertEquals(10, list9.size());

        List<StudentModel.Entity> list10 = studentModel.newQuery()
            .havingKeywordsIgnoreNull(null, StudentModel.Entity::getName, StudentModel.Entity::getAge,
                StudentModel.Entity::getId)
            .havingKeywordsIgnoreNull(null, StudentModel.Entity::getName, StudentModel.Entity::getAge,
                StudentModel.Entity::getId)
            .get()
            .toObjectList();
        Assert.assertEquals(10, list10.size());
    }


    @Test
    public void 条件_whereMayLike() {
        // select * from `student` where `name`like"小%"
        List<StudentModel.Entity> entityList0 = studentModel.newQuery()
            .whereMayLike(StudentModel.Entity::getName, "小%")
            .get()
            .toObjectList();
        Assert.assertEquals(5, entityList0.size());

        // select * from `student` where `name`like"小%"
        List<StudentModel.Entity> entityList1 = studentModel.newQuery().whereMayLike("name", "小%").get().toObjectList();
        Assert.assertEquals(5, entityList1.size());

        // select * from `student` where `name`="小"
        List<StudentModel.Entity> entityList2 = studentModel.newQuery()
            .whereMayLike("name", "小")
            .get()
            .toObjectList();
        Assert.assertEquals(0, entityList2.size());

        Map<String, Object> likeMap = new HashMap<>();
        likeMap.put("name", "%卡");
        Map<String, Object> likeMap2 = null;
        // select * from `student` where `name`like"%卡"
        List<StudentModel.Entity> entityList3 = studentModel.newQuery()
            .whereMayLike(likeMap)
            .whereMayLike(likeMap2)
            .get()
            .toObjectList();
        Assert.assertEquals(3, entityList3.size());


        StudentModel.Entity student = new StudentModel.Entity();
        student.setName("%卡");
        StudentModel.Entity student2 = null;
        // select * from `student` where `name`like"%卡"
        List<StudentModel.Entity> entityList4 = studentModel.newQuery()
            .whereMayLike(student)
            .whereMayLike(student2)
            .get()
            .toObjectList();
        Assert.assertEquals(3, entityList4.size());


        Map<String, Object> map = new HashMap<>();
        map.put("name", "%卡");
        map.put("age", null);
        // select * from `student` where `name`like"%卡" and `age`is null
        List<StudentModel.Entity> entityList5 = studentModel.newQuery()
            .whereMayLike(map)
            .get()
            .toObjectList();
        Assert.assertEquals(0, entityList5.size());

        Map<String, Object> map2 = new HashMap<>();
        map2.put("name", "%卡");
        map2.put("age", null);
        // select * from `student` where `name`like"%卡"
        List<StudentModel.Entity> entityList6 = studentModel.newQuery()
            .whereMayLikeIgnoreNull(map2)
            .get()
            .toObjectList();
        Assert.assertEquals(3, entityList6.size());
    }

    @Test
    public void 条件_havingMayLike() {
        // select * from `student` having `name`like"小%"
        List<StudentModel.Entity> entityList0 = studentModel.newQuery()
            .havingMayLike(StudentModel.Entity::getName, "小%")
            .get()
            .toObjectList();
        Assert.assertEquals(5, entityList0.size());

        // select * from `student` having `name`like"小%"
        List<StudentModel.Entity> entityList1 = studentModel.newQuery()
            .havingMayLike("name", "小%")
            .get()
            .toObjectList();
        Assert.assertEquals(5, entityList1.size());

        // select * from `student` having `name`="小"
        List<StudentModel.Entity> entityList2 = studentModel.newQuery()
            .havingMayLike("name", "小")
            .get()
            .toObjectList();
        Assert.assertEquals(0, entityList2.size());

        Map<String, Object> likeMap = new HashMap<>();
        likeMap.put("name", "%卡");
        Map<String, Object> likeMap2 = null;
        // select * from `student` having `name`like"%卡"
        List<StudentModel.Entity> entityList3 = studentModel.newQuery()
            .havingMayLike(likeMap)
            .havingMayLike(likeMap2)
            .get()
            .toObjectList();
        Assert.assertEquals(3, entityList3.size());


        StudentModel.Entity student = new StudentModel.Entity();
        student.setName("%卡");
        StudentModel.Entity student2 = null;
        // select * from `student` having `name`like"%卡"
        List<StudentModel.Entity> entityList4 = studentModel.newQuery()
            .havingMayLike(student)
            .havingMayLike(student2)
            .get()
            .toObjectList();
        Assert.assertEquals(3, entityList4.size());

        Map<String, Object> map = new HashMap<>();
        map.put("name", "%卡");
        map.put("age", null);
        // select * from `student` having `name`like"%卡" and `age`is null
        List<StudentModel.Entity> entityList5 = studentModel.newQuery()
            .havingMayLike(map)
            .get()
            .toObjectList();
        Assert.assertEquals(0, entityList5.size());

        Map<String, Object> map2 = new HashMap<>();
        map2.put("name", "%卡");
        map2.put("age", null);
        // select * from `student` having `name`like"%卡"
        List<StudentModel.Entity> entityList6 = studentModel.newQuery()
            .havingMayLikeIgnoreNull(map2)
            .get()
            .toObjectList();
        Assert.assertEquals(3, entityList6.size());
    }

    @Test
    public void 条件_whereIn_closure() {
        List<StudentModel.Entity> entityList0 = studentModel.newQuery().whereIn(StudentModel.Entity::getId,
            builder -> builder.select(StudentModel.Entity::getId).where(StudentModel.Entity::getAge, ">=", "11")
        ).andWhere(
            builder -> builder.whereNotIn(StudentModel.Entity::getSex,
                builder1 -> builder1.select(StudentModel.Entity::getSex).where(StudentModel.Entity::getSex, "1")
            )
        ).get().toObjectList();
        Assert.assertEquals(entityList0.size(), 3);
        System.out.println(entityList0);

        List<StudentModel.Entity> entityList1 = studentModel.newQuery().whereIn("id",
            builder -> builder.select("id").where("age", ">=", "11")
        ).andWhere(
            builder -> builder.whereNotIn("sex",
                builder1 -> builder1.select("sex").where("sex", "1")
            )
        ).get().toObjectList();
        Assert.assertEquals(entityList1.size(), 3);
        System.out.println(entityList1);
    }

    @Test
    public void 条件_whereNull() {
        List<StudentModel.Entity> entityList0 = studentModel.newQuery()
            .whereNotNull(StudentModel.Entity::getId)
            .get()
            .toObjectList();
        Assert.assertEquals(entityList0.size(), 10);

        List<StudentModel.Entity> entityList00 = studentModel.newQuery()
            .whereNull(StudentModel.Entity::getAge)
            .get()
            .toObjectList();
        Assert.assertEquals(entityList00.size(), 0);

        List<StudentModel.Entity> entityList1 = studentModel.newQuery().whereNotNull("id").get().toObjectList();
        Assert.assertEquals(entityList1.size(), 10);

        List<StudentModel.Entity> entityList2 = studentModel.newQuery().whereNull("age").get().toObjectList();
        Assert.assertEquals(entityList2.size(), 0);
    }

    @Test
    public void 条件_whereRaw() {
        RecordList<StudentModel.Entity, Integer> records = studentModel.newQuery().whereRaw((String) null).get();
        Assert.assertEquals(10, records.size());

        StudentModel.Entity entity = studentModel.newQuery().whereRaw("id=3").firstOrFail().toObject();
        Assert.assertEquals(3, entity.getId().intValue());

        StudentModel.Entity entity1 = studentModel.newQuery()
            .whereRaw("id>3")
            .whereRaw("id<5")
            .firstOrFail()
            .toObject();
        Assert.assertEquals(4, entity1.getId().intValue());

        StudentModel.Entity entity2 = studentModel.newQuery()
            .whereRaw("id> ?", Collections.singletonList(3))
            .whereRaw("id<?",
                Collections.singletonList(5))
            .firstOrFail()
            .toObject();
        Assert.assertEquals(4, entity2.getId().intValue());

        Set<String> sqlList = new HashSet<>();
        sqlList.add("id >5");
        sqlList.add("id<=8");
        sqlList.add(null);
        RecordList<StudentModel.Entity, Integer> records1 = studentModel.newQuery().whereRaw(sqlList).get();
        Assert.assertEquals(3, records1.count());
    }

    @Test
    public void 条件_havingRaw() {
        RecordList<StudentModel.Entity, Integer> records = studentModel.newQuery().havingRaw((String) null).get();
        Assert.assertEquals(10, records.size());

        StudentModel.Entity entity = studentModel.newQuery().havingRaw("id=3").firstOrFail().toObject();
        Assert.assertEquals(3, entity.getId().intValue());

        StudentModel.Entity entity1 = studentModel.newQuery()
            .havingRaw("id>3")
            .havingRaw("id<5")
            .firstOrFail()
            .toObject();
        Assert.assertEquals(4, entity1.getId().intValue());

        StudentModel.Entity entity2 = studentModel.newQuery()
            .havingRaw("id> ?", Collections.singletonList(3))
            .havingRaw("id< ?", Collections.singletonList(5))
            .firstOrFail()
            .toObject();
        Assert.assertEquals(4, entity2.getId().intValue());

        Set<String> sqlList = new HashSet<>();
        sqlList.add("id >5");
        sqlList.add("id<=8");
        sqlList.add(null);
        RecordList<StudentModel.Entity, Integer> records1 = studentModel.newQuery().havingRaw(sqlList).get();
        Assert.assertEquals(3, records1.count());
    }

    @Test
    public void 条件_whereRaw_havingRaw() {
        RecordList<StudentModel.Entity, Integer> records = studentModel.newQuery()
            .havingRaw("id>?", Arrays.asList(1))
            .whereRaw("name like ?", Arrays.asList("%腾"))
            .havingRaw("id < ?", Arrays.asList(9))
            .whereRaw("sex =?", Arrays.asList("1"))
            .get();
        Assert.assertEquals(1, records.size());
        Assert.assertEquals(3, records.toObjectList().get(0).getId().intValue());
    }

    @Test
    public void 条件_orWhere() {
        List<StudentModel.Entity> entityList0 = studentModel.newQuery().where(StudentModel.Entity::getId, "3").orWhere(
            (builder) -> builder.whereRaw("id=4")
        ).get().toObjectList();
        Assert.assertEquals(entityList0.size(), 2);

        List<StudentModel.Entity> entityList1 = studentModel.newQuery().where("id", "3").orWhere(
            (builder) -> builder.whereRaw("id=4")
        ).get().toObjectList();
        Assert.assertEquals(entityList1.size(), 2);

        List<StudentModel.Entity> entityList2 = studentModel.newQuery().where("id", "3").orWhere(
            (builder) -> builder.whereBetween("id", 4, "10").where("age", ">", "11")
        ).get().toObjectList();
        Assert.assertEquals(entityList2.size(), 6);
    }

    @Test
    public void 条件_andWhere() {
        List<StudentModel.Entity> entityList0 = studentModel.newQuery().where(StudentModel.Entity::getId, "3").andWhere(
            (builder) -> builder.whereRaw("id=4")
        ).get().toObjectList();
        Assert.assertEquals(entityList0.size(), 0);

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
    public void 条件_whereSubQuery_闭包() {
        List<Object> ins = new ArrayList<>();
        ins.add("1");
        ins.add("2");
        ins.add("3");
        RecordList<StudentModel.Entity, Integer> records0 = studentModel.newQuery()
            .where(StudentModel.Entity::getAge, "!=", "99")
            .whereSubQuery(StudentModel.Entity::getId, "in",
                builder -> builder.select(StudentModel.Entity::getId).whereIn(StudentModel.Entity::getId, ins))
            .get();
        Assert.assertEquals(records0.size(), 3);

        RecordList<StudentModel.Entity, Integer> records = studentModel.newQuery()
            .where("age", "!=", "99")
            .whereSubQuery("id", "in", builder -> builder.select("id").whereIn("id", ins))
            .get();
        Assert.assertEquals(records.size(), 3);
    }

    @Test
    public void 条件_whereSubQuery_字符串() {
        RecordList<StudentModel.Entity, Integer> records0 = studentModel.newQuery()
            .where(StudentModel.Entity::getAge, "!=", "99")
            .whereSubQuery(StudentModel.Entity::getId, "in", "select id from student where id = 3")
            .get();
        Assert.assertEquals(records0.size(), 1);

        RecordList<StudentModel.Entity, Integer> records = studentModel.newQuery()
            .where("age", "!=", "99")
            .whereSubQuery("id", "in", "select id from student where id = 3")
            .get();
        Assert.assertEquals(records.size(), 1);
    }

    @Test
    public void 条件_whereSubQuery_混合() {
        List<Object> ins = new ArrayList<>();
        ins.add("1");
        ins.add("2");
        ins.add("3");
        RecordList<StudentModel.Entity, Integer> records = studentModel.newQuery()
            .where("age", "!=", "99")
            .whereSubQuery("id", "in", builder -> builder.select("id").whereIn("id", ins).having("id", "!=", 2))
            .where("age", "!=", "99")
            .get();
        Assert.assertEquals(2, records.size());
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
        studentModel.newQuery()
            .where("sex", "1")
            .inRandomOrder(StudentModel.Entity::getId)
            .limit(5)
            .get()
            .toObjectList();
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
                builder -> builder.select(StudentModel.Entity::getId, StudentModel.Entity::getName,
                    StudentModel.Entity::getAge).whereBetween(StudentModel.Entity::getId, "2", "3")
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
    public void 排序_orderBy() {
        List<StudentModel.Entity> entities = studentModel.newQuery()
            .orderBy(StudentModel.Entity::getAge)
            .orderBy(StudentModel.Entity::getId, OrderBy.DESC)
            .get()
            .toObjectList();
        Assert.assertEquals(10, entities.size());
        Assert.assertEquals(6, entities.get(0).getAge().intValue());
        Assert.assertEquals(17, entities.get(9).getAge().intValue());
        Assert.assertEquals(7, entities.get(9).getId().intValue());

        List<StudentModel.Entity> entities1 = studentModel.newQuery()
            .orderBy(StudentModel.Entity::getId, OrderBy.DESC)
            .firstOrderBy(builder -> builder.orderBy(StudentModel.Entity::getAge))
            .get().toObjectList();
        Assert.assertEquals(10, entities1.size());
        Assert.assertEquals(6, entities1.get(0).getAge().intValue());
        Assert.assertEquals(17, entities1.get(9).getAge().intValue());
        Assert.assertEquals(7, entities1.get(9).getId().intValue());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void GROUP() {
        List<String> groupList = new ArrayList<>();
        groupList.add("id");
        groupList.add("age");
        List<StudentModel.Entity> entities0 = studentModel.newQuery()
            .select("id", "age")
            .where("id", "&", "1")
            .orderBy("id", OrderBy.DESC)
            .group(StudentModel.Entity::getSex, StudentModel.Entity::getId, StudentModel.Entity::getAge)
            .group(groupList)
            .get()
            .toObjectList();
        System.out.println(entities0);
        Assert.assertEquals(entities0.size(), 5);
        Assert.assertEquals(entities0.get(0).getId().intValue(), 9);
        Assert.assertEquals(entities0.get(1).getId().intValue(), 7);


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

        RecordList<StudentModel.Entity, Integer> records3 = studentModel.newQuery()
            .havingColumn(StudentModel.Entity::getAge, "<", StudentModel.Entity::getSex)
            .group("age", "sex")
            .select("age", "sex")
            .get();
        Assert.assertTrue(records3.isEmpty());
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
        // select `student`.*,`t`.`age` as `age2` from `student` inner join `student` as `t` on (`student`.`id`=`t`.`age`)
        RecordList<StudentModel.Entity, Integer> student_as_t = studentModel.newQuery()
            .select("student.*", "t.age as age2")
            .join("student as t", "student.id", "=", "t.age")
            .get();
        List<Map<String, Object>> maps = student_as_t.toMapList();
        Assert.assertEquals(maps.size(), 1);
        Assert.assertEquals(maps.get(0).get("id"), 6);
        Assert.assertEquals(maps.get(0).get("age2"), 6);
    }

    @Test
    public void join_manyJoin() {
        // select `student`.*,`t`.`age` as `age2` from `student` inner join `student` as `t` on (`student`.`id`=`t`.`age`)
        RecordList<StudentModel.Entity, Integer> student_as_t = studentModel.newQuery()
            .select("student.*", "t1.age as age1", "t2.age as age2")
            .join("student as t1", "student.id", "=", "t1.age")
            .join("student as t2", "student.id", "=", "t2.age")
            .get();
        List<Map<String, Object>> maps = student_as_t.toMapList();
        Assert.assertEquals(maps.size(), 1);
        Assert.assertEquals(maps.get(0).get("id"), 6);
        Assert.assertEquals(maps.get(0).get("age2"), 6);
    }

    @Test
    public void join_left() {
        // select `o`.* from `student` as `o` left join `student` as `s` on (`o`.`id`=`s`.`id`) order by `id` asc
        RecordList<StudentModel.Entity, Integer> records = studentModel.newQuery().select("o.*")
            .from("student as o")
            .join(JoinType.LEFT, "student as s", builder -> builder.whereColumn("o.id", "=", "s.id"))
            .orderBy("id").get();
        List<Map<String, Object>> maps = records.toMapList();
        Assert.assertEquals(maps.size(), 10);
    }

    @Test
    public void join_where() {
        // select `o`.* from `student` as `o` right join student as s on (`o`.`id`=`s`.`id` and `s`.`id`!="3" and `s`.`id`not in("4","5")) order by o.`id` asc
        RecordList<StudentModel.Entity, Integer> records = studentModel.newQuery().select("o.*")
            .from("student as o")
            .join(JoinType.RIGHT, "student as s", builder -> builder.whereColumn("o.id", "=", "s.id")
                .where("s.id", "!=", "3").whereNotIn("s.id", "4", "5"))
            .orderBy("o.id").get();
        List<Map<String, Object>> maps = records.toMapList();
        Assert.assertEquals(maps.size(), 10);
        Assert.assertNull(maps.get(0).get("id"));
        Assert.assertNull(maps.get(1).get("id"));
        Assert.assertNull(maps.get(2).get("id"));
        Assert.assertEquals(maps.get(3).get("id"), 1);
        Assert.assertEquals(maps.get(4).get("id"), 2);
        Assert.assertEquals(maps.get(5).get("id"), 6);
        Assert.assertEquals(maps.get(6).get("id"), 7);
        Assert.assertEquals(maps.get(7).get("id"), 8);
        Assert.assertEquals(maps.get(8).get("id"), 9);
        Assert.assertEquals(maps.get(9).get("id"), 10);
    }


    @Test
    public void join_subQuery() {
        // 找出age最大的男生女生的信息
        // select `student`.* from `student` inner join (select `sex`,max(age) as 'max_age' from `student` group by `sex`)t on (`student`.`sex`=`t`.`sex` and `student`.`age`=`t`.`max_age`);
        RecordList<StudentModel.Entity, Integer> records = studentModel.newQuery()
            .select("student.*")
            .join(JoinType.INNER,
                builder -> builder.select("sex").selectFunction("max", "age", "max_age").group("sex"),
                "t", builder -> builder.whereColumn("student.sex", "t.sex").whereColumn("student.age", "t.max_age"))
            .orderBy("id")
            .get();
        List<StudentModel.Entity> entities = records.toObjectList();
        Assert.assertEquals(entities.size(), 6);
        Assert.assertEquals(entities.get(0).getId().intValue(), 2);
        Assert.assertEquals(entities.get(1).getId().intValue(), 4);
        Assert.assertEquals(entities.get(2).getId().intValue(), 5);
        Assert.assertEquals(entities.get(3).getId().intValue(), 7);
        Assert.assertEquals(entities.get(4).getId().intValue(), 8);
        Assert.assertEquals(entities.get(5).getId().intValue(), 9);
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
        // 此处出现未处理的异常, 将不会关闭数据库连接, 表现是卡主
        // 使用闭包开启事物即可
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
            }, 3);
        });

        StudentModel.Entity entity = studentModel.newQuery().where("id", "1").firstOrFail().toObject();
        Assert.assertNotEquals(entity.getName(), "dddddd");
    }

    @Test
    public void 事物_lock_in_share_mode() {
        studentModel.newQuery().transaction(() -> {
            studentModel.newQuery().where("id", "3").sharedLock().get();
        }, 3);
    }

    @Test
    public void 事物_for_update() {
        studentModel.newQuery().transaction(() -> {
            studentModel.newQuery().where("id", "3").lockForUpdate().get();
        }, 3);
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
            studentModel.newQuery().orderBy("id").simplePaginate(1, 3);
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
        Assert.assertNull(paginate5.getLastPage());
        Assert.assertNull(paginate5.getTotal());

    }

    @Test
    public void 分页_快速分页_mapStyle() {
        Paginate<Map<String, Object>> paginate = studentModel.newQuery().orderBy("id").simplePaginateMapStyle(1, 3);
        System.out.println(paginate);
        Assert.assertEquals(paginate.getCurrentPage(), 1);
        Assert.assertNotNull(paginate.getFrom());
        Assert.assertNotNull(paginate.getTo());
        Assert.assertEquals(paginate.getFrom().intValue(), 1);
        Assert.assertEquals(paginate.getTo().intValue(), 3);
        Assert.assertNull(paginate.getLastPage());
        Assert.assertNull(paginate.getTotal());


        Paginate<Map<String, Object>> paginate2 = studentModel.newQuery()
            .where("sex", "1")
            .orWhere((builder -> builder.where("sex", "2")))
            .orderBy("id")
            .simplePaginateMapStyle(2, 3);
        System.out.println(paginate2);
        Assert.assertEquals(paginate2.getCurrentPage(), 2);
        Assert.assertNotNull(paginate2.getFrom());
        Assert.assertNotNull(paginate2.getTo());
        Assert.assertEquals(paginate2.getFrom().intValue(), 4);
        Assert.assertEquals(paginate2.getTo().intValue(), 6);
        Assert.assertNull(paginate2.getLastPage());
        Assert.assertNull(paginate2.getTotal());

        Paginate<Map<String, Object>> paginate3 = studentModel.newQuery()
            .where("sex", "1")
            .orWhere((builder -> builder.where("sex", "2")))
            .orderBy("id")
            .simplePaginateMapStyle(3, 3);
        System.out.println(paginate3);
        Assert.assertEquals(paginate3.getCurrentPage(), 3);
        Assert.assertNotNull(paginate3.getFrom());
        Assert.assertNotNull(paginate3.getTo());
        Assert.assertEquals(paginate3.getFrom().intValue(), 7);
        Assert.assertEquals(paginate3.getTo().intValue(), 9);
        Assert.assertNull(paginate3.getLastPage());
        Assert.assertNull(paginate3.getTotal());


        Paginate<Map<String, Object>> paginate4 = studentModel.newQuery()
            .orderBy("id")
            .where("sex", "1")
            .orWhere((builder -> builder.where("sex", "2")))
            .simplePaginateMapStyle(4, 3);
        System.out.println(paginate4);
        Assert.assertEquals(paginate4.getCurrentPage(), 4);
        Assert.assertNotNull(paginate4.getFrom());
        Assert.assertNotNull(paginate4.getTo());
        Assert.assertEquals(paginate4.getFrom().intValue(), 10);
        Assert.assertEquals(paginate4.getTo().intValue(), 10);
        Assert.assertNull(paginate4.getLastPage());
        Assert.assertNull(paginate4.getTotal());


        Paginate<Map<String, Object>> paginate5 = studentModel.newQuery()
            .where("sex", "1")
            .orWhere((builder -> builder.where("sex", "2")))
            .orderBy("id")
            .simplePaginateMapStyle(5, 3);
        System.out.println(paginate5);
        Assert.assertEquals(paginate5.getCurrentPage(), 5);
        Assert.assertNull(paginate5.getFrom());
        Assert.assertNull(paginate5.getTo());
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
        Assert.assertNotNull(paginate4.getLastPage());
        Assert.assertNotNull(paginate4.getTotal());
        Assert.assertEquals(paginate4.getLastPage().intValue(), 3);
        Assert.assertEquals(paginate4.getTotal().intValue(), 10);

        // 空数据时
        studentModel.newQuery().whereRaw("1").delete();
        Paginate<StudentModel.Entity> paginate5 = studentModel.newQuery()
            .orderBy("id")
            .where("sex", "1")
            .orWhere((builder -> builder.where("sex", "2")))
            .paginate(4, 4);
        Assert.assertEquals(paginate5.getCurrentPage(), 4);
        Assert.assertNull(paginate5.getFrom());
        Assert.assertNull(paginate5.getTo());
        Assert.assertNotNull(paginate5.getTotal());
        Assert.assertEquals(paginate5.getTotal().intValue(), 0);
    }

    @Test
    public void 分页_通用分页_mapStyle() {
        Paginate<Map<String, Object>> paginate =
            studentModel.newQuery().orderBy("id").paginateMapStyle(1,
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


        Paginate<Map<String, Object>> paginate2 =
            studentModel.newQuery()
                .andWhere((builder -> builder.where("sex", "1")))
                .orWhere((builder -> builder.where("sex", "2")))
                .orderBy("id")
                .paginateMapStyle(2, 4);
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


        // 防止过界
        Paginate<Map<String, Object>> paginate4 = studentModel.newQuery()
            .orderBy("id")
            .where("sex", "1")
            .orWhere((builder -> builder.where("sex", "2")))
            .paginateMapStyle(4, 4);
        System.out.println(paginate4);
        Assert.assertEquals(paginate4.getCurrentPage(), 4);
        Assert.assertNull(paginate4.getFrom());
        Assert.assertNull(paginate4.getTo());
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
        RecordList<StudentModel.Entity, Integer> records = studentModel.nativeQueryList(
            "select * from student where sex=?", e);
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

    @Test
    public void 原生_优化API() {
        Record<StudentModel.Entity, Integer> record = studentModel.newQuery()
            .query("select * from student where id=1");
        Assert.assertNotNull(record);
        Assert.assertEquals(record.toObject().getId().intValue(), 1);

        RecordList<StudentModel.Entity, Integer> records = studentModel.newQuery()
            .queryList("select * from student where sex= ? ", "2");
        Assert.assertEquals(records.size(), 4);
        Assert.assertEquals(records.get(0).toObject().getId().intValue(), 1);

        int execute = studentModel.newQuery()
            .execute("insert into `student`(`id`,`name`,`age`,`sex`) values( ? , ? , ? , ? )", "134", "testNAme", "11",
                "1");
        Assert.assertEquals(execute, 1);

        Record<StudentModel.Entity, Integer> query = studentModel.newQuery()
            .query("select * from student where sex=12");
        Assert.assertNull(query);

        Assert.assertThrows(EntityNotFoundException.class, () -> {
            studentModel.newQuery().queryOrFail("select * from student where sex=12");
        });
    }

    @Test
    public void 子查询_from() {
        // select count(*) as 'ccc' from (select `sex` from `student` group by `sex`)t
        RecordList<StudentModel.Entity, Integer> records = studentModel.newQuery()
            .selectFunction("count", "*", "ccc")
            .from("t",
                builder -> builder.group("sex").select("sex"))
            .get();

        Assert.assertEquals(records.size(), 1);
        Assert.assertEquals(records.toMapList().get(0).get("ccc").toString(), "2");

        // select count(*) as 'nvVeCH' from (select `sex` from `student` group by `sex`)nvVeCHsub limit 1
        Long count = studentModel.newQuery().group("sex").select("sex").count();
        Assert.assertEquals(count.intValue(), 2);
    }

    @Test
    public void 索引_forceAndIgnoreIndex() {
        RecordList<StudentModel.Entity, Integer> records1 = studentModel.newQuery()
            .whereRaw("1")
            .forceIndex("PRI")
            .get();
        Assert.assertEquals(records1.size(), 10);

        RecordList<StudentModel.Entity, Integer> records2 = studentModel.newQuery()
            .whereRaw("1")
            .ignoreIndex("PRI")
            .get();
        Assert.assertEquals(records2.size(), 10);

        RecordList<StudentModel.Entity, Integer> records3 = studentModel.newQuery()
            .whereRaw("1")
            .forceIndex("PRI")
            .ignoreIndex("PRI")
            .get();
        Assert.assertEquals(records3.size(), 10);
    }

    @Test
    public void 条件子句_when() {
        List<StudentModel.Entity> entities = studentModel.newQuery()
            .where("id", " >", 1)
            .when(true, builder -> builder.where("id", ">", 3))
            .where("id", " <", 7)
            .get()
            .toObjectList();
        Assert.assertEquals(3, entities.size());

        List<StudentModel.Entity> entities1 = studentModel.newQuery()
            .where("id", " >", 1)
            .when(false, builder -> builder.where("id", ">", 3))
            .where("id", " <", 7)
            .get()
            .toObjectList();
        Assert.assertEquals(5, entities1.size());

        List<StudentModel.Entity> entities2 = studentModel.newQuery()
            .where("id", " >", 1)
            .when(true, builder -> builder.where("id", ">", 3), builder -> builder.where("id", "<", 3))
            .where("id", " <", 7)
            .get()
            .toObjectList();
        Assert.assertEquals(3, entities2.size());

        List<StudentModel.Entity> entities3 = studentModel.newQuery()
            .where("id", " >", 1)
            .when(false, builder -> builder.where("id", ">", 3), builder -> builder.where("id", "<", 3))
            .where("id", " <", 7)
            .get()
            .toObjectList();
        Assert.assertEquals(1, entities3.size());
    }

}
