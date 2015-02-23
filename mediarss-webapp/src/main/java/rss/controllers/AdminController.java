package rss.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import rss.MediaRSSException;
import rss.context.UserContextHolder;
import rss.context.UserContextImpl;
import rss.controllers.vo.UserVO;
import rss.dao.*;
import rss.entities.*;
import rss.permissions.PermissionsService;
import rss.services.NewsService;
import rss.services.searchers.SearcherConfigurationService;
import rss.services.user.UserService;

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
    private UserDao userDao;

    @Autowired
    private PermissionsService permissionsService;

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

    @Autowired
    private NewsService newsService;

    @Autowired
    private UserService userService;

    @Autowired
    private SearcherConfigurationService searcherConfigurationService;

    @RequestMapping(value = "/notification", method = RequestMethod.POST)
    @ResponseBody
    public void sendNotification(HttpServletRequest request) {
        permissionsService.verifyAdminPermissions();

        String text = extractString(request, "text", true);
        userService.sendEmailToAllUsers(text);
    }

    @RequestMapping(value = "/users", method = RequestMethod.GET)
    @ResponseBody
    public Collection<UserVO> getAllUsers() {
        permissionsService.verifyAdminPermissions();

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
        permissionsService.verifyAdminPermissions();

        Show show = showDao.find(showId);
        showService.downloadFullScheduleWithTorrents(show, false);

        return "Downloaded schedule for '" + show.getName() + "'";
    }

    @RequestMapping(value = "/shows/autocomplete", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, Object> autoCompleteShows(HttpServletRequest request, HttpServletResponse response) {
        permissionsService.verifyAdminPermissions();
        return autoCompleteShowNames(request, response, true, null);
    }

    @RequestMapping(value = "/shows/delete/{showId}", method = RequestMethod.GET)
    @ResponseBody
    @Transactional(propagation = Propagation.REQUIRED)
    public String deleteShow(@PathVariable Long showId) {
        permissionsService.verifyAdminPermissions();

        Show show = showDao.find(showId);
        if (show == null) {
            return "Show with id " + showId + " is not found";
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
        permissionsService.verifyAdminPermissions();

        Movie movie = movieDao.find(movieId);
        if (movie == null) {
            return "Movie with id " + movieId + " is not found";
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
        permissionsService.verifyAdminPermissions();

        // remove previous impersonations
        UserContextHolder.popOnBehalfUserContexts();

        // if not impersonating back to myself
        if (UserContextHolder.getActualUserContext().getUserId() != userId) {
            User user = userCacheService.getUser(userId);
            UserContextHolder.pushUserContext(new UserContextImpl(user.getId(), user.getEmail(), user.isAdmin()));
        }
    }

    @RequestMapping(value = "/searcher-configurations", method = RequestMethod.GET)
    @ResponseBody
    public Collection<SearcherConfiguration> getSearcherConfigurations() {
        permissionsService.verifyAdminPermissions();

        return searcherConfigurationService.getSearcherConfigurations();
    }

    @RequestMapping(value = "/searcher-configuration/{name}/domain/add", method = RequestMethod.POST)
    @ResponseBody
    @Transactional(propagation = Propagation.REQUIRED)
    public void addDomainToSearcherConfiguration(@PathVariable String name, @RequestParam("domain") String domain) {
        permissionsService.verifyAdminPermissions();

        searcherConfigurationService.addDomain(name, domain);
    }

    @RequestMapping(value = "/searcher-configuration/{name}/domain/remove/{domain}", method = RequestMethod.GET)
    @ResponseBody
    @Transactional(propagation = Propagation.REQUIRED)
    public void removeDomainToSearcherConfiguration(@PathVariable String name, @PathVariable String domain) {
        permissionsService.verifyAdminPermissions();

        searcherConfigurationService.removeDomain(name, domain);
    }

    @RequestMapping(value = "/searcher-configuration/torrentz/enable", method = RequestMethod.POST)
    @ResponseBody
    @Transactional(propagation = Propagation.REQUIRED)
    public void enableTorrentzSearcher(@RequestParam boolean isEnabled) {
        permissionsService.verifyAdminPermissions();

        searcherConfigurationService.torrentzSetEnabled(isEnabled);
    }

    @RequestMapping(value = "/news", method = RequestMethod.POST)
    @ResponseBody
    @Transactional(propagation = Propagation.REQUIRED)
    public long createNews(HttpServletRequest request) {
        permissionsService.verifyAdminPermissions();

        String text = extractString(request, "text", true);
        News news = new News();
        news.setMessage(text);
        newsService.createNews(news);
        return news.getId();
    }

    @RequestMapping(value = "/news/dismiss", method = RequestMethod.GET)
    @ResponseBody
    @Transactional(propagation = Propagation.REQUIRED)
    public void dismissNews() {
        permissionsService.verifyAdminPermissions();
        User user = userCacheService.getUser(UserContextHolder.getCurrentUserContext().getUserId());
        newsService.dismissNews(user);
    }


}