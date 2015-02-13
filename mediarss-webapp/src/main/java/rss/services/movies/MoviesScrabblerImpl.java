package rss.services.movies;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import rss.services.JobRunner;
import rss.services.user.UserCacheService;
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
	private MovieService movieService;

	@Autowired
	private TopMoviesService topMoviesService;

	@Autowired
	private UserCacheService userCacheService;

	public MoviesScrabblerImpl() {
		super(JOB_NAME);
	}

	@Override
	protected String run() {
		transactionTemplate.execute(new TransactionCallbackWithoutResult() {
			@Override
			public void doInTransactionWithoutResult(TransactionStatus arg0) {
				movieService.downloadLatestMovies();
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

		transactionTemplate.execute(new TransactionCallbackWithoutResult() {
			@Override
			public void doInTransactionWithoutResult(TransactionStatus arg0) {
				userCacheService.invalidateMovies();
			}
		});

		return null;
	}
}
