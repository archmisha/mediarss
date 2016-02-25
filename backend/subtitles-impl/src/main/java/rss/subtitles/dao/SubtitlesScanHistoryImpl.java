package rss.subtitles.dao;

import org.hibernate.annotations.Index;
import rss.ems.entities.BaseEntity;
import rss.subtitles.SubtitlesScanHistory;
import rss.torrents.Torrent;
import rss.torrents.dao.TorrentImpl;
import rss.user.subtitles.SubtitleLanguage;

import javax.persistence.*;
import java.util.Date;

/**
 * User: dikmanm
 * Date: 15/05/13 11:56
 */
@javax.persistence.Entity(name = "SubtitlesScanHistory")
@javax.persistence.Table(name = "subtitles_scan_history", uniqueConstraints = @UniqueConstraint(columnNames = {"torrent_id", "language"}))
@org.hibernate.annotations.Table(appliesTo = "subtitles_scan_history", indexes = {
        @Index(name = "subs_scan_hist_torrentid_lang_idx", columnNames = {"torrent_id", "language"})
})
@NamedQueries({
        @NamedQuery(name = "SubtitlesScanHistory.find",
                query = "select s from SubtitlesScanHistory as s " +
                        "where s.torrent.id = :torrentId and s.language = :language")
})
public class SubtitlesScanHistoryImpl extends BaseEntity implements SubtitlesScanHistory {

    private static final long serialVersionUID = 6036706075140254769L;

    @ManyToOne(targetEntity = TorrentImpl.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "torrent_id")
    private Torrent torrent;

    @Column(name = "scan_date")
    private Date scanDate;

    @Column(name = "language")
    private SubtitleLanguage language;

    public Torrent getTorrent() {
        return torrent;
    }

    public void setTorrent(Torrent torrent) {
        this.torrent = torrent;
    }

    public Date getScanDate() {
        return scanDate;
    }

    public void setScanDate(Date scanDate) {
        this.scanDate = scanDate;
    }

    public SubtitleLanguage getLanguage() {
        return language;
    }

    public void setLanguage(SubtitleLanguage language) {
        this.language = language;
    }
}
