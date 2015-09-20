package rss.mail;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rss.environment.Environment;
import rss.environment.ServerMode;

import javax.mail.internet.InternetAddress;
import java.util.Collections;
import java.util.List;

/**
 * User: Michael Dikman
 * Date: 01/12/12
 * Time: 19:50
 */
@Service
public class EmailServiceImpl implements EmailService {

    private static final String EMAIL_SIGNATURE = "\n\n" + EmailConsts.APP_NAME + " Team";
    private static final String MEDIA_RSS_GROUP_EMAIL = "media-rss@googlegroups.com";

    @Autowired
    private EmailProvider emailProvider;

    @Override
    public void notifyOfFailedJob(String msg) {
        notifyToAdmins(
                EmailClassification.JOB,
                msg,
                "Failed sending email of a failed job"
        );
    }

    @Override
    public void notifyOfError(String msg) {
        notifyToAdmins(
                EmailClassification.ERROR,
                msg,
                "Failed sending email of an error"
        );
    }

    @Override
    public void notifyToAdmins(EmailClassification classification, String msg, String errorMsg) {
        // don't send admins notifications from dev env
        if (Environment.getInstance().getServerMode() != ServerMode.PROD) {
            return;
        }

        sendEmail(MEDIA_RSS_GROUP_EMAIL, classification, msg, errorMsg);
    }

    @Override
    public void sendEmail(String recipient, EmailClassification classification, String message) {
        sendEmail(Collections.singletonList(recipient), classification, message, "Failed sending email to user");
    }

    @Override
    public void sendEmail(String recipient, EmailClassification classification, String message, String errorMessage) {
        sendEmail(Collections.singletonList(recipient), classification, message, errorMessage);
    }

    @Override
    public void sendEmail(List<String> recipients, EmailClassification classification, String message) {
        sendEmail(recipients, classification, message, "Failed sending email to users");
    }

    // "gabayshiran", "ahri24986", lan4ear, 84ad17ad!, personal.media.rss, 83md16md
    private void sendEmail(List<String> recipients, EmailClassification classification, String message, String errorMessage) {
        try {
            String title = EmailConsts.APP_NAME;
            if (classification != EmailClassification.NONE) {
                title += " - " + classification.toString();
            }

            emailProvider.send(new InternetAddress("personal.media.rss@gmail.com", "Media-RSS Team"), "lan4ear", "84ad17ad!",
                    null, recipients, title, message + EMAIL_SIGNATURE);
        } catch (Exception e) {
            logError(recipients, errorMessage, e);
        }
    }

    private void logError(List<String> recipients, String message, Exception e) {
        message += ": " + StringUtils.join(recipients, ", ");
        if (e.getMessage() != null) {
            message += " Error: " + e.getMessage();
        }
        throw new RuntimeException(message, e);
    }
}
