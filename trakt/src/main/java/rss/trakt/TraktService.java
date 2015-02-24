package rss.trakt;

/**
 * User: dikmanm
 * Date: 16/02/2015 00:06
 */
public interface TraktService {

    void authenticateUser(String code);

    boolean isConnected(long userId);

    String getClientId();

    void disconnectUser(long userId);
}
