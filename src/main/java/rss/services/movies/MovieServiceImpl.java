package rss.services.movies;

import org.apache.commons.lang3.StringEscapeUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;
import rss.controllers.EntityConverter;
import rss.controllers.vo.DownloadStatus;
import rss.controllers.vo.UserMovieStatus;
import rss.controllers.vo.UserMovieVO;
import rss.dao.MovieDao;
import rss.dao.TorrentDao;
import rss.dao.UserTorrentDao;
import rss.entities.*;
import rss.services.PageDownloader;
import rss.services.SessionService;
import rss.services.downloader.MoviesTorrentEntriesDownloader;
import rss.services.log.LogService;
import rss.util.DurationMeter;

import java.io.Serializable;
import java.util.*;
import java.util.regex.Matcher;

/**
 * User: dikmanm
 * Date: 08/03/13 15:38
 */
@Service
public class MovieServiceImpl implements MovieService {

	private static final String IMDB_URL = "http://www.imdb.com/title/";

	@Autowired
	private SessionService sessionService;

	@Autowired
	private UserTorrentDao userTorrentDao;

	@Autowired
	private MovieDao movieDao;

	@Autowired
	private TorrentDao torrentDao;

	@Autowired
	private PageDownloader pageDownloader;

	@Autowired
	private LogService logService;

	@Autowired
	private EntityConverter entityConverter;

	@Autowired
	private TorrentzService torrentzService;

	@Autowired
	private TransactionTemplate transactionTemplate;

	@Transactional(propagation = Propagation.REQUIRED)
	public String getImdbPreviewPage(Movie movie) {
		String page;
		try {
			DurationMeter durationMeter = new DurationMeter();
			page = pageDownloader.downloadPage(movie.getImdbUrl());
			page = cleanImdbPage(movie.getName(), page);
			durationMeter.stop();
			logService.debug(getClass(), "IMDB page download for movie " + movie.getName() + " took " + durationMeter.getDuration() + " millis");
		} catch (Exception e) {
			page = null;
			logService.error(getClass(), e.getMessage(), e);
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

	@Transactional(propagation = Propagation.REQUIRED)
	public ArrayList<UserMovieVO> getFutureUserMovies(User user) {
		List<UserMovie> futureUserMovies = movieDao.findFutureUserMovies(user);
		Collections.sort(futureUserMovies, new Comparator<UserMovie>() {
			@Override
			public int compare(UserMovie o1, UserMovie o2) {
				return o2.getUpdated().compareTo(o1.getUpdated());
			}
		});

		ArrayList<UserMovieVO> result = new ArrayList<>();
		for (UserMovie userMovie : futureUserMovies) {
			result.add(entityConverter.toFutureMovie(userMovie.getMovie()).withScheduledOn(userMovie.getUpdated()));
		}
		return result;
	}

	@Transactional(propagation = Propagation.REQUIRED)
	public ArrayList<UserMovieVO> getUserMovies(User user) {
		Map<Long, Torrent> torrentsByIds = new HashMap<>();
		Set<Movie> latestMovies = getLatestMovies();
		MoviesToTorrentsMapper moviesMapper = new MoviesToTorrentsMapper(latestMovies);
		UserMoviesVOContainer userMoviesVOContainer = new UserMoviesVOContainer();

		// get all userMovies related to the latest movie "names"
		for (UserTorrent userTorrent : userTorrentDao.findUserMovies(latestMovies, user)) {
			Torrent torrent = userTorrent.getTorrent();
			torrentsByIds.put(torrent.getId(), torrent);
			Movie movie = moviesMapper.getMovie(torrent);
			UserMovieVO userMovieVO = userMoviesVOContainer.getUserMovie(movie);
			userMovieVO.addTorrentDownloadStatus(UserMovieStatus.fromUserTorrent(userTorrent).withViewed(true));
			updateLatestUploadDate(torrent, userMovieVO);
		}

		for (UserTorrent userTorrent : userTorrentDao.findScheduledUserMovies(user)) {
			Torrent torrent = userTorrent.getTorrent();
			if (!torrentsByIds.containsKey(torrent.getId())) {
				torrentsByIds.put(torrent.getId(), torrent);
				Movie movie = movieDao.find(torrent);
				UserMovieVO userMovieVO = userMoviesVOContainer.getUserMovie(movie);
				userMovieVO.addTorrentDownloadStatus(UserMovieStatus.fromUserTorrent(userTorrent).withViewed(true));
				updateLatestUploadDate(torrent, userMovieVO);
			}
		}

		// add movies that had no userMovies
		for (Movie movie : latestMovies) {
			UserMovieVO userMovieVO = userMoviesVOContainer.getUserMovie(movie);

			// if at least one is not viewed - show as not viewed
			for (Long torrentId : movie.getTorrentIds()) {
				if (!torrentsByIds.containsKey(torrentId)) {
					Torrent torrent = torrentDao.find(torrentId);
					torrentsByIds.put(torrent.getId(), torrent);
					userMovieVO.addTorrentDownloadStatus(new UserMovieStatus(DownloadStatus.NONE)
							.withTitle(torrent.getTitle())
							.withTorrentId(torrent.getId())
							.withUploadedDate(torrent.getDateUploaded())
							.withScheduledOn(null));
					updateLatestUploadDate(torrent, userMovieVO);
				}
			}
		}

		// sort and set viewed status
		ArrayList<UserMovieVO> result = new ArrayList<>(userMoviesVOContainer.getUserMovies());

		UserMovieStatusComparator comparator = new UserMovieStatusComparator(torrentsByIds);
		for (UserMovieVO userMovieVO : result) {
			Collections.sort(userMovieVO.getTorrents(), comparator);

			// now the first element is the newest
			userMovieVO.setViewed(false);
			if (!userMovieVO.getTorrents().isEmpty()) {
				UserMovie userMovie = movieDao.findUserMovie(userMovieVO.getId(), user);

				// userMovie is viewed only if all its torrents date are before the last movie view date
				Torrent torrent = torrentsByIds.get(userMovieVO.getTorrents().get(0).getTorrentId());
				if (userMovie != null && torrent.getDateUploaded().before(userMovie.getUpdated())) {
					userMovieVO.setViewed(true);
				}

				for (UserMovieStatus userMovieStatus : userMovieVO.getTorrents()) {
					torrent = torrentsByIds.get(userMovieStatus.getTorrentId());
					if (userMovie != null && torrent.getDateUploaded().before(userMovie.getUpdated())) {
						userMovieStatus.withViewed(true);
					}
				}
			}
		}

		Collections.sort(result, new Comparator<UserMovieVO>() {
			@Override
			public int compare(UserMovieVO o1, UserMovieVO o2) {
				Date o1LatestUploadDate = o1.getLatestUploadDate();
				Date o2LatestUploadDate = o2.getLatestUploadDate();
				if (o1LatestUploadDate.before(o2LatestUploadDate)) {
					return 1;
				} else if (o1LatestUploadDate.after(o2LatestUploadDate)) {
					return -1;
				} else {
					return o1.getTitle().compareTo(o2.getTitle());
				}
			}
		});
		return result;
	}


	private void updateLatestUploadDate(Torrent torrent, UserMovieVO userMovieVO) {
		Date cur = torrent.getDateUploaded();
		if (userMovieVO.getLatestUploadDate() == null || userMovieVO.getLatestUploadDate().before(cur)) {
			userMovieVO.setLatestUploadDate(cur);
		}
	}

	private Set<Movie> getLatestMovies() {
		Calendar c = Calendar.getInstance();
		c.setTime(sessionService.getPrevLoginDate());
		c.add(Calendar.DAY_OF_MONTH, -7);
		Date uploadedFromDate = c.getTime();
		return new HashSet<>(movieDao.findUploadedSince(uploadedFromDate));
	}

	public class UserMoviesVOContainer {
		private Map<String, UserMovieVO> lwUserMovies;

		public UserMoviesVOContainer() {
			lwUserMovies = new HashMap<>();
		}

		public UserMovieVO getUserMovie(Movie movie) {
			UserMovieVO userMovieVO = lwUserMovies.get(movie.getName());
			if (userMovieVO == null) {
				userMovieVO = new UserMovieVO()
						.withId(movie.getId())
						.withTitle(movie.getName())
						.withImdbUrl(movie.getImdbUrl());
				lwUserMovies.put(movie.getName(), userMovieVO);
			}
			return userMovieVO;
		}

		public boolean contains(Movie movie) {
			return lwUserMovies.containsKey(movie.getName());
		}

		public Collection<UserMovieVO> getUserMovies() {
			return lwUserMovies.values();
		}
	}

	public class MoviesToTorrentsMapper {
		private Map<Long, Movie> movieByTorrents = new HashMap<>();

		public MoviesToTorrentsMapper(Set<Movie> latestMovies) {
			for (Movie movie : latestMovies) {
				for (Long torrentId : movie.getTorrentIds()) {
					movieByTorrents.put(torrentId, movie);
				}
			}
		}

		public Movie getMovie(Torrent torrent) {
			return movieByTorrents.get(torrent.getId());
		}
	}

	public static class UserMovieStatusComparator implements Comparator<UserMovieStatus>, Serializable {
		private static final long serialVersionUID = -2265824299212043336L;

		private Map<Long, Torrent> torrentsByIds;

		public UserMovieStatusComparator(Map<Long, Torrent> torrentsByIds) {
			this.torrentsByIds = torrentsByIds;
		}

		@Override
		public int compare(UserMovieStatus o1, UserMovieStatus o2) {
			Torrent o2Torrent = torrentsByIds.get(o2.getTorrentId());
			Torrent o1Torrent = torrentsByIds.get(o1.getTorrentId());
			int i = o2Torrent.getDateUploaded().compareTo(o1Torrent.getDateUploaded());
			if (i != 0) {
				return i;
			}

			return o1Torrent.compareTo(o2Torrent);
		}
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public Movie addFutureMovieDownload(User user, String imdbId) {
		final String imdbUrl = IMDB_URL + imdbId;
		Movie movie = movieDao.findByImdbUrl(imdbUrl);
		if (movie == null) {
			String partialPage;
			try {
				partialPage = pageDownloader.downloadPageUntilFound(imdbUrl, MoviesTorrentEntriesDownloader.VIEWERS_PATTERN);
			} catch (Exception e) {
				// usually it is HTTP/1.1 404 Not Found
				if (!e.getMessage().contains("404 Not Found")) {
					logService.error(getClass(), "Failed downloading IMDB page " + imdbId + ": " + e.getMessage(), e);
				}
				return null;
			}
			Matcher oldYearMatcher = MoviesTorrentEntriesDownloader.OLD_YEAR_PATTERN.matcher(partialPage);
			oldYearMatcher.find();
			String name = oldYearMatcher.group(1);
			name = StringEscapeUtils.unescapeHtml4(name);

			final String finalName = name;
			movie = transactionTemplate.execute(new TransactionCallback<Movie>() {
				@Override
				public Movie doInTransaction(TransactionStatus arg0) {
					Movie movie = new Movie(finalName, imdbUrl);
					movieDao.persist(movie);
					return movie;
				}
			});

			// uses a separate transaction
			torrentzService.downloadMovie(movie);
		}

		UserMovie userMovie = movieDao.findUserMovie(movie.getId(), user);
		if (userMovie == null) {
			userMovie = new UserMovie();
			userMovie.setMovie(movie);
			userMovie.setUser(user);
			userMovie.setUpdated(new Date());
			movieDao.persist(userMovie);
		}

		return movie;
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public void markMovieViewed(User user, long movieId) {
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
