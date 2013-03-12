package rss.services.downloader;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import rss.dao.MovieDao;
import rss.dao.TorrentDao;
import rss.dao.UserTorrentDao;
import rss.entities.*;
import rss.services.EmailService;
import rss.services.PageDownloader;
import rss.services.SearchResult;
import rss.services.searchers.TorrentSearcher;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * User: Michael Dikman
 * Date: 03/12/12
 * Time: 09:10
 */
@Service("moviesTorrentEntriesDownloader")
public class MoviesTorrentEntriesDownloader extends TorrentEntriesDownloader<Movie, MovieRequest> {

	public static final Pattern NAME_YEAR_PATTERN = Pattern.compile(".*?\\((\\d+)\\)");

	public static final Pattern COMING_SOON_PATTERN = Pattern.compile("<div class=\"showtime\">.*?<h2>Coming Soon</h2>", Pattern.MULTILINE | Pattern.DOTALL);

	public static final Pattern VIEWERS_PATTERN = Pattern.compile("<span itemprop=\"ratingCount\">([^<]*)</span>");

	public static final Pattern OLD_YEAR_PATTERN = Pattern.compile("<meta name=\"title\" content=\"(.*?) - IMDb\" />");

	@Autowired
	private MovieDao movieDao;

	@Autowired
	private EmailService emailService;

	@Autowired
	@Qualifier("compositeMoviesSearcher")
	private TorrentSearcher<MovieRequest, Movie> compositeMoviesSearcher;

	@Autowired
	private TorrentDao torrentDao;

	@Autowired
	private PageDownloader pageDownloader;

	@Autowired
	private UserTorrentDao userTorrentDao;

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	protected SearchResult<Movie> downloadTorrent(MovieRequest movieRequest) {
		return compositeMoviesSearcher.search(movieRequest);
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	protected Movie onTorrentFound(MovieRequest movieRequest, SearchResult<Movie> searchResult) {
		String name = getMovieName(searchResult);
		if (name == null) { // when inferring imdb url if year is too old skipping it
			return null;
		}

		Torrent torrent = searchResult.getTorrent();
		Torrent persistedTorrent = torrentDao.findByUrl(torrent.getUrl());
		if (persistedTorrent == null) {
			torrentDao.persist(torrent);
			persistedTorrent = torrent;
		}

		// set the quality of the torrent
		for (MediaQuality mediaQuality : MediaQuality.topToBottom()) {
			if (persistedTorrent.getTitle().contains(mediaQuality.toString())) {
				persistedTorrent.setQuality(mediaQuality);
				break;
			}
		}
		persistedTorrent.setHash(movieRequest.getHash());

		Movie persistedMovie = movieDao.findByName(name);
		if (persistedMovie == null) {
			persistedMovie = new Movie(name, searchResult.getMetaData().getImdbUrl());
			movieDao.persist(persistedMovie);
		} else if (persistedMovie.getTorrentIds().isEmpty()) {
			// if movie already existed and had no torrents - this is the case where it is a future movie request by user
			Collection<User> users = movieDao.findUsersForFutureMovie(persistedMovie);
			logService.info(getClass(), "Detected a FUTURE movie " + persistedMovie.getName() + " for users: " + StringUtils.join(users, ", "));
			for (User user : users) {
				MovieUserTorrent userTorrent = new MovieUserTorrent();
				userTorrent.setUser(user);
				userTorrent.setAdded(new Date());
				userTorrent.setTorrent(persistedTorrent);
				userTorrentDao.persist(userTorrent);
			}
		}

		persistedMovie.getTorrentIds().add(persistedTorrent.getId());

		return persistedMovie;
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	protected void preDownloadPhase(Set<MovieRequest> mediaRequestsCopy) {
		//To change body of implemented methods use File | Settings | File Templates.
	}

	private String getMovieName(SearchResult<Movie> searchResult) {
		String name;

		// if found a result then try to get the real title from IMDB
		if (searchResult.getMetaData().getImdbUrl() != null) {
			long from = System.currentTimeMillis();

			try {
				// imdb pages are large, downloading until a regular expression is satisfied and that chunk is returned
				String partialPage = pageDownloader.downloadPageUntilFound(searchResult.getMetaData().getImdbUrl(), VIEWERS_PATTERN);
				// check for old year
				// <meta name="title" content="The Prestige (2006) - IMDb" />
				Matcher oldYearMatcher = OLD_YEAR_PATTERN.matcher(partialPage);
				oldYearMatcher.find();
				name = oldYearMatcher.group(1);
				name = StringEscapeUtils.unescapeHtml4(name);
				logService.info(getClass(), String.format("Downloading title for movie '%s' took %d millis", name, (System.currentTimeMillis() - from)));
				if (isInvalidMovieYear(name)) {
					logService.info(getClass(), "Skipping movie '" + name + "' due to old year");
					return null;
				}

				// ignore future movies which are not release yet
				Matcher comingSoonMatcher = COMING_SOON_PATTERN.matcher(partialPage);
				if (comingSoonMatcher.find()) {
					logService.info(getClass(), "Skipping movie '" + name + "' because it is not yet released");
					return null;
				}

				// check for number of reviewers for the movie
				Matcher viewersMatcher = VIEWERS_PATTERN.matcher(partialPage);
				if (!viewersMatcher.find()) {
					logService.warn(getClass(), "Failed retrieving number of viewers for '" + name + "': " + partialPage);
				} else {
					String viewers = viewersMatcher.group(1);
					viewers = viewers.replaceAll(",", "");
					int viewersNum = Integer.parseInt(viewers);
					if (viewersNum < 1000) {
						logService.info(getClass(), "Skipping movie '" + name + "' due to low number of viewers in imdb: " + viewersNum);
						return null;
					}
				}
				return name;
			} catch (Exception e) {
				logService.error(getClass(), "Failed downloading IMDB page " + searchResult.getMetaData().getImdbUrl() + ": " + e.getMessage(), e);
			}
		}

		// no imdb url so can't infer the title from imdb
		name = StringEscapeUtils.unescapeHtml4(searchResult.getTorrent().getTitle());
		logService.info(this.getClass(), String.format("No IMDB url for movie '%s', using torrent title as name", name));
		return name;
	}

	private boolean isInvalidMovieYear(String name) {
		Matcher matcher = NAME_YEAR_PATTERN.matcher(name);
		if (matcher.find()) {
			int year = Integer.parseInt(matcher.group(1));
			int curYear = Calendar.getInstance().get(Calendar.YEAR);
			int prevYear = curYear - 1;
			if (year != curYear && year != prevYear) {
				return true;
			}
		}

		return false;
	}

	@Override
	public void emailMissingRequests(Collection<MovieRequest> missingRequests) {
		if (missingRequests.isEmpty()) {
			return;
		}

		emailService.notifyOfMissingMovies(missingRequests);
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	protected Collection<Movie> removeCachedEntries(Set<MovieRequest> requests) {
		Set<Movie> result = new HashSet<>();

		Map<String, MovieRequest> byHash = new HashMap<>();
		for (MovieRequest request : requests) {
			byHash.put(request.getHash(), request);
		}

		// the name of the torrent file is not necessary the same as in the request which comes from search in torrentz.com
		// sometimes . - or spaces can be misplaces
		Collection<Torrent> torrents = torrentDao.findByHash(byHash.keySet());
		for (Torrent torrent : torrents) {
			Movie movie = movieDao.find(torrent);
			logService.info(this.getClass(), String.format("Movie \"%s\" was found in cache (torrent: \"%s\")", movie, torrent.getTitle()));
			result.add(movie);
			requests.remove(byHash.get(torrent.getHash()));
		}

		return result;
	}
}
