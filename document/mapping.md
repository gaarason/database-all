# database

Eloquent ORM for Java

## 目录

* [注册配置](/document/bean.md)
* [数据映射](/document/mapping.md)
    * [总览](#总览)
    * [数据库建议](#数据库建议)
    * [注解](#注解)
        * [Table](#Table)
        * [Primary](#Primary)
            * [自动主键](#自动主键)
            * [自定义主键](#自定义主键)
        * [Column](#Column)
        * [BelongsTo](#BelongsTo)
        * [BelongsToMany](#BelongsToMany)
        * [HasMany](#HasMany)
        * [HasOne](#HasOne)
* [数据模型](/document/model.md)
* [查询结果集](/document/record.md)
* [查询构造器](/document/query.md)
* [关联关系](/document/relationship.md)
* [生成代码](/document/generate.md)
* [版本信息](/document/version.md)

## 总览

数据映射是用将数据库字段与java对象进行相互转换的必要手段, 理解为`数据`  
[反向生成代码](/document/generate.md)  
**数据类型应该使用包装类型替代基本类型 例如使用`Integer`替代`int`**  
任意一个普通pojo对象即可, 下面是一个例子

```java
package temp.pojo;

import gaarason.database.eloquent.annotation.Column;
import gaarason.database.eloquent.annotation.Primary;
import gaarason.database.eloquent.annotation.Table;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@Table(name = "student")
public class Student implements Serializable {

    @Primary()
    @Column(name = "id", unsigned = true)
    private Long id;

    @Column(name = "name", length = 20, comment = "姓名")
    private String name;

    @Column(name = "age", unsigned = true, comment = "年龄")
    private Integer age;

    @Column(name = "sex", unsigned = true, comment = "性别1男2女")
    private Integer sex;

    @Column(name = "teacher_id", unsigned = true, comment = "教师id")
    private Long teacherId;

    @Column(name = "is_deleted")
    private Boolean isDeleted;
 
    @Column(name = "created_at", insertable = false, updatable = false, comment = "新增时间")
    private Date createdAt;
    
    @Column(name = "updated_at", insertable = false, updatable = false, comment = "更新时间")
    private Date updatedAt;
}

```

## 数据库建议

数据库字段建议不允许为`null`, 如果允许为null则在使用`ORM新增`等操作时,需要声明每一个字段,因为程序不能分辨`null`值的意义

## 注解

### Table

- `gaarason.database.eloquent.annotation.Table` 用于确定当前`pojo`映射的数据表名
- 当`pojo`的类名是对应表名的大驼峰时,可以省略(eg: `temp.pojo.SupTeacher`对应数据表`sup_teacher`时,可以省略)

### Primary

##### 自动主键

当数据库主键为`bigint unsigned`时, 可以使用雪花id生成器, 兼容10ms以内时间回拨, 单个进程每秒500w个id

- spring boot
    - 设置工作id gaarason.database.snow-flake.worker-id=2

```java
// 内部用法不建议使用, 因为api可能更改
long id = ContainerProvider.getBean(IdGenerator.SnowFlakesID.class).nextId();

// 建议使用定义时 @Primary() 强行指定
// 注意, 有且只有使用 ORM 新增时,且主键没有赋值时, 生效
// 且 默认的 IdGenerator.Auto.class 更加智能
@Primary(idGenerator = IdGenerator.SnowFlakesID.class)
private Long id;
```

##### 自定义主键

定义主键生成  
**注意, 有且只有使用 ORM 新增时,且主键没有赋值时, 生效**

```java
public static class CustomPrimaryKey implements IdGenerator<Integer> {
    private final static AtomicInteger id = new AtomicInteger(200);

    @Override
    public Integer nextId() {
        return id.getAndIncrement();
    }
}
```

指定使用

```java
@Primary(idGenerator = CustomPrimaryKey.class)
private Integer id;
```

数据插入

```java
final Record<PrimaryKeyTestModel.Entity, Integer> record0 = primaryKeyTestModel.newRecord();
record0.save();
Assert.assertEquals(200, record0.getEntity().getId().intValue());
```

### Column

- `gaarason.database.eloquent.annotation.Column` 用于确定每个数据字段的具体属性
- 当`insertable`以及`updatable`为`false`时, 对应字段的`ORM`操作将被忽略
- 如果某个数据对象没有`Primary`注解, 则大多数`ORM`操作将被禁用

### BelongsTo

- 一对一关系  
  见关联关系

### BelongsToMany

- 多对多关系  
  见关联关系

### HasMany

- 一对多关系  
  见关联关系

### HasOne

- 一对一关系  
  见关联关系




