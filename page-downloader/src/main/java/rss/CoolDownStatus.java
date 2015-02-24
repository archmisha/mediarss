package rss;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * User: dikmanm
 * Date: 08/01/13 19:26
 */
public class CoolDownStatus {

    private static Logger log = LoggerFactory.getLogger(CoolDownStatus.class);
    private static final int COOL_DOWN_THRESH_HOLD = 100;
	private static final int COOL_DOWN_TIMEOUT = 30 * 1000;

	private final Map<String, CoolDownEntry> entries;

	public CoolDownStatus() {
		entries = new HashMap<>();
	}

	public void authorizeAccess(String url) {
		String hostname = extractHostName(url);

		if (!entries.containsKey(hostname)) {
			synchronized (entries) {
				if (!entries.containsKey(hostname)) {
					entries.put(hostname, new CoolDownEntry());
				}
			}
		}

		CoolDownEntry coolDownEntry = entries.get(hostname);
//		log.debug("Checking cool down for " + hostname + " counter=" + coolDownEntry.getCounter());
		coolDownEntry.getLock().lock();
		try {
			// if last request was old enough reset the counters
			Calendar c = Calendar.getInstance();
			c.add(Calendar.MILLISECOND, -COOL_DOWN_TIMEOUT);
			if (coolDownEntry.getLastAccess().before(c.getTime())) {
				log.debug("Last access to " + hostname + " was old enough. resetting");
				coolDownEntry.reset();
			}

			coolDownEntry.setCounter(coolDownEntry.getCounter() + 1);
			while (coolDownEntry.getCounter() >= COOL_DOWN_THRESH_HOLD) {
				try {
					log.debug("Counter for " + hostname + " reached limit. Going to sleep");
					coolDownEntry.getCondition().await(COOL_DOWN_TIMEOUT, TimeUnit.MILLISECONDS);
					coolDownEntry.getCondition().signalAll();
				} catch (InterruptedException e) {
					log.error(e.getMessage(), e);
				}
				log.debug("Woke up after sleep for " + hostname);

				// need to make reset only once, even if multiple threads are sleeping
				if (coolDownEntry.getCounter() >= COOL_DOWN_THRESH_HOLD) {
					log.debug("Resetting counters only once for " + hostname + " after sleep");
					coolDownEntry.reset();
				}
				coolDownEntry.setCounter(coolDownEntry.getCounter() + 1);
			}


			coolDownEntry.setLastAccess(new Date());
		} finally {
			coolDownEntry.getLock().unlock();
		}
	}

	private String extractHostName(String url) {
		int ind = url.indexOf("://") + 3; // skip http://, https://
		int end = url.indexOf("/", ind);
		if (end == -1) {
			end = url.length();
		}
		String hostname = url.substring(ind, end);
//		log.debug("Hostname = " + hostname);
		return hostname;
	}

	private class CoolDownEntry {
		private final Condition condition;
		private int counter;
		private Date lastAccess;
		private Lock lock;

		private CoolDownEntry() {
			counter = 0;
			lastAccess = new Date();
			lock = new ReentrantLock();
			condition = lock.newCondition();
		}

		public int getCounter() {
			return counter;
		}

		public void setCounter(int counter) {
			this.counter = counter;
		}

		public Date getLastAccess() {
			return lastAccess;
		}

		public void setLastAccess(Date lastAccess) {
			this.lastAccess = lastAccess;
		}

		public void reset() {
			counter = 0;
			lastAccess = new Date();
		}

		public Lock getLock() {
			return lock;
		}

		public Condition getCondition() {
			return condition;
		}
	}
}
