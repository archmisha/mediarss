package rss.torrents;

import java.util.Date;

/**
 * User: Michael Dikman
 * Date: 24/11/12
 * Time: 14:36
 */
public interface Torrent extends Downloadable, Comparable<Torrent> {

    long getId();

    String toString();

    void setTitle(String title);

    String getTitle();

    String getName();

    String getUrl();

    Date getDateUploaded();

    int getSeeders();

    String getSourcePageUrl();

    void setSourcePageUrl(String sourcePageUrl);

    int compareTo(Torrent o);

    MediaQuality getQuality();

    void setQuality(MediaQuality quality);

    void setHash(String hash);

    String getHash();

    String getImdbId();

    int getSize();

    void setSize(int size);

    void setImdbId(String imdbId);

    boolean equals(Object o);

    int hashCode();
}
