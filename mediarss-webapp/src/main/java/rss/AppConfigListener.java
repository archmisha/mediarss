package rss;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import rss.content.ContentLoader;
import rss.environment.Environment;
import rss.environment.SettingsUpdateListener;
import rss.log.LogService;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * User: Michael Dikman
 * Date: 11/05/12
 * Time: 13:58
 */
public class AppConfigListener implements ServletContextListener {
    private static Logger LOGGER = LogManager.getLogger(AppConfigListener.class);
    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss:SSS");

    @Autowired
    private LogService logService;

    @Autowired
    private List<ContentLoader> contentLoaders;

    private ScheduledExecutorService logMemoryExecutorService;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        // debug
        WebApplicationContext springContext = WebApplicationContextUtils.getRequiredWebApplicationContext(sce.getServletContext());
        LOGGER.debug("springContext.getId()=" + springContext.getId());
        AutowireCapableBeanFactory autowireCapableBeanFactory = springContext.getAutowireCapableBeanFactory();
        autowireCapableBeanFactory.autowireBean(this);

        LOGGER.info("Server is started in " + Environment.getInstance().getServerMode() + " mode");

        Environment.getInstance().setDeploymentDate(getDeploymentDate());
        Environment.getInstance().setStartupDate(new Date());

        if (Environment.getInstance().isLogMemory()) {
            startMemoryPrinter();
        }

        Environment.getInstance().addUpdateListener(new SettingsUpdateListener() {
            @Override
            public void onSettingsUpdated() {
                if (Environment.getInstance().isLogMemory() && logMemoryExecutorService == null) {
                    startMemoryPrinter();
                } else if (!Environment.getInstance().isLogMemory() && logMemoryExecutorService != null) {
                    stopMemoryPrinter();
                }
            }
        });

        for (ContentLoader contentLoader : contentLoaders) {
            if (contentLoader.getSupportedModes().contains(Environment.getInstance().getServerMode())) {
                contentLoader.load();
            }
        }
    }

    private void stopMemoryPrinter() {
        LOGGER.info("Stopping memory printer task");
        logMemoryExecutorService.shutdown();
        logMemoryExecutorService = null;
    }

    private void startMemoryPrinter() {
        LOGGER.info("Starting memory printer task");
        logMemoryExecutorService = Executors.newSingleThreadScheduledExecutor();
        logMemoryExecutorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                printMemoryStats();
            }
        }, 0, 1, TimeUnit.MINUTES);
    }

    private void printMemoryStats() {
        int mb = 1024 * 1024;
        Runtime runtime = Runtime.getRuntime();

        logService.info(getClass(), "##### Heap utilization statistics [MB] #####");
        logService.info(getClass(), "Used Memory:" + (runtime.totalMemory() - runtime.freeMemory()) / mb);
        logService.info(getClass(), "Free Memory:" + runtime.freeMemory() / mb);
        logService.info(getClass(), "Total Memory:" + runtime.totalMemory() / mb);
        logService.info(getClass(), "Max Memory:" + runtime.maxMemory() / mb);

        logService.info(getClass(), "##### Heap utilization statistics [MB] ##### 2");
        ManagementFactory.getMemoryMXBean().getHeapMemoryUsage();
        logService.info(getClass(), "Heap " + ManagementFactory.getMemoryMXBean().getHeapMemoryUsage());
        logService.info(getClass(), "NonHeap " + ManagementFactory.getMemoryMXBean().getNonHeapMemoryUsage());
        logService.info(getClass(), "Threads " + ManagementFactory.getThreadMXBean().getThreadCount());
        logService.info(getClass(), "Peak Threads " + ManagementFactory.getThreadMXBean().getPeakThreadCount());
        List<MemoryPoolMXBean> beans = ManagementFactory.getMemoryPoolMXBeans();
        for (MemoryPoolMXBean bean : beans) {
            logService.info(getClass(), bean.getName() + " " + bean.getUsage());
        }

        for (GarbageCollectorMXBean bean : ManagementFactory.getGarbageCollectorMXBeans()) {
            logService.info(getClass(), bean.getName() + " " + bean.getCollectionCount() + " " + bean.getCollectionTime());
        }
    }

    private Date getDeploymentDate() {
        Date deployedDate = new Date(); // better than null, even if wrong
        try {
            String databaseProperties = System.getProperty("path-locator.txt");
            if (databaseProperties == null) {
                databaseProperties = "path-locator.txt";
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
            LOGGER.error(e.getMessage(), e);
        }
        return deployedDate;
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        if (logMemoryExecutorService != null) {
            stopMemoryPrinter();
        }

        // shutting spring
        BeanFactory bf = ContextLoader.getCurrentWebApplicationContext();
        if (bf instanceof ConfigurableApplicationContext) {
            ((ConfigurableApplicationContext) bf).close();
        }

        Environment.getInstance().shutdown();

        try {
            LOGGER.info("Sleeping 1000ms to finish shutting down");
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            LOGGER.info(e.getMessage(), e);
        }
    }
}
