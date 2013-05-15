package rss.services.searchers;

import rss.services.requests.episodes.EpisodeRequest;
import rss.services.requests.movies.MovieRequest;

/**
 * User: dikmanm
 * Date: 12/05/13 20:03
 */
public interface MediaRequestVisitor<S, T> {

	T visit(EpisodeRequest episodeRequest, S config);

	T visit(MovieRequest movieRequest, S config);
}
