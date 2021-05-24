# database
Eloquent ORM for Java
## 目录
* [注册bean](/document/bean.md)
* [数据映射](/document/mapping.md)
* [数据模型](/document/model.md)
* [查询结果集](/document/record.md)
* [查询构造器](/document/query.md)
* [生成代码](/document/generate.md)
* [关联关系](/document/relationship.md)
* [版本信息](/document/version.md)
    * [版本规范](#版本规范)
    * [历史版本](#历史版本)
    * [版本升级指引](#版本升级指引)
        * [1.0.x->2.0.x](#1.0.x->2.0.x)
## 版本规范
参照 a.b.c 方式, 例如 1.0.11 版本  
主版本号 a ：第一个数字，产品改动较大，一般无法向前兼容（要看具体项目）  
子版本号 b ：第二个数字，增加了新功能，一般情况下向前兼容, 可能存在小部分不兼容情况   
修正版本号 c ：第三个数字，修复 BUG 或 添加新功能，向前兼容   

## 历史版本

|版本|相关版本|升级描述|暂时兼容|不再兼容|
|----|----|----|----|----|
|2.3.6|com.alibaba:druid:1.2.5,spring-boot-dependencies:2.4.3|1.增加mysql中的`force index`与`ignore index`|||
|2.3.5|com.alibaba:druid:1.2.5,spring-boot-dependencies:2.4.3|1.增加count/max/min/avg/sum对group的兼容, 2.增加from对子查询的支持|||


## 版本升级指引
### 1.0.x->2.0.x
不兼容
- `gaarason.database.eloquent.Model<T>` 升级为 `gaarason.database.eloquent.Model<T, K>`, 
其中T为实体类, K为实体类中的主键类型(eg: Long)
- `gaarason.database.query.Builder<T>` 升级为 `gaarason.database.query.Builder<T, K>`
- `gaarason.database.eloquent.Record<T>` 升级为 `gaarason.database.eloquent.Record<T, K>`
- `gaarason.database.eloquent.RecordList<T>` 升级为 `gaarason.database.eloquent.RecordList<T, K>`
- `gaarason.database.contract.model.Query<T> :: Record<T> findOrFail(String id)` 升级为 `gaarason.database.contract.model.Query<T, K> :: Record<T, K> findOrFail(K id)`
- `gaarason.database.contract.model.Query<T> :: Record<T> find(String id)` 升级为 `gaarason.database.contract.model.Query<T, K> :: Record<T, K> find(K id)`
- `gaarason.database.contract.builder.Transaction<T> :: boolean transaction(Runnable runnable, int maxAttempts)`  升级为 `gaarason.database.contract.builder.Transaction<T> :: boolean transaction(Runnable runnable, int maxAttempts, boolean throwException)`

有影响
- ORM新增,现在会赋值数据库自增主键到原对象

新增加
- 新提供`K insertGetId()`/`K insertGetId(T entity)`/`K insertGetIdOrFail()`/`K insertGetIdOrFail(T entity)`/`List<K> insertGetIds(List<T> entityList)`

### 2.0.x->2.1.x

新增加
- 新提供关联关系相关注解`@HasOneOrMany()`,`@BelongsTo()`,`@BelongsToMany()`
- 新提供关联关系相关执行`Record::with(String column, GenerateSqlPart builderClosure, RelationshipRecordWith recordClosure)` 与 `RecordList::with()` 与 `Builer::with()` 方法签名类似, 接受3个参数 

