package rss.news.dao;

import rss.ems.entities.BaseEntity;
import rss.news.News;

import javax.persistence.Column;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

/**
 * Created by dikmanm on 26/10/2015.
 */
@javax.persistence.Entity(name = "News")
@javax.persistence.Table(name = "news")
@NamedQueries({
        @NamedQuery(name = "News.findByCreated",
                query = "select n from News as n where created >= :created")
})
public class NewsImpl extends BaseEntity implements News {

    private static final long serialVersionUID = -5441424391001104073L;

    @Column(name = "name")
    private String message;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
