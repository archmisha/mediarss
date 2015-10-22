package rss.torrents;

import java.util.Date;
import java.util.Set;

/**
 * User: Michael Dikman
 * Date: 03/12/12
 * Time: 08:25
 */
public interface Movie extends Media {

    void setName(String name);

    String toString();

    boolean equals(Object o);

    int hashCode();

    void setImdbUrl(String imdbUrl);

    String getImdbUrl();

    int getYear();

    void setYear(int year);

    String getSubCenterUrl();

    void setSubCenterUrl(String subCenterUrl);

    Date getSubCenterUrlScanDate();

    void setSubCenterUrlScanDate(Date subCenterUrlScanDate);

    Date getReleaseDate();

    void setReleaseDate(Date releaseDate);

    Set<Long> getTorrentIds();
}
