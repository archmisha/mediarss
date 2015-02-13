package rss.services.log;

/**
 * User: Michael Dikman
 * Date: 22/12/12
 * Time: 12:44
 */
public interface LogService {

    void info(Class<?> aClass, String msg);

	void warn(Class<?> aClass, String msg);

	void warn(Class<?> aClass, String msg, Exception ex);

    void error(Class<?> aClass, String msg);

    void error(Class<?> aClass, String msg, Exception ex);

	void debug(Class<?> aClass, String msg);
}
