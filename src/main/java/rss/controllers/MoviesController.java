package rss.controllers;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import rss.MediaRSSException;
import rss.dao.MovieDao;
import rss.dao.TorrentDao;
import rss.dao.UserDao;
import rss.entities.*;
import rss.services.SessionService;
import rss.services.movies.IMDBPreviewCacheService;

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

	@RequestMapping(value = "/imdb/{movieId}", method = RequestMethod.GET)
	@ResponseBody
	@Transactional(propagation = Propagation.REQUIRED)
	public String getImdbPage(@PathVariable long movieId) {
		Movie movie = movieDao.find(movieId);
		return imdbPreviewCacheService.getImdbPreviewPage(movie);
	}

	@RequestMapping(value = "/imdb/css/{cssFileName}.css", method = RequestMethod.GET)
	@ResponseBody
	@Transactional(propagation = Propagation.REQUIRED)
	public String getImdbPage(@PathVariable String cssFileName) {
		return imdbPreviewCacheService.getImdbCSS(cssFileName);
	}

	@RequestMapping(value = "/download", method = RequestMethod.POST)
	@ResponseBody
	@Transactional(propagation = Propagation.REQUIRED)
	public void movieDownload(@RequestParam("torrentId") long torrentId, @RequestParam("movieId") long movieId) {
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
				result.put("message", "Movie '" + movie.getName() + "' was scheduled for immediate download as it is already available");

				// take where max seeders
				int seeders = -1;
				Torrent theTorrent = null;
				for (Torrent torrent : torrentDao.findByIds(movie.getTorrentIds())) {
					if (seeders == -1 || seeders < torrent.getSeeders()) {
						seeders = torrent.getSeeders();
						theTorrent = torrent;
					}
				}
				UserMovieTorrent userMovieTorrent = new UserMovieTorrent();
				addUserTorrent(user, theTorrent, userMovieTorrent);
				userMovieTorrent.setUserMovie(userMovie);
				userMovie.getUserMovieTorrents().add(userMovieTorrent);
			} else {
				result.put("message", "Movie '" + movie.getName() + "' was scheduled for download when it will be available");
			}
		}

		// must be after movieUserTorrent creation
		result.put("user", createUserResponse(user, MOVIES_TAB));
		result.put("movieId", movie.getId());
		return result;
	}

	@RequestMapping(value = "/future/remove", method = RequestMethod.POST)
	@ResponseBody
	@Transactional(propagation = Propagation.REQUIRED)
	public Map<String, Object> removeFutureMovie(@RequestParam("movieId") long movieId) {
		User user = userDao.find(sessionService.getLoggedInUserId());
		UserMovie userMovie = movieDao.findUserMovie(movieId, user);
		for (UserMovieTorrent userMovieTorrent : userMovie.getUserMovieTorrents()) {
			userTorrentDao.delete(userMovieTorrent);
		}
		movieDao.delete(userMovie);

		Map<String, Object> result = new HashMap<>();
		result.put("message", "Movie '" + userMovie.getMovie().getName() + "' was removed from schedule for download");
		return result;
	}
}