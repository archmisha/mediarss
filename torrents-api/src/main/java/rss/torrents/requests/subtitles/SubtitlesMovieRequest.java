package rss.torrents.requests.subtitles;

import rss.torrents.Movie;
import rss.torrents.Torrent;
import rss.user.subtitles.SubtitleLanguage;

import java.util.List;

/**
 * User: dikmanm
 * Date: 15/05/13 15:41
 */
public class SubtitlesMovieRequest extends SubtitlesRequest {

	private Movie movie;

	public SubtitlesMovieRequest(Torrent torrent, Movie movie, List<SubtitleLanguage> languages) {
		super(torrent, movie.getName(), languages);
		this.movie = movie;
	}

	public Movie getMovie() {
		return movie;
	}
}
