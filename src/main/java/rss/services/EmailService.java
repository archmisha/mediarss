package rss.services;

import rss.entities.Show;
import rss.entities.User;
import rss.services.downloader.MovieRequest;
import rss.services.requests.EpisodeRequest;
import rss.services.requests.ShowRequest;

import java.util.Collection;

/**
 * User: Michael Dikman
 * Date: 01/12/12
 * Time: 19:50
 */
public interface EmailService {

    void notifyNewUserRegistered(User user);

    void sendAccountValidationLink(User user);

    void notifyOfMissingEpisode(ShowRequest torrentRequest);

    void notifyOfMissingMovie(MovieRequest torrentRequest);

	void notifyOfMissingEpisodes(Collection<ShowRequest> missingRequests);

	void notifyOfMissingMovies(Collection<MovieRequest> missingRequests);

	void sendPasswordRecoveryEmail(User user);

	void sendEmailToAllUsers(String text);

	void notifyOfATicket(User user, String type, String content);

	void notifyOfFailedJob(String msg);

	void notifyOfError(String message);
}
