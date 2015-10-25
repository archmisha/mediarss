package rss;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import rss.shows.tvrage.TVRageEpisode;
import rss.shows.tvrage.TVRageSeason;
import rss.shows.tvrage.TVRageShow;
import rss.shows.tvrage.TVRageShowInfo;
import rss.util.JsonTranslation;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

/**
 * User: dikmanm
 * Date: 17/08/2015 09:40
 */
@Path("/test-pages")
@Component
public class TestPagesResource {

    @Autowired
    private TestPagesServiceImpl testPagesService;

    @Path("/resetOverrides")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response resetOverrides() {
        testPagesService.resetOverrides();
        return Response.ok().build();
    }

    @Path("/shows/show")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createShow(String json) {
        TVRageShow show = JsonTranslation.jsonString2Object(json, TVRageShow.class);
        testPagesService.createShow(show);
        return Response.ok().build();
    }

    @Path("/shows/info")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createShowInfo(String json) {
        TVRageShowInfo showInfo = JsonTranslation.jsonString2Object(json, TVRageShowInfo.class);
        testPagesService.createShowInfo(showInfo);
        return Response.ok().build();
    }

    @Path("/shows/set-ended/{showId}")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response markShowEnded(@PathParam("showId") long showId) {
        testPagesService.markShowEnded(showId);
        return Response.ok().build();
    }

    @Path("/shows/info")
    @GET
    @Produces(MediaType.APPLICATION_XML)
    public Response getShowsList(@QueryParam("sid") String showId) {
        TVRageShowInfo showInfo = testPagesService.getShowInfo(Integer.parseInt(showId));

        StringBuilder sb = new StringBuilder();
        sb.append("<Show>");
        if (showInfo != null) {
            sb.append("<Episodelist>");
            for (TVRageSeason tvRageSeason : showInfo.getEpisodelist().getSeasons()) {
                sb.append("<Season no=\"").append(tvRageSeason.getNo()).append("\">");
                for (TVRageEpisode tvRageEpisode : tvRageSeason.getEpisodes()) {
                    sb.append("<episode>");
                    sb.append("<seasonnum>").append(tvRageEpisode.getSeasonnum()).append("</seasonnum>");
                    sb.append("<airdate>").append(tvRageEpisode.getAirdate()).append("</airdate>");
                    sb.append("</episode>");
                }
                sb.append("</Season>");
            }
            sb.append("</Episodelist>");
        }
        sb.append("</Show>");
        return Response.ok().entity(sb.toString()).build();
    }

    @Path("/shows/list")
    @GET
    @Produces(MediaType.APPLICATION_XML)
    public Response getShowsList() {
        List<TVRageShow> shows = testPagesService.getShowsList();

        // convert to TVRage style xml
        StringBuilder sb = new StringBuilder();
        sb.append("<shows>");
        for (TVRageShow show : shows) {
            sb.append("<show>");
            sb.append("<id>").append(show.getId()).append("</id>");
            sb.append("<name>").append(show.getName()).append("</name>");
            sb.append("<country>").append(show.getCountry()).append("</country>");
            sb.append("<status>").append(show.getStatus()).append("</status>");
            sb.append("</show>");
        }
        sb.append("</shows>");

        return Response.ok().entity(sb.toString()).build();
    }

    @Path("/1337x/search/{query}/0/")
    @GET
    @Produces(MediaType.APPLICATION_XML)
    public Response search(@PathParam("query") String query) {
        try {
            String page = IOUtils.toString(new ClassPathResource("1337x-1-search-results.html", getClass().getClassLoader()).getInputStream());
            page = page.replace("${torrentId}", unique());
            page = page.replace("${torrentName}", query);
            return Response.ok().entity(page).build();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Path("/1337x/torrent/{torrentId}/{query}")
    @GET
    @Produces(MediaType.APPLICATION_XML)
    public Response search(@PathParam("torrentId") String torrentId, @PathParam("query") String query) {
        try {
            String page = IOUtils.toString(new ClassPathResource("1337x-torrent-page.html", getClass().getClassLoader()).getInputStream());
            page = page.replace("${torrentName}", query);
            page = page.replace("${hash}", unique());
            return Response.ok().entity(page).build();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private String unique() {
        return RandomStringUtils.randomAlphanumeric(5).toLowerCase();
    }
}
