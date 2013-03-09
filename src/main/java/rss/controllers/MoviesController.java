package rss.controllers;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import rss.dao.*;
import rss.entities.*;
import rss.services.*;
import rss.services.log.LogService;
import rss.services.shows.ShowService;
import rss.util.DurationMeter;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;

@Controller
@RequestMapping("/movies")
public class MoviesController extends BaseController {

	@Autowired
	private UserService userService;

	@Autowired
	private UserDao userDao;

	@Autowired
	private EmailService emailService;

	@Autowired
	private SessionService sessionService;

	@Autowired
	private JobStatusDao jobStatusDao;

	@Autowired
	private ShowDao showDao;

	@Autowired
	private EntityConverter entityConverter;

	@Autowired
	private SettingsService settingsService;

	@Autowired
	private LogService logService;

	@Autowired
	private ShowService showService;

	@Autowired
	private SubtitlesService subtitlesService;

	@Autowired
	private MovieDao movieDao;

	@Autowired
	private PageDownloader pageDownloader;

	@Autowired
	private UserTorrentDao userTorrentDao;

	@Autowired
	private TorrentDao torrentDao;

	@RequestMapping(value = "/imdb/{movieId}", method = RequestMethod.GET)
	@ResponseBody
	@Transactional(propagation = Propagation.REQUIRED)
	public String getImdbPage(@PathVariable long movieId) {
		Movie movie = movieDao.find(movieId);

		String page = sessionService.getImdbMoviePage(movie.getId());
		if (page != null) {
			logService.debug(getClass(), "IMDB page for movie " + movie.getName() + " was found in cache");
		} else {
			DurationMeter durationMeter = new DurationMeter();
			page = pageDownloader.downloadPage(movie.getImdbUrl());
			durationMeter.stop();
			logService.debug(getClass(), "IMDB page download for movie " + movie.getName() + " took " + durationMeter.getDuration() + " millis");

			if (page != null) {
				page = cleanImdbPage(movie.getName(), page);
			}

			sessionService.setImdbMoviePage(movie.getId(), page);
		}

		return page;
	}

	private String cleanImdbPage(String name, String page) {
		DurationMeter durationMeter = new DurationMeter();
		Document doc = Jsoup.parse(page);
		doc.select("#maindetails_sidebar_bottom").remove();
		doc.select("#nb20").remove();
		doc.select("#titleRecs").remove();
		doc.select("#titleBoardsTeaser").remove();
		doc.select("div.article.contribute").remove();
		doc.select("#title_footer_links").remove();
		doc.select("#titleDidYouKnow").remove();
		doc.select("#footer").remove();
		doc.select("#root").removeAttr("id");
		doc.select("script").remove();
		doc.select("iframe").remove();
		doc.select("link[type!=text/css").remove();
		doc.select("#bottom_ad_wrapper").remove();
		doc.select("#pagecontent").removeAttr("id"); // got the style of the top line
		doc.select(".rightcornerlink").remove();
		doc.select("div#content-2-wide").removeAttr("id");
		doc.head().append("<style>html {min-width:100px;} body {margin:0px; padding:0px;}</style>");
		doc.body().append("<script>parent.resize_iframe()</script>");

		durationMeter.stop();
		logService.debug(getClass(), "Cleaning IMDB page for movie " + name + " took " + durationMeter.getDuration() + " millis");
		return doc.html();
	}

	@RequestMapping(value = "/download", method = RequestMethod.POST)
	@ResponseBody
	@Transactional(propagation = Propagation.REQUIRED)
	public void movieDownload(HttpServletRequest request) {
		long torrentId = extractMandatoryInteger(request, "torrentId");
		User user = userDao.find(sessionService.getLoggedInUserId());
		Torrent torrent = torrentDao.find(torrentId);
		addUserTorrent(user, torrent, new MovieUserTorrent());
		if (user.getSubtitles() != null) {
//			subtitlesService.downloadEpisodeSubtitles(torrent, episode, user.getSubtitles());
		}
	}

	@RequestMapping(value = "/view", method = RequestMethod.POST)
	@ResponseBody
	@Transactional(propagation = Propagation.REQUIRED)
	public void markMovieViewed(HttpServletRequest request) {
		long movieId = extractMandatoryInteger(request, "movieId");
		User user = userDao.find(sessionService.getLoggedInUserId());
		UserMovie userMovie = movieDao.findUserMovie(movieId, user);
		if (userMovie == null) {
			userMovie = new UserMovie();
			userMovie.setUser(user);
			userMovie.setMovie(movieDao.find(movieId));
			movieDao.persist(userMovie);
		}
		userMovie.setUpdated(new Date());
	}
}