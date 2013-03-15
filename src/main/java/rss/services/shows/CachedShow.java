package rss.services.shows;

/**
 * User: dikmanm
 * Date: 15/03/13 10:35
 */
public class CachedShow {

	private long id;
	private String name;

	public CachedShow(long id, String name) {
		this.id = id;
		this.name = name;
	}

	public long getId() {
		return id;
	}

	public String getName() {
		return name;
	}
}
