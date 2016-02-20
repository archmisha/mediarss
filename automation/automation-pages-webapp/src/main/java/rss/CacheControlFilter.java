package rss;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * User: dikmanm
 * Date: 20/03/13 16:48
 */
public class CacheControlFilter implements Filter {

    public static final String ACCEPT_HEADER = "Accept";
    public static final String JSON_CONTENT_TYPE = "application/json";
    private static Logger log = LogManager.getLogger(CacheControlFilter.class);

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        if (req instanceof HttpServletRequest && res instanceof HttpServletResponse) {
            HttpServletRequest request = (HttpServletRequest) req;
            HttpServletResponse response = (HttpServletResponse) res;
            String acceptHeader = request.getHeader(ACCEPT_HEADER);
            if (acceptHeader != null && acceptHeader.contains(JSON_CONTENT_TYPE)) {
//                if (request.getRequestURL() != null && isUrlFiltered(request.getRequestURL().toString())) {
                //set the maximum amount of time (in seconds) that a representation will be considered fresh
//                    response.setHeader("CACHE-CONTROL", MAX_AGE_VALUE);
//                } else{
//                    log.debug("Setting no cache for " + request.getRequestURL());
                response.setHeader("CACHE-CONTROL", "NO-CACHE");
//                }
            }
        } else {
            log.warn("Can not cast request/response into HttpServletRequest/HttpServletResponse - not setting cache-control header");
        }

        chain.doFilter(req, res);
    }

    @Override
    public void destroy() {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
