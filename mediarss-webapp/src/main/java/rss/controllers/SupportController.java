package rss.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import rss.entities.User;
import rss.services.EmailService;
import rss.services.SessionService;

import javax.servlet.http.HttpServletRequest;

/**
 * User: dikmanm
 * Date: 10/02/13 18:16
 */
@Controller
@RequestMapping("/support")
public class SupportController extends BaseController {

	@Autowired
	private SessionService sessionService;

	@Autowired
	private EmailService emailService;

	@RequestMapping(value = "", method = RequestMethod.POST)
	@ResponseBody
	public String submitSupportTicket(HttpServletRequest request) {
		String type = extractString(request, "type", true);
		String content = extractString(request, "content", true);
		User user = userCacheService.getUser(sessionService.getLoggedInUserId());

		emailService.notifyOfATicket(user, type, content);
		return type + " was successfully submitted";
	}
}