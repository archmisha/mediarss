package rss.services.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rss.controllers.vo.ShowsScheduleVO;
import rss.dao.UserDao;
import rss.entities.User;
import rss.services.log.LogService;
import rss.services.shows.ShowService;
import rss.util.DurationMeter;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
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
	protected LogService logService;

	@Autowired
	protected ShowService showService;

	@Autowired
	private UserDao userDao;

	private Map<User, UserCacheEntry> cache = new ConcurrentHashMap<>();

	private ScheduledExecutorService executorService;

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
		logService.info(getClass(), "Terminating users cache reload job");
		executorService.shutdown();
	}

	private void reloadCache() {
		DurationMeter duration = new DurationMeter();

		for (final User user : userDao.findAll()) {
			if (!cache.containsKey(user)) {
				cache.put(user, new UserCacheEntry());
			}
			reloadUser(user);
		}

		duration.stop();
		logService.info(getClass(), String.format("Loaded users cache (%d ms)", duration.getDuration()));
	}

	private void reloadUser(final User user) {
		performUserUpdate(user, new AtomicUserUpdate() {
			@Override
			public void run(UserCacheEntry cacheEntry) {
				invalidateSchedule(user);
			}
		});
	}

	@Override
	public ShowsScheduleVO getSchedule(User user) {
		UserCacheEntry cacheEntry = cache.get(user);
		return cacheEntry.getSchedule();
	}

	@Override
	public void invalidateSchedule(User user) {
		final ShowsScheduleVO schedule = showService.getSchedule(user);
		performUserUpdate(user, new AtomicUserUpdate() {
			@Override
			public void run(UserCacheEntry cacheEntry) {
				cacheEntry.setSchedule(schedule);
			}
		});
	}

	@Override
	public void addUser(User user) {
		cache.put(user, new UserCacheEntry());
		reloadUser(user);
	}

	private void performUserUpdate(User user, AtomicUserUpdate update) {
		UserCacheEntry cacheEntry = cache.get(user);
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
