package rss;

import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import rss.services.SessionService;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * User: dikmanm
 * Date: 08/02/2015 09:10
 */
public class RememberMeCookieFilter implements Filter {

	private ServletContext sc;

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		sc = filterConfig.getServletContext();
	}

	@Override
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain filterChain) throws IOException, ServletException {
		if (req instanceof HttpServletRequest && res instanceof HttpServletResponse) {
			HttpServletRequest request = (HttpServletRequest) req;
			HttpServletResponse response = (HttpServletResponse) res;

			ApplicationContext applicationContext = WebApplicationContextUtils.getWebApplicationContext(sc);
			SessionService sessionService = applicationContext.getBean(SessionService.class);
			sessionService.restoreUserDataFromCookie(request, response);
		}

		filterChain.doFilter(req, res);
	}

	@Override
	public void destroy() {

	}
}
