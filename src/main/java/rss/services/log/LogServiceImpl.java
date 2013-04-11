package rss.services.log;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import rss.dao.UserDao;
import rss.entities.User;
import rss.services.SessionService;

/**
 * User: Michael Dikman
 * Date: 22/12/12
 * Time: 12:44
 */
@Service
public class LogServiceImpl implements LogService {

	@Autowired
	private ApplicationContext applicationContext;

	@Autowired
	private UserDao userDao;

	@Override
	public void info(Class<?> aClass, String msg) {
		Log log = LogFactory.getLog(aClass);
		log.info(prepareMessage(msg));
	}

	@Override
	public void warn(Class<?> aClass, String msg) {
		Log log = LogFactory.getLog(aClass);
		log.warn(prepareMessage(msg));
	}

	@Override
	public void warn(Class<?> aClass, String msg, Exception ex) {
		Log log = LogFactory.getLog(aClass);
		log.warn(prepareMessage(msg), ex);
	}

	@Override
	public void error(Class<?> aClass, String msg) {
		Log log = LogFactory.getLog(aClass);
		log.error(prepareMessage(msg));
	}

	@Override
	public void error(Class<?> aClass, String msg, Exception ex) {
		Log log = LogFactory.getLog(aClass);
		log.error(prepareMessage(msg), ex);
	}

	@Override
	public void debug(Class<?> aClass, String msg) {
		Log log = LogFactory.getLog(aClass);
		log.debug(prepareMessage(msg));
	}

	private String prepareMessage(String msg) {
		try {
			SessionService sessionService = applicationContext.getBean(SessionService.class);
			if (!sessionService.isUserLogged()) {
				return msg;
			}
			User user = userDao.find(sessionService.getLoggedInUserId());
			return String.format("[%s(%d)] - %s", user.getEmail(), user.getId(), msg);
		} catch (BeansException e) {
			return msg;
		}
	}
}
