package rss.services.movies;

import rss.entities.Movie;
import rss.services.downloader.DownloadResult;
import rss.services.downloader.MovieRequest;

/**
 * User: dikmanm
 * Date: 16/03/13 22:12
 */
public interface TorrentzService {

	DownloadResult<Movie, MovieRequest> downloadLatestMovies();

	DownloadResult<Movie, MovieRequest> downloadMovie(Movie movie);
}
