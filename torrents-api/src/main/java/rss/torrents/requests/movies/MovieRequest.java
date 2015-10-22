package rss.torrents.requests.movies;

import rss.torrents.requests.MediaRequest;

/**
 * User: Michael Dikman
 * Date: 22/12/12
 * Time: 14:30
 */
public class MovieRequest extends MediaRequest {

	private int size;

	public MovieRequest(String title, String hash) {
		super(title, hash, Integer.MAX_VALUE);
	}

	public void setSize(int size) {
		this.size = size;
	}

	public int getSize() {
		return size;
	}
}
