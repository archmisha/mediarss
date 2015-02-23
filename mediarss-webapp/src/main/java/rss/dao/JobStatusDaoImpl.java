package rss.dao;

import org.springframework.stereotype.Repository;
import rss.ems.dao.BaseDaoJPA;
import rss.entities.JobStatus;

import java.util.HashMap;
import java.util.Map;

/**
 * User: Michael Dikman
 * Date: 24/05/12
 * Time: 11:05
 */
@Repository
public class JobStatusDaoImpl extends BaseDaoJPA<JobStatus> implements JobStatusDao {

    @Override
    public JobStatus find(String name) {
        Map<String, Object> params = new HashMap<String, Object>(1);
        params.put("name", name);
        return uniqueResult(super.<JobStatus>findByNamedQueryAndNamedParams("JobStatus.findByName", params));
    }
}
