package rss.entities;

import rss.services.searchers.SearchResult;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.Table;

/**
 * User: dikmanm
 * Date: 10/02/2015 14:59
 */
@Entity
@Table(name = "search_log")
public class SearchLog extends BaseEntity {

    private static final long serialVersionUID = -4982902733852515357L;

    @Column(name = "status")
    private SearchResult.SearchStatus status;

    @Column(name = "source")
    private String source;

    @Column(name = "url", length = 4000)
    private String url;

    @Column(name = "page")
    @Lob
    private byte[] page;

    @Column(name = "exception")
    @Lob
    private byte[] exception;

    @Column(name = "name")
    private String name;

    @Column(name = "season")
    private int season;

    @Column(name = "episode")
    private String episode;

    @Column(name = "type")
    private String type;

    public SearchResult.SearchStatus getStatus() {
        return status;
    }

    public void setStatus(SearchResult.SearchStatus status) {
        this.status = status;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public byte[] getPage() {
        return page;
    }

    public void setPage(byte[] page) {
        this.page = page;
    }

    public byte[] getException() {
        return exception;
    }

    public void setException(byte[] exception) {
        this.exception = exception;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getSeason() {
        return season;
    }

    public void setSeason(int season) {
        this.season = season;
    }

    public String getEpisode() {
        return episode;
    }

    public void setEpisode(String episode) {
        this.episode = episode;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
