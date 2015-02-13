package rss.services.shows;

import java.util.ArrayList;
import java.util.List;

/**
 * User: dikmanm
 * Date: 06/11/13 23:27
 */
public class UsersSearchesCache {

	private List<UserActiveSearch> searches;

	public UsersSearchesCache() {
		searches = new ArrayList<>();
	}

	public void addSearch(UserActiveSearch search) {
		this.searches.add(search);
	}

	public List<UserActiveSearch> getSearches() {
		return searches;
	}

	public void removeSearch(String id) {
		for (UserActiveSearch userActiveSearch : new ArrayList<>(searches)) {
			if (userActiveSearch.getSearchResultVO().getId().equals(id)) {
				searches.remove(userActiveSearch);
			}
		}
	}
}
