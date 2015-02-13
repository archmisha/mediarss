package mediarss.test;

import com.google.common.base.CaseFormat;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * User: dikmanm
 * Date: 12/02/2015 22:44
 */
@Component
public class Reporter {

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yy HH:mm:ss");

    public void info(String s) {
        print(s, "info");
    }

    public void error(String s) {
        print(s, "error");
    }

    public void warning(String s) {
        print(s, "warning");
    }

    public void debug(String s) {
        print(s, "debug");
    }

    private void print(String message, String severity) {
        String time = dateFormat.format(new Date(System.currentTimeMillis()));
        System.out.format("%s [%s] - %s\n", time, CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_UNDERSCORE, severity), message);
    }
}
