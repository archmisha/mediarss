package rss.shows;

/**
 * User: dikmanm
 * Date: 23/03/13 11:38
 */
public class ShowScheduleEpisodeItem {
	private String sequence;
	private String showName;

	public ShowScheduleEpisodeItem(String sequence, String showName) {
		this.sequence = sequence;
		this.showName = showName;
	}

	public String getSequence() {
		return sequence;
	}

	public String getShowName() {
		return showName;
	}
}
