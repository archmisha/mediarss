package rss.controllers;

import org.springframework.stereotype.Service;
import rss.controllers.vo.ShowVO;
import rss.controllers.vo.UserVO;
import rss.entities.Show;
import rss.entities.User;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * User: dikmanm
 * Date: 07/02/13 10:08
 */
@Service
public class EntityConverter {

	public List<ShowVO> toThinShows(Collection<Show> shows) {
		ArrayList<ShowVO> result = new ArrayList<>();
		for (Show show : shows) {
			result.add(new ShowVO().withId(show.getId()).withName(show.getName()));
		}
		return result;
	}

	public Collection<UserVO> toThinUser(Collection<User> users) {
		ArrayList<UserVO> result = new ArrayList<>();
		for (User user : users) {
			result.add(toThinUser(user));
		}
		return result;
	}

	public UserVO toThinUser(User user) {
		UserVO userVO = new UserVO()
				.withEmail(user.getEmail())
				.withFirstName(user.getFirstName())
				.withLastName(user.getLastName())
				.withLastLogin(user.getLastLogin())
				.withLastShowsFeedAccess(user.getLastShowsFeedGenerated())
				.withLastMoviesFeedAccess(user.getLastMoviesFeedGenerated())
				.withAdmin(user.isAdmin());
		if (user.getSubtitles() == null) {
			userVO.setSubtitles(null);
		} else {
			userVO.setSubtitles(user.getSubtitles().toString());
		}
		return userVO;
	}
}
