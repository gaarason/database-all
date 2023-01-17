package gaarason.database.generator.support;

import gaarason.database.generator.appointment.Style;
import gaarason.database.generator.exception.GeneratorException;
import gaarason.database.util.ObjectUtils;
import gaarason.database.util.StringUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 模版填充
 */
public class TemplateHelper {

    private static final String TEMPLATE_PATH = "/template/";

    private final Map<String, String> templateCache = new ConcurrentHashMap<>();

    private final Map<String, Object> sharedVariableMap = new ConcurrentHashMap<>();

    private final String outputDir;

    public TemplateHelper(Style style, String outputDir) {
        setSharedVariable("style", style.code);
        this.outputDir = outputDir;
    }

    public void writeBaseModel(Map<?, ?> parameters) throws GeneratorException {
        if (!getSharedVariable("style").equals(1)) {
            filePutContent("baseModel", String.valueOf(parameters.get("${base_model_name}")), parameters);
        }
    }

    public void writeBaseEntity(Map<?, ?> parameters) throws GeneratorException {
        if (!getSharedVariable("style").equals(2)) {
            filePutContent("baseEntity", String.valueOf(parameters.get("${base_entity_name}")), parameters);
        }
    }

    public void writeModel(Map<?, ?> parameters) throws GeneratorException {
        if (!getSharedVariable("style").equals(1)) {
            filePutContent("model", String.valueOf(parameters.get("${model_name}")), parameters);
        }
    }

    public void writeEntity(Map<?, ?> parameters) throws GeneratorException {
        if (!getSharedVariable("style").equals(2)) {
            filePutContent("entity", String.valueOf(parameters.get("${entity_name}")), parameters);
        }
    }

    public String fillBaseModelWithinBaseEntity(Map<?, ?> parameters) throws GeneratorException {
        String templateName = "baseModelWithinBaseEntity";
        String templateStr = templateCache.computeIfAbsent(templateName,
            k -> fileGetContent(getAbsoluteReadFileName(templateName)));
        return fillTemplate(templateStr, parameters);
    }

    public String fillModelWithinEntity(Map<?, ?> parameters) throws GeneratorException {
        String templateName = "modelWithinEntity";
        String templateStr = templateCache.computeIfAbsent(templateName,
            k -> fileGetContent(getAbsoluteReadFileName(templateName)));
        return fillTemplate(templateStr, parameters);
    }

    public String fillField(Map<?, ?> parameters) throws GeneratorException {
        String templateName = "field";
        String templateStr = templateCache.computeIfAbsent(templateName,
            k -> fileGetContent(getAbsoluteReadFileName(templateName)));
        return fillTemplate(templateStr, parameters);
    }

    protected void filePutContent(String templateName, String fileName, Map<?, ?> parameters) {
        String namespace = String.valueOf(parameters.get("${namespace}"));
        String path = getAbsoluteWriteFilePath(namespace);
        filePutContent(templateName, path, fileName, parameters);
    }

    /**
     * 通过模版写入文件
     * @param templateName 模版名称 eg: baseModel
     * @param path 写入路径 eg:  ./src/test/java/
     * @param fileName 写入文件名 eg: SssModel
     * @param parameters 模版参数
     * @throws GeneratorException 异常
     */
    protected void filePutContent(String templateName, String path, String fileName, Map<?, ?> parameters)
        throws GeneratorException {
        try {
            String templateStr = templateCache.computeIfAbsent(templateName,
                k -> fileGetContent(getAbsoluteReadFileName(templateName)));

            File file = new File(path);
            if (file.exists() || file.mkdirs()) {
                OutputStreamWriter outputStreamWriter = new OutputStreamWriter(
                    Files.newOutputStream(Paths.get(path + fileName + ".java")), StandardCharsets.UTF_8);
                // 模板填充
                outputStreamWriter.write(fillTemplate(templateStr, parameters));
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
     * 获取文件内容
     * @param fileName 文件名
     * @return 文件内容
     */
    protected static String fileGetContent(String fileName) {
        InputStream is = TemplateHelper.class.getResourceAsStream(fileName);
        assert is != null;
        BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
        String s;
        StringBuilder configContentStr = new StringBuilder();
        try {
            while ((s = br.readLine()) != null) {
                configContentStr.append(s).append("\n");
            }
        } catch (IOException e) {
            throw new GeneratorException(e);
        }
        return StringUtils.rtrim(configContentStr.toString(), "\n");
    }

    /**
     * 设置全局变量
     * @param key 键
     * @param value 值
     * @param <V> 值的类型
     */
    protected <V> void setSharedVariable(String key, V value) {
        try {
            sharedVariableMap.put(key, value);
        } catch (Exception e) {
            throw new GeneratorException(e);
        }
    }

    /**
     * 获取全局变量
     * @param key 键
     * @param <V> 值的类型
     * @return 值
     */
    protected <V> V getSharedVariable(String key) {
        try {
            return ObjectUtils.typeCast(sharedVariableMap.get(key));
        } catch (Exception e) {
            throw new GeneratorException(e);
        }
    }

    /**
     * 填充模板
     * @param template 模板内容
     * @param parameterMap 参数
     * @return 填充后的内容
     */
    protected static String fillTemplate(String template, Map<?, ?> parameterMap) {
        for (Map.Entry<?, ?> entry : parameterMap.entrySet()) {
            template = template.replace(String.valueOf(entry.getKey()), String.valueOf(entry.getValue()));
        }
        return template;
    }

    /**
     * 获取模板绝对路径
     * @param name 文件名
     * @return 绝对路径
     */
    protected static String getAbsoluteReadFileName(String name) {
        return TEMPLATE_PATH + name + ".ftl";
    }

    protected String getAbsoluteWriteFilePath(String namespace) {
        return StringUtils.rtrim(outputDir, "/") + "/" + namespace2dir(namespace) + '/';
    }

    /**
     * 将类的命名空间转化为对应的目录
     * @param namespace 命名空间
     * @return 目录
     */
    protected static String namespace2dir(String namespace) {
        return namespace.replace('.', '/');
    }

    /**
     * 打印记录
     * @param str 记录
     */
    protected static void consoleLog(String str) {
        System.out.println(str);
    }

}
