package rss.util;

/**
 * User: Michael Dikman
 * Date: 22/12/12
 * Time: 15:44
 */
public class StringUtils {

    public static String pad(int num, int len) {
        String str = "" + num;
        int i = len - str.length();
        while (i > 0) {
            str = "0" + str;
            --i;
        }
        return str;
    }
}
