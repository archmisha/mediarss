package rss.services.shows;

import org.apache.commons.lang3.StringUtils;
import org.paukov.combinatorics.Factory;
import org.paukov.combinatorics.Generator;
import org.paukov.combinatorics.ICombinatoricsVector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import rss.dao.ShowDao;
import rss.entities.Show;
import rss.services.log.LogService;
import rss.util.DurationMeter;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * User: dikmanm
 * Date: 15/03/13 10:31
 */
@Service
public class ShowsCacheServiceImpl implements ShowsCacheService {

	@Autowired
	private ShowDao showDao;

	@Autowired
	private TransactionTemplate transactionTemplate;

	@Autowired
	protected LogService logService;

	private Map<Long, CachedShow> cache;
	private Map<CachedShow, CachedShowSubset[]> showNameSubsets;
	private ScheduledExecutorService executorService;

	@PostConstruct
	private void postConstruct() {
		cache = new HashMap<>();
		showNameSubsets = new HashMap<>();

		executorService = Executors.newSingleThreadScheduledExecutor();
		executorService.scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
				transactionTemplate.execute(new TransactionCallbackWithoutResult() {
					@Override
					protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
						reloadCache();
					}
				});
			}
		}, 0, 1, TimeUnit.HOURS);
	}

	@PreDestroy
	private void preDestroy() {
		logService.info(getClass(), "Terminating shows cache reload job");
		executorService.shutdown();
	}

	private void reloadCache() {
		DurationMeter duration = new DurationMeter();

		cache.clear();
		showNameSubsets.clear();

		for (CachedShow show : showDao.findCachedShows()) {
			addShow(show);
		}

		duration.stop();
		logService.info(getClass(), String.format("Loaded shows cache (%d millis)", duration.getDuration()));
	}

	@Override
	public void updateShowEnded(Show show) {
		if (cache.containsKey(show.getId())) {
			cache.get(show.getId()).setEnded(show.isEnded());
		}
	}

	private void addShow(CachedShow show) {
		cache.put(show.getId(), show);

		String cur = ShowServiceImpl.normalize(show.getName());
		String[] arr = cur.split(" ");

		show.setWords(arr.length);
		show.setNormalizedName(cur);

		List<ICombinatoricsVector<String>> subsets = getSubsets(arr).generateAllObjects();
		CachedShowSubset[] cachedShowSubsets = new CachedShowSubset[subsets.size()];
		int i = 0;
		for (ICombinatoricsVector<String> subSet : subsets) {
			List<String> permutation = subSet.getVector();
			Collections.sort(permutation);
			cachedShowSubsets[i++] = new CachedShowSubset(StringUtils.join(permutation, " "), permutation.size());
		}

		showNameSubsets.put(show, cachedShowSubsets);
	}

	private Generator<String> getSubsets(String[] arr) {
		ICombinatoricsVector<String> initialSet = Factory.createVector(arr);
		return Factory.createSubSetGenerator(initialSet);
	}

	@Override
	public void put(Show show) {
		addShow(new CachedShow(show.getId(), show.getName(), show.isEnded()));
	}

	@Override
	public Collection<CachedShow> getAll() {
		// return a copy
		return new ArrayList<>(cache.values());
	}

	@Override
	public Collection<Map.Entry<CachedShow, CachedShowSubset[]>> getShowsSubsets() {
		return new CopyOnWriteArrayList<>(showNameSubsets.entrySet());
	}
}
