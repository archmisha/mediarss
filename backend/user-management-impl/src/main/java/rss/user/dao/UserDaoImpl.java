package rss.user.dao;

import org.springframework.stereotype.Repository;
import rss.ems.dao.BaseDaoJPA;
import rss.user.User;

import java.util.HashMap;
import java.util.Map;

/**
 * User: Michael Dikman
 * Date: 24/05/12
 * Time: 11:05
 */
@Repository
public class UserDaoImpl extends BaseDaoJPA<User> implements UserDao {

    @Override
    protected Class<? extends User> getPersistentClass() {
        return UserImpl.class;
    }

    @Override
    public User findByEmail(String email) {
        Map<String, Object> params = new HashMap<>(1);
        params.put("email", email.toLowerCase());
        return uniqueResult(super.<User>findByNamedQueryAndNamedParams("User.findByEmail", params));
    }
}
