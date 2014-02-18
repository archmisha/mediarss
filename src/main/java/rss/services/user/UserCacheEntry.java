package rss.services.user;

import rss.controllers.vo.ShowVO;
import rss.controllers.vo.ShowsScheduleVO;
import rss.controllers.vo.UserMovieVO;
import rss.entities.User;

import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * User: dikmanm
 * Date: 17/02/14 16:48
 */
public class UserCacheEntry {

	private Lock lock;
	private ShowsScheduleVO schedule;
	private List<ShowVO> trackedShows;
	private User user;
	private List<UserMovieVO> userMovies;

	public UserCacheEntry(User user) {
		lock = new ReentrantLock();
		this.user = user;
	}

	public void lock() {
		lock.lock();
	}

	public void unlock() {
		lock.unlock();
	}

	public ShowsScheduleVO getSchedule() {
		return schedule;
	}

	public void setSchedule(ShowsScheduleVO schedule) {
		this.schedule = schedule;
	}

	public List<ShowVO> getTrackedShows() {
		return trackedShows;
	}

	public void setTrackedShows(List<ShowVO> trackedShows) {
		this.trackedShows = trackedShows;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public User getUser() {
		return user;
	}

	public List<UserMovieVO> getUserMovies() {
		return userMovies;
	}

	public void setUserMovies(List<UserMovieVO> userMovies) {
		this.userMovies = userMovies;
	}
}
