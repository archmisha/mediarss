package rss.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import rss.ShowNotFoundException;
import rss.UserNotLoggedInException;
import rss.controllers.vo.EpisodeSearchResult;
import rss.dao.*;
import rss.entities.*;
import rss.services.*;
import rss.services.log.LogService;
import rss.services.shows.ShowService;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/shows")
public class ShowsController extends BaseController {

	@Autowired
	private UserDao userDao;

	@Autowired
	private SessionService sessionService;

	@Autowired
	private ShowDao showDao;

	@Autowired
	private ShowService showService;

	@Autowired
	private SubtitlesService subtitlesService;

	@Autowired
	private EpisodeDao episodeDao;

	@Autowired
	private TorrentDao torrentDao;

	@RequestMapping(value = "/addTracked/{showId}", method = RequestMethod.POST)
	@ResponseBody
	@Transactional(propagation = Propagation.REQUIRED)
	public void addTracked(@PathVariable long showId) {
		User user = userDao.find(sessionService.getLoggedInUserId());
		Show show = showDao.find(showId);
		user.getShows().add(show);
	}

	@RequestMapping(value = "/removeTracked/{showId}", method = RequestMethod.POST)
	@ResponseBody
	@Transactional(propagation = Propagation.REQUIRED)
	public void removeTracked(@PathVariable long showId) {
		User user = userDao.find(sessionService.getLoggedInUserId());
		Show show = showDao.find(showId);
		user.getShows().remove(show);
	}

	@RequestMapping(value = "/episode/download", method = RequestMethod.POST)
	@ResponseBody
	@Transactional(propagation = Propagation.REQUIRED)
	public void episodeDownload(HttpServletRequest request) {
		long torrentId = extractMandatoryInteger(request, "torrentId");
		User user = userDao.find(sessionService.getLoggedInUserId());
		Torrent torrent = torrentDao.find(torrentId);
		addUserTorrent(user, torrent, new EpisodeUserTorrent());
		if (user.getSubtitles() != null) {
			Episode episode = episodeDao.find(torrent);
			subtitlesService.downloadEpisodeSubtitles(torrent, episode, user.getSubtitles());
		}
	}

	@RequestMapping(value = "/search", method = RequestMethod.POST)
	@ResponseBody
	@Transactional(propagation = Propagation.REQUIRED)
	public EpisodeSearchResult search(HttpServletRequest request) {
		String title = extractString(request, "title", true);
		int season = extractInteger(request, "season", -1);
		int episode = extractInteger(request, "episode", -1);
		long showId = extractInteger(request, "showId", -1);
		User user = userDao.find(sessionService.getLoggedInUserId());
		try {
			Show show = showDao.find(showId);
			return showService.search(new EpisodeRequest(title, show, MediaQuality.HD720P, season, episode), user);
		} catch (ShowNotFoundException e) {
			return EpisodeSearchResult.createNoResults(title);
		}
	}

	/*@RequestMapping(value = "/addManual", method = RequestMethod.POST)
	@ResponseBody
	@Transactional(propagation = Propagation.REQUIRED)
	public Map<String, Object> addManual(HttpServletRequest request) {
		String showTvComUrl = extractString(request, "showTvComUrl", true);
		Show show = showService.downloadShowByUrl(showTvComUrl);
		Map<String, Object> result = new HashMap<>();
		if (show == null) {
			result.put("success", false);
			result.put("message", "Unable to find show (invalid url?)");
		} else if (show.isEnded()) {
			result.put("success", false);
			result.put("message", "Show '" + show.getName() + "' is already ended");
		} else {
			result.put("success", true);
			result.put("show", entityConverter.toThinShows(Collections.singleton(show)).get(0));
		}
		return result;
	}*/
}