# database
Eloquent ORM for Java
## 目录
* [注册bean](/document/bean.md)
* [数据映射](/document/mapping.md)
* [数据模型](/document/model.md)
* [查询结果集](/document/record.md)
* [查询构造器](/document/query.md)
* [反向生成代码](/document/generate.md)


- 如果希望以如下的方式在程序中查询数据, 那么这个项目不会让你失望 !
```java
// 查询id为4的一条数据
Student student = studentModel.find("4").toObject();

// select id,name from student where id=3 or(age>11 and id=7 and(id between 4 and 10 and age>11))
List<Student> Students = studentModel.newQuery().where("id", "3").orWhere(
    builder -> builder.where("age", ">", "11").where("id", "7").andWhere(
        builder2 -> builder2.whereBetween("id", "4", "10").where("age", ">", "11")
    )
).select("id", "name").get().toObjectList();
```