package rss.services.requests;

import rss.services.searchers.MatcherVisitor;
import rss.services.shows.ShowService;

import java.util.List;

/**
 * User: Michael Dikman
 * Date: 22/12/12
 * Time: 14:30
 */
public class MovieRequest extends MediaRequest {

	private static final long serialVersionUID = 1484459093147625288L;

	public MovieRequest(String title, String hash) {
		super(title, hash, Integer.MAX_VALUE);
	}

	@Override
	public List<ShowService.MatchCandidate> visit(MatcherVisitor visitor, List<ShowService.MatchCandidate> matchCandidates) {
		return visitor.filterMatching(this, matchCandidates);
	}
}
