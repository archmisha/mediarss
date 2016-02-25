package rss.mail;

import java.util.List;

/**
 * User: Michael Dikman
 * Date: 01/12/12
 * Time: 19:50
 */
public interface EmailService {

    void notifyOfFailedJob(String msg);

    void notifyOfError(String message);

    void notifyToAdmins(EmailClassification classification, String msg, String errorMsg);

    void sendEmail(String recipient, EmailClassification classification, String message);

    void sendEmail(String recipient, EmailClassification classification, String message, String errorMessage);

    void sendEmail(List<String> recipients, EmailClassification classification, String message);
}