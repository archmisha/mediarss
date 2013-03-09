package rss.controllers.vo;

import java.util.ArrayList;
import java.util.Date;

/**
 * User: Michael Dikman
 * Date: 01/12/12
 * Time: 09:05
 */
public class UserResponse {

	private UserVO user;
	private String tvShowsRssFeed;
	private String moviesRssFeed;
	private ArrayList<UserMovieVO> movies;
	private Date moviesLastUpdated;
	private ArrayList<ShowVO> shows;

	public UserResponse(UserVO user, String tvShowsRssFeed, String moviesRssFeed, ArrayList<ShowVO> shows) {
		this.user = user;
		this.tvShowsRssFeed = tvShowsRssFeed;
		this.moviesRssFeed = moviesRssFeed;
		this.shows = shows;
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

	public ArrayList<UserMovieVO> getMovies() {
		return movies;
	}

	public UserResponse withMovies(ArrayList<UserMovieVO> movies) {
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

	public ArrayList<ShowVO> getShows() {
		return shows;
	}
}
