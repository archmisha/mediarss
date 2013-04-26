package rss.services.searchers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rss.entities.Episode;
import rss.services.SearchResult;
import rss.services.requests.EpisodeRequest;
import rss.services.searchers.TorrentSearcher1337x;
import rss.services.shows.ShowService;

import java.util.ArrayList;
import java.util.List;

/**
 * User: dikmanm
 * Date: 13/04/13 18:25
 */
@Service("episodeTorrentSearcher1337x")
public class EpisodeTorrentSearcher1337x extends TorrentSearcher1337x<EpisodeRequest, Episode> {

	@Autowired
	private ShowService showService;

	@Override
	protected List<SearchResult<Episode>> filterMatching(EpisodeRequest mediaRequest, List<SearchResult<Episode>> searchResults) {
		List<ShowService.MatchCandidate> matchCandidates = new ArrayList<>();
		for (final SearchResult<Episode> searchResult : searchResults) {
			matchCandidates.add(new ShowService.MatchCandidate() {
				@Override
				public String getText() {
					return searchResult.getTorrent().getTitle();
				}

				@SuppressWarnings("unchecked")
				@Override
				public <T> T getObject() {
					return (T) searchResult;
				}
			});
		}

		List<SearchResult<Episode>> results = new ArrayList<>();
		for (ShowService.MatchCandidate matchCandidate : showService.filterMatching(mediaRequest, matchCandidates)) {
			results.add(matchCandidate.<SearchResult<Episode>>getObject());
		}
		return results;
	}
}
