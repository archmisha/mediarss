package rss.shows.thetvdb;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Created by dikmanm on 29/10/2015.
 */
public class TheTvDbShow {

    @XStreamAlias("id")
    private long id;

    @XStreamAlias("SeriesName")
    private String name;

    @XStreamAlias("Status")
    private String status;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TheTvDbShow that = (TheTvDbShow) o;

        return id == that.id;

    }

    @Override
    public int hashCode() {
        return (int) (id ^ (id >>> 32));
    }
}
