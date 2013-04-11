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
import java.util.*;
import java.util.concurrent.Executors;
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

	private List<CachedShow> cache;
	private Map<CachedShow, Collection<String>> showNameSubsets;

	@PostConstruct
	private void postConstruct() {
		cache = new ArrayList<>();
		showNameSubsets = new HashMap<>();

		Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(new Runnable() {
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

	private void reloadCache() {
		DurationMeter duration = new DurationMeter();

		cache.clear();
		showNameSubsets.clear();

		for (CachedShow show : showDao.findCachedShows()) {
			addShow(show);
		}

		duration.stop();
		logService.info(getClass(), "Loaded shows cache (" + duration.getDuration() + " millis)");
	}

	private void addShow(CachedShow show) {
		cache.add(show);

		Collection<String> permutationsList = new ArrayList<>();
		String cur = ShowServiceImpl.normalize(show.getName());
		for (ICombinatoricsVector<String> subSet : getSubsets(cur.split(" "))) {
			List<String> permutation = subSet.getVector();
			Collections.sort(permutation);
			permutationsList.add(StringUtils.join(permutation, " "));
		}

		showNameSubsets.put(show, permutationsList);
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
		return new ArrayList<>(cache);
	}

	@Override
	public Collection<String> getShowNameSubsets(CachedShow show) {
		return showNameSubsets.get(show);
	}

	@Override
	public List<Map.Entry<CachedShow, Collection<String>>> getShowsSubsets() {
		return new ArrayList<>(showNameSubsets.entrySet());
	}
}
