package rss.mail;

import javax.mail.internet.InternetAddress;

/**
 * User: dikmanm
 * Date: 07/03/2015 12:23
 */
public class EmailJSON {
    private InternetAddress from;
    private String title;
    private String message;

    public void setFrom(InternetAddress from) {
        this.from = from;
    }

    public InternetAddress getFrom() {
        return from;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
