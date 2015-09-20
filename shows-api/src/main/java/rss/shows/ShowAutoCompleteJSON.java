package rss.shows;

import java.util.List;

/**
 * User: dikmanm
 * Date: 22/08/2015 17:45
 */
public class ShowAutoCompleteJSON {

    private int total;
    private List<ShowAutoCompleteItem> shows;

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public List<ShowAutoCompleteItem> getShows() {
        return shows;
    }

    public void setShows(List<ShowAutoCompleteItem> shows) {
        this.shows = shows;
    }
}
