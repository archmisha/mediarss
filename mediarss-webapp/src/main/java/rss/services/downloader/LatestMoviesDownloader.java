package rss.services.downloader;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rss.services.requests.movies.MovieRequest;
import rss.services.searchers.SearchResult;
import rss.services.searchers.composite.torrentz.MovieTorrentzSearcher;

/**
 * User: dikmanm
 * Date: 18/05/13 08:46
 */
@Service
public class LatestMoviesDownloader extends MoviesDownloader {

	@Autowired
	private MovieTorrentzSearcher movieTorrentzSearcher;

	@Override
	protected SearchResult downloadTorrent(MovieRequest movieRequest) {
		return movieTorrentzSearcher.search(movieRequest);
	}
}
