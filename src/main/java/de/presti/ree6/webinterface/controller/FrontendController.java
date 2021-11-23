package de.presti.ree6.webinterface.controller;

import com.jagrosh.jdautilities.oauth2.Scope;
import com.jagrosh.jdautilities.oauth2.entities.OAuth2Guild;
import com.jagrosh.jdautilities.oauth2.session.Session;
import de.presti.ree6.webinterface.Server;
import de.presti.ree6.webinterface.utils.RandomUtil;
import net.dv8tion.jda.api.Permission;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.Random;

@Controller
public class FrontendController {

    @GetMapping("/")
    public String main() {
        return "main/index";
    }

    @GetMapping("/discord/auth")
    public ModelAndView startDiscordAuth(HttpServletResponse httpServletResponse) {
        return new ModelAndView("redirect:" + Server.getInstance().getOAuth2Client().generateAuthorizationURL("http://localhost:8080/discord/auth/callback", Scope.GUILDS, Scope.IDENTIFY, Scope.GUILDS_JOIN));
    }

    @RequestMapping("/discord/auth/callback")
    public ModelAndView discordLogin(@RequestParam String code, @RequestParam String state) {
        Session session = null;
        String identifier = RandomUtil.getRandomBase64String();
        try {
            session = Server.getInstance().getOAuth2Client().startSession(code, state, identifier, Scope.GUILDS, Scope.IDENTIFY, Scope.GUILDS_JOIN).complete();
        } catch (Exception ignore) {}

        if (session != null) return new ModelAndView("redirect:http://localhost:8080/panel?id=" + identifier);
        else return new ModelAndView("redirect:http://localhost:8080/error?typ=session");
    }

    @RequestMapping("/panel")
    public String openPanel(@RequestParam String id, Model model) {

        Session session = null;
        List<OAuth2Guild> guilds;

        // TODO move the buttons around so they look good.

        try {
            session = Server.getInstance().getOAuth2Client().getSessionController().getSession(id);
            guilds = Server.getInstance().getOAuth2Client().getGuilds(session).complete();
            guilds.removeIf(oAuth2Guild -> !oAuth2Guild.hasPermission(Permission.ADMINISTRATOR));
            model.addAttribute("guilds", guilds);
        } catch (Exception e) {
            model.addAttribute("IsError", true);
            model.addAttribute("error", "Couldn't load Guilds! (" + (session == null ? "0x001" : "0x002")  + ")");
        }

        return "panel/index";
    }
}
