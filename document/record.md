# database
Eloquent ORM for Java
## 目录
* [注册bean](/document/bean.md)
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