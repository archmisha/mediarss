package rss.services.movies;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import rss.entities.Movie;
import rss.services.EmailService;
import rss.services.JobRunner;
import rss.services.downloader.DownloadResult;
import rss.services.requests.MovieRequest;
import rss.util.QuartzJob;

/**
 * User: Michael Dikman
 * Date: 02/12/12
 * Time: 21:38
 */
@Service
@QuartzJob(name = "MoviesScrabbler", cronExp = "0 0 0/6 * * ?")
public class MoviesScrabblerImpl extends JobRunner implements MoviesScrabbler {

	@Autowired
	private EmailService emailService;


	@Autowired
	private MovieService movieService;

	public MoviesScrabblerImpl() {
		super(JOB_NAME);
	}

	@Override
	protected String run() {
		return transactionTemplate.execute(new TransactionCallback<String>() {
			@Override
			public String doInTransaction(TransactionStatus arg0) {
				DownloadResult<Movie, MovieRequest> downloadResult = movieService.downloadLatestMovies();
				emailService.notifyOfMissingMovies(downloadResult.getMissing());
				return null;
			}
		});
	}
}
