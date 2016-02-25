package rss.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import rss.permissions.PermissionsService;
import rss.user.UserService;

import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

/**
 * User: dikmanm
 * Date: 10/02/13 18:16
 */
@Path("/admin")
@Component
public class AdminResource {

    @Autowired
    private PermissionsService permissionsService;

    @Autowired
    private UserService userService;

    @RequestMapping(value = "/notification", method = RequestMethod.POST)
    @ResponseBody
    public Response sendNotification(@QueryParam("text") String text) {
        permissionsService.verifyAdminPermissions();

        userService.sendEmailToAllUsers(text);

        return Response.ok().build();
    }


}