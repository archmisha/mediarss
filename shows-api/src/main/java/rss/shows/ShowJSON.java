package rss.shows;

/**
 * User: dikmanm
 * Date: 07/02/13 18:35
 */
public class ShowJSON {

	private long id;
	private String name;
	private boolean ended;
	private int tvRageId;

	public long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public ShowJSON withName(String name) {
		this.name = name;
		return this;
	}

	public ShowJSON withId(long id) {
		this.id = id;
		return this;
	}

	public ShowJSON withEnded(boolean ended) {
		this.ended = ended;
		return this;
	}

	public boolean isEnded() {
		return ended;
	}

	public int getTvRageId() {
		return tvRageId;
	}

	public ShowJSON withTvRageId(int tvRageId) {
		this.tvRageId = tvRageId;
		return this;
	}
}
