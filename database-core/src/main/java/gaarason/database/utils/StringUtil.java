package gaarason.database.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtil {
    final private static String[] javaKeywords = {"abstract", "case", "continue", "enum", "for", "instanceof", "new",
        "return", "switch", "transient", "assert", "catch", "default", "extends", "goto", "int", "package", "short",
        "synchronized", "try", "boolean", "char", "do", "final", "if", "interface", "private", "static", "this", "void",
        "break", "class", "double", "finally", "implements", "long", "protected", "strictfp", "throw", "volatile", "byte",
        "const", "else", "float", "import", "native", "public", "super", "throws", "while", "byValue", "cast", "false",
        "future", "generic", "inner", "null", "operator", "outer", "rest", "true", "var"};

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

    /**
     * 是否是合法的标识符
     * @param input 输入字符串
     * @return 是否
     */
    public static boolean isJavaIdentifier(String input) {
        if (input != null && input.length() > 0) {
            int pos = 0;
            if (Character.isJavaIdentifierStart(input.charAt(pos))) {
                while (++pos < input.length()) {
                    if (!Character.isJavaIdentifierPart(input.charAt(pos))) {
                        return false;
                    }
                }
                return !isJavaKeyword(input);
            }
        }
        return false;
    }

    /**
     * 是否为java关键字
     * @param input 输入字符串
     * @return 是否
     */
    public static boolean isJavaKeyword(String input) {
        List<String> keyList = Arrays.asList(javaKeywords);
        return keyList.contains(input);
    }

    /**
     * md5
     * @param input 输入
     * @return 输出
     */
    public static String md5(String input) {
        char[] hexDigits = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
        try {
            byte[] btInput = input.getBytes(StandardCharsets.UTF_8);
            // 获得MD5摘要算法的 MessageDigest 对象
            MessageDigest mdInst = MessageDigest.getInstance("MD5");
            // 使用指定的字节更新摘要
            mdInst.update(btInput);
            // 获得密文
            byte[] md = mdInst.digest();
            // 把密文转换成十六进制的字符串形式
            int    j   = md.length;
            char[] str = new char[j * 2];
            int    k   = 0;
            for (byte byte0 : md) {
                str[k++] = hexDigits[byte0 >>> 4 & 0xf];
                str[k++] = hexDigits[byte0 & 0xf];
            }
            return new String(str);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
