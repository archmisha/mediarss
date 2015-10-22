package rss.controllers;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import rss.MediaRSSException;
import rss.context.UserContextHolder;
import rss.movies.*;
import rss.movies.dao.MovieDao;
import rss.movies.imdb.IMDBPreviewCacheService;
import rss.movies.imdb.IMDBService;
import rss.permissions.PermissionsService;
import rss.scheduler.JobStatusJson;
import rss.scheduler.SchedulerService;
import rss.torrents.Movie;
import rss.torrents.dao.UserTorrentDao;
import rss.user.User;
import rss.util.DurationMeter;

import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/movies")
public class MoviesController extends BaseController {

    @Autowired
    private PermissionsService permissionsService;

    @Autowired
    private MovieDao movieDao;

    @Autowired
    private IMDBPreviewCacheService imdbPreviewCacheService;

    @Autowired
    private IMDBService imdbService;

    @Autowired
    private SchedulerService schedulerService;

    @Autowired
    private MovieService movieService;

    @Autowired
    protected UserTorrentDao userTorrentDao;

    @RequestMapping(value = "/imdb/{movieId}", method = RequestMethod.GET)
    @ResponseBody
    @Transactional(propagation = Propagation.REQUIRED)
    public String getImdbPreviewPage(@PathVariable long movieId) {
        Movie movie = movieDao.find(movieId);
        return movieService.getImdbPreviewPage(movie);
    }

    @RequestMapping(value = "/imdb/css/{cssFileName}.css", method = RequestMethod.GET)
    @ResponseBody
    @Transactional(propagation = Propagation.REQUIRED)
    public String getImdbCssFile(@PathVariable String cssFileName) {
        return imdbPreviewCacheService.getImdbCSS(cssFileName);
    }

    @RequestMapping(value = "/imdb/person-image/{imageFileName}", method = RequestMethod.GET)
    @ResponseBody
    @Transactional(propagation = Propagation.REQUIRED)
    public void getImdbPersonImage(@SuppressWarnings("ParameterCanBeLocal") @PathVariable String imageFileName, HttpServletRequest request, ServletResponse response) {
        // imageFileName can sometimes arrive without the suffix (like .jpg) for some reason
        StringBuffer url = request.getRequestURL();
        imageFileName = url.substring(url.indexOf("/imdb/person-image/") + "/imdb/person-image/".length());
        if (imageFileName.endsWith("jpg")) {
            response.setContentType("image/jpeg");
        }
        InputStream imageInputStream = imdbService.getPersonImage(imageFileName);
        try (OutputStream os = response.getOutputStream()) {
            int bytes = IOUtils.copy(imageInputStream, os);
            response.setContentLength(bytes);
            os.flush();
        } catch (Exception e) {
            throw new MediaRSSException("Failed downloading IMDB image " + imageFileName + ": " + e.getMessage(), e);
        }
    }

    @RequestMapping(value = "/imdb/movie-image/{imageFileName}", method = RequestMethod.GET)
    @ResponseBody
    @Transactional(propagation = Propagation.REQUIRED)
    public void getImdbMovieImage(@SuppressWarnings("ParameterCanBeLocal") @PathVariable String imageFileName, HttpServletRequest request, ServletResponse response) {
        // imageFileName can sometimes arrive without the suffix (like .jpg) for some reason
        StringBuffer url = request.getRequestURL();
        imageFileName = url.substring(url.indexOf("/imdb/movie-image/") + "/imdb/movie-image/".length());
        InputStream imageInputStream = imdbService.getMovieImage(imageFileName);
        try (OutputStream os = response.getOutputStream()) {
            int bytes = IOUtils.copy(imageInputStream, os);
            response.setContentLength(bytes);
            os.flush();
        } catch (Exception e) {
            throw new MediaRSSException("Failed downloading IMDB image " + imageFileName + ": " + e.getMessage(), e);
        }
    }

    @RequestMapping(value = "/download", method = RequestMethod.POST)
    @ResponseBody
    @Transactional(propagation = Propagation.REQUIRED)
    public Map<String, Object> downloadMovie(@RequestParam("torrentId") long torrentId,
                                             @RequestParam("movieId") long movieId,
                                             @RequestParam("isUserMovies") boolean isUserMovies) {
        User user = userCacheService.getUser(UserContextHolder.getCurrentUserContext().getUserId());
        movieService.addMovieDownload(user, movieId, torrentId);

        userCacheService.invalidateUserMovies(user);
        userCacheService.invalidateAvailableMovies(user);

        Map<String, Object> result = new HashMap<>();
        if (isUserMovies) {
//			result.put("movies", userCacheService.getUserMovies(user));
        } else {
//			result.put("movies", movieService.getAvailableMovies(user));
            result.put("userMoviesCount", userCacheService.getUserMoviesCount(user));
        }
        return result;
    }

    @RequestMapping(value = "/redownload", method = RequestMethod.POST)
    @ResponseBody
    @Transactional(propagation = Propagation.REQUIRED)
    public Map<String, Object> redownload(@RequestParam("movieId") long movieId,
                                          @RequestParam("isUserMovies") boolean isUserMovies) {
        DurationMeter duration = new DurationMeter();
        permissionsService.verifyAdminPermissions();

        movieService.downloadMovie(movieDao.find(movieId));

        User user = userCacheService.getUser(UserContextHolder.getCurrentUserContext().getUserId());
        userCacheService.invalidateUserMovies(user);
        userCacheService.invalidateAvailableMovies(user);

        Map<String, Object> result = new HashMap<>();
        if (isUserMovies) {
//			result.put("movies", userCacheService.getUserMovies(user));
        } else {
//			result.put("movies", movieService.getAvailableMovies(user));
//			result.put("userMoviesCount", userCacheService.getUserMoviesCount(user));
        }

        duration.stop();
        logService.info(getClass(), "movie " + movieId + " redownload took " + duration.getDuration() + " ms");
        return result;
    }

    @RequestMapping(value = "/future/add", method = RequestMethod.POST)
    @ResponseBody
    @Transactional(propagation = Propagation.REQUIRED)
    public Map<String, Object> addMovieFromSearch(@RequestParam("imdbId") String imdbId) {
        User user = userCacheService.getUser(UserContextHolder.getCurrentUserContext().getUserId());
        Pair<Movie, Boolean> futureMovieResult = movieService.addFutureMovieDownload(user, imdbId);
        if (futureMovieResult == null) {
            throw new MediaRSSException("Movie ID was not found in IMDB").doNotLog();
        }

        Map<String, Object> result = new HashMap<>();
        Movie movie = futureMovieResult.getKey();
        if (futureMovieResult.getValue()) {
            result.put("message", "Movie '" + movie.getName() + "' was added");
        } else {
            if (!movie.getTorrentIds().isEmpty()) {
                result.put("message", "Movie '" + movie.getName() + "' was added to your movies and already available for download");
            } else {
                result.put("message", "Movie '" + movie.getName() + "' is being searched for torrents");//was added but is not yet available for download");
            }
        }

        userCacheService.invalidateUserMovies(user);
        logService.info(getClass(), "User " + user + " downloads " + movie);

        // must be after movieUserTorrent creation
        result.put("movies", userCacheService.getUserMovies(user));
        result.put("movieId", movie.getId());
        return result;
    }

    @RequestMapping(value = "/search", method = RequestMethod.POST)
    @ResponseBody
    @Transactional(propagation = Propagation.REQUIRED)
    public Map<String, Object> search(@RequestParam("query") String query) {
        User user = userCacheService.getUser(UserContextHolder.getCurrentUserContext().getUserId());
        Map<String, Object> result = new HashMap<>();
        result.put("searchResults", movieService.search(user, query));
        return result;
    }

    @RequestMapping(value = "/future/remove", method = RequestMethod.POST)
    @ResponseBody
    @Transactional(propagation = Propagation.REQUIRED)
    public Map<String, Object> removeFutureMovie(@RequestParam("movieId") long movieId) {
        DurationMeter duration = new DurationMeter();
        User user = userCacheService.getUser(UserContextHolder.getCurrentUserContext().getUserId());
        UserMovie userMovie = movieDao.findUserMovie(user, movieId);
        // no idea how can happen, dima had it
        if (userMovie != null) {
            for (UserMovieTorrent userMovieTorrent : userMovie.getUserMovieTorrents()) {
                userTorrentDao.delete(userMovieTorrent);
            }
            movieDao.delete(userMovie);
            userCacheService.invalidateUserMovies(user);
        }

        duration.stop();
        logService.info(getClass(), "movies [remove userMovies] " + duration.getDuration() + " ms");

        Map<String, Object> result = new HashMap<>();
        result.put("message", "Movie " + (userMovie == null ? "" : "'" + userMovie.getMovie().getName() + "'") + " was removed from your movies");
        return result;
    }

    @RequestMapping(value = "check-movies-being-searched", method = RequestMethod.POST)
    @ResponseBody
    @Transactional(propagation = Propagation.REQUIRED)
    public Map<String, Object> checkMoviesBeingSearched(@RequestParam("ids[]") long[] ids) {
        Map<String, Object> map = new HashMap<>();
        map.put("completed", movieService.getSearchCompletedMovies(ids));
        return map;
    }

    @RequestMapping(value = "/user-movies", method = RequestMethod.GET)
    @ResponseBody
    @Transactional(propagation = Propagation.REQUIRED)
    public Map<String, Object> getUserMovies() {
        DurationMeter duration = new DurationMeter();
        User user = userCacheService.getUser(UserContextHolder.getCurrentUserContext().getUserId());

        Map<String, Object> result = new HashMap<>();
        result.put("movies", trimMovieTorrents(userCacheService.getUserMovies(user)));
        duration.stop();
        logService.info(getClass(), "movies [userMovies] " + duration.getDuration() + " ms");
        return result;
    }

    @RequestMapping(value = "/available-movies", method = RequestMethod.GET)
    @ResponseBody
    @Transactional(propagation = Propagation.REQUIRED)
    public Map<String, Object> getAvailableMovies() {
        DurationMeter duration = new DurationMeter();
        User user = userCacheService.getUser(UserContextHolder.getCurrentUserContext().getUserId());

        Map<String, Object> result = new HashMap<>();
        result.put("movies", trimMovieTorrents(userCacheService.getAvailableMovies(user)));
        duration.stop();
        logService.info(getClass(), "movies [availableMovies] " + duration.getDuration() + " ms");
        return result;
    }

    @RequestMapping(value = "/initial-data/{category}", method = RequestMethod.GET)
    @ResponseBody
    @Transactional(propagation = Propagation.REQUIRED)
    public Map<String, Object> initialData(@PathVariable(value = "category") String category) {
        DurationMeter duration = new DurationMeter();
        User user = userCacheService.getUser(UserContextHolder.getCurrentUserContext().getUserId());

        Map<String, Object> result = new HashMap<>();
        if (category.equals("availableMovies")) {
            result.put("availableMovies", trimMovieTorrents(userCacheService.getAvailableMovies(user)));
            result.put("userMoviesCount", userCacheService.getUserMoviesCount(user));
        } else {
            result.put("userMovies", trimMovieTorrents(userCacheService.getUserMovies(user)));
            result.put("availableMoviesCount", userCacheService.getAvailableMoviesCount(user));
        }
        result.put("moviesLastUpdated", getMoviesLastUpdated());
        result.put("isAdmin", UserContextHolder.getCurrentUserContext().isAdmin());
        duration.stop();
        logService.info(getClass(), "movies [" + category + "] " + duration.getDuration() + " ms");

        return result;
    }

    @RequestMapping(value = "/torrents/{movieId}", method = RequestMethod.GET)
    @ResponseBody
    @Transactional(propagation = Propagation.REQUIRED)
    public Map<String, Object> getMovieTorrents(@PathVariable(value = "movieId") long movieId) {
        DurationMeter duration = new DurationMeter();
        User user = userCacheService.getUser(UserContextHolder.getCurrentUserContext().getUserId());

        Map<String, Object> result = new HashMap<>();
        List<UserMovieVO> movies = userCacheService.getAvailableMovies(user);
        movies.addAll(userCacheService.getUserMovies(user));
        for (UserMovieVO movie : movies) {
            if (movie.getId() == movieId) {
                result.put("viewedTorrents", movie.getViewedTorrents());
                result.put("notViewedTorrents", movie.getNotViewedTorrents());
                break;
            }
        }

        duration.stop();
        logService.info(getClass(), "movies [torrents] " + duration.getDuration() + " ms");
        return result;
    }

    private List<UserMovieVO> trimMovieTorrents(List<UserMovieVO> movies) {
        for (UserMovieVO movie : movies) {
            movie.getViewedTorrents().clear();
            while (movie.getNotViewedTorrents().size() > 3) {
                movie.getNotViewedTorrents().remove(movie.getNotViewedTorrents().size() - 1);
            }
        }
        return movies;
    }

    private Long getMoviesLastUpdated() {
        JobStatusJson jobStatus = schedulerService.getJobStatus(MoviesScrabblerImpl.MOVIES_SCRABBLER_JOB);
        if (jobStatus == null) {
            return null;
        }
        return jobStatus.getStart();
    }
}