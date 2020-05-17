# database
Eloquent ORM for Java
## 目录
* [注册bean](/document/bean.md)
* [数据映射](/document/mapping.md)
* [数据模型](/document/model.md)
    * [总览](#总览)
    * [数据库连接](#数据库连接)
    * [事件](#事件)
    * [作用域](#作用域)
        * [自定义查询作用域](#自定义查询作用域)
        * [软删除](#软删除)
* [查询结果集](/document/record.md)
* [查询构造器](/document/query.md)
* [关联关系](/document/relationship.md)
* [生成代码](/document/generate.md)
* [版本信息](/document/version.md)
## 总览

数据模型是将数据库操作集中声明的对象, 理解为`表`  
[反向生成代码](/document/generate.md)  

## 数据库连接

重写`getProxyDataSource`  
下面的例子使用`spring`注入后返回

**`ProxyDataSource`相关请看[注册bean](/document/bean.md)**

```java
package temp.model.base;

import gaarason.database.connections.ProxyDataSource;
import gaarason.database.eloquent.Model;

import javax.annotation.Resource;

/**
 * Model基类
 * @param <T> 实体类
 * @param <K> 实体类中的主键java类型, 不存在主键时, 可使用 Object
 */
abstract public class BaseModel<T, K> extends Model<T, K> {

    @Resource(name = "proxyDataSource")
    protected ProxyDataSource dataSource;

    @Override
    public ProxyDataSource getProxyDataSource(){
        return dataSource;
    }

}

```

子类只需要继承父类即可

```java
package temp.model;

import temp.model.base.BaseModel;
import temp.pojo.Student;
import org.springframework.stereotype.Repository;

@Repository
public class StudentModel extends BaseModel<Student, Long> {

}

```
## 事件

所有事件在`ORM`时触发, 事件可以继承自父类 

Eloquent 模型可以触发事件，允许你在模型生命周期中的多个时间点调用如下这些方法：retrieved, creating, created, updating, updated, saving, saved, deleting, deleted, restoring, restored。事件允许你在一个指定模型类每次保存或更新的时候执行代码。

`retrieved` 事件会在从数据库中获取已存在模型时触发。   
当一个新模型被首次保存的时候，`creating` 和 `created` 事件会被触发。 
如果一个模型已经在数据库中存在并调用 `save` 方法，`updating`和`updated` 事件会被触发。  
无论是创建还是更新，`saving`和`saved` 事件都会被触发。 
`deleting`, `deleted`, `restoring`, `restored`则分别在删除以及恢复时触发。 

- `ing`结尾的事件, 均可以阻止事件的进行

借用上面的`model`, 则一个事件的定义可以是以下形式
```java
package temp.model;

import temp.model.base.BaseModel;
import temp.pojo.Student;
import org.springframework.stereotype.Repository;

@Repository
public class StudentModel extends BaseModel<Student, Long> {

    @Override
    public void retrieved(Record<Entity, Long> entityRecord){
        System.out.println("已经从数据库中查询到数据");
    }
    
    @Override
    public boolean updating(Record<Entity, Long> record){
        if(record.getEntity().getId() == 9){
            System.out.println("正要修改id为9的数据, 但是拒绝");
            return false;
        }
        return true;
    }

}
```

## 作用域

在父类或者底层限制数据查询的有效范围

### 自定义查询作用域

```java
package temp.model;

import temp.model.base.BaseModel;
import temp.pojo.Student;
import org.springframework.stereotype.Repository;

@Repository
public class StudentModel extends BaseModel<Student, Long> {
    /**
     * 全局查询作用域
     * @param builder 查询构造器
     * @return 查询构造器
     */
    protected Builder<Student, Long> apply(Builder<Student, Long> builder) {
        // return builder->where("type", "2");
        return builder;
    }
}
```


### 软删除

- 软删除相关实现, 重写 `gaarason.database.eloquent.repository.BaseSoftDeleting`的相关方法  
- 开启软删除, 重写`softDeleting`方法结果为`true`

```java
package temp.model;

import temp.model.base.BaseModel;
import temp.pojo.Student;
import org.springframework.stereotype.Repository;

@Repository
public class StudentModel extends BaseModel<Student, Long> {

    /**
     * 是否启用软删除
     */
    protected boolean softDeleting() {
        return false;
    }

    /**
     * 删除(软/硬删除)
     * @param builder 查询构造器
     * @return 删除的行数
     */
    public int delete(Builder<Student, Long> builder) {
        return softDeleting() ? softDelete(builder) : builder.forceDelete();
    }

    /**
     * 恢复软删除
     * @param builder 查询构造器
     * @return 删除的行数
     */
    public int restore(Builder<Student, Long> builder) {
        return softDeleteRestore(builder);
    }

    /**
     * 软删除查询作用域(反)
     * @param builder 查询构造器
     */
    protected void scopeSoftDeleteOnlyTrashed(Builder<Student, Long> builder) {
        builder.where("is_deleted", "1");
    }

    /**
     * 软删除查询作用域(反)
     * @param builder 查询构造器
     */
    protected void scopeSoftDeleteWithTrashed(Builder<Student, Long> builder) {

    }

    /**
     * 软删除查询作用域
     * @param builder 查询构造器
     */
    protected void scopeSoftDelete(Builder<Student, Long> builder) {
        builder.where("is_deleted", "0");
    }


    /**
     * 软删除实现
     * @param builder 查询构造器
     * @return 删除的行数
     */
    protected int softDelete(Builder<Student, Long> builder) {
        return builder.data("is_deleted", "1").update();
    }

    /**
     * 恢复软删除实现
     * @param builder 查询构造器
     * @return 恢复的行数
     */
    protected int softDeleteRestore(Builder<Student, Long> builder) {
        return builder.data("is_deleted", "0").update();
    }
}
```