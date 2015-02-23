package rss;

import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import rss.context.UserContext;
import rss.context.UserContextHolder;
import rss.context.UserContextImpl;
import rss.controllers.UserController;
import rss.dao.UserDao;
import rss.entities.User;
import rss.util.CookieUtils;

import javax.servlet.*;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * User: dikmanm
 * Date: 08/02/2015 09:10
 */
public class UserContextFilter implements Filter {

    private ServletContext sc;
    private Set<String> whiteList;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        sc = filterConfig.getServletContext();
        whiteList = new HashSet<>(Arrays.asList(filterConfig.getInitParameter("urlWhiteList").split(",")));
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain filterChain) throws IOException, ServletException {
        if (!(req instanceof HttpServletRequest && res instanceof HttpServletResponse)) {
            filterChain.doFilter(req, res);
            return;
        }

        try {
            HttpServletRequest request = (HttpServletRequest) req;
            HttpServletResponse response = (HttpServletResponse) res;

            // algorithm:
            // if in white list - continue without context
            // if in session - restore from session
            // if in cookie - restore from cookie
            // otherwise redirect to root
            if (isRequestInWhiteList(request) || buildUserContextFromSession(request) || buildUserContextFromCookie(request)) {
                filterChain.doFilter(req, res);
            } else {
                response.sendRedirect("/");
            }
        } finally {
            UserContextHolder.cleanUserContext();
        }
    }

    private boolean isRequestInWhiteList(HttpServletRequest request) {
        for (String s : whiteList) {
            if (request.getRequestURI().startsWith(s)) {
                return true;
            }
        }
        return false;
    }

    private boolean buildUserContextFromSession(HttpServletRequest request) {
        HttpSession session = request.getSession();
        Object userContext = session.getAttribute(UserController.SESSION_USER_CONTEXT_ATTR);
        if (userContext != null) {
            UserContextHolder.pushUserContext((UserContext) userContext);
            return true;
        }
        return false;
    }

    private boolean buildUserContextFromCookie(HttpServletRequest request) {
        Cookie cookie = CookieUtils.getRememberMeCookie(request);
        if (cookie != null) {
            String[] arr = cookie.getValue().split(",");
            String email = arr[0];
            String series = arr[1];
            String token = arr[2];
            ApplicationContext applicationContext = WebApplicationContextUtils.getWebApplicationContext(sc);
            UserDao userDao = applicationContext.getBean(UserDao.class);
            User user = userDao.findByEmail(email);
            if (user != null && user.getLoginSeries().equals(series)) {
                if (user.getLoginToken().equals(token)) {
//						loggedIn = true;
                    UserContextHolder.pushUserContext(new UserContextImpl(user.getId(), user.getEmail(), user.isAdmin()));
                    return true;
                } else {
                    // theft is assumed
//						loggedIn = false;
                }
            }
        }

        return false;
    }

    @Override
    public void destroy() {

    }
}
