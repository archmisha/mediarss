package rss.services.searchers.composite;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import rss.entities.Episode;
import rss.entities.Movie;
import rss.services.requests.MediaRequest;
import rss.entities.Media;
import rss.services.requests.MovieRequest;
import rss.services.requests.ShowRequest;

import java.util.Arrays;
import java.util.Collection;

/**
 * User: Michael Dikman
 * Date: 04/12/12
 * Time: 19:15
 */
@Service("compositeMoviesSearcher")
public class MoviesCompositeSearcher extends MediaCompositeSearcher<MovieRequest, Movie> {

	@Autowired
	@Qualifier("movieTorrentzSearcher")
	private CompositeTorrentSearcher<MovieRequest, Movie> movieTorrentzSearcher;

	@Override
	protected CompositeTorrentSearcher<MovieRequest, Movie> getTorrentzSearcher() {
		return movieTorrentzSearcher;
	}

	@Override
    protected boolean shouldFailOnNoIMDBUrl() {
        return true;
    }
}
