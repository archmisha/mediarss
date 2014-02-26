package rss.controllers;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import rss.MediaRSSException;
import rss.dao.JobStatusDao;
import rss.dao.MovieDao;
import rss.dao.UserTorrentDao;
import rss.entities.*;
import rss.services.SessionService;
import rss.services.movies.IMDBPreviewCacheService;
import rss.services.movies.IMDBService;
import rss.services.movies.MovieService;
import rss.services.movies.MoviesScrabblerImpl;
import rss.util.DurationMeter;

import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/movies")
public class MoviesController extends BaseController {

	@Autowired
	private SessionService sessionService;

	@Autowired
	private MovieDao movieDao;

	@Autowired
	private IMDBPreviewCacheService imdbPreviewCacheService;

	@Autowired
	private IMDBService imdbService;

	@Autowired
	private JobStatusDao jobStatusDao;

	@Autowired
	private MovieService movieService;

	@Autowired
	protected UserTorrentDao userTorrentDao;

	@RequestMapping(value = "/imdb/{movieId}", method = RequestMethod.GET)
	@ResponseBody
	@Transactional(propagation = Propagation.REQUIRED)
	public String getImdbPreviewPage(@PathVariable long movieId) {
		Movie movie = movieDao.find(movieId);
		return movieService.getImdbPreviewPage(movie);
	}

	@RequestMapping(value = "/imdb/css/{cssFileName}.css", method = RequestMethod.GET)
	@ResponseBody
	@Transactional(propagation = Propagation.REQUIRED)
	public String getImdbCssFile(@PathVariable String cssFileName) {
		return imdbPreviewCacheService.getImdbCSS(cssFileName);
	}

	@RequestMapping(value = "/imdb/person-image/{imageFileName}", method = RequestMethod.GET)
	@ResponseBody
	@Transactional(propagation = Propagation.REQUIRED)
	public void getImdbPersonImage(@SuppressWarnings("ParameterCanBeLocal") @PathVariable String imageFileName, HttpServletRequest request, ServletResponse response) {
		// imageFileName can sometimes arrive without the suffix (like .jpg) for some reason
		StringBuffer url = request.getRequestURL();
		imageFileName = url.substring(url.indexOf("/imdb/person-image/") + "/imdb/person-image/".length());
		InputStream imageInputStream = imdbService.getPersonImage(imageFileName);
		try (OutputStream os = response.getOutputStream()) {
			int bytes = IOUtils.copy(imageInputStream, os);
			response.setContentLength(bytes);
			os.flush();
		} catch (Exception e) {
			throw new MediaRSSException("Failed downloading IMDB image " + imageFileName + ": " + e.getMessage(), e);
		}
	}

	@RequestMapping(value = "/imdb/movie-image/{imageFileName}", method = RequestMethod.GET)
	@ResponseBody
	@Transactional(propagation = Propagation.REQUIRED)
	public void getImdbMovieImage(@SuppressWarnings("ParameterCanBeLocal") @PathVariable String imageFileName, HttpServletRequest request, ServletResponse response) {
		// imageFileName can sometimes arrive without the suffix (like .jpg) for some reason
		StringBuffer url = request.getRequestURL();
		imageFileName = url.substring(url.indexOf("/imdb/movie-image/") + "/imdb/movie-image/".length());
		InputStream imageInputStream = imdbService.getMovieImage(imageFileName);
		try (OutputStream os = response.getOutputStream()) {
			int bytes = IOUtils.copy(imageInputStream, os);
			response.setContentLength(bytes);
			os.flush();
		} catch (Exception e) {
			throw new MediaRSSException("Failed downloading IMDB image " + imageFileName + ": " + e.getMessage(), e);
		}
	}

	@RequestMapping(value = "/download", method = RequestMethod.POST)
	@ResponseBody
	@Transactional(propagation = Propagation.REQUIRED)
	public Map<String, Object> downloadMovie(@RequestParam("torrentId") long torrentId,
											 @RequestParam("movieId") long movieId,
											 @RequestParam("isUserMovies") boolean isUserMovies) {
		User user = userCacheService.getUser(sessionService.getLoggedInUserId());
		movieService.addMovieDownload(user, movieId, torrentId);
		userCacheService.invalidateUserMovies(user);

		Map<String, Object> result = new HashMap<>();
		if (isUserMovies) {
			result.put("movies", userCacheService.getUserMovies(user));
		} else {
			result.put("movies", movieService.getAvailableMovies(user));
			result.put("userMoviesCount", userCacheService.getUserMoviesCount(user));
		}
		return result;
	}

//	@RequestMapping(value = "/view", method = RequestMethod.POST)
//	@ResponseBody
//	@Transactional(propagation = Propagation.REQUIRED)
//	public void markMovieViewed(@RequestParam("movieId") long movieId) {
//		User user = userCacheService.getUser(sessionService.getLoggedInUserId());
//		movieService.markMovieViewed(user, movieId);
//	}

	@RequestMapping(value = "/future/add", method = RequestMethod.POST)
	@ResponseBody
	@Transactional(propagation = Propagation.REQUIRED)
	public Map<String, Object> addMovieFromSearch(@RequestParam("imdbId") String imdbId) {
		User user = userCacheService.getUser(sessionService.getLoggedInUserId());
		Pair<Movie, Boolean> futureMovieResult = movieService.addFutureMovieDownload(user, imdbId);
		if (futureMovieResult == null) {
			throw new MediaRSSException("Movie ID was not found in IMDB").doNotLog();
		}

		Map<String, Object> result = new HashMap<>();
		Movie movie = futureMovieResult.getKey();
		if (futureMovieResult.getValue()) {
			result.put("message", "Movie '" + movie.getName() + "' was added");
		} else {
			if (!movie.getTorrentIds().isEmpty()) {
				result.put("message", "Movie '" + movie.getName() + "' was added to your movies and already available for download");
			} else {
				result.put("message", "Movie '" + movie.getName() + "' is being searched for torrents");//was added but is not yet available for download");
			}
		}

		userCacheService.invalidateUserMovies(user);
		logService.info(getClass(), "User " + user + " downloads " + movie);

		// must be after movieUserTorrent creation
		result.put("movies", userCacheService.getUserMovies(user));
		result.put("movieId", movie.getId());
		return result;
	}

	@RequestMapping(value = "/search", method = RequestMethod.POST)
	@ResponseBody
	@Transactional(propagation = Propagation.REQUIRED)
	public Map<String, Object> search(@RequestParam("query") String query) {
		User user = userCacheService.getUser(sessionService.getLoggedInUserId());
		Map<String, Object> result = new HashMap<>();
		result.put("searchResults", movieService.search(user, query));
		return result;
	}

	@RequestMapping(value = "/future/remove", method = RequestMethod.POST)
	@ResponseBody
	@Transactional(propagation = Propagation.REQUIRED)
	public Map<String, Object> removeFutureMovie(@RequestParam("movieId") long movieId) {
		User user = userCacheService.getUser(sessionService.getLoggedInUserId());
		UserMovie userMovie = movieDao.findUserMovie(user, movieId);
		// no idea how can happen, dima had it
		if (userMovie != null) {
			for (UserMovieTorrent userMovieTorrent : userMovie.getUserMovieTorrents()) {
				userTorrentDao.delete(userMovieTorrent);
			}
			movieDao.delete(userMovie);
			userCacheService.invalidateUserMovies(user);
		}

		Map<String, Object> result = new HashMap<>();
		result.put("message", "Movie " + (userMovie == null ? "" : "'" + userMovie.getMovie().getName() + "'") + " was removed from your movies");
		return result;
	}

	@RequestMapping(value = "check-movies-being-searched", method = RequestMethod.POST)
	@ResponseBody
	@Transactional(propagation = Propagation.REQUIRED)
	public Map<String, Object> checkMoviesBeingSearched(@RequestParam("ids[]") long[] ids) {
		Map<String, Object> map = new HashMap<>();
		map.put("completed", movieService.getSearchCompletedMovies(ids));
		return map;
	}

	@RequestMapping(value = "/user-movies", method = RequestMethod.GET)
	@ResponseBody
	@Transactional(propagation = Propagation.REQUIRED)
	public Map<String, Object> getUserMovies() {
		DurationMeter duration = new DurationMeter();
		User user = userCacheService.getUser(sessionService.getLoggedInUserId());

		Map<String, Object> result = new HashMap<>();
		result.put("movies", userCacheService.getUserMovies(user));
		duration.stop();
		logService.info(getClass(), "movies [userMovies] " + duration.getDuration() + " ms");
		return result;
	}

	@RequestMapping(value = "/available-movies", method = RequestMethod.GET)
	@ResponseBody
	@Transactional(propagation = Propagation.REQUIRED)
	public Map<String, Object> getAvailableMovies() {
		DurationMeter duration = new DurationMeter();
		User user = userCacheService.getUser(sessionService.getLoggedInUserId());

		Map<String, Object> result = new HashMap<>();
		result.put("movies", userCacheService.getAvailableMovies(user));
		duration.stop();
		logService.info(getClass(), "movies [availableMovies] " + duration.getDuration() + " ms");
		return result;
	}

	@RequestMapping(value = "/initial-data/{category}", method = RequestMethod.GET)
	@ResponseBody
	@Transactional(propagation = Propagation.REQUIRED)
	public Map<String, Object> initialData(@PathVariable(value = "category") String category) {
		DurationMeter duration = new DurationMeter();
		User user = userCacheService.getUser(sessionService.getLoggedInUserId());

		Map<String, Object> result = new HashMap<>();
		if (category.equals("availableMovies")) {
			result.put("availableMovies", userCacheService.getAvailableMovies(user));
			result.put("userMoviesCount", userCacheService.getUserMoviesCount(user));
		} else {
			result.put("userMovies", userCacheService.getUserMovies(user));
			result.put("availableMoviesCount", userCacheService.getAvailableMoviesCount(user));
		}
		result.put("moviesLastUpdated", getMoviesLastUpdated());
		result.put("isAdmin", isAdmin(user));
		duration.stop();
		logService.info(getClass(), "movies [" + category + "] " + duration.getDuration() + " ms");

		return result;
	}

	private Date getMoviesLastUpdated() {
		JobStatus jobStatus = jobStatusDao.find(MoviesScrabblerImpl.JOB_NAME);
		if (jobStatus == null) {
			return null;
		}
		return jobStatus.getStart();
	}
}