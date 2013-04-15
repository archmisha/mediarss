package rss.services.shows;

import com.google.common.base.Predicate;
import rss.controllers.vo.EpisodeSearchResult;
import rss.controllers.vo.ShowsScheduleVO;
import rss.entities.User;
import rss.services.requests.EpisodeRequest;
import rss.entities.Show;
import rss.services.requests.ShowRequest;

import java.util.List;
import java.util.Set;

/**
 * User: dikmanm
 * Date: 05/01/13 18:01
 */
public interface ShowService {

	void transformEpisodeRequest(ShowRequest episodeRequest);

	void downloadShowList();

	EpisodeSearchResult search(ShowRequest episodeRequest, User user);

	DownloadScheduleResult downloadSchedule();

	void downloadSchedule(Show show);

	List<AutoCompleteItem> autoCompleteShowNames(String term, boolean includeEnded, Predicate<? super AutoCompleteItem> predicate);

	ShowsScheduleVO getSchedule(Set<Show> shows);

	boolean isMatch(EpisodeRequest mediaRequest, String title);
//	Show downloadShowByUrl(String url);
}
