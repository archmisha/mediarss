package rss.test.util;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

/**
 * User: dikmanm
 * Date: 15/08/2015 23:57
 */
@Component
public class Unique {
    public String unique() {
        return RandomStringUtils.randomAlphanumeric(5).toLowerCase();
    }

    public String appendUnique(String str) {
        return appendUnique(str, "_");
    }

    public String appendUnique(String str, String separator) {
        if (str == null || StringUtils.isBlank(str)) {
            return unique();
        }
        return str + separator + unique();
    }

    public long randomLong() {
        return System.currentTimeMillis();
    }

    public int randomInt() {
        return RandomUtils.nextInt(1, Integer.MAX_VALUE);
    }
}
