package rss.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import rss.EmailAlreadyRegisteredException;
import rss.dao.UserDao;
import rss.entities.User;
import rss.util.StringUtils2;

import java.util.Date;

/**
 * User: Michael Dikman
 * Date: 15/12/12
 * Time: 12:11
 */
@Service
public class UserServiceImpl implements UserService {

	private static final Object registerSync = new Object();

	public static final String ACCOUNT_VALIDATION_LINK_SENT_MESSAGE = "Account validation link was sent to your email address";

	@Autowired
	private UserDao userDao;

	@Autowired
	private EmailService emailService;

	@Autowired
	private UrlService urlService;

	@Autowired
	private TransactionTemplate transactionTemplate;

	// no need for a transaction here, but inside creating a new transaction
	@Override
	@Transactional(propagation = Propagation.SUPPORTS)
	public String register(String firstName, String lastName, final String email, final String password) {
		User userOnServer = userDao.findByEmail(email);
		if (userOnServer != null) {
			throw new EmailAlreadyRegisteredException("Email already registered");
		}

		synchronized (registerSync) {
			return registerHelper(firstName, lastName, email, password);
		}
	}

	private String registerHelper(final String firstName, final String lastName, final String email, final String password) {
		final User user = transactionTemplate.execute(new TransactionCallback<User>() {
			@Override
			public User doInTransaction(TransactionStatus arg0) {
				// double checking inside the lock
				User userOnServer = userDao.findByEmail(email);
				if (userOnServer != null) {
					throw new EmailAlreadyRegisteredException("Email already registered");
				}

				User user = new User();
				user.setFirstName(firstName);
				user.setLastName(lastName);
				user.setEmail(email);
				user.setPassword(password);
				user.setCreated(new Date());
				user.setLastShowsFeedGenerated(null);
				user.setLastMoviesFeedGenerated(null);
				user.setSubtitles(null);
				user.setValidationHash(StringUtils2.generateUniqueHash());
				user.setFeedHash(StringUtils2.generateUniqueHash());
				userDao.persist(user);
				return user;
			}
		});

		try {
			emailService.notifyNewUserRegistered(user);
			emailService.sendAccountValidationLink(user);
			return ACCOUNT_VALIDATION_LINK_SENT_MESSAGE;
		} catch (Exception e) {
			transactionTemplate.execute(new TransactionCallbackWithoutResult() {
				@Override
				protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
					User persistedUser = userDao.find(user.getId());
					userDao.delete(persistedUser);
				}
			});
			throw new RuntimeException("Failed sending emails on user registration: " + e.getMessage(), e);
		}
	}

	public String getMoviesRssFeed(User user) {
		return urlService.getApplicationUrl() + String.format(UrlService.PERSONAL_RSS_FEED_URL, user.getId(), UrlService.MOVIES_RSS_FEED_TYPE, user.getFeedHash());
	}

	public String getTvShowsRssFeed(User user) {
		return urlService.getApplicationUrl() + String.format(UrlService.PERSONAL_RSS_FEED_URL, user.getId(), UrlService.TV_SHOWS_RSS_FEED_TYPE, user.getFeedHash());
	}
}
