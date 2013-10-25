package rss.services.shows;

/**
 * User: dikmanm
 * Date: 15/03/13 10:42
 */
public class ShowAutoCompleteItem {
	private long id;
	private String text;
	private boolean ended;

	public ShowAutoCompleteItem(long id, String text) {
		this.id = id;
		this.text = text;
	}

	public long getId() {
		return id;
	}

	public String getText() {
		return text;
	}

	public boolean isEnded() {
		return ended;
	}

	public void setEnded(boolean ended) {
		this.ended = ended;
	}
}
