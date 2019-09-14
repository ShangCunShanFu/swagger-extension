package cn.hgd11.swagger.extension.util;

/**
 * @author ：尚村山夫
 * @date ：Created in 2019/9/14 00:56
 * @modified By：
 */
public class StringUtils {

    public static String upperFirstChar(String oldStr) {
        char firstChar;
        if ((firstChar = oldStr.charAt(0)) > 65 && firstChar <= 90) {
            // 如果oldStr本来就符合首字母大写，则直接返回
            return oldStr;
        }

        if (firstChar < 97 || firstChar > 122) {
            // 首字母不是英文字母，如首字母是"_"开头
            return oldStr;
        }

        char[] cache = new char[oldStr.length()];
        oldStr.getChars(0, oldStr.length(), cache, 0);

        cache[0] = (char) (firstChar - 32);

        return new String(cache);
    }

    public static String getOrDefault(String sourceString, String defaultString) {
        if (isBlank(sourceString)) {
            return defaultString;
        }

        return sourceString;
    }

    public static boolean isBlank(String sourceString) {
        if (sourceString == null || sourceString.trim().isEmpty()) {
            return true;
        }

        return false;
    }
}
