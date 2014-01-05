package rss.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import rss.MediaRSSException;
import rss.controllers.vo.UserVO;
import rss.dao.*;
import rss.entities.*;
import rss.services.EmailService;
import rss.services.SessionService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * User: dikmanm
 * Date: 10/02/13 18:16
 */
@Controller
@RequestMapping("/admin")
public class AdminController extends BaseController {

	@Autowired
	private SessionService sessionService;

	@Autowired
	private UserDao userDao;

	@Autowired
	private EmailService emailService;

	@Autowired
	private EntityConverter entityConverter;

	@Autowired
	private ShowDao showDao;

	@Autowired
	private EpisodeDao episodeDao;

	@Autowired
	private TorrentDao torrentDao;

	@Autowired
	private MovieDao movieDao;

	@Autowired
	private SubtitlesDao subtitlesDao;

	@RequestMapping(value = "/notification", method = RequestMethod.POST)
	@ResponseBody
	public void sendNotification(HttpServletRequest request) {
		User user = userDao.find(sessionService.getLoggedInUserId());
		verifyAdminPermissions(user);

		String text = extractString(request, "text", true);
		emailService.sendEmailToAllUsers(text);
	}

	@RequestMapping(value = "/users", method = RequestMethod.GET)
	@ResponseBody
	public Collection<UserVO> getAllUsers() {
		User user = userDao.find(sessionService.getLoggedInUserId());
		verifyAdminPermissions(user);

		List<UserVO> users = entityConverter.toThinUser(userDao.findAll());
		Collections.sort(users, new Comparator<UserVO>() {
			@Override
			public int compare(UserVO o1, UserVO o2) {
				if (o2.getLastLogin() == null) {
					return -1;
				} else if (o1.getLastLogin() == null) {
					return 1;
				}
				return o2.getLastLogin().compareTo(o1.getLastLogin());
			}
		});
		return users;
	}

	@RequestMapping(value = "/downloadSchedule/{showId}", method = RequestMethod.GET)
	@ResponseBody
	@Transactional(propagation = Propagation.REQUIRED)
	public String downloadSchedule(@PathVariable Long showId) {
		User user = userDao.find(sessionService.getLoggedInUserId());
		verifyAdminPermissions(user);

		Show show = showDao.find(showId);
		showService.downloadFullScheduleWithTorrents(show, false);

		return "Downloaded schedule for '" + show.getName() + "'";
	}

	@RequestMapping(value = "/shows/autocomplete", method = RequestMethod.GET)
	@ResponseBody
	public Map<String, Object> autoCompleteShows(HttpServletRequest request, HttpServletResponse response) {
		User user = userDao.find(sessionService.getLoggedInUserId());
		verifyAdminPermissions(user);
		return autoCompleteShowNames(request, response, true, null);
	}

	@RequestMapping(value = "/shows/delete/{showId}", method = RequestMethod.GET)
	@ResponseBody
	@Transactional(propagation = Propagation.REQUIRED)
	public String deleteShow(@PathVariable Long showId) {
		User user = userDao.find(sessionService.getLoggedInUserId());
		verifyAdminPermissions(user);

		Show show = showDao.find(showId);
		if (show == null) {
			return "Show with id " + show + " is not found";
		}

		// allow deletion only if no one is tracking this show
		if (userDao.isShowBeingTracked(show)) {
			throw new MediaRSSException("Show is being tracked. Unable to delete").doNotLog();
		}

		for (Episode episode : show.getEpisodes()) {
			showService.disconnectTorrentsFromEpisode(episode);
			episodeDao.delete(episode);
		}

		showDao.delete(show);

		return "Show '" + show.getName() + "' (id=" + show.getId() + ") was deleted";
	}

	@RequestMapping(value = "/movies/delete/{movieId}", method = RequestMethod.GET)
	@ResponseBody
	@Transactional(propagation = Propagation.REQUIRED)
	public String deleteMovie(@PathVariable Long movieId) {
		User user = userDao.find(sessionService.getLoggedInUserId());
		verifyAdminPermissions(user);

		Movie movie = movieDao.find(movieId);
		if (movie == null) {
			return "Movie with id " + movie + " is not found";
		}

		// allow deletion only if no one is tracking this movie
		if (!movieDao.findUserMoviesByMovieId(movieId).isEmpty()) {
			throw new MediaRSSException("Movie is being tracked. Unable to delete").doNotLog();
		}

		for (Long torrentId : movie.getTorrentIds()) {
			Torrent torrent = torrentDao.find(torrentId);
			// happens when erasing a movie that has a commons torrent with other movie that also been erased
			if (torrent != null) {
				for (Subtitles subtitles : subtitlesDao.findByTorrent(torrent)) {
					subtitlesDao.delete(subtitles);
				}
				torrentDao.delete(torrent);
			}
		}
		movie.getTorrentIds().clear();
		movieDao.delete(movie);

		return "Movie '" + movie.getName() + "' (id=" + movie.getId() + ") was deleted";
	}

	@RequestMapping(value = "/impersonate/{userId}", method = RequestMethod.GET)
	@ResponseBody
	@Transactional(propagation = Propagation.REQUIRED)
	public void impersonate(@PathVariable Long userId) {
		User user = userDao.find(sessionService.getLoggedInUserId());
		verifyAdminPermissions(user);

		sessionService.impersonate(userId);
	}
}