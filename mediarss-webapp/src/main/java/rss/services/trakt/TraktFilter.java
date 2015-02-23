package rss.services.trakt;

import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import rss.environment.Environment;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * User: dikmanm
 * Date: 16/02/2015 15:04
 */
public class TraktFilter implements Filter {

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

            // called when we redirected the user to trakt and he authorized us and redirected back to us
            if (request.getParameter("code") != null) {
                ApplicationContext applicationContext = WebApplicationContextUtils.getWebApplicationContext(sc);
                applicationContext.getBean(rss.services.trakt.TraktService.class).authenticateUser(request.getParameter("code"));
//                <script>window.location.href=window.location.href.substring(0, window.location.href.indexOf('?')) + '#<%=request.getParameter("state")%>';</script>
                response.sendRedirect(Environment.getInstance().getServerHostUrl() + "/main#" + request.getParameter("state"));
                return;
            }
        }

        filterChain.doFilter(req, res);
    }

    @Override
    public void destroy() {

    }
}
