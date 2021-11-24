package de.presti.ree6.webinterface.controller;

import com.jagrosh.jdautilities.oauth2.Scope;
import com.jagrosh.jdautilities.oauth2.entities.OAuth2User;
import com.jagrosh.jdautilities.oauth2.requests.OAuth2URL;
import com.jagrosh.jdautilities.oauth2.session.Session;
import de.presti.ree6.webinterface.Server;
import org.json.JSONObject;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class BackendController {

    @RequestMapping(value = "/api/test", produces = "application/json")
    public JSONObject test(@RequestParam String id) {
        try {
            Session session = Server.getInstance().getOAuth2Client().getSessionController().getSession(id);
            OAuth2User oAuth2User = Server.getInstance().getOAuth2Client().getUser(session).complete();

            Server.getInstance().getOAuth2Client().joinGuild(oAuth2User, 805149057004732457L).complete();

            return new JSONObject().put("success", true);

        } catch (Exception ignore) {
            ignore.printStackTrace();
        }

        return new JSONObject().put("success", false);
    }

}
