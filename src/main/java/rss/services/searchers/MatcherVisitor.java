package rss.services.searchers;

import rss.services.requests.EpisodeRequest;
import rss.services.requests.MovieRequest;
import rss.services.shows.ShowService;

import java.util.ArrayList;
import java.util.List;

/**
 * User: dikmanm
 * Date: 28/04/13 23:37
 */
public class MatcherVisitor {

	private ShowService showService;

	public MatcherVisitor(ShowService showService) {
		this.showService = showService;
	}

	public List<ShowService.MatchCandidate> filterMatching(EpisodeRequest episodeRequest, List<ShowService.MatchCandidate> matchCandidates) {
		return showService.filterMatching(episodeRequest, matchCandidates);
	}

	public List<ShowService.MatchCandidate> filterMatching(MovieRequest movieRequest, List<ShowService.MatchCandidate> matchCandidates) {
		List<ShowService.MatchCandidate> results = new ArrayList<>();
		for (ShowService.MatchCandidate searchResult : matchCandidates) {
			if (searchResult.getText().toLowerCase().contains(movieRequest.getTitle().toLowerCase())) {
				results.add(searchResult);
			}
		}
		return results;
	}
}
