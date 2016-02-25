package rss.torrents.downloader;

import rss.torrents.Movie;
import rss.torrents.requests.movies.MovieRequest;
import rss.torrents.searchers.SearchResult;

import java.util.Set;

/**
 * User: dikmanm
 * Date: 16/10/2015 12:41
 */
public interface MovieTorrentsDownloader {

    SearchResult downloadTorrent(MovieRequest movieRequest);

    DownloadResult<Movie, MovieRequest> download(Set<MovieRequest> movieRequests, DownloadConfig downloadConfig);
}
