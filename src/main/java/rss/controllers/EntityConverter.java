package rss.controllers;

import org.springframework.stereotype.Service;
import rss.controllers.vo.ShowVO;
import rss.controllers.vo.UserVO;
import rss.entities.Show;
import rss.entities.User;

import java.util.ArrayList;
import java.util.Collection;

/**
 * User: dikmanm
 * Date: 07/02/13 10:08
 */
@Service
public class EntityConverter {

	public ArrayList<ShowVO> toThinShows(Collection<Show> shows) {
		ArrayList<ShowVO> result = new ArrayList<>();
		for (Show show : shows) {
			result.add(new ShowVO().withId(show.getId()).withName(show.getName()));
		}
		return result;
	}

	public UserVO toThinUser(User user) {
		UserVO userVO = new UserVO()
				.withEmail(user.getFirstName())
				.withAdmin(user.isAdmin());
		if (user.getSubtitles() == null) {
			userVO.setSubtitles(null);
		} else {
			userVO.setSubtitles(user.getSubtitles().toString());
		}
		return userVO;
	}
}
