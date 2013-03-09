package rss.services;

import rss.entities.Media;
import rss.entities.Torrent;

/**
 * User: Michael Dikman
 * Date: 26/11/12
 * Time: 00:46
 */
public class SearchResult<T extends Media> {

    // awaiting aging is when torrent was found but still the aging period hasn't passed
    public enum SearchStatus {
        NOT_FOUND, FOUND, AWAITING_AGING
    }

    private SearchStatus searchStatus;
    private Torrent torrent;
    private String source;
    private MetaData metaData;

    public SearchResult(Torrent torrent, String source) {
        this(torrent, source, SearchStatus.FOUND);
    }

    public SearchResult(Torrent torrent, String source, SearchStatus searchStatus) {
        this(searchStatus);
        this.torrent = torrent;
        this.source = source;
    }

    public SearchResult(SearchStatus searchStatus) {
        this.searchStatus = searchStatus;
        metaData = new MetaData();
    }

    public SearchStatus getSearchStatus() {
        return searchStatus;
    }

    public void setSearchStatus(SearchStatus searchStatus) {
        this.searchStatus = searchStatus;
    }

    public Torrent getTorrent() {
        return torrent;
    }

    public String getSource() {
        return source;
    }

    public MetaData getMetaData() {
        return metaData;
    }

    public class MetaData {
        private String imdbUrl;

        public String getImdbUrl() {
            return imdbUrl;
        }

        public void setImdbUrl(String imdbUrl) {
            this.imdbUrl = imdbUrl;
        }
    }
}
