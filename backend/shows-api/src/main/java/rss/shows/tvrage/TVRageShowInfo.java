package rss.shows.tvrage;

import java.util.List;

/**
 * User: dikmanm
 * Date: 17/08/2015 14:33
 */
public class TVRageShowInfo {
    private int showid;
    private String name;
    private String showname;
    private int totalseasons;
    private int seasons;
    private String showlink;
    private Object started;
    private Object startdate;
    private String ended;
    private String image;
    private String origin_country;
    private String status;
    private String classification;
    private List<String> genres;
    private List<String> akas;
    private int runtime;
    private String network;
    private Object airtime;
    private String airday;
    private String timezone;
    private TVRageEpisodeList Episodelist;

    // used by xtream with lower L
    public TVRageEpisodeList getEpisodelist() {
        return Episodelist;
    }

    public String getStatus() {
        return status;
    }

    public int getShowid() {
        return showid;
    }

    public void setShowid(int showid) {
        this.showid = showid;
    }

    public void setEpisodelist(TVRageEpisodeList episodelist) {
        Episodelist = episodelist;
    }
}
