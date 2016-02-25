package rss.torrents;

import rss.user.User;

import java.util.Date;

/**
 * User: dikmanm
 * Date: 18/10/2015 20:16
 */
public interface UserTorrent {

    User getUser();

    void setUser(User user);

    Date getAdded();

    void setAdded(Date added);

    Torrent getTorrent();

    void setTorrent(Torrent torrent);

    Date getDownloadDate();

    void setDownloadDate(Date downloadDate);
}
