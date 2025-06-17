# database

Eloquent ORM for Java

## 目录

* [注册配置 Configuration](/document/bean.md)
* [数据映射 Mapping](/document/mapping.md)
* [数据模型 Model](/document/model.md)
    * [总览](#总览)
    * [数据库连接](#数据库连接)
    * [事件](#事件)
        * [事件触发顺序-查询](#事件触发顺序-查询)
        * [事件触发顺序-新增](#事件触发顺序-新增)
        * [事件触发顺序-修改](#事件触发顺序-修改)
        * [事件触发顺序-删除](#事件触发顺序-删除)
        * [事件触发顺序-软删除恢复](#事件触发顺序-软删除恢复)
        * [事件触发顺序-硬删除](#事件触发顺序-硬删除)
        * [ORM事件](#ORM事件)
        * [Query事件](#Query事件)
        * [注解声明事件](#注解声明事件)
        * [事务完成后执行事件](#事务完成后执行事件)
        * [静默事件](#静默事件)
    * [作用域](#作用域)
        * [自定义查询作用域](#自定义查询作用域)
        * [软删除](#软删除)
* [查询结果集 Record](/document/record.md)
* [查询构造器 Query Builder](/document/query.md)
* [关联关系 Relationship](/document/relationship.md)
* [生成代码 Generate](/document/generate.md)
* [GraalVM](/document/graalvm.md)
* [版本信息 Version](/document/version.md)

## 总览

数据模型 Model是将数据库操作集中声明的对象, 理解为`表`  
[反向生成代码 Generate](/document/generate.md)

## 数据库连接

重写`getGaarasonDataSource`  
下面的例子使用`spring`注入后返回

**`GaarasonDataSource`相关请看[注册配置 Configuration](/document/bean.md)**

```java
package temp.model.base;

import gaarason.database.connection.GaarasonDataSourceWrapper;
import gaarason.database.eloquent.Model;

import javax.annotation.Resource;

/**
 * Model基类
 * @param <T> 实体类
 * @param <K> 实体类中的主键java类型, 不存在主键时, 可使用 Object
 */
abstract public class BaseModel<T, K> extends Model<MysqlBuilder<T, K>, T, K> {

    @Resource
    private GaarasonDataSource gaarasonDataSource;

    @Override
    public GaarasonDataSource getGaarasonDataSource() {
        return gaarasonDataSource;
    }

}

```

子类只需要继承父类即可

```java
package temp.model;

import temp.model.base.BaseModel;
import temp.pojo.Student;
import org.springframework.stereotype.Repository;

@Repository
public class StudentModel extends BaseModel<Student, Long> {

}

```

## 事件

Eloquent 模型，允许你在sql执行生命周期中的多个时间点调用如下这些方法：retrieving, retrieved, creating, created, updating, updated, saving, saved, deleting,
deleted, forceDeleting, forceDeleted, restoring, restored。事件允许你在一个指定模型类每次保存或更新的时候执行代码。

- 其中以 eventRecord 为方法名前缀的事件, 一般 **仅**在使用 [ORM](/document/record.md#ORM) 时, 触发 
- 其中以 eventQuery 为方法名前缀的事件, [ORM](/document/record.md#ORM) 以及 [Query](/document/query.md), **都会触发**
- [原生语句](/document/query.md#原生语句) 的查询方式, **不会触发**任何事件

### 事件触发顺序-查询

- `newQuery().get()`/`newQuery().first()`等
- `model.find()`等

| 次序 |      eventQuery      |     eventRecord      |
|:--:|:--------------------:|:--------------------:|
| 1  | eventQueryRetrieving |                      |
| 2  |                      | eventRecordRetrieved |
| 3  | eventQueryRetrieved  |                      |

### 事件触发顺序-新增

- `newQuery().insert()`/`newQuery().replace()`/`newQuery().upsert()`等
- `record.save()`等

| 次序 |     eventQuery     |     eventRecord     |
|:--:|:------------------:|:-------------------:|
| 1  |                    |  eventRecordSaving  |
| 2  |                    | eventRecordCreating |
| 3  | eventQueryCreating |                     |
| 4  | eventQueryCreated  |                     |
| 5  |                    | eventRecordCreated  |
| 6  |                    |  eventRecordSaved   |

### 事件触发顺序-修改

- `newQuery().update()`等
- `record.save()`/`record.saveByPrimaryKey()`等

| 次序 |     eventQuery     |     eventRecord     |
|:--:|:------------------:|:-------------------:|
| 1  |                    |  eventRecordSaving  |
| 2  |                    | eventRecordUpdating |
| 3  | eventQueryUpdating |                     |
| 4  | eventQueryUpdated  |                     |
| 5  |                    | eventRecordUpdated  |
| 6  |                    |  eventRecordSaved   |

### 事件触发顺序-删除

根据配置, 实际执行`软删除`或者`硬删除`

#### 实际为软删除

- `newQuery().delete()`等
- `record.delete()`等

| 次序 |     eventQuery     |     eventRecord     |
|:--:|:------------------:|:-------------------:|
| 1  |                    | eventRecordDeleting |
| 2  | eventQueryDeleting |                     |
| 3  | eventQueryUpdating |                     |
| 4  | eventQueryUpdated  |                     |
| 5  | eventQueryDeleted  |                     |
| 6  |                    | eventRecordDeleted  |

#### 实际为硬删除

- `newQuery().delete()`等
- `record.delete()`等

| 次序 |       eventQuery        |     eventRecord     |
|:--:|:-----------------------:|:-------------------:|
| 1  |                         | eventRecordDeleting |
| 2  |   eventQueryDeleting    |                     |
| 3  | eventQueryForceDeleting |                     |
| 4  | eventQueryForceDeleted  |                     |
| 5  |    eventQueryDeleted    |                     |
| 6  |                         | eventRecordDeleted  |


### 事件触发顺序-软删除恢复

- `newQuery().restore()`等
- `record.restore()`等

| 次序 |     eventQuery      |     eventRecord      |
|:--:|:-------------------:|:--------------------:|
| 1  |                     | eventRecordRestoring |
| 2  | eventQueryRestoring |                      |
| 3  | eventQueryUpdating  |                      |
| 4  |  eventQueryUpdated  |                      |
| 5  | eventQueryRestored  |                      |
| 6  |                     | eventRecordRestored  |

### 事件触发顺序-硬删除

- `newQuery().forceDelete()`等
- `record.forceDelete()`等

| 次序 |       eventQuery        |       eventRecord        |
|:--:|:-----------------------:|:------------------------:|
| 1  |                         | eventRecordForceDeleting |
| 2  | eventQueryForceDeleting |                          |
| 3  | eventQueryForceDeleted  |                          |
| 4  |                         | eventRecordForceDeleted  |

### ORM事件

所有事件在`ORM风格操作时`时触发,[ORM相关](/document/record.md#ORM) 事件可以继承自父类

则分别在删除以及恢复时触发。

- `ing`结尾的事件, 均可以阻止事件的进行, 需要通过`return false`进行打断.

借用上面的`model`, 则一个事件的定义可以是以下形式

```java
package temp.model;

import temp.model.base.BaseModel;
import temp.pojo.Student;
import org.springframework.stereotype.Repository;

@Repository
public class StudentModel extends BaseModel<Student, Long> {

    @Override
    public void eventRecordRetrieved(Record<Student, Long> entityRecord){
        System.out.println("已经从数据库中查询到数据");
    }
    
    @Override
    public boolean eventRecordUpdating(Record<Student, Long> record){
        if(record.getEntity().getId() == 9){
            System.out.println("正要修改id为9的数据, 但是拒绝");
            return false;
        }
        return true;
    }
}
```

### Query事件

所有事件在`Query风格操作时`时触发,[Query相关](/document/query.md) 事件可以继承自父类

- `ing`结尾的事件, 均可以阻止查询的进行, 需要通过`异常`进行打断.

借用上面的`model`, 则一个事件的定义可以是以下形式

```java
package temp.model;

import temp.model.base.BaseModel;
import temp.pojo.Student;
import org.springframework.stereotype.Repository;

@Repository
public class StudentModel extends BaseModel<Student, Long> {

    @Override
    public void eventQueryRetrieved(Record<Student, Long> entityRecord){
        System.out.println("已经从数据库中查询到数据");
    }
    
    @Override
    public void eventQueryUpdating(Builder<Student, Integer> builder){
        // 通过事件, 增加自定义的条件
        builder.where("xxx", "x");
    }

}
```

需要注意的是, `eventQueryCreated`方法包含3个不同的方法重载, 分别对应不同的响应类型.

### 注解声明事件

- 通过在模型上, 定义注解 `@ObservedBy()`, 可以将事件逻辑从模型上分离出去
- `@ObservedBy()`可以同时声明多个`事件处理器`, 他们将依次按序执行
- `事件处理器`需要实现`Event`接口
- `@ObservedBy()`可以被继承, 在`父model`上声明, 可以被子类锁触发

```java
// model 定义
@ObservedBy(StudentEvent.class)
public class StudentEventV2Model extends SingleModel<StudentEventV2Model.Entity, Integer> {
    
}
```
```java
// 事件处理程序定义
import gaarason.database.contract.model.Event;

public static class StudentEvent implements Event<MySqlBuilderV2<StudentEventV2Model.Entity, Integer>, StudentEventV2Model.Entity, Integer>{

    @Override
    public boolean eventRecordCreating(Record<Entity, Integer> record) {
        Entity entity = record.getEntity();
        // 不让 age 66 更新成功
        return entity.age != 66;
    }
}
```

### 事务完成后执行事件

- 为了避免`事务回滚`导致的`虚假事件`, 我们会希望, 数据库事务提交成功后, 再执行其事件处理程序
- 通过实现 `ShouldHandleEventsAfterCommit` 接口来实现这一点
- 如果没有正在进行的数据库事务，事件处理程序将和之前一样, 立即执行
- 值的注意的是, 此功能仅会影响 `ed` 类型的事件
- 事务成功提交后的事件, 将会使用`快照参数`进行回调
- 多个事务嵌套时, 会在最外层事务成功提交后再触发
- 此外, 即使没有使用`@ObservedBy()`声明专用的`事件处理器`, 也可以在`model`上实现 `ShouldHandleEventsAfterCommit` 达到一样的效果

```java
import gaarason.database.contract.model.Event;
import gaarason.database.contract.support.ShouldHandleEventsAfterCommit;

public static class StudentEvent implements Event<MySqlBuilderV2<StudentEventV2Model.Entity, Integer>, StudentEventV2Model.Entity, Integer>,
    ShouldHandleEventsAfterCommit {

    @Override
    public void eventRecordUpdated(Record<Entity, Integer> record) {
        Entity entity = record.getEntity();
        // .....
    }
}
```
### 静默事件

- 在`查询构造器`中, 通过`quiet()`包装的逻辑, 将不会触发任何事件
- 不支持在此中进行线程切换

```java
newQuery().quiet(() -> {
    // .... 不会触发事件
    newQuery().get();
});

```

- 在`Record`中同样提供了更加便捷的`静默`方法

```java
record.saveQuietly();
record.saveByPrimaryKeyQuietly();
record.deleteQuietly();
record.forceDeleteQuietly();
record.restoreQuietly();
record.restoreQuietly(boolean);
```

## 作用域

在父类或者底层限制数据查询的有效范围

### 自定义查询作用域

```java
package temp.model;

import temp.model.base.BaseModel;
import temp.pojo.Student;
import org.springframework.stereotype.Repository;

@Repository
public class StudentModel extends BaseModel<Student, Long> {
    /**
     * 全局查询作用域
     * @param builder 查询构造器
     * @return 查询构造器
     */
    protected Builder<Student, Long> apply(Builder<Student, Long> builder) {
        // return builder->where("type", "2");
        return builder;
    }
}
```

### 软删除

- 软删除相关实现, 重写 `gaarason.database.eloquent.query.BaseSoftDeleting`的相关方法
- 开启软删除, 重写`softDeleting`方法结果为`true`

```java
package temp.model;

import temp.model.base.BaseModel;
import temp.pojo.Student;
import org.springframework.stereotype.Repository;

@Repository
public class StudentModel extends BaseModel<Student, Long> {

    /**
     * 是否启用软删除
     */
    protected boolean softDeleting() {
        return false;
    }

    /**
     * 删除(软/硬删除)
     * @param builder 查询构造器
     * @return 删除的行数
     */
    public int delete(Builder<Student, Long> builder) {
        return softDeleting() ? softDelete(builder) : builder.forceDelete();
    }

    /**
     * 恢复软删除
     * @param builder 查询构造器
     * @return 删除的行数
     */
    public int restore(Builder<Student, Long> builder) {
        return softDeleteRestore(builder);
    }

    /**
     * 软删除查询作用域(反)
     * @param builder 查询构造器
     */
    protected void scopeSoftDeleteOnlyTrashed(Builder<Student, Long> builder) {
        builder.where("is_deleted", "1");
    }

    /**
     * 软删除查询作用域(反)
     * @param builder 查询构造器
     */
    protected void scopeSoftDeleteWithTrashed(Builder<Student, Long> builder) {

    }

    /**
     * 软删除查询作用域
     * @param builder 查询构造器
     */
    protected void scopeSoftDelete(Builder<Student, Long> builder) {
        builder.where("is_deleted", "0");
    }


    /**
     * 软删除实现
     * @param builder 查询构造器
     * @return 删除的行数
     */
    protected int softDelete(Builder<Student, Long> builder) {
        return builder.data("is_deleted", "1").update();
    }

    /**
     * 恢复软删除实现
     * @param builder 查询构造器
     * @return 恢复的行数
     */
    protected int softDeleteRestore(Builder<Student, Long> builder) {
        return builder.data("is_deleted", "0").update();
    }
}
```