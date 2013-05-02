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
import rss.controllers.EntityConverter;
import rss.controllers.vo.DownloadStatus;
import rss.controllers.vo.UserMovieStatus;
import rss.controllers.vo.UserMovieVO;
import rss.dao.MovieDao;
import rss.dao.TorrentDao;
import rss.dao.UserTorrentDao;
import rss.entities.*;
import rss.services.SessionService;
import rss.services.downloader.DownloadResult;
import rss.services.downloader.MoviesTorrentEntriesDownloader;
import rss.services.log.LogService;
import rss.services.requests.MovieRequest;
import rss.services.searchers.composite.torrentz.TorrentzParser;
import rss.services.searchers.composite.torrentz.TorrentzParserImpl;
import rss.services.searchers.composite.torrentz.TorrentzResult;
import rss.util.DateUtils;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.*;

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
	private IMDBService imdbService;

	@Autowired
	private LogService logService;

	@Autowired
	private EntityConverter entityConverter;

	@Autowired
	private MoviesTorrentEntriesDownloader moviesTorrentEntriesDownloader;

	@Autowired
	private TransactionTemplate transactionTemplate;

	@Autowired
	private TorrentzParser torrentzParser;

	@Transactional(propagation = Propagation.REQUIRED)
	public ArrayList<UserMovieVO> getUserMovies(User user) {
		ArrayList<UserMovieVO> result = new ArrayList<>();

		// first add movies without any torrents - future movies
		for (UserMovie userMovie : movieDao.findFutureUserMovies(user)) {
			result.add(entityConverter.toFutureMovie(userMovie.getMovie())
					.withScheduledOn(userMovie.getUpdated())
					.withAdded(userMovie.getUpdated()));
		}

		// then add movies that has torrents and the user selected a torrent to download
		for (UserTorrent userTorrent : userTorrentDao.findScheduledUserMovies(user, 14)) {
			Torrent torrent = userTorrent.getTorrent();
			Movie movie = movieDao.find(torrent);
			UserMovieVO userMovieVO = new UserMovieVO()
					.withId(movie.getId())
					.withTitle(movie.getName())
					.withImdbUrl(movie.getImdbUrl())
					.withAdded(userTorrent.getAdded());
			userMovieVO.setViewed(true);
			userMovieVO.addTorrentDownloadStatus(UserMovieStatus.fromUserTorrent(userTorrent).withViewed(true).withMovieId(movie.getId()));

			// add the rest of the torrents of the movie
			for (Long torrentId : movie.getTorrentIds()) {
				if (torrentId != torrent.getId()) {
					addTorrentToUserMovieVO(userMovieVO, torrentId);
				}
			}

			result.add(userMovieVO);
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
		MoviesToTorrentsMapper moviesMapper = new MoviesToTorrentsMapper(latestMovies);
		UserMoviesVOContainer userMoviesVOContainer = new UserMoviesVOContainer();

		// get all userMovies related to the latest movie "names"
		for (UserTorrent userTorrent : userTorrentDao.findUserMovies(user, latestMovies)) {
			Torrent torrent = userTorrent.getTorrent();
			torrentsByIds.put(torrent.getId(), torrent);
			Movie movie = moviesMapper.getMovie(torrent);
			UserMovieVO userMovieVO = userMoviesVOContainer.getUserMovie(movie);
			userMovieVO.addTorrentDownloadStatus(UserMovieStatus.fromUserTorrent(userTorrent).withViewed(true).withMovieId(movie.getId()));
			updateLatestUploadDate(torrent, userMovieVO);
		}

		// add movies that had no userMovies
		for (Movie movie : latestMovies) {
			UserMovieVO userMovieVO = userMoviesVOContainer.getUserMovie(movie);

			// if at least one is not viewed - show as not viewed
			for (Long torrentId : movie.getTorrentIds()) {
				if (!torrentsByIds.containsKey(torrentId)) {
					Torrent torrent = addTorrentToUserMovieVO(userMovieVO, torrentId);
					torrentsByIds.put(torrent.getId(), torrent);
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

	private Torrent addTorrentToUserMovieVO(UserMovieVO userMovieVO, Long torrentId) {
		Torrent torrent = torrentDao.find(torrentId);
		userMovieVO.addTorrentDownloadStatus(new UserMovieStatus(DownloadStatus.NONE)
				.withTitle(torrent.getTitle())
				.withTorrentId(torrent.getId())
				.withUploadedDate(torrent.getDateUploaded())
				.withScheduledOn(null)
				.withMovieId(userMovieVO.getId()));
		updateLatestUploadDate(torrent, userMovieVO);
		return torrent;
	}


	private void updateLatestUploadDate(Torrent torrent, UserMovieVO userMovieVO) {
		Date cur = torrent.getDateUploaded();
		if (userMovieVO.getLatestUploadDate() == null || userMovieVO.getLatestUploadDate().before(cur)) {
			userMovieVO.setLatestUploadDate(cur);
		}
	}

	private Set<Movie> getLatestMovies() {
		return new HashSet<>(movieDao.findUploadedSince(DateUtils.getPastDate(sessionService.getPrevLoginDate(), 7)));
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
	public Pair<UserMovie, Boolean> addMovieDownload(User user, long movieId) {
		Movie movie = movieDao.find(movieId);
		return addMovieDownload(user, movie);
	}

	// boolean: true if already exists, false if new
	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public Pair<UserMovie, Boolean> addFutureMovieDownload(User user, String imdbId) {
		try {
			final String imdbUrl = IMDB_URL + imdbId;
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

				// uses a separate transaction
//				torrentzService.downloadMovie(movie);
				MovieRequest movieRequest = new MovieRequest(movie.getName(), null);
				movieRequest.setImdbId(imdbId);
				moviesTorrentEntriesDownloader.download(Collections.singleton(movieRequest));

				// re-fetch the movie in this transaction after it got torrents
				movie = movieDao.find(movie.getId());
			}

			// if movie is too old and there are no torrents now - then there won't be any. no point adding it to user as scheduled
			Calendar c = Calendar.getInstance();
			if (movie.getYear() < c.get(Calendar.YEAR) - 1 && movie.getTorrentIds().isEmpty()) {
				throw new MediaRSSException("Unable to find torrents for movie '" + movie.getName() + "'").doNotLog();
			}

			return addMovieDownload(user, movie);
		} catch (InterruptedException | ExecutionException e) {
			throw new MediaRSSException(e.getMessage(), e);
		}
	}

	private Pair<UserMovie, Boolean> addMovieDownload(User user, Movie movie) {
		UserMovie userMovie = movieDao.findUserMovie(movie.getId(), user);
		if (userMovie == null) {
			userMovie = new UserMovie();
			userMovie.setMovie(movie);
			userMovie.setUser(user);
			userMovie.setUpdated(new Date());
			movieDao.persist(userMovie);
			return new MutablePair<>(userMovie, false);
		}

		return new MutablePair<>(userMovie, true);
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
