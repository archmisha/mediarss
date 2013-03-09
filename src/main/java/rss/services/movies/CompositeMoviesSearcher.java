package rss.services.movies;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import rss.services.MediaRequest;
import rss.entities.Media;
import rss.services.searchers.CompositeTorrentSearcher;
import rss.services.searchers.TorrentSearcher;

import java.util.Arrays;
import java.util.Collection;

/**
 * User: Michael Dikman
 * Date: 04/12/12
 * Time: 19:15
 */
@Service("compositeMoviesSearcher")
public class CompositeMoviesSearcher extends CompositeTorrentSearcher {

    @Autowired
    @Qualifier("publichdSearcher")
    private TorrentSearcher publichdSearcher;

    @Autowired
    @Qualifier("episodeSearcher1337x")
    private TorrentSearcher episodeSearcher1337x;

    @Autowired
    @Qualifier("thePirateBayEpisodeSearcher")
    private TorrentSearcher thePirateBayEpisodeSearcher;

    @Override
    protected boolean shouldFailOnNoIMDBUrl() {
        return true;
    }

    @Override
    protected Collection<? extends TorrentSearcher<MediaRequest, Media>> getTorrentSearchers() {
        return Arrays.<TorrentSearcher<MediaRequest, Media>>asList(publichdSearcher, thePirateBayEpisodeSearcher, episodeSearcher1337x);
    }
}
