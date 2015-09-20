package rss.shows;

import com.google.common.base.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import rss.ShowNotFoundException;
import rss.context.UserContextHolder;
import rss.dao.ShowDao;
import rss.dao.UserDao;
import rss.entities.User;
import rss.environment.Environment;
import rss.environment.ServerMode;
import rss.permissions.PermissionsService;
import rss.services.requests.episodes.FullSeasonRequest;
import rss.services.requests.episodes.FullShowRequest;
import rss.services.requests.episodes.ShowRequest;
import rss.services.requests.episodes.SingleEpisodeRequest;
import rss.services.shows.ShowQuery;
import rss.services.shows.ShowSearchService;
import rss.services.shows.UserActiveSearch;
import rss.shows.cache.UsersSearchesCache;
import rss.shows.entities.Show;
import rss.torrents.MediaQuality;
import rss.util.DurationMeter;
import rss.util.JsonTranslation;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.*;

@Path("/shows")
@Component
public class ShowsResource extends BaseController {

    @Autowired
    private UserDao userDao;

    @Autowired
    private UsersSearchesCache usersSearchesCache;

    @Autowired
    private PermissionsService permissionsService;

    @Autowired
    private ShowDao showDao;

    @Autowired
    protected UserCacheService userCacheService;

    @Autowired
    protected ShowSearchService showSearchService;

    @Path("/tracked-shows")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional(propagation = Propagation.REQUIRED)
    public Response getTrackedShows() {
        DurationMeter duration = new DurationMeter();
        User user = userCacheService.getUser(UserContextHolder.getCurrentUserContext().getUserId());
        List<ShowJSON> shows = userCacheService.getTrackedShows(user);

        Map<String, Object> result = new HashMap<>();
        result.put("trackedShows", shows);
        duration.stop();
        logService.info(getClass(), "Tracked shows " + duration.getDuration() + " ms");

        return Response.ok().entity(JsonTranslation.object2JsonString(result)).build();
    }

    @Path("/add-tracked/{showId}")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional(propagation = Propagation.REQUIRED)
    public Response addTracked(@PathParam("showId") final long showId) {
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

        return Response.ok().build();
    }

    @Path("/remove-tracked/{showId}")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional(propagation = Propagation.REQUIRED)
    public Response removeTracked(@PathParam("showId") final long showId) {
        User user = userDao.find(UserContextHolder.getCurrentUserContext().getUserId());
        Show show = showDao.find(showId);
        user.getShows().remove(show);

        // invalidate schedule to be regenerated next request
        userCacheService.invalidateUser(user);
        userCacheService.invalidateSchedule(user);
        userCacheService.invalidateTrackedShows(user);

        return Response.ok().build();
    }

    @Path("/tracked/autocomplete")
    @GET
    @Produces("text/javascript")
    @Transactional(propagation = Propagation.REQUIRED)
    public Response autoCompleteTracked(@QueryParam("term") String term) {
        User user = userCacheService.getUser(UserContextHolder.getCurrentUserContext().getUserId());
        final Set<Long> trackedShowsIds = new HashSet<>();
        for (ShowJSON show : userCacheService.getTrackedShows(user)) {
            trackedShowsIds.add(show.getId());
        }

        List<ShowAutoCompleteItem> result = showService.autoCompleteShowNames(term, false, new Predicate<ShowAutoCompleteItem>() {
            @Override
            public boolean apply(ShowAutoCompleteItem showAutoCompleteItem) {
                return !trackedShowsIds.contains(showAutoCompleteItem.getId());
            }
        });

        ShowAutoCompleteJSON showAutoCompleteJSON = new ShowAutoCompleteJSON();
        showAutoCompleteJSON.setTotal(result.size());
        showAutoCompleteJSON.setShows(result);
        return Response.ok().entity(JsonTranslation.object2JsonString(showAutoCompleteJSON)).build();
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

    @Path("/search")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional(propagation = Propagation.REQUIRED)
    public Response search(@QueryParam("title") String title,
                           @QueryParam("season") Integer season,
                           @QueryParam("episode") Integer episode,
                           @QueryParam("showId") Long showId,
                           @QueryParam("forceDownload") boolean forceDownload) {
        season = applyDefaultValue(season, -1);
        episode = applyDefaultValue(episode, -1);
        showId = applyDefaultValue(showId, -1l);
        Long userId = UserContextHolder.getCurrentUserContext().getUserId();

        // only admin is allowed to use forceDownload option
        if (forceDownload) {
            permissionsService.verifyAdminPermissions();
        }

        SearchResultJSON searchResultJSON;
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
            searchResultJSON = showSearchService.search(showRequest, userId, forceDownload);
        } catch (ShowNotFoundException e) {
            searchResultJSON = SearchResultJSON.createNoResults(title);
        }

        return Response.ok().entity(JsonTranslation.object2JsonString(searchResultJSON)).build();
    }

    @Path("/schedule")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional(propagation = Propagation.REQUIRED)
    public Response getSchedule() {
        DurationMeter duration = new DurationMeter();
        User user = userCacheService.getUser(UserContextHolder.getCurrentUserContext().getUserId());
        ShowsScheduleJSON schedule = userCacheService.getSchedule(user);
        duration.stop();
        logService.info(getClass(), "Schedule " + duration.getDuration() + " ms");
        return Response.ok().entity(JsonTranslation.object2JsonString(schedule)).build();
    }

    @RequestMapping(value = "/search/status", method = RequestMethod.GET)
    @ResponseBody
    @Transactional(propagation = Propagation.REQUIRED)
    public Map<String, Object> getSearchStatus() {
        Map<String, Object> result = new HashMap<>();
        List<UserActiveSearch> searches = usersSearchesCache.getSearches();
        List<SearchResultJSON> searchResults = new ArrayList<>();
        for (UserActiveSearch search : searches) {
            showSearchService.downloadResultToSearchResultVO(UserContextHolder.getCurrentUserContext().getUserId(),
                    search.getDownloadResult(), search.getSearchResultJSON());
            searchResults.add(search.getSearchResultJSON());
        }
        result.put("activeSearches", searchResults);
        return result;
    }

    @RequestMapping(value = "/search/remove/{searchId}", method = RequestMethod.GET)
    @ResponseBody
    @Transactional(propagation = Propagation.REQUIRED)
    public Response removeActiveSearch(@PathVariable String searchId) {
        usersSearchesCache.removeSearch(searchId);
        return Response.ok().build();
    }

    @Path("/get")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Transactional(propagation = Propagation.REQUIRED)
    public Response getShows(String json) {
        if (Environment.getInstance().getServerMode() != ServerMode.TEST) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        ShowQuery showQuery = JsonTranslation.jsonString2Object(json, ShowQuery.class);

        List<Show> shows = new ArrayList<>();
        if (showQuery.getName() != null) {
            Show show = showDao.findByName(showQuery.getName());
            if (show != null) {
                shows.add(show);
            }
        }

        final List<ShowJSON> thinShows = entityConverter.toThinShows(shows);
        return Response.ok().entity(JsonTranslation.object2JsonString(thinShows.toArray(new ShowJSON[thinShows.size()]))).build();
    }
}