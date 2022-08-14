package gaarason.database.generator.support;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.Version;
import gaarason.database.generator.appointment.Style;
import gaarason.database.generator.exception.GeneratorException;
import gaarason.database.util.ObjectUtils;
import gaarason.database.util.StringUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class TemplateHelper {

    private static final String TEMPLATE_PATH = "/template";

    private final Configuration configuration;

    private final Map<String, Object> sharedVariableMap = new HashMap<>();

    private final String outputDir;

    public TemplateHelper(Style style, String outputDir) {
        configuration = new Configuration(new Version("2.3.31"));
        configuration.setClassLoaderForTemplateLoading(TemplateHelper.class.getClassLoader(), TEMPLATE_PATH);
        setSharedVariable("style", style.code);
        this.outputDir = outputDir;
    }

    public void writeBaseModel(Map<?, ?> parameters) throws GeneratorException {
        if (!getSharedVariable("style").equals(1)) {
            filePutContent("baseModel", parameters);
        }
    }

    public void writeBaseEntity(Map<?, ?> parameters) throws GeneratorException {
        if (!getSharedVariable("style").equals(2)) {
            filePutContent("baseEntity", parameters);
        }
    }

    public void writeModel(Map<?, ?> parameters) throws GeneratorException {
        if (!getSharedVariable("style").equals(1)) {
            filePutContent("model", parameters);
        }
    }

    public void writeEntity(Map<?, ?> parameters) throws GeneratorException {
        if (!getSharedVariable("style").equals(2)) {
            filePutContent("entity", parameters);
        }
    }

    public void filePutContent(String templateName, Map<?, ?> parameters) {
        String namespace = String.valueOf(parameters.get("namespace"));
        String fileName = String.valueOf(parameters.get("file_name"));
        String path = getAbsoluteWriteFilePath(namespace);
        filePutContent(templateName, path, fileName, parameters);
    }

    /**
     * 通过模版写入文件
     * @param templateName 模版名称 eg: baseModel
     * @param path 写入路径 eg:  ./src/test/java/
     *
     * @param fileName 写入文件名 eg: SssModel
     * @param parameters 模版参数
     * @throws GeneratorException 异常
     */
    public void filePutContent(String templateName, String path, String fileName, Map<?, ?> parameters)
        throws GeneratorException {

        File file = new File(path);
        if (file.exists() || file.mkdirs()) {
            try (Writer out = new BufferedWriter(
                new OutputStreamWriter(Files.newOutputStream(Paths.get(path + fileName + ".java"))))) {
                Template template = configuration.getTemplate(templateName + ".ftl");
                template.process(parameters, out);
                out.flush();
                return;
            } catch (Throwable e) {
                throw new GeneratorException(e);
            }
        }
        throw new GeneratorException("目录建立失败 : " + file);
    }

    protected <V> void setSharedVariable(String key, V value) {
        try {
            sharedVariableMap.put(key, value);
            configuration.setSharedVariable(key, value);
        } catch (Exception e) {
            throw new GeneratorException(e);
        }
    }

    protected <V> V getSharedVariable(String key) {
        try {
            return ObjectUtils.typeCast(sharedVariableMap.get(key));
        } catch (Exception e) {
            throw new GeneratorException(e);
        }
    }

    private String getAbsoluteWriteFilePath(String namespace) {
        return StringUtils.rtrim(outputDir, "/") + "/" + namespace2dir(namespace) + '/';
    }

    /**
     * 将类的命名空间转化为对应的目录
     * @param namespace 命名空间
     * @return 目录
     */
    private static String namespace2dir(String namespace) {
        return namespace.replace('.', '/');
    }
}
