package rss.services.matching;

import rss.entities.MediaQuality;
import rss.services.requests.episodes.EpisodeRequest;
import rss.services.requests.movies.MovieRequest;
import rss.services.searchers.MediaRequestVisitor;
import rss.services.shows.ShowService;
import rss.util.CollectionUtils;

import java.util.*;

/**
 * User: dikmanm
 * Date: 28/04/13 23:37
 */
public class MatcherVisitor implements MediaRequestVisitor<List<MatchCandidate>, List<MatchCandidate>> {

	private ShowService showService;

	public MatcherVisitor(ShowService showService) {
		this.showService = showService;
	}

	@Override
	public List<MatchCandidate> visit(EpisodeRequest episodeRequest, List<MatchCandidate> matchCandidates) {
		return filterByQuality(showService.filterMatching(episodeRequest, matchCandidates), MediaQuality.HD720P, MediaQuality.HD1080P, MediaQuality.NORMAL);
	}

	@Override
	public List<MatchCandidate> visit(MovieRequest movieRequest, List<MatchCandidate> matchCandidates) {
		List<MatchCandidate> results = new ArrayList<>();
		for (MatchCandidate searchResult : matchCandidates) {
			String cur = searchResult.getText().toLowerCase();
			// filter out low quality
			if (cur.contains("brrip") || cur.contains("dvdscr")) {
				continue;
			}

			if (cur.contains(movieRequest.getTitle().toLowerCase())) {
				results.add(searchResult);
			}
		}
		return filterByQuality(results, MediaQuality.HD720P, MediaQuality.HD1080P);
	}

	private List<MatchCandidate> filterByQuality(List<MatchCandidate> matchCandidates, MediaQuality... qualities) {
		if (matchCandidates.isEmpty()) {
			return Collections.emptyList();
		}

		// map candidates by quality
		Map<MediaQuality, List<MatchCandidate>> map = new HashMap<>();
		for (MatchCandidate candidate : matchCandidates) {
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
