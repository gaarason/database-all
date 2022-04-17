package gaarason.database.generator;

import com.alibaba.druid.pool.DruidDataSource;
import gaarason.database.connection.GaarasonDataSourceBuilder;
import gaarason.database.connection.GaarasonDataSourceWrapper;
import gaarason.database.contract.connection.GaarasonDataSource;
import gaarason.database.contract.eloquent.Model;
import gaarason.database.lang.Nullable;
import gaarason.database.eloquent.ModelBean;
import gaarason.database.generator.element.field.Field;
import gaarason.database.generator.element.field.MysqlFieldGenerator;
import gaarason.database.generator.exception.GeneratorException;
import gaarason.database.provider.ModelShadowProvider;
import gaarason.database.util.StringUtils;

import javax.sql.DataSource;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;

/**
 * 自动生成
 * @author xt
 */
public class Generator {

    /**
     * 未知主键类型时, 使用的java类
     */
    private static final String UNKNOWN_PRIMARY_KEY_TYPE = "Serializable";

    /**
     * entity父类 对应的模板字符串
     */
    private static final String BASE_ENTITY_TEMPLATE_STR = fileGetContent(getAbsoluteReadFileName("baseEntity"));

    /**
     * entity 对应的模板字符串
     */
    private static final String ENTITY_TEMPLATE_STR = fileGetContent(getAbsoluteReadFileName("entity"));

    /**
     * entity field 对应的模板字符串
     */
    private static final String FIELD_TEMPLATE_STR = fileGetContent(getAbsoluteReadFileName("field"));

    /**
     * model父类 对应的模板字符串
     */
    private static final String BASE_MODEL_TEMPLATE_STR = fileGetContent(getAbsoluteReadFileName("baseModel"));

    /**
     * model 对应的模板字符串
     */
    private static final String MODEL_TEMPLATE_STR = fileGetContent(getAbsoluteReadFileName("model"));

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
     * 新增时,不可通过代码更改的字段
     */
    private String[] disInsertable = {};

    /**
     * 更新时,不可通过代码更改的字段
     */
    private String[] disUpdatable = {};

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
    private Model<? extends Serializable, ? extends Serializable> model;

    /**
     * 存储 表名 -> 主键类型 的映射关系, 稍微提高性能
     */
    private final ConcurrentHashMap<String, String> tablePrimaryKeyTypeMap = new ConcurrentHashMap<>();

    /**
     * 使用无参构造时,需要重写 getModel 方法
     */
    public Generator() {

    }

    /**
     * 有参构造
     * 默认使用 com.mysql.cj.jdbc.Driver 与 com.alibaba.druid.pool.DruidDataSource
     * @param jdbcUrl  数据库连接地址
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

        List<DataSource> dataSources = new ArrayList<>();
        dataSources.add(druidDataSource);

        ToolModel.gaarasonDataSource = GaarasonDataSourceBuilder.build(dataSources);

        this.model = ModelShadowProvider.getByModelClass(ToolModel.class).getModel();
    }

    /**
     * 有参构造
     * @param dataSource  数据源
     */
    public Generator(DataSource dataSource) {

        ToolModel.gaarasonDataSource = GaarasonDataSourceBuilder.build(dataSource);

        this.model = ModelShadowProvider.getByModelClass(ToolModel.class).getModel();
    }

    /**
     * 将类的命名空间转化为对应的目录
     * @param namespace 命名空间
     * @return 目录
     */
    private static String namespace2dir(String namespace) {
        return namespace.replace('.', '/');
    }

    /**
     * 填充模板
     * @param template     模板内容
     * @param parameterMap 参数
     * @return 填充后的内容
     */
    private static String fillTemplate(String template, Map<String, String> parameterMap) {
        for (Map.Entry<String, String> entry : parameterMap.entrySet()) {
            template = template.replace(entry.getKey(), entry.getValue());
        }
        return template;
    }

    /**
     * 获取绝对路径
     * @param name 文件名
     * @return 绝对路径
     */
    private static String getAbsoluteReadFileName(String name) {
        return "/template/" + name;
    }

    /**
     * 获取文件内容
     * @param fileName 文件名
     * @return 文件内容
     */
    private static String fileGetContent(String fileName) {
        InputStream is = Generator.class.getResourceAsStream(fileName);
        BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
        String s;
        StringBuilder configContentStr = new StringBuilder();
        try {
            while ((s = br.readLine()) != null) {
                configContentStr.append(s);
            }
        } catch (IOException e) {
            throw new GeneratorException(e);
        }

        return configContentStr.toString();
    }

    /**
     * 写入文件内容
     * @param path     文件路径
     * @param fileName 文件名
     * @param content  文件内容
     */
    private static void filePutContent(String path, String fileName, String content) {
        try {
            File file = new File(path);
            if (file.exists() || file.mkdirs()) {
                OutputStreamWriter outputStreamWriter = new OutputStreamWriter(
                    new FileOutputStream(path + fileName + ".java"), StandardCharsets.UTF_8);

                outputStreamWriter.write(content.replaceAll("\\\\n", "\n"));
                outputStreamWriter.flush();
                outputStreamWriter.close();
                // 控制台输出
                consoleLog(fileName + " 生成完毕, 路径 : " + path);
                return;
            }
            throw new GeneratorException("目录建立失败 : " + file);
        } catch (IOException e) {
            throw new GeneratorException(e);
        }
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
    public Model<? extends Serializable, ? extends Serializable> getModel() {
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
        // baseModel 文件内容
        String baseDaoTemplateStrReplace = fillBaseModelTemplate();
        // baseModel 写入文件
        filePutContent(getAbsoluteWriteFilePath(baseModelNamespace), baseModelName, baseDaoTemplateStrReplace);
        // baseEntity 文件内容
        String baseEntityTemplateStrReplace = fillBaseEntityTemplate(tables.get(0).entrySet().stream().findFirst().get().getValue().toString());
        // baseEntity 写入文件
        filePutContent(getAbsoluteWriteFilePath(baseEntityNamespace), baseEntityName, baseEntityTemplateStrReplace);

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
                    String entityTemplateStrReplace = fillPojoTemplate(tableName, entityName, entityComment);
                    // entity写入文件
                    filePutContent(getAbsoluteWriteFilePath(entityNamespace), entityName, entityTemplateStrReplace);

                    // 主键的java类型
                    String primaryKeyType = Optional.ofNullable(tablePrimaryKeyTypeMap.get(tableName))
                        .map(Object::toString)
                        .orElse(UNKNOWN_PRIMARY_KEY_TYPE);
                    // model文件名
                    String modelName = modelName(tableName);
                    // model文件内容
                    String modelTemplateStrReplace = fillModelTemplate(tableName, modelName, entityName,
                        primaryKeyType);
                    // model写入文件
                    filePutContent(getAbsoluteWriteFilePath(modelNamespace), modelName, modelTemplateStrReplace);
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

    /**
     * 填充baseModel模板内容
     * @return 内容
     */
    private String fillBaseModelTemplate() {
        Map<String, String> parameterMap = new HashMap<>();
        parameterMap.put("${namespace}", baseModelNamespace);
        parameterMap.put("${model_name}", baseModelName);
        parameterMap.put("${base_entity_name}", baseEntityName);
        parameterMap.put("${base_entity_namespace}", baseEntityNamespace);

        return fillTemplate(BASE_MODEL_TEMPLATE_STR, parameterMap);
    }

    /**
     * 填充baseEntity模板内容
     * @return 内容
     */
    private String fillBaseEntityTemplate(String tableName) {
        Map<String, String> parameterMap = new HashMap<>();
        parameterMap.put("${namespace}", baseEntityNamespace);
        parameterMap.put("${entity_name}", baseEntityName);

        parameterMap.put("${swagger_import}", isSwagger ?
            "import io.swagger.annotations.ApiModel;\n" +
                "import io.swagger.annotations.ApiModelProperty;\n" : "");
        parameterMap.put("${validator_import}", isValidator ?
            "import org.hibernate.validator.constraints.Length;\n" +
                "\n" +
                "import javax.validation.constraints.Max;\n" +
                "import javax.validation.constraints.Min;\n" : "\n");

        parameterMap.put("${static_fields}", entityStaticField ? fillStaticFieldsTemplate(tableName, true) : "");
        parameterMap.put("${fields}", fillFieldsTemplate(tableName, true));

        return fillTemplate(BASE_ENTITY_TEMPLATE_STR, parameterMap);
    }

    /**
     * 填充model模板内容
     * @param tableName  表名
     * @param modelName  dao对象名
     * @param entityName pojo对象名
     * @return 内容
     */
    private String fillModelTemplate(String tableName, String modelName, String entityName, String primaryKeyType) {
        Map<String, String> parameterMap = new HashMap<>();
        parameterMap.put("${namespace}", modelNamespace);
        parameterMap.put("${base_model_namespace}", baseModelNamespace);
        parameterMap.put("${base_model_name}", baseModelName);
        parameterMap.put("${entity_namespace}", entityNamespace);
        parameterMap.put("${entity_name}", entityName);
        parameterMap.put("${primary_key_type}", primaryKeyType);
        parameterMap.put("${model_name}", modelName);
        parameterMap.put("${is_spring_boot}", isSpringBoot ? "import org.springframework.stereotype.Repository;" +
            "\n\n@Repository" : "");

        return fillTemplate(MODEL_TEMPLATE_STR, parameterMap);
    }

    /**
     * 填充entity模板内容
     * @param tableName  表名
     * @param entityName 对象名
     * @param comment    表注释
     * @return 内容
     */
    private String fillPojoTemplate(String tableName, String entityName, String comment) {
        Map<String, String> parameterMap = new HashMap<>();
        parameterMap.put("${base_entity_namespace}", baseEntityNamespace);
        parameterMap.put("${base_entity_name}", baseEntityName);
        parameterMap.put("${namespace}", entityNamespace);
        parameterMap.put("${entity_name}", entityName);
        parameterMap.put("${table}", tableName);
        parameterMap.put("${swagger_import}", isSwagger ?
            "import io.swagger.annotations.ApiModel;\n" +
                "import io.swagger.annotations.ApiModelProperty;\n" : "");
        parameterMap.put("${validator_import}", isValidator ?
            "import org.hibernate.validator.constraints.Length;\n" +
                "\n" +
                "import javax.validation.constraints.Max;\n" +
                "import javax.validation.constraints.Min;\n" : "\n");
        parameterMap.put("${swagger_annotation}", isSwagger ? "@ApiModel(\"" + comment + "\")\n" : "");
        parameterMap.put("${static_fields}", entityStaticField ? fillStaticFieldsTemplate(tableName, false) : "");
        parameterMap.put("${fields}", fillFieldsTemplate(tableName, false));

        return fillTemplate(ENTITY_TEMPLATE_STR, parameterMap);
    }

    /**
     * 填充所有字段
     * @param tableName       表名
     * @param isForBaseEntity entity父类使用
     * @return 内容
     */
    private String fillFieldsTemplate(String tableName, boolean isForBaseEntity) {
        consoleLog("处理表 : " + tableName);

        StringBuilder str = new StringBuilder();
        // 字段信息
        List<Map<String, Object>> fields = descTable(tableName);

        for (Map<String, Object> field : fields) {
            // 每个字段的填充
            String fieldTemplateStrReplace = fillFieldTemplate(field, tableName, isForBaseEntity);
            // 追加
            str.append(fieldTemplateStrReplace);
        }
        return str.toString();
    }

    /**
     * 静态字段填充
     * @param tableName       表名
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
     * @param tableName            表名
     * @param isForBaseEntity      用于entity父类使用
     * @return 内容
     */
    private String fillFieldTemplate(Map<String, Object> fieldStringObjectMap, String tableName, boolean isForBaseEntity) {

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

        Field field = mysqlFieldGenerator.toField(disInsertable, disUpdatable);

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

        return fillTemplate(FIELD_TEMPLATE_STR, parameterMap);
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
        return getModel().newQuery().queryList("show tables", new ArrayList<>()).toMapList();
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
        return getModel().newQuery()
            .queryList("select * from information_schema.`columns` where table_schema = ? and table_name = ? order by ordinal_position",
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

    public void setDisInsertable(String... column) {
        disInsertable = column;
    }

    public void setDisUpdatable(String... column) {
        disUpdatable = column;
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


    private String getAbsoluteWriteFilePath(String namespace) {
        return StringUtils.rtrim(outputDir, "/") + "/" + namespace2dir(namespace) + '/';
    }

    public static class ToolModel extends ModelBean<ToolModel.Inner, Serializable> {

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
