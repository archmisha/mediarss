package rss.mail;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import org.springframework.stereotype.Service;

import javax.mail.internet.InternetAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * User: dikmanm
 * Date: 07/03/2015 12:07
 */
@Service
class TestEmailProvider implements EmailProvider {

    private Multimap<String, EmailJSON> emails = HashMultimap.create();

    @Override
    public void send(InternetAddress from, String username, String password, String recipientEmail, List<String> ccRecipientEmails, String title, String message) {
        EmailJSON emailJSON = new EmailJSON();
        emailJSON.setFrom(from);
        emailJSON.setTitle(title);
        emailJSON.setMessage(message);

        List<String> recipients = new ArrayList<>();
        recipients.add(recipientEmail);
        recipients.addAll(ccRecipientEmails);
        for (String recipient : recipients) {
            emails.put(recipient, emailJSON);
        }
    }

    public Collection<EmailJSON> getByEmail(String email) {
        return emails.get(email);
    }
}
