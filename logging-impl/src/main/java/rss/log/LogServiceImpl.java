package rss.log;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rss.user.context.UserContext;
import rss.user.context.UserContextHolder;
import rss.mail.EmailService;

/**
 * User: Michael Dikman
 * Date: 22/12/12
 * Time: 12:44
 */
@Service
public class LogServiceImpl implements LogService {

    @Autowired
    private EmailService emailService;

    @Override
    public void info(Class<?> aClass, String msg) {
        Logger log = LoggerFactory.getLogger(aClass);
        log.info(prepareMessage(msg));
    }

    @Override
    public void warn(Class<?> aClass, String msg) {
        Logger log = LoggerFactory.getLogger(aClass);
        log.warn(prepareMessage(msg));
    }

    @Override
    public void warn(Class<?> aClass, String msg, Exception ex) {
        Logger log = LoggerFactory.getLogger(aClass);
        log.warn(prepareMessage(msg), ex);
    }

    @Override
    public void error(Class<?> aClass, String msg) {
        Logger log = LoggerFactory.getLogger(aClass);
        String message = prepareMessage(msg);
        log.error(message);

        try {
            emailService.notifyOfError(message);
        } catch (Exception e) {
            log.error("Failed sending the error to admins by email: " + e.getMessage(), e);
        }
    }

    @Override
    public void error(Class<?> aClass, String msg, Exception ex) {
        Logger log = LoggerFactory.getLogger(aClass);
        String message = prepareMessage(msg);
        log.error(message, ex);

        try {
            message += ("\r\n" + ExceptionUtils.getStackTrace(ex));
            emailService.notifyOfError(message);
        } catch (Exception e) {
            log.error("Failed sending the error to admins by email: " + e.getMessage(), e);
        }
    }

    @Override
    public void debug(Class<?> aClass, String msg) {
        Logger log = LoggerFactory.getLogger(aClass);
        log.debug(prepareMessage(msg));
    }

    private String prepareMessage(String msg) {
        if (UserContextHolder.isUserContextEmpty()) {
            return msg;
        }

        UserContext userContext = UserContextHolder.getCurrentUserContext();
        return String.format("[%s(%d)] - %s", userContext.getEmail(), userContext.getUserId(), msg);
    }
}