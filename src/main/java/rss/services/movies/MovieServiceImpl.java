package rss.services.movies;

import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;
import rss.MediaRSSException;
import rss.controllers.vo.DownloadStatus;
import rss.controllers.vo.UserMovieTorrentVO;
import rss.controllers.vo.UserMovieVO;
import rss.dao.MovieDao;
import rss.dao.TorrentDao;
import rss.dao.UserTorrentDao;
import rss.dao.ViewDao;
import rss.entities.*;
import rss.services.SessionService;
import rss.services.downloader.DownloadResult;
import rss.services.downloader.MovieTorrentsDownloader;
import rss.services.log.LogService;
import rss.services.requests.movies.MovieRequest;
import rss.services.requests.subtitles.SubtitlesMovieRequest;
import rss.services.requests.subtitles.SubtitlesRequest;
import rss.services.searchers.composite.torrentz.TorrentzParser;
import rss.services.searchers.composite.torrentz.TorrentzParserImpl;
import rss.services.searchers.composite.torrentz.TorrentzResult;
import rss.services.subtitles.SubtitlesService;
import rss.util.DateUtils;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;
import java.util.concurrent.*;

/**
 * User: dikmanm
 * Date: 08/03/13 15:38
 */
@Service
public class MovieServiceImpl implements MovieService {

	private static final String IMDB_URL = "http://www.imdb.com/title/";
	public static final int USER_MOVIES_DISPLAY_DAYS_HISTORY = 14;

	@Autowired
	private SessionService sessionService;

	@Autowired
	private UserTorrentDao userTorrentDao;

	@Autowired
	private MovieDao movieDao;

	@Autowired
	private TorrentDao torrentDao;

	@Autowired
	private IMDBService imdbService;

	@Autowired
	private LogService logService;

	@Autowired
	private MovieTorrentsDownloader moviesTorrentEntriesDownloader;

	@Autowired
	private TransactionTemplate transactionTemplate;

	@Autowired
	private TorrentzParser torrentzParser;

	@Autowired
	private ViewDao viewDao;

	@Autowired
	private SubtitlesService subtitlesService;

	@Transactional(propagation = Propagation.REQUIRED)
	public long getUserMoviesCount(User user) {
		return movieDao.findUserMoviesIds(user, USER_MOVIES_DISPLAY_DAYS_HISTORY).size();
	}

	@Transactional(propagation = Propagation.REQUIRED)
	public ArrayList<UserMovieVO> getUserMovies(User user) {
		Map<Long, Torrent> torrentsByIds = new HashMap<>();
		UserMoviesVOContainer userMoviesVOContainer = new UserMoviesVOContainer();
		Map<Long, Movie> movies = new HashMap<>();

		for (UserMovie userMovie : movieDao.findUserMovies(user, USER_MOVIES_DISPLAY_DAYS_HISTORY)) {
			UserMovieVO userMovieVO = userMoviesVOContainer.getUserMovie(userMovie.getMovie());
			userMovieVO.withScheduledOn(userMovie.getUpdated());
			userMovieVO.withAdded(userMovie.getUpdated());
			userMovieVO.setViewed(true);
			// if there are not torrents at all yet, it is a future movie
			if (userMovie.getMovie().getTorrentIds().isEmpty()) {
				userMovieVO.setDownloadStatus(DownloadStatus.FUTURE);
			}
			movies.put(userMovie.getMovie().getId(), userMovie.getMovie());
		}

		// add UserMovieTorrents to those movies
		for (UserMovieTorrent userTorrent : userTorrentDao.findUserMovieTorrents(user, movies.keySet())) {
			Movie movie = userTorrent.getUserMovie().getMovie();
			UserMovieVO userMovieVO = userMoviesVOContainer.getUserMovie(movie);
			userMovieVO.addUserMovieTorrent(UserMovieTorrentVO.fromUserTorrent(userTorrent).withViewed(true), userTorrent.getTorrent().getDateUploaded());
			torrentsByIds.put(userTorrent.getTorrent().getId(), userTorrent.getTorrent());
		}

		// for all the movies add the rest of the torrents (which are not in userTorrents)
		enrichWithNonUserTorrents(user, movies.values(), userMoviesVOContainer, torrentsByIds);

		ArrayList<UserMovieVO> result = new ArrayList<>(userMoviesVOContainer.getUserMovies());

		UserMovieStatusComparator comparator = new UserMovieStatusComparator(torrentsByIds);
		for (UserMovieVO userMovieVO : result) {
			Collections.sort(userMovieVO.getTorrents(), comparator);
		}

		Collections.sort(result, new Comparator<UserMovieVO>() {
			@Override
			public int compare(UserMovieVO o1, UserMovieVO o2) {
				return o2.getAdded().compareTo(o1.getAdded());
			}
		});

		return result;
	}

	@Transactional(propagation = Propagation.REQUIRED)
	public ArrayList<UserMovieVO> getAvailableMovies(User user) {
		Map<Long, Torrent> torrentsByIds = new HashMap<>();
		Set<Movie> latestMovies = getLatestMovies();
		UserMoviesVOContainer userMoviesVOContainer = new UserMoviesVOContainer();

		// get all userMovieTorrents related to the latest movie "names"
		for (UserMovieTorrent userTorrent : userTorrentDao.findUserMovies(user, latestMovies)) {
			Torrent torrent = userTorrent.getTorrent();
			torrentsByIds.put(torrent.getId(), torrent);
			Movie movie = userTorrent.getUserMovie().getMovie();
			UserMovieVO userMovieVO = userMoviesVOContainer.getUserMovie(movie);
			userMovieVO.addUserMovieTorrent(UserMovieTorrentVO.fromUserTorrent(userTorrent).withViewed(true), torrent.getDateUploaded());
		}

		// add movies that had no userMovieTorrents
		enrichWithNonUserTorrents(user, latestMovies, userMoviesVOContainer, torrentsByIds);

		// sort and set viewed status
		ArrayList<UserMovieVO> result = new ArrayList<>(userMoviesVOContainer.getUserMovies());

		UserMovieStatusComparator comparator = new UserMovieStatusComparator(torrentsByIds);
		for (UserMovieVO userMovieVO : result) {
			Collections.sort(userMovieVO.getTorrents(), comparator);
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

	private Set<Movie> getLatestMovies() {
		return new HashSet<>(movieDao.findUploadedSince(DateUtils.getPastDate(sessionService.getPrevLoginDate(), 7)));
	}

	private void enrichWithNonUserTorrents(User user, Collection<Movie> movies, UserMoviesVOContainer userMoviesVOContainer, Map<Long, Torrent> torrentsByIds) {
		for (Movie movie : movies) {
			UserMovieVO userMovieVO = userMoviesVOContainer.getUserMovie(movie);

			boolean areAllViewed = true;
			View view = viewDao.find(user, movie.getId());

			for (Long torrentId : movie.getTorrentIds()) {
				if (!torrentsByIds.containsKey(torrentId)) {
					// todo: if slow optimize to one query per movie
					Torrent torrent = torrentDao.find(torrentId);
					torrentsByIds.put(torrent.getId(), torrent);

					boolean isViewed = view != null && view.getCreated().after(torrent.getCreated());
					userMovieVO.addUserMovieTorrent(UserMovieTorrentVO.fromTorrent(torrent, movie.getId()).withViewed(isViewed), torrent.getDateUploaded());
					areAllViewed &= isViewed;
				}
			}

			userMovieVO.setViewed(areAllViewed);
		}
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
						.withTitle(movie.getName());
//						.withImdbUrl(movie.getImdbUrl());
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

	public static class UserMovieStatusComparator implements Comparator<UserMovieTorrentVO>, Serializable {
		private static final long serialVersionUID = -2265824299212043336L;

		private Map<Long, Torrent> torrentsByIds;

		public UserMovieStatusComparator(Map<Long, Torrent> torrentsByIds) {
			this.torrentsByIds = torrentsByIds;
		}

		@Override
		public int compare(UserMovieTorrentVO o1, UserMovieTorrentVO o2) {
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
	public void addMovieDownload(User user, long movieId, long torrentId) {
		Movie movie = movieDao.find(movieId);
		Torrent torrent = torrentDao.find(torrentId);
		UserMovie userMovie = movieDao.findUserMovie(movie.getId(), user);
		if (userMovie == null) {
			userMovie = createUserMovie(user, movie);
		}

		UserMovieTorrent userTorrent = new UserMovieTorrent();
		userTorrent.setUser(user);
		userTorrent.setAdded(new Date());
		userTorrent.setTorrent(torrent);
		userTorrentDao.persist(userTorrent);

		userMovie.getUserMovieTorrents().add(userTorrent);
		userTorrent.setUserMovie(userMovie);

		if (user.getSubtitles() != null) {
			SubtitlesMovieRequest smr = new SubtitlesMovieRequest(torrent, movie, Collections.singletonList(user.getSubtitles()));
			subtitlesService.downloadSubtitlesAsync(Collections.<SubtitlesRequest>singletonList(smr));
		}

		logService.info(getClass(), "User " + user + " downloads '" + userMovie.getMovie() + "'");
	}

	// boolean: true if already exists, false if new
	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public Pair<Movie, Boolean> addFutureMovieDownload(User user, String imdbId) {
		try {
			// just if someone entered some junk in the imdbid, better get 404 than malformed url
			final String imdbUrl = IMDB_URL + URLEncoder.encode(imdbId, "UTF-8");
			Movie movie = movieDao.findByImdbUrl(imdbUrl);
			if (movie == null) {
				final IMDBParseResult imdbParseResult = imdbService.downloadMovieFromIMDBAndImagesAsync(imdbUrl);
				if (!imdbParseResult.isFound()) {
					return null;
				}

				// persisting the movie in a separate transaction cuz need the movie to be present then the downloader runs
				// in order to have a separate transaction, needed a new thread here
				FutureTask<Movie> futureTask = new FutureTask<>(new Callable<Movie>() {
					@Override
					public Movie call() throws Exception {
						return transactionTemplate.execute(new TransactionCallback<Movie>() {
							@Override
							public Movie doInTransaction(TransactionStatus arg0) {
								Movie movie = new Movie(imdbParseResult.getName(), imdbUrl, imdbParseResult.getYear());
								movieDao.persist(movie);
								return movie;
							}
						});
					}
				});
				ExecutorService executorService = Executors.newSingleThreadExecutor();
				executorService.submit(futureTask);
				executorService.shutdown();
				movie = futureTask.get();
			}

			if (movie.getTorrentIds().isEmpty()) {
				MovieRequest movieRequest = new MovieRequest(movie.getName(), null);
				movieRequest.setImdbId(imdbUrl);
				moviesTorrentEntriesDownloader.download(Collections.singleton(movieRequest));

				// re-fetch the movie in this transaction after it got torrents
				movie = movieDao.find(movie.getId());
			}

			// if movie is too old and there are no torrents now - then there won't be any. no point adding it to user as scheduled
			Calendar c = Calendar.getInstance();
			if (movie.getYear() < c.get(Calendar.YEAR) - 1 && movie.getTorrentIds().isEmpty()) {
				throw new MediaRSSException("Movie is old, was unable to find torrents for movie '" + movie.getName() + "'").doNotLog();
			}

			boolean isExists = true;
			UserMovie userMovie = movieDao.findUserMovie(movie.getId(), user);
			if (userMovie == null) {
				createUserMovie(user, movie);
				isExists = false;
			}
			return new MutablePair<>(movie, isExists);
		} catch (InterruptedException | ExecutionException | UnsupportedEncodingException e) {
			throw new MediaRSSException(e.getMessage(), e);
		}
	}

	private UserMovie createUserMovie(User user, Movie movie) {
		UserMovie userMovie = new UserMovie();
		userMovie.setMovie(movie);
		userMovie.setUser(user);
		userMovie.setUpdated(new Date());
		movieDao.persist(userMovie);
		return userMovie;
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public void markMovieViewed(User user, long movieId) {
		View view = viewDao.find(user, movieId);
		if (view == null) {
			view = new View();
			view.setUser(user);
			view.setObjectId(movieId);
			viewDao.persist(view);
		}
		view.setCreated(new Date());
	}

	@Override
	public DownloadResult<Movie, MovieRequest> downloadLatestMovies() {
		logService.info(getClass(), "Downloading '" + TorrentzParserImpl.TORRENTZ_LATEST_MOVIES_URL + "1d" + "'");
		Set<TorrentzResult> torrentzResults = torrentzParser.downloadByUrl(TorrentzParserImpl.TORRENTZ_LATEST_MOVIES_URL + "1d");

		// retry with 7 days
		if (torrentzResults.isEmpty()) {
			logService.info(getClass(), "Nothing found, downloading '" + TorrentzParserImpl.TORRENTZ_LATEST_MOVIES_URL + "7d" + "'");
			torrentzResults = torrentzParser.downloadByUrl(TorrentzParserImpl.TORRENTZ_LATEST_MOVIES_URL + "7d");
		}

		List<MovieRequest> movieRequests = new ArrayList<>();
		// filter out old year movies
		int curYear = Calendar.getInstance().get(Calendar.YEAR);
		int prevYear = curYear - 1;
		for (TorrentzResult torrentzResult : torrentzResults) {
			String name = torrentzResult.getTitle();
			if (!name.contains(String.valueOf(curYear)) && !name.contains(String.valueOf(prevYear))) {
				logService.info(getClass(), "Skipping movie '" + name + "' due to old year");
			} else {
				movieRequests.add(new MovieRequest(torrentzResult.getTitle(), torrentzResult.getHash()));
			}
		}

		return moviesTorrentEntriesDownloader.download(movieRequests);
	}
}
