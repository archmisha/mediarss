package rss.services.shows;

/**
 * User: dikmanm
 * Date: 15/04/13 18:08
 */
public class CachedShowSubset {
	private String subset;
	private int words;

	public CachedShowSubset(String subset, int words) {
		this.subset = subset;
		this.words = words;
	}

	public String getSubset() {
		return subset;
	}

	public int getWords() {
		return words;
	}
}
