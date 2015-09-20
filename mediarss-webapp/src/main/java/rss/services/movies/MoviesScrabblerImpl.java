package rss.services.movies;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import rss.scheduler.ScheduledJob;
import rss.services.user.UserCacheService;

/**
 * User: Michael Dikman
 * Date: 02/12/12
 * Time: 21:38
 */
@Service
public class MoviesScrabblerImpl implements ScheduledJob {

    public static final String MOVIES_SCRABBLER_JOB = "MoviesScrabbler";

    @Autowired
    private MovieService movieService;

    @Autowired
    private TopMoviesService topMoviesService;

    @Autowired
    private UserCacheService userCacheService;

    @Autowired
    protected TransactionTemplate transactionTemplate;

    @Override
    public String getName() {
        return MOVIES_SCRABBLER_JOB;
    }

    @Override
    public String getCronExp() {
        return "0 0 0/6 * * ?";
    }

    @Override
    public void run() {
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
    }
}
