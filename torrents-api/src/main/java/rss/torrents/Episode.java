package rss.torrents;

import java.util.Date;
import java.util.Set;

/**
 * User: Michael Dikman
 * Date: 24/11/12
 * Time: 14:26
 */
public interface Episode extends Media {

    int getSeason();

    int getEpisode();

    void setSeason(int season);

    void setEpisode(int episode);

    Date getLastUpdated();

    void setLastUpdated(Date lastUpdated);

    String toString();

    String getSeasonEpisode();

    boolean equals(Object o);

    int hashCode();

    void setShow(Show show);

    Date getAirDate();

    void setAirDate(Date airDate);

    Date getScanDate();

    void setScanDate(Date scanDate);

    Show getShow();

    boolean isUnAired();

    Set<Long> getTorrentIds();
}
