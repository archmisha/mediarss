package rss.services.movies;

import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import rss.MediaRSSException;
import rss.controllers.vo.DownloadStatus;
import rss.controllers.vo.UserMovieTorrentVO;
import rss.controllers.vo.UserMovieVO;
import rss.dao.MovieDao;
import rss.dao.TorrentDao;
import rss.dao.UserTorrentDao;
import rss.entities.*;
import rss.services.SessionService;
import rss.services.downloader.DownloadConfig;
import rss.services.downloader.DownloadResult;
import rss.services.downloader.LatestMoviesDownloader;
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
import rss.util.DurationMeter;

import javax.annotation.PostConstruct;
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

	public static final int USER_MOVIES_DISPLAY_DAYS_HISTORY = 14;
	public static final int DAYS_TORRENT_CONSIDERED_NEW = 7;

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
	private LatestMoviesDownloader latestMoviesDownloader;

	@Autowired
	private MovieTorrentsDownloader movieTorrentsDownloader;

	@Autowired
	private TransactionTemplate transactionTemplate;

	@Autowired
	private TorrentzParser torrentzParser;

//	@Autowired
//	private ViewDao viewDao;

	@Autowired
	private SubtitlesService subtitlesService;

	@Autowired
	private TopMoviesService topMoviesService;

	@Autowired
	private IMDBPreviewCacheService imdbPreviewCacheService;

	private ConcurrentMap<Movie, Movie> moviesBeingSearched = new ConcurrentHashMap<>();

	@PostConstruct
	@Transactional(propagation = Propagation.REQUIRED)
	public void postConstruct() {
		ExecutorService executorService = Executors.newSingleThreadExecutor();
		executorService.submit(new Runnable() {
			@Override
			public void run() {
				try {
					transactionTemplate.execute(new TransactionCallbackWithoutResult() {
						@Override
						protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
							logService.info(MovieServiceImpl.class, "Loading movies previews into cache");
							DurationMeter duration = new DurationMeter();
							Collection<Movie> latestMovies = movieDao.findOrderedByUploadedSince(IMDBPreviewCacheServiceImpl.MAX_MOVIE_PREVIEWS_CACHE);
							for (Movie movie : latestMovies) {
								getImdbPreviewPage(movie);
							}
							logService.info(MovieServiceImpl.class, "Loaded movies previews into cache (" + duration.getDuration() + " ms)");
						}
					});
				} catch (TransactionException e) {
					logService.error(MovieServiceImpl.class, "Failed pre-loading movies previews: " + e.getMessage(), e);
				}
			}
		});
		executorService.shutdown();
	}

	public List<UserMovieVO> getSearchCompletedMovies(long[] ids) {
		UserMoviesVOContainer userMoviesVOContainer = new UserMoviesVOContainer();
		Map<Long, Torrent> torrentsByIds = new HashMap<>();

		for (long id : ids) {
			Movie movie = movieDao.find(id);
			if (!moviesBeingSearched.containsKey(movie)) {
				UserMovieVO userMovieVO = userMoviesVOContainer.getUserMovie(movie);
				for (Torrent torrent : torrentDao.find(movie.getTorrentIds())) {
					userMovieVO.addUserMovieTorrent(UserMovieTorrentVO.fromTorrent(torrent, movie.getId()).withViewed(false)/*, torrent.getDateUploaded()*/);
					torrentsByIds.put(torrent.getId(), torrent);
				}
			}
		}

		ArrayList<UserMovieVO> result = new ArrayList<>(userMoviesVOContainer.getUserMovies());

		UserMovieStatusComparator comparator = new UserMovieStatusComparator(torrentsByIds);
		for (UserMovieVO userMovieVO : result) {
			Collections.sort(userMovieVO.getViewedTorrents(), comparator);
			Collections.sort(userMovieVO.getNotViewedTorrents(), comparator);
		}

		return result;
	}

//	@Transactional(propagation = Propagation.REQUIRED)
//	public int getUserMoviesCount(User user) {
//		return movieDao.findUserMoviesCount(user, USER_MOVIES_DISPLAY_DAYS_HISTORY);
//	}

//	@Override
//	public int getAvailableMoviesCount(User user) {
//		return movieDao.findUploadedSinceCount(DateUtils.getPastDate(sessionService.getPrevLoginDate(), 7));
//	}

	@Transactional(propagation = Propagation.REQUIRED)
	public List<UserMovieVO> getUserMovies(User user) {
		Map<Long, Torrent> torrentsByIds = new HashMap<>();
		UserMoviesVOContainer userMoviesVOContainer = new UserMoviesVOContainer();
		Map<Long, Movie> movies = new HashMap<>();

		for (UserMovie userMovie : movieDao.findUserMovies(user, USER_MOVIES_DISPLAY_DAYS_HISTORY)) {
			UserMovieVO userMovieVO = userMoviesVOContainer.getUserMovie(userMovie.getMovie());
			userMovieVO.withScheduledOn(userMovie.getUpdated());
			userMovieVO.withAdded(userMovie.getUpdated());
			// if there are not torrents at all yet, it is a future movie or being searched right now or an old movie without torrents
			if (userMovie.getMovie().getTorrentIds().isEmpty()) {
				if (moviesBeingSearched.containsKey(userMovie.getMovie())) {
					userMovieVO.setDownloadStatus(DownloadStatus.BEING_SEARCHED);
				} else if (isOldMovie(userMovie.getMovie())) {
					userMovieVO.setDownloadStatus(DownloadStatus.OLD);
				} else {
					userMovieVO.setDownloadStatus(DownloadStatus.FUTURE);
				}
			}
			movies.put(userMovie.getMovie().getId(), userMovie.getMovie());
		}

		ArrayList<UserMovieVO> result = populateUserMovieTorrents(user, torrentsByIds, movies.values(), userMoviesVOContainer);

		Collections.sort(result, new Comparator<UserMovieVO>() {
			@Override
			public int compare(UserMovieVO o1, UserMovieVO o2) {
				return o2.getAdded().compareTo(o1.getAdded());
			}
		});

		return result;
	}

	@Transactional(propagation = Propagation.REQUIRED)
	public List<UserMovieVO> getAvailableMovies(User user) {
		Map<Long, Torrent> torrentsByIds = new HashMap<>();
		UserMoviesVOContainer userMoviesVOContainer = new UserMoviesVOContainer();

		Collection<Movie> latestMovies = getLatestMovies();
		Collection<Movie> topMovies = getTopMovies();

		// filter out duplicates
		Set<Movie> movies = new HashSet<>();
		movies.addAll(latestMovies);
		movies.addAll(topMovies);

		ArrayList<UserMovieVO> result = populateUserMovieTorrents(user, torrentsByIds, movies, userMoviesVOContainer);

		Collections.sort(result, new Comparator<UserMovieVO>() {
			@Override
			public int compare(UserMovieVO o1, UserMovieVO o2) {
				Date o1ReleaseDate = o1.getReleaseDate();
				Date o2ReleaseDate = o2.getReleaseDate();
				if (o1ReleaseDate != null && o2ReleaseDate != null) {
					if (o1ReleaseDate.before(o2ReleaseDate)) {
						return 1;
					} else if (o1ReleaseDate.after(o2ReleaseDate)) {
						return -1;
					}
				}

				return o1.getTitle().compareTo(o2.getTitle());
			}
		});
		return result;
	}

	private Collection<Movie> getTopMovies() {
		return topMoviesService.getTopMovies();
	}

	private ArrayList<UserMovieVO> populateUserMovieTorrents(User user,
															 Map<Long, Torrent> torrentsByIds,
															 Collection<Movie> movies,
															 UserMoviesVOContainer userMoviesVOContainer) {
		// get all userMovieTorrents related to the given movies
		for (UserMovieTorrent userTorrent : userTorrentDao.findUserMovieTorrents(user, movies)) {
			Movie movie = userTorrent.getUserMovie().getMovie();
			UserMovieVO userMovieVO = userMoviesVOContainer.getUserMovie(movie);
			boolean isViewed = !DateUtils.isWithinDaysPast(userTorrent.getTorrent().getDateUploaded(), DAYS_TORRENT_CONSIDERED_NEW);
			userMovieVO.addUserMovieTorrent(UserMovieTorrentVO.fromUserTorrent(userTorrent).withViewed(isViewed)/*, userTorrent.getTorrent().getDateUploaded()*/);
			torrentsByIds.put(userTorrent.getTorrent().getId(), userTorrent.getTorrent());
		}

		// add movies that had no userMovieTorrents
		enrichWithNonUserTorrents(movies, userMoviesVOContainer, torrentsByIds);

		ArrayList<UserMovieVO> result = new ArrayList<>(userMoviesVOContainer.getUserMovies());

		UserMovieStatusComparator comparator = new UserMovieStatusComparator(torrentsByIds);
		for (UserMovieVO userMovieVO : result) {
			Collections.sort(userMovieVO.getNotViewedTorrents(), comparator);
			Collections.sort(userMovieVO.getViewedTorrents(), comparator);
		}
		return result;
	}

	private Collection<Movie> getLatestMovies() {
		return movieDao.findUploadedSince(DateUtils.getPastDate(new Date(), 7));
	}

	private void enrichWithNonUserTorrents(Collection<Movie> movies, UserMoviesVOContainer userMoviesVOContainer, Map<Long, Torrent> torrentsByIds) {
		for (Movie movie : movies) {
			UserMovieVO userMovieVO = userMoviesVOContainer.getUserMovie(movie);

			for (Torrent torrent : torrentDao.find(org.apache.commons.collections.CollectionUtils.subtract(movie.getTorrentIds(), torrentsByIds.keySet()))) {
				torrentsByIds.put(torrent.getId(), torrent);
				boolean isViewed = !DateUtils.isWithinDaysPast(torrent.getDateUploaded(), DAYS_TORRENT_CONSIDERED_NEW);
				userMovieVO.addUserMovieTorrent(UserMovieTorrentVO.fromTorrent(torrent, movie.getId()).withViewed(isViewed)/*, torrent.getDateUploaded()*/);
			}
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
						.withTitle(movie.getName())
						.withReleaseDate(movie.getReleaseDate());
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
		UserMovie userMovie = movieDao.findUserMovie(user, movie.getId());
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
		userMovie.setUpdated(new Date()); // update user movie so it keeps being in the user movies list

		if (user.getSubtitles() != null) {
			SubtitlesMovieRequest smr = new SubtitlesMovieRequest(torrent, movie, Collections.singletonList(user.getSubtitles()));
			subtitlesService.downloadSubtitlesAsync(new HashSet<>(Arrays.<SubtitlesRequest>asList(smr)));
		}

		logService.info(getClass(), "User " + user + " downloads '" + userMovie.getMovie() + "'");
	}

	// boolean: true if already exists, false if new
	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public Pair<Movie, Boolean> addFutureMovieDownload(User user, String imdbId) {
		try {
			// just if someone entered some junk in the imdbid, better get 404 than malformed url
			final String imdbUrl = IMDBServiceImpl.IMDB_URL + URLEncoder.encode(imdbId, "UTF-8");
			Movie movie = movieDao.findByImdbUrl(imdbUrl);
			if (movie == null) {
				final IMDBParseResult imdbParseResult = imdbService.downloadMovieFromIMDBAndImagesAsync(imdbUrl);
				if (!imdbParseResult.isFound()) {
					return null;
				}

				// persisting the movie in a separate transaction cuz need the movie to be present when the downloader runs
				// in order to have a separate transaction, needed a new thread here
				FutureTask<Movie> futureTask = new FutureTask<>(new Callable<Movie>() {
					@Override
					public Movie call() throws Exception {
						return transactionTemplate.execute(new TransactionCallback<Movie>() {
							@Override
							public Movie doInTransaction(TransactionStatus arg0) {
								Movie movie = new Movie(imdbParseResult.getName(), imdbUrl, imdbParseResult.getYear(), imdbParseResult.getReleaseDate());
								addMovie(movie, imdbParseResult);
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
				ExecutorService executorService = Executors.newSingleThreadExecutor();
				final Movie finalMovie = movie;
				moviesBeingSearched.put(movie, movie);
				executorService.submit(new Runnable() {
					@Override
					public void run() {
						MovieRequest movieRequest = new MovieRequest(finalMovie.getName(), null);
						movieRequest.setImdbId(imdbUrl);
						DownloadConfig downloadConfig = new DownloadConfig();
						downloadConfig.setAsyncHeavy(false);
						downloadConfig.setForceDownload(false);
						movieTorrentsDownloader.download(new HashSet<>(Arrays.asList(movieRequest)), downloadConfig);
						moviesBeingSearched.remove(finalMovie);
					}
				});
				executorService.shutdown();

				// re-fetch the movie in this transaction after it got torrents
//				movie = movieDao.find(movie.getId());
			}

			// if movie is too old and there are no torrents now - then there won't be any. no point adding it to user as scheduled
//			if (isOldMovie(movie) && movie.getTorrentIds().isEmpty()) {
//				throw new MediaRSSException("Movie is old, was unable to find torrents for movie '" + movie.getName() + "'").doNotLog();
//			}

			boolean isExists = true;
			UserMovie userMovie = movieDao.findUserMovie(user, movie.getId());
			if (userMovie == null) {
				createUserMovie(user, movie);
				isExists = false;
			} else {
				// update userMovie date so it will show in my movies list on top for sure
				userMovie.setUpdated(new Date());
			}

			return new MutablePair<>(movie, isExists);
		} catch (InterruptedException | ExecutionException | UnsupportedEncodingException e) {
			throw new MediaRSSException(e.getMessage(), e);
		}
	}

	private boolean isOldMovie(Movie movie) {
		Calendar c = Calendar.getInstance();
		return movie.getYear() < c.get(Calendar.YEAR) - 1;
	}

	private UserMovie createUserMovie(User user, Movie movie) {
		UserMovie userMovie = new UserMovie();
		userMovie.setMovie(movie);
		userMovie.setUser(user);
		userMovie.setUpdated(new Date());
		movieDao.persist(userMovie);
		return userMovie;
	}

//	@Override
//	@Transactional(propagation = Propagation.REQUIRED)
//	public void markMovieViewed(User user, long movieId) {
//		View view = viewDao.find(user, movieId);
//		if (view == null) {
//			view = new View();
//			view.setUser(user);
//			view.setObjectId(movieId);
//			viewDao.persist(view);
//		}
//		view.setCreated(new Date());
//	}

	@Override
	public DownloadResult<Movie, MovieRequest> downloadLatestMovies() {
		logService.info(getClass(), "Downloading '" + TorrentzParserImpl.TORRENTZ_LATEST_MOVIES_URL + "1d" + "'");
		DurationMeter duration = new DurationMeter();
		Collection<TorrentzResult> torrentzResults = torrentzParser.downloadByUrl(TorrentzParserImpl.TORRENTZ_LATEST_MOVIES_URL + "1d");

		// retry with 7 days
		if (torrentzResults.isEmpty()) {
			logService.info(getClass(), "Nothing found, downloading '" + TorrentzParserImpl.TORRENTZ_LATEST_MOVIES_URL + "7d" + "'");
			torrentzResults = torrentzParser.downloadByUrl(TorrentzParserImpl.TORRENTZ_LATEST_MOVIES_URL + "7d");
		}

		Set<MovieRequest> movieRequests = new HashSet<>();
		// filter out old year movies
		int curYear = Calendar.getInstance().get(Calendar.YEAR);
		int prevYear = curYear - 1;
		for (TorrentzResult torrentzResult : torrentzResults) {
			String name = torrentzResult.getTitle();
			if (!name.contains(String.valueOf(curYear)) && !name.contains(String.valueOf(prevYear))) {
				logService.info(getClass(), "Skipping movie '" + name + "' due to old year");
			} else {
				MovieRequest movieRequest = new MovieRequest(torrentzResult.getTitle(), torrentzResult.getHash());
				movieRequest.setUploaders(torrentzResult.getUploaders());
				movieRequest.setSize(torrentzResult.getSize());
				movieRequests.add(movieRequest);
			}
		}

		DownloadResult<Movie, MovieRequest> downloadResult = latestMoviesDownloader.download(movieRequests, new DownloadConfig());

		duration.stop();
		logService.info(getClass(), "Downloading latest movies took " + duration.getDuration() + " ms");

		return downloadResult;
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public Collection<IMDBAutoCompleteItem> search(User user, String query) {
		Collection<IMDBAutoCompleteItem> searchResults = imdbService.search(query);

		Map<String, IMDBAutoCompleteItem> imdbIds = new HashMap<>();
		for (IMDBAutoCompleteItem searchResult : searchResults) {
			imdbIds.put(IMDBServiceImpl.IMDB_URL + searchResult.getId(), searchResult);
		}

		for (UserMovie userMovie : movieDao.findUserMoviesByIMDBIds(user, imdbIds.keySet())) {
			imdbIds.get(userMovie.getMovie().getImdbUrl()).setAdded(true);
		}

		return searchResults;
	}

	@Override
	public void downloadUserMovies() {
		logService.info(getClass(), "Downloading user movies");
		DurationMeter duration = new DurationMeter();

		List<Movie> movies = movieDao.findAllUserMovies(MovieServiceImpl.USER_MOVIES_DISPLAY_DAYS_HISTORY);
		Set<MovieRequest> movieRequests = new HashSet<>();
		for (Movie movie : movies) {
			MovieRequest movieRequest = new MovieRequest(movie.getName(), null);
			movieRequest.setImdbId(movie.getImdbUrl());
			movieRequests.add(movieRequest);
		}
		movieTorrentsDownloader.download(movieRequests, new DownloadConfig());

		duration.stop();
		logService.info(getClass(), "Downloading user movies took " + duration.getDuration() + " ms");
	}

	@Override
	public void addMovie(Movie movie, IMDBParseResult imdbParseResult) {
		imdbPreviewCacheService.addImdbPreview(movie, imdbParseResult.getPage());
		movieDao.persist(movie);
	}

	@Override
	public String getImdbPreviewPage(Movie movie) {
		String page = imdbPreviewCacheService.getImdbPreviewPage(movie);
		if (page == null) {
			page = imdbService.downloadMovieFromIMDB(movie.getImdbUrl()).getPage();
			imdbPreviewCacheService.addImdbPreview(movie, page);
		}
		return page;
	}
}