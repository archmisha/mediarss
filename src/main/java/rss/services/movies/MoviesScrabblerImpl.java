package rss.services.movies;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;
import rss.MediaRSSException;
import rss.entities.Movie;
import rss.services.JobRunner;
import rss.services.PageDownloader;
import rss.services.downloader.DownloadResult;
import rss.services.downloader.MovieRequest;
import rss.services.downloader.MoviesTorrentEntriesDownloader;
import rss.services.parsers.PageParser;
import rss.util.QuartzJob;

import javax.annotation.PostConstruct;
import java.util.Set;

/**
 * User: Michael Dikman
 * Date: 02/12/12
 * Time: 21:38
 */
@Service
@QuartzJob(name = "MoviesScrabbler", cronExp = "0 0 0/6 * * ?")
public class MoviesScrabblerImpl extends JobRunner implements MoviesScrabbler {

	private static final String TORRENTZ_HIGH_RES_MOVIES_URL = "http://torrentz.com/searchS?f=movies+hd+video+highres+added%3A";

	@Autowired
	@Qualifier("torrentzParser")
	private PageParser torrentzParser;

	@Autowired
	private MoviesTorrentEntriesDownloader moviesTorrentEntriesDownloader;

	@Autowired
	private PageDownloader pageDownloader;

	@Autowired
	private TransactionTemplate transactionTemplate;

	public MoviesScrabblerImpl() {
		super(JOB_NAME);
	}

	@PostConstruct
	private void postConstruct() {
		createJobStatus();
	}

	@Override
	protected String run() {
		return transactionTemplate.execute(new TransactionCallback<String>() {
			@Override
			public String doInTransaction(TransactionStatus arg0) {

				String page = pageDownloader.downloadPage(TORRENTZ_HIGH_RES_MOVIES_URL + "1d");
				Set<MovieRequest> movies = torrentzParser.parse(page);

				// retry with 7 days
				if (movies.isEmpty()) {
					page = pageDownloader.downloadPage(TORRENTZ_HIGH_RES_MOVIES_URL + "7d");
					movies = torrentzParser.parse(page);
				}

				DownloadResult<Movie, MovieRequest> downloadResult = moviesTorrentEntriesDownloader.download(movies);
				moviesTorrentEntriesDownloader.emailMissingRequests(downloadResult.getMissing());

				return null;
			}
		});
	}
}
