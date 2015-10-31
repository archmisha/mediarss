package rss.shows.schedule;

import java.util.List;

/**
 * User: dikmanm
 * Date: 18/08/2015 23:46
 */
public class ShowsDaySchedule {
    private long date;
    private List<ShowScheduleEpisodeItem> shows;

    public ShowsDaySchedule(long date, List<ShowScheduleEpisodeItem> showNames) {
        this.date = date;
        this.shows = showNames;
    }

    public long getDate() {
        return date;
    }

    public List<ShowScheduleEpisodeItem> getShows() {
        return shows;
    }
}
