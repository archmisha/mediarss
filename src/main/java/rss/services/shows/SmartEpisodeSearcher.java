package rss.services.shows;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import rss.entities.Episode;
import rss.services.EpisodeRequest;
import rss.entities.MediaQuality;
import rss.services.SearchResult;
import rss.services.searchers.TorrentSearcher;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * User: Michael Dikman
 * Date: 01/12/12
 * Time: 18:59
 */
@Service("smartEpisodeSearcher")
public class SmartEpisodeSearcher implements TorrentSearcher<EpisodeRequest, Episode> {

    private static Log log = LogFactory.getLog(SmartEpisodeSearcher.class);

    @Autowired
    @Qualifier("compositeTVShowsEpisodeSearcher")
    private TorrentSearcher<EpisodeRequest, Episode> compositeEpisodeSearcher;

    private List<EpisodeModificator> episodeModificators;

    @PostConstruct
    private void postConstruct() {
        episodeModificators = Arrays.asList(
                new DoNothingEpisodeModificator(),
                new NormalQualityEpisodeModificator(),
                new ShowNameEndsWithYearEpisodeModificator());
    }

    @Override
    public SearchResult<Episode> search(EpisodeRequest episodeRequest) {
        String msgPrefix = null;
        for (EpisodeModificator episodeModificator : episodeModificators) {
            EpisodeRequest modifiedEpisode = episodeModificator.modify(episodeRequest);
            if (modifiedEpisode != null) {
                if (msgPrefix != null) { // skipping the first one
                    log.info(msgPrefix + episodeModificator.getDescription() + " (modificator: " + episodeModificator.getClass().getSimpleName() + ").");
                }

                SearchResult<Episode> searchResult = compositeEpisodeSearcher.search(modifiedEpisode);
                if (searchResult.getSearchStatus() != SearchResult.SearchStatus.NOT_FOUND) {
                    if (searchResult.getSearchStatus() == SearchResult.SearchStatus.FOUND) {
                        // overwrite with the original episode, without transformations - so the original will be cached in db. otherwise won't find it later
                        // overriding only the name, as its one of the transformations. but the quality want to leave the real one
                        // ugly a bit - need to be consistent with the transformations
                        // todo: think dont need anymore
//                        searchResult.getMedia().setName(tvShowEpisode.getName());
                    }
                    searchResult.getTorrent().setQuality(modifiedEpisode.getQuality());
                    return searchResult;
                }
                msgPrefix = "Episode \"" + modifiedEpisode.toString() + "\" is not found. ";
            }
        }
        return new SearchResult<>(SearchResult.SearchStatus.NOT_FOUND);
    }

    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

    private interface EpisodeModificator {
        EpisodeRequest modify(EpisodeRequest episodeRequest);

        String getDescription();
    }

    private class DoNothingEpisodeModificator implements EpisodeModificator {
        @Override
        public EpisodeRequest modify(EpisodeRequest episodeRequest) {
            return episodeRequest;
        }

        @Override
        public String getDescription() {
            return "";
        }
    }

    private class NormalQualityEpisodeModificator implements EpisodeModificator {
        @Override
        public EpisodeRequest modify(EpisodeRequest episodeRequest) {
            EpisodeRequest copy = new EpisodeRequest(episodeRequest);
            copy.setQuality(MediaQuality.NORMAL);
            return copy;
        }

        @Override
        public String getDescription() {
            return "Trying to find with non HD quality.";
        }
    }

    private class ShowNameEndsWithYearEpisodeModificator implements EpisodeModificator {

        private Pattern p;

        private ShowNameEndsWithYearEpisodeModificator() {
            p = Pattern.compile("(.+) \\(\\d+\\)");
        }

        @Override
        public EpisodeRequest modify(EpisodeRequest episodeRequest) {
            Matcher matcher = p.matcher(episodeRequest.getTitle());
            if (matcher.find()) {
                EpisodeRequest copy = new EpisodeRequest(episodeRequest);
                copy.setTitle(matcher.group(1));
                return copy;
            }
            return null;
        }

        @Override
        public String getDescription() {
            return "Trying without the year suffix.";
        }
    }
}
