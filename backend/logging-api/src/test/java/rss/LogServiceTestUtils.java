package rss;

import org.junit.Assert;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import rss.log.LogService;

/**
 * User: dikmanm
 * Date: 17/10/2015 19:54
 */
public class LogServiceTestUtils {

    public static void mockLogService(LogService logService) {
        // debug
        Mockito.doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocationOnMock) throws Throwable {
                String msg = (String) invocationOnMock.getArguments()[1];
                System.out.println(msg);
                return null;
            }
        }).when(logService).debug(Matchers.any(Class.class), Matchers.anyString());

        // info
        Mockito.doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocationOnMock) throws Throwable {
                String msg = (String) invocationOnMock.getArguments()[1];
                System.out.println(msg);
                return null;
            }
        }).when(logService).info(Matchers.any(Class.class), Matchers.anyString());

        // warn
        Mockito.doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocationOnMock) throws Throwable {
                String msg = (String) invocationOnMock.getArguments()[1];
                System.out.println(msg);
                return null;
            }
        }).when(logService).warn(Matchers.any(Class.class), Matchers.anyString());

        // error
        Mockito.doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocationOnMock) throws Throwable {
                String msg = (String) invocationOnMock.getArguments()[1];
                Exception ex = (Exception) invocationOnMock.getArguments()[2];
                System.out.println(msg);
                ex.printStackTrace();
                Assert.fail();
                return Matchers.any();
            }
        }).when(logService).error(Matchers.any(Class.class), Matchers.anyString(), Matchers.any(Exception.class));

        Mockito.doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocationOnMock) throws Throwable {
                String msg = (String) invocationOnMock.getArguments()[1];
                System.out.println(msg);
                Assert.fail();
                return Matchers.any();
            }
        }).when(logService).error(Matchers.any(Class.class), Matchers.anyString());
    }
}
