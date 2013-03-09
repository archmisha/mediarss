package rss;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.LogFactory;
import org.junit.Before;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.core.io.ClassPathResource;
import org.springframework.transaction.support.SimpleTransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.Log4jConfigurer;
import rss.services.log.LogService;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.ExecutorService;

import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;

/**
 * User: Michael Dikman
 * Date: 24/12/12
 * Time: 20:55
 */
public class BaseTest {

	public static final String ERROR_KEY = "ERROR";

	@Mock
	private LogService logService;

	@Before
	public void setup() {
		System.setProperty(ERROR_KEY, "");
		setupLog4j();
		setupDataBaseProperties();
		mockLogService();
	}

	protected static void setupLog4j() {
		try {
			// no need for /WEB-INF/classes/ prefix
			File log4jPropsFile = new ClassPathResource("test-log4j.properties", BaseTest.class.getClassLoader()).getFile();
			String path = log4jPropsFile.getAbsolutePath();
			Log4jConfigurer.initLogging(path, 30);
			LogFactory.getLog(AppConfigListener.class).info("Log4j system initialized from " + path);
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	protected static void setupDataBaseProperties() {
		System.setProperty("database.properties", "test-database.properties");
	}

	protected String loadPage(String filename) {
		String page = null;
		try {
			if (!filename.contains(".")) {
				filename = filename + ".html";
			}
			File file = new ClassPathResource("webpages/" + filename, BaseTest.class.getClassLoader()).getFile();
			page = IOUtils.toString(new FileReader(file));
		} catch (IOException e) {
			fail();
		}
		return page;
	}

	protected void mockLogService() {
		// debug
		Mockito.doAnswer(new Answer<Void>() {
			@Override
			public Void answer(InvocationOnMock invocationOnMock) throws Throwable {
				String msg = (String) invocationOnMock.getArguments()[1];
				System.out.println(msg);
				return null;
			}
		}).when(logService).debug(any(Class.class), anyString());

		// info
		Mockito.doAnswer(new Answer<Void>() {
			@Override
			public Void answer(InvocationOnMock invocationOnMock) throws Throwable {
				String msg = (String) invocationOnMock.getArguments()[1];
				System.out.println(msg);
				return null;
			}
		}).when(logService).info(any(Class.class), anyString());

		// warn
		Mockito.doAnswer(new Answer<Void>() {
			@Override
			public Void answer(InvocationOnMock invocationOnMock) throws Throwable {
				String msg = (String) invocationOnMock.getArguments()[1];
				System.out.println(msg);
				return null;
			}
		}).when(logService).warn(any(Class.class), anyString());

		// error
		Mockito.doAnswer(new Answer<Void>() {
			@Override
			public Void answer(InvocationOnMock invocationOnMock) throws Throwable {
				String msg = (String) invocationOnMock.getArguments()[1];
				Exception ex = (Exception) invocationOnMock.getArguments()[2];
				System.out.println(msg);
				ex.printStackTrace();
				fail();
				return any();
			}
		}).when(logService).error(any(Class.class), anyString(), any(Exception.class));

		Mockito.doAnswer(new Answer<Void>() {
			@Override
			public Void answer(InvocationOnMock invocationOnMock) throws Throwable {
				String msg = (String) invocationOnMock.getArguments()[1];
				System.out.println(msg);
				fail();
				return any();
			}
		}).when(logService).error(any(Class.class), anyString());
	}

	protected void mockExecutorServiceAsDirectExecutor(ExecutorService executor) {
		doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) throws Exception {
				Object[] args = invocation.getArguments();
				Runnable runnable = (Runnable) args[0];
				runnable.run();
				return null;
			}
		}).when(executor).submit(any(Runnable.class));
	}

	@SuppressWarnings("unchecked")
	protected void mockTransactionTemplate(TransactionTemplate transactionTemplate) {
		doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) throws Exception {
				Object[] args = invocation.getArguments();
				TransactionCallback transactionCallback = (TransactionCallback) args[0];
				transactionCallback.doInTransaction(new SimpleTransactionStatus());
				return null;
			}
		}).when(transactionTemplate).execute(any(TransactionCallback.class));
	}
}
