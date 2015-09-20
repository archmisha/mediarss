package rss.test.entities;

import java.util.Date;

/**
 * User: dikmanm
 * Date: 06/03/2015 00:03
 */
public class UserResult {
    private long id;
    private boolean isLoggedIn;
    private String email;
    private String firstName;
    private String lastName;
    private boolean admin;
    private String subtitles;
    private Date lastLogin;
    private Date lastShowsFeedGenerated;
    private Date lastMoviesFeedGenerated;
    private String validationHash;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public boolean isLoggedIn() {
        return isLoggedIn;
    }

    public void setLoggedIn(boolean isLoggedIn) {
        this.isLoggedIn = isLoggedIn;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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

    public boolean isAdmin() {
        return admin;
    }

    public void setAdmin(boolean admin) {
        this.admin = admin;
    }

    public String getSubtitles() {
        return subtitles;
    }

    public void setSubtitles(String subtitles) {
        this.subtitles = subtitles;
    }

    public Date getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(Date lastLogin) {
        this.lastLogin = lastLogin;
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

    public String getValidationHash() {
        return validationHash;
    }

    public void setValidationHash(String validationHash) {
        this.validationHash = validationHash;
    }
}
