package gaarason.database.generator;

import com.alibaba.druid.pool.DruidDataSource;
import gaarason.database.annotation.Table;
import gaarason.database.bootstrap.ContainerBootstrap;
import gaarason.database.connection.GaarasonDataSourceBuilder;
import gaarason.database.contract.connection.GaarasonDataSource;
import gaarason.database.contract.eloquent.Model;
import gaarason.database.contract.support.FieldConversion;
import gaarason.database.contract.support.FieldFill;
import gaarason.database.contract.support.FieldStrategy;
import gaarason.database.core.Container;
import gaarason.database.generator.appointment.Style;
import gaarason.database.generator.element.base.BaseElement;
import gaarason.database.generator.element.field.Field;
import gaarason.database.generator.element.field.MysqlFieldGenerator;
import gaarason.database.generator.exception.GeneratorException;
import gaarason.database.generator.support.TemplateHelper;
import gaarason.database.generator.support.TypeReference;
import gaarason.database.lang.Nullable;
import gaarason.database.provider.ModelShadowProvider;
import gaarason.database.util.StringUtils;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.io.Serializable;
import java.util.*;
import java.util.concurrent.*;

/**
 * 自动生成
 * @author xt
 */
public class Generator {


    private static final String TEMPLATE_PATH = "/template";

    /**
     * 未知主键类型时, 使用的java类
     */
    private static final String UNKNOWN_PRIMARY_KEY_TYPE = "Object";

    /**
     * 存储 表名 -> 主键类型 的映射关系, 稍微提高性能
     */
    private final ConcurrentHashMap<String, Class<?>> tablePrimaryKeyTypeMap = new ConcurrentHashMap<>();
    /**
     * 输出目录
     */
    private String outputDir = "./";
    /**
     * 命名空间
     */
    private String namespace = "data";
    /**
     * entity目录
     */
    private String entityDir = "entity";
    /**
     * entity前缀
     */
    private String entityPrefix = "";
    /**
     * entity后缀
     */
    private String entitySuffix = "";
    /**
     * BaseEntity 中的字段
     */
    private List<String> baseEntityFields = new ArrayList<>();
    /**
     * BaseEntity 目录
     */
    private String baseEntityDir = "base";
    /**
     * BaseEntity 类名
     */
    private String baseEntityName = "BaseEntity";
    /**
     * model目录
     */
    private String modelDir = "model";
    /**
     * model前缀
     */
    private String modelPrefix = "";
    /**
     * model后缀
     */
    private String modelSuffix = "Model";
    /**
     * baseModel目录
     */
    private String baseModelDir = "base";
    /**
     * baseModel类名
     */
    private String baseModelName = "BaseModel";
    /**
     * 是否使用spring boot注解 model
     */
    private boolean isSpringBoot;
    /**
     * 是否使用swagger注解 entity
     */
    private boolean isSwagger;
    /**
     * 是否使用 org.hibernate.validator.constraints.* 注解 entity
     */
    private boolean isValidator;
    /**
     * 是否生成静态字段名
     */
    private boolean entityStaticField;
    /**
     * 生成并发线程数
     */
    private int corePoolSize = 20;

    /**
     * 查询时，指定不查询的列
     */
    private String[] columnDisSelectable = {};

    /**
     * 字段, 填充方式
     */
    private final Map<String, Class<? extends FieldFill>> columnFill = new HashMap<>();
    /**
     * 字段, 使用策略
     */
    private final Map<String, Class<? extends FieldStrategy>> columnStrategy = new HashMap<>();
    /**
     * 字段, 新增使用策略
     */
    private final Map<String, Class<? extends FieldStrategy>> columnInsertStrategy = new HashMap<>();
    /**
     * 字段, 更新使用策略
     */
    private final Map<String, Class<? extends FieldStrategy>> columnUpdateStrategy = new HashMap<>();
    /**
     * 字段, 条件使用策略
     */
    private final Map<String, Class<? extends FieldStrategy>> columnConditionStrategy = new HashMap<>();

    /**
     * 字段, 序列化与反序列化方式
     */
    private final Map<String, Class<? extends FieldConversion>> columnConversion = new HashMap<>();

    /**
     * 代码风格
     */
    private Style style = Style.ENTITY;

    private TemplateHelper templateHelper;

    /**
     * model父类 所在的命名空间
     */
    private String baseModelNamespace;
    /**
     * entity父类 所在的命名空间
     */
    private String baseEntityNamespace;
    /**
     * model 所在的命名空间
     */
    private String modelNamespace;
    /**
     * entity 所在的命名空间
     */
    private String entityNamespace;
    /**
     * 用于委托执行的model
     */
    private Model<?, ?> model;

    /**
     * 使用无参构造时,需要重写 getModel 方法
     */
    public Generator() {

    }

    /**
     * 有参构造
     * 默认使用 com.mysql.cj.jdbc.Driver 与 com.alibaba.druid.pool.DruidDataSource
     * @param jdbcUrl 数据库连接地址
     * @param username 数据库用户名
     * @param password 数据库密码
     */
    public Generator(String jdbcUrl, String username, String password) {
        DruidDataSource druidDataSource = new DruidDataSource();
        druidDataSource.setUrl(jdbcUrl);
        druidDataSource.setUsername(username);
        druidDataSource.setPassword(password);
        druidDataSource.setDbType("com.alibaba.druid.pool.DruidDataSource");
        druidDataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
        druidDataSource.setInitialSize(20);
        druidDataSource.setMaxActive(20);
        druidDataSource.setLoginTimeout(3);
        druidDataSource.setQueryTimeout(3);

        ToolModel.gaarasonDataSource = GaarasonDataSourceBuilder.build(druidDataSource);

        this.model = ToolModel.gaarasonDataSource.getContainer()
            .getBean(ModelShadowProvider.class)
            .getByModelClass(ToolModel.class)
            .getModel();
    }

    /**
     * 有参构造
     * @param dataSource 数据源
     */
    public Generator(DataSource dataSource) {
        ToolModel.gaarasonDataSource = GaarasonDataSourceBuilder.build(dataSource,
            ContainerBootstrap.build().autoBootstrap());
        this.model = ToolModel.gaarasonDataSource.getContainer()
            .getBean(ModelShadowProvider.class)
            .getByModelClass(ToolModel.class)
            .getModel();
    }

    /**
     * 有参构造
     * 默认使用 com.mysql.cj.jdbc.Driver 与 com.alibaba.druid.pool.DruidDataSource
     * @param jdbcUrl 数据库连接地址
     * @param username 数据库用户名
     * @param password 数据库密码
     * @param container 容器
     */
    public Generator(String jdbcUrl, String username, String password, Container container) {
        DruidDataSource druidDataSource = new DruidDataSource();
        druidDataSource.setUrl(jdbcUrl);
        druidDataSource.setUsername(username);
        druidDataSource.setPassword(password);
        druidDataSource.setDbType("com.alibaba.druid.pool.DruidDataSource");
        druidDataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
        druidDataSource.setInitialSize(20);
        druidDataSource.setMaxActive(20);
        druidDataSource.setLoginTimeout(3);
        druidDataSource.setQueryTimeout(3);

        ToolModel.gaarasonDataSource = GaarasonDataSourceBuilder.build(druidDataSource, container);

        this.model = ToolModel.gaarasonDataSource.getContainer()
            .getBean(ModelShadowProvider.class)
            .getByModelClass(ToolModel.class)
            .getModel();
    }

    /**
     * 有参构造
     * @param dataSource 数据源
     */
    public Generator(DataSource dataSource, Container container) {
        ToolModel.gaarasonDataSource = GaarasonDataSourceBuilder.build(dataSource, container);
        this.model = ToolModel.gaarasonDataSource.getContainer()
            .getBean(ModelShadowProvider.class)
            .getByModelClass(ToolModel.class)
            .getModel();
    }

    /**
     * 将不合法的java标识符转换
     * @param name 未验证的java标识符
     * @return 合法的java标识符
     */
    private static String nameConverter(String name) {
        return StringUtils.isJavaIdentifier(name) ? name : "auto" + StringUtils.md5(name);
    }

    /**
     * 打印记录
     * @param str 记录
     */
    private static void consoleLog(String str) {
        System.out.println(str);
    }

    /**
     * 获取值, 并转化为字符串 or Null
     * @param fieldStringObjectMap
     * @param keyName
     * @return 字符串 or Null
     */
    @Nullable
    private static String getValue(Map<String, Object> fieldStringObjectMap, String keyName) {
        return Optional.ofNullable(fieldStringObjectMap.get(keyName)).map(Object::toString).orElse(null);
    }

    /**
     * 使用无惨可重写
     * @return 数据库操作model
     */
    public Model<?, ?> getModel() {
        return model;
    }

    /**
     * 初始化相关名称
     */
    private void init() {
        if (getModel() == null) {
            throw new GeneratorException("使用无参构造`public void Generator()`时,需要重写`getModel`方法,否则请使用`public void " +
                "Generator(String jdbcUrl, String username, String password)`");
        }

        templateHelper = new TemplateHelper(style, outputDir);

        baseModelNamespace = namespace + ("".equals(modelDir) ? "" : ("." + modelDir)) + ("".equals(
            baseModelDir) ? "" : ("." + baseModelDir));
        baseEntityNamespace = namespace + ("".equals(entityDir) ? "" : ("." + entityDir)) + ("".equals(
            baseEntityDir) ? "" : ("." + baseEntityDir));
        modelNamespace = namespace + ("".equals(modelDir) ? "" : ("." + modelDir));
        entityNamespace = namespace + ("".equals(entityDir) ? "" : ("." + entityDir));
    }

    /**
     * 开始生成
     */
    public void run() {
        // 初始化namespace
        init();
        // 表信息
        List<Map<String, Object>> tables = showTables();
        // 生成 baseModel
        processBaseModel();
        // 生成 baseEntity
        processBaseEntity(tables.get(0).entrySet().stream().findFirst().get().getValue().toString());

        ThreadPoolExecutor threadPool = new ThreadPoolExecutor(corePoolSize, corePoolSize + 1, 1,
            TimeUnit.MILLISECONDS, new ArrayBlockingQueue<>(65535));

        CountDownLatch countDownLatch = new CountDownLatch(tables.size());

        for (Map<String, Object> table : tables) {
            threadPool.execute(() -> {
                // 单个表
                for (Map.Entry<String, Object> entry : table.entrySet()) {
                    // 表名
                    String tableName = entry.getValue().toString();
                    // entity文件名
                    String entityName = entityName(tableName);
                    // 表信息
                    Map<String, Object> tableInfo = showTableInfo(tableName);
                    // 表注释
                    String entityComment = tableInfo.get("TABLE_COMMENT") == null ? tableName : tableInfo.get(
                        "TABLE_COMMENT").toString();


                    // entity文件内容
                    processEntity(tableName, entityName, entityComment);
                    // model文件名
                    String modelName = modelName(tableName);

                    // model文件内容
                    processModel(tableName, modelName, entityName);
                }
                countDownLatch.countDown();
            });
        }
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        consoleLog("全部生成完毕");
    }

    private void processBaseModel() {
        Map<String, Object> parameterMap = new HashMap<>();
        parameterMap.put("${namespace}", baseModelNamespace);
        parameterMap.put("${base_model_name}", baseModelName);
        parameterMap.put("${base_entity_name}", baseEntityName);
        parameterMap.put("${base_entity_namespace}", baseEntityNamespace);
        templateHelper.writeBaseModel(parameterMap);
    }

    private void processBaseEntity(String tableName) {
        BaseElement element = new BaseElement() {};
        element.setNamespace(baseEntityNamespace);
        element.setClassName(baseEntityName);

        element.type2Name(Serializable.class);
        element.type2Name("lombok.Data");

        Map<String, String> parameterMap = new HashMap<>();
        parameterMap.put("${namespace}", baseEntityNamespace);
        parameterMap.put("${base_entity_name}", baseEntityName);
        parameterMap.put("${static_fields}", entityStaticField ? fillStaticFieldsTemplate(tableName, true) : "");
        parameterMap.put("${fields}", fillFieldsTemplate(element, tableName, true));

        parameterMap.put("${base_model_within_base_entity}", fillBaseModelWithinBaseEntityTemplate(element));

        parameterMap.put("${imports}", element.printImports());

        templateHelper.writeBaseEntity(parameterMap);
    }


    /**
     * 填充baseModel模板内容
     */
    private void processEntity(String tableName, String entityName, String comment) {
        BaseElement element = new BaseElement() {};
        element.setNamespace(entityNamespace);
        element.setClassName(entityName);
        element.type2Name("lombok.Data");
        element.type2Name("lombok.EqualsAndHashCode");
        element.type2Name(Table.class);

        Map<String, String> parameterMap = new HashMap<>();
        parameterMap.put("${base_entity_namespace}", baseEntityNamespace);
        parameterMap.put("${base_entity_name}", element.type2Name(baseEntityNamespace + "." + baseEntityName));
        parameterMap.put("${namespace}", entityNamespace);
        parameterMap.put("${entity_name}", entityName);
        parameterMap.put("${table}", tableName);

        parameterMap.put("${swagger_annotation}",
            isSwagger ? element.anno2Name("io.swagger.annotations.ApiModel") + "(\"" + comment + "\")\n" : "");
        parameterMap.put("${static_fields}", entityStaticField ? fillStaticFieldsTemplate(tableName, false) : "");
        parameterMap.put("${fields}", fillFieldsTemplate(element, tableName, false));

        parameterMap.put("${model_within_entity}", fillModelWithinEntityTemplate(element, tableName, entityName));

        parameterMap.put("${imports}", element.printImports());

        templateHelper.writeEntity(parameterMap);
    }

    /**
     * 填充model模板内容
     * @param tableName 表名
     * @param modelName dao对象名
     * @param entityName pojo对象名
     */
    private void processModel(String tableName, String modelName, String entityName) {
        BaseElement element = new BaseElement() {};
        element.setNamespace(entityNamespace);
        element.setClassName(entityName);
        element.type2Name("lombok.Data");
        element.type2Name("lombok.EqualsAndHashCode");
        element.type2Name(Table.class);


        Map<String, String> parameterMap = new HashMap<>();
        parameterMap.put("${namespace}", modelNamespace);
        parameterMap.put("${base_model_namespace}", baseModelNamespace);
        parameterMap.put("${base_model_name}", baseModelName);
        parameterMap.put("${entity_namespace}", entityNamespace);
        parameterMap.put("${entity_name}", entityName);

        parameterMap.put("${primary_key_type}",
            tablePrimaryKeyTypeMap.containsKey(tableName) ? element.type2Name(tablePrimaryKeyTypeMap.get(tableName)) :
                element.type2Name(Object.class));

        parameterMap.put("${model_name}", modelName);
        parameterMap.put("${is_spring_boot}",
            isSpringBoot ? element.anno2Name("import org.springframework.stereotype.Repository") : "");

        parameterMap.put("${imports}", element.printImports());

        templateHelper.writeModel(parameterMap);
    }

    private String fillBaseModelWithinBaseEntityTemplate(BaseElement element) {
        if (Style.ENTITY.equals(style) || Style.ALL.equals(style)) {
            // 导入
            element.type2Name(new TypeReference<gaarason.database.eloquent.Model<?, ?>>() {});
            element.type2Name(new TypeReference<Collection<?>>() {});
            element.type2Name(new TypeReference<GaarasonDataSource>() {});
            element.anno2Name(Resource.class);

            // 模板替换参数
            Map<String, String> parameterMap = new HashMap<>(16);
            parameterMap.put("${base_entity_name}", baseEntityName);
            parameterMap.put("${base_model_name}", element.type2Name(baseEntityNamespace + "." + baseModelName));
            return templateHelper.fillBaseModelWithinBaseEntity(parameterMap);
        } else {
            return "";
        }
    }

    private String fillModelWithinEntityTemplate(BaseElement element, String tableName, String entityName) {
        if (Style.ENTITY.equals(style) || Style.ALL.equals(style)) {
            // 导入

            // 模板替换参数
            Map<String, String> parameterMap = new HashMap<>(16);

            parameterMap.put("${entity_name}", entityName);
            parameterMap.put("${is_spring_boot}",
                isSpringBoot ? element.type2Name("org.springframework.stereotype.@Repository") : "");
            parameterMap.put("${base_model_name}",
                element.type2Name(baseEntityNamespace + "." + baseEntityName + "$" + baseModelName));
            parameterMap.put("${primary_key_type}",
                tablePrimaryKeyTypeMap.containsKey(tableName) ?
                    element.type2Name(tablePrimaryKeyTypeMap.get(tableName)) :
                    element.type2Name(Object.class));

            return templateHelper.fillModelWithinEntity(parameterMap);
        } else {
            return "";
        }
    }

    /**
     * 填充所有字段
     * @param tableName 表名
     * @param isForBaseEntity entity父类使用
     * @return 内容
     */
    private String fillFieldsTemplate(BaseElement element, String tableName, boolean isForBaseEntity) {
        consoleLog("处理表 : " + tableName);

        StringBuilder str = new StringBuilder();
        // 字段信息
        List<Map<String, Object>> fields = descTable(tableName);

        for (Map<String, Object> field : fields) {
            // 每个字段的填充
            String fieldTemplateStrReplace = fillFieldTemplate(element, field, tableName, isForBaseEntity);
            // 追加
            str.append(fieldTemplateStrReplace);
        }
        return str.toString();
    }

    /**
     * 静态字段填充
     * @param tableName 表名
     * @param isForBaseEntity entity父类使用
     * @return 内容
     */
    private String fillStaticFieldsTemplate(String tableName, boolean isForBaseEntity) {
        StringBuilder str = new StringBuilder();
        // 字段信息
        List<Map<String, Object>> fields = descTable(tableName);

        for (Map<String, Object> field : fields) {
            // 原字段名
            String columnName = field.get("COLUMN_NAME").toString();

            // 在baseEntity中已存在
            if (baseEntityFields.contains(columnName)) {
                if (!isForBaseEntity) {
                    continue;
                }
            } else {
                if (isForBaseEntity) {
                    continue;
                }
            }

            // 静态字段名
            String staticName = nameConverter(columnName).toUpperCase();

            // 每个字段的填充(避免静态字段与普通属性名一样导致冲突, 一样时使用$前缀)
            String fieldTemplateStrReplace =
                "    public static final String " + (staticName.equals(columnName) ? "$" : "") + staticName +
                    " = \"" + columnName + "\";\n";
            // 追加
            str.append(fieldTemplateStrReplace);
        }
        return str.toString();
    }

    /**
     * 填充单个字段
     * @param fieldStringObjectMap 字段属性
     * @param tableName 表名
     * @param isForBaseEntity 用于entity父类使用
     * @return 内容
     */
    private String fillFieldTemplate(BaseElement element, Map<String, Object> fieldStringObjectMap, String tableName,
        boolean isForBaseEntity) {

        System.out.println(fieldStringObjectMap);
        // 判断数据源类型 目前仅支持mysql

        MysqlFieldGenerator mysqlFieldGenerator = new MysqlFieldGenerator();
        mysqlFieldGenerator.setColumnName(getValue(fieldStringObjectMap, MysqlFieldGenerator.COLUMN_NAME));

        mysqlFieldGenerator.setTableCatalog(getValue(fieldStringObjectMap, MysqlFieldGenerator.TABLE_CATALOG));
        mysqlFieldGenerator.setIsNullable(getValue(fieldStringObjectMap, MysqlFieldGenerator.IS_NULLABLE));
        mysqlFieldGenerator.setTableName(getValue(fieldStringObjectMap, MysqlFieldGenerator.TABLE_NAME));
        mysqlFieldGenerator.setTableSchema(getValue(fieldStringObjectMap, MysqlFieldGenerator.TABLE_NAME));
        mysqlFieldGenerator.setExtra(getValue(fieldStringObjectMap, MysqlFieldGenerator.THE_EXTRA));
        mysqlFieldGenerator.setColumnKey(getValue(fieldStringObjectMap, MysqlFieldGenerator.COLUMN_KEY));
        mysqlFieldGenerator.setCharacterOctetLength(
            getValue(fieldStringObjectMap, MysqlFieldGenerator.CHARACTER_OCTET_LENGTH));
        mysqlFieldGenerator.setNumericPrecision(getValue(fieldStringObjectMap, MysqlFieldGenerator.NUMERIC_PRECISION));
        mysqlFieldGenerator.setPrivileges(getValue(fieldStringObjectMap, MysqlFieldGenerator.THE_PRIVILEGES));
        mysqlFieldGenerator.setColumnComment(getValue(fieldStringObjectMap, MysqlFieldGenerator.COLUMN_COMMENT));
        mysqlFieldGenerator.setDatetimePrecision(
            getValue(fieldStringObjectMap, MysqlFieldGenerator.DATETIME_PRECISION));
        mysqlFieldGenerator.setCollationName(getValue(fieldStringObjectMap, MysqlFieldGenerator.COLLATION_NAME));
        mysqlFieldGenerator.setNumericScale(getValue(fieldStringObjectMap, MysqlFieldGenerator.NUMERIC_SCALE));
        mysqlFieldGenerator.setColumnType(getValue(fieldStringObjectMap, MysqlFieldGenerator.COLUMN_TYPE));
        mysqlFieldGenerator.setOrdinalPosition(getValue(fieldStringObjectMap, MysqlFieldGenerator.ORDINAL_POSITION));
        mysqlFieldGenerator.setCharacterMaximumLength(
            getValue(fieldStringObjectMap, MysqlFieldGenerator.CHARACTER_MAXIMUM_LENGTH));
        mysqlFieldGenerator.setDataType(getValue(fieldStringObjectMap, MysqlFieldGenerator.DATA_TYPE));
        mysqlFieldGenerator.setCharacterSetName(getValue(fieldStringObjectMap, MysqlFieldGenerator.CHARACTER_SET_NAME));
        mysqlFieldGenerator.setColumnDefault(getValue(fieldStringObjectMap, MysqlFieldGenerator.COLUMN_DEFAULT));

        mysqlFieldGenerator.setColumnDisSelectable(columnDisSelectable);
        mysqlFieldGenerator.setColumnFill(columnFill);
        mysqlFieldGenerator.setColumnStrategy(columnStrategy);
        mysqlFieldGenerator.setColumnInsertStrategy(columnInsertStrategy);
        mysqlFieldGenerator.setColumnUpdateStrategy(columnUpdateStrategy);
        mysqlFieldGenerator.setColumnConditionStrategy(columnConditionStrategy);
        mysqlFieldGenerator.setColumnConversion(columnConversion);

        Field field = mysqlFieldGenerator.toField(element);

        // 暂存主键类型
        if (field.isPrimary()) {
            tablePrimaryKeyTypeMap.put(tableName, field.getJavaClassTypeString());
        }

        // 在 baseEntity 中存在了, 子类不需要
        if (baseEntityFields.contains(mysqlFieldGenerator.getColumnName())) {
            if (!isForBaseEntity) {
                return "";
            }
        } else {
            if (isForBaseEntity) {
                return "";
            }
        }

        // 模板替换参数
        Map<String, String> parameterMap = new HashMap<>(16);
        parameterMap.put("${primary}", field.toAnnotationDatabasePrimary());
        parameterMap.put("${column}", field.toAnnotationDatabaseColumn());
        parameterMap.put("${field}", field.toFieldName());
        parameterMap.put("${apiModelProperty}", isSwagger ? field.toAnnotationSwaggerAnnotationsApiModelProperty() :
            "");
        parameterMap.put("${validator}", isValidator ? field.toAnnotationOrgHibernateValidatorConstraintValidator() :
            "");

        return templateHelper.fillField(parameterMap);
    }

    /**
     * 由表名生成实体类名
     * @param tableName 表名
     * @return 实体类名
     */
    private String entityName(String tableName) {
        String name = entityPrefix + StringUtils.lineToHump(tableName, true) + entitySuffix;
        return nameConverter(name);
    }

    /**
     * 由表名生成模型类名
     * @param tableName 表名
     * @return 模型类名
     */
    private String modelName(String tableName) {
        String name = modelPrefix + StringUtils.lineToHump(tableName, true) + modelSuffix;
        return nameConverter(name);
    }

    /**
     * 查看表信息
     * @return 表信息
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> showTableInfo(String tableName) {
        List<String> parameters = new ArrayList<>();
        parameters.add(DBName());
        parameters.add(tableName);
        return getModel().newQuery().queryOrFail("select * from information_schema.tables where table_schema = ? and" +
            " table_name = ? ", parameters).toMap();
    }

    /**
     * 查看有哪些表
     * @return 表列表
     */
    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> showTables() {
        return getModel().nativeQueryList("show tables", new ArrayList<>()).toMapList();
    }

    /**
     * 查看表中的字段结构
     * @param tableName 表名
     * @return 字段信息
     */
    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> descTable(String tableName) {
        List<String> parameters = new ArrayList<>();
        parameters.add(DBName());
        parameters.add(tableName);
        return getModel().nativeQueryList(
                "select * from information_schema.`columns` where table_schema = ? and table_name = ? order by ordinal_position",
                parameters)
            .toMapList();
    }

    /**
     * @return 数据库库名
     */
    @SuppressWarnings("unchecked")
    private String DBName() {
        String name = "";
        Model model = getModel();
        Map<String, Object> map = model.newQuery().queryOrFail("select database()", new ArrayList<>()).toMap();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (entry.getValue() != null) {
                name = entry.getValue().toString();
                break;
            }
        }
        if ("".equals(name)) {
            throw new GeneratorException("获取当前库名失败");
        }
        return name;
    }

    public void setBaseEntityFields(String... column) {
        baseEntityFields = Arrays.asList(column);
    }

    public void setStyle(Style style) {
        this.style = style;
    }

    public void setOutputDir(String outputDir) {
        this.outputDir = outputDir;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public void setEntityDir(String entityDir) {
        this.entityDir = entityDir;
    }

    public void setEntityPrefix(String entityPrefix) {
        this.entityPrefix = entityPrefix;
    }

    public void setEntitySuffix(String entitySuffix) {
        this.entitySuffix = entitySuffix;
    }

    public void setBaseEntityDir(String baseEntityDir) {
        this.baseEntityDir = baseEntityDir;
    }

    public void setBaseEntityName(String baseEntityName) {
        this.baseEntityName = baseEntityName;
    }

    public void setModelDir(String modelDir) {
        this.modelDir = modelDir;
    }

    public void setModelPrefix(String modelPrefix) {
        this.modelPrefix = modelPrefix;
    }

    public void setModelSuffix(String modelSuffix) {
        this.modelSuffix = modelSuffix;
    }

    public void setBaseModelDir(String baseModelDir) {
        this.baseModelDir = baseModelDir;
    }

    public void setBaseModelName(String baseModelName) {
        this.baseModelName = baseModelName;
    }

    public void setSpringBoot(boolean springBoot) {
        isSpringBoot = springBoot;
    }

    public void setSwagger(boolean swagger) {
        isSwagger = swagger;
    }

    public void setValidator(boolean validator) {
        isValidator = validator;
    }

    public void setEntityStaticField(boolean entityStaticField) {
        this.entityStaticField = entityStaticField;
    }

    public void setCorePoolSize(int corePoolSize) {
        this.corePoolSize = corePoolSize;
    }

    public Generator setColumnDisSelectable(String... columnDisSelectable) {
        this.columnDisSelectable = columnDisSelectable;
        return this;

    }

    public Generator setColumnFill(Class<? extends FieldFill> fieldFillClass, String... columns) {
        for (String column : columns) {
            columnFill.put(column, fieldFillClass);
        }
        return this;
    }

    public Generator setColumnStrategy(Class<? extends FieldStrategy> fieldStrategyClass, String... columns) {
        for (String column : columns) {
            columnStrategy.put(column, fieldStrategyClass);
        }
        return this;
    }

    public Generator setColumnInsertStrategy(Class<? extends FieldStrategy> fieldStrategyClass, String... columns) {
        for (String column : columns) {
            columnInsertStrategy.put(column, fieldStrategyClass);
        }
        return this;
    }

    public Generator setColumnUpdateStrategy(Class<? extends FieldStrategy> fieldStrategyClass, String... columns) {
        for (String column : columns) {
            columnUpdateStrategy.put(column, fieldStrategyClass);
        }
        return this;
    }

    public Generator setColumnConditionStrategy(Class<? extends FieldStrategy> fieldStrategyClass, String... columns) {
        for (String column : columns) {
            columnConditionStrategy.put(column, fieldStrategyClass);
        }
        return this;
    }

    public Generator setColumnConversion(Class<? extends FieldConversion> fieldConversionClass, String... columns) {
        for (String column : columns) {
            columnConversion.put(column, fieldConversionClass);
        }
        return this;
    }

    public static class ToolModel extends gaarason.database.eloquent.Model<ToolModel.Inner, Serializable> {

        protected static GaarasonDataSource gaarasonDataSource;

        @Override
        public GaarasonDataSource getGaarasonDataSource() {
            return gaarasonDataSource;
        }

        public static class Inner implements Serializable {

            private static final long serialVersionUID = 1L;
        }
    }

}
