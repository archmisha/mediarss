package rss.test.util;

import com.google.common.base.Function;

import java.util.List;

/**
 * User: dikmanm
 * Date: 13/02/2015 15:34
 */
public class AssertUtils {
    public static <FROM, TO> boolean contains(List<FROM> list, Function<FROM, TO> function, TO... values) {
        int i = 0;
        for (FROM obj : list) {
            if (values[i].equals(function.apply(obj))) {
                i++;
            }
            if (i == values.length) {
                return true;
            }
        }

        return false;
    }
}
