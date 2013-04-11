package rss.services.shows;

/**
 * User: dikmanm
 * Date: 15/03/13 10:35
 */
public class CachedShow {

	private long id;
	private String name;
	private boolean ended;

	public CachedShow(long id, String name, boolean ended) {
		this.id = id;
		this.name = name;
		this.ended = ended;
	}

	public long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public boolean isEnded() {
		return ended;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		CachedShow that = (CachedShow) o;

		if (id != that.id) return false;

		return true;
	}

	@Override
	public int hashCode() {
		return (int) (id ^ (id >>> 32));
	}
}
