package rss.torrents.downloader;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rss.torrents.requests.movies.MovieRequest;
import rss.torrents.searchers.SearchResult;
import rss.torrents.searchers.composite.torrentz.MovieTorrentzSearcher;

/**
 * User: dikmanm
 * Date: 18/05/13 08:46
 */
@Service
public class LatestMoviesDownloaderImpl extends MoviesDownloader implements LatestMoviesDownloader {

	@Autowired
	private MovieTorrentzSearcher movieTorrentzSearcher;

	@Override
	public SearchResult downloadTorrent(MovieRequest movieRequest) {
		return movieTorrentzSearcher.search(movieRequest);
	}
}
