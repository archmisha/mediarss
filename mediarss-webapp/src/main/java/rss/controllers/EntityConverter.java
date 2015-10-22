package rss.controllers;

import org.springframework.stereotype.Service;
import rss.context.UserContextHolder;
import rss.controllers.vo.SubtitlesVO;
import rss.controllers.vo.UserVO;
import rss.environment.Environment;
import rss.environment.ServerMode;
import rss.torrents.Subtitles;
import rss.torrents.Torrent;
import rss.user.User;

import java.util.*;

/**
 * User: dikmanm
 * Date: 07/02/13 10:08
 */
@Service
public class EntityConverter {

    public List<UserVO> toThinUser(Collection<User> users) {
        ArrayList<UserVO> result = new ArrayList<>();
        for (User user : users) {
            result.add(toThinUser(user));
        }
        return result;
    }

    public UserVO toThinUser(User user) {
        UserVO userVO = new UserVO()
                .withId(user.getId())
                .withLoggedIn(UserContextHolder.getCurrentUserContext().getUserId() == user.getId())
                .withEmail(user.getEmail())
                .withFirstName(user.getFirstName())
                .withLastName(user.getLastName())
                .withLastLogin(user.getLastLogin())
                .withLastShowsFeedAccess(user.getLastShowsFeedGenerated())
                .withLastMoviesFeedAccess(user.getLastMoviesFeedGenerated())
                .withAdmin(Environment.getInstance().getAdministratorEmails().contains(user.getEmail()));
        if (user.getSubtitles() == null) {
            userVO.setSubtitles(null);
        } else {
            userVO.setSubtitles(user.getSubtitles().toString());
        }
        if (Environment.getInstance().getServerMode() == ServerMode.TEST) {
            userVO.setValidationHash(user.getValidationHash());
        }
        return userVO;
    }

    public List<SubtitlesVO> toThinSubtitles(Collection<Subtitles> subtitles, Collection<Torrent> torrents) {
        Map<Long, Torrent> torrentsByIds = new HashMap<>();
        for (Torrent torrent : torrents) {
            torrentsByIds.put(torrent.getId(), torrent);
        }

        ArrayList<SubtitlesVO> result = new ArrayList<>();
        for (Subtitles subtitle : subtitles) {
            for (Long torrentId : subtitle.getTorrentIds()) {
                Torrent torrent = torrentsByIds.get(torrentId);
                // a subtitles might have multiple torrents attached, but if that subtitle was queried by only one of the torrents
                // the others will not be present in the map
                if (torrent != null) {
                    SubtitlesVO subtitlesVO = new SubtitlesVO();
                    subtitlesVO.setType("shows");
                    subtitlesVO.setName(torrent.getTitle());
                    subtitlesVO.setLanguage(subtitle.getLanguage().toString());
                    subtitlesVO.setId(subtitle.getId());
                    result.add(subtitlesVO);
                }
            }
        }

        return result;
    }
//
//    public Show fromThinShow(ShowJSON showJSON) {
//        Show show = new Show(showJSON.getName());
//        show.setEnded(showJSON.isEnded());
//        return show;
//    }
}
