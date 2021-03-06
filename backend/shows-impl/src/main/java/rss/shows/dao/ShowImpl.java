package rss.shows.dao;

import org.hibernate.annotations.Index;
import rss.ems.entities.BaseEntity;
import rss.torrents.Episode;
import rss.torrents.Show;
import rss.user.User;
import rss.user.dao.UserImpl;

import javax.persistence.*;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * User: dikmanm
 * Date: 17/10/2015 09:22
 */
@javax.persistence.Entity(name = "Show")
@javax.persistence.Table(name = "show")
@NamedQueries({
        // searching with lower case, so when user input "suits" it will also find "Suits"
        @NamedQuery(name = "Show.findByName",
                query = "select b from Show as b where lower(b.name) = :name"),
        @NamedQuery(name = "Show.findByTvRageId",
                query = "select b from Show as b where b.tvRageId = :tvRageId"),
        @NamedQuery(name = "Show.findByTheTvDbId",
                query = "select b from Show as b where b.theTvDbId = :theTvDbId"),
//		@NamedQuery(name = "Show.autoCompleteShowNames",
//				query = "select b from Show as b where lower(b.name) like :term"),
//		@NamedQuery(name = "Show.getNotEnded",
//				query = "select b from Show as b where b.ended = false"),
        @NamedQuery(name = "Show.getUsersCountTrackingShow",
                query = "select count(u) from Show as s inner join s.users as u where s.id = :showId"),
        @NamedQuery(name = "Show.getUserShows",
                query = "select s from Show as s inner join s.users as u where u.id = :userId"),
        @NamedQuery(name = "Show.findCachedShows",
                query = "select new rss.shows.CachedShow(s.id, s.name, s.ended) from Show as s"),
        @NamedQuery(name = "Show.getShowsWithoutTheTvDbId",
                query = "select s from Show as s where s.theTvDbId = null and theTvDbScanDate = null")
})
public class ShowImpl extends BaseEntity implements Show {

    private static final long serialVersionUID = -4408596786454177485L;

    @Column(name = "name", unique = true)
    @Index(name = "show_name_idx")
    private String name;

    @Column(name = "tvcom_url")
    private String tvComUrl;

    @Column(name = "ended")
    private boolean ended;

    @Column(name = "schedule_download_date")
    private Date scheduleDownloadDate;

    @OneToMany(mappedBy = "show", targetEntity = EpisodeImpl.class)
    private Set<Episode> episodes;

    @Column(name = "tvrage_id")
    private int tvRageId;

    //, unique = true
    @Column(name = "subcenter_url")
    private String subCenterUrl;

    @Column(name = "subcenter_url_scan_date")
    private Date subCenterUrlScanDate;

    @Column(name = "thetvdb_id")
    private Long theTvDbId;

    @Column(name = "thetvdb_scan_date")
    private Date theTvDbScanDate;

    @ManyToMany(cascade = CascadeType.ALL, targetEntity = UserImpl.class)
    @JoinTable(name = "user_shows",
            joinColumns = {@JoinColumn(name = "show_id")},
            inverseJoinColumns = {@JoinColumn(name = "user_id")}
    )
    private Set<User> users;

    public ShowImpl() {
        episodes = new HashSet<>();
        users = new HashSet<>();
    }

    public ShowImpl(String name) {
        this();
        this.name = name;
    }

    public boolean isEnded() {
        return ended;
    }

    public void setEnded(boolean ended) {
        this.ended = ended;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTvComUrl() {
        return tvComUrl;
    }

    public void setTvComUrl(String tvComUrl) {
        this.tvComUrl = tvComUrl;
    }

    public Set<Episode> getEpisodes() {
        return episodes;
    }

    @Override
    public String toString() {
        return name + " (id=" + id + ", thetvdb_id=" + theTvDbId + ")";
    }

    public int getTvRageId() {
        return tvRageId;
    }

    public void setTvRageId(int tvRageId) {
        this.tvRageId = tvRageId;
    }

    public Date getScheduleDownloadDate() {
        return scheduleDownloadDate;
    }

    public void setScheduleDownloadDate(Date scheduleDownloadDate) {
        this.scheduleDownloadDate = scheduleDownloadDate;
    }

    public String getSubCenterUrl() {
        return subCenterUrl;
    }

    public void setSubCenterUrl(String subCenterUrl) {
        this.subCenterUrl = subCenterUrl;
    }

    public Date getSubCenterUrlScanDate() {
        return subCenterUrlScanDate;
    }

    public void setSubCenterUrlScanDate(Date subCenterUrlScanDate) {
        this.subCenterUrlScanDate = subCenterUrlScanDate;
    }

    public void setTheTvDbScanDate(Date theTvDbScanDate) {
        this.theTvDbScanDate = theTvDbScanDate;
    }

    public Long getTheTvDbId() {
        return theTvDbId;
    }

    public void setTheTvDbId(long theTvDbId) {
        this.theTvDbId = theTvDbId;
    }

    @Override
    public Set<User> getUsers() {
        return users;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Show show = (Show) o;

        if (!name.equals(show.getName())) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
}
