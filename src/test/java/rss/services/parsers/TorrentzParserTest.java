package rss.services.parsers;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import rss.BaseTest;
import rss.services.PageDownloader;
import rss.services.searchers.composite.torrentz.TorrentzParserImpl;
import rss.services.searchers.composite.torrentz.TorrentzResult;

import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TorrentzParserTest extends BaseTest {

	@Mock
	private PageDownloader pageDownloader;

	@InjectMocks
	private TorrentzParserImpl torrentzParser = new TorrentzParserImpl();

	@Test
	public void testSkipPornTorrents() {
		String page = loadPage("singlePornTorrent");
		when(pageDownloader.downloadPage(anyString())).thenReturn(page);

		Collection<TorrentzResult> download = torrentzParser.downloadByUrl("123");

		assertEquals(0, download.size());
	}

	@Test
	public void testSkipNotVerifiedTorrents() {
		String page = loadPage("singleNotVerifiedTorrent");
		when(pageDownloader.downloadPage(anyString())).thenReturn(page);

		Collection<TorrentzResult> download = torrentzParser.downloadByUrl("123");

		assertEquals(13, download.size());
	}

	@Test
	public void testParseNormalTorrent() {
		String page = loadPage("normalTorrent");
		when(pageDownloader.downloadPage(anyString())).thenReturn(page);

		Collection<TorrentzResult> download = torrentzParser.downloadByUrl("123");

		assertEquals(1, download.size());
	}

	// todo
	@Test
	@Ignore("should be moved to torrent service impl tests")
	public void testSkipOldYearTorrents() {
		String page = loadPage("oldYearTorrent");
		when(pageDownloader.downloadPage(anyString())).thenReturn(page);

		Collection<TorrentzResult> download = torrentzParser.downloadByUrl("123");

		assertEquals(0, download.size());
	}

	@Test
	public void testEclipseSearchResults() {
		String page = loadPage("eclipse");
		when(pageDownloader.downloadPage(anyString())).thenReturn(page);

		Collection<TorrentzResult> download = torrentzParser.downloadByUrl("123");

		assertEquals(2, download.size());
	}
}
