package rss.dao;

import org.springframework.stereotype.Repository;
import rss.ems.dao.BaseDaoJPA;
import rss.entities.SearchLog;
import rss.services.requests.movies.MovieRequest;
import rss.services.searchers.SearchResult;
import rss.torrents.requests.MediaRequest;
import rss.torrents.searchers.MediaRequestVisitor;

/**
 * User: dikmanm
 * Date: 10/02/2015 15:03
 */
@Repository
public class SearchLogDaoImpl extends BaseDaoJPA<SearchLog> implements SearchLogDao {

    @Override
    public void logSearch(MediaRequest mediaRequest, String name, SearchResult.SearchStatus searchStatus, String url) {
        logSearch(mediaRequest, name, searchStatus, url, null, null);
    }

    @Override
    public void logSearch(MediaRequest mediaRequest, String name, SearchResult.SearchStatus searchStatus, String url, String page, String exception) {
        SearchLog searchLog = new SearchLog();
        mediaRequest.visit(new SearchLogVisitor(), searchLog);
        searchLog.setStatus(searchStatus);
        searchLog.setSource(name);
        searchLog.setUrl(url);
        if (page != null) {
            searchLog.setPage(page.getBytes());
        }
        if (exception != null) {
            searchLog.setException(exception.getBytes());
        }
        em.persist(searchLog);
    }

    private class SearchLogVisitor implements MediaRequestVisitor<SearchLog, Void> {

        @Override
        public Void visit(SingleEpisodeRequest episodeRequest, SearchLog searchLog) {
            searchLog.setType("show");
            searchLog.setName(episodeRequest.getTitle());
            searchLog.setSeason(episodeRequest.getSeason());
            searchLog.setEpisode(String.valueOf(episodeRequest.getEpisode()));
            return null;
        }

        @Override
        public Void visit(DoubleEpisodeRequest episodeRequest, SearchLog searchLog) {
            searchLog.setType("show");
            searchLog.setName(episodeRequest.getTitle());
            searchLog.setSeason(episodeRequest.getSeason());
            searchLog.setEpisode(episodeRequest.getEpisode1() + "-" + episodeRequest.getEpisode2());
            return null;
        }

        @Override
        public Void visit(FullSeasonRequest episodeRequest, SearchLog searchLog) {
            searchLog.setType("show");
            searchLog.setName(episodeRequest.getTitle());
            searchLog.setSeason(episodeRequest.getSeason());
            return null;
        }

        @Override
        public Void visit(MovieRequest movieRequest, SearchLog searchLog) {
            searchLog.setType("movie");
            searchLog.setName(movieRequest.getTitle());
            return null;
        }
    }
}
