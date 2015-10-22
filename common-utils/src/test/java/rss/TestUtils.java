package rss;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.transaction.support.SimpleTransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.concurrent.ExecutorService;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;

/**
 * User: dikmanm
 * Date: 17/10/2015 19:54
 */
public class TestUtils {

    public void mockExecutorServiceAsDirectExecutor(ExecutorService executor) {
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
    public void mockTransactionTemplate(TransactionTemplate transactionTemplate) {
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
