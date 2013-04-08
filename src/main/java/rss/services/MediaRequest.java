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

	private String title;

    protected MediaRequest() {
    }

    protected MediaRequest(String title) {
        this.title = title;
    }

    public String toQueryString() {
        return title;
    }

    @Override
    public String toString() {
        return title;
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
