package rss.util;

import java.util.Date;

/**
 * User: dikmanm
 * Date: 31/12/12 20:18
 */
public class DurationMeter {

	private final long from;
	private Date endTime;

	public DurationMeter() {
		from = System.currentTimeMillis();
	}

	public long getDuration() {
		return System.currentTimeMillis() - from;
	}

	public Date getStartTime() {
		return new Date(from);
	}

	public Date getEndTime() {
		return endTime;
	}

	public void stop() {
		endTime = new Date();
	}
}
