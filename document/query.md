# database
Eloquent ORM for Java
## 目录
* [注册bean](/document/bean.md)
* [数据映射](/document/mapping.md)
* [数据模型](/document/model.md)
* [查询结果集](/document/record.md)
* [查询构造器](/document/query.md)
    * [原生语句](#原生语句)
        * [原生查询](#原生查询)
        * [原生更新](#原生更新)
        * [原生新增](#原生新增)
    * [获取](#获取)
        * [分块处理](#分块处理)
    * [插入](#插入)
        * [自增id](#自增id)
    * [更新](#更新)
    * [删除](#删除)
        * [默认删除](#默认删除)
        * [强力删除](#强力删除)
    * [聚合函数](#聚合函数)
    * [自增或自减](#自增或自减)
    * [select](#select)
    * [where](#where)
        * [字段与值的比较](#字段与值的比较)
        * [字段之间的比较](#字段之间的比较)
        * [字段(不)在两值之间](#字段(不)在两值之间)
        * [字段(不)在范围内](#字段(不)在范围内)
        * [字段(不)为null](#字段(不)为null)
        * [子查询](#子查询)
        * [且](#且)
        * [或](#或)
        * [条件为真(假)](#条件为真(假))
    * [having](#having)
    * [order](#order)
    * [group](#group)
    * [join](#join)
    * [limit](#limit)
    * [from](#from)
    * [data](#data)
    * [union](#union)
    * [事务](#事务)
        * [手动事物](#手动事物)
        * [闭包事务](#闭包事务)
        * [共享锁与排他锁](#共享锁与排他锁)
    * [分页](#分页)
        * [快速分页](#快速分页)
        * [总数分页](#总数分页)
    * [功能](#功能)
        * [随机抽样](#随机抽样)
* [关联关系](/document/relationship.md)
* [生成代码](/document/generate.md)
* [版本信息](/document/version.md)

## 总览

一下以示例的方式说明, 均来自源码中的单元测试

## 原生语句

### 原生查询

```java
// 查询单条
Record<Student, Long> record = studentModel.newQuery()
            .query("select * from student where id=1", new ArrayList<>());

// 查询多条
List<String> parameters = new ArrayList<>();
parameters.add("2");
RecordList<Student, Long>, Long> records = studentModel.newQuery().queryList("select * from student where sex=?", parameters);
```
### 原生更新
```java
List<String> parameters = new ArrayList<>();
parameters.add("134");
parameters.add("testNAme");
parameters.add("11");
parameters.add("1");
int num = studentModel.newQuery()
    .execute("insert into `student`(`id`,`name`,`age`,`sex`) values( ? , ? , ? , ? )", parameters);
```
### 原生新增
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
    .executeGetIds("insert into `student`(`id`,`name`,`age`,`sex`) values( ? , ? , ? , ? )", parameters);
```

## 获取

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
当要进行大量数据查询时,可以使用分块,他将自动拼接`limit`字段,在闭包中返回`boolean`表示是否进行下一次迭代  
因为分块查询,并发的数据更改一定会伴随数据不准确的问题,如同redis中的`keys`与`scan`
```java
studentModel.where("age","<","9").dealChunk(2000, records -> {
    // do something
    records.toObjectList();
    return true;
});
```

## 插入

```java

// 推荐
// 实体赋值插入
Student student = new Student();
// student.setId(99); 数据库主键自增的话,可以省略
student.setName("姓名");
student.setAge(Byte.valueOf("13"));
student.setSex(Byte.valueOf("1"));
student.setTeacherId(0);
student.setCreatedAt(new Date(1312312312));
student.setUpdatedAt(new Date(1312312312));

// 返回受影响的行数
int num = studentModel.newQuery().insert(entity);

// 返回自增主键, 并对entity进行主键赋值
Long id = studentModel.newQuery().insertGetIdOrFail(entity);


// 推荐
// 多个实体操作
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
int num = studentModel.newQuery().insert(entityList);

// 返回自增主键列表
List<Long> ids = studentModel.newQuery().insertGetIds(entity);

// 构造语句插入
 List<String> columnNameList = new ArrayList<>();
columnNameList.add("name");
columnNameList.add("age");
columnNameList.add("sex");
List<String> valueList = new ArrayList<>();
valueList.add("testNAme134");
valueList.add("11");
valueList.add("1");

int num = studentModel.newQuery().select(columnNameList).value(valueList).insert();
```

### 自增id
当数据库主键为`bigint unsigned`时, 可以使用雪花id生成器  
默认使用本机mac地址转化为机器id(范围0-1023, 一旦出现机器id重复, 建议自行实现), 兼容10ms以内时间回拨, 单个进程每秒500w个id
```java
long id = gaarason.database.util.SnowFlakeIdUtil.getId();
```

## 更新
当一个更新语句没有`where`时,将会抛出`ConfirmOperationException`
```java
int num = studentModel.newQuery().data("name", "xxcc").where("id", "3").update();

int num = studentModel.newQuery().data("name", "vvv").where("id", ">", "3").update();

// 抛出`ConfirmOperationException`
studentModel.newQuery().data("name", "xxcc").update();

studentModel.newQuery().data("name", "xxcc").whereRaw(1).update();
```

## 删除

当前model如果非软删除, 则`默认删除`与`强力删除`效果一致  
`软删除`定义以及启用,请看[数据模型](/document/model.md)  
当一个删除语句没有`where`时,将会抛出`ConfirmOperationException`

### 默认删除
```java
int num = studentModel.newQuery().where("id", "3").delete();

// 抛出`ConfirmOperationException`
studentModel.newQuery().delete();

studentModel.newQuery().whereRaw(1).update();
```
### 强力删除
```java
int num = studentModel.newQuery().where("id", "3").forceDelete();

// 抛出`ConfirmOperationException`
studentModel.newQuery().forceDelete();

studentModel.newQuery().whereRaw(1).forceDelete();
```

## 聚合函数
select 中的字段应该确保已经出现在 group 中
```java
Long count0 = studentModel.newQuery().where("sex", "1").group("age").count("id");

Long count1 = studentModel.newQuery().select("id").where("sex", "1").group("id","age").count("id");

Long count2 = studentModel.newQuery().where("sex", "1").count("age");

String max0 = studentModel.newQuery().select("age").where("sex", "1").group("age").max("id");

String max1 = studentModel.newQuery().where("sex", "1").max("id");

String min = studentModel.newQuery().where("sex", "1").min("id");

String avg = studentModel.newQuery().where("sex", "1").avg("id");

String sum = studentModel.newQuery().where("sex", "2").sum("id");
```
## 自增或自减
```java
int update = studentModel.newQuery().dataDecrement("age", 2).whereRaw("id=4").update();

int update2 = studentModel.newQuery().dataIncrement("age", 4).whereRaw("id=4").update();
```

## select
```java
Record<Student, Long> record = studentModel.newQuery().select("name").select("id").select("id").first();

Record<Student, Long> record = studentModel.newQuery().select("name","id","created_at").first();

Record<Student, Long> record = studentModel.newQuery().selectFunction("concat_ws", "\"-\",`name`,`id`", "newKey").first();
```

## where
### 字段与值的比较
where
```java
Record<Student, Long> record = studentModel.newQuery().whereRaw("id<2").first();
Record<Student, Long> record = studentModel.newQuery().where("id", ">", "2").first();
Record<Student, Long> record = studentModel.newQuery().where("id", "!=", "2").first();
Record<Student, Long> record = studentModel.newQuery().where("id", "2").first();
Record<Student, Long> record = studentModel.newQuery().where("name", "like", "%明%").first();
```
### 字段之间的比较
whereColumn
```java
Record<Student, Long> record = studentModel.newQuery().whereColumn("id", ">", "sex").first();
```
### 字段(不)在两值之间
whereBetween
whereNotBetween
```java
RecordList<Student, Long>> records = studentModel.newQuery().whereBetween("id", "3", "5").get();

RecordList<Student, Long>> records = studentModel.newQuery().whereNotBetween("id", "3", "5").get();
```
### 字段(不)在范围内
whereIn
whereNotIn
```java
List<Object> idList = new ArrayList<>();
idList.add("4");
idList.add("5");
idList.add("6");
idList.add("7");
RecordList<Student, Long>> records = studentModel.newQuery().whereIn("id", idList).get();

RecordList<Student, Long>> records = studentModel.newQuery().whereNotIn("id", idList).get();

RecordList<Student, Long>> records = studentModel.newQuery().whereIn("id",
    builder -> builder.select("id").where("age", ">=", "11")
).andWhere(
    builder -> builder.whereNotIn("sex",
        builder1 -> builder1.select("sex").where("sex", "1")
    )
).get()
```
whereNotIn

### 字段(不)为null
whereNull
whereNotNull
```java
RecordList<Student, Long>> records = studentModel.newQuery().whereNull("id").get();

RecordList<Student, Long>> records = studentModel.newQuery().whereNotNull("id").get();
```

### 子查询
```java
List<Object> ins = new ArrayList<>();
ins.add("1");
ins.add("2");
ins.add("3");
RecordList<Student, Long>> records = studentModel.newQuery()
.where("age", "!=", "99")
.whereSubQuery("id", "in", builder -> builder.select("id").whereIn("id", ins))
.get();

RecordList<Student, Long>> records = studentModel.newQuery()
.where("age", "!=", "99")
.whereSubQuery("id", "in", "select id from student where id = 3")
.get();
```
### 且
andWhere
```java
RecordList<Student, Long>> records = studentModel.newQuery().where("id", "3").andWhere(
    (builder) -> builder.whereRaw("id=4")
).get();
```
### 或
orWhere
```java
RecordList<Student, Long>> records = studentModel.newQuery().where("id", "3").orWhere(
    (builder) -> builder.whereRaw("id=4")
).get();
```

### 条件为真(假)
whereExists
whereNotExists
```java
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
RecordList<Student, Long>> records = studentModel.newQuery().orderBy("id", OrderBy.DESC).get();
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
RecordList<Student, Long>> records = studentModel.newQuery()
.select("student.*", "t.age as age2")
.join("student as t", "student.id", "=", "t.age")
.get();
```

## limit
```java
RecordList<Student, Long>> records = studentModel.newQuery().orderBy("id", OrderBy.DESC).limit(2, 3).get();

RecordList<Student, Long>> records = studentModel.newQuery().orderBy("id", OrderBy.DESC).limit(2).get();
```
## from

用以指定表名,大多数情况下可以使用默认值

```java
RecordList<Student, Long>> records = studentModel.newQuery().from("student").get();
```
## data
```java
Map<String, String> map = new HashMap<>();
map.put("name", "gggg");
map.put("age", "7");
int num = studentModel.newQuery().data(map).where("id", "3").update();

int num = studentModel.newQuery().data("name","小明").data("age","7").where("id", "3").update();
```
## union
```java
RecordList<Student, Long>> records = studentModel.newQuery()
.unionAll((builder -> builder.where("id", "2")))
.union((builder -> builder.where("id", "7")))
.firstOrFail();
```

## 事务

- `隔离级别` 设置在[注册bean](/document/bean.md)的`SESSION SQL_MODE`   
- `多线程` 事物绑定在线程中 **`(重要)`**
  -  先开启事物, 再在事物中开启多线程执行, 子线程不处于事物中   
  -  先开启多线程, 再在每个子线程中开启事物(即使是同一个数据库连接GaarasonDataSource), 事物之间相互隔离, 可以按单线程思路书写业务   
- `传播性` PROPAGATION_NESTED 
  -  如果不存在事务，创建事务。如果存在事务，则嵌套在事务内，嵌套事务依赖外层事务提交，不进行独立事务提交。
  -  嵌套事务如果发生异常，则抛出异常，回滚嵌套事务的操作，回到开始嵌套事务的“保存点”，由外层事务的逻辑继续执行（外层捕获异常并处理即可）。
  -  嵌套事务如果不发生异常，则继续执行，不提交。由外层事务的逻辑继续执行，若外层事务后续发生异常，则回滚包括嵌套事务在内的所有事务。 

### 手动事物
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
### 闭包事物

- 异常自动回滚, 原样向上抛出
- 语义表达性更直观
- 自动处理死锁异常

无返回值  
```java
// 开启事物
studentModel.newQuery().transaction(() -> {
    // do something
    studentModel.newQuery().where("id", "1").data("name", "dddddd").update();
    StudentSingleModel.Entity entity = studentModel.newQuery().where("id", "1").firstOrFail().toObject();
}, 3);
```
有返回值
```java
// 开启事物
boolean success = studentModel.newQuery().transaction(() -> {
    // do something
    studentModel.newQuery().where("id", "1").data("name", "dddddd").update();
    StudentSingleModel.Entity entity = studentModel.newQuery().where("id", "1").firstOrFail().toObject();
    
    return true;
}, 3);
```
### 共享锁与排他锁
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
```java
Paginate<Student> paginate = studentModel.newQuery().orderBy("id").simplePaginate(1, 3);
```
### 总数分页
```java
Paginate<Student> paginate = studentModel.newQuery().orderBy("id").paginate(1, 4);
```
## 功能

### 随机抽样
inRandomOrder()  
接收一个参数,优先选用连续计数类型字段(均匀分布的自增主键最佳).  
在300w数据量下,效率约是`order by rand()`的5倍,任何情况下均有优越表现
```java
studentModel.newQuery().where("sex", "1").orderBy("RAND()").limit(5).get().toObjectList();

studentModel.newQuery().where("sex", "1").inRandomOrder("id").limit(5).get().toObjectList();
```