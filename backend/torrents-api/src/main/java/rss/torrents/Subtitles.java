package rss.torrents;

import rss.user.subtitles.SubtitleLanguage;

import java.util.Date;
import java.util.Set;

/**
 * User: dikmanm
 * Date: 25/01/13 00:08
 */
public interface Subtitles extends Downloadable {

    long getId();

    SubtitleLanguage getLanguage();

    void setLanguage(SubtitleLanguage language);

    Set<Long> getTorrentIds();

    Date getDateUploaded();

    byte[] getData();

    void setData(byte[] data);

    String getExternalId();

    void setExternalId(String externalId);

    String getName();

    void setFileName(String fileName);

    void setDateUploaded(Date dateUploaded);

    String toString();
}
