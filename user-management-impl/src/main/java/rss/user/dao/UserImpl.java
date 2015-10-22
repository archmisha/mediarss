package rss.user.dao;

import org.hibernate.annotations.Index;
import rss.ems.entities.BaseEntity;
import rss.user.User;
import rss.user.subtitles.SubtitleLanguage;

import javax.persistence.Column;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import java.util.Date;

/**
 * User: dikmanm
 * Date: 18/10/2015 19:54
 */
@javax.persistence.Entity(name = "User")
@javax.persistence.Table(name = "user")
@NamedQueries({
        @NamedQuery(name = "User.findByEmail",
                query = "select b from User as b where lower(b.email) = :email")/*,
        @NamedQuery(name = "User.findByTrackedShow",
                query = "select b from User as b inner join b.shows as s where s.id = :showId")*/
})
public class UserImpl extends BaseEntity implements User {

    private static final long serialVersionUID = -8612852113678825310L;

    @Column(name = "email", unique = true)
    @Index(name = "email_idx")
    private String email;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "password")
    private String password;

    @Column(name = "validation_hash")
    private String validationHash;

    @Column(name = "feed_hash")
    private String feedHash;

    @Column(name = "last_shows_feed_generated")
    private Date lastShowsFeedGenerated;

    @Column(name = "last_movies_feed_generated")
    private Date lastMoviesFeedGenerated;

    @Column(name = "login_token")
    private String loginToken;

    @Column(name = "login_series")
    private String loginSeries;

    @Column(name = "last_login")
    private Date lastLogin;

    @Column(name = "news_dismiss")
    private Date newsDismiss;

    @Column(name = "admin")
    private boolean isAdmin;

    @Column(name = "subtitles")
    private SubtitleLanguage subtitles;

    public Date getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(Date lastLogin) {
        this.lastLogin = lastLogin;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPassword() {
        return password;
    }

    public Date getLastShowsFeedGenerated() {
        return lastShowsFeedGenerated;
    }

    public void setLastShowsFeedGenerated(Date lastShowsFeedGenerated) {
        this.lastShowsFeedGenerated = lastShowsFeedGenerated;
    }

    public Date getLastMoviesFeedGenerated() {
        return lastMoviesFeedGenerated;
    }

    public void setLastMoviesFeedGenerated(Date lastMoviesFeedGenerated) {
        this.lastMoviesFeedGenerated = lastMoviesFeedGenerated;
    }

    public boolean isValidated() {
        return validationHash == null;
    }

    public void setValidationHash(String validationHash) {
        this.validationHash = validationHash;
    }

    public String getValidationHash() {
        return validationHash;
    }

    @Override
    public String toString() {
        return email + " (userId=" + id + ")";
    }

    public void setSubtitles(SubtitleLanguage subtitles) {
        this.subtitles = subtitles;
    }

    public SubtitleLanguage getSubtitles() {
        return subtitles;
    }

    public String getFeedHash() {
        return feedHash;
    }

    public void setFeedHash(String feedHash) {
        this.feedHash = feedHash;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getLoginToken() {
        return loginToken;
    }

    public void setLoginToken(String loginToken) {
        this.loginToken = loginToken;
    }

    public String getLoginSeries() {
        return loginSeries;
    }

    public void setLoginSeries(String loginSeries) {
        this.loginSeries = loginSeries;
    }

    public Date getNewsDismiss() {
        return newsDismiss;
    }

    public void setNewsDismiss(Date newsDismiss) {
        this.newsDismiss = newsDismiss;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public void setAdmin(boolean isAdmin) {
        this.isAdmin = isAdmin;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BaseEntity that = (BaseEntity) o;

        if (id != that.getId()) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return (int) (id ^ (id >>> 32));
    }
}
