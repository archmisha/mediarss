package rss.services.user;

import rss.controllers.vo.ShowsScheduleVO;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * User: dikmanm
 * Date: 17/02/14 16:48
 */
public class UserCacheEntry {

	private Lock lock;
	private ShowsScheduleVO schedule;

	public UserCacheEntry() {
		lock = new ReentrantLock();
	}

	public void lock() {
		lock.lock();
	}

	public void unlock() {
		lock.unlock();
	}

	public ShowsScheduleVO getSchedule() {
		return schedule;
	}

	public void setSchedule(ShowsScheduleVO schedule) {
		this.schedule = schedule;
	}
}
