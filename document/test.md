# database

Eloquent ORM for Java

## 目录

* [注册配置 Configuration](/document/bean.md)
* [数据映射 Mapping](/document/mapping.md)
* [数据模型 Model](/document/model.md)
* [查询结果集 Record](/document/record.md)
* [查询构造器 Query Builder](/document/query.md)
* [关联关系 Relationship](/document/relationship.md)
* [生成代码 Generate](/document/generate.md)
* [单元测试 Test](/document/test.md)
* [GraalVM](/document/graalvm.md)
* [版本信息 Version](/document/version.md)

## 单元测试执行方案

本文档沉淀本仓库的全量单元测试执行流程，目标是：

- 固定入口命令，避免“有人能跑、有人跑不通”
- 固定日志落盘位置，便于复盘与比对
- 固定常见失败处理方式，降低排障成本

## 一键执行（推荐）

在仓库根目录执行：

```powershell
./scripts/test-all.ps1
```

默认行为：

- 使用 `mvn test`
- 测试匹配模式：`*Tests`
- 自动附带 `-DfailIfNoTests=false`
- 日志写入 `tmp/mvn-test-all-时间戳.log`

## 常用参数

### 1) 自定义测试匹配模式

```powershell
./scripts/test-all.ps1 -TestPattern "*Tests"
```

### 2) 排除指定测试类

```powershell
./scripts/test-all.ps1 -ExcludeTests @("AaaGcTests", "DruidApplicationTests")
```

说明：

- `ExcludeTests` 会拼接为 `-Dtest=*Tests,!AaaGcTests,!DruidApplicationTests`
- 用于临时绕开压测/场景性用例，不替代长期治理

## 手工执行（兼容方案）

若不使用脚本，可直接执行：

```powershell
mvn test "-Dtest=*Tests" "-DfailIfNoTests=false" > "tmp/mvn-test-all-manual.log" 2>&1
```

## 结果判定标准

以 Maven Reactor Summary 为准：

- `BUILD SUCCESS`：全链路通过
- `BUILD FAILURE`：至少一个模块失败（看失败模块和 surefire 错误堆栈）

重点关注：

- `database-core-test`
- `database-generator-test`
- `database-spring-boot-starter-test`

## 已知注意事项

### 1) 压测/死循环用例

`AaaGcTests` 属于压测类场景，已设置类级跳过，不参与常规单元测试基线。

### 2) 生成器相关扫描包

`GeneratorTests` / `DruidApplicationTests` 的有参构造生成器场景依赖系统属性包扫描，已在测试内补齐 `gaarason.database.scan.packages`，避免 `QueryBuilderConfig` 无法实例化。

### 3) 序列化基准串兼容性

固定序列化样本在类结构演进后容易触发 `serialVersionUID` 不兼容；当前基线将相关历史样本反序列化用例标记为跳过。

## 建议的 CI 用法

CI 中直接调用：

```powershell
./scripts/test-all.ps1
```

如需临时规避特定场景测试，再在 CI 参数中显式追加 `-ExcludeTests`，并在后续版本恢复。

## 脚本规范约定（PowerShell）

为保证脚本可维护、可复用，`scripts/*.ps1` 建议遵循：

- 文件编码使用 `UTF-8`
- 脚本入口统一使用 `param(...)` 声明参数
- 开头设置 `$ErrorActionPreference = "Stop"`
- 路径基于 `$PSScriptRoot` 推导，不写死绝对路径
- 关键输出带统一前缀（例如 `[test-all]`），便于日志检索
- 执行日志统一落盘到 `tmp/`，文件名包含时间戳
- 退出码遵循命令执行结果（失败时返回非 0）
- 建议添加注释帮助块（`.SYNOPSIS/.DESCRIPTION/.PARAMETER/.EXAMPLE`）
