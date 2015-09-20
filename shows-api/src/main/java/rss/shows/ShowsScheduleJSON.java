package rss.shows;

import java.util.ArrayList;
import java.util.List;

/**
 * User: dikmanm
 * Date: 22/03/13 15:59
 */
public class ShowsScheduleJSON {

    private List<ShowsDaySchedule> schedules;

    public ShowsScheduleJSON() {
        this.schedules = new ArrayList<>();
    }

    public void addSchedule(long date, List<ShowScheduleEpisodeItem> showNames) {
        schedules.add(new ShowsDaySchedule(date, showNames));
    }

    public List<ShowsDaySchedule> getSchedules() {
        return schedules;
    }

}
