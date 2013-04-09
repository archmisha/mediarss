package rss.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import rss.controllers.vo.UserVO;
import rss.dao.ShowDao;
import rss.dao.UserDao;
import rss.entities.Show;
import rss.entities.User;
import rss.services.EmailService;
import rss.services.SessionService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * User: dikmanm
 * Date: 10/02/13 18:16
 */
@Controller
@RequestMapping("/admin")
public class AdminController extends BaseController {

	@Autowired
	private SessionService sessionService;

	@Autowired
	private UserDao userDao;

	@Autowired
	private EmailService emailService;

	@Autowired
	private EntityConverter entityConverter;

	@Autowired
	private ShowDao showDao;

	@RequestMapping(value = "/notification", method = RequestMethod.POST)
	@ResponseBody
	public void sendNotification(HttpServletRequest request) {
		User user = userDao.find(sessionService.getLoggedInUserId());
		verifyAdminPermissions(user);

		String text = extractString(request, "text", true);
		emailService.sendEmailToAllUsers(text);
	}

	@RequestMapping(value = "/users", method = RequestMethod.GET)
	@ResponseBody
	public Collection<UserVO> getAllUsers() {
		User user = userDao.find(sessionService.getLoggedInUserId());
		verifyAdminPermissions(user);

		List<UserVO> users = entityConverter.toThinUser(userDao.findAll());
		Collections.sort(users, new Comparator<UserVO>() {
			@Override
			public int compare(UserVO o1, UserVO o2) {
				if (o2.getLastLogin() == null) {
					return -1;
				} else if (o1.getLastLogin() == null) {
					return 1;
				}
				return o2.getLastLogin().compareTo(o1.getLastLogin());
			}
		});
		return users;
	}

	@RequestMapping(value = "/downloadSchedule/{showId}", method = RequestMethod.GET)
	@ResponseBody
	@Transactional(propagation = Propagation.REQUIRED)
	public String downloadSchedule(@PathVariable long showId) {
		User user = userDao.find(sessionService.getLoggedInUserId());
		verifyAdminPermissions(user);

		Show show = showDao.find(showId);
		showService.downloadSchedule(show);

		return "Downloaded schedule for '" + show.getName() + "'";
	}

	@RequestMapping(value = "/shows/autocomplete", method = RequestMethod.GET)
	@ResponseBody
	public void autoCompleteShows(HttpServletRequest request, HttpServletResponse response) {
		User user = userDao.find(sessionService.getLoggedInUserId());
		verifyAdminPermissions(user);
		autoCompleteShowNames(request, response, true, null);
	}
}