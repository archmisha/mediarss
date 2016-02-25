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

    void setSeason(int season);

    int getEpisode();

    void setEpisode(int episode);

    Date getLastUpdated();

    void setLastUpdated(Date lastUpdated);

    String toString();

    String getSeasonEpisode();

    boolean equals(Object o);

    int hashCode();

    Date getAirDate();

    void setAirDate(Date airDate);

    Date getScanDate();

    void setScanDate(Date scanDate);

    Show getShow();

    void setShow(Show show);

    boolean isUnAired();

    Set<Long> getTorrentIds();

    Long getTheTvDbId();

    void setTheTvDbId(long theTvDbEpisodeId);
}
