package rss.services.user;

import rss.controllers.vo.ShowVO;
import rss.controllers.vo.ShowsScheduleVO;
import rss.controllers.vo.UserMovieVO;
import rss.entities.User;

import java.util.List;

/**
 * User: dikmanm
 * Date: 17/02/14 16:43
 */
public interface UserCacheService {

	ShowsScheduleVO getSchedule(User user);

	void invalidateSchedule(User user);

	List<ShowVO> getTrackedShows(User user);

	void invalidateTrackedShows(User user);

	void addUser(User user);

	User getUser(long userId);

	void invalidateUser(User user);


	List<UserMovieVO> getUserMovies(User user);

	void invalidateUserMovies(User user);

	int getUserMoviesCount(User user);
}
