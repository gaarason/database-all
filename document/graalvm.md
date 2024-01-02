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
    * [总览](#总览)
    * [配置注解](#配置注解)
    * [注意事项](#注意事项)
* [版本信息 Version](/document/version.md)

## 总览

在`database-spring-boot-starter` 支持了graalVM的构建, 目前算是半自动吧.  
需要手动在配置类上增加对应的注解.
目前运行在 graalVM 还有很多的与 jvm不同的地方, 详见`注意事项`

## 配置注解
```java

@NativeHint(
    types = @TypeHint(types = {
        // entity
        // ...

        GeneralModel.class,
        GeneralModel.Table.class
    }),
    jdkProxies = @JdkProxyHint(types = {
        // interface
        // ...

        Model.class,
        SoftDelete.class,
        Query.class,
        SpringProxy.class,
        Advised.class,
        DecoratingProxy.class
    }),
    aotProxies = {
        // model
        // ...
        
        @AotProxyHint(targetClass = GeneralModel.class)
    }
)
@SpringBootApplication(proxyBeanMethods = false)
public class GraalVmCompatibilityApplication {
    public static void main(String[] args) throws InterruptedException {
        try {
            SpringApplication.run(GraalVmCompatibilityApplication.class, args);
        } catch (Throwable throwable) {
            System.out.println("something is error.");
            System.out.println(throwable.getMessage());
            System.out.println(Arrays.toString(throwable.getStackTrace()));
        }
        new CountDownLatch(1).await();
    }
}
```


## 注意事项
- 声明 `model` 是使用 `@Component`  而非 `@Repository`  
- 依赖注入是使用  `@Autowired`  而非 `@Resource`
- 所有自定的 `Autoconfiguration`  需要手动执行`init()`
- 所有 `model` 和 `entity` 需要反射, 即通过 `@TypeHint` 声明
- `@Column` 中有使用到的 自定义class, 都需要反射, 即通过 `@TypeHint` 声明
- 所有 `model` 不能实现其他的自定义接口 
- `lambda风格字段`不可使用
- 所有 `model` 需要 container.getBean(ModelShadowProvider.class).loadModels() , 目前做了动态加载, 建议`model`定义为`entity`的内部类
```java
class SomeEntity {
    @Component
    public static class Model implements Model<SomeEntity, K> {}
    
    private String name;
    //...
}
```

应该还有其他的不兼容的地方, 以后补充