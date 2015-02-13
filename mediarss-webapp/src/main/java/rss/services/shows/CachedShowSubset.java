package rss.services.shows;

/**
 * User: dikmanm
 * Date: 15/04/13 18:08
 */
public class CachedShowSubset {
	private String subset;
	private byte words;

	public CachedShowSubset(String subset, byte words) {
		this.subset = subset.intern();
		this.words = words;
	}

	public String getSubset() {
		return subset;
	}

	public byte getWords() {
		return words;
	}
}
