package rss.entities;

import rss.ems.entities.BaseEntity;

import javax.persistence.Column;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

/**
 * User: dikmanm
 * Date: 13/02/2015 12:24
 */
@javax.persistence.Entity
@javax.persistence.Table(name = "news")
@NamedQueries({
        @NamedQuery(name = "News.findByCreated",
                query = "select n from News as n where created >= :created")
})
public class News extends BaseEntity {

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
