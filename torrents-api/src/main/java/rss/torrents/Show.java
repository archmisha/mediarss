package rss.torrents;

import rss.user.User;

import java.util.Date;
import java.util.Set;

/**
 * User: Michael Dikman
 * Date: 24/12/12
 * Time: 23:26
 */

public interface Show extends Media {

    boolean isEnded();

    void setEnded(boolean ended);

    void setName(String name);

    String getTvComUrl();

    void setTvComUrl(String tvComUrl);

    Set<Episode> getEpisodes();

    String toString();

    void setTvRageId(int tvRageId);

    int getTvRageId();

    Date getScheduleDownloadDate();

    void setScheduleDownloadDate(Date scheduleDownloadDate);

    String getSubCenterUrl();

    void setSubCenterUrl(String subCenterUrl);

    Date getSubCenterUrlScanDate();

    void setSubCenterUrlScanDate(Date subCenterUrlScanDate);

    Set<User> getUsers();

    boolean equals(Object o);

    int hashCode();
}
