package rss.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import rss.dao.UserDao;
import rss.services.EmailService;
import rss.services.SessionService;

/**
 * User: dikmanm
 * Date: 10/02/13 18:16
 */
@Controller
@RequestMapping("/subtitles")
public class SubtitlesController extends BaseController {

	@Autowired
	private SessionService sessionService;

	@Autowired
	private UserDao userDao;

	@Autowired
	private EmailService emailService;

}