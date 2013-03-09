package rss.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import rss.controllers.vo.UserVO;
import rss.dao.UserDao;
import rss.entities.User;
import rss.services.EmailService;
import rss.services.SessionService;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;

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

		return entityConverter.toThinUser(userDao.findAll());
	}
}