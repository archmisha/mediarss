package rss.services.searchers;

import rss.entities.MediaQuality;
import rss.services.requests.EpisodeRequest;
import rss.services.requests.MovieRequest;
import rss.services.shows.ShowService;
import rss.util.CollectionUtils;

import java.util.*;

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
		return filterByQuality(showService.filterMatching(episodeRequest, matchCandidates), MediaQuality.HD720P, MediaQuality.HD1080P, MediaQuality.NORMAL);
	}

	public List<ShowService.MatchCandidate> filterMatching(MovieRequest movieRequest, List<ShowService.MatchCandidate> matchCandidates) {
		List<ShowService.MatchCandidate> results = new ArrayList<>();
		for (ShowService.MatchCandidate searchResult : matchCandidates) {
			if (searchResult.getText().toLowerCase().contains(movieRequest.getTitle().toLowerCase())) {
				results.add(searchResult);
			}
		}
		return filterByQuality(results, MediaQuality.HD720P, MediaQuality.HD1080P);
	}

	private List<ShowService.MatchCandidate> filterByQuality(List<ShowService.MatchCandidate> matchCandidates, MediaQuality... qualities) {
		// map candidates by quality
		Map<MediaQuality, List<ShowService.MatchCandidate>> map = new HashMap<>();
		for (ShowService.MatchCandidate candidate : matchCandidates) {
			for (MediaQuality quality : qualities) {
				if (candidate.getText().contains(quality.toString())) {
					CollectionUtils.safeListPut(map, quality, candidate);
					break;
				}
			}
		}

		// go over the qualities in order received and return what ever is found
		for (MediaQuality quality : qualities) {
			if (map.containsKey(quality) && !map.get(quality).isEmpty()) {
				return map.get(quality);
			}
		}
		return Collections.emptyList();
	}
}
