package rss.subtitles;

import rss.torrents.Torrent;
import rss.user.subtitles.SubtitleLanguage;

import java.util.Date;

/**
 * User: dikmanm
 * Date: 18/10/2015 21:04
 */
public interface SubtitlesScanHistory {

    void setTorrent(Torrent torrent);

    Date getScanDate();

    void setScanDate(Date scanDate);

    SubtitleLanguage getLanguage();

    void setLanguage(SubtitleLanguage language);
}
