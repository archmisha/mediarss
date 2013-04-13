package rss.services.parsers;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import rss.BaseTest;
import rss.entities.Movie;

import java.util.Collection;

import static org.junit.Assert.assertEquals;

/**
 * User: dikmanm
 * Date: 28/12/12 10:01
 */
@RunWith(MockitoJUnitRunner.class)
public class TorrentzParserTest extends BaseTest {

	private TorrentzParserImpl torrentzParser = new TorrentzParserImpl();

	@Test
	public void testSkipPornTorrents() {
		String page = loadPage("singlePornTorrent");

		Collection<Movie> download = torrentzParser.parse(page);

		assertEquals(0, download.size());
	}

	@Test
	public void testParseNormalTorrent() {
		String page = loadPage("normalTorrent");

		Collection<Movie> download = torrentzParser.parse(page);

		assertEquals(1, download.size());
	}

	// todo
	@Test
	@Ignore("should be moved to torrent service impl tests")
	public void testSkipOldYearTorrents() {
		String page = loadPage("oldYearTorrent");

		Collection<Movie> download = torrentzParser.parse(page);

		assertEquals(0, download.size());
	}

	@Test
	public void testEclipseSearchResults() {
		String page = loadPage("eclipse");

		Collection<Movie> download = torrentzParser.parse(page);

		assertEquals(2, download.size());
	}
}
