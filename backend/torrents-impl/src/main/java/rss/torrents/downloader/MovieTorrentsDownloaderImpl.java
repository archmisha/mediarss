package rss.torrents.downloader;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rss.torrents.requests.movies.MovieRequest;
import rss.torrents.searchers.MovieSearcher;
import rss.torrents.searchers.SearchResult;

/**
 * User: Michael Dikman
 * Date: 03/12/12
 * Time: 09:10
 */
@Service
public class MovieTorrentsDownloaderImpl extends MoviesDownloader implements MovieTorrentsDownloader {

	@Autowired
	private MovieSearcher movieSearcher;

	@Override
	public SearchResult downloadTorrent(MovieRequest movieRequest) {
		return movieSearcher.search(movieRequest);
	}
}
