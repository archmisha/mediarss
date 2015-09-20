package rss.services.searchers;

import org.apache.commons.lang3.tuple.Pair;
import rss.services.requests.episodes.DoubleEpisodeRequest;
import rss.services.requests.episodes.FullSeasonRequest;
import rss.services.requests.episodes.SingleEpisodeRequest;
import rss.services.requests.movies.MovieRequest;
import rss.torrents.Torrent;
import rss.torrents.searchers.MediaRequestVisitor;

import java.util.List;

/**
 * User: dikmanm
 * Date: 12/05/13 20:05
 */
public class PostParseSearchResultsVisitor implements MediaRequestVisitor<Pair<SimpleTorrentSearcher, SearchResult>, Void> {

	@Override
	public Void visit(SingleEpisodeRequest episodeRequest, Pair<SimpleTorrentSearcher, SearchResult> config) {
		return null;
	}

	@Override
	public Void visit(DoubleEpisodeRequest episodeRequest, Pair<SimpleTorrentSearcher, SearchResult> config) {
		return null;
	}

	@Override
	public Void visit(FullSeasonRequest episodeRequest, Pair<SimpleTorrentSearcher, SearchResult> config) {
		return null;
	}

	@Override
	public Void visit(MovieRequest movieRequest, Pair<SimpleTorrentSearcher, SearchResult> pair) {
		// now for the final results - should download the actual page to get the imdb info if exists
		List<Torrent> torrents = pair.getValue().getDownloadables();

		for (Torrent torrent : torrents) {
			if (torrent.getImdbId() != null) {
				return null;
			}
		}

		for (Torrent torrent : torrents) {
			if (torrent.getImdbId() == null) {
				String imdbUrl = pair.getKey().getImdbUrl(torrent);
				if (imdbUrl != null) {
					torrent.setImdbId(imdbUrl);
					return null;
				}
			}
		}

		return null;
	}
}
