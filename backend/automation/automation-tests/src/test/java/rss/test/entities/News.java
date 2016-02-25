package rss.test.entities;

import java.util.Date;

/**
 * User: dikmanm
 * Date: 13/02/2015 12:53
 */
public class News {
    private Date created;
    private long id;
    private String message;

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
