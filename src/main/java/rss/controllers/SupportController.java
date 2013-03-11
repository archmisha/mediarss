package rss.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import rss.dao.JobStatusDao;
import rss.dao.UserDao;
import rss.entities.JobStatus;
import rss.entities.User;
import rss.services.EmailService;
import rss.services.JobRunner;
import rss.services.SessionService;
import rss.services.SettingsService;
import rss.services.movies.MoviesScrabbler;
import rss.services.shows.ShowsListDownloaderService;
import rss.services.shows.ShowsScheduleDownloaderService;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.servlet.http.HttpServletRequest;
import java.security.InvalidParameterException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User: dikmanm
 * Date: 10/02/13 18:16
 */
@Controller
@RequestMapping("/support")
public class SupportController extends BaseController {

	@Autowired
	private JobStatusDao jobStatusDao;

	@Autowired
	private SessionService sessionService;

	@Autowired
	private UserDao userDao;

	@Autowired
	private MoviesScrabbler moviesScrabbler;

	@Autowired
	private ShowsListDownloaderService showsListDownloaderService;

	@Autowired
	private ShowsScheduleDownloaderService showsScheduleDownloaderService;

	@Autowired
	private EmailService emailService;

	@RequestMapping(value = "", method = RequestMethod.POST)
	@ResponseBody
	public String submitSupportTicket(HttpServletRequest request) {
		String type = extractString(request, "type", true);
		String content = extractString(request, "content", true);
		User user = userDao.find(sessionService.getLoggedInUserId());

		emailService.sendTicket(user, type, content);
		return type + " was successfully submitted";
	}
}