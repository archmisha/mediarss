package rss.user.context;

import java.util.Stack;

/**
 * <p>A utility class for acquiring current user context.</p>
 * <p/>
 * <p>Notes:
 * <ol>
 * <li>No null {@code UserContext}s are allowed to be pushed into {@code ContextHolder}'s stacks.</li>
 * <li>If popping a {@code UserContext} results in an empty stack for this thread, this thread's entry is removed altogether.</li>
 * </ol>
 * </p>
 */

public final class UserContextHolder {

    /**
     * Empty default private constructor
     */
    private UserContextHolder() {
    }

    private static final ThreadLocal<Stack<UserContext>> HOLDER = new ThreadLocal<Stack<UserContext>>() {
        @Override
        protected Stack<UserContext> initialValue() {
            return new Stack<>();
        }
    };


    /**
     * @return {@code true} if no current {@code UserContext} exists, {@code false} otherwise
     */
    public static boolean isUserContextEmpty() {
        Stack<UserContext> uCtxStack = HOLDER.get();
        if (uCtxStack.empty()) {
            cleanUserContext();
            return true;
        }
        return false;
    }

    /**
     * @return current {@code UserContext}
     * @throws java.util.EmptyStackException if no current {@code UserContext} exists
     */
    public static UserContext getCurrentUserContext() {
        // todo consider handling exception here
//        try {
        return HOLDER.get().peek();

//        } catch (EmptyStackException e) {
//            cleanUserContext();
//            return null;
//        }
    }

    /**
     * @return the {@code UserContext} of the actual logged-in user. It is assumed that the actual
     * {@code UserContext} is the one at the bottom of the stack.
     */
    public static UserContext getActualUserContext() {
        Stack<UserContext> uCtxStack = HOLDER.get();
        if (uCtxStack.empty()) {
            cleanUserContext();
            return null;
        }
        return uCtxStack.elementAt(0);
    }

    /**
     * <p>Cleans all current thread's {@code UserContext}s and removes this thread's {@code Stack}.</p>
     */
    public static void cleanUserContext() {
        HOLDER.get().clear();
        HOLDER.remove();
    }

    /**
     * <p>Pushes the given {@code UserContext} into this thread's {@code UserContext} stack.
     * Given {@code uCtxt} cannot be {@code null}.</p>
     *
     * @param uCtxt {@code UserContext} to push
     * @return the given {@code uCtxt}
     * @throws IllegalArgumentException if given {@code UserContext} is {@code null}
     */
    public static UserContext pushUserContext(UserContext uCtxt) {
        if (uCtxt == null) {
            throw new IllegalArgumentException("Cannot push <null> UserContext");
        }
        return HOLDER.get().push(uCtxt);
    }

    /**
     * <p>Pops topmost (i.e. current) {@code UserContext} from this thread's {@code UserContext} stack.
     * If the stack becomes empty after popping, the thread's entry is removed entirely.</p>
     *
     * @return the stack topmost (i.e. current) {@code UserContext}, or {@code null} if stack is empty
     */
    public static UserContext popUserContext() {
        Stack<UserContext> uCtxStack = HOLDER.get();
        if (uCtxStack.empty()) {
            cleanUserContext();
            return null;
        }
        UserContext uCtx = uCtxStack.pop();
        if (uCtxStack.empty()) {
            cleanUserContext();
        }

        return uCtx;
    }

    public static void popOnBehalfUserContexts() {
        Stack<UserContext> uCtxStack = HOLDER.get();
        while (uCtxStack.size() > 1) {
            UserContextHolder.popUserContext();
        }
    }

    @SuppressWarnings("unchecked")
    static Stack<UserContext> getContextStack() {
        return (Stack<UserContext>) HOLDER.get().clone();
    }
}