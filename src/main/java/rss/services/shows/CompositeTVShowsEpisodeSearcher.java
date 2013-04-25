package rss.services.shows;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import rss.services.requests.MediaRequest;
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
@Service("compositeTVShowsEpisodeSearcher")
public class CompositeTVShowsEpisodeSearcher extends CompositeTorrentSearcher {

	@Autowired
	@Qualifier("publichdSearcher")
	private TorrentSearcher publichdSearcher;

    @Autowired
    @Qualifier("episodeTorrentSearcher1337x")
    private TorrentSearcher episodeSearcher1337x;

    @Autowired
    @Qualifier("thePirateBayEpisodeTorrentSearcher")
    private TorrentSearcher thePirateBayEpisodeSearcher;

	@Autowired
	@Qualifier("torrentzEpisodeSearcher")
	private TorrentSearcher torrentzEpisodeSearcher;

    @Override
    protected boolean shouldFailOnNoIMDBUrl() {
        return false;
    }

    @Override
    protected Collection<? extends TorrentSearcher<MediaRequest, Media>> getTorrentSearchers() {
        return Arrays.<TorrentSearcher<MediaRequest, Media>>asList(torrentzEpisodeSearcher, thePirateBayEpisodeSearcher, episodeSearcher1337x, publichdSearcher);
    }
}
