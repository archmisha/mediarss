package rss.scheduler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import rss.permissions.PermissionsService;
import rss.util.JsonTranslation;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

/**
 * User: dikmanm
 * Date: 10/02/13 18:16
 */
@Path("/jobs")
@Component
public class JobsResource {

    @Autowired
    private PermissionsService permissionsService;

    @Autowired
    private SchedulerService schedulerService;

    @Path("/all")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAll() {
        permissionsService.verifyAdminPermissions();

        List<JobStatusJson> jobs = schedulerService.getAllJobs();

        return Response.ok().entity(JsonTranslation.object2JsonString(jobs.toArray(new JobStatusJson[jobs.size()]))).build();
    }

    @Path("/start/{name}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response start(@PathParam("name") String name) {
        permissionsService.verifyAdminPermissions();

        JobStatusJson jobStatus = schedulerService.executeJob(name);

        return Response.ok().entity(JsonTranslation.object2JsonString(jobStatus)).build();
    }

    @Path("/{name}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response get(@PathParam("name") String name) {
        permissionsService.verifyAdminPermissions();

        JobStatusJson jobStatus = schedulerService.getJobStatus(name);

        return Response.ok().entity(JsonTranslation.object2JsonString(jobStatus)).build();
    }
}