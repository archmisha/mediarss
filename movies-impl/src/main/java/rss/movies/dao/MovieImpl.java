package rss.movies.dao;

import rss.torrents.Movie;
import rss.torrents.dao.MediaImpl;

import javax.persistence.Column;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import java.util.Date;

/**
 * User: dikmanm
 * Date: 17/10/2015 10:34
 */
@javax.persistence.Entity(name = "Movie")
@javax.persistence.Table(name = "movie")
@NamedQueries({
        @NamedQuery(name = "Movie.findByDateUploaded",
                query = "select distinct m from Movie as m join m.torrentIds as tid " +
                        "where tid in (select t.id from Torrent as t where t.dateUploaded > :dateUploaded)"),
        @NamedQuery(name = "Movie.findOrderedByDateUploaded",
                query = "select m, max(t.dateUploaded) as uploaded from Movie as m join m.torrentIds as tid, Torrent as t " +
                        "where tid = t.id " +
                        "group by m.id " +
                        "order by uploaded desc"),
        @NamedQuery(name = "Movie.findByDateUploadedCount",
                query = "select distinct m.id from Movie as m join m.torrentIds as tid " +
                        "where tid in (select t.id from Torrent as t where t.dateUploaded > :dateUploaded)"),
        @NamedQuery(name = "Movie.findByTorrent",
                query = "select m from Movie as m join m.torrentIds as tid " +
                        "where :torrentId = tid"),
        @NamedQuery(name = "Movie.findByImdbUrl",
                query = "select m from Movie as m " +
                        "where m.imdbUrl = :imdbUrl")
})
public class MovieImpl extends MediaImpl implements Movie {

    private static final long serialVersionUID = 8378048151514553873L;

    @Column(name = "name")
    private String name;

    // this is only needed for movies, for tv shows better use tv.com
    @Column(name = "imdb_url", unique = true)
    private String imdbUrl;

    @Column(name = "year")
    private int year;

    @Column(name = "release_date")
    private Date releaseDate;

    //, unique = true
    @Column(name = "subcenter_url")
    private String subCenterUrl;

    @Column(name = "subcenter_url_scan_date")
    private Date subCenterUrlScanDate;

    // for hibernate
    public MovieImpl() {
    }

    public MovieImpl(String name, String imdbUrl, int year, Date releaseDate) {
        this();
        this.name = name;
        this.imdbUrl = imdbUrl;
        this.year = year;
        this.releaseDate = releaseDate;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return getName();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Movie movie = (Movie) o;

        //noinspection RedundantIfStatement
        if (!getName().equals(movie.getName())) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return getName().hashCode();
    }

    public void setImdbUrl(String imdbUrl) {
        this.imdbUrl = imdbUrl;
    }

    public String getImdbUrl() {
        return imdbUrl;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
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

    public Date getReleaseDate() {
        return releaseDate;
    }

    @Override
    public void setReleaseDate(Date releaseDate) {
        this.releaseDate = releaseDate;
    }
}
