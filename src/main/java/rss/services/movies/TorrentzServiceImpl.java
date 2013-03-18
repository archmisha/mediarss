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
import rss.services.parsers.PageParser;

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

//	private static final String TORRENTZ_HIGH_RES_MOVIES_URL = "http://torrentz.eu/search?f=movies+hd+video+highres+added%3A";
	private static final String TORRENTZ_HIGH_RES_MOVIES_URL = "http://torrentz.eu/search?f=movies+hd+video+-shows+-porn+-brrip+added%3A";
	private static final String TORRENTZ_MOVIE_SEARCH_URL = "http://torrentz.eu/search?f=movies+hd+video+highres+";

	@Autowired
	@Qualifier("torrentzParser")
	private PageParser torrentzParser;

	@Autowired
	private PageDownloader pageDownloader;

	@Autowired
	private LogService logService;

	@Autowired
	private MoviesTorrentEntriesDownloader moviesTorrentEntriesDownloader;

	@Override
	@Transactional(propagation = Propagation.NOT_SUPPORTED)
	public DownloadResult<Movie, MovieRequest> downloadLatestMovies() {
		String page = pageDownloader.downloadPage(TORRENTZ_HIGH_RES_MOVIES_URL + "1d");
		Set<MovieRequest> movies = torrentzParser.parse(page);

		// retry with 7 days
		if (movies.isEmpty()) {
			page = pageDownloader.downloadPage(TORRENTZ_HIGH_RES_MOVIES_URL + "7d");
			movies = torrentzParser.parse(page);
		}

		// filter out old year movies
		int curYear = Calendar.getInstance().get(Calendar.YEAR);
		int prevYear = curYear - 1;
		for (MovieRequest movieRequest : new ArrayList<>(movies)) {
			String name = movieRequest.getTitle();
			if (!name.contains(String.valueOf(curYear)) && !name.contains(String.valueOf(prevYear))) {
				logService.info(getClass(), "Skipping movie '" + name + "' due to old year");
				movies.remove(movieRequest);
			}
		}

		return moviesTorrentEntriesDownloader.download(movies);
	}

	@Override
	@Transactional(propagation = Propagation.NOT_SUPPORTED)
	public void downloadMovie(Movie movie) {
		try {
			String page = pageDownloader.downloadPage(TORRENTZ_MOVIE_SEARCH_URL + URLEncoder.encode(movie.getName(), "UTF-8"));
			Set<MovieRequest> movies = torrentzParser.parse(page);
			moviesTorrentEntriesDownloader.download(movies);
		} catch (UnsupportedEncodingException e) {
			throw new MediaRSSException(e.getMessage(), e);
		}
	}
}
