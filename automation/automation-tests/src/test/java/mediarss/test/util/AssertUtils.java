package mediarss.test.util;

import mediarss.test.entities.News;

import java.util.List;

/**
 * User: dikmanm
 * Date: 13/02/2015 15:34
 */
public class AssertUtils {
    public static boolean contains(List<News> list, long... ids) {
        int i = 0;
        for (News obj : list) {
            if (obj.getId() == ids[i]) {
                i++;
            }
            if (i == ids.length) {
                return true;
            }
        }

        return false;
    }
}
