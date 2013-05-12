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
public class MatcherVisitor implements MediaRequestVisitor<List<ShowService.MatchCandidate>, List<ShowService.MatchCandidate>> {

	private ShowService showService;

	public MatcherVisitor(ShowService showService) {
		this.showService = showService;
	}

	@Override
	public List<ShowService.MatchCandidate> visit(EpisodeRequest episodeRequest, List<ShowService.MatchCandidate> matchCandidates) {
		return filterByQuality(showService.filterMatching(episodeRequest, matchCandidates), MediaQuality.HD720P, MediaQuality.HD1080P, MediaQuality.NORMAL);
	}

	@Override
	public List<ShowService.MatchCandidate> visit(MovieRequest movieRequest, List<ShowService.MatchCandidate> matchCandidates) {
		List<ShowService.MatchCandidate> results = new ArrayList<>();
		for (ShowService.MatchCandidate searchResult : matchCandidates) {
			String cur = searchResult.getText().toLowerCase();
			// filter out low quality
			if (cur.contains("brrip")) {
				continue;
			}

			if (cur.contains(movieRequest.getTitle().toLowerCase())) {
				results.add(searchResult);
			}
		}
		return filterByQuality(results, MediaQuality.HD720P, MediaQuality.HD1080P);
	}

	private List<ShowService.MatchCandidate> filterByQuality(List<ShowService.MatchCandidate> matchCandidates, MediaQuality... qualities) {
		if (matchCandidates.isEmpty()) {
			return Collections.emptyList();
		}

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
