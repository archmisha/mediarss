package rss.services.searchers.composite.torrentz;

import org.springframework.beans.factory.annotation.Autowired;
import rss.entities.Media;
import rss.services.PageDownloader;
import rss.services.requests.MediaRequest;
import rss.services.searchers.SearchResult;
import rss.services.searchers.SimpleTorrentSearcher;
import rss.services.searchers.composite.AbstractCompositeSearcher;

import java.io.UnsupportedEncodingException;
import java.util.Set;

/**
 * User: Michael Dikman
 * Date: 02/12/12
 * Time: 22:08
 * <p/>
 * <dl><dt><a href="/8683cfd2d2bea23dd137d806cb942755345a5e4d">Premium Rush 2012 1080p Blu ray Remux AVC DTS <b>HD</b> MA 5 1 KRaLiMaRKo
 * </a> &#187; bluray remux <b>movies</b> <b>hd</b> <b>video</b> <b>highres</b> x264</dt>
 * <dd><span class="v" style="color: #A2EB80" title="8">1</span><span class="a"><span title="Sun, 02 Dec 2012 00:06:45">19 hours</span></span>
 * <span class="s">17 GB</span> <span class="u">8</span><span class="d">128</span></dd></dl>
 */
public abstract class TorrentzSearcher<T extends MediaRequest> extends AbstractCompositeSearcher<T> {

	@Autowired
	protected TorrentzParser torrentzParser;

	@Autowired
	protected PageDownloader pageDownloader;

	@Override
	protected SearchResult performSearch(T mediaRequest, SimpleTorrentSearcher<T, Media> torrentSearcher) {
		return torrentSearcher.searchById(mediaRequest);
	}

	protected void enrichRequestWithSearcherIds(T mediaRequest) {
		String entryPage = pageDownloader.downloadPage(TorrentzParserImpl.TORRENTZ_ENTRY_URL + mediaRequest.getHash());
		for (SimpleTorrentSearcher<T, Media> simpleTorrentSearcher : getTorrentSearchers()) {
			mediaRequest.setSearcherId(simpleTorrentSearcher.getName(), simpleTorrentSearcher.parseId(mediaRequest, entryPage));
		}
	}

	protected abstract String getSearchUrl(T mediaRequest);
}
