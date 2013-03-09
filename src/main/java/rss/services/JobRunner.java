package rss.services;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import rss.dao.JobStatusDao;
import rss.entities.JobStatus;
import rss.services.log.LogService;
import rss.util.DurationMeter;

import java.util.Date;
import java.util.concurrent.Executors;

/**
 * User: dikmanm
 * Date: 10/02/13 18:50
 */
public abstract class JobRunner extends QuartzJobBean {

	@Autowired
	private JobStatusDao jobStatusDao;

	@Autowired
	protected LogService logService;

	@Autowired
	private TransactionTemplate transactionTemplate;

	private String name;
	private boolean running;

	public JobRunner(String name) {
		this.name = name;
		running = false;
	}

	public JobStatus start() {
		if (running) {
			return getJobStatus();
		}
		running = true;
		logService.info(getClass(), "Job " + this.name + " started");

		final DurationMeter durationMeter = new DurationMeter();
		JobStatus jobStatus = updateJobStarted(durationMeter.getStartTime());

		final Class<?> aClass = getClass();
		Executors.newSingleThreadExecutor().submit(new Runnable() {
			@Override
			public void run() {
				String statusMessage;
				try {
					statusMessage = JobRunner.this.run();
				} catch (Exception e) {
					logService.error(aClass, e.getMessage(), e);
					statusMessage = "Failed";
				}

				durationMeter.stop();
				Date end = durationMeter.getEndTime();
				updateJobFinished(end, statusMessage);

				logService.info(aClass, String.format("Job " + JobRunner.this.name + " completed. Time took %d millis.", durationMeter.getDuration()));
				running = false;

			}
		});

		return jobStatus;
	}

	protected void updateJobFinished(final Date end, final String statusMessage) {
		transactionTemplate.execute(new TransactionCallbackWithoutResult() {
			@Override
			protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
				// must be present in the database
				JobStatus jobStatus = jobStatusDao.find(name);
				jobStatus.setEnd(end);
				jobStatus.setErrorMessage(statusMessage);
			}
		});
	}

	protected JobStatus updateJobStarted(final Date startTime) {
		return transactionTemplate.execute(new TransactionCallback<JobStatus>() {
			@Override
			public JobStatus doInTransaction(TransactionStatus transactionStatus) {
				// must be present in the database
				JobStatus jobStatus = jobStatusDao.find(name);
				jobStatus.setStart(startTime);
				jobStatus.setEnd(null);
				jobStatus.setErrorMessage(null);
				return jobStatus;
			}
		});
	}

	protected JobStatus getJobStatus() {
		return transactionTemplate.execute(new TransactionCallback<JobStatus>() {
			@Override
			public JobStatus doInTransaction(TransactionStatus transactionStatus) {
				return jobStatusDao.find(name);
			}
		});
	}

	protected void createJobStatus() {
		transactionTemplate.execute(new TransactionCallbackWithoutResult() {
			@Override
			protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
				JobStatus jobStatus = jobStatusDao.find(name);
				if (jobStatus == null) {
					jobStatus = new JobStatus();
					jobStatus.setName(name);
					jobStatusDao.persist(jobStatus);
				}
			}
		});
	}

	protected abstract String run();

	@Override
	protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
		start();
	}
}
