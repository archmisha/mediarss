package rss.controllers;

import com.google.common.base.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import rss.ShowNotFoundException;
import rss.context.UserContextHolder;
import rss.controllers.vo.SearchResultVO;
import rss.controllers.vo.ShowVO;
import rss.controllers.vo.ShowsScheduleVO;
import rss.dao.ShowDao;
import rss.dao.UserDao;
import rss.entities.MediaQuality;
import rss.entities.Show;
import rss.entities.User;
import rss.permissions.PermissionsService;
import rss.services.SessionService;
import rss.services.requests.episodes.FullSeasonRequest;
import rss.services.requests.episodes.FullShowRequest;
import rss.services.requests.episodes.ShowRequest;
import rss.services.requests.episodes.SingleEpisodeRequest;
import rss.services.shows.ShowAutoCompleteItem;
import rss.services.shows.ShowSearchService;
import rss.services.shows.UserActiveSearch;
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
    private PermissionsService permissionsService;

	@Autowired
	private ShowDao showDao;

	@Autowired
	protected ShowSearchService showSearchService;

	@RequestMapping(value = "/add-tracked/{showId}", method = RequestMethod.POST)
	@ResponseBody
	@Transactional(propagation = Propagation.REQUIRED)
	public Map<String, Object> addTracked(@PathVariable final long showId) {
        User user = userDao.find(UserContextHolder.getCurrentUserContext().getUserId());
        final Show show = showDao.find(showId);
		// if show was not being tracked before (becoming tracked now) - download its schedule
		boolean downloadSchedule = !userDao.isShowBeingTracked(show);
		user.getShows().add(show);

		// return the request asap to the user
		if (downloadSchedule) {
			showService.downloadFullScheduleWithTorrents(show, true);
		}

		// invalidate schedule to be regenerated next request
		userCacheService.invalidateUser(user);
		userCacheService.invalidateSchedule(user);
		userCacheService.invalidateTrackedShows(user);

		Map<String, Object> result = new HashMap<>();
		result.put("success", true);
		return result;
	}


	@RequestMapping(value = "/remove-tracked/{showId}", method = RequestMethod.POST)
	@ResponseBody
	@Transactional(propagation = Propagation.REQUIRED)
	public Map<String, Object> removeTracked(@PathVariable long showId) {
        User user = userDao.find(UserContextHolder.getCurrentUserContext().getUserId());
        Show show = showDao.find(showId);
		user.getShows().remove(show);

		// invalidate schedule to be regenerated next request
		userCacheService.invalidateUser(user);
		userCacheService.invalidateSchedule(user);
		userCacheService.invalidateTrackedShows(user);

		Map<String, Object> result = new HashMap<>();
		result.put("success", true);
		return result;
	}

	@RequestMapping(value = "/tracked/autocomplete", method = RequestMethod.GET)
	@ResponseBody
	@Transactional(propagation = Propagation.REQUIRED)
	public Map<String, Object> autoCompleteTracked(HttpServletRequest request, HttpServletResponse response) {
        User user = userCacheService.getUser(UserContextHolder.getCurrentUserContext().getUserId());
        final Set<Long> trackedShowsIds = new HashSet<>();
		for (ShowVO show : userCacheService.getTrackedShows(user)) {
			trackedShowsIds.add(show.getId());
		}

		return autoCompleteShowNames(request, response, false, new Predicate<ShowAutoCompleteItem>() {
			@Override
			public boolean apply(ShowAutoCompleteItem showAutoCompleteItem) {
				return !trackedShowsIds.contains(showAutoCompleteItem.getId());
			}
		});
	}

	@RequestMapping(value = "/episode/download", method = RequestMethod.POST)
	@ResponseBody
	@Transactional(propagation = Propagation.REQUIRED)
	public void episodeDownload(@RequestParam("torrentId") long torrentId) {
        User user = userCacheService.getUser(UserContextHolder.getCurrentUserContext().getUserId());
        showService.downloadEpisode(user, torrentId);
	}

	@RequestMapping(value = "/episode/download-all", method = RequestMethod.POST)
	@ResponseBody
	@Transactional(propagation = Propagation.REQUIRED)
	public void episodeDownloadAll(@RequestParam("torrentIds[]") long[] torrentIds) {
        User user = userCacheService.getUser(UserContextHolder.getCurrentUserContext().getUserId());
        for (long torrentId : torrentIds) {
			showService.downloadEpisode(user, torrentId);
		}
	}

	@RequestMapping(value = "/search", method = RequestMethod.POST)
	@ResponseBody
	@Transactional(propagation = Propagation.REQUIRED)
	public SearchResultVO search(@RequestParam("title") String title,
								 @RequestParam(value = "season", required = false) Integer season,
								 @RequestParam(value = "episode", required = false) Integer episode,
								 @RequestParam(value = "showId", required = false) Long showId,
								 @RequestParam(value = "forceDownload", required = false) boolean forceDownload) {
		season = applyDefaultValue(season, -1);
		episode = applyDefaultValue(episode, -1);
		showId = applyDefaultValue(showId, -1l);
        Long userId = UserContextHolder.getCurrentUserContext().getUserId();

		// only admin is allowed to use forceDownload option
		if (forceDownload) {
            permissionsService.verifyAdminPermissions();
        }

		try {
			Show show = showDao.find(showId);
			ShowRequest showRequest;
			if (season == -1) {
				showRequest = new FullShowRequest(userId, title, show, MediaQuality.HD720P);
			} else if (episode == -1) {
				showRequest = new FullSeasonRequest(userId, title, show, MediaQuality.HD720P, season);
			} else {
				showRequest = new SingleEpisodeRequest(userId, title, show, MediaQuality.HD720P, season, episode);
			}
            return showSearchService.search(showRequest, UserContextHolder.getCurrentUserContext().getUserId(), forceDownload);
        } catch (ShowNotFoundException e) {
			return SearchResultVO.createNoResults(title);
		}
	}

	@RequestMapping(value = "/tracked-shows", method = RequestMethod.GET)
	@ResponseBody
	@Transactional(propagation = Propagation.REQUIRED)
	public Map<String, Object> getTrackedShows() {
		DurationMeter duration = new DurationMeter();
        User user = userCacheService.getUser(UserContextHolder.getCurrentUserContext().getUserId());
        List<ShowVO> shows = userCacheService.getTrackedShows(user);

		Map<String, Object> result = new HashMap<>();
		result.put("trackedShows", shows);
		duration.stop();
		logService.info(getClass(), "Tracked shows " + duration.getDuration() + " ms");

		return result;
	}

	@RequestMapping(value = "/schedule", method = RequestMethod.GET)
	@ResponseBody
	@Transactional(propagation = Propagation.REQUIRED)
	public Map<String, Object> getSchedule() {
		DurationMeter duration = new DurationMeter();
        User user = userCacheService.getUser(UserContextHolder.getCurrentUserContext().getUserId());
        ShowsScheduleVO schedule = userCacheService.getSchedule(user);

		Map<String, Object> result = new HashMap<>();
		result.put("schedule", schedule);
		duration.stop();
		logService.info(getClass(), "Schedule " + duration.getDuration() + " ms");

		return result;
	}

	@RequestMapping(value = "/search/status", method = RequestMethod.GET)
	@ResponseBody
	@Transactional(propagation = Propagation.REQUIRED)
	public Map<String, Object> getSearchStatus() {
		Map<String, Object> result = new HashMap<>();
		List<UserActiveSearch> searches = sessionService.getUsersSearchesCache().getSearches();
		List<SearchResultVO> searchResults = new ArrayList<>();
		for (UserActiveSearch search : searches) {
            showSearchService.downloadResultToSearchResultVO(UserContextHolder.getCurrentUserContext().getUserId(),
                    search.getDownloadResult(), search.getSearchResultVO());
			searchResults.add(search.getSearchResultVO());
		}
		result.put("activeSearches", searchResults);
		return result;
	}

	@RequestMapping(value = "/search/remove/{searchId}", method = RequestMethod.GET)
	@ResponseBody
	@Transactional(propagation = Propagation.REQUIRED)
	public Map<String, Object> removeActiveSearch(@PathVariable String searchId) {
		sessionService.getUsersSearchesCache().removeSearch(searchId);

		Map<String, Object> result = new HashMap<>();
		result.put("status", "success");
		return result;
	}
}