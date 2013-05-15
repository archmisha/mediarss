package rss.services.requests.movies;

import rss.services.requests.MediaRequest;
import rss.services.searchers.MediaRequestVisitor;

/**
 * User: Michael Dikman
 * Date: 22/12/12
 * Time: 14:30
 */
public class MovieRequest extends MediaRequest {

	public MovieRequest(String title, String hash) {
		super(title, hash, Integer.MAX_VALUE);
	}

	@Override
	public <S, T> T visit(MediaRequestVisitor<S, T> visitor, S config) {
		return visitor.visit(this, config);
	}
}
