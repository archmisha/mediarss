package rss.controllers.vo;

/**
 * User: dikmanm
 * Date: 07/02/13 18:35
 */
public class ShowVO {

	private long id;
	private String name;

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
}
