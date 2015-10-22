package rss.cache;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;
import rss.log.LogService;
import rss.movies.MovieService;
import rss.movies.UserMovieVO;
import rss.shows.CachedShow;
import rss.shows.ShowJSON;
import rss.shows.ShowService;
import rss.shows.ShowsScheduleJSON;
import rss.torrents.Show;
import rss.user.User;
import rss.user.UserService;
import rss.util.DurationMeter;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * User: dikmanm
 * Date: 17/02/14 16:43
 */
@Service
public class UserCacheServiceImpl implements UserCacheService {

    @Autowired
    private LogService logService;

    @Autowired
    private ShowService showService;

    @Autowired
    private UserService userService;

    @Autowired
    private MovieService movieService;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    private ShowsCacheService showsCacheService;

    private Map<Long, UserCacheEntry> cache = new ConcurrentHashMap<>();

    private ScheduledExecutorService executorService;

    @PostConstruct
    private void postConstruct() {
        executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                try {
                    // need transaction template, cuz otherwise when getting user shows it throws LazyInitializationException
                    transactionTemplate.execute(new TransactionCallback<Object>() {
                        @Override
                        public Object doInTransaction(TransactionStatus transactionStatus) {
                            reloadCache();
                            return null;
                        }
                    });
                } catch (Exception e) {
                    logService.error(getClass(), "Failed loading users cache: " + e.getMessage(), e);
                }
            }
        }, 0, 1, TimeUnit.HOURS);
    }

    @PreDestroy
    private void preDestroy() {
        logService.info(getClass(), "Terminating users cache reload job");
        executorService.shutdown();
    }

    private void reloadCache() {
        DurationMeter duration = new DurationMeter();

        for (final User user : userService.getAllUsers()) {
            if (!cache.containsKey(user.getId())) {
                cache.put(user.getId(), new UserCacheEntry(user));
            }
            reloadUser(user);
        }

        duration.stop();
        logService.info(getClass(), String.format("Loaded users cache (%d ms)", duration.getDuration()));
    }

    private void reloadUser(final User user) {
        invalidateSchedule(user);
        invalidateTrackedShows(user);
        invalidateUserMovies(user);
        invalidateAvailableMovies(user);
    }

    @Override
    public ShowsScheduleJSON getSchedule(User user) {
        UserCacheEntry cacheEntry = cache.get(user.getId());
        return cacheEntry.getSchedule();
    }

    @Override
    public void invalidateSchedule(User user) {
        final ShowsScheduleJSON schedule = showService.getSchedule(user);
        performUserUpdate(user.getId(), new AtomicUserUpdate() {
            @Override
            public void run(UserCacheEntry cacheEntry) {
                cacheEntry.setSchedule(schedule);
            }
        });
    }

    @Override
    public List<ShowJSON> getTrackedShows(User user) {
        UserCacheEntry cacheEntry = cache.get(user.getId());
        List<ShowJSON> shows = new ArrayList<>();

        final List<Long> trackedShows = cacheEntry.getTrackedShows();
        if (!trackedShows.isEmpty()) {
            Map<Long, CachedShow> showsMap = Maps.uniqueIndex(showsCacheService.getAll(), new Function<CachedShow, Long>() {
                @Override
                public Long apply(CachedShow show) {
                    return show.getId();
                }
            });

            for (Long id : trackedShows) {
                CachedShow cachedShow = showsMap.get(id);
                if (cachedShow != null) {
                    shows.add(new ShowJSON().withId(cachedShow.getId()).withName(cachedShow.getName())
                            .withEnded(cachedShow.isEnded()));
                }
            }
        }
        return shows;
    }

    @Override
    public void invalidateTrackedShows(User user) {
        final List<Long> trackedShows = Lists.transform(showService.getTrackedShows(user), new Function<Show, Long>() {
            @Override
            public Long apply(Show show) {
                return show.getId();
            }
        });
        performUserUpdate(user.getId(), new AtomicUserUpdate() {
            @Override
            public void run(UserCacheEntry cacheEntry) {
                cacheEntry.setTrackedShows(trackedShows);
            }
        });
    }

    @Override
    public void addUser(User user) {
        cache.put(user.getId(), new UserCacheEntry(user));
        reloadUser(user);
    }

    @Override
    public User getUser(long userId) {
        UserCacheEntry cacheEntry = cache.get(userId);
        if (cacheEntry == null) { // can happen from register servlet
            return null;
        }
        return cacheEntry.getUser();
    }

    @Override
    public void invalidateUser(User user) {
        final User newUser = userService.getUser(user.getId());
        if (newUser == null) {
            cache.remove(user.getId());
        } else {
            performUserUpdate(user.getId(), new AtomicUserUpdate() {
                @Override
                public void run(UserCacheEntry cacheEntry) {
                    cacheEntry.setUser(newUser);
                }
            });
        }
    }

    @Override
    public List<UserMovieVO> getUserMovies(User user) {
        UserCacheEntry cacheEntry = cache.get(user.getId());
        return levelOneCopy(cacheEntry.getUserMovies());
    }

    @Override
    public void invalidateUserMovies(User user) {
        final List<UserMovieVO> userMovies = movieService.getUserMovies(user);
        performUserUpdate(user.getId(), new AtomicUserUpdate() {
            @Override
            public void run(UserCacheEntry cacheEntry) {
                cacheEntry.setUserMovies(userMovies);
            }
        });
    }

    @Override
    public int getUserMoviesCount(User user) {
        UserCacheEntry cacheEntry = cache.get(user.getId());
        return cacheEntry.getUserMovies().size();
    }

    @Override
    public void invalidateAvailableMovies(User user) {
        final List<UserMovieVO> userMovies = movieService.getAvailableMovies(user);
        performUserUpdate(user.getId(), new AtomicUserUpdate() {
            @Override
            public void run(UserCacheEntry cacheEntry) {
                cacheEntry.setAvailableMovies(userMovies);
            }
        });
    }

    @Override
    public List<UserMovieVO> getAvailableMovies(User user) {
        UserCacheEntry cacheEntry = cache.get(user.getId());
        return levelOneCopy(cacheEntry.getAvailableMovies());
    }

    @Override
    public int getAvailableMoviesCount(User user) {
        UserCacheEntry cacheEntry = cache.get(user.getId());
        return cacheEntry.getAvailableMovies().size();
    }

    @Override
    public void invalidateMovies() {
        for (UserCacheEntry userCacheEntry : cache.values()) {
            User user = userCacheEntry.getUser();
            invalidateAvailableMovies(user);
            invalidateUserMovies(user);
        }
    }

    private List<UserMovieVO> levelOneCopy(List<UserMovieVO> movies) {
        List<UserMovieVO> result = new ArrayList<>();
        for (UserMovieVO movie : movies) {
            UserMovieVO copy = new UserMovieVO();
            copy.setId(movie.getId());
            copy.setTitle(movie.getTitle());
            copy.getNotViewedTorrents().addAll(movie.getNotViewedTorrents());
            copy.setNotViewedTorrentsCount(movie.getNotViewedTorrentsCount());
            copy.getViewedTorrents().addAll(movie.getViewedTorrents());
            copy.setViewedTorrentsCount(movie.getViewedTorrentsCount());
            copy.setDownloadStatus(movie.getDownloadStatus());
            copy.setReleaseDate(movie.getReleaseDate());
            result.add(copy);
        }

        return result;
    }

    private void performUserUpdate(long userId, AtomicUserUpdate update) {
        UserCacheEntry cacheEntry = cache.get(userId);
        cacheEntry.lock();
        try {
            update.run(cacheEntry);
        } finally {
            cacheEntry.unlock();
        }
    }

    private interface AtomicUserUpdate {
        void run(UserCacheEntry cacheEntry);
    }
}
