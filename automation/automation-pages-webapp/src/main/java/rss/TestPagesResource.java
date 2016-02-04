package rss;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import rss.shows.thetvdb.TheTvDbEpisode;
import rss.shows.thetvdb.TheTvDbShow;
import rss.util.JsonTranslation;

import javax.ws.rs.*;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * User: dikmanm
 * Date: 17/08/2015 09:40
 */
@Path("/test-pages")
@Component
public class TestPagesResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestPagesResource.class);

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
        TheTvDbShow show = JsonTranslation.jsonString2Object(json, TheTvDbShow.class);
        testPagesService.createShow(show);
        return Response.ok().build();
    }

    @Path("/shows/episode")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createEpisode(String json) {
        TheTvDbEpisode episode = JsonTranslation.jsonString2Object(json, TheTvDbEpisode.class);
        testPagesService.createEpisode(episode);
        return Response.ok().build();
    }

    @Path("/torrents")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createTorrent(String json) {
        Map<String, String> params = JsonTranslation.jsonString2Object(json, Map.class);
        String showName = params.get("show");
        String season = params.get("season");
        String episode = params.get("episode");
        LOGGER.info("Adding torrent " + showName + " " + season + " " + episode);
        testPagesService.createTorrent(showName, season, episode);
        return Response.ok().build();
    }

    @Path("/shows/set-ended/{showId}")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response markShowEnded(@PathParam("showId") long showId) {
        testPagesService.markShowEnded(showId);
        return Response.ok().build();
    }

    @Path("/shows/search")
    @GET
    @Produces(MediaType.APPLICATION_XML)
    public Response searchShow(@QueryParam("name") String name) {
        TheTvDbShow show = testPagesService.getShow(name);
        StringBuilder sb = new StringBuilder();
        sb.append("<Data>");
        if (show != null) {
            sb.append("<Series>");
            sb.append("<id>").append(show.getId()).append("</id>");
            sb.append("<SeriesName>").append(show.getName()).append("</SeriesName>");
            sb.append("</Series>");
        }
        sb.append("</Data>");
        return Response.ok().entity(sb.toString()).build();
    }

    @Path("/1337x/search/{query}/0/")
    @GET
    @Produces(MediaType.APPLICATION_XML)
    public Response search(@PathParam("query") String query) {
        try {
            LOGGER.info("Searching 1337x for " + query + ". Returning " + (testPagesService.hasTorrentId(query) ? "found" : "not found"));

            // only of torrentId is mapped, return it in the search results
            String torrentName = testPagesService.hasTorrentId(query) ? query : unique();
            String page = IOUtils.toString(new ClassPathResource("1337x-1-search-results.html", getClass().getClassLoader()).getInputStream());
            page = page.replace("${torrentId}", unique());
            page = page.replace("${torrentName}", torrentName);
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
            LOGGER.info("Searching 1337x for torrentId " + torrentId + " and query " + query + ". Returning " + (testPagesService.hasTorrentId(query) ? "found" : "not found"));

            String page = IOUtils.toString(new ClassPathResource("1337x-torrent-page.html", getClass().getClassLoader()).getInputStream());
            page = page.replace("${torrentName}", query);
            page = page.replace("${hash}", unique());
            return Response.ok().entity(page).build();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Path("/thetvdb/episodes/info/{episodeId}/en.xml")
    @GET
    @Produces(MediaType.APPLICATION_XML)
    public Response getEpisode(@PathParam("episodeId") Long episodeId) {
        TheTvDbEpisode episode = testPagesService.getEpisode(episodeId);
        StringBuilder sb = new StringBuilder();
        sb.append("<Data>");
        if (episode != null) {
            sb.append(toXml(episode));
        }
        sb.append("</Data>");
        return Response.ok().entity(sb.toString()).build();
    }

    @Path("/thetvdb/shows/info/{showId}/all/en.zip")
    @GET
    @Produces("application/zip")
    public Response getShow(@PathParam("showId") Long showId) {
        try {
            TheTvDbShow show = testPagesService.getShow(showId);

            StringBuilder sb = new StringBuilder();
            sb.append("<Data>");
            if (show != null) {
                sb.append("<Series>");
                sb.append("<id>").append(show.getId()).append("</id>");
                sb.append("<SeriesName>").append(show.getName()).append("</SeriesName>");
                sb.append("<Status>").append(show.getStatus()).append("</Status>");
                sb.append("</Series>");

                for (TheTvDbEpisode episode : testPagesService.getEpisodesByShow(showId)) {
                    sb.append("<Episode>");
                    sb.append(toXml(episode));
                    sb.append("</Episode>");
                }
            }
            sb.append("</Data>");

            LOGGER.info("show info: " + sb.toString());

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ZipOutputStream zos = new ZipOutputStream(baos);
            zos.putNextEntry(new ZipEntry("en.xml"));
            zos.write(sb.toString().getBytes());
            zos.closeEntry();
            zos.close();
            baos.close();

            CacheControl cacheControl = new CacheControl();
            cacheControl.setNoCache(true);
            return Response.ok(baos.toByteArray()).cacheControl(cacheControl).build();
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Path("/thetvdb/server-time")
    @GET
    @Produces(MediaType.APPLICATION_XML)
    public Response getServerTime() {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("<Update>");
            sb.append("<Time>").append(System.currentTimeMillis()).append("</Time>");
            sb.append("</Update>");
            return Response.ok().entity(sb.toString()).build();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Path("/thetvdb/updates/{serverTime}")
    @GET
    @Produces(MediaType.APPLICATION_XML)
    public Response getUpdates(@PathParam("serverTime") Long serverTime) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("<Items>");
            sb.append("<Time>").append(System.currentTimeMillis()).append("</Time>");
            for (TheTvDbEpisode episode : testPagesService.getEpisodes(serverTime)) {
                sb.append("<Episode>").append(episode.getId()).append("</Episode>");
            }
            for (TheTvDbShow show : testPagesService.getShows(serverTime)) {
                sb.append("<Series>").append(show.getId()).append("</Series>");
            }
            sb.append("</Items>");

            LOGGER.info("updates: " + sb.toString());

            return Response.ok().entity(sb.toString()).build();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private String toXml(TheTvDbEpisode episode) {
        StringBuilder sb = new StringBuilder();
        sb.append("<id>").append(episode.getId()).append("</id>");
        sb.append("<SeasonNumber>").append(episode.getSeason()).append("</SeasonNumber>");
        sb.append("<EpisodeNumber>").append(episode.getEpisode()).append("</EpisodeNumber>");
        sb.append("<FirstAired>").append(episode.getAirDate()).append("</FirstAired>");
        sb.append("<seriesid>").append(episode.getShowId()).append("</seriesid>");
        return sb.toString();
    }

    private String unique() {
        return RandomStringUtils.randomAlphanumeric(5).toLowerCase();
    }
}
