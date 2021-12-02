# database
Eloquent ORM for Java
## 目录
* [注册配置](/document/bean.md)
* [数据映射](/document/mapping.md)
* [数据模型](/document/model.md)
* [查询结果集](/document/record.md)
* [查询构造器](/document/query.md)
* [生成代码](/document/generate.md)
* [关联关系](/document/relationship.md)
* [版本信息](/document/version.md)
    * [版本规范](#版本规范)
    * [版本升级指引](#版本升级指引)
## 版本规范
参照 a.b.c 方式, 例如 1.0.11 版本  
主版本号 a ：第一个数字，产品改动较大，一般无法向前兼容（要看具体项目）  
子版本号 b ：第二个数字，增加了新功能，一般情况下向前兼容, 可能存在小部分不兼容情况   
修正版本号 c ：第三个数字，修复 BUG，向前兼容

## 版本升级指引

### 2.12.0

- 增强对于`ContainerProvider`的使用.

### 2.11.0

- 在模型(`Model`)中增加方法`create(entity)`
- 在查询结果集(`Record`)中增加方法`isDirty()`/`getDirty()`/`getDirtyMap()`/`isDirty(fieldName)`/`isClean()`/`isClean(fieldName)`/`getOriginal()`/`getOriginal(fieldName)`
- 在查询结果集(`RecordList`)中增加方法`avg(fieldName)`/`sum(fieldName)`/`max(fieldName)`/`min(fieldName)`/`mode(fieldName)`/`median(fieldName)`/`chunk(closure, newSize)`/`chunk(newSize)`/`chunkToMap(newSize)`/`contains(fieldName, value)`/`contains(closure)`/`count()`/`countBy(closure)`/`countBy(fieldName)`/`every(closure)`/`filter(closure)`/`filter()`/`filter(fieldName)`/`reject(closure)`/`first(closure)`/`first()`/`groupBy(closure)`/`groupBy(fieldName)`
- 在查询结果集(`RecordList`)中增加方法`implode(fieldName, delimiter)`/`implode(closure, delimiter)`/`keyBy(fieldName)`/`keyBy(closure)`/`last(closure)`/`last()`/`mapToGroups(closureKey, closureValue)`/`mapWithKeys(closureKey, closureValue)`/`pluck(fieldName)`/`pluck(fieldNameForValue, fieldNameForKey)`/`shift()`/`pop()`/`prepend(element)`/`push(element)`/`put(index, element)`/`pull(index)`/`random()`/`random(count)`/`reverse()`/`sortBy(closure, ase)`/`sortBy(closure)`/`sortBy(fieldName)`
- 在查询结果集(`RecordList`)中增加方法`sortByDesc(fieldName)`/`sortByDesc(closure) `/`splice(offset)`/`splice(offset, taken)`/`take(count)`/`unique(closure)`/`unique(fieldName)`

### 2.10.0

- 整体改进，数据库字段值为null的兼容性，但依然建议数据库字段不使用null
- 在模型(`Model`)中增加方法`findOrFail(Object id)`/`find(Object id)`/`findMany(Object... ids)`
- 在模型(`Model`)中变更方法`findMany(Collection<K> ids)` -> `findMany(Collection<Object> ids)`
- 在查询构造器(`Builder`)中增加方法`whereIgnoreNull(column, symbol, value)`/`whereIgnoreNull(column, value)`/`whereIgnoreNull(map)`/`whereInIgnoreEmpty(column, valueList)`/`whereInIgnoreEmpty(column, valueArray)`/`whereNotInIgnoreEmpty(column, valueList)`/`whereNotInIgnoreEmpty(column, valueArray)`
- 在查询构造器(`Builder`)中增加方法`havingIgnoreNull(column, symbol, value)`/`havingIgnoreNull(column, value)`/`havingIgnoreNull(map)`/`havingInIgnoreEmpty(column, valueList)`/`havingInIgnoreEmpty(column, valueArray)`/`havingNotInIgnoreEmpty(column, valueList)`/`havingNotInIgnoreEmpty(column, valueArray)`
- 在查询构造器(`Builder`)中增加方法`dataIgnoreNull(column, value)`/`dataIgnoreNull(map)`


### 2.9.0

- 在模型(`Model`)中增加方法`findOrNew(entity)`/`findByPrimaryKeyOrNew(entity)`/`findByPrimaryKeyOrCreate(entity)`/`findOrNew(conditionEntity, complementEntity)`/`updateByPrimaryKeyOrCreate(entity)`
- 在查询结果集(`Record`)中增加方法`fillEntity(entity)`

### 2.8.0

- 实体代码生成时对于数据库的时间日期等,使用LocalDateTime/LocalDate/LocalTime等java类型对应

### 2.7.0

- 在模型(`Model`)中增加方法`findOrCreate(entity)`/`findOrCreate(conditionEntity, complementEntity)`/`updateOrCreate(conditionEntity, complementEntity)`
- 在查询构造器(`Builder`)中增加方法`Builder<T, K> where(T entity)`/`Builder<T, K> where(Map<String, Object> map)`/`Builder<T, K> having(T entity)`/`Builder<T, K> having(Map<String, Object> map)`

### 2.6.0

- 在实体中增加对`java.sql.Date`/`java.sql.Time`/`java.sql.Timestamp`/`BigDecimal`/`Blob`/`Clob`类型的支持, 其中`Blob`/`Clob`仅支持查询

- 将关联关系`Bind`中部分方法， 中间表数据参数类型由`Map<String, String>`变更到`Map<String, Object>`
- 将关联关系`Bind`中部分方法， 手动指定目标表的主键， 参数类型由`String`、`Collection<String> ids`变更到`Object`、`Collection<Object> ids`

```java
int attach(Record<?, ?> targetRecord, Map<String, Object> relationDataMap);
int attach(RecordList<?, ?> targetRecords, Map<String, Object> relationDataMap);
int attach(Object id, Map<String, Object> relationDataMap);
int attach(Collection<Object> ids, Map<String, Object> relationDataMap);

int sync(Record<?, ?> targetRecord, Map<String, Object> relationDataMap);
int sync(RecordList<?, ?> targetRecords, Map<String, Object> relationDataMap);
int sync(Object id, Map<String, Object> relationDataMap);
int sync(Collection<Object> ids, Map<String, Object> relationDataMap);

int toggle(Record<?, ?> targetRecord, Map<String, Object> relationDataMap);
int toggle(RecordList<?, ?> targetRecords, Map<String, Object> relationDataMap);
int toggle(Object id, Map<String, Object> relationDataMap);
int toggle(Collection<Object> ids, Map<String, Object> relationDataMap);
```

### 2.5.0

- 将`Builder`中部分方法, 参数类型由`String`变更为`Object`
```java
Builder<T, K> where(String column, String symbol, Object value);
Builder<T, K> where(String column, Object value);
Builder<T, K> whereIn(String column, Object... valueArray);
Builder<T, K> whereNotIn(String column, Object... valueArray);
Builder<T, K> whereBetween(String column, Object min, Object max);
Builder<T, K> whereNotBetween(String column, Object min, Object max);

Builder<T, K> having(String column, String symbol, Object value);
Builder<T, K> having(String column, Object value);
Builder<T, K> havingIn(String column, Object... valueArray);
Builder<T, K> havingNotIn(String column, Object... valueArray);
Builder<T, K> havingBetween(String column, Object min, Object max);
Builder<T, K> havingNotBetween(String column, Object min, Object max);

Builder<T, K> data(String column, Object value);
Builder<T, K> data(Map<String, Object> map);
```

### 2.4.0
- 在实体中增加对java8的LocalDate/LocalDateTime/LocalTime类型的支持.


### 2.0.x->2.1.x

新增加
- 新提供关联关系相关注解`@HasOneOrMany()`,`@BelongsTo()`,`@BelongsToMany()`
- 新提供关联关系相关执行`Record::with(String column, GenerateSqlPart builderClosure, RelationshipRecordWith recordClosure)` 与 `RecordList::with()` 与 `Builer::with()` 方法签名类似, 接受3个参数

### 2.0.x->2.1.x

新增加
- 新提供关联关系相关注解`@HasOneOrMany()`,`@BelongsTo()`,`@BelongsToMany()`
- 新提供关联关系相关执行`Record::with(String column, GenerateSqlPart builderClosure, RelationshipRecordWith recordClosure)` 与 `RecordList::with()` 与 `Builer::with()` 方法签名类似, 接受3个参数


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
