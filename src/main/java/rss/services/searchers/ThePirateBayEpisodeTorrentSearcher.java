package rss.services.searchers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rss.entities.Episode;
import rss.entities.Torrent;
import rss.services.requests.EpisodeRequest;
import rss.services.shows.ShowService;

import java.util.ArrayList;
import java.util.List;

/**
 * User: dikmanm
 * Date: 13/04/13 14:26
 */
@Service("thePirateBayEpisodeTorrentSearcher")
public class ThePirateBayEpisodeTorrentSearcher extends ThePirateBayTorrentSearcher<EpisodeRequest, Episode> {

	@Autowired
	private ShowService showService;

	@Override
	protected void verifySearchResults(EpisodeRequest mediaRequest, List<Torrent> results) {
		List<ShowService.MatchCandidate> matchCandidates = new ArrayList<>();
		for (final Torrent torrent : results) {
			matchCandidates.add(new ShowService.MatchCandidate() {
				@Override
				public String getText() {
					return torrent.getTitle();
				}

				@SuppressWarnings("unchecked")
				@Override
				public <T> T getObject() {
					return (T) torrent;
				}
			});
		}

		results.clear();
		for (ShowService.MatchCandidate matchCandidate : showService.filterMatching(mediaRequest, matchCandidates)) {
			results.add(matchCandidate.<Torrent>getObject());
		}
	}

	@Override
	protected void logNoImdbFound(Torrent torrent) {
		logService.debug(getClass(), "Didn't find IMDB url for: " + torrent.getTitle());
	}
}
