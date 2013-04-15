package rss.services.movies;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import rss.MediaRSSException;
import rss.entities.Movie;
import rss.services.PageDownloader;
import rss.services.downloader.DownloadResult;
import rss.services.downloader.MovieRequest;
import rss.services.downloader.MoviesTorrentEntriesDownloader;
import rss.services.log.LogService;
import rss.services.parsers.TorrentzParser;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Set;

/**
 * User: dikmanm
 * Date: 16/03/13 22:12
 */
@Service
public class TorrentzServiceImpl implements TorrentzService {

	public static final String HOST_NAME = "http://torrentz.eu/";

	// removed filters: highres
	public static final String FILTERS = "+-shows+-porn+-brrip+-episodes";
	private static final String TORRENTZ_HIGH_RES_MOVIES_URL = HOST_NAME + "search?f=movies+hd+video" + FILTERS + "+added%3A";
	private static final String TORRENTZ_MOVIE_SEARCH_URL = HOST_NAME + "search?f=movies+hd+video" + FILTERS + "+";

	public static final String TORRENTZ_ENTRY_URL = HOST_NAME;

	@Autowired
	@Qualifier("torrentzParser")
	private TorrentzParser torrentzParser;

	@Autowired
	private PageDownloader pageDownloader;

	@Autowired
	private LogService logService;

	@Autowired
	private MoviesTorrentEntriesDownloader moviesTorrentEntriesDownloader;

	@Override
	@Transactional(propagation = Propagation.NOT_SUPPORTED)
	public DownloadResult<Movie, MovieRequest> downloadLatestMovies() {
		Set<MovieRequest> movieRequests = downloadByUrl(TORRENTZ_HIGH_RES_MOVIES_URL + "1d");

		// retry with 7 days
		if (movieRequests.isEmpty()) {
			movieRequests = downloadByUrl(TORRENTZ_HIGH_RES_MOVIES_URL + "7d");
		}

		// filter out old year movies
		int curYear = Calendar.getInstance().get(Calendar.YEAR);
		int prevYear = curYear - 1;
		for (MovieRequest movieRequest : new ArrayList<>(movieRequests)) {
			String name = movieRequest.getTitle();
			if (!name.contains(String.valueOf(curYear)) && !name.contains(String.valueOf(prevYear))) {
				logService.info(getClass(), "Skipping movie '" + name + "' due to old year");
				movieRequests.remove(movieRequest);
			}
		}

		return moviesTorrentEntriesDownloader.download(movieRequests);
	}

	@Override
	@Transactional(propagation = Propagation.NOT_SUPPORTED)
	public DownloadResult<Movie, MovieRequest> downloadMovie(Movie movie, String imdbId) {
		try {
			Set<MovieRequest> movieRequests = downloadByUrl(TORRENTZ_MOVIE_SEARCH_URL + URLEncoder.encode(movie.getName(), "UTF-8"));
			for (MovieRequest movieRequest : movieRequests) {
				movieRequest.setImdbId(imdbId);
			}
			return moviesTorrentEntriesDownloader.download(movieRequests);
		} catch (UnsupportedEncodingException e) {
			throw new MediaRSSException(e.getMessage(), e);
		}
	}

	private Set<MovieRequest> downloadByUrl(String url) {
		String page = pageDownloader.downloadPage(url);
		Set<MovieRequest> movieRequests = torrentzParser.parse(page);

		for (MovieRequest movieRequest : movieRequests) {
			String entryPage = pageDownloader.downloadPage(TORRENTZ_ENTRY_URL + movieRequest.getHash());
			movieRequest.setPirateBayId(torrentzParser.getPirateBayId(entryPage));
		}

		return movieRequests;
	}
}
