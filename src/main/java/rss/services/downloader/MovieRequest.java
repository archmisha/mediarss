package rss.services.downloader;

import rss.services.MediaRequest;

/**
 * User: Michael Dikman
 * Date: 22/12/12
 * Time: 14:30
 */
public class MovieRequest extends MediaRequest {

	private static final long serialVersionUID = 1484459093147625288L;

	private String hash;

	public MovieRequest(String title, String hash) {
        super(title);
        this.hash = hash;
    }

    public String getHash() {
        return hash;
    }

	@Override
    public String toString() {
        StringBuilder sb = new StringBuilder(super.toString());
        if (hash != null) {
            sb.append(" (").append(hash).append(")");
        }
        return sb.toString();
    }
}
