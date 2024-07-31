package gaarason.database.generator.element;

import java.util.List;

public class JavaElement {

    /**
     * 注释
     */
    protected List<String> javaDocLines;

    /**
     * 可见性
     */
    protected JavaVisibility visibility;

    /**
     * 静态
     */
    protected boolean isStatic;

    /**
     * 不可变
     */
    protected boolean isFinal;

    /**
     * 注解
     */
    protected List<Annotation> annotations;

    /**
     * 将注解转化为String, 已用于渲染模板
     * @param numberOfSpaces 每个注解前面的空格数量
     * @return eg:@ApiModelProperty(value = "消息id", example = "39e0f74f-93fd-224c-0078-988542600fd3", required = true)
     */
    public String toAnnotationsString(int numberOfSpaces) {
        // 对齐用的空格
        StringBuilder spaceBuilder = new StringBuilder();
        for (int i = 0; i < numberOfSpaces; i++) {
            spaceBuilder.append(' ');
        }
        String spaces = spaceBuilder.toString();

        // 注解
        StringBuilder stringBuilder = new StringBuilder();
        for (Annotation annotation : annotations) {
            // eg:        @ApiModelProperty(
            stringBuilder.append(spaces).append('@').append(annotation.getName()).append('(');

            for (Annotation.Attribute attribute : annotation.getAttributes()) {
                // eg:value = "消息id",
                stringBuilder.append(attribute.getName()).append(" = ").append(attribute.getValue()).append(", ");
            }
            if (!annotation.getAttributes().isEmpty()) {
                stringBuilder.deleteCharAt(stringBuilder.length() - 1).deleteCharAt(stringBuilder.length() - 1);
            }
            stringBuilder.append(")\n");
        }
        return stringBuilder.toString();
    }

    public List<String> getJavaDocLines() {
        return javaDocLines;
    }

    public void setJavaDocLines(List<String> javaDocLines) {
        this.javaDocLines = javaDocLines;
    }

    public JavaVisibility getVisibility() {
        return visibility;
    }

    public void setVisibility(JavaVisibility visibility) {
        this.visibility = visibility;
    }

    public boolean isStatic() {
        return isStatic;
    }

    public void setStatic(boolean aStatic) {
        isStatic = aStatic;
    }

    public boolean isFinal() {
        return isFinal;
    }

    public void setFinal(boolean aFinal) {
        isFinal = aFinal;
    }

    public List<Annotation> getAnnotations() {
        return annotations;
    }

    public void setAnnotations(List<Annotation> annotations) {
        this.annotations = annotations;
    }

    /**
     * 注解
     */
    public static class Annotation {

        /**
         * 完全限定类名
         */
        protected String fullyQualifiedClassName;

        /**
         * 类名
         */
        protected String name;

        /**
         * 注解的属性
         */
        protected List<Attribute> attributes;

        public String getFullyQualifiedClassName() {
            return fullyQualifiedClassName;
        }

        public void setFullyQualifiedClassName(String fullyQualifiedClassName) {
            this.fullyQualifiedClassName = fullyQualifiedClassName;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public List<Attribute> getAttributes() {
            return attributes;
        }

        public void setAttributes(List<Attribute> attributes) {
            this.attributes = attributes;
        }

        /**
         * 注解的属性
         */
        public static class Attribute {

            /**
             * 注解的属性键
             */
            protected String name;

            /**
             * 注解的属性值(目前只支持String)
             */
            protected String value;

            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }

            public String getValue() {
                return value;
            }

            public void setValue(String value) {
                this.value = value;
            }
        }
    }

}
