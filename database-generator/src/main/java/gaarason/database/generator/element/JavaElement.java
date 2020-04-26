package gaarason.database.generator.element;

import lombok.Data;

import java.util.List;

@Data
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
    protected boolean isStatic = false;

    /**
     * 不可变
     */
    protected boolean isFinal = false;

    /**
     * 注解
     */
    protected List<Annotation> annotations;

    /**
     * 将注解转化为String, 已用于渲染模板
     * @param numberOfSpaces 每个注解前面的空格数量
     * @return eg:@ApiModelProperty(value = "消息id", example = "39e0f74f-93fd-224c-0078-988542600fd3", required = true)
     */
    public String toAnnotationsString(int numberOfSpaces){
        // 对齐用的空格
        StringBuilder spaceBuilder = new StringBuilder();
        for (int i = 0 ; i < numberOfSpaces; i++){
            spaceBuilder.append(" ");
        }
        String spaces = spaceBuilder.toString();

        // 注解
        StringBuilder stringBuilder = new StringBuilder();
        for (Annotation annotation : annotations) {
            // eg:        @ApiModelProperty(
            stringBuilder.append(spaces).append("@").append(annotation.getName()).append("(");

            for (Annotation.Attribute attribute : annotation.getAttributes()) {
                // eg:value = "消息id",
                stringBuilder.append(attribute.getName()).append(" = ").append(attribute.getValue()).append(", ");
            }
            if(annotation.getAttributes().size() > 0){
                stringBuilder.deleteCharAt(stringBuilder.length() - 1).deleteCharAt(stringBuilder.length() - 1);
            }
            stringBuilder.append(")\n");
        }
        return stringBuilder.toString();
    }

    /**
     * 注解
     */
    @Data
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

        /**
         * 注解的属性
         */
        @Data
        public static class Attribute {
            /**
             * 注解的属性键
             */
            protected String name;

            /**
             * 注解的属性值(目前只支持String)
             */
            protected String value;
        }
    }

}
