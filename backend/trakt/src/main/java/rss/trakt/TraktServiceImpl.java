package rss.trakt;

import com.google.gson.annotations.SerializedName;
import com.mongodb.client.model.Filters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rss.PageDownloader;
import rss.user.context.UserContextHolder;
import rss.environment.Environment;
import rss.environment.ServerMode;
import rss.log.LogService;
import rss.rms.ResourceManagementService;
import rss.rms.query.RmsQueryInformation;
import rss.util.JsonTranslation;

import java.util.HashMap;
import java.util.Map;

/**
 * User: dikmanm
 * Date: 16/02/2015 00:06
 */
@Service
public class TraktServiceImpl implements TraktService {

    public static final String CLIENT_ID = "a84e93a6f9cd9ef59cee83704b6fc76796c6b5dec2a6b172a0541296e9881c59";
    public static final String CLIENT_SECRET = "1a32bdc21d5e0213ac76449f3e84a6b1e22897bf27d001018f22fdacfb3695f9";

    public static final String CLIENT_ID_TEST = "7a0e908fe281b383b2c043a09a4f5f58a100e60a298133a53ddd0c3c24f18601";
    public static final String CLIENT_SECRET_TEST = "907fe2dc532dd6fcfa5766e5a2d04b8a4ab3e3b911b1cec9af335b882b658589";

    @Autowired
    private PageDownloader pageDownloader;

    @Autowired
    private ResourceManagementService rmsService;

    @Autowired
    private LogService logService;

    @Override
    public String getClientId() {
        return Environment.getInstance().getServerMode() == ServerMode.PROD ? CLIENT_ID : CLIENT_ID_TEST;
    }

    public void authenticateUser(String code) {
        long userId = UserContextHolder.getCurrentUserContext().getUserId();

        Map<String, String> params = new HashMap<>();
        params.put("code", code);
        params.put("client_id", getClientId());
        params.put("client_secret", Environment.getInstance().getServerMode() == ServerMode.PROD ? CLIENT_SECRET : CLIENT_SECRET_TEST);
        params.put("redirect_uri", Environment.getInstance().getServerHostUrl() + "/main");
        params.put("grant_type", "authorization_code");

        TraktAuthTokenResponse traktAuthTokenResponse;
        if (Environment.getInstance().getServerMode() != ServerMode.TEST) {
            String response = pageDownloader.sendPostRequest("https://api-v2launch.trakt.tv/oauth/token", JsonTranslation.object2JsonString(params));
            traktAuthTokenResponse = JsonTranslation.jsonString2Object(response, TraktAuthTokenResponse.class);
        } else {
            traktAuthTokenResponse = new TraktAuthTokenResponse();
            traktAuthTokenResponse.setRefreshToken("testRefreshToken");
            traktAuthTokenResponse.setAccessToken("testAccessToken");
            traktAuthTokenResponse.setExpiresIn(999);
            traktAuthTokenResponse.setScope("public");
            traktAuthTokenResponse.setTokenType("bearer");
        }

        TraktAuthJson traktAuthJson = getTraktAuthJson(userId);
        if (traktAuthJson == null) {
            traktAuthJson = new TraktAuthJson();
            traktAuthJson.setUserId(userId);
        }
        traktAuthJson.setAccessToken(traktAuthTokenResponse.getAccessToken());
        traktAuthJson.setExpiresIn(traktAuthTokenResponse.getExpiresIn());
        traktAuthJson.setRefreshToken(traktAuthTokenResponse.getRefreshToken());
        rmsService.saveOrUpdate(traktAuthJson, TraktAuthJson.class);
    }

    @Override
    public void disconnectUser(long userId) {
        rmsService.delete(rmsService.factory().createDeleteResourceOperation(TraktAuthJson.class, getQueryInfoForUser(userId)));
    }

    private RmsQueryInformation getQueryInfoForUser(long userId) {
        return rmsService.factory().createRmsQueryBuilder()
                .filter(Filters.eq("userId", userId)).getRmsQueryInformation();
    }

    @Override
    public boolean isConnected(long userId) {
        TraktAuthJson traktAuthJson = getTraktAuthJson(userId);
        if (traktAuthJson == null) {
            return false;
        }

        // todo: check if expired and then try to renew
//        if (traktAuthJson.getExpiresIn())

        return traktAuthJson.getAccessToken() != null;
    }

    private TraktAuthJson getTraktAuthJson(long userId) {
        return rmsService.get(rmsService.factory()
                .createGetResourceOperation(TraktAuthJson.class, getQueryInfoForUser(userId)));
    }

    private class TraktAuthTokenResponse {
        @SerializedName("access_token")
        private String accessToken;
        @SerializedName("token_type")
        private String tokenType;
        @SerializedName("expires_in")
        private long expiresIn;
        @SerializedName("refresh_token")
        private String refreshToken;
        private String scope;

        public String getAccessToken() {
            return accessToken;
        }

        public void setAccessToken(String accessToken) {
            this.accessToken = accessToken;
        }

        public String getTokenType() {
            return tokenType;
        }

        public void setTokenType(String tokenType) {
            this.tokenType = tokenType;
        }

        public long getExpiresIn() {
            return expiresIn;
        }

        public void setExpiresIn(long expiresIn) {
            this.expiresIn = expiresIn;
        }

        public String getRefreshToken() {
            return refreshToken;
        }

        public void setRefreshToken(String refreshToken) {
            this.refreshToken = refreshToken;
        }

        public String getScope() {
            return scope;
        }

        public void setScope(String scope) {
            this.scope = scope;
        }
    }
}