package rss;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import rss.controllers.vo.UserResponse;
import rss.dao.*;
import rss.entities.*;
import rss.services.*;
import rss.services.movies.MoviesScrabbler;
import rss.services.shows.ShowService;
import rss.services.shows.ShowsScheduleDownloaderService;
//import rssFeed.shared.MainService;

import java.util.*;

/**
 * User: Michael Dikman
 * Date: 12/05/12
 * Time: 08:45
 * <p/>
 * The rss side implementation of the RPC service.
 * <p/>
 * Note that performance reasons dictate that as few services as possible
 * are exposed as remote services. Try to concentrate all methods in this interface, even though it might
 * become loaded. However, this class should not do any work. Instead, all work should be delegated to SubService
 * classes that will receive the input and needed servlet environment (request / session / etc).
 */
@Service("mainService")
public class MainServiceImpl /*implements MainService*/ {

	private static Log log = LogFactory.getLog(MainServiceImpl.class);

	@Autowired
	private UserDao userDao;

	@Autowired
	private EpisodeDao episodeDao;

	@Autowired
	private SessionService sessionService;

//	@Autowired
//	private EmailService emailService;

	@Autowired
	private MoviesScrabbler moviesScrabbler;

	@Autowired
	private JobStatusDao jobStatusDao;

//	@Autowired
//	private SettingsService settingsService;

//	@Autowired
//	private UserService userService;

	@Autowired
	private TorrentDao torrentDao;

	@Autowired
	private ShowDao showDao;

	@Autowired
	private UserTorrentDao userTorrentDao;

//	@Autowired
//	private TVShowsTorrentEntriesDownloader torrentEntriesDownloader;

	@Autowired
	private SubtitlesService subtitlesService;

	@Autowired
	private ShowRssService showRssService;

	@Autowired
	private ShowService showService;

	@Autowired
	private ShowsScheduleDownloaderService showsScheduleDownloaderService;

	// we have an inside transaction for this to ensure synchronization
//	@Override
	/*public GenerateFeedResponse register(final User user) {
		return null;
//		return userService.register(user.getEmail(), user.getPassword());
	}*/

//	@Override
	/*public void logout() {
		sessionService.clearLoggedInUser();
	}*/

//	@Override
	/*@Transactional(propagation = Propagation.REQUIRED)
	public UserResponse login(String email, String password) {
		User user = userDao.findByEmail(email);
		if (user == null || !user.getPassword().equals(password)) {
			return new UserResponse("Username or password are incorrect");
		}
		if (!user.isValidated()) {
			emailService.sendAccountValidationLink(user); // resend account validation link
			return new UserResponse("Account email is not validated. Please validate before logging in");
		}
		sessionService.setLoggedInUser(user);
		user.setLastLogin(new Date()); // important to be after setting the lastLoginDate. session service saves the previous
		return createUserResponse(user);
	}*/

//	@Override
	public ArrayList<User> getAllUsers() {
		User user = userDao.find(sessionService.getLoggedInUserId());
		if (!user.isAdmin()) {
			log.error("Detected impersonation of admin user trying to get all users data. User: " + user.getEmail());
			return new ArrayList<>();
		}

		return new ArrayList<>(userDao.findAll());
	}

//	@Override
	/*@Transactional(propagation = Propagation.REQUIRED)
	public PreLoginData getPreLoginData() {
		PreLoginData preLoginData = new PreLoginData();
		preLoginData.withDeploymentDate(settingsService.getDeploymentDate());

		if (sessionService.isUserLogged()) {
			User user = userDao.find(sessionService.getLoggedInUserId());
			preLoginData.withLoggedInUser(createUserResponse(user));
		}

		return preLoginData;
	}*/

	@Transactional(propagation = Propagation.REQUIRED)
	private UserResponse createUserResponse(User user) {
		return null;
//		return userService.getUserResponse(user)
//				.withMoviesLastUpdated(getMoviesLastUpdated())
//				.withAllShows(new ArrayList<>(showDao.getNotEnded()));
	}

	/*private Date getMoviesLastUpdated() {
		JobStatus jobStatus = jobStatusDao.find(JobType.MOVIES);
		if (jobStatus == null) {
			return null;
		}
		return jobStatus.getLastExecution();
	}*/

//	@Override
	/*@Transactional(propagation = Propagation.REQUIRED)
	public void startMoviesJob() {
		User user = userDao.find(sessionService.getLoggedInUserId());
		if (!user.isAdmin()) {
			log.error("Detected impersonation of admin user trying to start movies job. User: " + user.getEmail());
			return;
		}

		moviesScrabbler.scrambleMovies();
	}*/

//	@Override
	/*@Transactional(propagation = Propagation.REQUIRED)
	public void startDownloadSchedulesJob() {
		User user = userDao.find(sessionService.getLoggedInUserId());
		if (!user.isAdmin()) {
			log.error("Detected impersonation of admin user trying to start movies job. User: " + user.getEmail());
			return;
		}

		showsScheduleDownloaderService.downloadSchedule();
	}*/

//	@Override
	/*@Transactional(propagation = Propagation.REQUIRED)
	public void startDownloadShowsListJob() {
		User user = userDao.find(sessionService.getLoggedInUserId());
		if (!user.isAdmin()) {
			log.error("Detected impersonation of admin user trying to start movies job. User: " + user.getEmail());
			return;
		}

		showService.downloadShowList();
	}*/

////	@Override
//	@Transactional(propagation = Propagation.REQUIRED)
//	public void addUserMovie(long torrentId) {
//		User user = userDao.find(sessionService.getLoggedInUserId());
//		Torrent torrent = torrentDao.find(torrentId);
////		addUserTorrent(user, torrent);
//		if (user.getSubtitles() != null) {
////			subtitlesService.downloadEpisodeSubtitles(torrent, episode, user.getSubtitles());
//		}
//	}

	/*@Transactional(propagation = Propagation.REQUIRED)
	private UserTorrent addUserTorrent(User user, Torrent torrent) {
		UserTorrent userTorrent = new UserTorrent();
		userTorrent.setUser(user);
		userTorrent.setAdded(new Date());
		userTorrent.setTorrent(torrent);
		userTorrent.setIgnored(false);
		userTorrentDao.persist(userTorrent);
		return userTorrent;
	}*/

//	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public void addUserEpisode(long torrentId) {
		User user = userDao.find(sessionService.getLoggedInUserId());
		Torrent torrent = torrentDao.find(torrentId);
//		addUserTorrent(user, torrent);
		if (user.getSubtitles() != null) {
			Episode episode = episodeDao.find(torrent);
			subtitlesService.downloadEpisodeSubtitles(torrent, episode, user.getSubtitles());
		}
	}

//	@Override
//	@Transactional(propagation = Propagation.REQUIRED)
//	public void removeUserMovie(long movieId) {
//		ignoreUserTorrent(Collections.singletonList(movieId));
//	}

//	@Override
	// todo
//	@Transactional(propagation = Propagation.REQUIRED)
//	public void ignoreUserTorrent(List<Long> torrentIds) {
//		User user = userDao.find(sessionService.getLoggedInUserId());
//		userTorrentDao.findUserMovies(latestMovies, loggedInUser)
//		for (Long torrentId : torrentIds) {
//			UserTorrent userTorrent = userTorrentDao.findMovieUserTorrentByTorrentId(torrentId, user);
//			if (userTorrent == null) {
//				Torrent torrent = torrentDao.find(torrentId);
////				userTorrent = addUserTorrent(user, torrent);
//			}
//			userTorrent.setIgnored(true);
//		}
//	}

//	@Override
	/*@Transactional(propagation = Propagation.REQUIRED) // cuz caching what was found
	public ArrayList<UserTorrentVO> search(EpisodeRequest episodeRequest) {
		ArrayList<UserTorrentVO> result = new ArrayList<>();
		Collection<Episode> downloaded = torrentEntriesDownloader.download(Collections.singleton(episodeRequest)).getDownloaded();

		Map<Torrent, Episode> episodeByTorrents = new HashMap<>();
		final Map<Torrent, Episode> episodeByTorrentsForComparator = new HashMap<>();
		for (Episode episode : downloaded) {
			for (Long torrentId : episode.getTorrentIds()) {
				Torrent torrent = torrentDao.find(torrentId);
				episodeByTorrents.put(torrent, episode);
				episodeByTorrentsForComparator.put(torrent, episode);
			}
		}

		// add those containing user torrent
		for (UserTorrent userTorrent : userTorrentDao.findUserEpisodes(downloaded)) {
			episodeByTorrents.remove(userTorrent.getTorrent());
			UserTorrentVO lwUserEpisode = new UserTorrentVO(userTorrent.getTorrent(), true);
			result.add(lwUserEpisode);
		}

		// add the rest of the episodes
		for (Torrent torrent : episodeByTorrents.keySet()) {
			UserTorrentVO lwUserEpisode = new UserTorrentVO(torrent, false);
			result.add(lwUserEpisode);
		}

		final EpisodesComparator episodesComparator = new EpisodesComparator();
		Collections.sort(result, new Comparator<UserTorrentVO>() {
			@Override
			public int compare(UserTorrentVO o1, UserTorrentVO o2) {
				Episode episode1 = episodeByTorrentsForComparator.get(o1.getTorrent());
				Episode episode2 = episodeByTorrentsForComparator.get(o2.getTorrent());
				return episodesComparator.compare(episode1, episode2);
			}
		});

		return result;
	}*/

//	@Override
//	public DummyWhiteListSerialization dummy(DummyWhiteListSerialization dummy) {
//		return null;
//	}

//	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public void addShow(Show show) {
		User user = userDao.find(sessionService.getLoggedInUserId());
		Show showFromDb = showDao.find(show.getId());
		user.getShows().add(showFromDb);
	}

//	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public void removeShow(Show show) {
		User user = userDao.find(sessionService.getLoggedInUserId());
		Show showFromDb = showDao.find(show.getId());
		user.getShows().remove(showFromDb);
	}

//	@Override
	/*@Transactional(propagation = Propagation.REQUIRED)
	public void subtitles(SubtitleLanguage language) {
		User user = userDao.find(sessionService.getLoggedInUserId());
		user.setSubtitles(language);
	}*/

//	@Override
	/*@Transactional(propagation = Propagation.REQUIRED)
	public RssImportResponse showRssImport(String username, String password) {
		try {
			User user = userDao.find(sessionService.getLoggedInUserId());

			if (!showRssService.validateCredentials(username, password)) {
				return new RssImportResponse().withErrorMessage("Invalid credentials");
			}

			log.info("Importing user " + username + " from showRss into " + user.getEmail());
			Collection<String> shows = showRssService.getShows(username, password);
			for (String showName : shows) {
				Show show = showService.findShow(showName);
				user.getShows().add(show);
			}

			log.info("Imported " + shows.size() + " shows for user " + user.getEmail());

			return new RssImportResponse().withUser(createUserResponse(user));
		} catch (ConnectionTimeoutException e) {
			return new RssImportResponse().withErrorMessage(e.getMessage());
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw e;
		}
	}*/
}