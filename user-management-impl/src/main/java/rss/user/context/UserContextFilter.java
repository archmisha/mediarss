package rss.user.context;

import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import rss.user.context.SessionUserContext;
import rss.user.context.UserContextHolder;
import rss.user.context.UserContextImpl;
import rss.user.User;
import rss.user.UserService;
import rss.user.context.CookieUtils;

import javax.servlet.*;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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
            if (request.getRequestURI().equals("/") || isRequestInWhiteList(request) || buildUserContextFromSession(request) || buildUserContextFromCookie(request)) {
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
        return new SessionUserContext(request.getSession()).restoreFromSession();
    }

    private boolean buildUserContextFromCookie(HttpServletRequest request) {
        Cookie cookie = CookieUtils.getRememberMeCookie(request);
        if (cookie != null) {
            String[] arr = cookie.getValue().split(",");
            String email = arr[0];
            String series = arr[1];
            String token = arr[2];
            ApplicationContext applicationContext = WebApplicationContextUtils.getWebApplicationContext(sc);
            UserService userService = applicationContext.getBean(UserService.class);
            User user = userService.findByEmail(email);
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
