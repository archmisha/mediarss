package rss.services.movies;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import rss.dao.MovieDao;
import rss.entities.Movie;
import rss.services.SettingsService;
import rss.services.log.LogService;
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

	private static final int TOP_MOVIES_COUNT = 30;

	@Autowired
	private TopMoviesDownloader movieFoneTopMoviesDownloader;

	@Autowired
	private SettingsService settingsService;

	@Autowired
	private MovieDao movieDao;

	@Autowired
	private LogService logService;

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public void downloadTopMovies() {
		logService.info(getClass(), "Downloading top movies");
		DurationMeter duration = new DurationMeter();

		Set<Movie> topMovies = movieFoneTopMoviesDownloader.getTopMovies(TOP_MOVIES_COUNT);
		Set<Long> ids = new HashSet<>();
		for (Movie movie : topMovies) {
			ids.add(movie.getId());
		}
		settingsService.setPersistentSetting("topMovies", StringUtils.join(ids, ","));

		duration.stop();
		logService.info(getClass(), "Downloading top movies took " + duration.getDuration() + " ms");
	}

	@Override
	public Collection<Movie> getTopMovies() {
		Set<Long> ids = new HashSet<>();
		String val = settingsService.getPersistentSetting("topMovies");
		if (StringUtils.isBlank(val)) {
			return Collections.emptySet();
		}
		for (String str : StringUtils.split(val, ",")) {
			ids.add(Long.parseLong(str));
		}
		return movieDao.find(ids);
	}
}
