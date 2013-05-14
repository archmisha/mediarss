package rss.controllers;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.bind.annotation.*;
import rss.MediaRSSException;
import rss.dao.MovieDao;
import rss.dao.TorrentDao;
import rss.dao.UserDao;
import rss.entities.*;
import rss.services.SessionService;
import rss.services.movies.IMDBPreviewCacheService;
import rss.services.movies.IMDBService;
import rss.util.DurationMeter;

import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/movies")
public class MoviesController extends BaseController {

	@Autowired
	private UserDao userDao;

	@Autowired
	private SessionService sessionService;

	@Autowired
	private MovieDao movieDao;

	@Autowired
	private TorrentDao torrentDao;

	@Autowired
	private IMDBPreviewCacheService imdbPreviewCacheService;

	@Autowired
	private IMDBService imdbService;

	@RequestMapping(value = "/imdb/{movieId}", method = RequestMethod.GET)
	@ResponseBody
	@Transactional(propagation = Propagation.REQUIRED)
	public String getImdbPreviewPage(@PathVariable long movieId) {
		Movie movie = movieDao.find(movieId);
		return imdbPreviewCacheService.getImdbPreviewPage(movie);
	}

	@RequestMapping(value = "/imdb/css/{cssFileName}.css", method = RequestMethod.GET)
	@ResponseBody
	@Transactional(propagation = Propagation.REQUIRED)
	public String getImdbCssFile(@PathVariable String cssFileName) {
		return imdbPreviewCacheService.getImdbCSS(cssFileName);
	}

	@RequestMapping(value = "/imdb/image/{imageFileName}", method = RequestMethod.GET)
	@ResponseBody
	@Transactional(propagation = Propagation.REQUIRED)
	public void getImdbImage(@SuppressWarnings("ParameterCanBeLocal") @PathVariable String imageFileName, HttpServletRequest request, ServletResponse response) {
		// imageFileName can sometimes arrive without the suffix (like .jpg) for some reason
		StringBuffer url = request.getRequestURL();
		imageFileName = url.substring(url.indexOf("/imdb/image/") + "/imdb/image/".length());
//			response.setContentType("image/jpeg");

		InputStream imageInputStream = imdbService.getImage(imageFileName);

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
	public Map<String, Object> movieDownload(@RequestParam("torrentId") long torrentId,
											 @RequestParam("movieId") long movieId,
											 @RequestParam("isUserMovies") boolean isUserMovies) {
		User user = userDao.find(sessionService.getLoggedInUserId());
		Torrent torrent = torrentDao.find(torrentId);
		Pair<UserMovie, Boolean> futureMovieResult = movieService.addMovieDownload(user, movieId);
		UserMovie userMovie = futureMovieResult.getKey();
		UserMovieTorrent userMovieTorrent = new UserMovieTorrent();
		addUserTorrent(user, torrent, userMovieTorrent);
		userMovie.getUserMovieTorrents().add(userMovieTorrent);
		userMovieTorrent.setUserMovie(userMovie);
		if (user.getSubtitles() != null) {
//			subtitlesService.downloadEpisodeSubtitles(torrent, episode, user.getSubtitles());
		}

		logService.info(getClass(), "User " + user + " downloads " + userMovie.getMovie());
		Map<String, Object> result = new HashMap<>();
		if (isUserMovies) {
			result.put("movies", movieService.getUserMovies(user));
		} else {
			result.put("movies", movieService.getAvailableMovies(user));
			result.put("userMoviesCount", movieService.getUserMoviesCount(user));
		}
		return result;
	}

	@RequestMapping(value = "/view", method = RequestMethod.POST)
	@ResponseBody
	@Transactional(propagation = Propagation.REQUIRED)
	public void markMovieViewed(@RequestParam("movieId") long movieId) {
		User user = userDao.find(sessionService.getLoggedInUserId());
		movieService.markMovieViewed(user, movieId);
	}

	@RequestMapping(value = "/future/add", method = RequestMethod.POST)
	@ResponseBody
	@Transactional(propagation = Propagation.REQUIRED)
	public Map<String, Object> addFutureMovie(@RequestParam("imdbId") String imdbId) {
		User user = userDao.find(sessionService.getLoggedInUserId());
		Pair<UserMovie, Boolean> futureMovieResult = movieService.addFutureMovieDownload(user, imdbId);
		if (futureMovieResult == null) {
			throw new MediaRSSException("Movie ID was not found in IMDB").doNotLog();
		}

		Map<String, Object> result = new HashMap<>();
		UserMovie userMovie = futureMovieResult.getKey();
		Movie movie = userMovie.getMovie();
		if (futureMovieResult.getValue()) {
			result.put("message", "Movie '" + movie.getName() + "' was already scheduled for download");
		} else {
			if (!movie.getTorrentIds().isEmpty()) {
				result.put("message", "Movie '" + movie.getName() + "' was added to your movies and already available for download");
			} else {
				result.put("message", "Movie '" + movie.getName() + "' was scheduled for download when it will be available");
			}
		}

		logService.info(getClass(), "User " + user + " downloads " + movie);

		// must be after movieUserTorrent creation
		result.put("movies", movieService.getUserMovies(user));
		result.put("movieId", movie.getId());
		return result;
	}

	@RequestMapping(value = "/future/remove", method = RequestMethod.POST)
	@ResponseBody
	@Transactional(propagation = Propagation.REQUIRED)
	public Map<String, Object> removeFutureMovie(@RequestParam("movieId") long movieId) {
		User user = userDao.find(sessionService.getLoggedInUserId());
		UserMovie userMovie = movieDao.findUserMovie(movieId, user);
		// now idea how can happen, dima had it
		if (userMovie != null) {
			for (UserMovieTorrent userMovieTorrent : userMovie.getUserMovieTorrents()) {
				userTorrentDao.delete(userMovieTorrent);
			}
			movieDao.delete(userMovie);
		}

		Map<String, Object> result = new HashMap<>();
		result.put("message", "Movie '" + userMovie.getMovie().getName() + "' was removed from schedule for download");
		return result;
	}

	@RequestMapping(value = "/userMovies", method = RequestMethod.GET)
	@ResponseBody
	@Transactional(propagation = Propagation.REQUIRED)
	public Map<String, Object> getUserMovies() {
		User user = userDao.find(sessionService.getLoggedInUserId());
		Map<String, Object> result = new HashMap<>();
		result.put("movies", movieService.getUserMovies(user));
		return result;
	}

	@RequestMapping(value = "/availableMovies", method = RequestMethod.GET)
	@ResponseBody
	@Transactional(propagation = Propagation.REQUIRED)
	public Map<String, Object> getAvailableMovies() {
		User user = userDao.find(sessionService.getLoggedInUserId());
		Map<String, Object> result = new HashMap<>();
		result.put("movies", movieService.getAvailableMovies(user));
		return result;
	}

	@RequestMapping(value = "/initial-data", method = RequestMethod.GET)
	@ResponseBody
	@Transactional(propagation = Propagation.REQUIRED)
	public Map<String, Object> initialData() {
		User user = userDao.find(sessionService.getLoggedInUserId());

		DurationMeter duration = new DurationMeter();
		Map<String, Object> result = new HashMap<>();
		result.put("availableMovies", movieService.getAvailableMovies(user));
		result.put("userMoviesCount", movieService.getUserMoviesCount(user));
		result.put("moviesLastUpdated", getMoviesLastUpdated());
		result.put("isAdmin", isAdmin(user));
		duration.stop();
		logService.info(getClass(), "initialData " + duration.getDuration() + " millis");

		return result;
	}
}