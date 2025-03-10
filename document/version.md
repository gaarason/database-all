# database

Eloquent ORM for Java

## 目录

* [注册配置 Configuration](/document/bean.md)
* [数据映射 Mapping](/document/mapping.md)
* [数据模型 Model](/document/model.md)
* [查询结果集 Record](/document/record.md)
* [查询构造器 Query Builder](/document/query.md)
* [生成代码 Generate](/document/generate.md)
* [关联关系 Relationship](/document/relationship.md)
* [GraalVM](/document/graalvm.md)
* [版本信息 Version](/document/version.md)
    * [版本规范](#版本规范)
    * [版本升级指引](#版本升级指引)

## 版本规范

参照 a.b.c 方式, 例如 1.0.11 版本  
主版本号 a ：第一个数字，产品改动较大，一般无法向前兼容（要看具体项目）  
子版本号 b ：第二个数字，增加了新功能，一般情况下向前兼容, 可能存在小部分不兼容情况   
修正版本号 c ：第三个数字，修复 BUG，向前兼容

## 版本升级指引

### 6.0.0

- 更新查询构造器 builder`与`model`的泛型定义, 使之可以进行完全的自定义行为, 便于拓展
- `gaarason.database.eloquent.Model<T, K>` 升级为 `gaarason.database.eloquent.Model<B extends Builder<B, T, K>, T, K>`
- `gaarason.database.query.Builder<T, K>` 升级为 `gaarason.database.query.BuilderBuilder<B extends Builder<B, T, K>, T, K>`
- 查询构造器`builder`中, 增加`showType(new ShowType<MysqlBuilder<Entity, Long>>)`方法, 用于在个别场景下, 手动指定查询构造器的类型


### 5.5.10

- 修复不同`jdk`下的差异问题

### 5.5.9

- 关联关系相关优化, 以提高多层级时`with()`的耗时
- 关联关系相关实现类`HasOneOrManyQueryRelation`,`BelongsToQueryRelation`,`BelongsToManyQueryRelation`初始化方式调整, 是其更易被继承

### 5.5.8

- 修复在开启`软删除`的情况下, 使用含`group`的`普通分页查询`出错的问题.

### 5.5.7

- 关联关系相关实现类`HasOneOrManyQueryRelation`,`BelongsToQueryRelation`,`BelongsToManyQueryRelation`更改可见性, 是其更易被继承

### 5.5.6

- 代码优化以提高性能

### 5.5.5

- 代码优化以提高性能

### 5.5.4

- 优化对于使用关系查询(`with`)时, 对于内部`RelationGetSupport.toObjectList()`调用过多的问题, 以提高性能
- 优化对于使用关系查询(`with`)时, 手动指定目标表查询列 (`select`)的情况, 优先使用指定的`select`和必要关系键, 缺省时查询所有列

### 5.5.3

- 在`Builder`中, 集合类位操作的相关的方法, `havingBitIn()`,`havingBitNotIn()`,`havingBitStrictIn()`,`havingBitStrictNotIn()`由数据库逻辑实现, 改为数据库位操作实现


### 5.5.2

- 修复, 在`SpringBoot`中同时使用`Mybatis`时, `Mybatis`所持有的事务外的产生的的数据库链接, 无法正常释放的问题

### 5.5.1

- 修复, 在`SpringBoot`中使用事务注解时, 事务提交可能异常失败的问题
- 部分依赖升级

### 5.5.0

- 在`Builder`中, 增加了部分方法, `whereNot()`, `havingNot()`, `havingAnyLike()`, `havingAllLike()`,`whereAnyLike()`,`whereAllLike()`
- 在`Builder`中, 移除了部分方法, `havingKeywordsIgnoreNull()`, `whereKeywordsIgnoreNull()`
- 在`Builder`中, 更名了部分方法, `whereLikeIgnoreNull()` -> `whereLike()`

### 5.4.0

- 在`Builder`中, 增加了位操作的相关的方法, `havingBit()`, `havingBitNot()`,`havingBitIn()`,`havingBitNotIn()`,`havingBitStrictIn()`,`havingBitStrictNotIn()`

### 5.3.0

- 在`Builder`中, 增加了位操作的相关的方法, `whereBit()`, `whereBitNot()`,`whereBitIn()`,`whereBitNotIn()`,`whereBitStrictIn()`,`whereBitStrictNotIn()`,`dataBit()`,`dataBitIncrease()`,`dataBitDecrease()`
- 在`@Column`中, 增加 `conversion` 接口的通用位处理`FieldConversion.Bit`

### 5.2.0

- 在`Model`中, 修改了定义事件的相关的方法名 (原 `retrieved`, `creating`, `created`, `updating`, `updated`, `saving`, `saved`, `deleting`, `deleted`, `restoring`, `restored`) , 使之更不易混淆, 并在[数据模型 Model](/document/model.md#事件)中提示事件触发次序
- 在`Model`中, query 相关事件方法以`eventQuery`为方法前缀, 包含`eventQueryRetrieving`,`eventQueryRetrieved`,`eventQueryRetrieved`,`eventQueryCreating`,`eventQueryCreated`,`eventQueryUpdating`,`eventQueryUpdated`,`eventQueryDeleting`,`eventQueryDeleted`,`eventQueryRestoring`,`eventQueryRestored`
- 在`Model`中, record 相关事件方法以`recordQuery`为方法前缀, 包含`eventRecordRetrieved`,`eventRecordRetrieved`,`eventRecordCreating`,`eventRecordCreated`,`eventRecordUpdating`,`eventRecordUpdated`,`eventRecordDeleting`,`eventRecordDeleted`,`eventRecordRestoring`,`eventRecordRestored`

### 5.1.0

- 在代码生成中, 增加对 javax 以及 jakarta 的兼容

### 5.0.0

- 支持 SpringBoot3 以及 java17

### 4.9.1

- 优化

### 4.9.0

- 在`@Column`中, 对 `fill`增加可选实现, `FieldFill.CreatedTimeFill.class`/`FieldFill.UpdatedTimeFill.class`分别在 ORM 的 insert/update 时,
  对时间类型的字段进行当前时间的填充

### 4.8.2

- 增强在多个事务管理器, 同时存在时的兼容性

### 4.8.1

- 在`Builder`中, 增加`count`对于非`group`下的自定义`select`的支持, 同步影响`paginate`等分页等函数

### 4.8.0

- 支持多态关联关系 Relationship, 对于 `@hasOneOrMany`, `@belongsTo`, `@belongsToMany`增加多态属性
- 支持关联集合查询, 在`Builder`中, 增加`withCount()`, `withMax()`,`withMin()`,`withAvg()`,`withSum()`
- 支持关联反向筛选, 在`Builder`中, 增加`whereHas()`, `whereNotHas()`

### 4.7.0

- 支持自定义关联关系 Relationship, 增加`@Relation`用于标注自定义的关联关系 Relationship注解, 并指明其解析器

### 4.6.0

- 优化`@HasOneOrMany(),@BelongsTo(),@BelongsToMany()`的实现
- 关联关系 Relationship定义时, 对于复数关系, 在原本仅支持`List<F>`的基础上, 增加数据类型支持`F[]`/`ArrayList<F>`/`LinkedHashSet<F>`/`LinkedList<F>`/`Set<F>`
- 在`Builder`中, 增加`setBuilder(builder)`/`mergerBuilder(builder)`
- 接口（`Record`/`RecordList`/`Builder`）实现序列化接口，以支持 RPC 传递

### 4.5.1

- 优化`FieldConversion.EnumInteger`/`FieldConversion.EnumString`的实现
- 优化`代码生成`, 在生成的`entity`上增加`@Accessors(chain = true)`

### 4.5.0

- 在`@Column`中, 增加 `conversion` 接口的泛型约束, 并增加通用枚举处理`FieldConversion.EnumInteger`/`FieldConversion.EnumString`
- 在`Builder`中, 修改使用`entity`作为参数的操作, 在保持相同语义(获取/填充/序列化)的前提下, 不再修改(回填)传入的`entity`
- 在`Record`中, 保持使用`ORM`风格的操作依然对所持有的`entity`进行同步修改(回填)

### 4.4.0

- 在`Model`中, 增加原生异步执行`nativeQueryListAsync`,`nativeQueryAsync`,`nativeQueryOrFailAsync`,`nativeExecuteAsync`
  ,`nativeExecuteGetIdsAsync`,`nativeExecuteGetIdsAsync`
- 在`Builder`中, 增加异步闭包事务 `transactionAsync`
- 修复`Generator`中, 当对于spring环境下生成的代码有误的问题

### 4.3.1

- 代码优化

### 4.3.0

- 在`@Column` 中增加了`json`的支持, 需要手动引入下相关的`jackson`依赖

### 4.2.0

- 在`database-spring-boot-starter` 中支持了graalVM的构建, 需要手动在配置类上增加对应的注解, 详见文档

### 4.1.0

- 实体代码生成时, 增加更多一种的风格作为选择

### 4.0.0

- 在`Record`中, 增加 `saveByPrimaryKey()`, 更改`fillEntity()`返回值
- 查询结果集 Record(`RecordList`) 现在是`LinkedList`的子类, 而非之前的的`ArrayList`, 同时更改了`pop()`/`push(element)`的行为, 并移除了`prepend(element)`

- 在`Builder`中, 增加 `select(anyEntity)`, `select(anyEntityClass)`更改`where(entity)`为`where(anyEntity)`,`having(entity)`
  为`having(anyEntity)`返回值
- 在`Builder`中, 增加 `whereFind(map)`, `whereNotFind(map)`,`whereNotLike(column, value)`,`whereNotLike(anyEntity)`
  ,`whereNotLike(map)`,`whereMayNotLike(column, value)`,`whereMayNotLikeIgnoreNull(column, value)`
  ,`whereMayNotLike(anyEntity)`,
  `whereMayNotLike(map)`,`whereMayNotLikeIgnoreNull(map)`,`whereBetweenRaw()`,`whereNotBetweenRaw()` 以及having与其对应的方法
- 在`Builder`中, 更改`whereKeywordsIgnoreNull()`的行为, 重命名`whereLike()`为 `whereLikeIgnoreNull()`, 移除 更改`whereKeywords()`
- 在`@Column`中, 更改`strategy`/`insertStrategy`/`updateStrategy`/`conditionStrategy`的类型为接口类型(`FieldStrategy`)便于业务自定义实现
- 在`@Column`中, 增加 `conversion` 属性便于业务自定义实现序列化与反序列化;
- 在`Builder`中, 修改`find(id)`/`findOrFail(id)`/`insert(entity)`/`insert(list<entity>)`/`insertGetId(entity)`
  /`insertGetIdOrFail(entity)`/`insertGetIds(list<entity>)`/`update(entity)`等方法的参数类型为Object;
- 在`Builder`中, 新增 `form(entity)`;
- 现在`Container`不再是全局静态, 而是使用对象生命周期管理, 便于同个进程下多个容器之间进行隔离
- 现在`ModelShadow`不再是全局静态的, 而是使用`Container`进行管理

### 3.5.2

- 小优化

### 3.5.1

- 对于包扫描配置, 在`springboot`下默认是`@SpringBootApplication`所在的包

### 3.5.0

- 增加对于包扫描配置的支持, 如`System.setProperty("gaarason.database.scan.packages", "you.package1,you.package2")`
  或者在springboot下使用注解`@GaarasonDatabaseScan("you.package1,you.package2")`

### 3.4.0

- 在查询构造器 Query(`Builder`)中, 增加`dealChunk(num, column, chunkFunctionalInterface)`支持是用索引条件的分块查询
- 在查询构造器 Query(`Builder`)中, 增加`firstOrderBy(closure)`支持将闭包中的排序字段添加到首部

### 3.3.1

- 在查询构造器 Query(`Builder`)中, 使`dealChunk()`当结果集为空时, 不再进行回调
- 在查询构造器 Query(`Builder`)中, 表达式风格的列名与属性名, 现在支持父类实体

### 3.2.0

- 在查询构造器 Query(`Builder`)中,将原生查询的绑定参数类型由`Collection<String>`更改为`Collection<?>`
- 在模型(`Model`)中, 将事件方法`log(String sql, Collection<String> parameterList)`
  更改为`log(String sql, Collection<?> parameterList)`
- 在模型(`GeneralModel`)中, 实现`log.debug`
- 底层实现"SoftCache", 以替代"sun.misc.SoftCache", 增加对于高版本jdk的兼容性

### 3.1.0

- 在查询构造器 Query(`Builder`)中增加对于表达式风格的列名与属性名的支持
- 在查询结果集 Record(`Record`)中增加对于表达式风格的列名与属性名的支持
- 在查询结果集 Record合(`RecordList`)中增加对于表达式风格的列名与属性名的支持

### 3.0.0

- 重写了底层的语法构造器(`Grammar`), 使其可以在任意语句下, 完成参数绑定.
- 在查询构造器 Query(`Builder`)中增加`toSql(sqlType, closure)`/`whereRaw(sqlPart, parameters)`/`havingRaw(sqlPart, parameters)`
  /`orderByRaw(sqlPart)`
- 在查询构造器 Query(`Builder`)中增加`when(condition, closure)`/`when(condition, closure, closure)`
- 在查询构造器 Query(`Builder`)中增加`whereKeywords(value, column...)`/`whereKeywordsIgnoreNull(value, column...)`
  /`havingKeywords(value, column...)`/`havingKeywordsIgnoreNull(value, column...)`等方法
- 在查询构造器 Query(`Builder`)中增加`whereMayLikeIgnoreNull(map)/whereMayLikeIgnoreNull(column, value)`
  /`havingMayLikeIgnoreNull(map)/havingMayLikeIgnoreNull(column, value)`
- 在查询构造器 Query(`Builder`)中增加`andWhereIgnoreEmpty(closure)`/`orWhereIgnoreEmpty(closure)`/`andHavingIgnoreEmpty(closure)`
  /`orHavingIgnoreEmpty(closure)`

### 2.22.0

- 在查询构造器 Query(`Builder`)中增加`whereMayLike`/`havingMayLike`系列执行方法`whereMayLike(column, value)`/`whereMayLike(entity)`
  /`whereMayLike(map)`/`havingMayLike(column, value)`/`havingMayLike(entity)`/`havingMayLike(map)`
- 在查询构造器 Query(`Builder`)中增加`whereRaw(list)`/`havingRaw(list)`
- 增加`GaarasonAutoconfiguration`接口, 程序会自动通过包扫描, 完成加载对其子类加载, 并调用其`init()` 完成初始化
- `InstanceCreatorFunctionalInterface`接口中, 增加默认的`getOrder()`方法. 用于支持, 在调用`ContainerProvider.register()`进行优先级判断.

### 2.21.0

- 在查询构造器 Query(`Builder`)中增加`whereLike`/`havingLike`系列执行方法`whereLike(column, value)`/`whereLike(entity)`/`whereLike(map)`
  /`havingLike(column, value)`/`havingLike(entity)`/`havingLike(map)`
- 修复查询构造器 Query(`Builder`)中使用`where(entity)`/`having(entity)`等方法时, 当实体`entity`中`@Column`中`insertable=false`时, 不正确的行为.

### 2.20.0

- 更改了项目的模块分布, 主要是拆分出的`database-api`模块, 以供rpc的接口模块去做依赖, 以及`database-query-*`模块, 以供拓展更多的数据库支持
- 更新了部分的依赖的版本
- 为了降低使用的门槛, 依然在`database-core`中维持了`druid`依赖, 以及`database-spring-boot-starter`中维持了`database-query-*`
  与`mysql-connector-java`等.

### 2.19.0

- 更改了`database-spring-boot-starter`中的配置逻辑, 通过配置中的(spring.datasource.type=**DataSource)现在可以使用任意的基本数据源了. 因此这项功能,
  所以配置文件将仅支持spring data风格, 而非 druid 风格.

### 2.18.1

- 在Spring自动配置类(`GaarasonDatabaseConfiguration`)中, 对`GaarasonDataSource`增加`@Primary`

### 2.18.0

- 在查询构造器 Query(`Builder`)中增加`join`系列执行方法`joinRaw(sqlPart)`/`join(joinType, table, joinConditions)`
  /`join(joinType, tempTable, alias, joinConditions)`

### 2.17.0

- `ModelShadowProvider`中增加`getByTableName(tableName)`
- `EntityUtils`中增加`entityAssignmentBySimpleMap(stringObjectMapList, entityClass)`

### 2.16.2

- 修复查询结果集 Record(`Record`)中使用`fillEntity(entity)`等方法时,由于实体`entity`存在复杂继承时引发的错误

### 2.16.1

- 分页对象(`Paginate`)中增加`Serializable`接口

### 2.16.0

- 在查询构造器 Query(`Builder`)中增加执行方法`simplePaginateMapStyle(currentPage, perPage)`/`paginateMapStyle(currentPage, perPage)`

### 2.15.0

- 在查询构造器 Query(`Builder`)中增加执行方法`insertMapStyle(entityMap)`/`insertMapStyle(entityMapList)`/`insertGetIdMapStyle(entityMap)`
  /`insertGetIdOrFailMapStyle(entityMap)`/`insertGetIdsMapStyle(entityMapList)`/`updateMapStyle(entityMap)`

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
- 在查询结果集 Record(`Record`)中增加方法`isDirty()`/`getDirty()`/`getDirtyMap()`/`isDirty(fieldName)`/`isClean()`/`isClean(fieldName)`
  /`getOriginal()`/`getOriginal(fieldName)`
- 在查询结果集 Record(`RecordList`)中增加方法`avg(fieldName)`/`sum(fieldName)`/`max(fieldName)`/`min(fieldName)`/`mode(fieldName)`
  /`median(fieldName)`/`chunk(closure, newSize)`/`chunk(newSize)`/`chunkToMap(newSize)`/`contains(fieldName, value)`
  /`contains(closure)`/`count()`/`countBy(closure)`/`countBy(fieldName)`/`every(closure)`/`filter(closure)`/`filter()`
  /`filter(fieldName)`/`reject(closure)`/`first(closure)`/`first()`/`groupBy(closure)`/`groupBy(fieldName)`
- 在查询结果集 Record(`RecordList`)中增加方法`implode(fieldName, delimiter)`/`implode(closure, delimiter)`/`keyBy(fieldName)`
  /`keyBy(closure)`/`last(closure)`/`last()`/`mapToGroups(closureKey, closureValue)`
  /`mapWithKeys(closureKey, closureValue)`/`pluck(fieldName)`/`pluck(fieldNameForValue, fieldNameForKey)`/`shift()`
  /`pop()`/`prepend(element)`/`push(element)`/`put(index, element)`/`pull(index)`/`random()`/`random(count)`/`reverse()`
  /`sortBy(closure, ase)`/`sortBy(closure)`/`sortBy(fieldName)`
- 在查询结果集 Record(`RecordList`)中增加方法`sortByDesc(fieldName)`/`sortByDesc(closure) `/`splice(offset)`/`splice(offset, taken)`
  /`take(count)`/`unique(closure)`/`unique(fieldName)`

### 2.10.0

- 整体改进，数据库字段值为null的兼容性，但依然建议数据库字段不使用null
- 在模型(`Model`)中增加方法`findOrFail(Object id)`/`find(Object id)`/`findMany(Object... ids)`
- 在模型(`Model`)中变更方法`findMany(Collection<K> ids)` -> `findMany(Collection<Object> ids)`
- 在查询构造器 Query(`Builder`)中增加方法`whereIgnoreNull(column, symbol, value)`/`whereIgnoreNull(column, value)`/`whereIgnoreNull(map)`
  /`whereInIgnoreEmpty(column, valueList)`/`whereInIgnoreEmpty(column, valueArray)`
  /`whereNotInIgnoreEmpty(column, valueList)`/`whereNotInIgnoreEmpty(column, valueArray)`
- 在查询构造器 Query(`Builder`)中增加方法`havingIgnoreNull(column, symbol, value)`/`havingIgnoreNull(column, value)`
  /`havingIgnoreNull(map)`/`havingInIgnoreEmpty(column, valueList)`/`havingInIgnoreEmpty(column, valueArray)`
  /`havingNotInIgnoreEmpty(column, valueList)`/`havingNotInIgnoreEmpty(column, valueArray)`
- 在查询构造器 Query(`Builder`)中增加方法`dataIgnoreNull(column, value)`/`dataIgnoreNull(map)`

### 2.9.0

- 在模型(`Model`)中增加方法`findOrNew(entity)`/`findByPrimaryKeyOrNew(entity)`/`findByPrimaryKeyOrCreate(entity)`
  /`findOrNew(conditionEntity, complementEntity)`/`updateByPrimaryKeyOrCreate(entity)`
- 在查询结果集 Record(`Record`)中增加方法`fillEntity(entity)`

### 2.8.0

- 实体代码生成时对于数据库的时间日期等,使用LocalDateTime/LocalDate/LocalTime等java类型对应

### 2.7.0

- 在模型(`Model`)中增加方法`findOrCreate(entity)`/`findOrCreate(conditionEntity, complementEntity)`
  /`updateOrCreate(conditionEntity, complementEntity)`
- 在查询构造器 Query(`Builder`)中增加方法`Builder<T, K> where(T entity)`/`Builder<T, K> where(Map<String, Object> map)`
  /`Builder<T, K> having(T entity)`/`Builder<T, K> having(Map<String, Object> map)`

### 2.6.0

- 在实体中增加对`java.sql.Date`/`java.sql.Time`/`java.sql.Timestamp`/`BigDecimal`/`Blob`/`Clob`类型的支持, 其中`Blob`/`Clob`仅支持查询

- 将关联关系 Relationship`Bind`中部分方法， 中间表数据参数类型由`Map<String, String>`变更到`Map<String, Object>`
- 将关联关系 Relationship`Bind`中部分方法， 手动指定目标表的主键， 参数类型由`String`、`Collection<String> ids`变更到`Object`、`Collection<Object> ids`

```java
int attach(Record<?, ?> targetRecord,Map<String, Object> relationDataMap);
int attach(RecordList<?, ?> targetRecords,Map<String, Object> relationDataMap);
int attach(Object id,Map<String, Object> relationDataMap);
int attach(Collection<Object> ids,Map<String, Object> relationDataMap);

int sync(Record<?, ?> targetRecord,Map<String, Object> relationDataMap);
int sync(RecordList<?, ?> targetRecords,Map<String, Object> relationDataMap);
int sync(Object id,Map<String, Object> relationDataMap);
int sync(Collection<Object> ids,Map<String, Object> relationDataMap);

int toggle(Record<?, ?> targetRecord,Map<String, Object> relationDataMap);
int toggle(RecordList<?, ?> targetRecords,Map<String, Object> relationDataMap);
int toggle(Object id,Map<String, Object> relationDataMap);
int toggle(Collection<Object> ids,Map<String, Object> relationDataMap);
```

### 2.5.0

- 将`Builder`中部分方法, 参数类型由`String`变更为`Object`

```java
Builder<T, K> where(String column,String symbol,Object value);
Builder<T, K> where(String column,Object value);
Builder<T, K> whereIn(String column,Object...valueArray);
Builder<T, K> whereNotIn(String column,Object...valueArray);
Builder<T, K> whereBetween(String column,Object min,Object max);
Builder<T, K> whereNotBetween(String column,Object min,Object max);

Builder<T, K> having(String column,String symbol,Object value);
Builder<T, K> having(String column,Object value);
Builder<T, K> havingIn(String column,Object...valueArray);
Builder<T, K> havingNotIn(String column,Object...valueArray);
Builder<T, K> havingBetween(String column,Object min,Object max);
Builder<T, K> havingNotBetween(String column,Object min,Object max);

Builder<T, K> data(String column,Object value);
Builder<T, K> data(Map<String, Object> map);
```

### 2.4.0

- 在实体中增加对java8的LocalDate/LocalDateTime/LocalTime类型的支持.

### 2.0.x->2.1.x

新增加

- 新提供关联关系 Relationship相关注解`@HasOneOrMany()`,`@BelongsTo()`,`@BelongsToMany()`
- 新提供关联关系 Relationship相关执行`Record::with(String column, GenerateSqlPart builderClosure, RelationshipRecordWith recordClosure)`
  与 `RecordList::with()` 与 `Builer::with()` 方法签名类似, 接受3个参数

### 2.0.x->2.1.x

新增加

- 新提供关联关系 Relationship相关注解`@HasOneOrMany()`,`@BelongsTo()`,`@BelongsToMany()`
- 新提供关联关系 Relationship相关执行`Record::with(String column, GenerateSqlPart builderClosure, RelationshipRecordWith recordClosure)`
  与 `RecordList::with()` 与 `Builer::with()` 方法签名类似, 接受3个参数

### 1.0.x->2.0.x

不兼容

- `gaarason.database.eloquent.Model<T>` 升级为 `gaarason.database.eloquent.Model<T, K>`, 其中T为实体类, K为实体类中的主键类型(eg: Long)
- `gaarason.database.query.Builder<T>` 升级为 `gaarason.database.query.Builder<T, K>`
- `gaarason.database.eloquent.Record<T>` 升级为 `gaarason.database.eloquent.Record<T, K>`
- `gaarason.database.eloquent.RecordList<T>` 升级为 `gaarason.database.eloquent.RecordList<T, K>`
- `gaarason.database.contract.model.Query<T> :: Record<T> findOrFail(String id)`
  升级为 `gaarason.database.contract.model.Query<T, K> :: Record<T, K> findOrFail(K id)`
- `gaarason.database.contract.model.Query<T> :: Record<T> find(String id)`
  升级为 `gaarason.database.contract.model.Query<T, K> :: Record<T, K> find(K id)`
- `gaarason.database.contract.builder.Transaction<T> :: boolean transaction(Runnable runnable, int maxAttempts)`
  升级为 `gaarason.database.contract.builder.Transaction<T> :: boolean transaction(Runnable runnable, int maxAttempts, boolean throwException)`

有影响

- ORM新增,现在会赋值数据库自增主键到原对象

新增加

- 新提供`K insertGetId()`/`K insertGetId(T entity)`/`K insertGetIdOrFail()`/`K insertGetIdOrFail(T entity)`
  /`List<K> insertGetIds(List<T> entityList)`
