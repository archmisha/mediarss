package rss.torrents.searchers;

/**
 * User: dikmanm
 * Date: 01/05/13 23:07
 */
public class TorrentzResult {

	private String title;
	private String hash;
	private int uploaders;
	private int size;

	public TorrentzResult(String name, String hash, int uploaders, int size) {
		this.title = name;
		this.hash = hash;
		this.uploaders = uploaders;
		this.size = size;
	}

	public String getTitle() {
		return title;
	}

	public int getUploaders() {
		return uploaders;
	}

	public String getHash() {
		return hash;
	}

	public int getSize() {
		return size;
	}
}
