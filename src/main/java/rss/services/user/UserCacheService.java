package rss.services.user;

import rss.controllers.vo.ShowsScheduleVO;
import rss.entities.User;

/**
 * User: dikmanm
 * Date: 17/02/14 16:43
 */
public interface UserCacheService {

	ShowsScheduleVO getSchedule(User user);

	void invalidateSchedule(User user);

	void addUser(User user);
}
