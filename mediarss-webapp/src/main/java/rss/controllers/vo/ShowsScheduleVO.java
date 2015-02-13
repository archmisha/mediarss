package rss.controllers.vo;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * User: dikmanm
 * Date: 22/03/13 15:59
 */
public class ShowsScheduleVO {

	private List<ShowsDaySchedule> schedules;

	public ShowsScheduleVO() {
		this.schedules = new ArrayList<>();
	}

	public void addSchedule(Date date, List<ShowScheduleEpisodeItem> showNames) {
		schedules.add(new ShowsDaySchedule(date, showNames));
	}

	public List<ShowsDaySchedule> getSchedules() {
		return schedules;
	}

	private class ShowsDaySchedule {
		private Date date;
		private List<ShowScheduleEpisodeItem> shows;

		public ShowsDaySchedule(Date date, List<ShowScheduleEpisodeItem> showNames) {
			this.date = date;
			this.shows = showNames;
		}

		public Date getDate() {
			return date;
		}

		public List<ShowScheduleEpisodeItem> getShows() {
			return shows;
		}
	}
}
