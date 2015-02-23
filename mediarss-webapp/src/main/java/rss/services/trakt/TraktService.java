package rss.services.trakt;

import rss.entities.User;

/**
 * User: dikmanm
 * Date: 16/02/2015 00:06
 */
public interface TraktService {

    void authenticateUser(String code);

    boolean isConnected(User user);

    String getClientId();

    void disconnectUser(User user);
}
