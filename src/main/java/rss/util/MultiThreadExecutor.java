package rss.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import rss.services.log.LogService;

import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * User: dikmanm
 * Date: 16/04/13 12:37
 */
public class MultiThreadExecutor {

	private static  final Log log = LogFactory.getLog(MultiThreadExecutor.class);

	public static <T> void execute(ExecutorService executorService, Collection<T> list, final MultiThreadExecutorTask<T> task) {
		for (final T element : list) {
			executorService.submit(new Runnable() {
				@Override
				public void run() {
					task.run(element);
				}
			});
		}

		executorService.shutdown();
		try {
			executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
		} catch (InterruptedException e) {
			log.error("Error waiting for tasks to execute: " + e.getMessage(), e);
		}
	}

	public interface MultiThreadExecutorTask<T> {
		void run(T element);
	}
}
