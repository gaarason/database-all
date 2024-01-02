# database

Eloquent ORM for Java

## 目录

* [注册配置 Configuration](/document/bean.md)
* [数据映射 Mapping](/document/mapping.md)
* [数据模型 Model](/document/model.md)
* [查询结果集 Record](/document/record.md)
* [查询构造器 Query Builder](/document/query.md)
* [关联关系 Relationship](/document/relationship.md)
    * [总览](#总览)
    * [关系定义](#关系定义)
        * [常规](#常规)
            * [一对一](#一对一)
            * [一对多](#一对多)
            * [反向一对多/一对一](#反向一对多/一对一)
            * [多对多](#多对多)
        * [多态](#多态)
            * [多态一对一](#多态一对一)
            * [多态一对多](#多态一对多)
            * [多态反向一对多/一对一](#多态反向一对多/一对一)
            * [多态多对多](#多态多对多)
        * [自定义关系](#自定义关系)
            * [自定义注解](#自定义注解)
            * [自定义注解解析器](#自定义注解解析器)
            * [使用自定义注解](#使用自定义注解)
    * [关联查询](#关联查询)
        * [关联方法](#关联方法)
            * [示例一对一](#示例一对一)
            * [示例多级](#示例多级)
            * [示例筛选](#示例筛选)
            * [示例无线级筛选](#示例无线级筛选)
            * [示例混合场景](#示例混合场景)
            * [示例分页](#示例分页)
    * [关联聚合查询](#关联聚合查询)
        * [withCount](#withCount)
        * [其他聚合函数](#其他聚合函数)
            * [withMax](#withMax)
            * [withMin](#withMin)
            * [withAvg](#withAvg)
            * [withSum](#withSum)
    * [关联反向筛选](#关联反向筛选)
        * [whereHas/whereNotHas](#whereHas/whereNotHas)
    * [更新关系](#更新关系)
        * [附加关系](#附加关系)
        * [解除关系](#解除关系)
        * [同步关系](#同步关系)
        * [切换关系](#切换关系)
* [生成代码 Generate](/document/generate.md)
* [GraalVM](/document/graalvm.md)
* [版本信息 Version](/document/version.md)

## 总览

数据表经常要与其它表做关联，比如一篇博客文章可能有很多评论，或者一个订单会被关联到下单用户   
Eloquent 让组织和处理这些关联关系 Relationship变得简单，并且支持多种不同类型的关联关系 Relationship，更重要的是会进行查询优化，这点在多层级关系的情况下尤其明显

## 关系定义

通过在`entity`中声明对应的属性, 并在属性上使用相关注解`@HasOneOrMany()`,`@BelongsTo()`,`@BelongsToMany()`标记    
所有注解在包 `gaarason.database.eloquent.annotation.*` 中   
现在, 不再需要指定目标模型, 程序会根据字段类型`entity`的找到正确的目标模型

**重要：所有`对应的关系键`的java类型必须严格一致**

### 常规

最常见的关系场景

#### 一对一

`@HasOneOrMany()` 其中包含2个属性:

- `sonModelForeignKey`表示子表的外键
- `localModelLocalKey`表示本表的关联键, 默认值为本表的主键(`@Primary()`修饰的键)

以下是一个`teacher`包含一个`pet`(宠物)的例子
```
teacher
    id - integer

pet
    id - integer
    master_id - integer
```
```java
public class Teacher implements Serializable {
    // ...

    // 省略了`localModelLocalKey`, 表示本表关系键为主键(`@Primary()`修饰的键)
    @HasOneOrMany(sonModelForeignKey = "master_id")
    private Pet pet;
}
```

#### 一对多

同样使用`@HasOneOrMany()`注解, 用法也是一致的, 要注意的是使用此注解的属性需要是`List<F>`/`F[]`/`ArrayList<F>`/`LinkedHashSet<F>`/`LinkedList<F>`
/`Set<F>`类型

以下是一个`teacher`包含多个`student`的例子
```
teacher
    id - integer

student
    id - integer
    teacher_id - integer
```
```java
public class Teacher implements Serializable {
    // ...

    // 省略了`localModelLocalKey`, 表示本表关系键为主键(`@Primary()`修饰的键)
    @HasOneOrMany(sonModelForeignKey = "teacher_id")
    private List<Student> students;
}
```

#### 反向一对多/一对一

`@BelongsTo()` 其中包含2个属性:

- `localModelForeignKey`表示本表的外键
- `parentModelLocalKey`表示父表的关联键, 默认值为父表的主键(`@Primary()`修饰的键)

以下是一个`teacher`包含多个`student`的场景下, 需要从`student`找到`teacher`的例子
```
teacher
    id - integer

student
    id - integer
    teacher_id - integer
```
```java
public class Student implements Serializable {
    // ...

    // 省略了`parentModelLocalKey`, 表示父表的关联键为父表主键(`@Primary()`修饰的键)
    @BelongsTo(localModelForeignKey = "teacher_id")
    private Teacher teacher;
}
```

#### 多对多

`@BelongsToMany()` 其中包含5个属性:

- `relationModel`表示`关系表`的模型
- `localModelLocalKey`表示`本表`中`关联键`, 默认值为`本表`的主键(`@Primary()`修饰的键)
- `foreignKeyForLocalModel`表示`关系表`中`关联本表的外键`
- `foreignKeyForTargetModel`表示`关系表`中`关联目标表的外键`
- `targetModelLocalKey`表示`目标表`中`关联键`, 默认值为`目标表`的主键(`@Primary()`修饰的键)

- 使用此注解的属性需要是`List<F>`/`F[]`/`ArrayList<F>`/`LinkedHashSet<F>`/`LinkedList<F>`/`Set<F>`类型

以下是一个`teacher`包含多个`student`,同时, 一个`student`包含多个`teacher`的场景, 关系表使用`relationship_student_teacher`
```
teacher
    id - integer

student
    id - integer
    
relationship_student_teacher
    teacher_id - integer
    student_id - integer
```

```java
public class Student implements Serializable {
    // ...

    // 省略了`localModelLocalKey`, 表示`本表`中`关联键`, 默认值为`本表`的主键(`@Primary()`修饰的键)
    // 省略了`targetModelLocalKey`, 表示`目标表`中`关联键`, 默认值为`目标表`的主键(`@Primary()`修饰的键)
    // 当上述均成立时, 关系成立
    @BelongsToMany(relationModel = RelationshipStudentTeacherModel.class,
            foreignKeyForLocalModel = "teacher_id", foreignKeyForTargetModel = "student_id")
    private List<Student> students;
}
```

以上是`student`维度的建立, `teacher`维度的类似, 暂略

### 多态

灵活的关系场景  
多态关联允许目标模型借助单个关联从属于多个模型    
例如，你正在构建一个允许用户共享博客文章和视频的应用程序，其中 Comment 模型可能**同时**从属于 Post 和 Video 模型, 甚至包括他自己 Comment。
  
用法上, 相比较于`常规`关系, 复用了对应的注解, 但在注解中增加了额外的`多态属性`, 用于指明其多态的规则实现  

#### 多态一对一

`@HasOneOrMany()` 其中包含2个多态属性:

- `sonModelMorphKey`表示`子表`中的`多态类型键`
- `sonModelMorphValue`表示`子表`中的`多态类型键`的值, 默认值为`本表`的表名

以下是`Comment`同时从属于 `Post` 和 他自己的场景
```
post
    id - integer

comment
    id - integer
    p_type - string
    p_id - integer
```
以下是一个`Post`包含一个`Comment`的场景的定义

```java
public class Post extends BaseEntity {
    // ...

    // 省略了`sonModelMorphValue`, 表示当 p_type 的值为 Post的表名时, 关系成立
    @HasOneOrMany(sonModelForeignKey = "p_id", sonModelMorphKey = "p_type")
    private Comment comment;
}
```
以下是一个`Comment`包含一个`Comment`的场景的定义

```java
public class Comment extends BaseEntity {
    // ...

    // 省略了`sonModelMorphValue`, 表示当 p_type 的值为 Comment的表名时, 关系成立
    @HasOneOrMany(sonModelForeignKey = "p_id", sonModelMorphKey = "p_type")
    private Comment comment;
}
```

#### 多态一对多

同样使用`@HasOneOrMany()`注解, 用法也是一致的, 要注意的是使用此注解的属性需要是`List<F>`/`F[]`/`ArrayList<F>`/`LinkedHashSet<F>`/`LinkedList<F>`
/`Set<F>`类型


以下是`Comment`同时从属于 `Post` 和 他自己的场景
```
post
    id - integer

comment
    id - integer
    p_type - string
    p_id - integer
```
以下是一个`Post`包含多个`Comment`的场景的定义

```java
public class Post extends BaseEntity {
    // ...

    // 省略了`sonModelMorphValue`, 表示当 p_type 的值为 Post的表名时, 关系成立
    @HasOneOrMany(sonModelForeignKey = "p_id", sonModelMorphKey = "p_type")
    private List<Comment> comments;
}
```
以下是一个`Comment`包含多个`Comment`的场景的定义

```java
public class Comment extends BaseEntity {
    // ...

    // 省略了`sonModelMorphValue`, 表示当 p_type 的值为 Comment的表名时, 关系成立
    @HasOneOrMany(sonModelForeignKey = "p_id", sonModelMorphKey = "p_type")
    private List<Comment> comments;
}
```
#### 多态反向一对多/一对一

`@BelongsTo()` 其中包含2个多态属性:

- `localModelMorphKey`表示`本表`中的`多态类型键`
- `localModelMorphValue`表示`本表`中的`多态类型键`的值, 默认值为`父表`的表名

以下是`Comment`同时从属于 `Post` 和 他自己的场景
```
post
    id - integer

comment
    id - integer
    p_type - string
    p_id - integer
```

以下是一个`Comment`从属与`Comment`以及`Post`的场景的定义

```java
public class Comment extends BaseEntity {
    // ...

    // 省略了`localModelMorphValue`, 表示当 p_type 的值为 Post的表名时, 关系成立
    @BelongsTo(localModelForeignKey = "p_id", localModelMorphKey = "p_type")
    private Post post;

    // 省略了`localModelMorphValue`, 表示当 p_type 的值为 Comment的表名时, 关系成立
    @BelongsTo(localModelForeignKey = "p_id", localModelMorphKey = "p_type")
    private Comment pcomment;
}
```

#### 多态多对多

`@BelongsToMany()` 其中包含4个多态属性:

- `morphKeyForLocalModel`表示`关系表`中的`本表`的`多态类型键`
- `morphValueForLocalModel`表示`关系表`中的`本表`的`多态类型键`的值, 默认值为`本表`的表名
- `morphKeyForTargetModel`表示`关系表`中的`目标表`的`多态类型键`
- `morphValueForTargetModel``关系表`中的`目标表`的`多态类型键`的值, 默认值为`目标表`的表名

以下是`Comment`和`Post`同时与`Image`存在多对多关系的场景, 其中`Relation`为中间表, 用于实现多态
```
post
    id - integer

comment
    id - integer

image
    id - integer
    
relation
    relation_one_type - string
    relation_one_value - integer
    relation_two_type - string
    relation_two_value - integer
```

以下是一个`Post`与`Image`多对多关系的定义

```java
public class Post extends BaseEntity {
    // ...

    // 省略了`morphValueForLocalModel`, 表示当 relation_one_value 的值为 Post的表名时, 和本表(post)关系成立
    // 省略了`morphValueForTargetModel`, 表示当 relation_two_value 的值为 Image的表名时, 和目标表(image)关系成立
    // 当上述均成立时, 关系成立
    @BelongsToMany(relationModel = SuperRelation.Model.class, foreignKeyForLocalModel = "relation_one_value", foreignKeyForTargetModel = "relation_two_value",
        morphKeyForLocalModel = "relation_one_type", morphKeyForTargetModel = "relation_two_type")
    private List<Image> imagesWithMorph;

}
```

以下是一个`Comment`与`Image`多对多关系的定义

```java
public class Comment extends BaseEntity {
    // ...

    // 省略了`morphValueForLocalModel`, 表示当 relation_one_value 的值为 Comment的表名时, 和本表(comment)关系成立
    // 省略了`morphValueForTargetModel`, 表示当 relation_two_value 的值为 Image的表名时, 和目标表(image)关系成立
    // 当上述均成立时, 关系成立
    @BelongsToMany(relationModel = SuperRelation.Model.class, foreignKeyForLocalModel = "relation_one_value", foreignKeyForTargetModel = "relation_two_value",
        morphKeyForLocalModel = "relation_one_type", morphKeyForTargetModel = "relation_two_type")
    private List<Image> imagesWithMorph;

}
```
以下是一个`Image`同时与`Comment`以及`Post`多对多关系的定义

```java
public class Image extends BaseEntity {
    // ...

    // 省略了`morphValueForLocalModel`, 表示当 relation_two_value 的值为 Image的表名时, 和本表(image)关系成立
    // 省略了`morphValueForTargetModel`, 表示当 relation_one_value 的值为 Post的表名时, 和目标表(post)关系成立
    // 当上述均成立时, 关系成立
    @BelongsToMany(relationModel = SuperRelation.Model.class, foreignKeyForLocalModel = "relation_two_value", foreignKeyForTargetModel = "relation_one_value",
        morphKeyForLocalModel = "relation_two_type", morphKeyForTargetModel = "relation_one_type")
    private List<Post> posts;

    // 省略了`morphValueForLocalModel`, 表示当 relation_two_value 的值为 Image的表名时, 和本表(image)关系成立
    // 省略了`morphValueForTargetModel`, 表示当 relation_one_value 的值为 Comment的表名时, 和目标表(comment)关系成立
    // 当上述均成立时, 关系成立
    @BelongsToMany(relationModel = SuperRelation.Model.class, foreignKeyForLocalModel = "relation_two_value", foreignKeyForTargetModel = "relation_one_value",
        morphKeyForLocalModel = "relation_two_type", morphKeyForTargetModel = "relation_one_type")
    private List<Comment> comments;
}
```
以下是中间表`SuperRelation`以及其`model`的定义 (普通的定义)
```java
public class SuperRelation extends BaseEntity {
    // ...
    
    @Column(name = "relation_one_type", length = 200L)
    private String relationOneType;

    @Column(name = "relation_one_value", unsigned = true)
    private Long relationOneValue;

    @Column(name = "relation_two_type", length = 200L)
    private String relationTwoType;

    @Column(name = "relation_two_value", unsigned = true)
    private Long relationTwoValue;

    public static class Model extends BaseEntity.BaseModel<SuperRelation, Long> {

    }
}
```

### 自定义关系

用于自定义不同的数据模型之间的关系

#### 自定义注解

自定义的注解可以和预置的注解一样使用

```java

@Documented
@Inherited
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
// 使用 @Relation 标注这是一个关联关系的注解, 并指明其解析器
@Relation(HasOneQueryRelation.class)
public @interface HasOneCustom {
    /**
     * `子表`中的`关联本表的外键`
     * @return `子表`中的`关联本表的外键`
     */
    String sonModelForeignKey();

    /**
     * `本表`中的`关联键`, 默认值为`本表`的主键(`@Primary()`修饰的键)
     * @return `本表`中的`关联键`
     */
    String localModelLocalKey() default "";

}

```

#### 自定义注解解析器

- 实现 `RelationSubQuery` 的各个方法, 建议参考预置的解析器, 这部分的逻辑比较复杂与繁琐
- 提供此构造函数 `public constructor(Field field, ModelShadowProvider modelShadowProvider, Model<?, ?> model)`
```java
public static class HasOneQueryRelation extends BaseRelationSubQuery implements RelationSubQuery {

    // 提供此构造函数
    
    public HasOneQueryRelation(Field field, ModelShadowProvider modelShadowProvider, Model<?, ?> model) {
        super(modelShadowProvider, model);
    }
    
    
    // ... 实现 RelationSubQuery 的各个方法
}
```

#### 使用自定义注解
- 在实体中, 和预置的注解一样的使用方式
```java
public class Entity implements Serializable {
    
    // .. 其他数据库字段

    // 和预置的注解一样的使用方式
    @HasOneCustom(sonModelForeignKey = "student_id")
    private RelationshipStudentTeacher relationshipStudentTeacher;
}

```

## 关联查询

当使用`firstOrFail()`等方法获取查询结果时, 可以通过`with()`方法声明需要关联的属性(需要事先在实体对象`entity`中定义)  
由于 Eloquent 所有关联关系都是通过实体对象`entity`的属性定义的，所以只有当用`toObject()`或者`toObjectList()`转化为实体对象`entity`时, 他们的定义才会生效

### 关联方法

`Record::with(String column, GenerateSqlPart builderClosure, RelationshipRecordWith recordClosure)`
与 `RecordList::with()` 与 `Builer::with()` 方法签名类似, 接受3个参数

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

## 关联聚合查询

- 有时你可能需要计算给定关系的相关模型的数量, 或者仅仅想知道其中最大的某项是什么, 而不实际加载模型
- 针对于一对一/一对多/反向一对一/多对多, 以及其对应的多态形式的所有关联关系, 均进行了支持

### withCount
- 计算指定关联关系的数量, 并将结果拖地在实体上
- 默认的属性是`{relationFieldName}Count`
```java
// 在实体中定义用于接受结果的属性
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Table(name = "teacher")
public class Teacher extends BaseEntity implements Serializable {
    // ... 省略其他字段
    
    @HasOneOrMany(sonModelForeignKey = "teacher_id")
    private List<Student> students;


    // ---------------- -----------------//
    @Column(inDatabase = false)
    private Long studentsCount;
}

```
- withCount(relationFieldName)
```java
// 统计id为 1,2,6的三位老师, 分别有几个学生
List<Teacher> teachers = teacherModel.newQuery()
            .whereIn(Teacher::getId, 1, 2, 6)
            .orderBy(Teacher::getId)
            .withCount(Teacher::getStudents)
            .get()
            .toObjectList();
// 查看结果
teachers.get(0).getStudentsCount()
```
- withCount(relationFieldName, 关系表中的统计列, 结果落地的属性)
```java
// 统计id为 1,2,6的三位老师, 的学生们拥有id的数量, 并落地在 studentCount 属性上
List<Teacher> teachers = teacherModel.newQuery()
            .whereIn(Teacher::getId, 1, 2, 6)
            .orderBy(Teacher::getId)
            .withCount(Teacher::getStudents, Student::getId, Teacher::getStudentsCount)
            .get()
            .toObjectList();
// 查看结果
teachers.get(0).getStudentsCount()
```
- withCount(relationFieldName, 关系表中的统计列, 自定义查询 ,结果落地的属性)
```java
// 统计id为 1,2,6的三位老师的, sex=2的学生们拥有id的数量, 并落地在 studentsCount 属性上
List<Teacher> teachers = teacherModel.newQuery()
            .whereIn(Teacher::getId, 1, 2, 6)
            .orderBy(Teacher::getId)
            .withCount(Teacher::getStudents, Student::getId, builder -> builder.where(Student::getSex, 2), Teacher::getStudentsCount)
            .get()
            .toObjectList();
// 查看结果
teachers.get(0).getStudentsCount()
```

### 其他聚合函数
- `withMax`/`withMin`/`withAvg`/`withSum`
- 计算指定关联关系的指定列的`统计`, 并将结果拖地在实体上
- 默认的属性是`{relationFieldName}{function}{column}`

#### withMax

- 计算指定关联关系的指定列的最大值, 并将结果拖地在实体上
- 默认的属性是`{relationFieldName}Max{column}`

```java
// 在实体中定义用于接受结果的属性
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Table(name = "teacher")
public class Teacher extends BaseEntity implements Serializable {
    // ... 省略其他字段

    @BelongsToMany(relationModel = RelationshipStudentTeacherModel.class, 
        foreignKeyForLocalModel = "teacher_id", foreignKeyForTargetModel = "student_id", 
        localModelLocalKey = "id", targetModelLocalKey = "id")
    private Student[] students;


    // ---------------- -----------------//
    @Column(inDatabase = false)
    private Long studentsMaxAge;
}

```
- withMax(relationFieldName, 关系表中的统计列)
```java
// 分别统计id为 1,2,6的三位老师的, 学生们中最大的age, 并落地在 studentsMaxAge 属性上
List<Teacher> teachers = teacherModel.newQuery()
    .whereIn(Teacher::getId, 1, 2, 6)
    .orderBy(Teacher::getId)
    .withMax(Teacher::getStudents, Student::getAge)
    .get()
    .toObjectList();

// 查看结果
    teachers.get(0).getStudentsMaxAge()
```


- withMax(relationFieldName, 关系表中的统计列, 结果落地的属性)
```java
// 分别统计id为 1,2,6的三位老师的, 学生们中最大的id, 并落地在 studentsMaxAge 属性上
List<Teacher> teachers = teacherModel.newQuery()
            .whereIn(Teacher::getId, 1, 2, 6)
            .orderBy(Teacher::getId)
            .withMax(Teacher::getStudents, Student::getId, Teacher::getStudentsMaxAge)
            .get()
            .toObjectList();

// 查看结果
teachers.get(0).getStudentsMaxAge()
```

- withMax(relationFieldName, 关系表中的统计列, 自定义查询, 结果落地的属性)
```java
// 分别统计id为 1,2,6的三位老师的, sex=2 的学生们中最大的id, 并落地在 studentsMaxAge 属性上
List<Teacher> teachers = teacherModel.newQuery()
            .whereIn(Teacher::getId, 1, 2, 6)
            .orderBy(Teacher::getId)
            .withMax(Teacher::getStudents, Student::getId, builder -> builder.where(Student::getSex, 2), Teacher::getStudentsMaxAge)
            .get()
            .toObjectList();

// 查看结果
teachers.get(0).getStudentsMaxAge()
```

#### withMin
略
#### withAvg
略
#### withSum
略

## 关联反向筛选
- 使用从表关系筛选主表结果
- 检索模型记录时, 你可能希望根据关系的存在限制结果. 例如, 假设要检索至少有一条评论的所有博客文章.
### whereHas/whereNotHas

- whereHas/whereNotHas(relationFieldName)
```java
// 查询所有有学生的老师
List<Teacher> teacherList = teacherModel.newQuery()
    .whereHas(Teacher::getStudentArray)
    .get()
    .toObjectList();
```

- whereHas/whereNotHas(relationFieldName, 自定义查询)
```java
// 查询所有的老师, 这些老师的学生们都不大于16岁
List<Teacher> teacherList = teacherModel.newQuery()
    .whereNotHas(Teacher::getStudentArray, builder -> builder.where(Student::getAge, ">", "16"))
    .orderBy(Teacher::getId)
    .get()
    .toObjectList();

// 查询所有学生, 这些学生的有男老师
List<Student> students = studentModel.newQuery()
    .whereHas("teacher", builder -> builder.where("sex", 1))
    .orderBy(Student::getId)
    .get()
    .toObjectList();
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

- @HasOneOrMany : 针对每个范围内的值, 将会调用 `attach` 与 `detach`
- @BelongsTo : 针对每个范围内的值, 将会调用 `attach` 与 `detach`
- @BelongsToMany : 针对每个范围内的值, 将会调用 `attach` 与 `detach`，两个模型在数据库中都保持不变, 可以指定附加的字段在增加关系时生效

```java

Record<User, int> userRecord = UserModel.findOrFail(1);

// 以下2种写法等价, 返回受影响的行数
userRecord.bind("roles").sync(RoleModel.findMany(1,2));
userRecord.bind("roles").sync(1,2);

```

### 切换关系

多对多关联还提供了一个 toggle 方法用于切换给定 ID 的附加状态，如果给定ID当前被附加，则取消附加，类似的，如果当前没有附加，则附加, 使用 toggle 方法。

- @HasOneOrMany : 针对每个范围内的值, 将会调用 `attach` 与 `detach`
- @BelongsTo : 针对每个范围内的值, 将会调用 `attach` 与 `detach`
- @BelongsToMany : 针对每个范围内的值, 将会调用 `attach` 与 `detach`，两个模型在数据库中都保持不变, 可以指定附加的字段在增加关系时生效

```java

Record<User, int> userRecord = UserModel.findOrFail(1);

// 以下2种写法等价, 返回受影响的行数
userRecord.bind("roles").toggle(RoleModel.findMany(1,2));
userRecord.bind("roles").toggle(1,2);

```