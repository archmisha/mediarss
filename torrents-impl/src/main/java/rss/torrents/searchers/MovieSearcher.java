package rss.torrents.searchers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import rss.torrents.requests.movies.MovieRequest;
import rss.torrents.searchers.composite.DefaultCompositeSearcher;
import rss.torrents.searchers.composite.MoviesCompositeSearcher;
import rss.torrents.searchers.composite.torrentz.MovieTorrentzSearcher;
import rss.torrents.searchers.composite.torrentz.TorrentzSearcher;

/**
 * User: dikmanm
 * Date: 12/05/13 21:30
 */
@Service
public class MovieSearcher extends AbstractMediaSearcher<MovieRequest> {

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
