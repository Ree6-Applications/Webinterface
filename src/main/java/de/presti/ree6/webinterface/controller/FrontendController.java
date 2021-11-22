package de.presti.ree6.webinterface.controller;

import com.jagrosh.jdautilities.oauth2.Scope;
import de.presti.ree6.webinterface.Server;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletResponse;

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
    public String discordLogin(@RequestParam String code, @RequestParam String state) {
        System.out.println(code + " - " + state);
        return "main/index";
    }
}
