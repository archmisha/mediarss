package rss.services.movies;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import rss.entities.Movie;
import rss.services.JobRunner;
import rss.services.downloader.DownloadResult;
import rss.services.requests.movies.MovieRequest;
import rss.util.QuartzJob;

/**
 * User: Michael Dikman
 * Date: 02/12/12
 * Time: 21:38
 */
@Service
@QuartzJob(name = "MoviesScrabbler", cronExp = "0 0 0/6 * * ?")
public class MoviesScrabblerImpl extends JobRunner implements MoviesScrabbler {

//	@Autowired
//	private EmailService emailService;

	@Autowired
	private MovieService movieService;

	@Autowired
	private TopMoviesService topMoviesService;

	public MoviesScrabblerImpl() {
		super(JOB_NAME);
	}

	@Override
	protected String run() {
		transactionTemplate.execute(new TransactionCallbackWithoutResult() {
			@Override
			public void doInTransactionWithoutResult(TransactionStatus arg0) {
				DownloadResult<Movie, MovieRequest> downloadResult = movieService.downloadLatestMovies();

				// no need to send emails here, we didn't search by name for something specific
				// if one of the movies in the latest list is not found, it means maybe was no IMDB ID on the page
//				emailService.notifyOfMissingMovies(downloadResult.getMissing());
			}
		});

		transactionTemplate.execute(new TransactionCallbackWithoutResult() {
			@Override
			protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
				movieService.downloadUserMovies();
			}
		});

		transactionTemplate.execute(new TransactionCallbackWithoutResult() {
			@Override
			public void doInTransactionWithoutResult(TransactionStatus arg0) {
				topMoviesService.downloadTopMovies();
			}
		});

		return null;
	}
}
