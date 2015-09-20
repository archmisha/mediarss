package rss.torrents.searchers;

import rss.services.requests.episodes.DoubleEpisodeRequest;
import rss.services.requests.episodes.FullSeasonRequest;
import rss.services.requests.episodes.SingleEpisodeRequest;
import rss.services.requests.movies.MovieRequest;

/**
 * User: dikmanm
 * Date: 12/05/13 20:03
 */
public interface MediaRequestVisitor<S, T> {

	T visit(SingleEpisodeRequest episodeRequest, S config);

	T visit(DoubleEpisodeRequest episodeRequest, S config);

	T visit(FullSeasonRequest episodeRequest, S config);

	T visit(MovieRequest movieRequest, S config);
}
