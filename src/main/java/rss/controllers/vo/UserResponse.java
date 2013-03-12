package rss.controllers.vo;

import java.util.Date;
import java.util.List;

/**
 * User: Michael Dikman
 * Date: 01/12/12
 * Time: 09:05
 */
public class UserResponse {

	private UserVO user;
	private String tvShowsRssFeed;
	private String moviesRssFeed;
	private List<UserMovieVO> movies;
	private Date moviesLastUpdated;
	private List<ShowVO> shows;

	public UserResponse(UserVO user, String tvShowsRssFeed, String moviesRssFeed) {
		this.user = user;
		this.tvShowsRssFeed = tvShowsRssFeed;
		this.moviesRssFeed = moviesRssFeed;
	}

	public UserVO getUser() {
		return user;
	}

	public String getTvShowsRssFeed() {
		return tvShowsRssFeed;
	}

	public String getMoviesRssFeed() {
		return moviesRssFeed;
	}

	public List<UserMovieVO> getMovies() {
		return movies;
	}

	public UserResponse withMovies(List<UserMovieVO> movies) {
		this.movies = movies;
		return this;
	}

	public UserResponse withMoviesLastUpdated(Date moviesLastUpdated) {
		this.moviesLastUpdated = moviesLastUpdated;
		return this;
	}

	public Date getMoviesLastUpdated() {
		return moviesLastUpdated;
	}

	public UserResponse withShows(List<ShowVO> shows) {
		this.shows = shows;
		return this;
	}

	public List<ShowVO> getShows() {
		return shows;
	}
}
