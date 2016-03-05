package rss.automation.pages;

/**
 * Created by dikmanm on 04/02/2016.
 */
public class Torrent {

    private String showName;
    private String season;
    private String episode;

    public Torrent(String showName, String season, String episode) {
        this.showName = showName;
        this.season = season;
        this.episode = episode;
    }

    public String getShowName() {
        return showName;
    }

    public String getSeason() {
        return season;
    }

    public String getEpisode() {
        return episode;
    }
}
