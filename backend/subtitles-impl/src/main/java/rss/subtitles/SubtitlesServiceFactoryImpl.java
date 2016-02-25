package rss.subtitles;

import org.springframework.stereotype.Component;
import rss.subtitles.dao.DoubleEpisodeSubtitles;
import rss.subtitles.dao.SingleEpisodeSubtitles;
import rss.subtitles.dao.SubtitlesImpl;
import rss.subtitles.dao.SubtitlesScanHistoryImpl;
import rss.torrents.Subtitles;

/**
 * User: dikmanm
 * Date: 17/10/2015 16:28
 */
@Component
public class SubtitlesServiceFactoryImpl implements SubtitlesServiceFactory {
    public Subtitles createMovieSubtitles() {
        return new SubtitlesImpl();
    }

    public Subtitles createEpisodeSubtitles(int season, int episode) {
        return new SingleEpisodeSubtitles(season, episode);
    }

    public Subtitles createDoubleEpisodeSubtitles(int season, int episode1, int episode2) {
        return new DoubleEpisodeSubtitles(season, episode1, episode2);
    }

    @Override
    public SubtitlesScanHistory createSubtitlesScanHistory() {
        return new SubtitlesScanHistoryImpl();
    }

    @Override
    public SubtitlesConverter getConverter() {
        return new SubtitlesConverter();
    }
}
