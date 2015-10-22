package rss.movies;

import org.springframework.stereotype.Component;
import rss.movies.dao.MovieImpl;
import rss.torrents.Movie;

/**
 * User: dikmanm
 * Date: 18/10/2015 20:41
 */
@Component
public class MovieServiceFactoryImpl implements MovieServiceFactory {
    public Movie createMovie() {
        return new MovieImpl();
    }
}
