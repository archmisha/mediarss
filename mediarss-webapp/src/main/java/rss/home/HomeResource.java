package rss.home;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import rss.cache.UserCacheService;
import rss.context.UserContextHolder;
import rss.controllers.EntityConverter;
import rss.log.LogService;
import rss.services.feed.RssFeedGenerator;
import rss.subtitles.SubtitlesService;
import rss.torrents.Subtitles;
import rss.torrents.Torrent;
import rss.user.User;
import rss.user.subtitles.SubtitleLanguage;
import rss.util.DurationMeter;
import rss.util.JsonTranslation;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Path("/user")
@Component
public class HomeResource {

    @Autowired
    private LogService logService;

    @Autowired
    private SubtitlesService subtitlesService;

    @Autowired
    private UserCacheService userCacheService;

    @Autowired
    @Qualifier("tVShowsRssFeedGeneratorImpl")
    private RssFeedGenerator tvShowsRssFeedGenerator;

    @Autowired
    protected EntityConverter entityConverter;

    @Path("/subtitles")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional(propagation = Propagation.REQUIRED)
    public Response subtitles(@QueryParam("subtitles") String subtitles) {
        User user = userCacheService.getUser(UserContextHolder.getCurrentUserContext().getUserId());
        user.setSubtitles(SubtitleLanguage.fromString(subtitles));
        return Response.ok().build();
    }

    @Path("/initial-data")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional(propagation = Propagation.REQUIRED)
    public Response initialData() {
        User user = userCacheService.getUser(UserContextHolder.getCurrentUserContext().getUserId());

        DurationMeter duration = new DurationMeter();
        Map<String, Object> result = new HashMap<>();
        result.put("subtitles", SubtitleLanguage.getValues());
        result.put("userSubtitles", user.getSubtitles() == null ? null : user.getSubtitles().toString());

        Set<Torrent> torrents = tvShowsRssFeedGenerator.getFeedTorrents(user);
        Collection<Subtitles> subtitles = subtitlesService.find(torrents, user.getSubtitles());
        result.put("recentSubtitles", entityConverter.toThinSubtitles(subtitles, torrents));

        duration.stop();
        logService.info(getClass(), "initialData " + duration.getDuration() + " ms");

        return Response.ok().entity(JsonTranslation.object2JsonString(result)).build();
    }
}