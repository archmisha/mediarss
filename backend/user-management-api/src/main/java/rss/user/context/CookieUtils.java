package rss.user.context;

import rss.user.User;
import rss.util.StringUtils2;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;

/**
 * User: dikmanm
 * Date: 21/02/2015 14:32
 */
public class CookieUtils {

    public static final String REMEMBER_ME_COOKIE_NAME = "media-rss";

    public static NewCookie createRememberMeCookie(User user) {
        user.setLoginSeries(StringUtils2.generateUniqueHash());
        user.setLoginToken(StringUtils2.generateUniqueHash());
        return new NewCookie(REMEMBER_ME_COOKIE_NAME, user.getEmail() + "," + user.getLoginSeries() + "," + user.getLoginToken(), "/", null, null, Integer.MAX_VALUE, false);
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

    public static void invalidateRememberMeCookie(String rememberMeCookie, Response.ResponseBuilder responseBuilder) {
        if (rememberMeCookie != null) {
            NewCookie cookie = new NewCookie(REMEMBER_ME_COOKIE_NAME, null, "/", null, null, 0, false);
            responseBuilder.cookie(new NewCookie(cookie));
        }
    }
}