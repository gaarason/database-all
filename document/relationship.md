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
        * [多对多](#多对多)
        * [远层一对一](#远层一对一)
        * [远层一对多](#远层一对多)
        * [一对一(多态)](#一对一(多态))
        * [一对多(多态)](#一对多(多态))
        * [多对多(多态)](#多对多(多态))
    * [关联查询](#关联查询)
        * [关联方法](#关联方法)
        * [查询存在的关联关系](#查询存在的关联关系)
        * [无关联结果查询](#无关联结果查询)
        * [多态关联查询](#多态关联查询)
        * [统计关联模型](#统计关联模型)
    * [渴求式加载](#渴求式加载)
        * [带条件约束的渴求式加载](#带条件约束的渴求式加载)
        * [懒惰渴求式加载](#懒惰渴求式加载)
    * [插入更新关联模型](#插入更新关联模型)
        * [save](#save)
        * [create](#create)
        * [从属关联关系](#从属关联关系)
        * [多对多关联](#多对多关联)
    * [触发父模型时间戳更新](#触发父模型时间戳更新)
* [生成代码](/document/generate.md)
* [版本信息](/document/version.md)

## 总览

数据表经常要与其它表做关联，比如一篇博客文章可能有很多评论，或者一个订单会被关联到下单用户，Eloquent 让组织和处理这些关联关系变得简单，并且支持多种不同类型的关联关系

## 普通java对象

`gaarason.database.eloquent.Record<T, K>`通过`toObject`可以转化为对应的泛型实体  
`gaarason.database.eloquent.RecordList<T, K>`通过`toObjectList`可以转化为对应的泛型实体列表  

## 通用map对象