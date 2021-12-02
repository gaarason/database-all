# database
Eloquent ORM for Java
## 目录
* [注册配置](/document/bean.md)
* [数据映射](/document/mapping.md)
* [数据模型](/document/model.md)
* [查询结果集](/document/record.md)
* [查询构造器](/document/query.md)
* [关联关系](/document/relationship.md)
    * [总览](#总览)
    * [关系定义](#关系定义)
        * [一对一](#一对一)
        * [一对多](#一对多)
        * [反向一对多/一对一](#反向一对多/一对一)
        * [多对多](#多对多)
    * [关联查询](#关联查询)
        * [关联方法](#关联方法)
            * [示例一对一](#示例一对一)
            * [示例多级](#示例多级)
            * [示例筛选](#示例筛选)
            * [示例无线级筛选](#示例无线级筛选)
            * [示例混合场景](#示例混合场景)
            * [示例分页](#示例分页)
    * [更新关系](#更新关系)
        * [附加关系](#附加关系)
        * [解除关系](#解除关系)
        * [同步关系](#同步关系)
        * [切换关系](#切换关系)
* [生成代码](/document/generate.md)
* [版本信息](/document/version.md)

## 总览

数据表经常要与其它表做关联，比如一篇博客文章可能有很多评论，或者一个订单会被关联到下单用户   
Eloquent 让组织和处理这些关联关系变得简单，并且支持多种不同类型的关联关系，更重要的是会进行查询优化，这点在多层级关系的情况下尤其明显  

## 关系定义

通过在`entity`中声明对应的属性, 并在属性上使用相关注解`@HasOneOrMany()`,`@BelongsTo()`,`@BelongsToMany()`标记    
所有注解在包 `gaarason.database.eloquent.annotation.*` 中   
现在, 不再需要指定目标模型, 程序会根据字段类型`entity`的找到正确的目标模型

**重要：所有`对应的关系键`的java类型必须严格一致**

### 一对一

`@HasOneOrMany()` 其中包含2个属性:  
- `sonModelForeignKey`表示子表的外键  
- `localModelLocalKey`表示本表的关联键,默认值为本表的主键(`@Primary()`修饰的键)  

以下是一个`teacher`包含一个`pet`(宠物)的例子  
```java
package gaarason.database.test.models.relation.pojo;

import gaarason.database.eloquent.annotation.*;
import gaarason.database.test.models.relation.model.RelationshipStudentTeacherModel;
import gaarason.database.test.models.relation.model.StudentModel;
import lombok.Data;

import java.io.Serializable;

@Data
@Table(name = "teacher")
public class Teacher implements Serializable {

    @Primary()
    @Column(name = "id")
    private Integer id;

    @Column(name = "name", length = 20, comment = "姓名")
    private String name;

    @Column(name = "age", unsigned = true, comment = "年龄")
    private Integer age;

    @Column(name = "sex", unsigned = true, comment = "性别1男2女")
    private Integer sex;

    @Column(name = "subject", length = 20, comment = "科目")
    private String subject;

    // 一对一关联关系声明
    @HasOneOrMany(sonModelForeignKey = "master_id", localModelLocalKey = "id")
    private Pet pet;

}


```

### 一对多

同样使用`@HasOneOrMany()`注解, 用法也是一致的, 唯一要注意的是使用此注解的属性需要是`List<?>`类型

以下是一个`teacher`包含多个`student`的例子  
```java
package gaarason.database.test.models.relation.pojo;

import gaarason.database.eloquent.annotation.*;
import gaarason.database.test.models.relation.model.RelationshipStudentTeacherModel;
import gaarason.database.test.models.relation.model.StudentModel;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
@Table(name = "teacher")
public class Teacher implements Serializable {

    @Primary()
    @Column(name = "id")
    private Integer id;

    @Column(name = "name", length = 20, comment = "姓名")
    private String name;

    @Column(name = "age", unsigned = true, comment = "年龄")
    private Integer age;

    @Column(name = "sex", unsigned = true, comment = "性别1男2女")
    private Integer sex;

    @Column(name = "subject", length = 20, comment = "科目")
    private String subject;

    // 一对多关联关系声明
    @HasOneOrMany(sonModelForeignKey = "teacher_id", localModelLocalKey = "id")
    private List<Student> students;

}

```

### 反向一对多/一对一

`@BelongsTo()` 其中包含2个属性:  
- `localModelForeignKey`表示本表的外键  
- `parentModelLocalKey`表示父表的关联键,默认值为父表的主键(`@Primary()`修饰的键)  

以下是一个`teacher`包含多个`student`的场景下, 需要从`student`找到`teacher`的例子  
```java
package gaarason.database.test.models.relation.pojo;

import gaarason.database.eloquent.annotation.*;
import gaarason.database.test.models.relation.model.RelationshipStudentTeacherModel;
import gaarason.database.test.models.relation.model.TeacherModel;
import lombok.Data;

import java.io.Serializable;

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

    @BelongsTo(localModelForeignKey = "teacher_id", parentModelLocalKey = "id")
    private Teacher teacher;

}

```

### 多对多

`@BelongsToMany()` 其中包含5个属性:  
- `relationModel`表示`关系表`的模型  
- `localModelLocalKey`表示`本表`中`关联键`  
- `foreignKeyForLocalModel`表示`关系表`中`关联本表的外键`   
- `foreignKeyForTargetModel`表示`关系表`中`关联目标表的外键`  
- `targetModelLocalKey`表示`目标表`中`关联键` 

以下是一个`teacher`包含多个`student`,同时, 一个`student`包含多个`teacher`的场景, 关系表使用`relationship_student_teacher`    
```java
package gaarason.database.test.models.relation.pojo;

import gaarason.database.eloquent.annotation.*;
import gaarason.database.test.models.relation.model.RelationshipStudentTeacherModel;
import gaarason.database.test.models.relation.model.TeacherModel;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

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

    @Column(name = "is_deleted")
    private Boolean isDeleted;

    @BelongsToMany(relationModel = RelationshipStudentTeacherModel.class,
            foreignKeyForLocalModel = "teacher_id", foreignKeyForTargetModel = "student_id", localModelLocalKey = "id",
            targetModelLocalKey = "id")
    private List<Student> students;

}

```
以上是`student`维度的建立, `teacher`维度的类似, 暂略  

## 关联查询

当使用`firstOrFail()`等方法获取查询结果时, 可以通过`with()`方法声明需要关联的属性(需要事先在实体对象`entity`中定义)  
由于 Eloquent 所有关联关系都是通过实体对象`entity`的属性定义的，所以只有当用`toObject()`或者`toObjectList()`转化为实体对象`entity`时, 他们的定义才会生效

### 关联方法

`Record::with(String column, GenerateSqlPart builderClosure, RelationshipRecordWith recordClosure)` 与 `RecordList::with()` 与 `Builer::with()` 方法签名类似, 接受3个参数 
 
- `column`希望执行关联的属性名(非数据库字段), 可以使用`.`快捷指定下级  
- `builderClosure`所关联的Model的查询构造器约束  
- `recordClosure`所关联的Model的再一级关联, 可以指定下级   

下面是一些例子, 基本都可以在`database-core-test`模块的单元测试中找到  

#### 示例一对一
```java
// select * from student limit 1
// select * from teacher where id in (?)
Student student = studentModel.newQuery().firstOrFail().with("teacher").toObject();

```
#### 示例多级


```java
// 多级简单一对一
// select * from student limit 1
// select * from teacher where id in (?)
// select * from pet where id in (?)
Student student = studentModel.newQuery().firstOrFail().with("teacher.pet").toObject();
```
#### 示例筛选

```java
// 多级简单一对一包含筛选
// select * from student limit 1
// select * from teacher where id in (?) and age > 32
Student student = studentModel.newQuery().firstOrFail().with("teacher", bulider -> bulider.where("age",">","32")).toObject();

```
#### 示例无线级筛选

```java
// select * from `student` limit 1
// select * from `teacher` where `id`in("6")
// select * from `student` where `teacher_id`in("6")
// select * from `student` where `id`in("2","3") and `teacher_id`in("6")
Student student = studentModel.newQuery().firstOrFail().with("teacher", builder -> builder, 
    record -> record.with("students", builder -> builder,
        record1 -> record1.with("teacher", builder -> builder, 
            record2 -> record2.with("students",builder -> builder.whereIn("id", "3", "2"), 
                record3 -> record3.with("teacher"))))).toObject();

```
#### 示例混合场景

```java
// select * from `student` limit 1
// select * from `teacher` where `id`in("6")
// select * from `student` where `teacher_id`in("6")
// select * from `relationship_student_teacher` where `student_id`in("1","2","3","4")
// select * from `teacher` where `id`in("1","2","6")
// select * from `student` where `teacher_id`in("1","2","6")
// select * from `teacher` where `id`in("1","2","6")
// select * from `student` where `id`in("2","3") and `teacher_id`in("1","2","6")
Student student = studentModel.newQuery().firstOrFail().with("teacher.students.relationshipStudentTeachers", builder -> builder, 
    record -> record.with("teacher.students.teacher.students.teacher.students",builder -> builder.whereIn("id", "3", "2"), record2 -> record2.with("teacher"))).toObject();

```
#### 示例分页

```java
// select count(*) as '7f9a1374-22fe-461e-8b80-66509c2ff7a6' from `student` limit 1
// select * from `student` order by `id` asc limit 0,4
// select * from `relationship_student_teacher` where `student_id`in("1","2","3","4")
// select * from `teacher` where `id`in("1","2","6")
// select * from `relationship_student_teacher` where `teacher_id`in("1","2","6") order by `student_id` asc
// select * from `student` where `id`in("1","2","3","4","5","6","7","8","9","10")
Paginate<Student> paginate = studentModel.newQuery().orderBy("id").with("relationshipStudentTeachers.teacher.relationshipStudentTeachers",
     builder -> builder.orderBy("student_id"), record2 -> record2.with("student")).paginate(1, 4);

```
#### 示例中间表数据查询

在定义 entity 时, 除了通过 @BelongsToMany 注解定义与目标表的多堆多关系时, 还可以通过 @HasOneOrMany 定义与中间表的一对多关系  
在查询中间表时, 直接 with 对应的一对多关系即可, 并且在同时 with 对应的多对多关系时, 不会产生额外的查询  

```java
studentModel..newQuery().with("teachers").with("relation").get();

```

## 更新关系
 
- 处理多对多关联的时候，Eloquent 还提供了一些额外的辅助函数使得处理关联模型变得更加方便。  
- 这些关系的都需要在 Entity 中进行声明, 并在 Record 中使用。  
- 在 Record 中使用时, 需要先用`bind()`指明要处理的关系(属性名)   
- 需要注意的是一下的 4 类操作, 均可在全部 3 类关系上使用, 但是中间表数据插入仅对`@BelongsToMany`关系生效   
- 4 类操作在使用集合作为参数时, 参数代表的含义是主键集合(并不是关系键, 程序会根据注解中的声明找到真正的关系键)   


### 附加关系 

我们假定一个用户可能有多个角色，同时一个角色属于多个用户，要通过在连接模型的中间表中插入记录附加角色到用户上，可以使用 attach 方法   
- @HasOneOrMany : 会将子表(目标表)的外键的值更新为本表的关系键值   
- @BelongsTo : 会将本表的外键的值更新为父表(目标表)的关系键值   
- @BelongsToMany : 在中间表中新增记录, 2个外键分表指向本表的关系键与目标表的关系键, 可以指定附加的字段   

```java

Record<User, int> userRecord = UserModel.findOrFail(1);

// 以下2种写法等价, 返回受影响的行数
userRecord.bind("roles").attach(RoleModel.findMany(1,2));
userRecord.bind("roles").attach(Arrays.asList(1, 2));

// 附加关联关系到模型，还可以以MAP形式传递额外被插入数据到中间表
// 以下2种写法等价
HashMap<String, Object> map = new HashMap<>();
map.put("note", note);
userRecord.bind("roles").attach(RoleModel.findMany(1,2), map);
userRecord.bind("roles").attach(Arrays.asList(1, 2), map);

```

### 解除关系 

当然，有时候有必要从用户中移除角色，要移除一个关联记录，使用 detach 方法。 
- @HasOneOrMany : 会将子表(目标表)的外键的值更新为默认值(String则为"", integer则为"0")  
- @BelongsTo : 会将本表的外键的值更新为默认值(String则为"", integer则为"0")   
- @BelongsToMany : 在中间表中移除相应的记录, 但是，两个模型在数据库中都保持不变  

```java

Record<User, int> userRecord = UserModel.findOrFail(1);

// 以下2种写法等价, 返回受影响的行数
userRecord.bind("roles").detach(RoleModel.findMany(1,2));
userRecord.bind("roles").detach(1,2);

```

### 同步关系 

有时候有要将用户更新到指定的角色, 任何不在指定范围对应记录将会移除, 使用 sync 方法。 
- @HasOneOrMany : 针对每个范围内的值, 将会调用 `attach` 与 `attach`  
- @BelongsTo : 针对每个范围内的值, 将会调用 `attach` 与 `attach`  
- @BelongsToMany : 针对每个范围内的值, 将会调用 `attach` 与 `attach`，两个模型在数据库中都保持不变, 可以指定附加的字段在增加关系时生效   

```java

Record<User, int> userRecord = UserModel.findOrFail(1);

// 以下2种写法等价, 返回受影响的行数
userRecord.bind("roles").sync(RoleModel.findMany(1,2));
userRecord.bind("roles").sync(1,2);

```

### 切换关系 

多对多关联还提供了一个 toggle 方法用于切换给定 ID 的附加状态，如果给定ID当前被附加，则取消附加，类似的，如果当前没有附加，则附加, 使用 toggle 方法。  
- @HasOneOrMany : 针对每个范围内的值, 将会调用 `attach` 与 `attach`   
- @BelongsTo : 针对每个范围内的值, 将会调用 `attach` 与 `attach`   
- @BelongsToMany : 针对每个范围内的值, 将会调用 `attach` 与 `attach`，两个模型在数据库中都保持不变, 可以指定附加的字段在增加关系时生效   

```java

Record<User, int> userRecord = UserModel.findOrFail(1);

// 以下2种写法等价, 返回受影响的行数
userRecord.bind("roles").toggle(RoleModel.findMany(1,2));
userRecord.bind("roles").toggle(1,2);

```