package rss.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import rss.user.context.UserContextHolder;
import rss.mail.EmailClassification;
import rss.mail.EmailService;
import rss.user.User;

import javax.servlet.http.HttpServletRequest;

/**
 * User: dikmanm
 * Date: 10/02/13 18:16
 */
@Controller
@RequestMapping("/support")
public class SupportController extends BaseController {

    @Autowired
    private EmailService emailService;

    @RequestMapping(value = "", method = RequestMethod.POST)
    @ResponseBody
    public String submitSupportTicket(HttpServletRequest request) {
        String type = extractString(request, "type", true);
        String content = extractString(request, "content", true);
        User user = userCacheService.getUser(UserContextHolder.getCurrentUserContext().getUserId());

        EmailClassification classification;
        if (type.equals("Feature")) {
            classification = EmailClassification.SUPPORT_FEATURE;
        } else {
            classification = EmailClassification.SUPPORT_DEFECT;
        }
        emailService.notifyToAdmins(classification,
                "User " + user.getFirstName() + " " + user.getLastName() + " (" + user.getEmail() +
                        ") has submitted the following " + type + ": \n" + content,
                "Failed sending email of a new ticket");
        return type + " was successfully submitted";
    }
}