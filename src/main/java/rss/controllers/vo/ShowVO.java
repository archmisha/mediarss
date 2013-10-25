package rss.controllers.vo;

/**
 * User: dikmanm
 * Date: 07/02/13 18:35
 */
public class ShowVO {

	private long id;
	private String name;
	private boolean ended;

	public long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public ShowVO withName(String name) {
		this.name = name;
		return this;
	}

	public ShowVO withId(long id) {
		this.id = id;
		return this;
	}

	public ShowVO withEnded(boolean ended) {
		this.ended = ended;
		return this;
	}

	public boolean isEnded() {
		return ended;
	}
}
