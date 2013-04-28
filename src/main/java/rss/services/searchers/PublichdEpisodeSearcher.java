package rss.services.searchers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rss.entities.Episode;
import rss.services.searchers.SearchResult;
import rss.services.requests.EpisodeRequest;
import rss.services.shows.ShowService;

import java.util.Collections;

/**
 * User: Michael Dikman
 * Date: 04/12/12
 * Time: 19:17
 */
@Service("publichdEpisodeSearcher")
public class PublichdEpisodeSearcher extends PublichdSearcher<EpisodeRequest, Episode> {

	@Autowired
	private ShowService showService;

	protected boolean isMatching(final EpisodeRequest mediaRequest, SearchResult<Episode> searchResult) {
		ShowService.MatchCandidate matchCandidate = new ShowService.MatchCandidate() {
			@Override
			public String getText() {
				return mediaRequest.getTitle();
			}

			@SuppressWarnings("unchecked")
			@Override
			public <T> T getObject() {
				return (T) mediaRequest;
			}
		};

		if (!showService.filterMatching(mediaRequest, Collections.singletonList(matchCandidate)).isEmpty()) {
			return true;
		}
		return false;
	}
}
