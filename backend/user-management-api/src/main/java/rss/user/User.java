package rss.user;

import rss.user.subtitles.SubtitleLanguage;

import java.util.Date;

/**
 * User: Michael Dikman
 * Date: 25/11/12
 * Time: 11:58
 */
public interface User {

    long getId();

    void setCreated(Date created);

    Date getLastLogin();

    void setLastLogin(Date lastLogin);

    String getEmail();

    void setEmail(String email);

    void setPassword(String password);

    String getPassword();

    Date getLastShowsFeedGenerated();

    void setLastShowsFeedGenerated(Date lastShowsFeedGenerated);

    Date getLastMoviesFeedGenerated();

    void setLastMoviesFeedGenerated(Date lastMoviesFeedGenerated);

    boolean isValidated();

    void setValidationHash(String validationHash);

    String getValidationHash();

    String toString();

    void setSubtitles(SubtitleLanguage subtitles);

    SubtitleLanguage getSubtitles();

    String getFeedHash();

    void setFeedHash(String feedHash);

    String getFirstName();

    void setFirstName(String firstName);

    String getLastName();

    void setLastName(String lastName);

    String getLoginToken();

    void setLoginToken(String loginToken);

    String getLoginSeries();

    void setLoginSeries(String loginSeries);

    Date getNewsDismiss();

    void setNewsDismiss(Date newsDismiss);

    boolean isAdmin();

    void setAdmin(boolean isAdmin);

    boolean equals(Object o);

    int hashCode();
}