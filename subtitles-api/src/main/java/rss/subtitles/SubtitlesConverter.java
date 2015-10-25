package rss.subtitles;

import rss.torrents.Subtitles;
import rss.torrents.Torrent;

import java.util.*;

/**
 * Created by dikmanm on 25/10/2015.
 */
public class SubtitlesConverter {

    public List<SubtitlesJSON> toSubtitlesJSON(Collection<Subtitles> subtitles, Collection<Torrent> torrents) {
        Map<Long, Torrent> torrentsByIds = new HashMap<>();
        for (Torrent torrent : torrents) {
            torrentsByIds.put(torrent.getId(), torrent);
        }

        ArrayList<SubtitlesJSON> result = new ArrayList<>();
        for (Subtitles subtitle : subtitles) {
            for (Long torrentId : subtitle.getTorrentIds()) {
                Torrent torrent = torrentsByIds.get(torrentId);
                // a subtitles might have multiple torrents attached, but if that subtitle was queried by only one of the torrents
                // the others will not be present in the map
                if (torrent != null) {
                    SubtitlesJSON subtitlesJSON = new SubtitlesJSON();
                    subtitlesJSON.setType("shows");
                    subtitlesJSON.setName(torrent.getTitle());
                    subtitlesJSON.setLanguage(subtitle.getLanguage().toString());
                    subtitlesJSON.setId(subtitle.getId());
                    result.add(subtitlesJSON);
                }
            }
        }

        return result;
    }
}
