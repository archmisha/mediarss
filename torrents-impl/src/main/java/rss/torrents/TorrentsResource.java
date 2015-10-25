package rss.torrents;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import rss.permissions.PermissionsService;
import rss.torrents.searchers.config.SearcherConfiguration;
import rss.torrents.searchers.config.SearcherConfigurationService;
import rss.util.JsonTranslation;

import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import java.util.Collection;

/**
 * Created by dikmanm on 25/10/2015.
 */
@Path("/torrents")
@Component
public class TorrentsResource {

    @Autowired
    private PermissionsService permissionsService;

    @Autowired
    private SearcherConfigurationService searcherConfigurationService;

    @RequestMapping(value = "/searcher-configurations", method = RequestMethod.GET)
    @ResponseBody
    public Response getSearcherConfigurations() {
        permissionsService.verifyAdminPermissions();

        Collection<SearcherConfiguration> searcherConfigurations = searcherConfigurationService.getSearcherConfigurations();

        return Response.ok().entity(JsonTranslation.object2JsonString(searcherConfigurations.toArray(new SearcherConfiguration[searcherConfigurations.size()]))).build();
    }

    @RequestMapping(value = "/searcher-configuration/{name}/domain/add", method = RequestMethod.POST)
    @ResponseBody
    @Transactional(propagation = Propagation.REQUIRED)
    public Response addDomainToSearcherConfiguration(@PathVariable String name, @RequestParam("domain") String domain) {
        permissionsService.verifyAdminPermissions();

        searcherConfigurationService.addDomain(name, domain);
        return Response.ok().build();
    }

    @RequestMapping(value = "/searcher-configuration/{name}/domain/remove/{domain}", method = RequestMethod.GET)
    @ResponseBody
    @Transactional(propagation = Propagation.REQUIRED)
    public Response removeDomainToSearcherConfiguration(@PathVariable String name, @PathVariable String domain) {
        permissionsService.verifyAdminPermissions();

        searcherConfigurationService.removeDomain(name, domain);
        return Response.ok().build();
    }

    @RequestMapping(value = "/searcher-configuration/torrentz/enable", method = RequestMethod.POST)
    @ResponseBody
    @Transactional(propagation = Propagation.REQUIRED)
    public Response enableTorrentzSearcher(@RequestParam boolean isEnabled) {
        permissionsService.verifyAdminPermissions();

        searcherConfigurationService.torrentzSetEnabled(isEnabled);
        return Response.ok().build();
    }
}
