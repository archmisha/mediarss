package rss.services.shows;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import rss.scheduler.ScheduledJob;
import rss.shows.ShowService;

/**
 * User: dikmanm
 * Date: 31/12/12 21:00
 */
@Service
public class ShowsListDownloaderServiceImpl implements ScheduledJob {

    @Autowired
    private ShowService showService;

    @Autowired
    protected TransactionTemplate transactionTemplate;

    @Override
    public String getName() {
        return "ShowsListDownloader";
    }

    @Override
    public String getCronExp() {
        return "0 0 0 1/7 * ?";
    }

    @Override
    public void run() {
        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus arg0) {
                showService.downloadShowList();
            }
        });
    }
}
