# database
Eloquent ORM for Java
## 目录
* [注册bean](/document/bean.md)
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
* [生成代码](/document/generate.md)
* [版本信息](/document/version.md)

## 总览

数据表经常要与其它表做关联，比如一篇博客文章可能有很多评论，或者一个订单会被关联到下单用户  
Eloquent 让组织和处理这些关联关系变得简单，并且支持多种不同类型的关联关系，更重要的是会进行查询优化，这点在多层级关系的情况下尤其明显  

## 关系定义

通过在`entity`中声明对应的属性, 并在属性上使用相关注解`@HasOneOrMany()`,`@BelongsTo()`,`@BelongsToMany()`标记    
所有注解在包 `gaarason.database.eloquent.annotations.*` 中

### 一对一

`@HasOneOrMany()` 其中包含3个属性:  
`sonModel`表示子表的模型  
`sonModelForeignKey`表示子表的外键  
`localModelLocalKey`表示本表的关联键,默认值为本表的主键(`@Primary()`修饰的键)  

以下是一个`teacher`包含一个`pet`(宠物)的例子  
```java
package gaarason.database.test.relation.data.pojo;

import gaarason.database.eloquent.annotations.*;
import gaarason.database.test.relation.data.model.RelationshipStudentTeacherModel;
import gaarason.database.test.relation.data.model.StudentModel;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
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

    // 一对一关联关系声明
    @HasOneOrMany(sonModel = PetModel.class, sonModelForeignKey = "master_id", localModelLocalKey = "id")
    private Pet pet;

}


```

### 一对多

同样使用`@HasOneOrMany()`注解, 用法也是一致的, 唯一要注意的是使用此注解的属性需要是`List<?>`类型

以下是一个`teacher`包含多个`student`的例子  
```java
package gaarason.database.test.relation.data.pojo;

import gaarason.database.eloquent.annotations.*;
import gaarason.database.test.relation.data.model.RelationshipStudentTeacherModel;
import gaarason.database.test.relation.data.model.StudentModel;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
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
    @HasOneOrMany(sonModel = StudentModel.class, sonModelForeignKey = "teacher_id", localModelLocalKey = "id")
    private List<Student> students;

}

```

### 反向一对多/一对一

`@BelongsTo()` 其中包含3个属性:  
`parentModel`表示父表的模型  
`localModelForeignKey`表示本表的外键  
`parentModelLocalKey`表示父表的关联键,默认值为父表的主键(`@Primary()`修饰的键)  

以下是一个`teacher`包含多个`student`的场景下, 需要从`student`找到`teacher`的例子  
```java
package gaarason.database.test.relation.data.pojo;

import gaarason.database.eloquent.annotations.*;
import gaarason.database.test.relation.data.model.RelationshipStudentTeacherModel;
import gaarason.database.test.relation.data.model.TeacherModel;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
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

    @Column(name = "teacher_id", unsigned = true, comment = "教师id")
    private Long teacherId;

    @Column(name = "is_deleted")
    private Boolean isDeleted;

    @BelongsTo(parentModel = TeacherModel.class, localModelForeignKey = "teacher_id", parentModelLocalKey = "id")
    private Teacher teacher;

}

```

### 多对多

`@BelongsToMany()` 其中包含5个属性:  
`targetModel`表示`目标表`的模型  
`relationModel`表示`关系表`的模型  
`localModelLocalKey`表示`本表`中`关联键`  
`foreignKeyForLocalModel`表示`关系表`中`关联本表的外键`   
`foreignKeyForTargetModel`表示`关系表`中`关联目标表的外键`  
`targetModelLocalKey`表示`目标表`中`关联键` 

以下是一个`teacher`包含多个`student`,同时, 一个`student`包含多个`teacher`的场景, 关系表使用`relationship_student_teacher`    
```java
package gaarason.database.test.relation.data.pojo;

import gaarason.database.eloquent.annotations.*;
import gaarason.database.test.relation.data.model.RelationshipStudentTeacherModel;
import gaarason.database.test.relation.data.model.TeacherModel;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
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

    @BelongsToMany(targetModel = StudentModel.class, relationModel = RelationshipStudentTeacherModel.class,
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
 
`column`希望执行关联的属性名(非数据库字段), 可以使用`.`快捷指定下级  
`builderClosure`所关联的Model的查询构造器约束  
`recordClosure`所关联的Model的再一级关联, 可以指定下级   

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