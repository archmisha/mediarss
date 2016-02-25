package rss.torrents.searchers;

import rss.torrents.requests.MediaRequest;
import rss.torrents.requests.movies.MovieRequest;
import rss.torrents.requests.shows.DoubleEpisodeRequest;
import rss.torrents.requests.shows.FullSeasonRequest;
import rss.torrents.requests.shows.SingleEpisodeRequest;

/**
 * User: dikmanm
 * Date: 12/05/13 20:03
 */
public interface MediaRequestVisitor<S, T> {

	T visit(MediaRequest mediaRequest, S config);

	T visit(SingleEpisodeRequest episodeRequest, S config);

	T visit(DoubleEpisodeRequest episodeRequest, S config);

	T visit(FullSeasonRequest episodeRequest, S config);

	T visit(MovieRequest movieRequest, S config);
}
