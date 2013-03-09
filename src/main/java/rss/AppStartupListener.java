/*
package rss;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.ContextStartedEvent;
import org.springframework.core.io.ClassPathResource;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import rss.services.JobRunner;
import rss.services.OOTBContentLoader;
import rss.services.PageDownloader;
import rss.services.SettingsService;
import rss.services.shows.ShowService;
import rss.services.shows.ShowsScheduleDownloaderService;
import rss.services.shows.TVRageServiceImpl;
import rss.util.QuartzJob;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

*/
/**
 * User: Michael Dikman
 * Date: 25/10/12
 * Time: 00:28
 *//*

public class AppStartupListener implements ApplicationListener<ContextStartedEvent> {

	public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy");

	private static Log log = LogFactory.getLog(AppStartupListener.class);

	@Autowired
	private TransactionTemplate transactionTemplate;

	@Autowired
	private Scheduler scheduler;

	@Autowired
	private SettingsService settingsService;

	@Autowired
	private ShowService showService;

	@Autowired
	private ShowsScheduleDownloaderService showsScheduleDownloaderService;

	@Autowired
	private OOTBContentLoader ootbContentLoader;

	@Autowired
	private PageDownloader pageDownloader;

	@Override
	public void onApplicationEvent(ContextStartedEvent event) {
//		ootbContentLoader.loadTVRageShows();

		for (int i : Arrays.asList(
				20954,
				21157,
				2590,
				29631,
				29632,
				29633,
				29634,
				29635,
				29636,
				29637,
				29638,
				29639,
				29640,
				29641,
				29642,
				29643,
				29644,
				29645,
				29646,
				29647,
				29648,
				29649,
				29650,
				29651,
				29652,
				29653,
				29654,
				29655,
				29656,
				29657,
				29658,
				29659,
				29660,
				29661,
				29662,
				29663,
				29664,
				29665,
				29666,
				29667,
				29668,
				29669,
				29670,
				29671,
				29672,
				29673,
				29674,
				29675,
				29676,
				29677,
				29678,
				29679,
				29680,
				29681,
				29682,
				29683,
				29684,
				29685,
				29686,
				29687,
				29688,
				29689,
				29690,
				29691,
				29692,
				29693,
				29694,
				29695,
				29696,
				29697,
				29698,
				29699,
				29700,
				29701,
				29702,
				29703,
				29704,
				29705,
				29706,
				29707,
				29708,
				29709,
				29710,
				29711,
				29712,
				29713,
				29714,
				29715,
				29716,
				29717,
				29718,
				29719,
				29720,
				29721,
				29722,
				29723,
				29724,
				29725,
				29726,
				29727,
				29728,
				29729,
				29730,
				29731,
				29732,
				29733,
				29734,
				29735,
				29736,
				29737,
				29738,
				29739,
				29740,
				29741,
				29742,
				29743,
				29744,
				29745,
				29746,
				29747,
				29748,
				29749,
				29750,
				29751,
				29752,
				29753,
				29754,
				29755,
				29756,
				29757,
				29758,
				29759,
				29760,
				29761,
				29762,
				29763,
				29764,
				29765,
				29766,
				29767,
				29768,
				29769,
				29770,
				29771,
				29772,
				29773,
				29774,
				29775,
				29776,
				29777,
				29778,
				29779,
				29780,
				29781,
				29782,
				29783,
				29784,
				29785,
				29786,
				29787,
				29788,
				29789,
				29790,
				29791,
				29792,
				29793,
				29794,
				29795,
				29796,
				29797,
				29798,
				29799,
				29800,
				29801,
				29802,
				29803,
				29804,
				29805,
				29806,
				29807,
				29808,
				29809,
				29810,
				29811,
				29812,
				29813,
				29814,
				29815,
				29816,
				29817,
				29818,
				29819,
				29820,
				29821,
				29822,
				29823,
				29824,
				29825,
				29826,
				29827,
				29833,
				3140,
				31418,
				31426,
				3256,
				3719,
				5714,
				7024,
				7051)) {
		*/
/*ExecutorService executorService = Executors.newFixedThreadPool(20);
		for (int i = 1; i <= 34768; ++i) {*//*

			final int finalI = i;
//			executorService.submit(new Runnable() {
//				@Override
//				public void run() {
					log.info("current i=" + finalI);
					try {
						if (!new File("c:\\tvrage\\" + finalI + ",").exists()) {
							String page = pageDownloader.downloadPage(TVRageServiceImpl.SHOW_INFO_URL + finalI);
							FileWriter output = new FileWriter("c:\\tvrage\\" + finalI + ",");
							IOUtils.write(page, output);
							output.close();
						}
					} catch (IOException e) {
						log.error(e.getMessage(), e);
					}
//				}
//			});
		}

//		executorService.shutdown();
//		try {
//			executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
//		} catch (InterruptedException e) {
//			log.error("Error waiting for download tasks to execute: " + e.getMessage(), e);
//		}

		System.exit(1);
		try {
			this.loadCronTriggerBeans(event.getApplicationContext());
		} catch (Exception e) {
			log.error("Failed starting quartz scheduler: " + e.getMessage(), e);
		}

		// track deployment date
		Date deployedDate = getDeploymentDate();
		settingsService.setDeploymentDate(deployedDate);
	}

	private void startDownloads() {
		// download shows and schedules on startup
		try {
			transactionTemplate.execute(new TransactionCallbackWithoutResult() {
				@Override
				protected void doInTransactionWithoutResult(TransactionStatus arg0) {
					showService.downloadShowList();
				}
			});
		} catch (Throwable e) {
			throw new RuntimeException("Failed downloading shows / schedules", e);
		}

		// must be in a separate transaction!!!
		// also no need to suspend app startup for that, this can be a background process
		ExecutorService executorService = Executors.newSingleThreadExecutor();
		executorService.submit(new Runnable() {
			@Override
			public void run() {
				try {
					transactionTemplate.execute(new TransactionCallbackWithoutResult() {
						@Override
						protected void doInTransactionWithoutResult(TransactionStatus arg0) {
							((JobRunner) showsScheduleDownloaderService).start();
						}
					});
				} catch (Throwable e) {
					throw new RuntimeException("Failed downloading shows / schedules", e);
				}
			}
		});
	}

	@SuppressWarnings("unchecked")
	private void loadCronTriggerBeans(ApplicationContext applicationContext) {
		Map<String, Object> quartzJobBeans = applicationContext.getBeansWithAnnotation(QuartzJob.class);
		for (Map.Entry<String, Object> entry : quartzJobBeans.entrySet()) {
			try {
				Object job = entry.getValue();
				QuartzJob quartzJobAnnotation = applicationContext.findAnnotationOnBean(entry.getKey(), QuartzJob.class);
				if (!Job.class.isAssignableFrom(job.getClass())) {
					throw new RuntimeException(job.getClass() + " doesn't implemented " + Job.class);
				}

				JobDetail jobDetail = JobBuilder.newJob((Class<? extends Job>) job.getClass()).withIdentity(quartzJobAnnotation.name()).build();
//				Trigger trigger = TriggerBuilder.newTrigger().forJob(jobDetail).withIdentity(quartzJobAnnotation.name() + "_trigger").build();

				scheduler.addJob(jobDetail, true);
//				scheduler.scheduleJob(trigger);
				log.info("Loading quartz Job: " + entry.getValue());
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
		}
	}

	private Date getDeploymentDate() {
		Date deployedDate = new Date(); // better than null, even if wrong
		try {
			String databaseProperties = System.getProperty("database.properties");
			if (databaseProperties == null) {
				databaseProperties = "database.properties";
			}

			// use existing file to locate the real path
			ClassPathResource refClassPathResource = new ClassPathResource(databaseProperties, AppConfigListener.class.getClassLoader());
			String path = refClassPathResource.getURI().getPath();
			path = path.substring(0, path.indexOf(databaseProperties));
			path = path + "deploymentDate.txt";
			File file = new File(path);
			if (file.exists()) {
				BufferedReader br = new BufferedReader(new FileReader(file));
				deployedDate = DATE_FORMAT.parse(br.readLine());
			} else {
				deployedDate = new Date();
//				file.createNewFile();
				FileOutputStream fos = new FileOutputStream(file, false);
				fos.write(DATE_FORMAT.format(deployedDate).getBytes());
				fos.close();
			}
		} catch (Throwable e) {
			log.error(e.getMessage(), e);
		}
		return deployedDate;
	}
}
*/
