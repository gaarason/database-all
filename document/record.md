# database
Eloquent ORM for Java
## 目录
* [注册配置](/document/bean.md)
* [数据映射](/document/mapping.md)
* [数据模型](/document/model.md)
* [查询结果集](/document/record.md)
    * [总览](#总览)
    * [普通java对象](#普通java对象)
    * [通用map对象](#通用map对象)
    * [自定list对象](#自定list对象)
    * [ORM](#ORM)
        * [基本操作](#基本操作)
            * [新增](#新增)
                * [自动主键](/document/mapping.md)
                * [自定义主键](/document/mapping.md)
            * [查询](#查询)
            * [更新](#更新)
            * [删除](#删除)
        * [拓展操作](#拓展操作)
            * [findOrCreate](#findOrCreate)
            * [findOrNew](#findOrNew)
            * [findByPrimaryKeyOrCreate](#findByPrimaryKeyOrCreate)
            * [findByPrimaryKeyOrNew](#findByPrimaryKeyOrNew)
            * [updateOrCreate](#updateOrCreate)
            * [updateByPrimaryKeyOrCreate](#updateByPrimaryKeyOrCreate)
        * [集合操作](#集合操作)
            * [avg](#avg)
            * [sum](#sum)
            * [max](#max)
            * [min](#min)
            * [mode](#mode)
            * [median](#median)
            * [chunk](#chunk)
            * [contains](#contains)
            * [count](#count)
            * [countBy](#countBy)
            * [every](#every)
            * [filter](#filter)
            * [reject](#reject)
            * [first](#first)
            * [groupBy](#groupBy)
            * [implode](#implode)
            * [keyBy](#keyBy)
            * [last](#last)
            * [mapToGroups](#mapToGroups)
            * [mapWithKeys](#mapWithKeys)
            * [shift](#shift)
            * [pop](#pop)
            * [prepend](#prepend)
            * [push](#push)
            * [put](#put)
            * [pull](#pull)
            * [random](#random)
            * [reverse](#reverse)
            * [sortBy](#sortBy)
            * [sortByDesc](#sortByDesc)
            * [splice](#splice)
            * [take](#take)
            * [unique](#unique)
* [查询构造器](/document/query.md)
* [关联关系](/document/relationship.md)
* [生成代码](/document/generate.md)
* [版本信息](/document/version.md)
    
## 总览

使用查询构造器或者ORM查询数据库记录时, 返回的类型一般为  

- 单条记录`gaarason.database.eloquent.Record<T, K>`
- 多条记录`gaarason.database.eloquent.RecordList<T, K>`
- 受影响的行数`int`

## 普通java对象

`gaarason.database.eloquent.Record<T, K>`通过`toObject`可以转化为对应的泛型实体  
`gaarason.database.eloquent.RecordList<T, K>`通过`toObjectList`可以转化为对应的泛型实体列表  

## 通用map对象

`gaarason.database.eloquent.Record<T, K>`通过`toMap`可以转化为`Map<String, Object>`  
`gaarason.database.eloquent.RecordList<T, K>`通过`toMapList`可以转化`List<Map<String, Object>>`  

## 自定list对象

`gaarason.database.eloquent.RecordList<T, K>`通过`toList`可以转化为`List<Object>`  
```$xslt
// 返回由id组成的列表
List<Object> list = studentModel.newQuery().get().toList(
    theRecord -> theRecord.toObject().getId();
)
```
## ORM

对于`gaarason.database.eloquent.Record<T, K>`对象提供ORM相关的能力  
所有操作均可触发[数据模型](/document/model.md)`事件`

### 基本操作

#### 新增

执行一次`insert` ，并赋值通过程序插入的已知的属性  
并尝试赋值数据库产生的`自增主键`。 

```java
// 先获取新的 record
Record<Student, Long> record = studentModel.newRecord();

// 此处不应使用 toObject() 获取具体泛型对象
Student student = record.getEntity();

// 设置属性
student.setName("肖邦");

// 保存
record.save();
```

#### 查询
findOrFail
```java
// 先获取 record , 参数类型为主键类型
// 查找id=3的记录, 记录不存在则抛出`EntityNotFoundException`异常
Record<Student, Long> record = studentModel.findOrFail(3);

// 查找id=3的记录, 记录不存在则返回null
    Record<Student, Long> record = studentModel.newQuery().where("id","3").first();
```

findAll
```java
// select name,age from student
RecordList<Student, Long> record = studentModel.findAll("name","age");
```
findMany

```java
// select * from student where id in (1,2,3)
RecordList<Student, Long> record = studentModel.findMany(1,2,3);

List<Object> ids = new ArrayList<>();
ids.add(1);
ids.add(2);
ids.add(3);
// select * from student where id in (1,2,3)
RecordList<Student, Long> record = studentModel.findMany(ids);

```

#### 更新
```java
// 先获取 record , 参数类型为主键类型
Record<Student, Long> record = studentModel.findOrFail(3);

// 此处不应使用 toObject() 获取具体泛型对象
Student student = record.getEntity();

// 设置属性
student.setName("肖邦");

// 保存
record.save();
```

#### 删除
```java

// 先获取 record , 参数类型为主键类型
Record<Student, Long> record = studentModel.findOrFail(3);

// 删除
record.delete();
```
### 拓展操作

#### findOrCreate

##### findOrCreate(entity)

- findOrCreate(entity) 方法先尝试通过给定实体(列/值对)在数据库中查找记录，如果没有找到的话则通过给定属性创建一个新的记录。
- 当创建一个新的记录时, 仅会执行 insert 语句, 也就是说那些没有在实体中确认的字段, 且数据库存在默认值的字段, 返回的结果集不会包含. 如果需要完整的信息可以手动调用 refresh() 重新从数据库中获取

```java
// 按照 entity 中的非null属性作为查询条件, 如果没有找到的话则通过给定属性创建一个新的记录. (实体定义时, 对于基本数据结构要使用包装类型)
// select * from student where ... limit 1
// 如果上面查询没有数据 则执行 insert into `student`(`...`) values("...")
Record<StudentORMModel.Entity, Integer> theRecord = studentORMModel.findOrCreate(entity);

// 刷新结果集属性 (select * from student where ... limit 1)
theRecord.refresh();

```

##### findOrCreate(conditionEntity, complementEntity)

- findOrCreate(conditionEntity, complementEntity) 方法先尝试通过给定 conditionEntity实体(列/值对)在数据库中查找记录，如果没有找到的话则通过给定 complementEntity 与 conditionEntity 实体(列/值对)的并集(complementEntity中的属性有更高的优先级)创建一个新的记录 。
- 当创建一个新的记录时, 仅会执行 insert 语句, 也就是说那些没有在实体中确认的字段, 且数据库存在默认值的字段, 返回的结果集不会包含. 如果需要完整的信息可以手动调用 refresh() 重新从数据库中获取

```java
// 按照 conditionEntity 中的非null属性作为查询条件. (实体定义时, 对于基本数据结构要使用包装类型)
// select * from student where ... limit 1
// 如果上面查询没有数据 则执行 insert into `student`(`...`) values("...")
Record<StudentORMModel.Entity, Integer> theRecord = studentORMModel.findOrCreate(conditionEntity, complementEntity);

// 刷新结果集属性 (select * from student where ... limit 1)
theRecord.refresh();

```

#### findOrNew

##### findOrNew(entity)

- 和 findOrCreate(entity) 非常类似, 差别在于, 如果没有找到对应的记录的话则通过给定属性创建一个新的记录, 这个记录不会自动持久化到数据库中, 需要手动调用 save() 进行持久化

```java
// 按照 entity 中的非null属性作为查询条件, 如果没有找到的话则通过给定属性创建一个新的记录. (实体定义时, 对于基本数据结构要使用包装类型)
// select * from student where ... limit 1
// 如果上面查询没有数据 则构造数据结果集进行返回
Record<StudentORMModel.Entity, Integer> theRecord = studentORMModel.findOrNew(entity);

// 手动持久化 insert into `student`(`...`) values("...")
theRecord.save();

// 刷新结果集属性 (select * from student where ... limit 1)
theRecord.refresh();

```

##### findOrNew(conditionEntity, complementEntity)

- 和 findOrCreate(conditionEntity, complementEntity) 非常类似, 差别在于, 如果没有找到对应的记录的话则通过给定属性创建一个新的记录, 这个记录不会自动持久化到数据库中, 需要手动调用 save() 进行持久化

```java
// 按照 conditionEntity 中的非null属性作为查询条件. (实体定义时, 对于基本数据结构要使用包装类型)
// select * from student where ... limit 1
// 如果上面查询没有数据 则构造数据结果集进行返回
Record<StudentORMModel.Entity, Integer> theRecord = studentORMModel.findOrNew(conditionEntity, complementEntity);

// 手动持久化 insert into `student`(`...`) values("...")
theRecord.save();

// 刷新结果集属性 (select * from student where ... limit 1)
theRecord.refresh();

```

#### findByPrimaryKeyOrCreate

##### findByPrimaryKeyOrCreate(entity)

- 和 findOrCreate(entity) 非常类似, 差别在于, 在进行查询操作时, 仅会使用主键进行查询

```java
// 使用 entity 中主键属性作为查询条件, 如果没有找到的话则通过给定属性创建一个新的记录.
// select * from student where ... limit 1
// 如果上面查询没有数据 则执行 insert into `student`(`...`) values("...")
Record<StudentORMModel.Entity, Integer> theRecord = studentORMModel.findByPrimaryKeyOrCreate(entity);

// 刷新结果集属性 (select * from student where ... limit 1)
theRecord.refresh();

```

#### findByPrimaryKeyOrNew

##### findByPrimaryKeyOrNew(entity)

- 和 findByPrimaryKeyOrCreate(entity) 非常类似, 差别在于, 如果没有找到对应的记录的话则通过给定属性创建一个新的记录, 这个记录不会自动持久化到数据库中, 需要手动调用 save() 进行持久化
```java
// 使用 entity 中主键属性作为查询条件, 如果没有找到的话则通过给定属性创建一个新的记录.
// select * from student where ... limit 1
// 如果上面查询没有数据 则构造数据结果集进行返回
Record<StudentORMModel.Entity, Integer> theRecord = studentORMModel.findByPrimaryKeyOrNew(entity);

// 手动持久化 insert into `student`(`...`) values("...")
theRecord.save();

// 刷新结果集属性 (select * from student where ... limit 1)
theRecord.refresh();

```

#### updateOrCreate

- updateOrCreate(T conditionEntity, T complementEntity) 方法先尝试通过给定 conditionEntity实体(列/值对)在数据库中查找记录，如果没有找到的话则通过给定 complementEntity 与 conditionEntity 实体(列/值对)的并集(complementEntity中的属性有更高的优先级)创建一个新的记录 。如果找到的话则通过给定 complementEntity (列/值对) 对其进行更新 。

```java
// 按照 conditionEntity 中的非null属性作为查询条件 select * from student where ... limit 1 (实体定义时, 对于基本数据结构要使用包装类型)
// 如果上面查询没有数据 则执行 insert into `student`(...) values(...)
// 如果上面查询存在数据 则执行 update `student` set ...
Record<StudentORMModel.Entity, Integer> theRecord = studentORMModel.updateOrCreate(stu1, stu2);

```
#### updateByPrimaryKeyOrCreate

- updateByPrimaryKeyOrCreate(entity) 方法先尝试使用 entity 中主键属性作为查询条件查找记录，如果没有找到的话则通过给定 entity 实体(列/值对)创建一个新的记录 。如果找到的话则通过给定 entity (列/值对) 对其进行更新 。

```java
// 按照 entity 中的书剑属性作为查询条件 select * from student where ... limit 1 
// 如果上面查询没有数据 则执行 insert into `student`(...) values(...)
// 如果上面查询存在数据 则执行 update `student` set ...
Record<StudentORMModel.Entity, Integer> theRecord = studentORMModel.updateByPrimaryKeyOrCreate(stu);

```

### 集合操作

- 为处理结果集数据提供了方便的封装

#### avg
- 返回集合中的所有元素的指定属性的值的平均值
```java
final BigDecimal decimal = records.avg("age");
Assert.assertEquals(new BigDecimal("13.7"), decimal);
```

#### sum
- 返回集合中所有元素的指定属性值的总和
```java
final BigDecimal decimal = records.sum("age");
Assert.assertEquals(137, decimal.intValue());
```

#### max
- 返回集合中所有元素的指定属性值的最大值
```java
final int age = records.max("age").intValue();
Assert.assertEquals(17, age);
```

#### min
- 返回集合所有元素的指定属性的值的最小值
```java
final int age = records.min("age").intValue();
Assert.assertEquals(6, age);
```
#### mode
- 返回集合所有元素的指定属性的值的众数
```java
List<Byte> ages = records.mode("age");
Assert.assertFalse(ages.isEmpty());
Assert.assertEquals(2, ages.size());
Assert.assertTrue(ages.contains(Byte.valueOf("17")));
Assert.assertTrue(ages.contains(Byte.valueOf("11")));
```

#### median
- 返回集合所有元素的指定属性的值的中位数
```java
BigDecimal medianAge = records.median("age");
Assert.assertEquals(new BigDecimal("15.5"), medianAge);
```

#### chunk
- 将一个集合中的元素分割成多个小尺寸的小集合
##### chunk(int newSize)
- 将一个集合中的元素分割成多个小尺寸的小集合
```java
Assert.assertEquals(10, records.size());
List<List<Record<StudentModel.Entity, Integer>>> lists = records.chunk(4);
Assert.assertEquals(3, lists.size());
Assert.assertEquals(4, lists.get(0).size());
Assert.assertEquals(4, lists.get(1).size());
Assert.assertEquals(2, lists.get(2).size());
```
##### chunk(ReturnTwo<Integer, E, W> closure, int newSize)
- 将一个集合中的匀速分割成多个小尺寸的小集合, 小集合中类型是自定义的
```java
Assert.assertEquals(10, records.size());
List<List<Record<StudentModel.Entity, Integer>>> lists = records.chunk((index, e) -> e, 4);
Assert.assertEquals(3, lists.size());
Assert.assertEquals(4, lists.get(0).size());
Assert.assertEquals(4, lists.get(1).size());
Assert.assertEquals(2, lists.get(2).size());
```
##### chunkToMap(int newSize)
- 将一个集合分割成多个小尺寸的小集合, 小集合中类型是Map<String, Object>
```java
Assert.assertEquals(10, records.size());
final List<List<Map<String, Object>>> lists = records.chunkToMap(4);
Assert.assertEquals(3, lists.size());
Assert.assertEquals(4, lists.get(0).size());
Assert.assertEquals(4, lists.get(1).size());
Assert.assertEquals(2, lists.get(2).size());
```

#### contains

##### contains(String fieldName, Object value)
- 判断集合是否存在任何一个元素的属性的值等于给定值
```java
Assert.assertFalse(records.contains("name", "sssssssssss"));
Assert.assertTrue(records.contains("name", "小卡卡"));
```
##### contains(DecideTwo<Integer, E> closure)
- 判断集合是否存在任何一个元素满足条件
```java
// 存在主键值为10的元素
boolean contains = records.contains((index, e) -> Objects.equals(e.getOriginalPrimaryKeyValue(), 10));
Assert.assertTrue(contains);
```

#### count
- 返回集合中所有元素的总数
```java
// 等价 size()
Assert.assertEquals(10, records.count());
```

#### countBy
##### countBy(String fieldName)
- 计算集合中每个元素的指定属性的值的出现次数
```java
// 每个年龄多少人
Map<Byte, Integer> ageMap = ObjectUtils.typeCast(records.countBy("age"));

Assert.assertEquals(5, ageMap.size());
Assert.assertEquals(2, ageMap.get(Byte.valueOf("16")).intValue());
Assert.assertEquals(3, ageMap.get(Byte.valueOf("11")).intValue());
Assert.assertEquals(3, ageMap.get(Byte.valueOf("17")).intValue());
Assert.assertNotEquals(3, ageMap.get(Byte.valueOf("15")).intValue());
Assert.assertEquals(1, ageMap.get(Byte.valueOf("15")).intValue());
```
##### countBy(ReturnTwo<Integer, E, W> closure)
- 计算集合中每个元素的自定义维度的出现次数
```java
// 每个性别多少人
Map<Byte, Integer> sexMap = records.countBy((index, e) -> e.getEntity().getSex());

Assert.assertEquals(2, sexMap.size());
Assert.assertEquals(6, sexMap.get(Byte.valueOf("1")).intValue());
Assert.assertEquals(4, sexMap.get(Byte.valueOf("2")).intValue());
```
#### every
##### every(DecideTwo<Integer, E> closure)
- 是否集合的所有元素能够通过给定的真理测试
- 如果集合为空，every 方法将返回 true
```java
// 每个人的年龄都比1大吗
final boolean b = records.every((index, e) -> ConverterUtils.cast(e.getMetadataMap().get("age").getValue(), Byte.class) > Byte.parseByte("1"));

Assert.assertTrue(b);
```
#### filter
- 过滤集合
- 改变自身
##### filter()
- 集合中的所有元素为空的都会被移除
```java
final int filter1 = records.filter();

Assert.assertEquals(0, filter1);
```
##### filter(String fieldName)
- 集合中的所有元素的指定属性的值为空的都会被移除
```java
// 移除没有老师的元素（学生）
final int num = records.filter("teacherId");

Assert.assertEquals(1, num);
Assert.assertEquals(9, records.size());
for (Record<StudentModel.Entity, Integer> record : records) {
    Assert.assertFalse(ObjectUtils.isEmpty(record.toObject().getTeacherId()));
}
```
##### filter(DecideTwo<Integer, E> closure)
- 通过给定回调过滤集合，只有通过给定真理测试的元素才会保留下来
```java
// 移除没有name的元素
records.filter((index, e) -> !ObjectUtils.isEmpty(ConverterUtils.castNullable(elementGetValueByFieldName(e, "name"), Object.class)));
```
#### reject
- 通过给定回调过滤集合，只有通过给定真理测试的元素才会被移除
- 改变自身
- 与`filter(DecideTwo<Integer, E> closure)`类似
```java
// 移除 sex = 1 的数据
final int reject = records.reject((index, e) -> (Byte.valueOf("1")).equals(e.getMetadataMap().get("sex").getValue()));

Assert.assertEquals(6, reject);
Assert.assertEquals(4, records.size());
```

#### first
- 没有数据满足时， 返回null
##### first
- 返回第一个元素
```java
// 等价 get(0)
final Record<StudentModel.Entity, Integer> first = records.first();
assert first != null;
```
##### first(DecideTwo<Integer, E> closure)
- 通过给定回调过滤集合，返回满足条件的第一个元素
```java
// 返回第一个id大于4的元素
final Record<StudentModel.Entity, Integer> record = records.first((index, e) -> e.toObject().getId() > 4);

assert record != null;
Assert.assertEquals(5, record.toObject().getId().intValue());
```

#### groupBy
##### groupBy(String fieldName)
- 对集合中的元素按照通过给定属性的值进行分组
```java
//  按性别分组
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
```
##### groupBy(ReturnTwo<Integer, E, W> closure)
- 通过给定回调对集合中的元素进行分组
```java
// 按照年龄取模的值进行分组
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
```
#### implode
##### implode(String fieldName, CharSequence delimiter)
- 将集合中的每一个元素的属性的值, 使用分隔符连接成一个字符串
```java
final String name = records.implode("name", "##");

Assert.assertEquals("小明##小张##小腾##小云##小卡卡##非卡##狄龙##金庸##莫西卡##象帕", name);
```
##### implode(ReturnOne<E, String> closure, CharSequence delimiter)
- 连接集合中的元素
```java
final String name2 = records.implode(e -> {
    final StudentModel.Entity entity = e.toObject();
    return entity.getId() + "*" + entity.getName();
}, "|");

Assert.assertEquals("1*小明|2*小张|3*小腾|4*小云|5*小卡卡|6*非卡|7*狄龙|8*金庸|9*莫西卡|10*象帕", name2);

```
#### keyBy
##### keyBy(String fieldName)
- 方法将指定属性名的值作为集合的键，如果多个元素拥有同一个键，只有最后一个会出现在新集合里面
```java
final Map<String, Record<StudentModel.Entity, Integer>> keyByName = records.keyBy("name");

Assert.assertEquals(10, keyByName.size());
for (Map.Entry<String, Record<StudentModel.Entity, Integer>> entry : keyByName.entrySet()) {
    Assert.assertEquals(entry.getKey(), entry.getValue().toObject().getName());
}
```
##### keyBy(ReturnTwo<Integer, E, W> closure)
- 方法将指定回调的结果作为集合的键，如果多个元素拥有同一个键，只有最后一个会出现在新集合里面
```java
final Map<Integer, Record<StudentModel.Entity, Integer>> keyByTeacherId = records.keyBy((index, e) -> e.toObject().getTeacherId());

Assert.assertEquals(5, keyByTeacherId.size());
for (Map.Entry<Integer, Record<StudentModel.Entity, Integer>> entry : keyByTeacherId.entrySet()) {
    Assert.assertEquals(entry.getKey(), entry.getValue().toObject().getTeacherId());
}
```

#### last
- 返回最后个元素
- 没有数据满足时， 返回null
##### last()
- 返回最后个元素
```java
Assert.assertEquals(records.last(), records.get(records.size() - 1));
```
##### last(DecideTwo<Integer, E> closure)
- 通过给定回调过滤集合，返回满足条件的最后个元素
- 每个元素都会执行此回调
```java
// 返回最后一个teacherID=2的元素
final Record<StudentModel.Entity, Integer> last = records.last((index, e) -> e.toObject().getTeacherId().equals(2));
assert last != null;
Assert.assertEquals(8, last.toObject().getId().intValue());
Assert.assertEquals(2, last.toObject().getTeacherId().intValue());
```
#### mapToGroups
##### mapToGroups(ReturnTwo<Integer, E, W> closureKey, ReturnTwo<Integer, E, Y> closureValue)
- 通过给定回调对集合中的元素进行分组
```java
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
```
#### mapWithKeys
##### mapWithKeys(ReturnTwo<Integer, E, W> closureKey, ReturnTwo<Integer, E, Y> closureValue)
- 通过给定回调对集合元素进行索引
```java
final Map<Integer, String> map = records.mapWithKeys((index, e) -> {
    final StudentModel.Entity entity1 = e.toObject();
    return entity1.getSex() + entity1.getAge();
}, (index, e) -> {
    final StudentModel.Entity entity1 = e.toObject();
    return entity1.getId() + entity1.getName();
});
Assert.assertEquals(5, map.size());
```

#### pluck
##### pluck(String fieldName)
- 将集合中的每个元素的指定属性的值, 组合成新的列表
```java
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
```
##### pluck(String fieldNameForValue, String fieldNameForKey)
- 将集合中的每个元素的指定属性value的值, 使用给定的属性key的值进行索引, 如果存在重复索引，最后一个匹配的元素将会插入集合
```java
final Map<Byte, String> pluck = records.pluck("name", "age");
Assert.assertEquals(5, pluck.size());
Assert.assertEquals("象帕", pluck.get(Byte.parseByte("15")));
Assert.assertEquals("莫西卡", pluck.get(Byte.parseByte("17")));
Assert.assertEquals("小卡卡", pluck.get(Byte.parseByte("11")));
Assert.assertEquals("非卡", pluck.get(Byte.parseByte("16")));
Assert.assertEquals("小明", pluck.get(Byte.parseByte("6")));
```
#### shift
- 移除并返回集合中的第一个元素, 集合为空时返回null
- 改变自身
```java
final Record<StudentModel.Entity, Integer> record1 = records.shift();
Assert.assertEquals(1, record1.toObject().getId().intValue());
```

#### pop
- 移除并返回集合中最后的元素, 集合为空时返回null
- 改变自身
```java
final Record<StudentModel.Entity, Integer> record1 = records.pop();
Assert.assertEquals(10, record1.toObject().getId().intValue());
```

#### prepend
##### prepend(E element)
- 添加元素到集合开头, 其他元素后移
- 改变自身
```java
// 等价 add(0, element)
records.prepend(element);
```

#### push
##### push(E element)
- 添加元素到集合结尾
- 改变自身
```java
// 等价 add(element)
records.push(element);
```

#### put
##### put(int index, E element)
- 在集合中设置给定键和值, 原值将被替换
- 改变自身
```java
// 等价 set(10 , element)
records.put(10, element);
```
#### pull
##### pull(int index)
- 通过索引从集合中移除并返回元素, 其后的元素前移
- 改变自身
```java
final Record<StudentModel.Entity, Integer> record1 = records.pull(6);
```

#### random
##### random() 
- 集合中返回随机元素, 集合为空时返回null
```java
final Record<StudentModel.Entity, Integer> random1 = records.random();
Assert.assertNotNull(random1);
```

##### random(int count)
- 从集合中返回指定个数的随机元素
```java
final List<Record<StudentModel.Entity, Integer>> records1 = CollectionTests.records.random(10);
Assert.assertEquals(10, records1.size());
```

#### reverse
- 将集合中元素的顺序颠倒, 不影响原集合
```java
final List<Record<StudentModel.Entity, Integer>> recordList = records.reverse();

Assert.assertEquals(10, recordList.size());
for (int i = 0; i < 10; i++){
    final Integer id = recordList.get(i).toObject().getId();
    Assert.assertEquals(10 - i, id.intValue());
}
```

#### sortBy
- 对集合进行排序
##### sortBy(String fieldName)
- 通过元素中的指定属性的值，对集合进行正序排序
```java
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
```

##### sortBy(ReturnTwo<Integer, E, BigDecimal> closure)
- 通过给定回调对集合进行正序排序
```java
// 按年龄小到大排序
List<Record<StudentModel.Entity, Integer>> sortByAge = sortBy((index, e) -> {
    final BigDecimal decimal = ConverterUtils.castNullable(elementGetValueByFieldName(e, "age"), BigDecimal.class);
    return decimal == null ? BigDecimal.ZERO : decimal;
});
```
##### sortBy(ReturnTwo<Integer, E, BigDecimal> closure, boolean ase)
- 通过给定回调对集合进行排序
```java
  // 按年龄大到小排序
  List<Record<StudentModel.Entity, Integer>> sortByAge = sortBy((index, e) -> {
  final BigDecimal decimal = ConverterUtils.castNullable(elementGetValueByFieldName(e, "age"), BigDecimal.class);
  return decimal == null ? BigDecimal.ZERO : decimal;
  }, false);
```
#### sortByDesc
- 对集合进行排序
##### sortByDesc(String fieldName)
- 类似`sortBy(String fieldName)`
##### sortByDesc(ReturnTwo<Integer, E, BigDecimal> closure)
- 类似`sortBy(ReturnTwo<Integer, E, BigDecimal> closure)`

#### splice
- 集合切片
- 影响自身
##### splice(int offset)
- 从给定位置开始移除并返回元素切片
```java
List<Record<StudentModel.Entity, Integer>> records1 = records.splice(8);

Assert.assertEquals(2, records1.size());
Assert.assertEquals(9, records1.get(0).toObject().getId().intValue());
Assert.assertEquals(10, records1.get(1).toObject().getId().intValue());
```
##### splice(int offset, int taken)
- 从给定位置开始移除指定数据大小并返回元素切片
```java
List<Record<StudentModel.Entity, Integer>> records2 = records.splice(1, 3);

Assert.assertEquals(3, records2.size());
Assert.assertEquals(2, records2.get(0).toObject().getId().intValue());
Assert.assertEquals(3, records2.get(1).toObject().getId().intValue());
Assert.assertEquals(4, records2.get(2).toObject().getId().intValue());
```
#### take
- 使用指定数目的元素返回一个新的集合
- 影响自身
##### take(int count)
- 使用指定数目的元素返回一个新的集合
```java
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
```
```java
// 后3个元素
List<Record<StudentModel.Entity, Integer>> records2 = records.take(-3);
Assert.assertEquals(3, records2.size());
Assert.assertEquals(8, records2.get(0).toObject().getId().intValue());
Assert.assertEquals(9, records2.get(1).toObject().getId().intValue());
Assert.assertEquals(10, records2.get(2).toObject().getId().intValue());
```

#### unique
- 去除重复
##### unique(String fieldName)
- 使用元素中的指定属性来剔除重复的元素，不影响自身
```java
// 不同性别的各返回一人
List<Record<StudentModel.Entity, Integer>> records2 = records.unique("sex");
Assert.assertEquals(2, records2.size());
Assert.assertEquals(1, records2.get(0).toObject().getId().intValue());
Assert.assertEquals(3, records2.get(1).toObject().getId().intValue());
```

##### unique(ReturnTwo<Integer, E, Object> closure)
- 使用回调来剔除重复的元素，不影响自身
```java
// 不同年龄的各返回一人
records.unique((index, e) -> elementGetValueByFieldName(e, "age"));
```