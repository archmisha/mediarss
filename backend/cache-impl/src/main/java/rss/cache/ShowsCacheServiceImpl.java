package rss.cache;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import org.apache.commons.lang3.StringUtils;
import org.paukov.combinatorics.Factory;
import org.paukov.combinatorics.Generator;
import org.paukov.combinatorics.ICombinatoricsVector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rss.environment.Environment;
import rss.log.LogService;
import rss.shows.CachedShow;
import rss.shows.ShowService;
import rss.torrents.Show;
import rss.torrents.TorrentUtils;
import rss.util.DurationMeter;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * User: dikmanm
 * Date: 15/03/13 10:31
 */
@Service
public class ShowsCacheServiceImpl implements ShowsCacheService {

    @Autowired
    private ShowService showService;

    @Autowired
    protected LogService logService;

    private Multimap<Long, CachedShow> cache = HashMultimap.create();
    private Multimap<Long, CachedShowSubsetSet> showNameSubsets = HashMultimap.create();

    private ScheduledExecutorService executorService;

    private ArrayList<CachedShow> readOnlyCachedShows;
    private ArrayList<CachedShowSubsetSet> readOnlyShowSubsets;

    private Lock lock = new ReentrantLock();

    @PostConstruct
    private void postConstruct() {
        executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                reloadCache();
            }
        }, 0, 1, TimeUnit.HOURS);
    }

    @PreDestroy
    private void preDestroy() {
        logService.info(getClass(), "Terminating shows cache reload job");
        executorService.shutdown();
    }

    private void reloadCache() {
        lock.lock();
        try {
            DurationMeter duration = new DurationMeter();

            // store the ids of the existing shows in the cache, to know which to remove later
            Set<Long> existingShowIds = new HashSet<>(cache.keySet());

            for (CachedShow show : showService.findCachedShows()) {
                addShow(show);
                existingShowIds.remove(show.getId());
            }

            // remove shows which were not found in the DB
            for (Long existingShowId : existingShowIds) {
                // only if found and removed the show from the first cache, try to remove it also from the second one
                if (!cache.removeAll(existingShowId).isEmpty()) {
                    showNameSubsets.removeAll(existingShowId);
                }
            }

            readOnlyCachedShows = new ArrayList<>(cache.values());
            readOnlyShowSubsets = new ArrayList<>(showNameSubsets.values());

            duration.stop();
            logService.info(getClass(), String.format("Loaded shows cache (%d ms)", duration.getDuration()));
        } catch (Exception e) {
            logService.error(getClass(), "Failed loading shows cache: " + e.getMessage(), e);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void updateShowEnded(Show show) {
        lock.lock();
        try {
            if (cache.containsKey(show.getId())) {
                for (CachedShow cachedShow : cache.get(show.getId())) {
                    cachedShow.setEnded(show.isEnded());
                }
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void put(Show show) {
        lock.lock();
        try {
            boolean isChangedMaps = addShow(new CachedShow(show.getId(), show.getName(), show.isEnded()));
            if (isChangedMaps) {
                readOnlyCachedShows = new ArrayList<>(cache.values());
                readOnlyShowSubsets = new ArrayList<>(showNameSubsets.values());
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    public Collection<CachedShow> getAll() {
        return readOnlyCachedShows;
    }

    @Override
    public Collection<CachedShowSubsetSet> getShowsSubsets() {
        return readOnlyShowSubsets;
    }

    private boolean addShow(CachedShow show) {
        // if cache already contains this show, the only thing to update is the ended field
        Collection<CachedShow> cachedShows = cache.get(show.getId());
        if (!cachedShows.isEmpty()) {
            for (CachedShow cachedShow : cachedShows) {
                cachedShow.setEnded(show.isEnded());
            }
            return false;
        }

        addShowHelper(show);

        // handle alias
        String alias = Environment.getInstance().getShowAlias(show.getName());
        if (!StringUtils.isBlank(alias)) {
            CachedShow aliasShow = new CachedShow(show.getId(), alias, show.isEnded());
            addShowHelper(aliasShow);
        }

        return true;
    }

    private void addShowHelper(CachedShow show) {
        String cur = TorrentUtils.normalize(show.getName());
        String[] arr = cur.split(" ");

        show.setWords(arr.length);
        show.setNormalizedName(cur);

        List<ICombinatoricsVector<String>> subsets = getSubsets(arr).generateAllObjects();
        CachedShowSubset[] cachedShowSubsets = new CachedShowSubset[subsets.size()];
        int i = 0;
        for (ICombinatoricsVector<String> subSet : subsets) {
            List<String> permutation = subSet.getVector();
            Collections.sort(permutation);
            cachedShowSubsets[i++] = new CachedShowSubset(StringUtils.join(permutation, " "), (byte) permutation.size());
        }

        cache.put(show.getId(), show);
        showNameSubsets.put(show.getId(), new CachedShowSubsetSet(show, cachedShowSubsets));
    }

    private Generator<String> getSubsets(String[] arr) {
        ICombinatoricsVector<String> initialSet = Factory.createVector(arr);
        return Factory.createSubSetGenerator(initialSet);
    }
}
