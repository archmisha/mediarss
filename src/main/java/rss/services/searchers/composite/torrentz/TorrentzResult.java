package rss.services.searchers.composite.torrentz;

/**
 * User: dikmanm
 * Date: 01/05/13 23:07
 */
public class TorrentzResult {

	private String title;
	private String hash;
	private int uploaders;

	public TorrentzResult(String name, String hash, int uploaders) {
		this.title = name;
		this.hash = hash;
		this.uploaders = uploaders;
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
}
