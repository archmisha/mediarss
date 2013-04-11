package rss.controllers;

import com.google.common.base.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import rss.ShowNotFoundException;
import rss.controllers.vo.EpisodeSearchResult;
import rss.dao.EpisodeDao;
import rss.dao.ShowDao;
import rss.dao.TorrentDao;
import rss.dao.UserDao;
import rss.entities.*;
import rss.services.EpisodeRequest;
import rss.services.SessionService;
import rss.services.SubtitlesService;
import rss.services.shows.AutoCompleteItem;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashSet;
import java.util.Set;

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

	@RequestMapping(value = "/tracked/autocomplete", method = RequestMethod.GET)
	@ResponseBody
	@Transactional(propagation = Propagation.REQUIRED)
	public void autoCompleteTracked(HttpServletRequest request, HttpServletResponse response) {
		User user = userDao.find(sessionService.getLoggedInUserId());
		final Set<Long> trackedShowsIds = new HashSet<>();
		for (Show show : user.getShows()) {
			trackedShowsIds.add(show.getId());
		}

		autoCompleteShowNames(request, response, false, new Predicate<AutoCompleteItem>() {
			@Override
			public boolean apply(rss.services.shows.AutoCompleteItem autoCompleteItem) {
				return !trackedShowsIds.contains(autoCompleteItem.getId());
			}
		});
	}

	@RequestMapping(value = "/episode/download", method = RequestMethod.POST)
	@ResponseBody
	@Transactional(propagation = Propagation.REQUIRED)
	public void episodeDownload(@RequestParam("torrentId") long torrentId) {
		User user = userDao.find(sessionService.getLoggedInUserId());
		downloadEpisode(torrentId, user);
	}

	private void downloadEpisode(long torrentId, User user) {
		Torrent torrent = torrentDao.find(torrentId);
		addUserTorrent(user, torrent, new EpisodeUserTorrent());
		if (user.getSubtitles() != null) {
			Episode episode = episodeDao.find(torrent);
			subtitlesService.downloadEpisodeSubtitles(torrent, episode, user.getSubtitles());
		}
	}

	@RequestMapping(value = "/episode/downloadAll", method = RequestMethod.POST)
	@ResponseBody
	@Transactional(propagation = Propagation.REQUIRED)
	public void episodeDownloadAll(@RequestParam("torrentIds[]") long[] torrentIds) {
		User user = userDao.find(sessionService.getLoggedInUserId());
		for (long torrentId : torrentIds) {
			downloadEpisode(torrentId, user);
		}
	}

	@RequestMapping(value = "/search", method = RequestMethod.POST)
	@ResponseBody
	@Transactional(propagation = Propagation.REQUIRED)
	public EpisodeSearchResult search(@RequestParam("title") String title,
									  @RequestParam(value = "season", required = false) Integer season,
									  @RequestParam(value = "episode", required = false) Integer episode,
									  @RequestParam(value = "showId", required = false) Long showId) {
		season = applyDefaultValue(season, -1);
		episode = applyDefaultValue(episode, -1);
		showId = applyDefaultValue(showId, -1l);
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