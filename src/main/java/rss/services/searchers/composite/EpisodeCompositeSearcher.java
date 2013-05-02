package rss.services.searchers.composite;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import rss.entities.Episode;
import rss.entities.Media;
import rss.services.requests.MediaRequest;
import rss.services.requests.ShowRequest;

/**
 * User: Michael Dikman
 * Date: 04/12/12
 * Time: 19:15
 */
@Service("compositeEpisodeSearcher")
public class EpisodeCompositeSearcher extends MediaCompositeSearcher<ShowRequest, Episode> {

	@Autowired
	@Qualifier("torrentzEpisodeSearcher")
	private CompositeTorrentSearcher<ShowRequest, Episode> torrentzEpisodeSearcher;

	@Override
	protected CompositeTorrentSearcher<ShowRequest, Episode> getTorrentzSearcher() {
		return torrentzEpisodeSearcher;
	}

	@Override
	protected boolean shouldFailOnNoIMDBUrl() {
		return false;
	}
}
