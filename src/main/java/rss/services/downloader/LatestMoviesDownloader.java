package rss.services.downloader;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rss.services.requests.movies.MovieRequest;
import rss.services.searchers.SearchResult;
import rss.services.searchers.composite.torrentz.MovieTorrentzSearcher;

import java.util.Collection;

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

	@Override
	protected void processMissingRequests(Collection<MovieRequest> missing) {
		// no need to send emails here, we didn't search by name for something specific
		// if one of the movies in the latest list is not found, it means maybe was no IMDB ID on the page
	}
}
