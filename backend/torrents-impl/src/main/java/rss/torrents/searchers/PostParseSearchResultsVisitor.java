package rss.torrents.searchers;

import org.apache.commons.lang3.tuple.Pair;
import rss.torrents.Torrent;
import rss.torrents.requests.MediaRequest;
import rss.torrents.requests.movies.MovieRequest;
import rss.torrents.requests.shows.DoubleEpisodeRequest;
import rss.torrents.requests.shows.FullSeasonRequest;
import rss.torrents.requests.shows.SingleEpisodeRequest;

import java.util.List;

/**
 * User: dikmanm
 * Date: 12/05/13 20:05
 */
public class PostParseSearchResultsVisitor implements MediaRequestVisitor<Pair<SimpleTorrentSearcher, SearchResult>, Void> {

	@Override
	public Void visit(MediaRequest mediaRequest, Pair<SimpleTorrentSearcher, SearchResult> config) {
		SearcherUtils.applyVisitor(this, mediaRequest, config);
		return null;
	}

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
