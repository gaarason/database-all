package gaarason.database.generator.element.field;

import gaarason.database.generator.element.base.BaseElement;
import gaarason.database.lang.Nullable;
import gaarason.database.util.StringUtils;

import java.util.regex.Pattern;

/**
 * mysql 数据库字段信息分析
 */
abstract class BaseFieldGenerator {

    protected static final Pattern tinyintPattern = Pattern.compile("tinyint\\((\\d)\\)");

    /**
     * 将不合法的java标识符转换
     * @param name 未验证的java标识符
     * @return 合法的java标识符
     */
    protected static String nameConverter(String name) {
        return StringUtils.isJavaIdentifier(name) ? name : "a" + StringUtils.md5(name);
    }

    /**
     * 字符符替换与转义
     * @param str 原字符串
     * @return 换行符替换后的字符串
     */
    @Nullable
    protected static String safeCharactersToReplace(@Nullable String str) {
        if(null != str){
            str = StringUtils.replace(str,"\r", "");
            str = StringUtils.replace(str,"\n", "");
            str = StringUtils.replace(str,"\\\r", "");
            str = StringUtils.replace(str,"\\\n", "");
            return StringUtils.replace(str,"\"", "");
        }
        return null;
    }

    /**
     * 截取完全限定类名中的类名 java.util.Date -> Date
     * @param classType 全限定类名(类)
     * @return 类名
     */
    protected static String cutClassName(Class<?> classType) {
        String className = classType.getName();
        String[] split = className.split("\\.");
        return split[split.length - 1];
    }

    /**
     * 生成Field
     * @return Field
     */
    public abstract Field toField(BaseElement element);
}
