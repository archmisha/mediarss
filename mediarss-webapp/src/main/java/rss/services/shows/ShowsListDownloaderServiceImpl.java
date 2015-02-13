package rss.services.shows;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import rss.services.JobRunner;
import rss.util.QuartzJob;

/**
 * User: dikmanm
 * Date: 31/12/12 21:00
 */
@Service
@QuartzJob(name = "ShowsListDownloader", cronExp = "0 0 0 1/7 * ?")
public class ShowsListDownloaderServiceImpl extends JobRunner implements ShowsListDownloaderService {

	@Autowired
	private ShowService showService;

	public ShowsListDownloaderServiceImpl() {
		super(JOB_NAME);
	}

	@Override
	protected String run() {
		transactionTemplate.execute(new TransactionCallbackWithoutResult() {
			@Override
			protected void doInTransactionWithoutResult(TransactionStatus arg0) {
				showService.downloadShowList();
			}
		});
		return null;
	}
}
