package rss.controllers;

import com.google.common.base.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import rss.ShowNotFoundException;
import rss.controllers.vo.EpisodeSearchResult;
import rss.controllers.vo.ShowVO;
import rss.dao.ShowDao;
import rss.dao.UserDao;
import rss.entities.*;
import rss.services.SessionService;
import rss.services.requests.episodes.FullSeasonRequest;
import rss.services.requests.episodes.FullShowRequest;
import rss.services.requests.episodes.ShowRequest;
import rss.services.requests.episodes.SingleEpisodeRequest;
import rss.services.shows.AutoCompleteItem;
import rss.services.shows.ShowSearchService;
import rss.util.DurationMeter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

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

	@RequestMapping(value = "/remove-tracked/{showId}", method = RequestMethod.POST)
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
		showService.downloadEpisode(user, torrentId);
	}

	@RequestMapping(value = "/episode/download-all", method = RequestMethod.POST)
	@ResponseBody
	@Transactional(propagation = Propagation.REQUIRED)
	public void episodeDownloadAll(@RequestParam("torrentIds[]") long[] torrentIds) {
		User user = userDao.find(sessionService.getLoggedInUserId());
		for (long torrentId : torrentIds) {
			showService.downloadEpisode(user, torrentId);
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

	@RequestMapping(value = "/tracked-shows", method = RequestMethod.GET)
	@ResponseBody
	@Transactional(propagation = Propagation.REQUIRED)
	public Map<String, Object> getTrackedShows() {
		User user = userDao.find(sessionService.getLoggedInUserId());

		List<ShowVO> shows = entityConverter.toThinShows(user.getShows());
		Collections.sort(shows, new Comparator<ShowVO>() {
			@Override
			public int compare(ShowVO o1, ShowVO o2) {
				return o1.getName().compareToIgnoreCase(o2.getName());
			}
		});

		DurationMeter duration = new DurationMeter();
		Map<String, Object> result = new HashMap<>();
		result.put("trackedShows", shows);
		result.put("isAdmin", isAdmin(user));
		duration.stop();
		logService.info(getClass(), "Tracked shows " + duration.getDuration() + " millis");

		return result;
	}

	@RequestMapping(value = "/schedule", method = RequestMethod.GET)
	@ResponseBody
	@Transactional(propagation = Propagation.REQUIRED)
	public Map<String, Object> getSchedule() {
		User user = userDao.find(sessionService.getLoggedInUserId());

		DurationMeter duration = new DurationMeter();
		Map<String, Object> result = new HashMap<>();
		result.put("schedule", showService.getSchedule(user.getShows()));
		result.put("isAdmin", isAdmin(user));
		duration.stop();
		logService.info(getClass(), "Schedule " + duration.getDuration() + " millis");

		return result;
	}
}