package rss.shows;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.primitives.Ints;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.hibernate.NonUniqueResultException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import rss.RecoverableConnectionException;
import rss.cache.ShowsCacheService;
import rss.cache.UserCacheService;
import rss.environment.Environment;
import rss.log.LogService;
import rss.mail.EmailClassification;
import rss.mail.EmailService;
import rss.shows.dao.*;
import rss.shows.providers.ShowData;
import rss.shows.providers.ShowsProvider;
import rss.shows.providers.SyncData;
import rss.shows.schedule.ShowScheduleEpisodeItem;
import rss.shows.schedule.ShowsScheduleJSON;
import rss.subtitles.SubtitlesService;
import rss.torrents.*;
import rss.torrents.dao.TorrentDao;
import rss.torrents.dao.UserTorrentDao;
import rss.torrents.downloader.DownloadConfig;
import rss.torrents.downloader.DownloadResult;
import rss.torrents.downloader.EpisodeTorrentsDownloader;
import rss.torrents.matching.MatchCandidate;
import rss.torrents.requests.shows.EpisodeRequest;
import rss.torrents.requests.shows.FullSeasonRequest;
import rss.torrents.requests.shows.ShowRequest;
import rss.torrents.requests.shows.SingleEpisodeRequest;
import rss.user.User;
import rss.util.CollectionUtils;
import rss.util.DateUtils;
import rss.util.StringUtils2;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * User: dikmanm
 * Date: 05/01/13 18:01
 */
@Service
public class ShowServiceImpl implements ShowService, ShowServiceInternal {

    public static final Pattern RANGE_EPISODES_PATTERN = Pattern.compile("e\\d+");
    private static final int MAX_CONCURRENT_SHOWS = 10;

    @Autowired
    protected UserCacheService userCacheService;
    @Autowired
    protected UserTorrentDao userTorrentDao;
    @Autowired
    private ShowDao showDao;
    @Autowired
    private UserEpisodeTorrentDao userEpisodeTorrentDao;
    @Autowired
    private ShowsProvider showsProvider;
    @Autowired
    private EpisodeTorrentsDownloader torrentEntriesDownloader;
    @Autowired
    private TransactionTemplate transactionTemplate;
    @Autowired
    private EpisodeDao episodeDao;
    @Autowired
    private LogService logService;
    @Autowired
    private ShowsCacheService showsCacheService;
    @Autowired
    private EmailService emailService;
    @Autowired
    private ShowSearchService showSearchService;
    @Autowired
    private SubtitlesService subtitlesService;
    @Autowired
    private TorrentDao torrentDao;

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void saveNewShow(Show show) {
        logService.info(getClass(), String.format("It is a new show! - Persisting '%s' (theTvDb_id=%d)", show.getName(), show.getTheTvDbId()));
        showDao.persist(show);
        showsCacheService.put(show);
        downloadSchedule(show);
    }

    @Override
    // substitute show name with alias and seasons if needed
    public void transformEpisodeRequest(ShowRequest showRequest) {
        String alias = Environment.getInstance().getShowAlias(showRequest.getShow().getName());
        if (!StringUtils.isBlank(alias)) {
            showRequest.setTitle(alias);

            if (showRequest instanceof EpisodeRequest) {
                EpisodeRequest episodeRequest = (EpisodeRequest) showRequest;
                episodeRequest.setSeason(Environment.getInstance().getShowSeasonAlias(showRequest.getShow().getName(), episodeRequest.getSeason()));
            }
        }
    }

    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void downloadShowList() {
        final Class aClass = getClass();
        Collection<Show> downloadedShows = showsProvider.downloadShowList();

        ExecutorService executorService = Executors.newFixedThreadPool(MAX_CONCURRENT_SHOWS);
        for (final Show downloadedShow : downloadedShows) {
            executorService.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
                            @Override
                            protected void doInTransactionWithoutResult(TransactionStatus arg0) {
                                Show show = findShow(downloadedShow);

                                if (show == null) {
                                    saveNewShow(downloadedShow);
                                } else {
                                    // update existing shows with TheTvDb id
                                    show.setTheTvDbId(downloadedShow.getTheTvDbId());

                                    // update show status that might have changed
                                    if (show.isEnded() != downloadedShow.isEnded()) {
                                        show.setEnded(downloadedShow.isEnded());
                                        // since show becomes ended, download its episodes schedule one last time
                                        downloadSchedule(show);
                                    } else {
                                        // download full schedule anyway, since we are here only if we had no thetvdb id
                                        // and we get here once, then we store thetvdb id and not get here anymore
                                        downloadSchedule(show);
                                    }
                                }
                            }
                        });
                    } catch (RecoverableConnectionException e) {
                        // don't want to send email of 'Connection timeout out' errors, cuz tvrage is slow sometimes
                        // will retry to update show status in the next job run - warn level not send to email
                        logService.warn(aClass, String.format("Failed downloading info for show '%s': %s", downloadedShow.getName(), e.getMessage()));
                    } catch (Exception e) {
                        logService.error(aClass, String.format("Failed downloading info for show '%s': %s", downloadedShow.getName(), e.getMessage()), e);
                    }
                }
            });
        }

        executorService.shutdown();
        try {
            executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
        } catch (InterruptedException e) {
            logService.error(getClass(), "Error waiting for download tasks to execute: " + e.getMessage(), e);
        }
    }

    // todo: should move to showDao?
    private Show findShow(Show downloadedShow) {
        Show show = showDao.find(downloadedShow.getId());
        if (show == null) {
            show = showDao.findByTheTvDbId(downloadedShow.getTheTvDbId());
        }
        if (show == null && downloadedShow.getName() != null) {
            show = showDao.findByName(downloadedShow.getName());
        }
        return show;
    }

    @Override
    public void addTrackedShow(User user, final long showId) {
        final Show show = showDao.find(showId);

        // if show was not being tracked before (becoming tracked now) - download its schedule
        boolean downloadSchedule = !showDao.isShowBeingTracked(show);

        // now add the user as tracking the show
        show.getUsers().add(user);

        if (downloadSchedule) {
            // must separate schedule download and torrent download into separate transactions
            // cuz in the first creating episodes which must be available (committed) in the second part
            // and the second part spawns separate threads and transactions
            final Collection<Episode> newEpisodesToDownload = transactionTemplate.execute(new TransactionCallback<Collection<Episode>>() {
                @Override
                public Collection<Episode> doInTransaction(TransactionStatus arg0) {
                    // re-query so show will be in this transaction
                    Show show = showDao.find(showId);
                    Collection<Episode> episodes = downloadScheduleHelper(show);

                    DownloadScheduleResult downloadScheduleResult = new DownloadScheduleResult();
                    downloadScheduleResultHelper(show, episodes, downloadScheduleResult);
                    return downloadScheduleResult.getNewEpisodes();
                }
            });

            // return the request asap to the user, so download torrent async
            final Class aClass = getClass();
            ExecutorService executorService = Executors.newSingleThreadExecutor();
            executorService.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        downloadScheduleHelper(newEpisodesToDownload);
                    } catch (Exception e) {
                        logService.error(aClass, String.format("Failed downloading schedule of show '%s': %s", show, e.getMessage()), e);
                    }
                }
            });
            executorService.shutdown();
        }

        // invalidate schedule to be regenerated next request
//        userCacheService.invalidateUser(user); user didnt change
        userCacheService.invalidateSchedule(user);
        userCacheService.invalidateTrackedShows(user);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void downloadSchedule(final Show show) {
        Collection<Episode> episodes = downloadScheduleHelper(show);
        downloadScheduleResultHelper(show, episodes, new DownloadScheduleResult());
    }

    private Collection<Episode> downloadScheduleHelper(final Show show) {
        logService.info(getClass(), String.format("Downloading full schedule for '%s'", show));
        ShowData showData = showsProvider.getShowData(show);
        show.setEnded(showData.getShow().isEnded());
        show.setScheduleDownloadDate(new Date());
        showsCacheService.updateShowEnded(show);
        return showData.getEpisodes();
    }

    private void downloadScheduleHelper(Collection<Episode> episodesToDownload) {
        // download torrents for the new episodes
        final Set<ShowRequest> episodeRequests = new HashSet<>();
        for (Episode episode : episodesToDownload) {
            Show show = episode.getShow();
            if (episode.getEpisode() == -1) {
                episodeRequests.add(new FullSeasonRequest(null, show.getName(), show, MediaQuality.HD720P, episode.getSeason()));
            } else {
                episodeRequests.add(new SingleEpisodeRequest(null, show.getName(), show, MediaQuality.HD720P, episode.getSeason(), episode.getEpisode()));
            }
            logService.debug(getClass(), "Will try to download torrents of " + episode);
        }

        final Set<ShowRequest> missing = new HashSet<>();
        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus arg0) {
                DownloadResult<Episode, ShowRequest> downloadResult = torrentEntriesDownloader.download(episodeRequests, new DownloadConfig());
                missing.addAll(downloadResult.getMissing());
            }
        });

        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus arg0) {
                // if missing episode is released today (no matter the hour) then don't email it
                for (ShowRequest episodeRequest : new ArrayList<>(missing)) {
                    for (Episode episode : episodeDao.find(episodeRequest)) {
                        // might be null cuz maybe there is no such episode at all - who knows what they search for
                        if (/*episode != null &&*/ episode.getAirDate() != null && DateUtils.isToday(episode.getAirDate())) {
                            missing.remove(episodeRequest);
                        }
                    }
                }
                notifyOfMissingEpisodes(missing);
            }
        });
    }

    private void notifyOfMissingEpisodes(Set<ShowRequest> missingRequests) {
        if (missingRequests.isEmpty()) {
            return;
        }

        emailService.notifyToAdmins(
                EmailClassification.JOB,
                "The following episodes were not found:\n  " + StringUtils.join(missingRequests, "\n  "),
                "Failed sending email of missing episodes");
    }

    @Override
    public List<ShowAutoCompleteItem> autoCompleteShowNames(String term, boolean includeEnded, Predicate<? super ShowAutoCompleteItem> predicate) {
        term = term.toLowerCase().trim();
        List<ShowAutoCompleteItem> result = new ArrayList<>();
        for (CachedShow cachedShow : showsCacheService.getAll()) {
            if ((includeEnded || !cachedShow.isEnded()) && cachedShow.getName().toLowerCase().contains(term)) {
                ShowAutoCompleteItem showAutoCompleteItem = new ShowAutoCompleteItem(cachedShow.getId(), cachedShow.getName());
                showAutoCompleteItem.setEnded(cachedShow.isEnded());
                result.add(showAutoCompleteItem);
            }
        }
        if (predicate != null) {
            result = new ArrayList<>(Collections2.filter(result, predicate));
        }

        Collections.sort(result, new Comparator<ShowAutoCompleteItem>() {
            @Override
            public int compare(ShowAutoCompleteItem o1, ShowAutoCompleteItem o2) {
                return o1.getText().compareToIgnoreCase(o2.getText());
            }
        });
        return result;
    }

    @Override
    public ShowsScheduleJSON getSchedule(User user) {
        Collection<Episode> episodes = episodeDao.getEpisodesForSchedule(user);
        Map<Date, Set<ShowScheduleEpisodeItem>> map = new HashMap<>();
        // add today
        map.put(new Date(), new HashSet<ShowScheduleEpisodeItem>());

        for (Episode episode : episodes) {
            Date episodeDate = episode.getAirDate();
            Date date = null;
            for (Date curDate : map.keySet()) {
                if (DateUtils.isSameDay(episodeDate, curDate)) {
                    date = curDate;
                    break;
                }
            }
            if (date == null) {
                date = episodeDate;
                map.put(date, new HashSet<ShowScheduleEpisodeItem>());
            }
            map.get(date).add(new ShowScheduleEpisodeItem(episode.getSeasonEpisode().toUpperCase(), episode.getShow().getName()));
        }

        // sort the dates, oldest at beginning
        List<Date> dates = new ArrayList<>(map.keySet());
        Collections.sort(dates);

        ShowsScheduleJSON schedule = new ShowsScheduleJSON();
        for (Date date : dates) {
            List<ShowScheduleEpisodeItem> showNames = new ArrayList<>(map.get(date));
            Collections.sort(showNames, new Comparator<ShowScheduleEpisodeItem>() {
                @Override
                public int compare(ShowScheduleEpisodeItem o1, ShowScheduleEpisodeItem o2) {
                    int res = o1.getShowName().compareToIgnoreCase(o2.getShowName());
                    if (res != 0) {
                        return res;
                    }

                    return o1.getSequence().compareToIgnoreCase(o2.getSequence());
                }
            });
            schedule.addSchedule(date.getTime(), showNames);
        }
        return schedule;
    }

    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public DownloadScheduleResult downloadLatestScheduleWithTorrents() {
        // must separate schedule download and torrent download into separate transactions
        // cuz in the first creating episodes which must be available (committed) in the second part
        // and the second part spawns separate threads and transactions
        final DownloadScheduleResult downloadScheduleResult = transactionTemplate.execute(new TransactionCallback<DownloadScheduleResult>() {
            @Override
            public DownloadScheduleResult doInTransaction(TransactionStatus arg0) {
                DownloadScheduleResult downloadScheduleResult = new DownloadScheduleResult();

                SyncData syncData = showsProvider.getSyncData();
                for (Show showShell : syncData.getShows()) {
                    Show show = findShow(showShell);
                    if (show == null) {
                        saveNewShow(showShell);
                    } else {
                        show.setTheTvDbId(showShell.getTheTvDbId());
                        if (show.isEnded() != showShell.isEnded()) {
                            show.setEnded(showShell.isEnded());
                            // since show becomes ended, download its episodes schedule one last time
                            downloadSchedule(show);
                        }
                    }
                }

                Collection<Episode> episodes = syncData.getEpisodes();

                // collect all future episode schedules as given by the showsProvider
                Map<Show, List<Episode>> map = new HashMap<>();

                // just to skip already processed shows
                Map<Show, Boolean> processedShows = new HashMap<>();
                Map<Show, Show> showShellToShowMap = new HashMap<>();
                for (Episode episode : episodes) {
                    Show showShell = episode.getShow();
                    if (!processedShows.containsKey(showShell)) {
                        Show show = findShow(showShell);

                        // don't save new shows in the map, only save the show to db
                        // if show is not found, it is not being tracked
                        if (show == null) {
                            saveNewShow(showShell);
                            processedShows.put(showShell, false);
                        } else {
                            if (show.getTheTvDbId() == null) {
                                show.setTheTvDbId(showShell.getTheTvDbId());
                            }
                            processedShows.put(showShell, showDao.isShowBeingTracked(show));
                            showShellToShowMap.put(showShell, show);
                        }
                    }

                    // save only tracked shows in the map (also here show != null)
                    // changing this now, since there is no job that downloads the whole shows list.
                    // there is a single job now that downloads all new updates
//                    if (processedShows.get(showShell)) {
                        Show show = showShellToShowMap.get(showShell);
                        CollectionUtils.safeListPut(map, show, episode);
                        episode.setShow(show); // replacing showShell with real show
//                    }
                }

                // handle the case, where the job didn't run for a long time, and there is a gap between
                // the schedules and what we have in db - we need to fill this gap by downloading full show schedule
                // for the tracked shows
                if (shouldDownloadGap(map, episodes)) {
                    logService.info(getClass(), "Detected that there is an episode schedule gap, " +
                            "will download full schedules for tracked shows");
                    for (Show show : map.keySet()) {
                        downloadSchedule(show);
                    }
                } else {
                    for (Map.Entry<Show, List<Episode>> entry : map.entrySet()) {
                        Show show = entry.getKey();
                        List<Episode> curEpisodes = entry.getValue();

                        try {
                            logService.info(getClass(), "Downloading latest schedule for '" + show + "'");
                            downloadScheduleResultHelper(show, curEpisodes, downloadScheduleResult);
                        } catch (Exception e) {
                            downloadScheduleResult.addFailedShow(show);
                            // why before there was no exception trace printed to log?
                            logService.error(getClass(), "Failed downloading schedule for show " + show + " " + e.getMessage(), e);
                        }
                    }
                }

                return downloadScheduleResult;
            }
        });

        downloadScheduleHelper(downloadScheduleResult.getNewEpisodes());
        return downloadScheduleResult;
    }

    // checking if the earliest airdate episode is already in db
    private boolean shouldDownloadGap(Map<Show, List<Episode>> map, Collection<Episode> episodes) {
        boolean shouldDownloadGap = false;
        if (!episodes.isEmpty()) {
            List<Episode> sortedEpisodes = new ArrayList<>(episodes);
            Collections.sort(sortedEpisodes, new Comparator<Episode>() {
                @Override
                public int compare(Episode o1, Episode o2) {
                    // air dates shouldn't be null here
                    return o1.getAirDate().compareTo(o2.getAirDate());
                }
            });
            // look for an episode with existing show
            for (Episode episode : sortedEpisodes) {
                if (map.containsKey(episode.getShow())) {
                    shouldDownloadGap = !episodeDao.exists(episode.getShow(), episode);
                    break;
                }
            }
        }
        return shouldDownloadGap;
    }

    private void downloadScheduleResultHelper(Show show, Collection<Episode> episodes, DownloadScheduleResult downloadScheduleResult) {
        EpisodesMapper mapper = new EpisodesMapper(show.getEpisodes());
        for (Episode episode : episodes) {
            Episode persistedEpisode = mapper.get(episode.getSeason(), episode.getEpisode());
            if (persistedEpisode == null) {
                persistEpisodeToShow(show, episode);
                downloadScheduleResult.addNewEpisode(episode);
                persistedEpisode = episode;
            }
            persistedEpisode.setAirDate(episode.getAirDate());
            persistedEpisode.setLastUpdated(new Date());
        }

        // create full season episode if needed. Take all episodes, sort them and look at the last one
        // all season before the season of the last episode should exist
        // persisting inside
        for (Episode episode : findMissingFullSeasonEpisodes(show)) {
            downloadScheduleResult.addNewEpisode(episode);
        }

        // in addition we need past episodes, which might have no torrents for some reason (maybe job was broken or something)
        for (Episode episode : show.getEpisodes()) {
            if (episode.getTorrentIds().isEmpty()) {
                // if there are episodes without torrents, try to find their torrents
                downloadScheduleResult.addNewEpisode(episode);
            }
        }
    }

    @Override
    public Collection<Episode> findMissingFullSeasonEpisodes(Show show) {
        Collection<Episode> fullSeasonNewEpisodes = new ArrayList<>();
        if (!show.getEpisodes().isEmpty()) {
            EpisodesMapper mapper = new EpisodesMapper(show.getEpisodes());
            ArrayList<Episode> sortedEpisodes = new ArrayList<>(show.getEpisodes());
            Collections.sort(sortedEpisodes, new EpisodesComparator());
            int lastEpisodeSeason = sortedEpisodes.get(sortedEpisodes.size() - 1).getSeason();
            // running until lastEpisodeSeason-1 (including)
            for (int i = 1; i < lastEpisodeSeason; ++i) {
                if (mapper.get(i, -1) == null) {
                    Episode episode = new EpisodeImpl(i, -1);
                    persistEpisodeToShow(show, episode);
                    fullSeasonNewEpisodes.add(episode);
                }
            }
        }
        return fullSeasonNewEpisodes;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void disconnectTorrentsFromEpisode(Episode episode) {
        // episode is always connected to a single show - not a problem
        for (Long torrentId : episode.getTorrentIds()) {
            // delete the torrent only if it is connected to a single episode
            Torrent torrent = torrentDao.find(torrentId);
            try {
                episodeDao.find(torrent);

                for (UserTorrent userTorrent : userTorrentDao.findUserEpisodeTorrentByTorrentId(torrentId)) {
                    userTorrentDao.delete(userTorrent);
                }

                subtitlesService.deleteSubtitlesByTorrent(torrent);

                torrentDao.delete(torrent);
            } catch (NonUniqueResultException e) {
//				System.out.println("Aaaaa " + e.getMessage());
//				e.printStackTrace();
                // if exception was thrown - it means the torrent is connected to multiple episodes
            }
        }

        episode.getTorrentIds().clear();
        episodeDao.merge(episode);
    }

    @Override
    public void persistEpisodeToShow(Show show, Episode episode) {
        episodeDao.persist(episode);
        episode.setShow(show);
        show.getEpisodes().add(episode);
    }

    @Override
    public List<MatchCandidate> filterMatching(EpisodeRequest mediaRequest, Collection<MatchCandidate> matchCandidates) {
        if (matchCandidates.isEmpty()) {
            return Collections.emptyList();
        }

        // take everything before s01e01
        // do LD on the texts
        String requestTitle = TorrentUtils.normalize(mediaRequest.getTitle());
        Show show = mediaRequest.getShow();

        List<Pair<Integer, MatchCandidate>> pairs = new ArrayList<>();
        for (MatchCandidate candidate : matchCandidates) {
            String title = TorrentUtils.normalize(candidate.getText());

            String titlePrefix = null;
            String titleSuffix = null;

            if (mediaRequest instanceof FullSeasonRequest) {
                // take everything before season 1 or s01 the first of the 2
                String fullSeasonEnum = "season " + mediaRequest.getSeason();
                String shortSeasonEnum = "s" + StringUtils.leftPad(String.valueOf(mediaRequest.getSeason()), 2, '0');

                int indexOfFullSeason = StringUtils2.indexOf(fullSeasonEnum, title, Integer.MAX_VALUE);
                int indexOfShortSeason = StringUtils2.indexOf(shortSeasonEnum, title, Integer.MAX_VALUE);
                int indexOfSeason = Math.min(indexOfShortSeason, indexOfFullSeason);
                if (indexOfSeason == Integer.MAX_VALUE) {
                    continue;
                }
                int seasonEnumLength = indexOfSeason == indexOfShortSeason ? shortSeasonEnum.length() : fullSeasonEnum.length();

                titlePrefix = title.substring(0, indexOfSeason);
                titleSuffix = title.substring(indexOfSeason + seasonEnumLength);
            } else {
                String seasonEpisode = mediaRequest.getSeasonEpisode();
                int indexOfSeasonEpisode = title.indexOf(seasonEpisode);
                if (indexOfSeasonEpisode != -1) {
                    // take everything before s01e01
                    titlePrefix = title.substring(0, indexOfSeasonEpisode);
                    titleSuffix = title.substring(indexOfSeasonEpisode + seasonEpisode.length());
                }
            }

            // if we were in any of the conditions
            if (titlePrefix != null) {
                // show name is a heavier search than the title suffix one
                titlePrefix = titlePrefix.trim();
                boolean titleSuffixMatch = isTitleSuffixMatch(titleSuffix);
                if (titleSuffixMatch && isShowNameMatch(titlePrefix, show)) {
                    pairs.add(new ImmutablePair<>(StringUtils.getLevenshteinDistance(titlePrefix, requestTitle), candidate));
                } else {
                    logService.info(getClass(), String.format("Removing '%s' cuz a bad %s match for '%s'",
                            title, (titleSuffixMatch ? "show name" : "title suffix"), mediaRequest.toString()));
                }
            }
        }

        if (pairs.isEmpty()) {
            return Collections.emptyList();
        }

        Collections.sort(pairs, new Comparator<Pair<Integer, MatchCandidate>>() {
            @Override
            public int compare(Pair<Integer, MatchCandidate> o1, Pair<Integer, MatchCandidate> o2) {
                return Ints.compare(o1.getKey(), o2.getKey());
            }
        });
        Pair<Integer, MatchCandidate> first = pairs.get(0);
        int i;
        for (i = 1; i < pairs.size(); ++i) {
            if (!first.getKey().equals(pairs.get(i).getKey())) {
                break;
            }
        }

        // log skipped entries
        for (Pair<Integer, MatchCandidate> pair : pairs.subList(i, pairs.size())) {
            logService.info(getClass(), "Removing '" + pair.getValue().getText() + "' cuz a bad match for '" + mediaRequest.toString() + "'");
        }

        List<MatchCandidate> result = new ArrayList<>();
        for (Pair<Integer, MatchCandidate> pair : pairs.subList(0, i)) {
            result.add(pair.getValue());
        }
        return result;
    }

    private boolean isShowNameMatch(String title, Show show) {
        for (CachedShow curShow : showSearchService.statisticMatch(title)) {
            if (curShow.getId() == show.getId()) {
                return true;
            }
        }
        return false;
    }

    private boolean isTitleSuffixMatch(String titleSuffix) {
        titleSuffix = titleSuffix.trim();
        // take only when after season 1 there is text or number > 100 or  nothing
        if (StringUtils.isBlank(titleSuffix) || Character.isLetter(titleSuffix.charAt(0))) {
            // if it starts with e, then it might be something of the sort S08E01 E05 - then need to skip it
            Matcher matcher = RANGE_EPISODES_PATTERN.matcher(titleSuffix);
            return !matcher.find();
        }

        if (titleSuffix.length() >= 3) {
            String t = titleSuffix.substring(0, 3);
            if (NumberUtils.isNumber(t) && Integer.parseInt(t) >= 100) {
                return true;
            }
        }
        return false;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void downloadEpisode(User user, long torrentId) {
        Torrent torrent = torrentDao.find(torrentId);
        UserTorrent userTorrent = userEpisodeTorrentDao.findUserEpisodeTorrent(user, torrentId);
        // handling case of re-downloading here
        if (userTorrent == null) {
            userTorrent = new UserEpisodeTorrentImpl();
            userTorrent.setUser(user);
            userTorrent.setTorrent(torrent);
            userTorrentDao.persist(userTorrent);
        }
        userTorrent.setAdded(new Date());
    }

    @Override
    public List<Show> getTrackedShows(User user) {
        return showDao.getUserShows(user);
    }

    @Override
    public Show find(long showId) {
        return showDao.find(showId);
    }

    @Override
    public Collection<CachedShow> findCachedShows() {
        return showDao.findCachedShows();
    }

    @Override
    public List<Episode> find(ShowRequest showRequest) {
        return episodeDao.find(showRequest);
    }

    @Override
    public void delete(Episode episode) {
        episodeDao.delete(episode);
    }

    @Override
    public void delete(Show show) {
        showDao.delete(show);
    }

    @Override
    public boolean isShowBeingTracked(Show show) {
        return showDao.isShowBeingTracked(show);
    }


    @Override
    public Collection<Episode> getEpisodesToDownload(User user) {
        return episodeDao.getEpisodesToDownload(user);
    }

    @Override
    public Show findByName(String name) {
        return showDao.findByName(name);
    }

    @Override
    public Show findByTvRageId(int tvRageId) {
        return showDao.findByTvRageId(tvRageId);
    }

    @Override
    public void updateShow(Show show) {
        showDao.persist(show);
    }
}
