package rss.services;

import java.io.Serializable;

/**
 * User: Michael Dikman
 * Date: 22/12/12
 * Time: 14:30
 */
public abstract class MediaRequest implements Serializable {

	private static final long serialVersionUID = 5299194875537926970L;

	protected String pirateBayId;
	private String imdbId;
	private String hash;
	private String title;

	protected MediaRequest() {
	}

	protected MediaRequest(String title) {
		this.title = title;
	}

	protected MediaRequest(String title, String hash) {
		this.title = title;
		this.hash = hash;
	}

	public String getHash() {
		return hash;
	}

	public void setHash(String hash) {
		this.hash = hash;
	}

	public String toQueryString() {
		return title;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(title);
		if (hash != null) {
			sb.append(" (").append(hash).append(")");
		}
		return sb.toString();
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getPirateBayId() {
		return pirateBayId;
	}

	public void setPirateBayId(String pirateBayId) {
		this.pirateBayId = pirateBayId;
	}

	public String getImdbId() {
		return imdbId;
	}

	public void setImdbId(String imdbId) {
		this.imdbId = imdbId;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		MediaRequest that = (MediaRequest) o;

		if (title != null ? !title.equals(that.title) : that.title != null) return false;

		return true;
	}

	@Override
	public int hashCode() {
		return title != null ? title.hashCode() : 0;
	}
}
