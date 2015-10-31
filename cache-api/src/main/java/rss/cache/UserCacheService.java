package rss.cache;

import rss.movies.UserMovieVO;
import rss.shows.ShowJSON;
import rss.shows.schedule.ShowsScheduleJSON;
import rss.user.User;

import java.util.List;

/**
 * User: dikmanm
 * Date: 17/02/14 16:43
 */
public interface UserCacheService {

	ShowsScheduleJSON getSchedule(User user);

	void invalidateSchedule(User user);

	List<ShowJSON> getTrackedShows(User user);

	void invalidateTrackedShows(User user);

	void addUser(User user);

	User getUser(long userId);

	void invalidateUser(User user);

	List<UserMovieVO> getUserMovies(User user);

	void invalidateUserMovies(User user);

	int getUserMoviesCount(User user);

	void invalidateAvailableMovies(User user);

	List<UserMovieVO> getAvailableMovies(User user);

	int getAvailableMoviesCount(User user);

	void invalidateMovies();
}
