package rss.services.shows;

import org.apache.commons.lang3.StringUtils;
import org.paukov.combinatorics.Factory;
import org.paukov.combinatorics.Generator;
import org.paukov.combinatorics.ICombinatoricsVector;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import rss.dao.ShowDao;
import rss.entities.Show;
import rss.services.log.LogService;
import rss.util.DurationMeter;
import rss.util.QuartzJob;

import javax.annotation.PostConstruct;
import java.util.*;

/**
 * User: dikmanm
 * Date: 15/03/13 10:31
 */
@Service
@QuartzJob(name = "ShowsCacheServiceImpl", cronExp = "0 0 0/1 * * ?")
public class ShowsCacheServiceImpl extends QuartzJobBean implements ShowsCacheService {

	@Autowired
	private ShowDao showDao;

	@Autowired
	private TransactionTemplate transactionTemplate;

	@Autowired
	protected LogService logService;

	private List<CachedShow> cache;
	private Map<Long, Collection<String>> showNamePermutations;

	@PostConstruct
	private void postConstruct() {
		cache = new ArrayList<>();
		showNamePermutations = new HashMap<>();

		reloadCache();
	}

	private void reloadCache() {
		logService.info(getClass(), "Loading shows cache");
		Thread.dumpStack();
		DurationMeter duration = new DurationMeter();

		cache.clear();
		showNamePermutations.clear();

		for (CachedShow show : showDao.findCachedShows()) {
			addShow(show);
		}

		duration.stop();
		logService.info(getClass(), "Loading shows cache took " + duration.getDuration() + " millis");
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

		showNamePermutations.put(show.getId(), permutationsList);
	}

	private Generator<String> getSubsets(String[] arr) {
		ICombinatoricsVector<String> initialSet = Factory.createVector(arr);
		return Factory.createSubSetGenerator(initialSet);
	}

	@Override
	protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
		transactionTemplate.execute(new TransactionCallbackWithoutResult() {
			@Override
			protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
				reloadCache();
			}
		});
	}

	@Override
	public void put(Show show) {
		addShow(new CachedShow(show.getId(), show.getName()));
	}

	@Override
	public Collection<CachedShow> getAll() {
		// return a copy
		return new ArrayList<>(cache);
	}

	@Override
	public Collection<String> getShowNamePermutations(CachedShow show) {
		return showNamePermutations.get(show.getId());
	}
}
