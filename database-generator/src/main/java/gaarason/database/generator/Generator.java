package gaarason.database.generator;

import gaarason.database.eloquent.Model;
import gaarason.database.generator.element.ColumnAnnotation;
import gaarason.database.generator.element.Field;
import gaarason.database.generator.element.PrimaryAnnotation;
import gaarason.database.utils.StringUtil;
import lombok.Setter;

import java.io.*;
import java.util.*;

abstract public class Generator {

    @Setter
    private String outputDir = "./";

    @Setter
    private String namespace = "data";

    @Setter
    private String entityDir = "entity";

    @Setter
    private String entityPrefix = "";

    @Setter
    private String entitySuffix = "";

    @Setter
    private String modelDir = "model";

    @Setter
    private String modelPrefix = "";

    @Setter
    private String modelSuffix = "Model";

    @Setter
    private String baseModelDir = "base";

    @Setter
    private String baseModelName = "BaseModel";

    @Setter
    private Boolean isSpringBoot = false;

    @Setter
    private Boolean staticField = false;

    private String[] disInsertable = {};

    private String[] disUpdatable = {};

    final private static String entityTemplateStr = fileGetContent(getAbsoluteReadFileName("entity"));

    final private static String fieldTemplateStr = fileGetContent(getAbsoluteReadFileName("field"));

    final private static String baseModelTemplateStr = fileGetContent(getAbsoluteReadFileName("baseModel"));

    final private static String modelTemplateStr = fileGetContent(getAbsoluteReadFileName("model"));

    private String baseModelNamespace;

    private String modelNamespace;

    private String entityNamespace;

    /**
     * @return 数据库操作model
     */
    abstract public Model getModel();

    private void init() {
        baseModelNamespace = namespace + ("".equals(modelDir) ? "" : ("." + modelDir)) + ("".equals(
            baseModelDir) ? "" : ("." + baseModelDir));
        modelNamespace = namespace + ("".equals(modelDir) ? "" : ("." + modelDir));
        entityNamespace = namespace + ("".equals(entityDir) ? "" : ("." + entityDir));
    }

    public void run() {
        // 初始化namespace
        init();
        // 表信息
        List<Map<String, Object>> tables = showTables();
        // baseModel 文件内容
        String baseDaoTemplateStrReplace = fillBaseModelTemplate();
        // baseModel 写入文件
        filePutContent(getAbsoluteWriteFilePath(baseModelNamespace), baseModelName, baseDaoTemplateStrReplace);

        for (Map<String, Object> table : tables) {
            // 单个表
            for (String key : table.keySet()) {
                // 表名
                String tableName = table.get(key).toString();

                // entity文件名
                String pojoName = entityName(tableName);
                // entity文件内容
                String pojoTemplateStrReplace = fillPojoTemplate(tableName, pojoName);
                // entity写入文件
                filePutContent(getAbsoluteWriteFilePath(entityNamespace), pojoName, pojoTemplateStrReplace);

                // model文件名
                String daoName = modelName(tableName);
                // model文件内容
                String daoTemplateStrReplace = fillModelTemplate(tableName, daoName, pojoName);
                // model写入文件
                filePutContent(getAbsoluteWriteFilePath(modelNamespace), daoName, daoTemplateStrReplace);
            }
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
    private String fillModelTemplate(String tableName, String modelName, String entityName) {
        Map<String, String> parameterMap = new HashMap<>();
        parameterMap.put("${namespace}", modelNamespace);
        parameterMap.put("${base_model_namespace}", baseModelNamespace);
        parameterMap.put("${base_model_name}", baseModelName);
        parameterMap.put("${entity_namespace}", entityNamespace);
        parameterMap.put("${entity_name}", entityName);
        parameterMap.put("${model_name}", modelName);
        parameterMap.put("${is_spring_boot}", isSpringBoot ? "import org.springframework.stereotype.Component;" +
            "\n\n@Component" : "");

        return fillTemplate(modelTemplateStr, parameterMap);
    }

    /**
     * 填充entity模板内容
     * @param tableName  表名
     * @param entityName 对象名
     * @return 内容
     */
    private String fillPojoTemplate(String tableName, String entityName) {
        Map<String, String> parameterMap = new HashMap<>();
        parameterMap.put("${namespace}", entityNamespace);
        parameterMap.put("${entity_name}", entityName);
        parameterMap.put("${table}", tableName);
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
            String fieldTemplateStrReplace = fillFieldTemplate(field);
            // 追加
            str.append(fieldTemplateStrReplace);
        }
        return str.toString();
    }

    private String fillStaticFieldsTemplate(String tableName) {
        StringBuilder str = new StringBuilder();
        // 字段信息
        List<Map<String, Object>> fields = descTable(tableName);

        for (Map<String, Object> field : fields) {
            String columnName = field.get("COLUMN_NAME").toString();
            // 每个字段的填充
            String fieldTemplateStrReplace = "    final public static String " + columnName.toUpperCase() +
                " = \"" + columnName + "\";\n";
            // 追加
            str.append(fieldTemplateStrReplace);
        }
        return str.toString();
    }

    /**
     * 填充单个字段
     * @param field 字段属性
     * @return 内容
     */
    private String fillFieldTemplate(Map<String, Object> field) {

        consoleLog("处理字段 : " + field);

        // field
        Field fieldInfo = new Field();
        fieldInfo.setName(StringUtil.lineToHump(field.get("COLUMN_NAME").toString()));
        fieldInfo.setDataType(field.get("DATA_TYPE").toString());
        fieldInfo.setColumnType(field.get("COLUMN_TYPE").toString());

        // @Column
        ColumnAnnotation columnAnnotation = new ColumnAnnotation();
        columnAnnotation.setName(field.get("COLUMN_NAME").toString());
        columnAnnotation.setUnique(field.get("COLUMN_KEY").toString().equals("UNI"));
        columnAnnotation.setUnsigned(field.get("COLUMN_TYPE").toString().contains("unsigned"));
        columnAnnotation.setNullable(field.get("IS_NULLABLE").toString().equals("YES"));
        columnAnnotation.setInsertable(!Arrays.asList(disInsertable).contains(field.get("COLUMN_NAME").toString()));
        columnAnnotation.setUpdatable(!Arrays.asList(disUpdatable).contains(field.get("COLUMN_NAME").toString()));
        if (field.get("CHARACTER_MAXIMUM_LENGTH") != null) {
            columnAnnotation.setLength(Long.valueOf(field.get("CHARACTER_MAXIMUM_LENGTH").toString()));
        }
        columnAnnotation.setComment(field.get("COLUMN_COMMENT").toString());

        // @primary
        PrimaryAnnotation primaryAnnotation = null;
        if (field.get("COLUMN_KEY").toString().equals("PRI")) {
            primaryAnnotation = new PrimaryAnnotation();
            primaryAnnotation.setIncrement(field.get("EXTRA").toString().equals("auto_increment"));
        }

        // 模板替换参数
        Map<String, String> parameterMap = new HashMap<>();
        parameterMap.put("${primary}", primaryAnnotation == null ? "" : primaryAnnotation.toString());
        parameterMap.put("${column}", columnAnnotation.toString());
        parameterMap.put("${field}", fieldInfo.toString());

        return fillTemplate(fieldTemplateStr, parameterMap);
    }

    private String entityName(String tableName) {
        return entityPrefix + StringUtil.lineToHump(tableName, true) + entitySuffix;
    }

    private String modelName(String tableName) {
        return modelPrefix + StringUtil.lineToHump(tableName, true) + modelSuffix;
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> showTables() {
        return getModel().newQuery().queryList("show tables", new ArrayList<>()).toMapList();
    }

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
        Set<String> strings = parameterMap.keySet();
        for (String string : strings) {
            template = template.replace(string, parameterMap.get(string));
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
        BufferedReader br               = new BufferedReader(new InputStreamReader(is));
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
            if (!file.exists()) {
                file.mkdirs();
            }
            FileWriter writer = new FileWriter(path + fileName + ".java", false);
            writer.write(content.replaceAll("\\\\n", "\n"));
            writer.flush();
            writer.close();
            // 控制台输出
            consoleLog(fileName + " 生成完毕, 路径 : " + path);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void consoleLog(String str) {
        System.out.println(str);
    }
}
