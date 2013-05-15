package rss.services.searchers;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.primitives.Ints;
import org.apache.commons.collections.IteratorUtils;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rss.entities.MediaQuality;
import rss.entities.Show;
import rss.entities.Subtitles;
import rss.services.PageDownloader;
import rss.services.log.LogService;
import rss.services.matching.MatchCandidate;
import rss.services.matching.MatchingUtils;
import rss.services.requests.SubtitlesEpisodeRequest;
import rss.services.requests.SubtitlesRequest;
import rss.services.requests.SubtitlesSingleEpisodeRequest;
import rss.services.subtitles.SubtitleLanguage;

import java.security.InvalidParameterException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;

/**
 * User: dikmanm
 * Date: 11/05/13 17:31
 */
@Service
public class SubCenterSubtitlesSearcher implements Searcher<SubtitlesRequest, Subtitles> {

	public static final String NAME = "www.subscenter.org";
	public static final String SEARCH_URL = "http://" + NAME + "/he/subtitle/search/?q=";
	public static final String ENTRY_URL = "http://" + NAME + "/%s/%d/%d";
	public static final String DATA_URL = "http://" + NAME + "/he/subtitle/download/%s/%d/?v=%s&key=%s";
	private static final Pattern ENTRY_FOUND_PATTERN = Pattern.compile("subtitles_info");

	@Autowired
	private PageDownloader pageDownloader;

	@Autowired
	private LogService logService;

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public SearchResult search(SubtitlesRequest mediaRequest) {
		try {
			// first find the url for the show, if don't have it yet
			if (mediaRequest instanceof SubtitlesEpisodeRequest) {
				SubtitlesEpisodeRequest ser = (SubtitlesEpisodeRequest) mediaRequest;
				Show show = ser.getShow();
				if (StringUtils.isBlank(show.getSubCenterUrl())) {
					getSubCenterShowUrl(show, mediaRequest);
				}

				// now download the page of the specific request
				logService.info(getClass(), "SubCenter show url: " + show.getSubCenterUrl());
				if (StringUtils.isBlank(show.getSubCenterUrl())) {
					return SearchResult.createNotFound();
				}

				if (mediaRequest instanceof SubtitlesSingleEpisodeRequest) {
					SubtitlesSingleEpisodeRequest sser = (SubtitlesSingleEpisodeRequest) mediaRequest;
					String page = pageDownloader.downloadPageUntilFound(String.format(ENTRY_URL, show.getSubCenterUrl(), ser.getSeason(), sser.getEpisode()), ENTRY_FOUND_PATTERN);

					SearchResult searchResult = new SearchResult(NAME);
					for (SubtitleLanguage subtitleLanguage : ser.getLanguages()) {
						SubCenterSubtitles foundSubtitles = parseEntryPage(mediaRequest, sser, page, subtitleLanguage);

						// for the best result only, download the actual subtitles
						byte[] data = pageDownloader.downloadData(String.format(DATA_URL,
								toSubCenterLanguages(subtitleLanguage), foundSubtitles.getId(), foundSubtitles.getName(), foundSubtitles.getKey()));

						Subtitles subtitles = new Subtitles();
						subtitles.setLanguage(foundSubtitles.getLanguage());
						subtitles.setFileName(foundSubtitles.getName());
						subtitles.setExternalId(String.valueOf(foundSubtitles.getId()));
						subtitles.setTorrent(mediaRequest.getTorrent());
						subtitles.setDateUploaded(foundSubtitles.getDateUploaded());
						subtitles.setData(data);

						logService.info(getClass(), "FINAL: '" + foundSubtitles + "' for '" + sser.getTorrent().getTitle() + "'");
						searchResult.addDownloadable(subtitles);
					}

					return searchResult;
				}
			}
		} catch (Exception e) {
			logService.error(getClass(), e.getMessage(), e);
		}
		return SearchResult.createNotFound();
	}

	private SubCenterSubtitles parseEntryPage(SubtitlesRequest mediaRequest, SubtitlesSingleEpisodeRequest sser,
											  String page, SubtitleLanguage subtitleLanguage) throws Exception {
		// parse subtitles found on page
		int idx = page.indexOf("subtitles_groups = ") + "subtitles_groups = ".length();
		String subsInfo = page.substring(idx, page.indexOf("subtitles_info", idx)).trim();
		ObjectMapper mapper = new ObjectMapper();
		JsonNode jsonNode = mapper.readTree(subsInfo);

		// for the same subtitles name, pick the one with the most downloads
		SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy ,HH:mm");
		Map<String, SubCenterSubtitles> results = new HashMap<>();
		for (JsonNode node1 : IteratorUtils.toList(jsonNode.get(toSubCenterLanguages(subtitleLanguage)).getElements())) {
			for (JsonNode node2 : IteratorUtils.toList(node1.getElements())) {
				for (JsonNode node3 : IteratorUtils.toList(node2.getElements())) {
					long id = node3.get("id").getIntValue();
					String key = node3.get("key").getTextValue();
					String name = node3.get("subtitle_version").getTextValue();
					int downloaded = node3.get("downloaded").getIntValue();
					Date dateUploaded = DATE_FORMAT.parse(node3.get("created_on").getTextValue());

					if (!name.endsWith(".srt")) {
						// zip suffix is not ended to the subtitles, it is default
						name += ".zip";
					}

					// take the best downloaded value
					if (!results.containsKey(name) || results.get(name).getDownloaded() < downloaded) {
						results.put(name, new SubCenterSubtitles(id, name, key, downloaded, SubtitleLanguage.HEBREW, dateUploaded));
					}
				}
			}
		}

		String[] keywords = new String[]{"dvdrip"};
		Map<String, Boolean> keywordMatches = new HashMap<>();
		for (String keyword : keywords) {
			keywordMatches.put(keyword, mediaRequest.getTorrent().getTitle().toLowerCase().contains("dvdrip"));
		}

		for (Map.Entry<String, SubCenterSubtitles> entry : new ArrayList<>(results.entrySet())) {
			String name = entry.getKey().toLowerCase();
			// remove file suffixes
			for (String suffix : Arrays.asList(".zip", ".srt")) {
				if (name.endsWith(suffix)) {
					name = name.substring(0, name.length() - suffix.length());
					break;
				}
			}

			// determine quality
			MediaQuality quality = SearcherUtils.findQuality(name);
			if (quality != mediaRequest.getTorrent().getQuality()) {
				logService.info(getClass(), "Skipping " + name + " - quality doesn't match");
				results.remove(entry.getKey());
			} else {
				// for those keywords that are true - exist in the torrent, need to verify they are in the subtitles
				for (Map.Entry<String, Boolean> entry2 : keywordMatches.entrySet()) {
					if (entry2.getValue() && !name.contains(entry2.getKey())) {
						logService.info(getClass(), "Skipping " + name + " - keyword " + entry2.getKey() + " doesn't match");
						results.remove(entry.getKey());
					}
				}
			}
		}

		logService.info(getClass(), "left with: " + StringUtils.join(results, ", "));
		ArrayList<SubCenterSubtitles> list = new ArrayList<>(results.values());
		Collections.sort(list, new Comparator<SubCenterSubtitles>() {
			@Override
			public int compare(SubCenterSubtitles o1, SubCenterSubtitles o2) {
				return Ints.compare(o2.getDownloaded(), o1.getDownloaded());
			}
		});
		SubCenterSubtitles bestResult = list.get(0);

		return bestResult;
	}

	private void getSubCenterShowUrl(Show show, SubtitlesRequest mediaRequest) {
		String page = pageDownloader.downloadPage(SEARCH_URL + show.getName());

		// figure out how many pages there are
		Document doc = Jsoup.parse(page);
		String pagesPart = doc.select(".minibuttonpage").get(0).children().get(0).html();
//		System.out.println(pagesPart.split(" ")[5]);
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

//		System.out.println("found search results: " + searchResults.size());
		SubCenterSearchResult subCenterSearchResult = MatchingUtils.filterByLevenshteinDistance(mediaRequest.getName(), Collections2.transform(searchResults, new Function<SubCenterSearchResult, MatchCandidate>() {
			@Override
			public MatchCandidate apply(final SubCenterSearchResult subCenterSearchResult) {
				return new MatchCandidate() {
					@Override
					public String getText() {
						return subCenterSearchResult.getName();
					}

					@SuppressWarnings("unchecked")
					@Override
					public <T> T getObject() {
						return (T) subCenterSearchResult;
					}
				};
			}
		})).getObject();

		if (subCenterSearchResult != null) {
			logService.info(getClass(), "Filtered candidates: " + subCenterSearchResult);
			show.setSubCenterUrl(subCenterSearchResult.getSubCenterUrl());
		}
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
//			name = ShowServiceImpl.normalize(name);
			SubCenterSearchResult result = new SubCenterSearchResult(href, isShow, showName);
			results.add(result);
		}

		return results;
	}

	private String toSubCenterLanguages(SubtitleLanguage language) {
		switch (language) {
			case HEBREW:
				return "he";
			case ENGLISH:
				return "en";
			default:
				throw new InvalidParameterException("SubCenter doesn't support language: " + language);
		}
	}

	private class SubCenterSearchResult {
		private String subCenterUrl;
		private boolean isShow;
		private String name;

		public SubCenterSearchResult(String subCenterUrl, boolean isShow, String name) {
			this.subCenterUrl = subCenterUrl;
			this.isShow = isShow;
			this.name = name;
		}

		private String getSubCenterUrl() {
			return subCenterUrl;
		}

		private boolean isShow() {
			return isShow;
		}

		private String getName() {
			return name;
		}

		@Override
		public String toString() {
			return "SubCenterSearchResult{" +
				   "subCenterUrl='" + subCenterUrl + '\'' +
				   ", isShow=" + isShow +
				   ", name='" + name + '\'' +
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

	private class SubCenterSubtitles {
		private long id;
		private String name;
		private String key;
		private int downloaded;
		private SubtitleLanguage language;
		private Date dateUploaded;

		public SubCenterSubtitles(long id, String name, String key, int downloaded, SubtitleLanguage language, Date dateUploaded) {
			this.id = id;
			this.name = name;
			this.key = key;
			this.downloaded = downloaded;
			this.language = language;
			this.dateUploaded = dateUploaded;
		}

		private String getName() {
			return name;
		}

		private String getKey() {
			return key;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;

			SubCenterSubtitles that = (SubCenterSubtitles) o;

			if (!key.equals(that.key)) return false;

			return true;
		}

		@Override
		public int hashCode() {
			return key.hashCode();
		}

		public int getDownloaded() {
			return downloaded;
		}

		private Date getDateUploaded() {
			return dateUploaded;
		}

		@Override
		public String toString() {
			return "name='" + name + '\'' + ", downloaded=" + downloaded;
		}

		public SubtitleLanguage getLanguage() {
			return language;
		}

		public long getId() {
			return id;
		}
	}
}
