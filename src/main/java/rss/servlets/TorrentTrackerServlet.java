package rss.servlets;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import com.turn.ttorrent.tracker.EmbeddedTracker;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * User: dikmanm
 * Date: 30/01/13 20:45
 */
public class TorrentTrackerServlet extends HttpServlet {

	@Autowired
	private EmbeddedTracker embeddedTracker;

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
		embeddedTracker.handleAnnounceRequest(request, response);
	}
}
