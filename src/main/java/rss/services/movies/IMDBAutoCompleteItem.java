package rss.services.movies;

/**
 * User: dikmanm
 * Date: 25/05/13 17:26
 */
public class IMDBAutoCompleteItem {
	private int year;
	private String name;
	private String image;
	private String id;
	private boolean added;

	public IMDBAutoCompleteItem(String name, String id, int year, String image) {
		this.name = name;
		this.id = id;
		this.year = year;
		this.image = image;
	}

	public int getYear() {
		return year;
	}

	public String getName() {
		return name;
	}

	public String getImage() {
		return image;
	}

	public String getId() {
		return id;
	}

	public boolean isAdded() {
		return added;
	}

	public void setAdded(boolean added) {
		this.added = added;
	}
}
