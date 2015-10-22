package rss.torrents.requests.subtitles;

import org.apache.commons.lang3.StringUtils;
import rss.torrents.Show;
import rss.torrents.Torrent;
import rss.user.subtitles.SubtitleLanguage;

import java.util.Date;
import java.util.List;

/**
 * User: dikmanm
 * Date: 13/05/13 22:02
 */
public class SubtitlesSingleEpisodeRequest extends SubtitlesEpisodeRequest {

    private int episode;

    public SubtitlesSingleEpisodeRequest(Torrent torrent, Show show, int season, int episode, List<SubtitleLanguage> languages, Date airDate) {
        super(torrent, show, season, languages);
        this.episode = episode;
        this.airDate = airDate;
    }

    public int getEpisode() {
        return episode;
    }

    @Override
    protected String getSeasonEpisode() {
        return "s" + StringUtils.leftPad(String.valueOf(getSeason()), 2, '0') +
                "e" + StringUtils.leftPad(String.valueOf(episode), 2, '0');
    }
}
