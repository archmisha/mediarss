package rss.services.downloader;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rss.services.requests.movies.MovieRequest;
import rss.services.searchers.MovieSearcher;
import rss.services.searchers.SearchResult;

/**
 * User: Michael Dikman
 * Date: 03/12/12
 * Time: 09:10
 */
@Service
public class MovieTorrentsDownloader extends MoviesDownloader {

	@Autowired
	private MovieSearcher movieSearcher;

	@Override
	protected SearchResult downloadTorrent(MovieRequest movieRequest) {
		return movieSearcher.search(movieRequest);
	}
}
