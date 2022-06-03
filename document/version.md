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

### 3.5.1

- 对于包扫描配置, 在`springboot`下默认是`@SpringBootApplication`所在的包

### 3.5.0

- 增加对于包扫描配置的支持, 如`System.setProperty("gaarason.database.scan.packages", "you.package1,you.package2")`或者在springboot下使用注解`@GaarasonDatabaseScan("you.package1,you.package2")`

### 3.4.0

- 在查询构造器(`Builder`)中, 增加`dealChunk(num, column, chunkFunctionalInterface)`支持是用索引条件的分块查询
- 在查询构造器(`Builder`)中, 增加`firstOrderBy(closure)`支持将闭包中的排序字段添加到首部

### 3.3.1

- 在查询构造器(`Builder`)中, 使`dealChunk()`当结果集为空时, 不再进行回调
- 在查询构造器(`Builder`)中, 表达式风格的列名与属性名, 现在支持父类实体

### 3.2.0

- 在查询构造器(`Builder`)中,将原生查询的绑定参数类型由`Collection<String>`更改为`Collection<?>`
- 在模型(`Model`)中, 将事件方法`log(String sql, Collection<String> parameterList)`更改为`log(String sql, Collection<?> parameterList)`
- 在模型(`GeneralModel`)中, 实现`log.debug`
- 底层实现"SoftCache", 以替代"sun.misc.SoftCache", 增加对于高版本jdk的兼容性

### 3.1.0

- 在查询构造器(`Builder`)中增加对于表达式风格的列名与属性名的支持
- 在查询结果集(`Record`)中增加对于表达式风格的列名与属性名的支持
- 在查询结果集合(`RecordList`)中增加对于表达式风格的列名与属性名的支持

### 3.0.0

- 重写了底层的语法构造器(`Grammar`), 使其可以在任意语句下, 完成参数绑定.
- 在查询构造器(`Builder`)中增加`toSql(sqlType, closure)`/`whereRaw(sqlPart, parameters)`/`havingRaw(sqlPart, parameters)`/`orderByRaw(sqlPart)`
- 在查询构造器(`Builder`)中增加`when(condition, closure)`/`when(condition, closure, closure)`
- 在查询构造器(`Builder`)中增加`whereKeywords(value, column...)`/`whereKeywordsIgnoreNull(value, column...)`/`havingKeywords(value, column...)`/`havingKeywordsIgnoreNull(value, column...)`等方法
- 在查询构造器(`Builder`)中增加`whereMayLikeIgnoreNull(map)/whereMayLikeIgnoreNull(column, value)`/`havingMayLikeIgnoreNull(map)/havingMayLikeIgnoreNull(column, value)`
- 在查询构造器(`Builder`)中增加`andWhereIgnoreEmpty(closure)`/`orWhereIgnoreEmpty(closure)`/`andHavingIgnoreEmpty(closure)`/`orHavingIgnoreEmpty(closure)`

### 2.22.0

- 在查询构造器(`Builder`)中增加`whereMayLike`/`havingMayLike`系列执行方法`whereMayLike(column, value)`/`whereMayLike(entity)`/`whereMayLike(map)`/`havingMayLike(column, value)`/`havingMayLike(entity)`/`havingMayLike(map)`
- 在查询构造器(`Builder`)中增加`whereRaw(list)`/`havingRaw(list)`
- 增加`GaarasonAutoconfiguration`接口, 程序会自动通过包扫描, 完成加载对其子类加载, 并调用其`init()` 完成初始化
- `InstanceCreatorFunctionalInterface`接口中, 增加默认的`getOrder()`方法. 用于支持, 在调用`ContainerProvider.register()`进行优先级判断.

### 2.21.0

- 在查询构造器(`Builder`)中增加`whereLike`/`havingLike`系列执行方法`whereLike(column, value)`/`whereLike(entity)`/`whereLike(map)`/`havingLike(column, value)`/`havingLike(entity)`/`havingLike(map)`
- 修复查询构造器(`Builder`)中使用`where(entity)`/`having(entity)`等方法时, 当实体`entity`中`@Column`中`insertable=false`时, 不正确的行为.

### 2.20.0

- 更改了项目的模块分布, 主要是拆分出的`database-api`模块, 以供rpc的接口模块去做依赖, 以及`database-query-*`模块, 以供拓展更多的数据库支持
- 更新了部分的依赖的版本
- 为了降低使用的门槛, 依然在`database-core`中维持了`druid`依赖, 以及`database-spring-boot-starter`中维持了`database-query-*`与`mysql-connector-java`等.

### 2.19.0

- 更改了`database-spring-boot-starter`中的配置逻辑, 通过配置中的(spring.datasource.type=**DataSource)现在可以使用任意的基本数据源了. 因此这项功能, 所以配置文件将仅支持spring data风格, 而非 druid 风格.

### 2.18.1

- 在Spring自动配置类(`GaarasonDatabaseConfiguration`)中, 对`GaarasonDataSource`增加`@Primary`

### 2.18.0

- 在查询构造器(`Builder`)中增加`join`系列执行方法`joinRaw(sqlPart)`/`join(joinType, table, joinConditions)`/`join(joinType, tempTable, alias, joinConditions)`

### 2.17.0

- `ModelShadowProvider`中增加`getByTableName(tableName)`
- `EntityUtils`中增加`entityAssignmentBySimpleMap(stringObjectMapList, entityClass)`

### 2.16.2

- 修复查询结果集(`Record`)中使用`fillEntity(entity)`等方法时,由于实体`entity`存在复杂继承时引发的错误

### 2.16.1

- 分页对象(`Paginate`)中增加`Serializable`接口

### 2.16.0

- 在查询构造器(`Builder`)中增加执行方法`simplePaginateMapStyle(currentPage, perPage)`/`paginateMapStyle(currentPage, perPage)`

### 2.15.0

- 在查询构造器(`Builder`)中增加执行方法`insertMapStyle(entityMap)`/`insertMapStyle(entityMapList)`/`insertGetIdMapStyle(entityMap)`/`insertGetIdOrFailMapStyle(entityMap)`/`insertGetIdsMapStyle(entityMapList)`/`updateMapStyle(entityMap)`

### 2.14.0

- 在实体(`Entity`)中, 优化 @Primary 注解中指定主键生成器的策略, 更方便的自定义主键生成
- 优化`SpringBoot`中的自动配置类

### 2.13.0

- 在模型(`Model`)中, 移除 `findMany(K... ids)`
- 升级包扫描依赖

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
