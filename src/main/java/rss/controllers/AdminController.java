package rss.controllers;

import org.hibernate.NonUniqueResultException;
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
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

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
	public void autoCompleteShows(HttpServletRequest request, HttpServletResponse response) {
		User user = userDao.find(sessionService.getLoggedInUserId());
		verifyAdminPermissions(user);
		autoCompleteShowNames(request, response, true, null);
	}

	@RequestMapping(value = "/shows/delete/{showId}", method = RequestMethod.GET)
	@ResponseBody
	@Transactional(propagation = Propagation.REQUIRED)
	public String deleteShow(@PathVariable Long showId) {
		User user = userDao.find(sessionService.getLoggedInUserId());
		verifyAdminPermissions(user);

		Show show = showDao.find(showId);
		// allow deletion only if no one is tracking this show
		if (userDao.isShowBeingTracked(show)) {
			throw new MediaRSSException("Show is being tracked. Unable to delete").doNotLog();
		}

		for (Episode episode : show.getEpisodes()) {
			// episode is always connected to a single show - not a problem
			for (Long torrentId : episode.getTorrentIds()) {
				// delete the torrent only if it is connected to a single episode
				Torrent torrent = torrentDao.find(torrentId);
				try {
					episodeDao.find(torrentId);

					for (UserTorrent userTorrent : userTorrentDao.findUserEpisodeTorrentByTorrentId(torrentId)) {
						userTorrentDao.delete(userTorrent);
					}
					torrentDao.delete(torrent);
				} catch (NonUniqueResultException e) {
					// if exception was thrown - it means the torrent is connected to multiple episodes
				}
			}

			episode.getTorrentIds().clear();
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
		if (!movieDao.findUserMovies(movieId).isEmpty()) {
			throw new MediaRSSException("Movie is being tracked. Unable to delete").doNotLog();
		}

		for (Long torrentId : movie.getTorrentIds()) {
			Torrent torrent = torrentDao.find(torrentId);
			torrentDao.delete(torrent);
		}
		movie.getTorrentIds().clear();
		movieDao.delete(movie);

		return "Movie '" + movie.getName() + "' (id=" + movie.getId() + ") was deleted";
	}
}