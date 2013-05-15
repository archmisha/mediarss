package rss.services.searchers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import rss.entities.Movie;
import rss.services.requests.movies.MovieRequest;
import rss.services.searchers.composite.DefaultCompositeSearcher;
import rss.services.searchers.composite.MoviesCompositeSearcher;
import rss.services.searchers.composite.torrentz.MovieTorrentzSearcher;
import rss.services.searchers.composite.torrentz.TorrentzSearcher;

/**
 * User: dikmanm
 * Date: 12/05/13 21:30
 */
@Service
public class MovieSearcher extends AbstractMediaSearcher<MovieRequest, Movie> {

	@Autowired
	@Qualifier("moviesCompositeSearcher")
	private MoviesCompositeSearcher moviesCompositeSearcher;

	@Autowired
	private MovieTorrentzSearcher movieTorrentzSearcher;

	@Override
	protected DefaultCompositeSearcher<MovieRequest> getDefaultCompositeSearcher() {
		return moviesCompositeSearcher;
	}

	@Override
	protected TorrentzSearcher<MovieRequest> getTorrentzSearcher() {
		return movieTorrentzSearcher;
	}
}
