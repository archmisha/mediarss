package rss.services.movies;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import rss.controllers.vo.DownloadStatus;
import rss.controllers.vo.UserMovieStatus;
import rss.controllers.vo.UserMovieVO;
import rss.dao.MovieDao;
import rss.dao.TorrentDao;
import rss.dao.UserTorrentDao;
import rss.entities.*;
import rss.services.SessionService;

import java.io.Serializable;
import java.util.*;

/**
 * User: dikmanm
 * Date: 08/03/13 15:38
 */
@Service
public class MovieServiceImpl implements MovieService {

	@Autowired
	private SessionService sessionService;

	@Autowired
	private UserTorrentDao userTorrentDao;

	@Autowired
	private MovieDao movieDao;

	@Autowired
	private TorrentDao torrentDao;

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
							.withTorrentId(torrent.getId()));
					updateLatestUploadDate(torrent, userMovieVO);
				}
			}
		}

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
}
