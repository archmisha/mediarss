package rss.context;

import javax.servlet.http.HttpSession;
import java.util.Stack;

/**
 * User: dikmanm
 * Date: 06/03/2015 11:00
 */
public class SessionUserContext {

    public static final String SESSION_USER_CONTEXT_ATTR = "UserContext";

    private HttpSession session;

    public SessionUserContext(HttpSession session) {
        this.session = session;
    }

    @SuppressWarnings("unchecked")
    public boolean restoreFromSession() {
        Stack<UserContext> uCtxStack = (Stack<UserContext>) session.getAttribute(SESSION_USER_CONTEXT_ATTR);
        if (uCtxStack == null) {
            return false;
        }

        for (UserContext userContext : uCtxStack) {
            UserContextHolder.pushUserContext(userContext);
        }
        return true;
    }

    public void storeInSession() {
        session.setAttribute(SESSION_USER_CONTEXT_ATTR, UserContextHolder.getContextStack());
    }
}
