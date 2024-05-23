# database

Eloquent ORM for Java

## 目录

* [注册配置 Configuration](/document/bean.md)
* [数据映射 Mapping](/document/mapping.md)
    * [总览](#总览)
    * [数据库建议](#数据库建议)
    * [注解](#注解)
        * [Table](#Table)
        * [Primary](#Primary)
            * [自动主键](#自动主键)
            * [自定义主键](#自定义主键)
        * [Column](#Column)
            * [使用策略](#使用策略)
            * [是否查询](#是否查询)
            * [字段填充](#字段填充)
            * [类型转化](#类型转化)
                * [Default](#Default)
                * [Json](#Json)
                * [EnumInteger](#EnumInteger)
                * [EnumString](#EnumString)
                * [Bit](#Bit)
                * [自定义类型转化](#自定义类型转化)
            * [执行顺序](#执行顺序)
                * [实体到数据库](#实体到数据库)
                * [数据库到实体](#数据库到实体)
        * [BelongsTo](#BelongsTo)
        * [BelongsToMany](#BelongsToMany)
        * [HasMany](#HasMany)
        * [HasOne](#HasOne)
* [数据模型 Model](/document/model.md)
* [查询结果集 Record](/document/record.md)
* [查询构造器 Query Builder](/document/query.md)
* [关联关系 Relationship](/document/relationship.md)
* [生成代码 Generate](/document/generate.md)
* [GraalVM](/document/graalvm.md)
* [版本信息 Version](/document/version.md)

## 总览

数据映射是用将数据库字段与java对象进行相互转换的必要手段, 理解为`数据`  
[反向生成代码 Generate](/document/generate.md)  
**数据类型应该使用包装类型替代基本类型 例如使用`Integer`替代`int`**  
任意一个普通pojo对象即可, 下面是一个例子

```java
package temp.pojo;

import gaarason.database.annotation.Column;
import gaarason.database.annotation.Primary;
import gaarason.database.annotation.Table;
import gaarason.database.contract.support.FieldConversion;
import gaarason.database.contract.support.FieldStrategy;
import gaarason.database.contract.support.IdGenerator;
import gaarason.database.lang.Nullable;
import lombok.Data;
import java.io.Serializable;
import java.util.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@Table(name = "null_test")
public class PrimaryKeyEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Primary(idGenerator = CustomPrimaryKey.class)
    private Integer id;

    @Column(length = 20, nullable = true)
    private String name;

    @Column(name = "json_object_column", conversion = FieldConversion.Json.class, strategy = FieldStrategy.Always.class)
    private Info info;

    @Column(name = "json_array_column", conversion = FieldConversion.Json.class, strategy = FieldStrategy.Always.class)
    private List<Info> infos;

    @Column(name = "time_column", nullable = true)
    private LocalTime timeColumn;

    @Column(name = "date_column", nullable = true)
    private LocalDate dateColumn;

    @Column(name = "datetime_column", nullable = true)
    private LocalDateTime datetimeColumn;

    @Column(name = "timestamp_column", nullable = true)
    private Date timestampColumn;

    private boolean isDeleted;

}

public static class Info {
    public String name;
    public Integer age;
}

```

## 数据库建议

数据库字段建议不允许为`null`, 如果允许为null则在使用`ORM新增`等操作时,需要声明每一个字段,因为程序不能分辨`null`值的意义

## 注解

### Table

- `gaarason.database.eloquent.annotation.Table` 用于确定当前`entity`映射的数据表名
- 当`entity`的类名是对应表名的大驼峰时,可以省略(eg: `temp.pojo.SupTeacher`对应数据表`sup_teacher`时,可以省略)

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

PrimaryKeyTestModel.Entity entity = record0.getEntity();

// 其他属性设置
entity.setName("www");

record0.save();

Assert.assertEquals(200, record0.getEntity().getId().intValue());
```

### Column

- `gaarason.database.eloquent.annotation.Column` 用于确定每个数据字段的具体属性
- 通过对于`Column`的数据设置, 可以精准的控制每一个字段, 在增删查改下的行为
- 如果某个数据对象没有`Primary`注解, 则大多数`ORM`操作将被禁用

#### 使用策略
- strategy
- 是否在插入/更新/条件时使用本字段的值  
- 默认 insertStrategy() == FieldStrategy.Default.class , 即 insertStrategy() 取用 strategy() 的值
- 默认 updateStrategy() == FieldStrategy.Default.class , 即 updateStrategy() 取用 strategy() 的值
- 默认 conditionStrategy() == FieldStrategy.Default.class , 即 conditionStrategy() 取用 strategy() 的值
- 默认 strategy() == FieldStrategy.NotNull.class, 即非null时使用

#### 是否查询
- selectable
- 缺省时是否查询本字段
- 使用 select * 查询将略过本字段
- 主要对于大字段使用

#### 字段填充
- fill
- 字段填充策略
- 业务上可以自行实现 `FieldFill` 接口, 已确定在 插入/更新/条件时, 填充的值
- 提供 `FieldFill.NotFill.class`(默认)不做填充
- 提供 `FieldFill.CreatedTimeFill.class` 在 insert 时对时间类型的字段进行当前时间的填充
- 提供 `FieldFill.UpdatedTimeFill.class` 在 insert 时对时间类型的字段进行当前时间的填充

```java
@Data
public class Entity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Primary
    private Integer id;

    @Column
    private String name;

    @Column(fill = FieldFill.CreatedTimeFill.class)
    private LocalTime timeColumn;

    @Column(fill = FieldFill.UpdatedTimeFill.class)
    private LocalDate dateColumn;

}
```

#### 类型转化
- conversion
- 序列与反序列化
- 业务上可以自行实现 `FieldConversion` 接口, 已确定本字段特定的序列化与反序列化方式  


##### Default
- conversion() 默认值为 FieldConversion.Default.class, 可以解决绝大多数的基本类型的序列化与反序列化

##### Json
- conversion() 可选值为 FieldConversion.Json.class, 以json规范进行序列化与反序列化, 数据的字段应该为合法的json字符串
- 实现依赖于`jackson`, 需要自行引入 `com.fasterxml.jackson.core: jackson-databind` 以及 `com.fasterxml.jackson.datatype: jackson-datatype-jsr310`依赖项
- 因为`json规范`的兼容性细节较多, 所以业务上也可以参考`JsonConversion`自行实现, 与使用
- 数据库列一般使用 varchar

##### EnumInteger
- conversion() 可选值为 FieldConversion.EnumInteger.class, 以枚举类型的`自然次序`进行序列化与反序列化
- 枚举类型的自然次序从 0 开始
- 数据库列一般使用 int

##### EnumString
- conversion() 可选值为 FieldConversion.EnumString.class, 以枚举类型的`名称`进行序列化与反序列化
- 数据库列一般使用 varchar

##### Bit
- conversion() 可选值为 FieldConversion.Bit.class, 将集合按位进行序列化与反序列化
- 数据库列一般使用 int, bigint

##### 自定义类型转化

```java
// 例如, 需要自定义对性别 的序列化与反序列化
// 以便将数据库中的1,2, 序列化为对象上的自定义的枚举类型Sex
public enum Sex {
    MAN,
    WOMAN,
    OTHER;

    static class SexConversion implements FieldConversion {

        @Nullable
        @Override
        public Object serialize(Field field, @Nullable Object fieldValue) {
            if (MAN.equals(fieldValue)) {
                return 1;
            } else if (WOMAN.equals(fieldValue)) {
                return 2;
            } else {
                return 3;
            }
        }

        @Nullable
        @Override
        public Object acquisition(Field field, ResultSet resultSet, String columnName) throws SQLException {
            int sex = resultSet.getInt(columnName);
            if (sex == 1) {
                return MAN;
            } else if (sex == 2) {
                return WOMAN;
            } else {
                return OTHER;
            }
        }

        @Nullable
        @Override
        public Object deserialize(Field field, @Nullable Object originalValue) {
            String str = String.valueOf(originalValue);
            if ("1".equals(str)) {
                return MAN;
            } else if ("2".equals(str)) {
                return WOMAN;
            } else {
                return OTHER;
            }
        }
    }
}

@Data
@Table(name = "student")
public class EnumEntity implements Serializable {

    // other ...
    
    /**
     * 声明使用自定义的字段类型转化
     */
    @Column(conversion = Sex.SexConversion.class)
    private Sex sex;
}

/**
 * 使用
 */
String name = "test_people";
AnnotationTestModel.EnumEntity entity = new AnnotationTestModel.EnumEntity();
entity.setSex(AnnotationTestModel.Sex.WOMAN);
entity.setName(name);

// 普通插入
Integer id = annotationTestModel.newQuery().from(entity).insertGetId(entity);

// 获取后自动转化
AnnotationTestModel.EnumEntity resultEntity = annotationTestModel.newQuery()
    .from(entity)
    .findOrFail(id)
    .toObject(AnnotationTestModel.EnumEntity.class);

Assert.assertEquals(name, resultEntity.getName());
Assert.assertEquals(AnnotationTestModel.Sex.WOMAN, resultEntity.getSex());

```


#### 执行顺序
##### 实体到数据库
- 对于实体`entity`的每一个字段  
- 先根据本次的使用用途, 执行`fill()`进行属性填充    
- 再根据本次的使用用途, 执行对应的`strategy()`进行有效性判断  
- 在有效的前提下, 使用`conversion()`进行序列化, 并准备参与`sql`的执行
- 执行成功后, 在是`ORM`的场景下, 还会将有效的值( `strategy()`通过, 且未进行`conversion()` )回填到对象`entity`

##### 数据库到实体
- 对于实体`entity`的每一个字段
- 当数据库查询执行成功(sql执行成功)后, 立即使用`conversion()`中的`acquisition(field, resultSet, columnName)`进行数据库结果集的获取  
- 当在`record`上使用`toObject()/toObject(SomeEntity.class)`等方法时, 使用`conversion()`中的`deserialize(field, originalValue)`进行结果的序列化
- 最终体现在实体上

### BelongsTo

- 一对一关系  
  见关联关系 Relationship

### BelongsToMany

- 多对多关系  
  见关联关系 Relationship

### HasMany

- 一对多关系  
  见关联关系 Relationship

### HasOne

- 一对一关系  
  见关联关系 Relationship




