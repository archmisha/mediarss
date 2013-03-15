package rss.services.shows;

/**
* User: dikmanm
* Date: 15/03/13 10:42
*/
public class AutoCompleteItem {
	private long id;
	private String text;

	public AutoCompleteItem(long id, String text) {
		this.id = id;
		this.text = text;
	}

	public long getId() {
		return id;
	}

	public String getText() {
		return text;
	}
}
