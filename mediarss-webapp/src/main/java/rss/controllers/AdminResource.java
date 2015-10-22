package rss.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import rss.MediaRSSException;
import rss.cache.UserCacheService;
import rss.context.UserContextHolder;
import rss.entities.News;
import rss.movies.MovieService;
import rss.permissions.PermissionsService;
import rss.services.NewsService;
import rss.shows.ShowAutoCompleteItem;
import rss.shows.ShowAutoCompleteJSON;
import rss.shows.ShowService;
import rss.subtitles.SubtitlesService;
import rss.torrents.Episode;
import rss.torrents.Movie;
import rss.torrents.Show;
import rss.torrents.Torrent;
import rss.torrents.dao.TorrentDao;
import rss.torrents.searchers.config.SearcherConfiguration;
import rss.torrents.searchers.config.SearcherConfigurationService;
import rss.user.User;
import rss.user.UserService;
import rss.util.JsonTranslation;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User: dikmanm
 * Date: 10/02/13 18:16
 */
@Path("/admin")
@Component
public class AdminResource {

    @Autowired
    private PermissionsService permissionsService;

    @Autowired
    private EntityConverter entityConverter;

    @Autowired
    private TorrentDao torrentDao;

    @Autowired
    private MovieService movieService;

    @Autowired
    private SubtitlesService subtitlesService;

    @Autowired
    private NewsService newsService;

    @Autowired
    private UserService userService;

    @Autowired
    private SearcherConfigurationService searcherConfigurationService;

    @Autowired
    private UserCacheService userCacheService;

    @Autowired
    private ShowService showService;

    @RequestMapping(value = "/notification", method = RequestMethod.POST)
    @ResponseBody
    public Response sendNotification(@QueryParam("text") String text) {
        permissionsService.verifyAdminPermissions();

        userService.sendEmailToAllUsers(text);

        return Response.ok().build();
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

    @Path("/shows/autocomplete")
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

    @RequestMapping(value = "/shows/delete/{showId}", method = RequestMethod.GET)
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

    @RequestMapping(value = "/movies/delete/{movieId}", method = RequestMethod.GET)
    @ResponseBody
    @Transactional(propagation = Propagation.REQUIRED)
    public Response deleteMovie(@PathVariable Long movieId) {
        permissionsService.verifyAdminPermissions();

        Movie movie = movieService.find(movieId);
        if (movie == null) {
            Map<String, Object> map = new HashMap<>();
            map.put("message", "Movie with id " + movieId + " is not found");
            return Response.ok().entity(JsonTranslation.object2JsonString(map)).build();
        }

        // allow deletion only if no one is tracking this movie
        if (!movieService.findUserMoviesByMovieId(movieId).isEmpty()) {
            throw new MediaRSSException("Movie is being tracked. Unable to delete").doNotLog();
        }

        for (Long torrentId : movie.getTorrentIds()) {
            Torrent torrent = torrentDao.find(torrentId);
            // happens when erasing a movie that has a commons torrent with other movie that also been erased
            if (torrent != null) {
                subtitlesService.deleteSubtitlesByTorrent(torrent);
                torrentDao.delete(torrent);
            }
        }
        movie.getTorrentIds().clear();
        movieService.delete(movie);

        Map<String, Object> map = new HashMap<>();
        map.put("message", "Movie '" + movie.getName() + "' (id=" + movie.getId() + ") was deleted");
        return Response.ok().entity(JsonTranslation.object2JsonString(map)).build();
    }

    @RequestMapping(value = "/searcher-configurations", method = RequestMethod.GET)
    @ResponseBody
    public Response getSearcherConfigurations() {
        permissionsService.verifyAdminPermissions();

        Collection<SearcherConfiguration> searcherConfigurations = searcherConfigurationService.getSearcherConfigurations();

        return Response.ok().entity(JsonTranslation.object2JsonString(searcherConfigurations.toArray(new SearcherConfiguration[searcherConfigurations.size()]))).build();
    }

    @RequestMapping(value = "/searcher-configuration/{name}/domain/add", method = RequestMethod.POST)
    @ResponseBody
    @Transactional(propagation = Propagation.REQUIRED)
    public Response addDomainToSearcherConfiguration(@PathVariable String name, @RequestParam("domain") String domain) {
        permissionsService.verifyAdminPermissions();

        searcherConfigurationService.addDomain(name, domain);
        return Response.ok().build();
    }

    @RequestMapping(value = "/searcher-configuration/{name}/domain/remove/{domain}", method = RequestMethod.GET)
    @ResponseBody
    @Transactional(propagation = Propagation.REQUIRED)
    public Response removeDomainToSearcherConfiguration(@PathVariable String name, @PathVariable String domain) {
        permissionsService.verifyAdminPermissions();

        searcherConfigurationService.removeDomain(name, domain);
        return Response.ok().build();
    }

    @RequestMapping(value = "/searcher-configuration/torrentz/enable", method = RequestMethod.POST)
    @ResponseBody
    @Transactional(propagation = Propagation.REQUIRED)
    public Response enableTorrentzSearcher(@RequestParam boolean isEnabled) {
        permissionsService.verifyAdminPermissions();

        searcherConfigurationService.torrentzSetEnabled(isEnabled);
        return Response.ok().build();
    }

    @Path("/news")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Transactional(propagation = Propagation.REQUIRED)
    public Response createNews(@QueryParam("text") String text) {
        permissionsService.verifyAdminPermissions();

        News news = new News();
        news.setMessage(text);
        newsService.createNews(news);

        Map<String, Object> result = new HashMap<>();
        result.put("id", news.getId());
        return Response.ok().entity(JsonTranslation.object2JsonString(result)).build();
    }

    @Path("/news/dismiss")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional(propagation = Propagation.REQUIRED)
    public Response dismissNews() {
        permissionsService.verifyAdminPermissions();
        User user = userCacheService.getUser(UserContextHolder.getCurrentUserContext().getUserId());
        newsService.dismissNews(user);
        return Response.ok().build();
    }
}