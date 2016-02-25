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

    void setId(long id);

    boolean isEnded();

    void setEnded(boolean ended);

    void setName(String name);

    @Deprecated
    String getTvComUrl();

    @Deprecated
    void setTvComUrl(String tvComUrl);

    Set<Episode> getEpisodes();

    String toString();

    @Deprecated
    int getTvRageId();

    @Deprecated
    void setTvRageId(int tvRageId);

    Date getScheduleDownloadDate();

    void setScheduleDownloadDate(Date scheduleDownloadDate);

    @Deprecated
    String getSubCenterUrl();

    @Deprecated
    void setSubCenterUrl(String subCenterUrl);

    @Deprecated
    Date getSubCenterUrlScanDate();

    @Deprecated
    void setSubCenterUrlScanDate(Date subCenterUrlScanDate);

    void setTheTvDbScanDate(Date theTvDbScanDate);

    Long getTheTvDbId();

    void setTheTvDbId(long theTvDbId);

    Set<User> getUsers();

    boolean equals(Object o);

    int hashCode();
}
