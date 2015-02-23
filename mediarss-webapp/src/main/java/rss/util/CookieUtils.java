package rss.util;

import rss.entities.User;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * User: dikmanm
 * Date: 21/02/2015 14:32
 */
public class CookieUtils {

    public static final String REMEMBER_ME_COOKIE_NAME = "media-rss";

    public static void createRememberMeCookie(User user, HttpServletResponse response) {
        user.setLoginSeries(StringUtils2.generateUniqueHash());
        user.setLoginToken(StringUtils2.generateUniqueHash());
        Cookie cookie = new Cookie(REMEMBER_ME_COOKIE_NAME, user.getEmail() + "," + user.getLoginSeries() + "," + user.getLoginToken());
        cookie.setMaxAge(Integer.MAX_VALUE);
        cookie.setPath("/");
        response.addCookie(cookie);
    }

    public static Cookie getRememberMeCookie(HttpServletRequest request) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if (cookie.getName().equals(REMEMBER_ME_COOKIE_NAME)) {
                    return cookie;
                }
            }
        }
        return null;
    }

    public static void invalidateRememberMeCookie(HttpServletRequest request, HttpServletResponse response) {
        // invalidate cookie
        Cookie cookie = getRememberMeCookie(request);
        if (cookie != null) {
            cookie = new Cookie(REMEMBER_ME_COOKIE_NAME, null);
            cookie.setMaxAge(0);
            cookie.setValue(null);
            cookie.setPath("/");
            response.addCookie(cookie);
        }
    }
}
