package rss.movies;

import rss.torrents.Movie;
import rss.user.User;

import java.util.Date;
import java.util.Set;

/**
 * User: dikmanm
 * Date: 12/02/13 21:06
 */
public interface UserMovie {

    User getUser();

    void setUser(User user);

    Date getUpdated();

    void setUpdated(Date updated);

    Movie getMovie();

    void setMovie(Movie movie);

    Set<UserMovieTorrent> getUserMovieTorrents();

    void setUserMovieTorrents(Set<UserMovieTorrent> userMovieTorrents);
}
