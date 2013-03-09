package rss.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import rss.NoPermissionsException;
import rss.dao.JobStatusDao;
import rss.dao.UserDao;
import rss.entities.JobStatus;
import rss.entities.User;
import rss.services.JobRunner;
import rss.services.SessionService;
import rss.services.log.LogService;
import rss.services.movies.MoviesScrabbler;
import rss.services.shows.ShowsListDownloaderService;
import rss.services.shows.ShowsScheduleDownloaderService;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.servlet.http.HttpServletRequest;
import java.security.InvalidParameterException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * User: dikmanm
 * Date: 10/02/13 18:16
 */
@Controller
@RequestMapping("/jobs")
public class JobsController extends BaseController {

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

	private Map<String, JobRunner> jobRunners = new HashMap<>();

	@PostConstruct
	private void postConstruct() {
		jobRunners.put(MoviesScrabbler.JOB_NAME, (JobRunner) moviesScrabbler);
		jobRunners.put(ShowsListDownloaderService.JOB_NAME, (JobRunner) showsListDownloaderService);
		jobRunners.put(ShowsScheduleDownloaderService.JOB_NAME, (JobRunner) showsScheduleDownloaderService);
	}

	@PreDestroy
	private void preDestroy() {
		jobRunners.clear();
	}

	@RequestMapping(value = "", method = RequestMethod.GET)
	@ResponseBody
//	@Transactional(propagation = Propagation.REQUIRED)
	public Collection<JobStatus> getAll() {
		User user = userDao.find(sessionService.getLoggedInUserId());
		verifyAdminPermissions(user);

		return jobStatusDao.findAll();
	}

	@RequestMapping(value = "/start", method = RequestMethod.POST)
	@ResponseBody
	public JobStatus start(HttpServletRequest request) {
		User user = userDao.find(sessionService.getLoggedInUserId());
		verifyAdminPermissions(user);

		String name = extractString(request, "name", true);
		if (!jobRunners.containsKey(name)) {
			throw new InvalidParameterException("Unknown job name: " + name);
		}
		JobStatus jobStatus = jobRunners.get(name).start();
		return jobStatus;
	}

	@RequestMapping(value = "/{id}", method = RequestMethod.GET)
	@ResponseBody
	public JobStatus get(@PathVariable long id) {
		User user = userDao.find(sessionService.getLoggedInUserId());
		verifyAdminPermissions(user);

		JobStatus jobStatus = jobStatusDao.find(id);
		return jobStatus;
	}
}