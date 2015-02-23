package rss.context;

import java.io.Serializable;

/**
 * This interface provides access to user specific context information.
 */
public interface UserContext extends Serializable {

    String getEmail();

    long getUserId();

    boolean isAdmin();
}
