package rss.services.shows;

import com.google.common.base.Predicate;
import rss.controllers.vo.EpisodeSearchResult;
import rss.controllers.vo.ShowsScheduleVO;
import rss.entities.Episode;
import rss.entities.User;
import rss.services.EpisodeRequest;
import rss.entities.Show;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * User: dikmanm
 * Date: 05/01/13 18:01
 */
public interface ShowService {

	void transformEpisodeRequest(EpisodeRequest episodeRequest);

	void downloadShowList();

	EpisodeSearchResult search(EpisodeRequest episodeRequest, User user);

	DownloadScheduleResult downloadSchedule();

	void downloadSchedule(Show show);

	List<AutoCompleteItem> autoCompleteShowNames(String term, boolean includeEnded, Predicate<? super AutoCompleteItem> predicate);

	ShowsScheduleVO getSchedule(Set<Show> shows);

	boolean isMatch(EpisodeRequest mediaRequest, String title);
//	Show downloadShowByUrl(String url);
}
