package rss.torrents.searchers.composite.torrentz;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import rss.LogServiceTestUtils;
import rss.PageDownloader;
import rss.PageDownloaderTestUtils;
import rss.log.LogService;
import rss.torrents.searchers.TorrentzResult;

import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TorrentzParserTest {

    @Mock
    private PageDownloader pageDownloader;
    @Mock
    private LogService logService;
    @InjectMocks
    private TorrentzParserImpl torrentzParser = new TorrentzParserImpl();

    @Before
    public void before() {
        LogServiceTestUtils.mockLogService(logService);
    }

    @Test
    public void testSkipPornTorrents() {
        String page = PageDownloaderTestUtils.loadPage("singlePornTorrent");
        when(pageDownloader.downloadPage(anyString())).thenReturn(page);

        Collection<TorrentzResult> download = torrentzParser.downloadByUrl("123");

        assertEquals(0, download.size());
    }

    @Test
    @Ignore
    public void testSkipNotVerifiedTorrents() {
        String page = PageDownloaderTestUtils.loadPage("singleNotVerifiedTorrent");
        when(pageDownloader.downloadPage(anyString())).thenReturn(page);

        Collection<TorrentzResult> download = torrentzParser.downloadByUrl("123");

        assertEquals(13, download.size());
    }

    @Test
    @Ignore
    public void testParseNormalTorrent() {
        String page = PageDownloaderTestUtils.loadPage("normalTorrent");
        when(pageDownloader.downloadPage(anyString())).thenReturn(page);

        Collection<TorrentzResult> download = torrentzParser.downloadByUrl("123");

        assertEquals(1, download.size());
    }

    // todo
    @Test
    @Ignore("should be moved to torrent service impl tests")
    public void testSkipOldYearTorrents() {
        String page = PageDownloaderTestUtils.loadPage("oldYearTorrent");
        when(pageDownloader.downloadPage(anyString())).thenReturn(page);

        Collection<TorrentzResult> download = torrentzParser.downloadByUrl("123");

        assertEquals(0, download.size());
    }

    @Test
    public void testEclipseSearchResults() {
        String page = PageDownloaderTestUtils.loadPage("eclipse");
        when(pageDownloader.downloadPage(anyString())).thenReturn(page);

        Collection<TorrentzResult> download = torrentzParser.downloadByUrl("123");

        assertEquals(2, download.size());
    }
}
