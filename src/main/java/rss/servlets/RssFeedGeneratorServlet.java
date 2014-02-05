package rss.servlets;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import rss.dao.UserDao;
import rss.entities.User;
import rss.services.UrlService;
import rss.services.feed.RssFeedGenerator;
import rss.services.log.LogService;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * User: Michael Dikman
 * Date: 24/11/12
 * Time: 16:58
 */
public class RssFeedGeneratorServlet extends HttpServlet {

	private static final long serialVersionUID = 339778056686808601L;

	@Autowired
	@Qualifier("tVShowsRssFeedGeneratorImpl")
	private RssFeedGenerator tvShowsRssFeedGenerator;

	@Autowired
	@Qualifier("moviesRssFeedGeneratorImpl")
	private RssFeedGenerator moviesRssFeedGenerator;

	@Autowired
	private UserDao userDao;

	@Autowired
	private LogService logService;

	@Autowired
	private TransactionTemplate transactionTemplate;

	@Override
	public void init() throws ServletException {
		super.init();

		ServletContext servletContext = getServletContext();
		WebApplicationContext springContext = WebApplicationContextUtils.getWebApplicationContext(servletContext);
		AutowireCapableBeanFactory autowireCapableBeanFactory = springContext.getAutowireCapableBeanFactory();
		autowireCapableBeanFactory.autowireBean(this);
	}

	@Override
	protected void doGet(HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
		final PrintWriter out = response.getWriter();
		final long userId;
		final String type;
		final String feedHash;
		try {
			userId = Integer.parseInt(request.getParameter(UrlService.USER_ID_URL_PARAMETER));
			type = request.getParameter(UrlService.MEDIA_TYPE_URL_PARAMETER);
			feedHash = request.getParameter(UrlService.USER_FEED_HASH_PARAMETER);
		} catch (NumberFormatException e) {
			logService.error(getClass(), "Invalid user parameter: " + e.getMessage() + " in queryString: " + request.getQueryString(), e);
			out.println("Invalid url. Please contact support for assistance");
			return;
		}

		try {
			transactionTemplate.execute(new TransactionCallbackWithoutResult() {
				@Override
				protected void doInTransactionWithoutResult(TransactionStatus arg0) {
					User user = userDao.find(userId);
					if (user == null) {
						logService.error(getClass(), "Invalid user parameter: " + userId);
						out.println("Failed generating feed. Please contact support for assistance");
						return;
					}

					if (type == null) {
						logService.error(getClass(), "Missing type parameter for user: " + user.getEmail() + " (" + userId + ")");
						out.println("Failed generating feed. Please contact support for assistance");
						return;
					}

					if (!user.getFeedHash().equalsIgnoreCase(feedHash)) {
						logService.error(getClass(), "Feed hash does not match for user " + user.getEmail() + " (" + userId + "): " + feedHash);
						out.println("Failed generating feed. Please contact support for assistance");
						return;
					}

					switch (type) {
						case UrlService.TV_SHOWS_RSS_FEED_TYPE: {
							String rssFeed = tvShowsRssFeedGenerator.generateFeed(user);
							response.setContentType("application/rss+xml");
							out.println(rssFeed);
							break;
						}
						case UrlService.MOVIES_RSS_FEED_TYPE: {
							String rssFeed = moviesRssFeedGenerator.generateFeed(user);
							response.setContentType("application/rss+xml");
							out.println(rssFeed);
							break;
						}
						default:
							logService.error(getClass(), "Invalid type parameter: " + type);
							out.println("Invalid url. Please contact support for assistance");
							break;
					}
				}
			});
		} catch (Exception e) {
			logService.error(getClass(), "Invalid feed request (maybe user parameter is invalid=" + userId + "): " + e.getMessage(), e);
			out.println("Failed generating feed. Please contact support for assistance");
		}
	}
}
