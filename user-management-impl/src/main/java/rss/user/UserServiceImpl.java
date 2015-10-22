package rss.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import rss.cache.UserCacheService;
import rss.environment.Environment;
import rss.environment.ServerMode;
import rss.environment.UrlService;
import rss.mail.EmailClassification;
import rss.mail.EmailConsts;
import rss.mail.EmailService;
import rss.user.dao.UserDao;
import rss.user.dao.UserImpl;
import rss.util.StringUtils2;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * User: Michael Dikman
 * Date: 15/12/12
 * Time: 12:11
 */
@Service
public class UserServiceImpl implements UserService {

    private static final Object registerSync = new Object();

    private static final String ACCOUNT_VALIDATION_LINK_SENT_MESSAGE = "Account validation link was sent to your email address";
    private static final String PASSWORD_RECOVERY_SENT_MESSAGE = "Password recovery email was sent to your email account";
    private static final String USER_CREATED_WITHOUT_VALIDATION_MESSAGE = "User created without email notification and validation";

    @Autowired
    private UserDao userDao;

    @Autowired
    private EmailService emailService;

    @Autowired
    private UrlService urlService;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    private UserCacheService userCacheService;

    // no need for a transaction here, but inside creating a new transaction
    @Override
    @Transactional(propagation = Propagation.SUPPORTS)
    public String register(String firstName, String lastName, final String email, final String password, boolean isAdmin) {
        User userOnServer = userDao.findByEmail(email);
        if (userOnServer != null) {
            throw new EmailAlreadyRegisteredException("Email already registered");
        }

        synchronized (registerSync) {
            return registerHelper(firstName, lastName, email, password, isAdmin);
        }
    }

    private String registerHelper(final String firstName, final String lastName, final String email, final String password, final boolean isAdmin) {
        final User user = transactionTemplate.execute(new TransactionCallback<User>() {
            @Override
            public User doInTransaction(TransactionStatus arg0) {
                // double checking inside the lock
                User userOnServer = userDao.findByEmail(email);
                if (userOnServer != null) {
                    throw new EmailAlreadyRegisteredException("Email already registered");
                }

                User user = new UserImpl();
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
                user.setAdmin(isAdmin);
                userDao.persist(user);
                return user;
            }
        });

        userCacheService.addUser(user);

        if (Environment.getInstance().getServerMode() != ServerMode.TEST) {
            try {
                emailService.notifyToAdmins(EmailClassification.NEW_USER,
                        "New user subscribed: " + user.getEmail(),
                        "Failed sending email about a new user");
                sendAccountValidationLink(user);
                return ACCOUNT_VALIDATION_LINK_SENT_MESSAGE;
            } catch (Exception e) {
                transactionTemplate.execute(new TransactionCallbackWithoutResult() {
                    @Override
                    protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
                        User persistedUser = userDao.find(user.getId());
                        userDao.delete(persistedUser);
                        userCacheService.invalidateUser(persistedUser);
                    }
                });
                throw new RuntimeException("Failed sending emails on user registration: " + e.getMessage(), e);
            }
        } else {
            return USER_CREATED_WITHOUT_VALIDATION_MESSAGE;
        }
    }

    @Override
    public ForgotPasswordResult forgotPassword(User user) {
        if (user.isValidated()) {
            emailService.sendEmail(user.getEmail(), EmailClassification.PASSWORD_RECOVERY,
                    "You password is: " + user.getPassword() + "\r\n\r\n" +
                            "If you never requested password recovery please ignore this email. We are sorry for the inconvenience\r\n\r\n" +
                            "For support or questions you can reply to this email.");
            return new ForgotPasswordResult(PASSWORD_RECOVERY_SENT_MESSAGE);
        } else {
            sendAccountValidationLink(user);
            return new ForgotPasswordResult(ACCOUNT_VALIDATION_LINK_SENT_MESSAGE);
        }
    }

    @Override
    public void sendAccountValidationLink(User user) {
        emailService.sendEmail(user.getEmail(), EmailClassification.NONE,
                "We are really happy you decided to use " + EmailConsts.APP_NAME + ".\r\n\r\n" +
                        "To activate your account follow this link: " +
                        urlService.getApplicationUrl() + "register/?" + UrlService.USER_ID_URL_PARAMETER + "=" + user.getId() +
                        "&" + UrlService.HASH_URL_PARAMETER + "=" + user.getValidationHash() + "\r\n\r\n" +
                        "If you never registered to " + EmailConsts.APP_NAME + " please ignore this email. We are sorry for the inconvenience\r\n\r\n" +
                        "For support or questions you can reply to this email.",
                "Failed sending account validation email to user");
    }

    @Override
    public void sendEmailToAllUsers(String message) {
        List<String> emails = new ArrayList<>();
        for (User user : userDao.findAll()) {
            emails.add(user.getEmail());
        }

        emailService.sendEmail(emails, EmailClassification.ANNOUNCEMENT, message);
    }

    public String getMoviesRssFeed(User user) {
        return urlService.getApplicationUrl() + String.format(UrlService.PERSONAL_RSS_FEED_URL, user.getId(), UrlService.MOVIES_RSS_FEED_TYPE, user.getFeedHash());
    }

    public String getTvShowsRssFeed(User user) {
        return urlService.getApplicationUrl() + String.format(UrlService.PERSONAL_RSS_FEED_URL, user.getId(), UrlService.TV_SHOWS_RSS_FEED_TYPE, user.getFeedHash());
    }

    @Override
    public User getUser(long userId) {
        return userDao.find(userId);
    }

    @Override
    public List<User> getAllUsers() {
        return userDao.findAll();
    }

    @Override
    public void updateUser(User user) {
        userDao.merge(user);
    }

    @Override
    public User find(long userId) {
        return userDao.find(userId);
    }

    @Override
    public User findByEmail(String email) {
        return userDao.findByEmail(email);
    }
}
