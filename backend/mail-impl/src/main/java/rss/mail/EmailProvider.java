package rss.mail;

import javax.mail.internet.InternetAddress;
import java.util.List;

/**
 * User: dikmanm
 * Date: 07/03/2015 12:07
 */
interface EmailProvider {
    void send(final InternetAddress from, final String username, final String password,
              String recipientEmail, List<String> ccRecipientEmails, String title, String message);
}
