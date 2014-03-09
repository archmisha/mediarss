package rss.services.movies;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import rss.dao.MovieDao;
import rss.entities.Movie;
import rss.services.SettingsService;
import rss.services.downloader.DownloadConfig;
import rss.services.downloader.MovieTorrentsDownloader;
import rss.services.log.LogService;
import rss.services.requests.movies.MovieRequest;
import rss.util.DurationMeter;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * User: dikmanm
 * Date: 20/02/14 17:48
 */
@Service
public class TopMoviesServiceImpl implements TopMoviesService {

	private static final int TOP_MOVIES_COUNT = 20;
	public static final String TOP_MOVIES_KEY = "topMovies";

	@Autowired
	private TopMoviesDownloader movieFoneTopMoviesDownloader;

	@Autowired
	private SettingsService settingsService;

	@Autowired
	private MovieDao movieDao;

	@Autowired
	private LogService logService;

	@Autowired
	private MovieTorrentsDownloader movieTorrentsDownloader;

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public void downloadTopMovies() {
		logService.info(getClass(), "Downloading top movies");
		DurationMeter duration = new DurationMeter();

		Set<Movie> movies = movieFoneTopMoviesDownloader.getTopMovies(TOP_MOVIES_COUNT);

		Set<MovieRequest> movieRequests = new HashSet<>();
		for (Movie movie : movies) {
			MovieRequest movieRequest = new MovieRequest(movie.getName(), null);
			movieRequest.setImdbId(movie.getImdbUrl());
			movieRequests.add(movieRequest);
		}
		movieTorrentsDownloader.download(movieRequests, new DownloadConfig());

		Set<Long> ids = new HashSet<>();
		for (Movie movie : movies) {
			ids.add(movie.getId());
		}
		settingsService.setPersistentSetting(TOP_MOVIES_KEY, StringUtils.join(ids, ","));

		duration.stop();
		logService.info(getClass(), "Downloading top movies took " + duration.getDuration() + " ms");
	}

	@Override
	public Collection<Movie> getTopMovies() {
		Set<Long> ids = new HashSet<>();
		String val = settingsService.getPersistentSetting(TOP_MOVIES_KEY);
		if (StringUtils.isBlank(val)) {
			return Collections.emptySet();
		}
		for (String str : StringUtils.split(val, ",")) {
			ids.add(Long.parseLong(str));
		}
		return movieDao.find(ids);
	}
}
