package gaarason.database.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtil {
    final private static Pattern linePattern = Pattern.compile("_(\\w)");

    final private static Pattern humpPattern = Pattern.compile("[A-Z]");

    /**
     * 下划线转驼峰
     * @param str              原字符串
     * @param firstIsUpperCase 大驼峰
     * @return 处理后的字符
     */
    public static String lineToHump(String str, boolean... firstIsUpperCase) {
        str = ltrim(rtrim(str.toLowerCase(), "_"), "_");
        if (firstIsUpperCase.length != 0 && firstIsUpperCase[0]) {
            str = "_" + str;
        }
        Matcher      matcher = linePattern.matcher(str);
        StringBuffer sb      = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(sb, matcher.group(1).toUpperCase());
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    /**
     * 小驼峰转下划线
     * @param str 原字符串
     * @return 处理后的字符
     */
    public static String humpToLine(String str) {
        Matcher      matcher = humpPattern.matcher(str);
        StringBuffer sb      = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(sb, "_" + matcher.group(0).toLowerCase());
        }
        matcher.appendTail(sb);
        return ltrim(sb.toString(), "_");
    }

    /**
     * 移除字符串左侧的所有character
     * @param str       原字符串
     * @param character 将要移除的字符
     * @return 处理后的字符
     */
    public static String ltrim(String str, String character) {
        final int length = character.length();
        if (str.equals("") || str.length() < length)
            return str;
        return str.substring(0, length).equals(character) ? ltrim(str.substring(length), character) : str;
    }

    /**
     * 移除字符串右侧的所有character
     * @param str       原字符串
     * @param character 将要移除的字符
     * @return 处理后的字符
     */
    public static String rtrim(String str, String character) {
        final int length = character.length();
        if (str.equals("") || str.length() < length)
            return str;
        return str.substring(str.length() - length).equals(character) ? rtrim(str.substring(0, str.length() - length),
            character) : str;
    }
}
