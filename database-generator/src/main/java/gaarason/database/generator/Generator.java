package gaarason.database.generator;

import com.alibaba.druid.pool.DruidDataSource;
import gaarason.database.connection.GaarasonDataSourceProvider;
import gaarason.database.core.lang.Nullable;
import gaarason.database.eloquent.Model;
import gaarason.database.generator.element.field.Field;
import gaarason.database.generator.element.field.MysqlFieldGenerator;
import gaarason.database.util.StringUtil;
import lombok.Setter;

import javax.sql.DataSource;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;

public class Generator {

    /**
     * 输出目录
     */
    @Setter
    private String outputDir = "./";

    /**
     * 命名空间
     */
    @Setter
    private String namespace = "data";

    /**
     * entity目录
     */
    @Setter
    private String entityDir = "entity";

    /**
     * entity前缀
     */
    @Setter
    private String entityPrefix = "";

    /**
     * entity后缀
     */
    @Setter
    private String entitySuffix = "";

    /**
     * model目录
     */
    @Setter
    private String modelDir = "model";

    /**
     * model前缀
     */
    @Setter
    private String modelPrefix = "";

    /**
     * model后缀
     */
    @Setter
    private String modelSuffix = "Model";

    /**
     * baseModel目录
     */
    @Setter
    private String baseModelDir = "base";

    /**
     * baseModel类名
     */
    @Setter
    private String baseModelName = "BaseModel";

    /**
     * 是否使用spring boot注解 model
     */
    @Setter
    private Boolean isSpringBoot = false;

    /**
     * 是否使用swagger注解 entity
     */
    @Setter
    private Boolean isSwagger = false;

    /**
     * 是否使用 org.hibernate.validator.constraints.* 注解 entity
     */
    @Setter
    private Boolean isValidator = false;

    /**
     * 是否生成静态字段名
     */
    @Setter
    private Boolean staticField = false;

    /**
     * 生成并发线程数
     */
    @Setter
    private int corePoolSize = 20;

    /**
     * 新增时,不可通过代码更改的字段
     */
    private String[] disInsertable = {};

    /**
     * 更新时,不可通过代码更改的字段
     */
    private String[] disUpdatable = {};

    final private static String entityTemplateStr = fileGetContent(getAbsoluteReadFileName("entity"));

    final private static String fieldTemplateStr = fileGetContent(getAbsoluteReadFileName("field"));

    final private static String baseModelTemplateStr = fileGetContent(getAbsoluteReadFileName("baseModel"));

    final private static String modelTemplateStr = fileGetContent(getAbsoluteReadFileName("model"));

    private String baseModelNamespace;

    private String modelNamespace;

    private String entityNamespace;

    private Model model;

    private ConcurrentHashMap<String, String> tablePrimaryKeyTypeMap = new ConcurrentHashMap<>();

    /**
     * 使用无惨可重写
     * @return 数据库操作model
     */
    public Model getModel() {
        return model;
    }

    /**
     * 使用无参构造时,需要重写 getModel 方法
     */
    public Generator() {

    }

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

        model = new ToolModel(new GaarasonDataSourceProvider(dataSources));
    }

    protected static class ToolModel extends Model<ToolModel.Inner, Object> {
        private GaarasonDataSourceProvider gaarasonDataSourceProvider;

        public ToolModel(GaarasonDataSourceProvider dataSource) {
            gaarasonDataSourceProvider = dataSource;
        }

        public GaarasonDataSourceProvider getGaarasonDataSource() {
            return gaarasonDataSourceProvider;
        }

        public static class Inner {
        }
    }

    /**
     * 初始化相关名称
     */
    private void init() {
        if (getModel() == null) {
            throw new RuntimeException("使用无参构造`public void Generator()`时,需要重写`getModel`方法,否则请使用`public void " +
                "Generator(String jdbcUrl, String username, String password)`");
        }
        baseModelNamespace = namespace + ("".equals(modelDir) ? "" : ("." + modelDir)) + ("".equals(
            baseModelDir) ? "" : ("." + baseModelDir));
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
                        .orElse("Object");
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

        return fillTemplate(baseModelTemplateStr, parameterMap);
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

        return fillTemplate(modelTemplateStr, parameterMap);
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
        parameterMap.put("${static_fields}", staticField ? fillStaticFieldsTemplate(tableName) : "");
        parameterMap.put("${fields}", fillFieldsTemplate(tableName));

        return fillTemplate(entityTemplateStr, parameterMap);
    }

    /**
     * 填充所有字段
     * @param tableName 表名
     * @return 内容
     */
    private String fillFieldsTemplate(String tableName) {
        consoleLog("处理表 : " + tableName);

        StringBuilder str = new StringBuilder();
        // 字段信息
        List<Map<String, Object>> fields = descTable(tableName);

        for (Map<String, Object> field : fields) {
            // 每个字段的填充
            String fieldTemplateStrReplace = fillFieldTemplate(field, tableName);
            // 追加
            str.append(fieldTemplateStrReplace);
        }
        return str.toString();
    }

    /**
     * 静态字段填充
     * @param tableName 表名
     * @return 内容
     */
    private String fillStaticFieldsTemplate(String tableName) {
        StringBuilder str = new StringBuilder();
        // 字段信息
        List<Map<String, Object>> fields = descTable(tableName);

        for (Map<String, Object> field : fields) {
            // 原字段名
            String columnName = field.get("COLUMN_NAME").toString();
            // 静态字段名
            String staticName = nameConverter(columnName).toUpperCase();

            // 每个字段的填充(避免静态字段与普通属性名一样导致冲突, 一样时使用$前缀)
            String fieldTemplateStrReplace =
                "    final public static String " + (staticName.equals(columnName) ? "$" : "") + staticName +
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
     * @return 内容
     */
    private String fillFieldTemplate(Map<String, Object> fieldStringObjectMap, String tableName) {

        System.out.println(fieldStringObjectMap);
        // 判断数据源类型 目前仅支持mysql

        MysqlFieldGenerator mysqlFieldGenerator = new MysqlFieldGenerator();
        mysqlFieldGenerator.setTableCatalog(getValue(fieldStringObjectMap, MysqlFieldGenerator.TABLE_CATALOG));
        mysqlFieldGenerator.setIsNullable(getValue(fieldStringObjectMap, MysqlFieldGenerator.IS_NULLABLE));
        mysqlFieldGenerator.setTableName(getValue(fieldStringObjectMap, MysqlFieldGenerator.TABLE_NAME));
        mysqlFieldGenerator.setTableSchema(getValue(fieldStringObjectMap, MysqlFieldGenerator.TABLE_NAME));
        mysqlFieldGenerator.setExtra(getValue(fieldStringObjectMap, MysqlFieldGenerator.EXTRA));
        mysqlFieldGenerator.setColumnName(getValue(fieldStringObjectMap, MysqlFieldGenerator.COLUMN_NAME));
        mysqlFieldGenerator.setColumnKey(getValue(fieldStringObjectMap, MysqlFieldGenerator.COLUMN_KEY));
        mysqlFieldGenerator.setCharacterOctetLength(
            getValue(fieldStringObjectMap, MysqlFieldGenerator.CHARACTER_OCTET_LENGTH));
        mysqlFieldGenerator.setNumericPrecision(getValue(fieldStringObjectMap, MysqlFieldGenerator.NUMERIC_PRECISION));
        mysqlFieldGenerator.setPrivileges(getValue(fieldStringObjectMap, MysqlFieldGenerator.PRIVILEGES));
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
        if (field.getPrimary()) {
            tablePrimaryKeyTypeMap.put(tableName, field.getJavaClassTypeString());
        }

        // 模板替换参数
        Map<String, String> parameterMap = new HashMap<>();
        parameterMap.put("${primary}", field.toAnnotationDatabasePrimary());
        parameterMap.put("${column}", field.toAnnotationDatabaseColumn());
        parameterMap.put("${field}", field.toFieldName());
        parameterMap.put("${apiModelProperty}", isSwagger ? field.toAnnotationSwaggerAnnotationsApiModelProperty() :
            "");
        parameterMap.put("${validator}", isValidator ? field.toAnnotationOrgHibernateValidatorConstraintValidator() :
            "");

        return fillTemplate(fieldTemplateStr, parameterMap);
    }

    /**
     * 由表名生成实体类名
     * @param tableName 表名
     * @return 实体类名
     */
    private String entityName(String tableName) {
        String name = entityPrefix + StringUtil.lineToHump(tableName, true) + entitySuffix;
        return nameConverter(name);
    }

    /**
     * 由表名生成模型类名
     * @param tableName 表名
     * @return 模型类名
     */
    private String modelName(String tableName) {
        String name = modelPrefix + StringUtil.lineToHump(tableName, true) + modelSuffix;
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
            .queryList("select * from information_schema.`columns` where table_schema = ? and table_name = ? ",
                parameters)
            .toMapList();
    }

    private static String namespace2dir(String namespace) {
        return namespace.replace('.', '/');
    }

    /**
     * @return 数据库库名
     */
    @SuppressWarnings("unchecked")
    private String DBName() {
        String              name  = "";
        Model               model = getModel();
        Map<String, Object> map   = model.newQuery().queryOrFail("select database()", new ArrayList<>()).toMap();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (entry.getValue() != null) {
                name = entry.getValue().toString();
                break;
            }
        }
        if ("".equals(name)) {
            throw new RuntimeException("获取当前库名失败");
        }
        return name;
    }

    public void setDisInsertable(String... column) {
        disInsertable = column;
    }

    public void setDisUpdatable(String... column) {
        disUpdatable = column;
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

    private static String getAbsoluteReadFileName(String name) {
        return "/template/" + name;
    }

    private String getAbsoluteWriteFilePath(String namespace) {
        return StringUtil.rtrim(outputDir, "/") + "/" + namespace2dir(namespace) + '/';
    }

    private static String fileGetContent(String fileName) {
        InputStream    is               = Generator.class.getResourceAsStream(fileName);
        BufferedReader br               = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
        String         s;
        StringBuilder  configContentStr = new StringBuilder();
        try {
            while ((s = br.readLine()) != null) {
                configContentStr.append(s);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return configContentStr.toString();
    }

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
            throw new RuntimeException("目录建立失败 : " + file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 将不合法的java标识符转换
     * @param name 未验证的java标识符
     * @return 合法的java标识符
     */
    private static String nameConverter(String name) {
        return StringUtil.isJavaIdentifier(name) ? name : "a" + StringUtil.md5(name);
    }

    /**
     * 记录
     * @param str
     */
    private static void consoleLog(String str) {
        System.out.println(str);
    }

    /**
     * 获取值,并转化为字符串 or Null
     * @param fieldStringObjectMap
     * @param keyName
     * @return 字符串 or Null
     */
    @Nullable
    private static String getValue(Map<String, Object> fieldStringObjectMap, String keyName) {
        return Optional.ofNullable(fieldStringObjectMap.get(keyName)).map(Object::toString).orElse(null);
    }
}
