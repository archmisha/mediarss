package rss.servlets;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import rss.dao.UserDao;
import rss.services.UrlService;
import rss.entities.User;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * User: Michael Dikman
 * Date: 06/12/12
 * Time: 22:47
 */
public class RegisterServlet extends HttpServlet {

    private static final long serialVersionUID = 2432977559774323426L;

    private static Log log = LogFactory.getLog(RegisterServlet.class);

    @Autowired
    private UserDao userDao;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    private UrlService urlService;

    @Override
    public void init() throws ServletException {
        super.init();

        ServletContext servletContext = getServletContext();
        WebApplicationContext springContext = WebApplicationContextUtils.getWebApplicationContext(servletContext);
        AutowireCapableBeanFactory autowireCapableBeanFactory = springContext.getAutowireCapableBeanFactory();
        autowireCapableBeanFactory.autowireBean(this);
    }

    @Override
    protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
        final PrintWriter out = response.getWriter();
        response.setContentType("text/html");

		long userId = -1;
        try {
            userId = Integer.parseInt(request.getParameter(UrlService.USER_ID_URL_PARAMETER));
            final String hash = request.getParameter(UrlService.HASH_URL_PARAMETER);

			final long finalUserId = userId;
			transactionTemplate.execute(new TransactionCallbackWithoutResult() {
                @Override
                protected void doInTransactionWithoutResult(TransactionStatus arg0) {
                    User user = userDao.find(finalUserId);
                    if (user == null) {
                        log.error("Invalid user parameter: " + finalUserId);
                        out.println("Invalid url. Please contact support for assistance");
                        return;
                    }

                    // maybe user clicked twice the same link so validation hash is already null
                    if (user.getValidationHash() == null || user.getValidationHash().equals(hash)) {
                        user.setValidationHash(null);

						response.setHeader("Refresh", "5;url=" + urlService.getApplicationUrl());
                        out.println("<html><body>Account validated. Click <a href=\"" + urlService.getApplicationUrl() +
									"\">here</a> to proceed or wait to be automatically redirected</body></html>");
                    } else {
                        // invalid hash
                        log.error("Invalid hash validation for user: " + user);
                        out.println("Invalid url. Please contact support for assistance");
                    }
                }
            });
        } catch (NumberFormatException e) {
            log.error("Invalid user parameter: " + e.getMessage(), e);
            out.println("Invalid url. Please contact support for assistance");
        } catch (Exception e) {
            log.error("Invalid request (maybe user parameter is invalid " + userId + "): " + e.getMessage(), e);
            out.println("Invalid url. Please contact support for assistance");
        }
    }
}
