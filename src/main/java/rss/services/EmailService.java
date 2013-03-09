package rss.services;

import rss.entities.Show;
import rss.entities.User;
import rss.services.downloader.MovieRequest;

import java.util.Collection;

/**
 * User: Michael Dikman
 * Date: 01/12/12
 * Time: 19:50
 */
public interface EmailService {

    void notifyNewUserRegistered(User user);

    void sendAccountValidationLink(User user);

    void notifyOfMissingEpisode(EpisodeRequest torrentRequest);

    void notifyOfMissingMovie(MovieRequest torrentRequest);

	void notifyOfMissingEpisodes(Collection<EpisodeRequest> missingRequests);

	void notifyOfMissingMovies(Collection<MovieRequest> missingRequests);

	void notifyShowCreatedBlindly(Show show);

	void sendPasswordRecoveryEmail(User user);

	void sendEmailToAllUsers(String text);
}
