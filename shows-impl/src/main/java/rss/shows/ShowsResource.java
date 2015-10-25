package rss.shows;

import com.google.common.base.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import rss.MediaRSSException;
import rss.cache.UserActiveSearch;
import rss.cache.UserCacheService;
import rss.cache.UsersSearchesCache;
import rss.environment.Environment;
import rss.environment.ServerMode;
import rss.log.LogService;
import rss.permissions.PermissionsService;
import rss.services.shows.ShowQuery;
import rss.shows.dao.ShowDao;
import rss.torrents.Episode;
import rss.torrents.MediaQuality;
import rss.torrents.Show;
import rss.torrents.requests.shows.FullSeasonRequest;
import rss.torrents.requests.shows.FullShowRequest;
import rss.torrents.requests.shows.ShowRequest;
import rss.torrents.requests.shows.SingleEpisodeRequest;
import rss.user.User;
import rss.user.UserService;
import rss.user.context.UserContextHolder;
import rss.util.DurationMeter;
import rss.util.JsonTranslation;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.*;

@Path("/shows")
@Component
public class ShowsResource {

    @Autowired
    protected UserCacheService userCacheService;
    @Autowired
    protected ShowSearchService showSearchService;
    @Autowired
    private LogService logService;
    @Autowired
    private UserService userService;
    @Autowired
    private UsersSearchesCache usersSearchesCache;
    @Autowired
    private PermissionsService permissionsService;
    @Autowired
    private ShowService showService;
    @Autowired
    private ShowDao showDao;

    public static List<ShowJSON> showListToJson(Collection<Show> shows) {
        ArrayList<ShowJSON> result = new ArrayList<>();
        for (Show show : shows) {
            result.add(new ShowJSON().withId(show.getId()).withName(show.getName()).withEnded(show.isEnded()).withTvRageId(show.getTvRageId()));
        }
        Collections.sort(result, new Comparator<ShowJSON>() {
            @Override
            public int compare(ShowJSON o1, ShowJSON o2) {
                return o1.getName().compareToIgnoreCase(o2.getName());
            }
        });
        return result;
    }

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
        User user = userService.find(UserContextHolder.getCurrentUserContext().getUserId());
        final Show show = showDao.find(showId);
        // if show was not being tracked before (becoming tracked now) - download its schedule
        boolean downloadSchedule = !showDao.isShowBeingTracked(show);
        show.getUsers().add(user);

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
        User user = userService.find(UserContextHolder.getCurrentUserContext().getUserId());
        Show show = showDao.find(showId);
        show.getUsers().remove(user);

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

    @Path("/episode/download")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional(propagation = Propagation.REQUIRED)
    public Response episodeDownload(@QueryParam("torrentId") long torrentId) {
        User user = userCacheService.getUser(UserContextHolder.getCurrentUserContext().getUserId());
        showService.downloadEpisode(user, torrentId);
        return Response.ok().build();
    }

    @Path("/episode/download-all")
    @GET
    @Transactional(propagation = Propagation.REQUIRED)
    public Response episodeDownloadAll(@QueryParam("torrentIds") String torrentIds) {
        User user = userCacheService.getUser(UserContextHolder.getCurrentUserContext().getUserId());
        for (String torrentId : torrentIds.split(",")) {
            showService.downloadEpisode(user, Long.parseLong(torrentId));
        }
        return Response.ok().build();
    }

    @Path("/search")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional(propagation = Propagation.REQUIRED)
    public Response search(@QueryParam("title") String title,
                           @QueryParam("season") Integer season,
                           @QueryParam("episode") Integer episode,
                           @QueryParam("showId") Long showId,
                           @QueryParam("forceDownload") boolean forceDownload) {
        season = applyDefaultValue(season, -1);
        episode = applyDefaultValue(episode, -1);
        showId = applyDefaultValue(showId, -1L);
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

    @Path("/search/status")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional(propagation = Propagation.REQUIRED)
    public Response getSearchStatus() {
        List<UserActiveSearch> searches = usersSearchesCache.getSearches();
        List<SearchResultJSON> searchResults = new ArrayList<>();
        for (UserActiveSearch search : searches) {
            showSearchService.downloadResultToSearchResultVO(UserContextHolder.getCurrentUserContext().getUserId(),
                    search.getDownloadResult(), search.getSearchResultJSON());
            searchResults.add(search.getSearchResultJSON());
        }
        return Response.ok().entity(JsonTranslation.object2JsonString(searchResults)).build();
    }

    @Path("/search/remove/{searchId}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional(propagation = Propagation.REQUIRED)
    public Response removeActiveSearch(@PathParam("searchId") String searchId) {
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

        final List<ShowJSON> thinShows = showListToJson(shows);
        return Response.ok().entity(JsonTranslation.object2JsonString(thinShows.toArray(new ShowJSON[thinShows.size()]))).build();
    }

    @RequestMapping(value = "/downloadSchedule/{showId}", method = RequestMethod.GET)
    @ResponseBody
    @Transactional(propagation = Propagation.REQUIRED)
    public Response downloadSchedule(@PathVariable Long showId) {
        permissionsService.verifyAdminPermissions();

        Show show = showService.find(showId);
        showService.downloadFullScheduleWithTorrents(show, false);

        Map<String, Object> map = new HashMap<>();
        map.put("message", "Downloaded schedule for '" + show.getName() + "'");
        return Response.ok().entity(JsonTranslation.object2JsonString(map)).build();
    }

    @Path("/autocomplete")
    @GET
    @Produces("text/javascript")
    public Response autoCompleteShows(@QueryParam("term") String term) {
        permissionsService.verifyAdminPermissions();

        List<ShowAutoCompleteItem> result = showService.autoCompleteShowNames(term, true, null);

        ShowAutoCompleteJSON showAutoCompleteJSON = new ShowAutoCompleteJSON();
        showAutoCompleteJSON.setTotal(result.size());
        showAutoCompleteJSON.setShows(result);
        return Response.ok().entity(JsonTranslation.object2JsonString(showAutoCompleteJSON)).build();
    }

    @RequestMapping(value = "/delete/{showId}", method = RequestMethod.GET)
    @ResponseBody
    @Transactional(propagation = Propagation.REQUIRED)
    public Response deleteShow(@PathVariable Long showId) {
        permissionsService.verifyAdminPermissions();

        Show show = showService.find(showId);
        if (show == null) {
            Map<String, Object> map = new HashMap<>();
            map.put("message", "Show with id " + showId + " is not found");
            return Response.ok().entity(JsonTranslation.object2JsonString(map)).build();
        }

        // allow deletion only if no one is tracking this show
        if (showService.isShowBeingTracked(show)) {
            throw new MediaRSSException("Show is being tracked. Unable to delete").doNotLog();
        }

        for (Episode episode : show.getEpisodes()) {
            showService.disconnectTorrentsFromEpisode(episode);
            showService.delete(episode);
        }

        showService.delete(show);

        Map<String, Object> map = new HashMap<>();
        map.put("message", "Show '" + show.getName() + "' (id=" + show.getId() + ") was deleted");
        return Response.ok().entity(JsonTranslation.object2JsonString(map)).build();
    }

    protected <T> T applyDefaultValue(T value, T defaultValue) {
        return value == null ? defaultValue : value;
    }
}