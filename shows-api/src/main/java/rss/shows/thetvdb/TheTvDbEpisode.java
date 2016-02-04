package rss.shows.thetvdb;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Created by dikmanm on 29/10/2015.
 */
public class TheTvDbEpisode {

    @XStreamAlias("id")
    private long id;

    @XStreamAlias("SeasonNumber")
    private int season;

    @XStreamAlias("EpisodeNumber")
    private int episode;

    @XStreamAlias("FirstAired")
    private String airDate;

    @XStreamAlias("seriesid")
    private long showId;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getSeason() {
        return season;
    }

    public void setSeason(int season) {
        this.season = season;
    }

    public int getEpisode() {
        return episode;
    }

    public void setEpisode(int episode) {
        this.episode = episode;
    }

    public String getAirDate() {
        return airDate;
    }

    public void setAirDate(String airDate) {
        this.airDate = airDate;
    }

    public long getShowId() {
        return showId;
    }

    public void setShowId(long showId) {
        this.showId = showId;
    }
}
