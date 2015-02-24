package rss.trakt;

import rss.rms.RmsResource;

import java.util.Calendar;

/**
 * User: dikmanm
 * Date: 16/02/2015 14:44
 */
public class TraktAuthJson implements RmsResource {

    private String id;
    private long created;
    private long userId;
    private String accessToken;
    private String refreshToken;
    private long expiresIn;

    public TraktAuthJson() {
        created = Calendar.getInstance().getTime().getTime();
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public long getCreated() {
        return created;
    }

    public void setCreated(long created) {
        this.created = created;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public long getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(long expiresIn) {
        this.expiresIn = expiresIn;
    }
}
