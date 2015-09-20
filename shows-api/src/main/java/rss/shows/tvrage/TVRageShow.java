package rss.shows.tvrage;

/**
 * User: dikmanm
 * Date: 17/08/2015 14:32
 */
public class TVRageShow {
    private int id;
    private String name;
    private String country;
    private int status;

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCountry() {
        return country;
    }

    public int getStatus() {
        return status;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
