package rss.services;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rss.dao.UserDao;
import rss.entities.Show;
import rss.entities.User;
import rss.services.downloader.MovieRequest;
import rss.util.GoogleMail;

import javax.mail.MessagingException;
import java.util.*;

/**
 * User: Michael Dikman
 * Date: 01/12/12
 * Time: 19:50
 */
@Service
public class EmailServiceImpl implements EmailService {

	public static final String APP_NAME = "Personalized Media RSS";
	public static final String EMAIL_SIGNATURE = "\n\nMichael Dikman\n" + APP_NAME + " Team";
	private static Log log = LogFactory.getLog(EmailServiceImpl.class);

	@Autowired
	private UrlService urlService;

	@Autowired
	private UserDao userDao;

	public void notifyNewUserRegistered(User user) {
		try {
			sendEmail(getAdministratorEmails(), APP_NAME + " - Users",
					"New user subscribed: " + user.getEmail() +
					EMAIL_SIGNATURE);
		} catch (MessagingException e) {
			if (e.getMessage() != null) {
				throw new RuntimeException("Failed sending email about a new user. Error: " + e.getMessage(), e);
			} else {
				throw new RuntimeException("Failed sending email about a new user", e);
			}
		}
	}

	public void sendAccountValidationLink(User user) {
		try {
			sendEmail(user.getEmail(), APP_NAME,
					"We are really happy you decided to use " + APP_NAME + ".\r\n\r\n" +
					"To activate your account follow this link: " +
					urlService.getApplicationUrl() + "register/?" + UrlService.USER_ID_URL_PARAMETER + "=" + user.getId() +
					"&" + UrlService.HASH_URL_PARAMETER + "=" + user.getValidationHash() + "\r\n\r\n" +
					"If you never registered to " + APP_NAME + " please ignore this email. We are sorry for the inconvenience\r\n\r\n" +
					"For support or questions you can reply to this email." +
					EMAIL_SIGNATURE);
		} catch (MessagingException e) {
			if (e.getMessage() != null) {
				throw new RuntimeException("Failed sending email with account validation link to user. Error: " + e.getMessage(), e);
			} else {
				throw new RuntimeException("Failed sending email with account validation link to user", e);
			}
		}
	}

	@Override
	public void notifyOfMissingEpisode(EpisodeRequest torrentRequest) {
		notifyOfMissingEpisodes(Collections.singletonList(torrentRequest));
	}

	@Override
	public void notifyOfMissingEpisodes(Collection<EpisodeRequest> missingRequests) {
		try {
			sendEmail(getAdministratorEmails(), APP_NAME + " - Jobs",
					"The following torrents were not found:\n  " + StringUtils.join(missingRequests, "\n  ") +
					EMAIL_SIGNATURE);
		} catch (MessagingException e) {
			if (e.getMessage() != null) {
				throw new RuntimeException("Failed sending email of missing torrent. Error: " + e.getMessage(), e);
			} else {
				throw new RuntimeException("Failed sending email of missing torrent", e);
			}
		}
	}

	@Override
	public void notifyOfMissingMovie(MovieRequest torrentRequest) {
		notifyOfMissingMovies(Collections.singletonList(torrentRequest));
	}

	@Override
	public void notifyOfMissingMovies(Collection<MovieRequest> missingRequests) {
		try {
			sendEmail(getAdministratorEmails(), APP_NAME,
					"The following torrents were not found:\n  " + StringUtils.join(missingRequests, "\n  ") +
					EMAIL_SIGNATURE);
		} catch (MessagingException e) {
			if (e.getMessage() != null) {
				throw new RuntimeException("Failed sending email of missing torrent. Error: " + e.getMessage(), e);
			} else {
				throw new RuntimeException("Failed sending email of missing torrent", e);
			}
		}
	}

	@Override
	public void notifyShowCreatedBlindly(Show show) {
		try {
			sendEmail(getAdministratorEmails(), APP_NAME,
					"The following show was created without verification of tv.com url: \n" +
					show + " - " + show.getTvComUrl() +
					EMAIL_SIGNATURE);
		} catch (MessagingException e) {
			if (e.getMessage() != null) {
				throw new RuntimeException("Failed sending email of missing torrent. Error: " + e.getMessage(), e);
			} else {
				throw new RuntimeException("Failed sending email of missing torrent", e);
			}
		}
	}

	@Override
	public void sendPasswordRecoveryEmail(User user) {
		try {
			sendEmail(user.getEmail(), APP_NAME + " - password recovery",
					"You password is: " + user.getPassword() + "\r\n\r\n" +
					"If you never requested password recovery please ignore this email. We are sorry for the inconvenience\r\n\r\n" +
					"For support or questions you can reply to this email." +
					EMAIL_SIGNATURE);
		} catch (MessagingException e) {
			if (e.getMessage() != null) {
				throw new RuntimeException("Failed sending email to user. Error: " + e.getMessage(), e);
			} else {
				throw new RuntimeException("Failed sending email to user", e);
			}
		}
	}

	@Override
	public void sendEmailToAllUsers(String text) {
		try {
			List<String> emails = new ArrayList<>();
			for (User user : userDao.findAll()) {
				emails.add(user.getEmail());
			}

			sendEmail(emails, APP_NAME + " - Announcement", text + EMAIL_SIGNATURE);
		} catch (MessagingException e) {
			if (e.getMessage() != null) {
				throw new RuntimeException("Failed sending email to user. Error: " + e.getMessage(), e);
			} else {
				throw new RuntimeException("Failed sending email to user", e);
			}
		}
	}

	private void sendEmail(String recipient, String title, String message) throws MessagingException {
		GoogleMail.Send("lan4ear", "84ad17ad!", recipient, title, message);
	}

	private void sendEmail(List<String> recipients, String title, String message) throws MessagingException {
		if (recipients.size() == 1) {
			sendEmail(recipients.get(0), title, message);
			return;
		}

		List<String> recipientsCopy = new ArrayList<>(recipients);
		GoogleMail.Send("lan4ear", "84ad17ad!", recipientsCopy.remove(0), StringUtils.join(recipientsCopy, " "), title, message);
	}

	private List<String> getAdministratorEmails() {
		return Arrays.asList("archmisha@gmail.com");
	}
}
