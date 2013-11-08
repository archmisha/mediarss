package rss.services;

import rss.controllers.vo.ShowsScheduleVO;
import rss.entities.User;
import rss.services.shows.UsersSearchesCache;

import java.util.Date;

/**
 * User: Michael Dikman
 * Date: 01/12/12
 * Time: 13:49
 */
public interface SessionService {

	void setLoggedInUser(User user);

	Long getLoggedInUserId();

	Date getPrevLoginDate();

	void clearLoggedInUser();

	boolean isUserLogged();

	ShowsScheduleVO getSchedule();

	void setSchedule(ShowsScheduleVO schedule);

	UsersSearchesCache getUsersSearchesCache();
}
