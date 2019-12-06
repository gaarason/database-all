package gaarason.database.generator;

import gaarason.database.eloquent.Model;
import gaarason.database.generator.element.ColumnAnnotation;
import gaarason.database.generator.element.Field;
import gaarason.database.generator.element.PrimaryAnnotation;
import gaarason.database.utils.StringUtil;
import lombok.Setter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

abstract public class Manager {

    @Setter
    private String namespace = "temp";

    @Setter
    private String pojoDir = "pojo";

    @Setter
    private String pojoPrefix = "";

    @Setter
    private String pojoSuffix = "";

    @Setter
    private String daoDir = "model";

    @Setter
    private String daoPrefix = "";

    @Setter
    private String daoSuffix = "Model";

    @Setter
    private String baseDaoDir = "base";

    @Setter
    private String baseDaoName = "BaseModel";

    @Setter
    private Boolean staticField = false;

    private String[] disInsertable = {};

    private String[] disUpdatable = {};

    final private static String pojoTemplateStr = fileGetContent(getAbsoluteReadFileName("pojo"));

    final private static String fieldTemplateStr = fileGetContent(getAbsoluteReadFileName("field"));

    final private static String baseDaoTemplateStr = fileGetContent(getAbsoluteReadFileName("baseDao"));

    final private static String daoTemplateStr = fileGetContent(getAbsoluteReadFileName("dao"));

    private String baseDaoNamespace;

    private String daoNamespace;

    private String pojoNamespace;

    /**
     * @return 数据库操作model
     */
    abstract public Model getModel();

    private void init() {
        baseDaoNamespace = namespace + ("".equals(daoDir) ? "" : ("." + daoDir)) + ("".equals(
            baseDaoDir) ? "" : ("." + baseDaoDir));
        daoNamespace = namespace + ("".equals(daoDir) ? "" : ("." + daoDir));
        pojoNamespace = namespace + ("".equals(pojoDir) ? "" : ("." + pojoDir));
    }

    public void run() {
        // 初始化namespace
        init();
        // 表信息
        List<Map<String, Object>> tables = showTables();
        // baseDao 文件内容
        String baseDaoTemplateStrReplace = fillBaseDaoTemplate();
        // baseDao 写入文件
        filePutContent(getAbsoluteWriteFilePath(baseDaoNamespace), baseDaoName, baseDaoTemplateStrReplace);

        for (Map<String, Object> table : tables) {
            // 单个表
            for (String key : table.keySet()) {
                // 表名
                String tableName = table.get(key).toString();

                // pojo文件名
                String pojoName = pojoName(tableName);
                // pojo文件内容
                String pojoTemplateStrReplace = fillPojoTemplate(tableName, pojoName);
                // pojo写入文件
                filePutContent(getAbsoluteWriteFilePath(pojoNamespace), pojoName, pojoTemplateStrReplace);

                // dao文件名
                String daoName = daoName(tableName);
                // dao文件内容
                String daoTemplateStrReplace = fillDaoTemplate(tableName, daoName, pojoName);
                // dao写入文件
                filePutContent(getAbsoluteWriteFilePath(daoNamespace), daoName, daoTemplateStrReplace);
            }
        }
    }

    /**
     * 填充baseDao模板内容
     * @return 内容
     */
    private String fillBaseDaoTemplate() {
        Map<String, String> parameterMap = new HashMap<>();
        parameterMap.put("${namespace}", baseDaoNamespace);
        parameterMap.put("${dao_name}", baseDaoName);

        return fillTemplate(baseDaoTemplateStr, parameterMap);
    }

    /**
     * 填充dao模板内容
     * @param tableName 表名
     * @param daoName   dao对象名
     * @param pojoName  pojo对象名
     * @return 内容
     */
    private String fillDaoTemplate(String tableName, String daoName, String pojoName) {
        Map<String, String> parameterMap = new HashMap<>();
        parameterMap.put("${namespace}", daoNamespace);
        parameterMap.put("${base_dao_namespace}", baseDaoNamespace);
        parameterMap.put("${base_dao_name}", baseDaoName);
        parameterMap.put("${pojo_namespace}", pojoNamespace);
        parameterMap.put("${pojo_name}", pojoName);
        parameterMap.put("${dao_name}", daoName);

        return fillTemplate(daoTemplateStr, parameterMap);
    }

    /**
     * 填充pojo模板内容
     * @param tableName 表名
     * @param pojoName  对象名
     * @return 内容
     */
    private String fillPojoTemplate(String tableName, String pojoName) {
        Map<String, String> parameterMap = new HashMap<>();
        parameterMap.put("${namespace}", pojoNamespace);
        parameterMap.put("${pojo_name}", pojoName);
        parameterMap.put("${table}", tableName);
        parameterMap.put("${static_fields}", staticField ? fillStaticFieldsTemplate(tableName) : "");
        parameterMap.put("${fields}", fillFieldsTemplate(tableName));

        return fillTemplate(pojoTemplateStr, parameterMap);
    }

    /**
     * 填充所有字段
     * @param tableName 表名
     * @return 内容
     */
    private String fillFieldsTemplate(String tableName) {
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
            columnAnnotation.setLength(Integer.valueOf(field.get("CHARACTER_MAXIMUM_LENGTH").toString()));
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

    private String pojoName(String tableName) {
        return pojoPrefix + StringUtil.lineToHump(tableName, true) + pojoSuffix;
    }

    private String daoName(String tableName) {
        return daoPrefix + StringUtil.lineToHump(tableName, true) + daoSuffix;
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
        return Thread.currentThread().getStackTrace()[1].getClass().getResource("/").toString().replace(
            "file:", "") + "../../src" +
            "/main/java/gaarason/database/generator/template/" + name;
    }

    private String getAbsoluteWriteFilePath(String namespace) {
        return Thread.currentThread().getStackTrace()[1].getClass()
            .getResource("/")
            .toString()
            .replace("file:", "") + "../../src/test/java/" + namespace2dir(namespace) + '/';
    }

    private static String fileGetContent(String fileName) {
        try {
            String          encoding    = "UTF-8";
            File            file        = new File(fileName);
            Long            filelength  = file.length();
            byte[]          filecontent = new byte[filelength.intValue()];
            FileInputStream in          = new FileInputStream(file);
            in.read(filecontent);
            in.close();
            return new String(filecontent, encoding);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void filePutContent(String path, String fileName, String content) {
        try {
            File file = new File(path);
            if (!file.exists()) {
                file.mkdirs();
            }
            FileWriter writer = new FileWriter(path + fileName + ".java", false);
            writer.write(content);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
