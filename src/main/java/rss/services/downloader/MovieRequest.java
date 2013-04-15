package rss.services.downloader;

import rss.services.requests.MediaRequest;

/**
 * User: Michael Dikman
 * Date: 22/12/12
 * Time: 14:30
 */
public class MovieRequest extends MediaRequest {

	private static final long serialVersionUID = 1484459093147625288L;
	private int uploaders;

	public MovieRequest(String title, String hash) {
        super(title, hash);
    }

	public int getUploaders() {
		return uploaders;
	}

	public void setUploaders(int uploaders) {
		this.uploaders = uploaders;
	}
}
