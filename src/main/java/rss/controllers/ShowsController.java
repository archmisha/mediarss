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
import rss.services.SessionService;
import rss.services.requests.FullSeasonRequest;
import rss.services.requests.FullShowRequest;
import rss.services.requests.ShowRequest;
import rss.services.requests.SingleEpisodeRequest;
import rss.services.shows.AutoCompleteItem;
import rss.services.shows.ShowSearchService;
import rss.services.subtitles.SubtitlesService;
import rss.util.DurationMeter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
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

	@Autowired
	protected ShowSearchService showSearchService;

	@RequestMapping(value = "/addTracked/{showId}", method = RequestMethod.POST)
	@ResponseBody
	@Transactional(propagation = Propagation.REQUIRED)
	public Map<String, Object> addTracked(@PathVariable final long showId) {
		User user = userDao.find(sessionService.getLoggedInUserId());
		final Show show = showDao.find(showId);
		// if show was not being tracked before (becoming tracked now) - download its schedule
		boolean downloadSchedule = !userDao.isShowBeingTracked(show);
		user.getShows().add(show);

		// return the request asap to the user
		if (downloadSchedule) {
			showService.downloadFullScheduleWithTorrents(show, true);
		}

		Map<String, Object> result = new HashMap<>();
		result.put("schedule", showService.getSchedule(user.getShows()));
		return result;
	}

	@RequestMapping(value = "/removeTracked/{showId}", method = RequestMethod.POST)
	@ResponseBody
	@Transactional(propagation = Propagation.REQUIRED)
	public Map<String, Object> removeTracked(@PathVariable long showId) {
		User user = userDao.find(sessionService.getLoggedInUserId());
		Show show = showDao.find(showId);
		user.getShows().remove(show);

		Map<String, Object> result = new HashMap<>();
		result.put("schedule", showService.getSchedule(user.getShows()));
		return result;
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

	@RequestMapping(value = "/episode/downloadAll", method = RequestMethod.POST)
	@ResponseBody
	@Transactional(propagation = Propagation.REQUIRED)
	public void episodeDownloadAll(@RequestParam("torrentIds[]") long[] torrentIds) {
		User user = userDao.find(sessionService.getLoggedInUserId());
		for (long torrentId : torrentIds) {
			downloadEpisode(torrentId, user);
		}
	}

	private void downloadEpisode(long torrentId, User user) {
		Torrent torrent = torrentDao.find(torrentId);
		addUserTorrent(user, torrent, new UserEpisodeTorrent());
		if (user.getSubtitles() != null) {
			Episode episode = episodeDao.find(torrent);
			subtitlesService.downloadEpisodeSubtitles(torrent, episode, user.getSubtitles());
		}
	}

	@RequestMapping(value = "/search", method = RequestMethod.POST)
	@ResponseBody
	@Transactional(propagation = Propagation.REQUIRED)
	public EpisodeSearchResult search(@RequestParam("title") String title,
									  @RequestParam(value = "season", required = false) Integer season,
									  @RequestParam(value = "episode", required = false) Integer episode,
									  @RequestParam(value = "showId", required = false) Long showId,
									  @RequestParam(value = "forceDownload", required = false) boolean forceDownload) {
		season = applyDefaultValue(season, -1);
		episode = applyDefaultValue(episode, -1);
		showId = applyDefaultValue(showId, -1l);
		User user = userDao.find(sessionService.getLoggedInUserId());

		// only admin is allowed to use forceDownload option
		if (forceDownload) {
			verifyAdminPermissions(user);
		}

		try {
			Show show = showDao.find(showId);
			ShowRequest episodeRequest;
			if (season == -1) {
				episodeRequest = new FullShowRequest(title, show, MediaQuality.HD720P);
			} else if (episode == -1) {
				episodeRequest = new FullSeasonRequest(title, show, MediaQuality.HD720P, season);
			} else {
				episodeRequest = new SingleEpisodeRequest(title, show, MediaQuality.HD720P, season, episode);
			}
			return showSearchService.search(episodeRequest, user, forceDownload);
		} catch (ShowNotFoundException e) {
			return EpisodeSearchResult.createNoResults(title);
		}
	}

	@RequestMapping(value = "/initial-data", method = RequestMethod.GET)
	@ResponseBody
	@Transactional(propagation = Propagation.REQUIRED)
	public Map<String, Object> initialData() {
		User user = userDao.find(sessionService.getLoggedInUserId());

		DurationMeter duration = new DurationMeter();
		Map<String, Object> result = new HashMap<>();
		result.put("trackedShows", sort(entityConverter.toThinShows(user.getShows())));
		result.put("schedule", showService.getSchedule(user.getShows()));
		duration.stop();
		logService.info(getClass(), "initialData " + duration.getDuration() + " millis");

		return result;
	}
}