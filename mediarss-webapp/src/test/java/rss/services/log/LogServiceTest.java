package rss.services.log;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;
import rss.BaseTest;

/**
 * User: Michael Dikman
 * Date: 22/12/12
 * Time: 12:44
 */
@Service
public class LogServiceTest implements LogService {

    @Override
    public void info(Class<?> aClass, String msg) {
        Log log = LogFactory.getLog(aClass);
        log.info(msg);
    }

    @Override
    public void warn(Class<?> aClass, String msg) {
        Log log = LogFactory.getLog(aClass);
        log.warn(msg);
    }

    @Override
    public void warn(Class<?> aClass, String msg, Exception ex) {
        Log log = LogFactory.getLog(aClass);
        log.warn(msg, ex);
    }

    @Override
    public void error(Class<?> aClass, String msg) {
        Log log = LogFactory.getLog(aClass);
        log.error(msg);
        System.setProperty(BaseTest.ERROR_KEY, msg);
    }

    @Override
    public void error(Class<?> aClass, String msg, Exception ex) {
        Log log = LogFactory.getLog(aClass);
        log.error(msg, ex);
        System.setProperty(BaseTest.ERROR_KEY, msg);
    }

    @Override
    public void debug(Class<?> aClass, String msg) {
        Log log = LogFactory.getLog(aClass);
        log.debug(msg);
    }
}
