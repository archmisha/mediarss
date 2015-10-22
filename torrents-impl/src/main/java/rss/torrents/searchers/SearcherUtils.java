package rss.torrents.searchers;

import rss.torrents.MediaQuality;
import rss.torrents.requests.MediaRequest;
import rss.torrents.requests.movies.MovieRequest;
import rss.torrents.requests.shows.DoubleEpisodeRequest;
import rss.torrents.requests.shows.FullSeasonRequest;
import rss.torrents.requests.shows.SingleEpisodeRequest;

/**
 * User: dikmanm
 * Date: 14/05/13 00:40
 */
public class SearcherUtils {

    public static MediaQuality findQuality(String str) {
        str = str.toLowerCase();
        for (MediaQuality mediaQuality : MediaQuality.values()) {
            // skipping normal, cuz its "" - will always match
            if (mediaQuality != MediaQuality.NORMAL && str.contains(mediaQuality.toString())) {
                return mediaQuality;
            }
        }
        return MediaQuality.NORMAL;
    }

    public static <T, S> T applyVisitor(MediaRequestVisitor<S, T> visitor, MediaRequest mediaRequest, S data) {
        if (mediaRequest instanceof SingleEpisodeRequest) {
            return visitor.visit((SingleEpisodeRequest) mediaRequest, data);
        } else if (mediaRequest instanceof DoubleEpisodeRequest) {
            return visitor.visit((DoubleEpisodeRequest) mediaRequest, data);
        } else if (mediaRequest instanceof FullSeasonRequest) {
            return visitor.visit((FullSeasonRequest) mediaRequest, data);
        } else if (mediaRequest instanceof MovieRequest) {
            return visitor.visit((MovieRequest) mediaRequest, data);
        } else {
            throw new IllegalArgumentException("Unsupported media type: " + mediaRequest.getClass());
        }
    }
}
