package rss.shows.tvrage;

/**
 * User: dikmanm
 * Date: 17/08/2015 14:32
 */
public class TVRageEpisode {
    private int epnum;
    private String seasonnum;
    private String prodnum;
    private String airdate;
    private String link;
    private String title;
    private String screencap;
    // for movies part
    private int season;
    private int runtime;

    public int getEpnum() {
        return epnum;
    }

    public String getSeasonnum() {
        return seasonnum;
    }

    public String getProdnum() {
        return prodnum;
    }

    public String getAirdate() {
        return airdate;
    }

    public String getLink() {
        return link;
    }

    public String getTitle() {
        return title;
    }

    public String getScreencap() {
        return screencap;
    }

    public void setSeasonnum(String seasonnum) {
        this.seasonnum = seasonnum;
    }

    public void setAirdate(String airdate) {
        this.airdate = airdate;
    }
}
