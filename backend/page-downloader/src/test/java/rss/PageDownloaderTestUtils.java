package rss;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import static org.junit.Assert.fail;

/**
 * User: dikmanm
 * Date: 17/10/2015 19:54
 */
public class PageDownloaderTestUtils {

    public static String loadPage(String filename) {
        String page = null;
        try {
            if (!filename.contains(".")) {
                filename = filename + ".html";
            }
            File file = new ClassPathResource("webpages/" + filename, PageDownloaderTestUtils.class.getClassLoader()).getFile();
            page = IOUtils.toString(new FileReader(file));
        } catch (IOException e) {
            fail(ExceptionUtils.getStackTrace(e));
        }
        return page;
    }
}
