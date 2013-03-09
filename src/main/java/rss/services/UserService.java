package rss.services;

import rss.entities.User;
import rss.controllers.vo.UserResponse;

/**
 * User: Michael Dikman
 * Date: 15/12/12
 * Time: 12:10
 */
public interface UserService {

    String register(String firstName, String lastName, final String email, final String password);

    UserResponse getUserResponse(User user);
}
