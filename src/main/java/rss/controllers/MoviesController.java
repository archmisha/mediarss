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

import javax.servlet.http.HttpServletRequest;
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

	@RequestMapping(value = "/imdb/{movieId}", method = RequestMethod.GET)
	@ResponseBody
	@Transactional(propagation = Propagation.REQUIRED)
	public String getImdbPage(@PathVariable long movieId) {
		Movie movie = movieDao.find(movieId);

		String page = sessionService.getImdbMoviePage(movie.getId());
		if (page != null) {
			logService.debug(getClass(), "IMDB page for movie " + movie.getName() + " was found in cache");
		} else {
			page = movieService.getImdbPreviewPage(movie);
			sessionService.setImdbMoviePage(movie.getId(), page);
		}

		return page;
	}

	@RequestMapping(value = "/download", method = RequestMethod.POST)
	@ResponseBody
	@Transactional(propagation = Propagation.REQUIRED)
	public void movieDownload(HttpServletRequest request) {
		long torrentId = extractMandatoryInteger(request, "torrentId");
		User user = userDao.find(sessionService.getLoggedInUserId());
		Torrent torrent = torrentDao.find(torrentId);
		addUserTorrent(user, torrent, new MovieUserTorrent());
		if (user.getSubtitles() != null) {
//			subtitlesService.downloadEpisodeSubtitles(torrent, episode, user.getSubtitles());
		}
	}

	@RequestMapping(value = "/view", method = RequestMethod.POST)
	@ResponseBody
	@Transactional(propagation = Propagation.REQUIRED)
	public void markMovieViewed(HttpServletRequest request) {
		long movieId = extractMandatoryInteger(request, "movieId");
		User user = userDao.find(sessionService.getLoggedInUserId());
		movieService.markMovieViewed(user, movieId);
	}

	@RequestMapping(value = "/future/add", method = RequestMethod.POST)
	@ResponseBody
	@Transactional(propagation = Propagation.REQUIRED)
	public Map<String, Object> addFutureMovie(@RequestParam("imdbId") String imdbId) {
		User user = userDao.find(sessionService.getLoggedInUserId());
		Pair<Movie, Boolean> futureMovieResult = movieService.addFutureMovieDownload(user, imdbId);
		if (futureMovieResult == null) {
			throw new MediaRSSException("Movie ID was not found in IMDB").doNotLog();
		}

		Map<String, Object> result = new HashMap<>();
		Movie movie = futureMovieResult.getKey();
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
				addUserTorrent(user, theTorrent, new MovieUserTorrent());
			} else {
				result.put("message", "Movie '" + movie.getName() + "' was scheduled for download when it will be available");
			}
		}

		// must be after movieUserTorrent creation
		result.put("user", createUserResponse(user, MOVIES_TAB));
		return result;
	}

	@RequestMapping(value = "/future/remove", method = RequestMethod.POST)
	@ResponseBody
	@Transactional(propagation = Propagation.REQUIRED)
	public Map<String, Object> removeFutureMovie(HttpServletRequest request) {
		long movieId = extractMandatoryInteger(request, "movieId");
		User user = userDao.find(sessionService.getLoggedInUserId());
		UserMovie userMovie = movieDao.findUserMovie(movieId, user);
		movieDao.delete(userMovie);

		Map<String, Object> result = new HashMap<>();
		result.put("message", "Movie '" + userMovie.getMovie().getName() + "' was removed from schedule for download");
		return result;
	}
}