# database

Eloquent ORM for Java

## 目录

* [注册配置 Configuration](/document/bean.md)
* [数据映射 Mapping](/document/mapping.md)
* [数据模型 Model](/document/model.md)
* [查询结果集 Record](/document/record.md)
* [查询构造器 Query Builder](/document/query.md)
    * [表达式风格](#表达式风格)
    * [原生语句](#原生语句)
        * [原生查询](#原生查询)
        * [原生更新](#原生更新)
        * [原生新增](#原生新增)
    * [异步原生语句](#异步原生语句)
        * [原生异步查询](#原生异步查询)
        * [原生异步更新](#原生异步更新)
        * [原生异步新增](#原生异步新增)
    * [获取](#获取)
        * [分块处理](#分块处理)
    * [插入](#插入)
    * [更新](#更新)
    * [插入or更新](#插入or更新)
    * [删除](#删除)
        * [默认删除](#默认删除)
        * [强力删除](#强力删除)
    * [聚合统计函数](#聚合统计函数)
        * [无group](#无group)
        * [count](#count)
        * [max](#max)
        * [min](#min)
        * [avg](#avg)
        * [sum](#sum)
    * [自增或自减](#自增或自减)
        * [列在原值的基础上增加值](#列在原值的基础上增加值)
        * [位存储增加指定的选项](#位存储增加指定的选项)
    * [select](#select)
    * [when](#when)
    * [where](#where)
        * [字段与值的比较](#字段与值的比较)
        * [字段之间的比较](#字段之间的比较)
        * [字段在两值之间](#字段在两值之间)
        * [字段在范围内](#字段在范围内)
        * [字段为null](#字段为null)
        * [字段包含位](#字段包含位)
        * [子查询](#子查询)
        * [且](#且)
        * [或](#或)
        * [否定](#否定)
        * [条件为真](#条件为真)
    * [having](#having)
    * [order](#order)
    * [group](#group)
    * [join](#join)
    * [limit](#limit)
    * [from](#from)
    * [index](#index)
    * [data](#data)
    * [union](#union)
    * [事务](#事务)
        * [手动事物](#手动事物)
        * [闭包事务](#闭包事务)
        * [闭包异步事务](#闭包异步事务)
        * [共享锁与排他锁](#共享锁与排他锁)
    * [分页](#分页)
        * [快速分页](#快速分页)
        * [总数分页](#总数分页)
    * [功能](#功能)
        * [随机抽样](#随机抽样)
        * [构造器序列化](#构造器序列化)
        * [构造器传递](#构造器传递)
        * [类型指定](#类型指定)
        * [lastRaw](#lastRaw)
* [关联关系 Relationship](/document/relationship.md)
* [生成代码 Generate](/document/generate.md)
* [GraalVM](/document/graalvm.md)
* [版本信息 Version](/document/version.md)

## 总览

一下以示例的方式说明, 均来自源码中的单元测试

## 表达式风格

目前支持表达式风格的列名与属性名

```java
// select name,age from student where id in (1,2,3)
studentModel.newQuery().whereIn(Student::getId, 1,2,3).select(Student::getName).select(Student::getAge).get();
```

## 原生语句

- 语句中使用 ? 做占位符, 注意问号(?)前后应该分别保留1个半角空格, 以便SQL日志记录

### 原生查询

#### query queryList

```java
// 查询单条
Record<Student, Long> record = studentModel.newQuery()
    .query("select * from student where id=1", new ArrayList<>());
Record<Student, Long> record = studentModel.newQuery()
    .query("select * from student where id=1");

// 查询多条
List<String> parameters = new ArrayList<>();
parameters.add("2");
RecordList<Student, Long>, Long> records1 = studentModel.newQuery().queryList("select * from student where sex= ? ", parameters);

RecordList<Student, Long>, Long> records2 = studentModel.newQuery().queryList("select * from student where sex= ? ", "2");
```

### 原生更新

#### execute

```java
List<String> parameters = new ArrayList<>();
parameters.add("134");
parameters.add("testNAme");
parameters.add("11");
parameters.add("1");
int num1 = studentModel.newQuery()
    .execute("insert into `student`(`id`,`name`,`age`,`sex`) values( ? , ? , ? , ? )", parameters);
int num2 = studentModel.newQuery()
    .execute("insert into `student`(`id`,`name`,`age`,`sex`) values( ? , ? , ? , ? )", "134","testNAme","11","1");
```

### 原生新增

#### executeGetId executeGetIds

```java

List<String> parameters = new ArrayList<>();
parameters.add("134");
parameters.add("testNAme");
parameters.add("11");
parameters.add("1");

// 获取自增id 
Object id = studentModel.newQuery()
    .executeGetId("insert into `student`(`id`,`name`,`age`,`sex`) values( ? , ? , ? , ? )", parameters);

// 获取自增id列表
List<Object> ids = studentModel.newQuery()
    .executeGetIds("insert into `student`(`id`,`name`,`age`,`sex`) values( ? , ? , ? , ? )", "134","testNAme","11","1");
```

## 异步原生语句

- 异步执行执行SQL语句, 通过`gaarason.database.async-pool.*`配置异步线程池大小
- 如果当前线程已在事物中, 那么为了保证事务特性的准确, 将会自动降级为同步执行
- 如果想要异步执行事务, 需要使用"异步事务 `transactionAsync`"

### 原生异步查询

#### nativeQueryListAsync nativeQueryAsync nativeQueryOrFailAsync

```java
// 查询单条
CompletableFuture<Record<StudentModel.Entity, Integer>> future = studentModel.nativeQueryAsync(
    "select sleep(" + second + ")", null);

// 堵塞获取结果
future.get();
```

### 原生异步更新

#### nativeExecuteAsync

```java
// 执行
CompletableFuture<Integer> future = studentModel.nativeExecuteAsync(
    "update student set age=1 where id=1", null);

// 堵塞获取结果
future.get();
```


### 原生异步新增

#### nativeExecuteGetIdsAsync nativeExecuteGetIdAsync

```java
// 执行
CompletableFuture<Integer> future = studentModel.nativeExecuteGetIdAsync(
    "insert student name values (xiaoming)", null);

// 堵塞获取结果
future.get();
```

## 获取

#### first firstOrFail get

```java
// select name,id from student limit 1
Record<Student, Long> record = studentModel.newQuery().select("name").select("id").first();

// select * from student where name="小龙" limit 1
Record<Student, Long> record = studentModel.newQuery().where("name", "小龙").firstOrFail();

// select * from student where id=9 limit 1
Record<Student, Long> record = studentModel.findOrFail("9")

// select * from student where `age`<9
RecordList<Student, Long>> records = studentModel.where("age","<","9").get();
```

### 分块处理

当要进行大量数据查询时,可以使用分块, 在闭包中返回`boolean`表示是否进行下一次迭代  
因为分块查询, 并发的数据更改一定会伴随数据不准确的问题, 如同redis中的`keys`与`scan`

#### dealChunk

##### 使用limit分页

```java
// 使用 limit 分页
studentModel.where("age","<","9").dealChunk(2000, records -> {
    // do something
    records.toObjectList();
    return true;
});
```

##### 使用索引分页

```java
// 使用 索引 分页
studentModel.where("age","<","9").dealChunk(2000, Student::getId, records -> {
    // do something
    records.toObjectList();
    return true;
});

```

## 插入

#### value values insert insertGetId insertGetIdOrFail insertGetIds
- value 单行插入的数据, 参数可以是`值列表`, `值MAP`, 以及`实体`, 其中`值MAP`和`实体`均已经包含`column`调用
- values 多行插入的数据, 一般等价于多个`value`的调用
- insert 返回插入的行数
- insertGetId 返回插入后返回的主键id
- insertGetIdOrFail 返回插入后返回的主键id(失败则异常)
- insertGetIds 返回插入后返回的主键id列表

```java

// 推荐
// a. 实体赋值插入
Student student = new Student();
// student.setId(99); 数据库主键自增的话,可以省略
student.setName("姓名");
student.setAge(Byte.valueOf("13"));
student.setSex(Byte.valueOf("1"));
student.setTeacherId(0);
student.setCreatedAt(new Date(1312312312));
student.setUpdatedAt(new Date(1312312312));

// 返回受影响的行数
int num = studentModel.newQuery().value(entity).insert();

// 返回自增主键, 并对entity进行主键赋值
Long id = studentModel.newQuery().value(entity).insertGetIdOrFail();


// 推荐
// b. 多个实体操作
List<Student> studentList = new ArrayList<>();
for (int i = 99; i < 1000; i++) {
    Student student = new Student();
//    entity.setId(i);
    entity.setName("姓名");
    entity.setAge(Byte.valueOf("13"));
    entity.setSex(Byte.valueOf("1"));
    entity.setTeacherId(i * 3);
    entity.setCreatedAt(new Date());
    entity.setUpdatedAt(new Date());
    entityList.add(entity);
}

// 返回受影响的行数
int num = studentModel.newQuery().values(entityList).insert();

// 返回自增主键列表
List<Long> ids = studentModel.newQuery().value(entity).insertGetIds();

// c. 构造语句插入
 List<String> columnNameList = new ArrayList<>();
columnNameList.add("name");
columnNameList.add("age");
columnNameList.add("sex");
List<String> valueList = new ArrayList<>();
valueList.add("testNAme134");
valueList.add("11");
valueList.add("1");

int num = studentModel.newQuery().column(columnNameList).value(valueList).insert();

// d. map赋值插入
Map<String, Object> map = new HashMap<>();
map.put("id", 99);
map.put("name", "姓名");
map.put("age", 13);
map.put("sex", 1);
map.put("teacher_id", 0);
map.put("created_at", LocalDateUtils.SIMPLE_DATE_FORMAT.get().format(new Date(1312312312)));
map.put("updated_at", LocalDateUtils.SIMPLE_DATE_FORMAT.get().format(new Date(1312312312)));

int insert = studentModel.newQuery().value(map).insert();

// e.多个list插入
studentModel.newQuery()
        .column("id", "name")
        .values(Arrays.asList(Arrays.asList(19, "xxcc1"), Arrays.asList(199, "xxcc2")))
        .insert();

// f.多个map操作
List<Map<String, Object>> maps = new ArrayList<>();
for (int i = 99; i < 10000; i++) {
    Map<String, Object> map = new HashMap<>();
    map.put("name", "姓名");
    map.put("age", 13);
    map.put("sex", 1);
    map.put("teacher_id", i * 3);
    map.put("created_at", LocalDateUtils.SIMPLE_DATE_FORMAT.get().format(new Date()));
    map.put("updated_at", LocalDateUtils.SIMPLE_DATE_FORMAT.get().format(new Date()));
    maps.add(map);
}
int insert = studentModel.newQuery().values(maps).insert();

// g.查询后插入
// insert into `student`(`name`)(select `name` from `student` as `student_770379332` where `sex`="1" limit "3")
studentModel.newQuery()
    .column(StudentModel.Entity::getName)
    .values(builder -> builder.select(StudentModel.Entity::getName)
            .where(StudentModel.Entity::getSex, 1)
            .limit(3))
    .insert();
```

## 更新

#### update

- update 返回更新的行数

当一个更新语句没有`where`时,将会抛出`ConfirmOperationException`

```java
int num = studentModel.newQuery().data("name", "xxcc").where("id", "3").update();

int num = studentModel.newQuery().data("name", "vvv").where("id", ">", "3").update();

// 抛出`ConfirmOperationException`
studentModel.newQuery().data("name", "xxcc").update();

studentModel.newQuery().data("name", "xxcc").whereRaw(1).update();

// 使用map
Map<String, Object> map = new HashMap<>();
map.put("name", "gggg");
map.put("age", "7");

int update = studentModel.newQuery().data(map).where("id", "3").update();

int update = studentModel.newQuery().data(new Student()).where("id", "3").update();

```

## 插入or更新

单个原子操作中更新或创建记录

#### replace upsert

- `replace` 对于`mysql`而言为 :  **无冲突则插入, 有冲突就先删除再插入 (其余列使用默认值)**
- `upsert` 对于`mysql`而言为 : **无冲突则插入, 有冲突就更新指定列 (其余列不变)**
- 均触发`插入类型`的事件 (**并非`更新`事件**)

```java
// 一次一行
// sql 风格
// replace into `student`(`id`,`name`) values ("3","xxcc")
studentModel.newQuery().column("id", "name").value(Arrays.asList(3, "xxcc")).replace();

// map风格
Map<String, Object> map = new HashMap<>();
map.put("id", 99);
map.put("name", "姓名");
map.put("age", 13);
map.put("sex", 1);
map.put("teacher_id", 0);
map.put("created_at", LocalDateUtils.SIMPLE_DATE_FORMAT.get().format(new Date(1312312312)));
map.put("updated_at", LocalDateUtils.SIMPLE_DATE_FORMAT.get().format(new Date(1312312312)));
studentModel.newQuery().value(map).replace();

// 实体风格
studentModel.newQuery().value(new Student()).replace();

// 一次多行
// sql 风格
// replace into `student`(`id`,`name`) values ("19","xxcc1"),("199","xxcc2")
studentModel.newQuery().column("id", "name")
.values(Arrays.asList(Arrays.asList(19, "xxcc1"), Arrays.asList(199, "xxcc2")))
.replace();

// map风格
List<Map<String, Object>> maps = new ArrayList<>();
for (int i = 99; i < 10000; i++) {
Map<String, Object> map = new HashMap<>();
    map.put("name", "姓名");
    map.put("age", 13);
    map.put("sex", 1);
    map.put("teacher_id", i * 3);
    map.put("created_at", LocalDateUtils.SIMPLE_DATE_FORMAT.get().format(new Date()));
    map.put("updated_at", LocalDateUtils.SIMPLE_DATE_FORMAT.get().format(new Date()));
    maps.add(map);
}
studentModel.newQuery().values(maps).replace();

// 实体风格
studentModel.newQuery().values(Array.asList(new Student(), new Student())).replace();



// 一次一行
// sql 风格
// insert into `student`(`id`,`name`) values ("3","xxcc") ON DUPLICATE KEY UPDATE `name`=VALUES(`name`)
studentModel.newQuery().column("id", "name").value(Arrays.asList(3, "xxcc")).upsert(StudentModel.Entity::getName);

// 一次多行
// sql 风格
// insert into `student`(`id`,`name`) values ("19","xxcc1"),("199","xxcc2") ON DUPLICATE KEY UPDATE `name`=VALUES(`name`)
istudentModel.newQuery().column("id", "name")
.values(Arrays.asList(Arrays.asList(19, "xxcc1"), Arrays.asList(199, "xxcc2")))
.upsert("name");

```


## 删除

当前model如果非软删除, 则`默认删除`与`强力删除`效果一致  
`软删除`定义以及启用,请看[数据模型 Model](/document/model.md)  
当一个删除语句没有`where`时,将会抛出`ConfirmOperationException`

### 默认删除

#### delete

```java
int num = studentModel.newQuery().where("id", "3").delete();

// 抛出`ConfirmOperationException`
studentModel.newQuery().delete();

studentModel.newQuery().whereRaw(1).delete();
```

### 强力删除

#### forceDelete

```java
int num = studentModel.newQuery().where("id", "3").forceDelete();

// 抛出`ConfirmOperationException`
studentModel.newQuery().forceDelete();

studentModel.newQuery().whereRaw(1).forceDelete();
```

## 聚合统计函数

count/max/min/avg/sum 使用是需要注意下各个方法返回的数据类型

### 无group

```java
// select count(id) as 'eUTIdN' from `student` where `sex`="1" limit 1
// 统计时会忽略 select 中指定的 name
Long count00 = studentModel.newQuery().select("name").where("sex", "1").count("id");
Assert.assertEquals(count00.intValue(), 6);
    
// select count(id) as 'eUTIdN' from `student` where `sex`="1" limit 1
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
```

### count

select 中的字段应该确保已经出现在 group 中

```java
// 以下为手动
// select count(*) as 'ccc' from (select `sex` from `student` group by `sex`)t
RecordList<StudentModel.Entity, Integer> records = studentModel.newQuery().selectFunction("count", "*", "ccc").from("t",
    builder -> builder.group("sex").select("sex")).get();
Assert.assertEquals(records.size(), 1);
Assert.assertEquals(records.toMapList().get(0).get("ccc").toString(), "2");

// select count(*) as 'ccc' from (select `sex` from `student` group by `sex`)t limit 1
Record<StudentModel.Entity, Integer> record = studentModel.newQuery().selectFunction("count", "*", "ccc").from("t",
    builder -> builder.group("sex").select("sex")).firstOrFail();
Assert.assertEquals(record.toMap().get("ccc").toString(), "2");

// 以下为自动
// select count(sex) as 'qQhLPU' from (select `sex` from `student` group by `sex`)qQhLPUsub limit 1
Long count01 = studentModel.newQuery().group("sex").count("sex");
Assert.assertEquals(count01.longValue(), 2);

// select count(*) as 'GtMbMe' from (select `sex`,`age`,`name` from `student` group by `sex`,`age`,`name`)GtMbMesub limit 1
Long count02 = studentModel.newQuery().group("sex").group("age","name").count("*");
Assert.assertEquals(count02.longValue(), 10);

// select count(*) as 'oLmXhJ' from (select `sex` from `student` group by `sex`)oLmXhJsub limit 1
Long count03 = studentModel.newQuery().group("sex").count();
Assert.assertEquals(count03.longValue(), 2);

// select count(*) as 'HXXFaq' from (select `sex` from `student` group by `sex`)HXXFaqsub limit 1
Long count04 = studentModel.newQuery().group("sex").select("sex").count("*");
Assert.assertEquals(count04.longValue(), 2);
```

### max

```java
// 以下为手动
// select max(sex) as 'ccc' from (select `sex` from `student` group by `sex`)t
RecordList<StudentModel.Entity, Integer> records = studentModel.newQuery().selectFunction("max", "sex", "ccc").from("t",
    builder -> builder.group("sex").select("sex")).get();
Assert.assertEquals(records.size(), 1);
Assert.assertEquals(records.toMapList().get(0).get("ccc").toString(), "2");

// select max(sex) as 'ccc' from (select `sex` from `student` group by `sex`)t limit 1
Record<StudentModel.Entity, Integer> record = studentModel.newQuery().selectFunction("max", "sex", "ccc").from("t",
    builder -> builder.group("sex").select("sex")).firstOrFail();
Assert.assertEquals(record.toMap().get("ccc").toString(), "2");

// 以下为自动
// select max(sex) as 'MlXcWL' from (select `sex` from `student` group by `sex`)MlXcWLsub limit 1
String max1 = studentModel.newQuery().group("sex").max("sex");
Assert.assertEquals(max1, "2");

// select max(sex) as 'ZldfCz' from (select `sex`,`age`,`name` from `student` group by `sex`,`age`,`name`)ZldfCzsub limit 1
String count02 = studentModel.newQuery().group("sex").group("age","name").max("sex");
Assert.assertEquals(count02, "2");

// select max(sex) as 'uOhnwy' from (select `sex` from `student` group by `sex`)uOhnwysub limit 1
String count03 = studentModel.newQuery().group("sex").max("sex");
Assert.assertEquals(count03, "2");

// select max(sex) as 'thbZAz' from (select `sex` from `student` group by `sex`)thbZAzsub limit 1
String count04 = studentModel.newQuery().group("sex").select("sex").max("sex");
Assert.assertEquals(count04, "2");
```

### min

```java
// 以下为手动
// select min(sex) as 'ccc' from (select `sex` from `student` group by `sex`)t limit 1
Record<StudentModel.Entity, Integer> record = studentModel.newQuery().selectFunction("min", "sex", "ccc").from("t",
    builder -> builder.group("sex").select("sex")).firstOrFail();
Assert.assertEquals(record.toMap().get("ccc").toString(), "1");

// 以下为自动
// select min(sex) as 'NZpuZx' from (select `sex` from `student` group by `sex`)NZpuZxsub limit 1
String min1 = studentModel.newQuery().group("sex").min("sex");
Assert.assertEquals(min1, "1");

// select min(sex) as 'YAhmzr' from (select `sex`,`age`,`name` from `student` group by `sex`,`age`,`name`)YAhmzrsub limit 1
String min2 = studentModel.newQuery().group("sex").group("age","name").min("sex");
Assert.assertEquals(min2, "1");

// select min(sex) as 'RntldM' from (select `sex` from `student` group by `sex`)RntldMsub limit 1
String min3 = studentModel.newQuery().group("sex").min("sex");
Assert.assertEquals(min3, "1");

// select min(sex) as 'oUnMLS' from (select `sex` from `student` group by `sex`)oUnMLSsub limit 1
String min4 = studentModel.newQuery().group("sex").select("sex").min("sex");
Assert.assertEquals(min4, "1");
```

### avg

```java
// 以下为手动
// select avg(sex) as 'ccc' from (select `sex` from `student` group by `sex`)t limit 1
Record<StudentModel.Entity, Integer> record = studentModel.newQuery().selectFunction("avg", "sex", "ccc").from("t",
    builder -> builder.group("sex").select("sex")).firstOrFail();
Assert.assertEquals(record.toMap().get("ccc").toString(), "1.5000");

// 以下为自动
// select avg(sex) as 'IImErp' from (select `sex` from `student` group by `sex`)IImErpsub limit 1
BigDecimal res1 = studentModel.newQuery().group("sex").avg("sex");
Assert.assertEquals(res1.toString(), "1.5000");

// select avg(sex) as 'JuDitC' from (select `sex`,`age`,`name` from `student` group by `sex`,`age`,`name`)JuDitCsub limit 1
BigDecimal res2 = studentModel.newQuery().group("sex").group("age","name").avg("sex");
Assert.assertEquals(res2.toString(), "1.4000");

// select avg(sex) as 'LRxkwD' from (select `sex` from `student` group by `sex`)LRxkwDsub limit 1
BigDecimal res3 = studentModel.newQuery().group("sex").avg("sex");
Assert.assertEquals(res3.toString(), "1.5000");

// select avg(sex) as 'tcRKqt' from (select `sex` from `student` group by `sex`)tcRKqtsub limit 1
BigDecimal res4 = studentModel.newQuery().group("sex").select("sex").avg("sex");
Assert.assertEquals(res4.toString(), "1.5000");
```

### sum

```java
// 以下为手动
// select sum(sex) as 'ccc' from (select `sex` from `student` group by `sex`)t limit 1
Record<StudentModel.Entity, Integer> record = studentModel.newQuery().selectFunction("sum", "sex", "ccc").from("t",
    builder -> builder.group("sex").select("sex")).firstOrFail();
Assert.assertEquals(record.toMap().get("ccc").toString(), "3");

// 以下为自动
// select sum(sex) as 'DLfORT' from (select `sex` from `student` group by `sex`)DLfORTsub limit 1
BigDecimal min1 = studentModel.newQuery().group("sex").sum("sex");
Assert.assertEquals(min1.toString(), "3");

// select sum(sex) as 'yMpOUV' from (select `sex`,`age`,`name` from `student` group by `sex`,`age`,`name`)yMpOUVsub limit 1
BigDecimal min2 = studentModel.newQuery().group("sex").group("age","name").sum("sex");
Assert.assertEquals(min2.toString(), "14");

// select sum(sex) as 'MxNqTs' from (select `sex` from `student` group by `sex`)MxNqTssub limit 1
BigDecimal min3 = studentModel.newQuery().group("sex").sum("sex");
Assert.assertEquals(min3.toString(), "3");

// select sum(sex) as 'aVtVwE' from (select `sex` from `student` group by `sex`)aVtVwEsub limit 1
BigDecimal min4 = studentModel.newQuery().group("sex").select("sex").sum("sex");
Assert.assertEquals(min4.toString(), "3");
```

## 自增或自减
### 列在原值的基础上增加值
#### dataIncrement dataDecrement  
列在原值的基础上增加(减少)值
```java
// update `student` set`age`= `age`+4  where id=4 
int update2 = studentModel.newQuery().dataIncrement("age", 4).whereRaw("id=4").update();

// update `student` set`age`= `age`-2  where id=4 
int update = studentModel.newQuery().dataDecrement("age", 2).whereRaw("id=4").update();
```
### 位存储增加指定的选项
#### dataBitIncrement dataBitDecrement 
列(位存储)增加(移除)指定的选项  
重复执行时, 幂等
```java
// 目标选项集合
ArrayList<Object> objects = new ArrayList<>();
objects.add(4);
objects.add(5);

// update student set `sex`=`sex`| "48"  where id=4
int update = studentModel.newQuery().dataBitIncrement(StudentModel.Entity::getSex, objects).whereRaw("id=4").update();

// update student set `sex`=`sex`& ~ "48"  where id=4
int update2 = studentModel.newQuery().dataBitDecrement(StudentModel.Entity::getSex, objects).whereRaw("id=4").update();
```

## select

确定查询时返回的列

```java
// select name,id,id from student limit 1;
studentModel.newQuery().select("name").select("id").select("id").first();

// select name,id,created_at from student limit 1;
studentModel.newQuery().select("name","id","created_at").first();

// select concat_ws(name, id) as newkey from student limit 1;
studentModel.newQuery().selectFunction("concat_ws", "\"-\",`name`,`id`", "newKey").first();
```

## when

有时候你可能想要某些条件为 true 的时候才将条件子句应用到查询。例如，你可能只想给定值在请求中存在的情况下才应用 where 语句，这可以通过 when 方法实现

```java
// select * from student where id > 3
studentModel.newQuery().when(true, builder -> builder.where("id", ">", 3)).get()

// select * from student
studentModel.newQuery().when(false, builder -> builder.where("id", ">", 3)).get()


// select * from student where id > 3
studentModel.newQuery().when(true, builder -> builder.where("id", ">", 3), builder -> builder.where("id", "<", 3)).get()

// select * from student where id < 3
studentModel.newQuery().when(false, builder -> builder.where("id", ">", 3), builder -> builder.where("id", "<", 3)).get()
```

## where

### 字段与值的比较

#### where

比较列与值   
值为null时, 会使用 is null 语句

```java
// select * from student where name id < 2 limit 1
Record<Student, Long> record = studentModel.newQuery().whereRaw("id<2").first();

// select * from student where name id > 2 limit 1
Record<Student, Long> record = studentModel.newQuery().where("id", ">", "2").first();

// select * from student where name id != 2 limit 1
Record<Student, Long> record = studentModel.newQuery().where("id", "!=", "2").first();

// select * from student where name id = 2 limit 1
Record<Student, Long> record = studentModel.newQuery().where("id", "2").first();

// select * from student where name like "%明%" limit 1
Record<Student, Long> record = studentModel.newQuery().where("name", "like", "%明%").first();

// select * from student where id is null limit 1
Record<Student, Long> record = studentModel.newQuery().where("id", null).first();
```

#### whereLike

* "列like值" 的查询条件
* 忽略 value 为 null 、"" 、% 、%% 的情况
* 其中值如果没有在开头或结尾自行包含 % 符号，则在开头以及结尾拼接 % 符号

```java
// select * from `student` where `name`like"小%"
studentModel.newQuery().whereLike("name", "小%").get();

// select * from `student` where `name`like"%小%"
studentModel.newQuery().whereLike("name", "小").get();

// select * from `student` where `name`like"%卡"
Map<String, Object> likeMap = new HashMap<>();
likeMap.put("name", "%卡");

entityList3 = studentModel.newQuery().whereLike(likeMap).get();

// select * from `student` where `name`like"%卡"
StudentModel.Entity student = new StudentModel.Entity();
student.setName("%卡");

studentModel.newQuery().whereLike(student).get();
```
#### whereAnyLike

* 在多个列中, 查找值, 任一满足
* 忽略 value 为 null 、"" 、% 、%% 的情况
* 其中值如果没有在开头或结尾自行包含 % 符号，则在开头以及结尾拼接 % 符号
* sql eg : where ( column1 like %value% or column2 like %value% )

```java
// select * from `student`
studentModel.newQuery().whereAny(null, "name", "age", "id").get()

// select * from `student` where ((`name`like"%1") or (`age`like"%1") or (`id`like"%1")) and ((`name`like"%张") or (`age`like"%张") or (`id`like"%张"))
studentModel.newQuery().whereAny("%1", "name", "age", "id").whereKeywords("%张", "name", "age", "id").get()

```
#### whereAllLike

* 在多个列中, 查找值, 全部满足
* 忽略 value 为 null 、"" 、% 、%% 的情况
* 其中值如果没有在开头或结尾自行包含 % 符号，则在开头以及结尾拼接 % 符号
* sql eg : where ( column1 like %value% and column2 like %value% )

```java
// select * from `student`
studentModel.newQuery().whereAny(null, "name", "age", "id").get()

// select * from `student` where ((`name`like"%1") and (`age`like"%1") and (`id`like"%1"))
studentModel.newQuery().whereAllLike("%1", "name", "age", "id").get()

```

#### whereMayLike  whereMayLikeIgnoreNull

* 选择可能的条件类型
* 忽略 value 为 % 、%% 的情况
* 当 value 以 %开头或者结尾时, 使用like查询
* 当 value 为 null 时, 使用 is null 查询
* 其他情况下, 使用 = 查询

```java
// select * from `student` where `name`like"小%"
studentModel.newQuery().whereMayLike("name", "小%").get();

// select * from `student` where `name` is null
studentModel.newQuery().whereMayLike("name", null).get();

// select * from `student` where `name`="小"
studentModel.newQuery().whereMayLike("name", "小").get();


// select * from `student` where `name`like"%卡" and `des`="卡"
Map<String, Object> likeMap = new HashMap<>();
likeMap.put("name", "%卡");
likeMap.put("des", "卡");

entityList3 = studentModel.newQuery().whereMayLike(likeMap).get();

// select * from `student` where `name`like"%卡" and `des`="卡"
StudentModel.Entity student = new StudentModel.Entity();
student.setName("%卡");
student.setDes("卡");

studentModel.newQuery().whereMayLike(student).get();
```


#### whereIgnoreNull

会忽略为`null`的值

```java
// select * from student where id = 123 limit 1
Record<Student, Long> record = studentModel.newQuery().whereIgnoreNull("id", 123).whereIgnoreNull("name", null).first();


map.put("id", 123);
map.put("name", null);
// select * from student where id = 123 limit 1
Record<Student, Long> record = studentModel.newQuery().whereIgnoreNull(map).first();
```

### 字段之间的比较

#### whereColumn

```java
// select * from student where `id` > `sex` limit 1
Record<Student, Long> record = studentModel.newQuery().whereColumn("id", ">", "sex").first();
```

### 字段在两值之间

#### whereBetween whereNotBetween

```java
// select * from student where `id` between "3" and "5" 
RecordList<Student, Long>> records = studentModel.newQuery().whereBetween("id", "3", "5").get();

// select * from student where `id` not between "3" and "5" 
RecordList<Student, Long>> records = studentModel.newQuery().whereNotBetween("id", "3", "5").get();
```

### 字段在范围内

#### whereIn whereNotIn

```java
List<Object> idList = new ArrayList<>();
idList.add("4");
idList.add("5");
idList.add("6");
idList.add("7");

// select * from student where `id`in( "4" , "5" , "6" , "7" )
RecordList<Student, Long> records = studentModel.newQuery().whereIn("id", idList).get();

// select * from student where `id`not in( "4" , "5" , "6" , "7" )
RecordList<Student, Long> records = studentModel.newQuery().whereNotIn("id", idList).get();

// select * from student where `id` in( select id from student where age>=11) and ( sex not in (select sex from student where sex=1) )
RecordList<Student, Long> records = studentModel.newQuery().whereIn("id",
    builder -> builder.select("id").where("age", ">=", "11")
).andWhere(
    builder -> builder.whereNotIn("sex",
        builder1 -> builder1.select("sex").where("sex", "1")
    )
).get()
```

#### whereInIgnoreEmpty whereNotInIgnoreEmpty

和 whereIn whereNotIn 相比较，当参数为空时，会忽略。不会忽略列表中的`null`

```java
List<Object> idList = new ArrayList<>();
// select * from student  limit 1
RecordList<Student, Long>> records = studentModel.newQuery().whereInIgnoreEmpty("id", idList).get();

// select * from student  limit 1
RecordList<Student, Long>> records = studentModel.newQuery().whereNotInIgnoreEmpty("id", idList).get();

List<Object> idListHasNull = new ArrayList<>();
idListHasNull.add(null)
// select * from student id in (null) limit 1
RecordList<Student, Long>> records = studentModel.newQuery().whereNotInIgnoreEmpty("id", idListHasNull).get();
```

### 字段为null

#### whereNull  whereNotNull

```java
// select * from student where id is null;
studentModel.newQuery().whereNull("id").get();

// select * from student where id is not null;
studentModel.newQuery().whereNotNull("id").get();
```

### 字段包含位

对于多选的业务场景, 使用数字类型按位存贮, 有着极高的空间利用效率  
例如, 业务中需要保存用户的爱好, 爱好多选项为 : 0-听歌,1-旅游,2-观音,3-垂钓,4-游戏.  
那么在数据库中定义一个tinyint列, 初始值0, 对应2进制为00000000  
当用户的爱好是 1-旅游,2-观音,3-垂钓时, 更改2进制对应的位值(右到左), 即00001110, 对应的十进制为14  
   
这边提供了高效, 且友好的使用方式

#### whereBit whereBitNot
列包(不)含选项值
```java
// 查询有爱好6的学生
// select * from student where (`hobby`&64)>0
studentModel.newQuery().whereBit(StudentModel.Entity::getHobby, 6).get();

// 查询没有爱好6的学生
// select * from student where (`hobby`&64)=0
studentModel.newQuery().whereBitNot(StudentModel.Entity::getHobby, 6).get();
```

#### whereBitIn whereBitNotIn
列包(不)含选项值其一

```java
List<Object> list = new ArrayList<>();
list.add(5);
list.add(6);
// 查询有爱好5或者爱好6的学生
// select * from student where (((`hobby`&32)>0) or ((`hobby`&64)>0))
studentModel.newQuery().whereBitIn(StudentModel.Entity::getHobby, list).get();

// 查询没有爱好5或者爱好6的学生
// select * from student where (((`hobby`&32)=0) or ((`hobby`&64)=0))
studentModel.newQuery().whereBitNotIn(StudentModel.Entity::getHobby, list).get();
```

#### whereBitStrictIn whereBitStrictNotIn
列包完全(不)含所有选项值

```java
List<Object> list = new ArrayList<>();
list.add(5);
list.add(6);
// 查询有爱好5和爱好6的学生
// select * from student where (((`hobby`&32)>0) and ((`hobby`&64)>0))
studentModel.newQuery().whereBitStrictIn(StudentModel.Entity::getHobby, list).get();

// 查询没有爱好5和爱好6的学生
// select * from student where (((`hobby`&32)=0) and ((`hobby`&64)=0))
studentModel.newQuery().whereBitStrictNotIn(StudentModel.Entity::getHobby, list).get();
```

### 子查询

#### whereSubQuery

```java
List<Object> ins = new ArrayList<>();
ins.add("1");
ins.add("2");
ins.add("3");

// select * from student where age != 99 and id in (select id from student where id in 1,2,3)
RecordList<Student, Long>> records = studentModel.newQuery()
.where("age", "!=", "99")
.whereSubQuery("id", "in", builder -> builder.select("id").whereIn("id", ins))
.get();

// select * from student where age != 99 and id in (select id from student where id = 3)
RecordList<Student, Long>> records = studentModel.newQuery()
.where("age", "!=", "99")
.whereSubQuery("id", "in", "select id from student where id = 3")
.get();
```

### 且

#### andWhere  andWhereIgnoreEmpty

```java
// select * from student where id = 3 and (id=4)
studentModel.newQuery().where("id", "3").andWhere(
    builder -> builder.whereRaw("id=4")
).get();
```

### 或

#### orWhere  orWhereIgnoreEmpty

```java
// select * from student where id = 3 or (id=4)
RecordList<Student, Long>> records = studentModel.newQuery().where("id", "3").orWhere(
    (builder) -> builder.whereRaw("id=4")
).get();
```
### 否定

#### whereNot
否定一组给定的查询约束
```java
// select * from student where `id`= "3"  and (!(id<>4))
studentModel.newQuery().where(StudentModel.Entity::getId, "3").andWhere(
        builder -> builder.whereNot(
                b -> b.whereRaw("id<>4")
        )
).get()
```

### 条件为真

#### whereExists  whereNotExists

```java
// select `id`,`name`,`age` from student group by `id`,`name`,`age` having `id`between "1" and "2"  
// and exists (select `id`,`name`,`age` from student where `id`between "2" and "3" ) 
// and not exists (select `id`,`name`,`age` from student where `id`between "2" and "4" )
RecordList<Student, Long>> records = studentModel.newQuery()
.select("id", "name", "age")
.whereBetween("id", "1", "2")
.whereExists(
    builder -> builder.select("id", "name", "age").whereBetween("id", "2", "3")
)
.whereNotExists(
    builder -> builder.select("id", "name", "age").whereBetween("id", "2", "4")
)
.get();
```

## having

类似于where, 暂略

## order

```java
// select * from student order by id desc
studentModel.newQuery().orderBy("id", OrderBy.DESC).get();

// select * from student order by id desc, name asc
studentModel.newQuery().orderBy("id", OrderBy.DESC).orderBy("name", OrderBy.ASC).get();

// 使用firstOrderBy将排序片段增加到首位
// select * from student order by name asc, id desc
studentModel.newQuery().orderBy("id", OrderBy.DESC).firstOrderBy(builder -> builder.orderBy("name", OrderBy.ASC)).get();
```

## group

因为在[注册bean](/document/bean.md)时默认设置了`SESSION SQL_MODE`, 所以`gourp`的结果类似`Oracle`

```java
RecordList<Student, Long>> records = studentModel.newQuery()
.select("id", "age")
.where("id", "&", "1")
.orderBy("id", OrderBy.DESC)
.group("sex", "id", "age")
.get();
```

## join

因为在`select`中使用的别名, 所以在使用`toObject`时无法正确匹配实例属性,因此建议使用`toMap`

```java
// select `student`.*,`t`.`age` as `age2` from `student` inner join `student` as `t` on (`student`.`id`=`t`.`age`)
RecordList<StudentModel.Entity, Integer> student_as_t = studentModel.newQuery()
    .select("*").selectRaw("t.age as age2")
    .join("student as t", "id", "=", "t.age")
    .get();
    List<Map<String, Object>> maps = student_as_t.toMapList();

// select `student`.*,`t`.`age` as `age2` from `student` inner join `student` as `t` on (`student`.`id`=`t`.`age`)
RecordList<StudentModel.Entity, Integer> student_as_t = studentModel.newQuery()
    .select("*")
    .selectRaw( "t1.age as age1,t2.age as age2")
    .join("student as t1", "id", "=", "t1.age")
    .join("student as t2", "id", "=", "t2.age")
    .get();
List<Map<String, Object>> maps = student_as_t.toMapList();

// select `o`.* from `student` as `o` left join `student` as `s` on (`o`.`id`=`s`.`id`) order by `id` asc
RecordList<StudentModel.Entity, Integer> records = studentModel.newQuery().select("*")
    .from("student")
    .join(JoinType.LEFT, "student as s", builder -> builder.whereRaw(builder.columnAlias("id") + "=s.id"))
    .orderBy("id").get();
List<Map<String, Object>> maps = records.toMapList();

// select `o`.* from `student` as `o` right join student as s on (`o`.`id`=`s`.`id` and `s`.`id`!="3" and `s`.`id`not in("4","5")) order by o.`id` asc
RecordList<StudentModel.Entity, Integer> records = studentModel.newQuery().select("*")
    .join(JoinType.RIGHT, "student as s", builder -> builder.whereRaw(builder.columnAlias("id") + "=s.id")
            .whereRaw("s.id!=3").whereRaw("s.id not in (4,5)"))
    .orderBy("id").get();
List<Map<String, Object>> maps = records.toMapList();

// 找出age最大的男生/女生的信息(有同年龄的就都找出来)
// select `student`.* from `student` inner join (select `sex`,max(age) as 'max_age' from `student` group by `sex`)t on (`student`.`sex`=`t`.`sex` and `student`.`age`=`t`.`max_age`);
RecordList<StudentModel.Entity, Integer> records = studentModel.newQuery()
    .select("*")
    .join(JoinType.INNER,
            builder -> builder.select("sex").selectFunction("max", builder.columnAlias("age"), "max_age").group("sex"),
            "t", builder -> builder.whereRaw(builder.columnAlias("sex") + "=t.sex").whereRaw(builder.columnAlias("age") + "=t.max_age"))
    .orderBy("id")
    .get();
List<StudentModel.Entity> entities = records.toObjectList();
```

## limit
限制获取的数量
```java
// 跳过2条, 取3条
RecordList<Student, Long>> records = studentModel.newQuery().orderBy("id", OrderBy.DESC).limit(2, 3).get();

// 取2条记录
RecordList<Student, Long>> records = studentModel.newQuery().orderBy("id", OrderBy.DESC).limit(2).get();
```

## from

用以指定表名,大多数情况下可以不手动调用, 使用默认值

```java
RecordList<Student, Long>> records = studentModel.newQuery().from("student").get();
```

子查询

```java
// select count(*) as 'ccc' from (select `sex` from `student` group by `sex`)t
RecordList<StudentModel.Entity, Integer> records = studentModel.newQuery().selectFunction("count", "*", "ccc")
    .from("t", builder -> builder.group("sex").select("sex"))
    .get();

Assert.assertEquals(records.size(), 1);
Assert.assertEquals(records.toMapList().get(0).get("ccc").toString(), "2");

// select count(*) as 'nvVeCH' from (select `sex` from `student` group by `sex`)nvVeCHsub limit 1
Long count = studentModel.newQuery().group("sex").select("sex").count();
Assert.assertEquals(count.intValue(), 2);
```

## index
指定索引

#### forceIndex  ignoreIndex

用以指定使用的索引或者不使用的索引

```java
// 指定使用PRI索引
RecordList<StudentModel.Entity, Integer> records1 = studentModel.newQuery().whereRaw("1").forceIndex("PRI").get();

// 指定不使用PRI索引
RecordList<StudentModel.Entity, Integer> records2 = studentModel.newQuery().whereRaw("1").ignoreIndex("PRI").get();

// 举个例子, 不要在意细节
RecordList<StudentModel.Entity, Integer> records3 = studentModel.newQuery().whereRaw("1").forceIndex("PRI").ignoreIndex("PRI").get();
```

## data

### data

```java
Map<String, String> map = new HashMap<>();
map.put("name", "gggg");
map.put("age", "7");

// update `student` set`name`="gggg",`age`="7" where `id`="3"
int num = studentModel.newQuery().data(map).where("id", "3").update();

// update `student` set`name`="小明",`age`="7" where `id`="3"
int num = studentModel.newQuery().data("name","小明").data("age","7").where("id", "3").update();


Map<String, String> mapHasNull = new HashMap<>();
mapHasNull.put("name", "gggg");
mapHasNull.put("age", null);

// update `student` set`name`="gggg",`age`= null where `id`="3"
int num = studentModel.newQuery().data(mapHasNull).where("id", "3").update();

// update `student` set`name`="小明",`age`=null where `id`="3"
int num = studentModel.newQuery().data("name","小明").data("age",null).where("id", "3").update();
```

### dataIgnoreNull

会忽略为`null`的值

```java
Map<String, String> map = new HashMap<>();
map.put("name", "gggg");
map.put("age", null);

// update `student` set`name`="gggg" where `id`="3"
int num = studentModel.newQuery().dataIgnoreNull(map).where("id", "3").update();

// update `student` set`age`="7" where `id`="3"
int num = studentModel.newQuery().dataIgnoreNull("name",null).data("age","7").where("id", "3").update();
```

### dateBit
将列的值设定为指定选项(多选项 0,1,2,3,4....)
```java
// 将id为1的学生的爱好设置为5
// update student set `sex`= "32"  where `id`= "1" 
studentModel.newQuery().where("id", 1).dataBit(StudentModel.Entity::getBobby, Collections.singletonList(5)).update();
```

## union

```java
RecordList<Student, Long>> records = studentModel.newQuery()
.unionAll((builder -> builder.where("id", "2")))
.union((builder -> builder.where("id", "7")))
.firstOrFail();
```

## 事务

- `多线程` 事物绑定在线程中 **`(非常重要)`**
    - 先开启事物，再在事物中开启多线程执行，子线程不处于事物中
    - 先开启多线程，再在每个子线程中开启事物(即使是同一个数据库连接GaarasonDataSource)，事物之间相互隔离，可以按单线程思路书写业务。

- `隔离级别` 可以设置在[注册bean](/document/bean.md)的`SESSION SQL_MODE`。
- `传播性` 全局默认 PROPAGATION_NESTED
    - 如果不存在事务，创建事务。如果存在事务，则嵌套在事务内，嵌套事务依赖外层事务提交，不进行独立事务提交。
    - 嵌套事务如果发生异常，则抛出异常，回滚嵌套事务的操作，回到开始嵌套事务的“保存点”，由外层事务的逻辑继续执行（外层捕获异常并处理即可）。
    - 嵌套事务如果不发生异常，则继续执行，不提交。由外层事务的逻辑继续执行，若外层事务后续发生异常，则回滚包括嵌套事务在内的所有事务。
- `Spring boot`
    - 通过`@EnableTransactionManagement` 开启事物管理后使用 `@Transactional` 管理亦可，此时的事务特性遵循`Spring`规范。

### 手动事物
请手动捕获异常, 以确保 rollBack()/commit() 的正确执行, 以释放连接
```java
// 开启事物
studentModel.newQuery().begin();

// do something
// 请手动捕获异常, 以确保 rollBack()/commit() 的正确执行, 以释放连接
studentModel.newQuery().where("id", "1").data("name", "dddddd").update();
StudentSingleModel.Entity entity = studentModel.newQuery().where("id", "1").firstOrFail().toObject();

// 回滚
studentModel.newQuery().rollBack();

// 提交
studentModel.newQuery().commit();
```

### 闭包事务

- 异常自动回滚, 原样向上抛出
- 语义表达性更直观
- 自动处理死锁异常

#### 无返回值

```java
// 开启事物
studentModel.newQuery().transaction(() -> {
    // do something
    studentModel.newQuery().where("id", "1").data("name", "dddddd").update();
    StudentSingleModel.Entity entity = studentModel.newQuery().where("id", "1").firstOrFail().toObject();
}, 3);
```

#### 有返回值

```java
// 开启事物
boolean success = studentModel.newQuery().transaction(() -> {
    // do something
    studentModel.newQuery().where("id", "1").data("name", "dddddd").update();
    StudentSingleModel.Entity entity = studentModel.newQuery().where("id", "1").firstOrFail().toObject();
    
    return true;
}, 3);
```
### 闭包异步事务

事务中的语句将和事务一起异步执行

#### transactionAsync

```java
// 事物中执行
CompletableFuture<Boolean> future = studentModel.newQuery().transactionAsync(() -> {
    Record<StudentModel.Entity, Integer> record = studentModel.newQuery().findOrFail(1);
    StudentModel.Entity student = record.getEntity();
    student.setName(newName);
    return record.save();
});

// 堵塞获取结果
future.get();
```

### 共享锁与排他锁

#### sharedLock  lockForUpdate

```java
studentModel.newQuery().transaction(()->{
    studentModel.newQuery().where("id", "3").sharedLock().get();
}, 3);
```

```java
studentModel.newQuery().transaction(()->{
    studentModel.newQuery().where("id", "3").lockForUpdate().get();
}, 3);
```

## 分页

### 快速分页

#### simplePaginate

不包含总数的分页

```java
Paginate<Student> paginate = studentModel.newQuery().orderBy("id").simplePaginate(1, 3);

Paginate<Map<String, Object>> paginateMap = studentModel.newQuery().orderBy("id").simplePaginateMapStyle(1, 3);
```

### 总数分页

#### paginate

包含总数的分页, 会额外执行一次总数查询

```java
Paginate<Student> paginate = studentModel.newQuery().orderBy("id").paginate(1, 4);

Paginate<Map<String, Object>> paginate = studentModel.newQuery().orderBy("id").paginateMapStyle(1, 4);
```

## 功能

### 随机抽样

#### inRandomOrder

接收一个参数,优先选用连续计数类型字段(均匀分布的自增主键最佳).  
在300w数据量下,效率约是`order by rand()`的5倍,任何情况下均有优越表现

```java
studentModel.newQuery().where("sex","1").orderBy("RAND()").limit(5).get().toObjectList();

studentModel.newQuery().where("sex","1").inRandomOrder("id").limit(5).get().toObjectList();
```

### 构造器序列化

`builder`可以序列化到`String`或者`byte[]`

#### serializeToString serialize deserialize

```java
// 一个未使用过的, 查询构造器
Builder<Student, Integer> builder = studentModel.newQuery().with("teachersBelongsToMany",b->{
    return b.limit(student1.getAge());
});

// 序列化
// byte[] serialize = builder.serialize();
String serialize = builder.serializeToString();

// 反序列化
Builder<Student, Integer> builderCopy = Builder.deserialize(serialize);
```

### 构造器传递

#### setBuilder mergerBuilder

```java
Builder<Student, Integer> builder = studentModel.newQuery().where("sex","1");

// 覆盖 setBuilder
// select * from student where sex=1
studentModel.newQuery().limit(5).setBuilder(builder).get().toObjectList();

// 合并 mergerBuilder
// select * from student where sex=1 limit 5
studentModel.newQuery().limit(5).mergerBuilder(builder).get().toObjectList();
```

#### 类型指定
在是用`with(string)`等方法时, 可以指定到查询构造器, 以便编译器在编码时给出代码提示
```java
studentModel.newQuery().with("teacher", builder -> builder.showType(
                new ShowType<MySqlBuilderV2<Teacher, Long>>() {}).paginate(1, 15);
```

#### lastRaw
- 在`查询构造器`生成的sql的尾部, 拼接不经过任何处理的原生sql片段 (支持sql参数绑定)
```java
// select * from `student` as `student_290579508` order by `student_290579508`.`id` desc limit 2 ,3
studentModel.newQuery().orderBy("id", OrderBy.DESC).lastRaw("limit 2 ,3").get().toObjectList();

// select * from `student` as `student_1101048445` order by `student_1101048445`.`id` desc limit "8","3"
studentModel.newQuery().orderBy("id", OrderBy.DESC).lastRaw("limit  ? , ? ", Arrays.asList(8, 3)).get().toObjectList();
```