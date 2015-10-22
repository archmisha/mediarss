package rss.user;

import java.util.List;

/**
 * User: Michael Dikman
 * Date: 15/12/12
 * Time: 12:10
 */
public interface UserService {

    String register(String firstName, String lastName, final String email, final String password, boolean isAdmin);

	String getMoviesRssFeed(User user);

	String getTvShowsRssFeed(User user);

	User getUser(long userId);

    ForgotPasswordResult forgotPassword(User user);

    void sendEmailToAllUsers(String message);

    void sendAccountValidationLink(User user);

    List<User> getAllUsers();

    void updateUser(User user);

    User find(long userId);

    User findByEmail(String email);
}
