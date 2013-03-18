package rss.services;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rss.dao.UserDao;
import rss.entities.Show;
import rss.entities.User;
import rss.services.downloader.MovieRequest;
import rss.util.GoogleMail;

import javax.mail.MessagingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * User: Michael Dikman
 * Date: 01/12/12
 * Time: 19:50
 */
@Service
public class EmailServiceImpl implements EmailService {

	public static final String APP_NAME = "Personalized Media RSS";
	public static final String EMAIL_SIGNATURE = "\n\nMichael Dikman\n" + APP_NAME + " Team";
	public static final String JOBS_TITLE_SUFFIX = " - Jobs";
	public static final String USERS_TITLE_SUFFIX = " - Users";

	@Autowired
	private UrlService urlService;

	@Autowired
	private SettingsService settingsService;

	@Autowired
	private UserDao userDao;

	public void notifyNewUserRegistered(User user) {
		notifyToAdmins(
				USERS_TITLE_SUFFIX,
				"New user subscribed: " + user.getEmail(),
				"Failed sending email about a new user");
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
			logError("Failed sending email with account validation link to user", e);
		}
	}

	@Override
	public void notifyOfMissingEpisode(EpisodeRequest torrentRequest) {
		notifyOfMissingEpisodes(Collections.singletonList(torrentRequest));
	}

	@Override
	public void notifyOfMissingEpisodes(Collection<EpisodeRequest> missingRequests) {
		if (missingRequests.isEmpty()) {
			return;
		}

		notifyToAdmins(
				JOBS_TITLE_SUFFIX,
				"The following torrents were not found:\n  " + StringUtils.join(missingRequests, "\n  "),
				"Failed sending email of missing torrent");
	}

	@Override
	public void notifyOfMissingMovie(MovieRequest torrentRequest) {
		notifyOfMissingMovies(Collections.singletonList(torrentRequest));
	}

	@Override
	public void notifyOfMissingMovies(Collection<MovieRequest> missingRequests) {
		notifyToAdmins(
				JOBS_TITLE_SUFFIX,
				"The following torrents were not found:\n  " + StringUtils.join(missingRequests, "\n  "),
				"Failed sending email of missing torrent");
	}

	@Override
	public void notifyShowCreatedBlindly(Show show) {
		notifyToAdmins(
				JOBS_TITLE_SUFFIX,
				"The following show was created without verification of tv.com url: \n" +
				show + " - " + show.getTvComUrl(),
				"Failed sending email of missing torrent");
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
			logError("Failed sending email to user", e);
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
			logError("Failed sending email to user", e);
		}
	}

	@Override
	public void notifyOfATicket(User user, String type, String content) {
		notifyToAdmins(
				" - " + type,
				"User " + user.getFirstName() + " " + user.getLastName() + " (" + user.getEmail() +
				") has submitted the following " + type + ": \n" + content,
				"Failed sending email of a new ticket");
	}

	@Override
	public void notifyOfFailedJob(String msg) {
		notifyToAdmins(
				JOBS_TITLE_SUFFIX,
				msg,
				"Failed sending email of a failed job"
		);
	}

	private void notifyToAdmins(String titleSuffix, String msg, String errorMsg) {
		try {
			sendEmail(settingsService.getAdministratorEmails(), APP_NAME + titleSuffix,
					msg +
					EMAIL_SIGNATURE);
		} catch (MessagingException e) {
			logError(errorMsg, e);
		}
	}

	private void logError(String message, MessagingException e) {
		if (e.getMessage() != null) {
			message += " Error: " + e.getMessage();
		}
		throw new RuntimeException(message, e);
	}

	private void sendEmail(String recipient, String title, String message) throws MessagingException {
		GoogleMail.Send("lan4ear", "84ad17ad!", recipient, title, message);
	}

	private void sendEmail(List<String> recipients, String title, String message) throws MessagingException {
		if (recipients.size() == 1) {
			sendEmail(recipients.get(0), title, message);
			return;
		}

		GoogleMail.Send("lan4ear", "84ad17ad!", null, StringUtils.join(recipients, " "), title, message);
	}
}
