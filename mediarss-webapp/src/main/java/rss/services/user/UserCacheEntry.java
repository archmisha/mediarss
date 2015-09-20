package rss.services.user;

import rss.controllers.vo.UserMovieVO;
import rss.entities.User;
import rss.shows.ShowsScheduleJSON;

import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * User: dikmanm
 * Date: 17/02/14 16:48
 */
public class UserCacheEntry {

    private Lock lock;
    private ShowsScheduleJSON schedule;
    private List<Long> trackedShows;
    private User user;
    private List<UserMovieVO> userMovies;
    private List<UserMovieVO> availableMovies;

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

    public ShowsScheduleJSON getSchedule() {
        return schedule;
    }

    public void setSchedule(ShowsScheduleJSON schedule) {
        this.schedule = schedule;
    }

    public List<Long> getTrackedShows() {
        return trackedShows;
    }

    public void setTrackedShows(List<Long> trackedShows) {
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

    public List<UserMovieVO> getAvailableMovies() {
        return availableMovies;
    }

    public void setAvailableMovies(List<UserMovieVO> availableMovies) {
        this.availableMovies = availableMovies;
    }
}
