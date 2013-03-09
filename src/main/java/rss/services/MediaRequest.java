package rss.services;

import java.io.Serializable;

/**
 * User: Michael Dikman
 * Date: 22/12/12
 * Time: 14:30
 */
public abstract class MediaRequest implements Serializable {

    private static final long serialVersionUID = 5299194875537926970L;

    private String title;

    protected MediaRequest() {
    }

    protected MediaRequest(String title) {
        this.title = title;
    }

    public String toQueryString() {
        return title;
    }

    @Override
    public String toString() {
        return title;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
