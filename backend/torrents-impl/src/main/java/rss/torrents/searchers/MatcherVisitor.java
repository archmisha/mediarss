package rss.torrents.searchers;

import rss.shows.ShowService;
import rss.torrents.MediaQuality;
import rss.torrents.matching.MatchCandidate;
import rss.torrents.requests.MediaRequest;
import rss.torrents.requests.movies.MovieRequest;
import rss.torrents.requests.shows.DoubleEpisodeRequest;
import rss.torrents.requests.shows.EpisodeRequest;
import rss.torrents.requests.shows.FullSeasonRequest;
import rss.torrents.requests.shows.SingleEpisodeRequest;
import rss.torrents.searchers.composite.torrentz.TorrentzParserImpl;
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

	private List<MatchCandidate> episodeVisitHelper(EpisodeRequest episodeRequest, List<MatchCandidate> matchCandidates) {
		return filterByQuality(showService.filterMatching(episodeRequest, matchCandidates), MediaQuality.HD720P, MediaQuality.HD1080P, MediaQuality.NORMAL);
	}

	@Override
	public List<MatchCandidate> visit(MediaRequest mediaRequest, List<MatchCandidate> config) {
		return SearcherUtils.applyVisitor(this, mediaRequest, config);
	}

	@Override
	public List<MatchCandidate> visit(SingleEpisodeRequest episodeRequest, List<MatchCandidate> config) {
		return episodeVisitHelper(episodeRequest, config);
	}

	@Override
	public List<MatchCandidate> visit(DoubleEpisodeRequest episodeRequest, List<MatchCandidate> config) {
		return episodeVisitHelper(episodeRequest, config);
	}

	@Override
	public List<MatchCandidate> visit(FullSeasonRequest episodeRequest, List<MatchCandidate> config) {
		return episodeVisitHelper(episodeRequest, config);
	}

	@Override
	public List<MatchCandidate> visit(MovieRequest movieRequest, List<MatchCandidate> matchCandidates) {
		List<MatchCandidate> results = new ArrayList<>();
		for (MatchCandidate searchResult : matchCandidates) {
			String cur = searchResult.getText().toLowerCase();
			// filter out low quality
			boolean skip = false;
			for (String type : TorrentzParserImpl.TYPES_TO_SKIP) {
				if (cur.contains(type)) {
					skip = true;
				}
			}

			if (!skip && cur.contains(movieRequest.getTitle().toLowerCase())) {
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
