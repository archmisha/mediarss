package rss.dao;

import rss.ems.dao.Dao;
import rss.entities.JobStatus;

/**
 * User: Michael Dikman
 * Date: 12/05/12
 * Time: 15:30
 */
public interface JobStatusDao extends Dao<JobStatus> {

    JobStatus find(String name);
}
