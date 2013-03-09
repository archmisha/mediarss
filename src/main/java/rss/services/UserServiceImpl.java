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
import rss.controllers.EntityConverter;
import rss.controllers.vo.ShowVO;
import rss.controllers.vo.UserResponse;
import rss.dao.UserDao;
import rss.entities.User;
import rss.services.movies.MovieService;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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

	@Autowired
	private EntityConverter entityConverter;

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
				SecureRandom random = new SecureRandom();
				user.setValidationHash(new BigInteger(130, random).toString(32));
				user.setFeedHash(new BigInteger(130, random).toString(32));
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

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public UserResponse getUserResponse(User user) {
		String tvShowsRssFeed = urlService.getApplicationUrl() + String.format(UrlService.PERSONAL_RSS_FEED_URL, user.getId(), UrlService.TV_SHOWS_RSS_FEED_TYPE, user.getFeedHash());
		String moviesRssFeed = urlService.getApplicationUrl() + String.format(UrlService.PERSONAL_RSS_FEED_URL, user.getId(), UrlService.MOVIES_RSS_FEED_TYPE, user.getFeedHash());
		return new UserResponse(entityConverter.toThinUser(user), tvShowsRssFeed, moviesRssFeed, sort(entityConverter.toThinShows(user.getShows())));
	}

	private ArrayList<ShowVO> sort(ArrayList<ShowVO> shows) {
		Collections.sort(shows, new Comparator<ShowVO>() {
			@Override
			public int compare(ShowVO o1, ShowVO o2) {
				return o1.getName().compareToIgnoreCase(o2.getName());
			}
		});
		return shows;
	}
}
