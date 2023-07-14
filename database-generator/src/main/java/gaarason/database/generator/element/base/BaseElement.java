package gaarason.database.generator.element.base;

import gaarason.database.appointment.FinalVariable;
import gaarason.database.generator.support.TypeReference;
import gaarason.database.util.StringUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

abstract public class BaseElement {

    /**
     * 分割符号
     */
    protected static final String SEPARATOR_SYMBOL = FinalVariable.Symbol.SEPARATOR;
    
    /**
     * 包名 eg: gaarason.database.generator.element.base.
     */
    private String namespace = "";

    /**
     * Import
     */
    private final LinkedList<String> imports = new LinkedList<>();

    /**
     * 类名 eg: BaseElement
     * 即文件名
     */
    private String className = "";

    /**
     * type 转化到名字
     * 并将其加入 import
     * @param typeReference 类型 eg: new TypeReference<List<Map<Object, Integer>>>() {}
     * @return 类型名 eg: List<Map<Object, Integer>>
     */
    public String type2Name(TypeReference<?> typeReference) {
        Type type = typeReference.getType();
        return type2Name(type);
    }

    /**
     * type 转化到名字
     * 并将其加入 import
     * @param typeString 类型 eg: gaarason.database.generator.element.base.BaseElement
     * @return 类型名 eg: BaseElement
     */
    public String type2Name(String typeString) {
        addImports(typeString);
        String[] split = typeString.split(SEPARATOR_SYMBOL);
        // 兼容内部类
        return StringUtils.replace(split[split.length - 1], "$", ".");
    }

    /**
     * type 转化到名字
     * 并将其加入 import
     * @param annotation 类型 eg: gaarason.database.generator.element.base.BaseElement.class
     * @return 类型名 eg: BaseElement
     */
    public String type2Name(Class<?> annotation) {
        String typeName = annotation.getTypeName();
        addImports(typeName);
        String[] split = typeName.split(SEPARATOR_SYMBOL);
        // 兼容内部类
        return StringUtils.replace(split[split.length - 1], "$", ".");
    }

    /**
     * type 转化到名字
     * 并将其加入 import
     * @param annotation 类型 eg: gaarason.database.generator.element.base.BaseElement.class
     * @return 类型名 eg: @BaseElement
     */
    public String anno2Name(Class<? extends Annotation> annotation) {
        return "@" + type2Name(annotation);
    }

    /**
     * type 转化到名字
     * 并将其加入 import
     * @param typeString 类型 eg: gaarason.database.generator.element.base.BaseElement
     * @return 类型名 eg: @BaseElement
     */
    public String anno2Name(String typeString) {
        return "@" + type2Name(typeString);
    }


    /**
     * 合并所有的Import, 相同的key不会重复
     * @param imports 外部的所有的Import
     */
    public void addAllImports(LinkedList<String> imports) {
        for (String clazz : imports) {
            addImports(clazz);
        }
    }

    /**
     * 返回所有的Import
     * @return 所有的Import
     */
    public LinkedList<String> getImports() {
        return imports;
    }

    /**
     * 打印所有的Import
     * @return 所有的Import的代码块
     */
    public String printImports() {
        List<String> importsStr = new LinkedList<>();
        for (String typeName : imports) {
            // 处理注解
            typeName = StringUtils.replace(typeName, "@", "");

            // 省略 java.lang.
            if ("java.lang.".equals(getNamespace(typeName))) {
                continue;
            }

            // 省略 当前包
            if (namespace.equals(getNamespace(typeName))) {
                continue;
            }

            // 兼容内部类
            if (typeName.contains("$")) {
                int index = typeName.indexOf('$');
                typeName = typeName.substring(0, index);
            }

            String importStr = "import " + typeName + ";";
            // 去重, 兼容内部类的情况
            if (!importsStr.contains(importStr)) {
                importsStr.add(importStr);
            }
        }
        return importsStr.stream().sorted().collect(Collectors.joining("\n"));
    }

    /**
     * 获取设置的 namespace
     * @return namespace
     */
    public String getNamespace() {
        return namespace;
    }

    /**
     * 设置 namespace
     * @param namespace namespace
     */
    public void setNamespace(String namespace) {
        this.namespace = StringUtils.rtrim(namespace, ".") + ".";
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    /**
     * type转化到名字
     * @param type 类型
     * @return 类型名
     */
    protected String type2Name(Type type) {
        String typeName = type.getTypeName();
        if (type instanceof Class) {
            addImports(type.getTypeName());
            String[] split = typeName.split(SEPARATOR_SYMBOL);
            // 兼容内部类
            return StringUtils.replace(split[split.length - 1], "$", ".");
        } else if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;
            Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
            Type rawType = parameterizedType.getRawType();
            addImports(((Class<?>) rawType).getTypeName());
            String[] split = rawType.getTypeName().split(SEPARATOR_SYMBOL);
            StringBuilder stringBuilder = new StringBuilder(split[split.length - 1]).append("<");
            List<String> sss = new LinkedList<>();
            for (Type typeArgument : actualTypeArguments) {
                sss.add(type2Name(typeArgument));
            }
            stringBuilder.append(String.join(", ", sss)).append(">");
            return stringBuilder.toString();
        }
        return typeName;
    }

    /**
     * 加入Import
     * @param classes 类型
     */
    protected void addImports(String... classes) {
        for (String clazz : classes) {
            if (!imports.contains(clazz)) {
                imports.add(clazz);
            }
        }
    }

    /**
     * 根据typeName截取其中的package
     * @param typeName eg:java.lang.String
     * @return eg:java.lang.
     */
    protected static String getNamespace(String typeName) {
        StringBuilder stringBuilder = new StringBuilder();
        String[] strings = typeName.split(SEPARATOR_SYMBOL);
        for (int i = 0; i < strings.length - 1; i++) {
            stringBuilder.append(strings[i]).append(".");
        }
        return stringBuilder.toString();
    }

}
