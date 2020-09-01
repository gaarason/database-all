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
        * [新增](#新增)
        * [查询](#查询)
        * [更新](#更新)
        * [删除](#删除)
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

### 新增

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
### 查询
```java
// 先获取 record , 参数类型为主键类型
// 查找id=3的记录, 记录不存在则抛出`EntityNotFoundException`异常
Record<Student, Long> record = studentModel.findOrFail(3);

// 查找id=3的记录, 记录不存在则返回null
Record<Student, Long> record = studentModel.newQuery().where("id","3").first();

```

### 更新
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

### 删除
```java

// 先获取 record , 参数类型为主键类型
Record<Student, Long> record = studentModel.findOrFail(3);

// 删除
record.delete();
```