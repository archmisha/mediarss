package rss.services.searchers;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rss.entities.Show;
import rss.entities.Subtitles;
import rss.services.PageDownloader;
import rss.services.requests.SubtitlesRequest;
import rss.services.shows.ShowService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * User: dikmanm
 * Date: 11/05/13 17:31
 */
@Service
public class SubCenterSubtitlesSearcher implements Searcher<SubtitlesRequest, Subtitles> {

	public static final String NAME = "www.subscenter.org";
	public static final String SEARCH_URL = "http://" + NAME + "/he/subtitle/search/?q=";

	@Autowired
	private PageDownloader pageDownloader;

	@Autowired
	private ShowService showService;

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public SearchResult search(SubtitlesRequest mediaRequest) {
		// first find the url for the show, if don't have it yet
		Show show = mediaRequest.getEpisodes().get(0).getShow();
		if (StringUtils.isBlank(show.getSubcenterUrl())) {
			getSubCenterShowUrl(show, mediaRequest);
		}

		// now download the page of the specific request
		System.out.println("Subcenter show url: " + show.getSubcenterUrl());
		if (StringUtils.isBlank(show.getSubcenterUrl())) {
			return SearchResult.createNotFound();
		}

//		pageDownloader.downloadPage()
		return SearchResult.createNotFound();
	}

	private void getSubCenterShowUrl(Show show, SubtitlesRequest mediaRequest) {
		String page = pageDownloader.downloadPage(SEARCH_URL + show.getName());

		// figure out how many pages there are
		Document doc = Jsoup.parse(page);
		String pagesPart = doc.select(".minibuttonpage").get(0).children().get(0).html();
		System.out.println(pagesPart.split(" ")[5]);
		int pages = Integer.parseInt(pagesPart.split(" ")[5]);

		List<SubCenterSearchResult> searchResults = new ArrayList<>();
		searchResults.addAll(parseSearchResultsPage(doc));
		for (int i = 1; i <= pages; ++i) {
			page = pageDownloader.downloadPage(SEARCH_URL + show.getName() + "&page=" + i);
			doc = Jsoup.parse(page);
			searchResults.addAll(parseSearchResultsPage(doc));
		}

		for (SubCenterSearchResult searchResult : new ArrayList<>(searchResults)) {
			if (!searchResult.isShow()) {
				searchResults.remove(searchResult);
			}
		}

		System.out.println("found search results: " + searchResults.size());

		List<ShowService.MatchCandidate> matchCandidates = new ArrayList<>();
		for (SubCenterSearchResult searchResult : searchResults) {
			matchCandidates.add(toMatchCandidate(searchResult));
		}
		List<ShowService.MatchCandidate> filteredCandidates = mediaRequest.getMediaRequest().visit(new MatcherVisitor(showService), matchCandidates);
		System.out.println("Filtered candidates: " + StringUtils.join(filteredCandidates, ","));
		if (!filteredCandidates.isEmpty()) {
			show.setSubcenterUrl(filteredCandidates.get(0).<SubCenterSearchResult>getObject().getSubCenterUrl());
		}
	}

	private ShowService.MatchCandidate toMatchCandidate(final SubCenterSearchResult searchResult) {
		return new ShowService.MatchCandidate() {
			@Override
			public String getText() {
				return searchResult.getShowName();
			}

			@SuppressWarnings("unchecked")
			@Override
			public <T> T getObject() {
				return (T) searchResult;
			}
		};
	}

	private Collection<SubCenterSearchResult> parseSearchResultsPage(Document doc) {
		Collection<SubCenterSearchResult> results = new ArrayList<>();
		for (Element element : doc.select(".generalWindowRight a")) {
			String href = element.attr("href");
			if (href.endsWith("/")) {
				href = href.substring(0, href.length() - 1);
			}
			boolean isShow = href.contains("/series/");
			String showName = href.substring(href.lastIndexOf("/") + 1);
//			showName = ShowServiceImpl.normalize(showName);
			SubCenterSearchResult result = new SubCenterSearchResult(href, isShow, showName);
			results.add(result);
		}

		return results;
	}

	private class SubCenterSearchResult {
		private String subCenterUrl;
		private boolean isShow;
		private String showName;

		public SubCenterSearchResult(String subCenterUrl, boolean isShow, String showName) {
			this.subCenterUrl = subCenterUrl;
			this.isShow = isShow;
			this.showName = showName;
		}

		private String getSubCenterUrl() {
			return subCenterUrl;
		}

		private boolean isShow() {
			return isShow;
		}

		private String getShowName() {
			return showName;
		}

		@Override
		public String toString() {
			return "SubCenterSearchResult{" +
				   "subCenterUrl='" + subCenterUrl + '\'' +
				   ", isShow=" + isShow +
				   ", showName='" + showName + '\'' +
				   '}';
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;

			SubCenterSearchResult that = (SubCenterSearchResult) o;

			if (subCenterUrl != null ? !subCenterUrl.equals(that.subCenterUrl) : that.subCenterUrl != null)
				return false;

			return true;
		}

		@Override
		public int hashCode() {
			return subCenterUrl != null ? subCenterUrl.hashCode() : 0;
		}
	}
}
