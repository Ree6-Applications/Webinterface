package de.presti.ree6.webinterface.controller;

import com.jagrosh.jdautilities.oauth2.Scope;
import com.jagrosh.jdautilities.oauth2.entities.OAuth2Guild;
import com.jagrosh.jdautilities.oauth2.session.Session;
import de.presti.ree6.webinterface.Server;
import de.presti.ree6.webinterface.bot.BotInfo;
import de.presti.ree6.webinterface.utils.RandomUtil;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * Controller for the Frontend to manage what the user sees.
 */
@Controller
public class FrontendController {

    /**
     * A Get Mapper for the Main Page.
     * @return {@link String} for Thyme to the HTML Page.
     */
    @GetMapping("/")
    public String main() {
        return "main/index";
    }

    /**
     * A Get Mapper for generation of a Discord OAuth2 Session
     * @return {@link ModelAndView} with the redirect data.
     */
    @GetMapping("/discord/auth")
    public ModelAndView startDiscordAuth() {
        return new ModelAndView("redirect:" + Server.getInstance().getOAuth2Client().generateAuthorizationURL("http://localhost:8080/discord/auth/callback", Scope.GUILDS, Scope.IDENTIFY, Scope.GUILDS_JOIN));
    }

    /**
     * The Request Mapper for the Discord Auth callback.
     * @param code the OAuth2 Code from Discord.
     * @param state the local State of the OAuth2 Session.
     * @return {@link ModelAndView} with the redirect data.
     */
    @RequestMapping("/discord/auth/callback")
    public ModelAndView discordLogin(@RequestParam String code, @RequestParam String state) {
        Session session = null;

        // Generate a secure Base64 String for the Identifier.
        String identifier = RandomUtil.getRandomBase64String();

        try {
            // Try creating a Session.
            session = Server.getInstance().getOAuth2Client().startSession(code, state, identifier, Scope.GUILDS, Scope.IDENTIFY, Scope.GUILDS_JOIN).complete();
        } catch (Exception ignore) {}

        // If the given data was valid and a Session has been created redirect to the panel Site. If not redirect to error.
        if (session != null) return new ModelAndView("redirect:http://localhost:8080/panel?id=" + identifier);
        else return new ModelAndView("redirect:http://localhost:8080/error");
    }

    /**
     * Request Mapper for the Server selection Panel.
     * @param id the Session Identifier.
     * @param model the ViewModel.
     * @return {@link String} for Thyme to the HTML Page.
     */
    @RequestMapping("/panel")
    public String openPanel(@RequestParam String id, Model model) {

        Session session = null;
        List<OAuth2Guild> guilds;

        try {
            // Try retrieving the Session from the Identifier.
            session = Server.getInstance().getOAuth2Client().getSessionController().getSession(id);

            // Try retrieving the Guilds of the OAuth2 User.
            guilds = Server.getInstance().getOAuth2Client().getGuilds(session).complete();

            // Remove every Guild from the List where the OAuth2 User doesn't have Administration permission.
            guilds.removeIf(oAuth2Guild -> !oAuth2Guild.hasPermission(Permission.ADMINISTRATOR));

            // Set the Identifier.
            model.addAttribute("identifier", id);

            // Add the Guilds as Attribute to the ViewModel.
            model.addAttribute("guilds", guilds);
        } catch (Exception e) {
            // If the Session is null just return to the default Page.
            if (session == null) return "main/index";

            // If the Session isn't null give the User a Notification that his Guilds couldn't be loaded.
            model.addAttribute("IsError", true);
            model.addAttribute("error", "Couldn't load Guilds!");
        }

        // Return Panel Page.
        return "panel/index";
    }

    /**
     * Request Mapper for the Server Panel Page.
     * @param id the Session Identifier.
     * @param guildID the ID of the selected Guild.
     * @param model the ViewModel.
     * @return {@link String} for Thyme to the HTML Page.
     */
    @RequestMapping("/panel/server")
    public String openServerPanel(@RequestParam String id, @RequestParam String guildID, Model model) {

        Session session = null;

        // TODO add actual data getting from JDA Bot Instance.

        try {
            // Try retrieving the Session from the Identifier.
            session = Server.getInstance().getOAuth2Client().getSessionController().getSession(id);

            // Try retrieving the Guild of the OAuth2 User by its ID.
            List<OAuth2Guild> guildList = Server.getInstance().getOAuth2Client().getGuilds(session).complete();
            guildList.removeIf(guild -> !guild.getId().equalsIgnoreCase(guildID) || !guild.hasPermission(Permission.ADMINISTRATOR));

            // If the given Guild ID couldn't be found in his Guild list redirect him to the Error page.
            if (guildList.size() <= 0) return "error/index";

            // Retrieve the Guild by its giving ID.
            Guild guild = BotInfo.botInstance.getGuildById(guildID);

            // If the Guild couldn't be loaded redirect to Error page.
            if (guild == null) return "error/index";

            // Set the Identifier.
            model.addAttribute("identifier", id);

            // If a Guild has been found set it as Attribute.
            model.addAttribute("guild", guildList.stream().findFirst().get());

            // Retrieve every Role and Channel of the Guild and set them as Attribute.
            model.addAttribute("invites", Server.getInstance().getSqlConnector().getSqlWorker().getInvites(guild.getId()));
            model.addAttribute("commandstats", "");
        } catch (Exception e) {
            // If the Session is null just return to the default Page.
            if (session == null) return "main/index";

            // If the Session isn't null give the User a Notification that the Guild couldn't be loaded.
            model.addAttribute("IsError", true);
            model.addAttribute("error", "Couldn't load Guild Information! ");
        }

        // Return to the Server Panel Page.
        return "panel/server/index";
    }

    /**
     * Request Mapper for the Social Panel Page.
     * @param id the Session Identifier.
     * @param guildID the ID of the selected Guild.
     * @param model the ViewModel.
     * @return {@link String} for Thyme to the HTML Page.
     */
    @RequestMapping("/panel/social")
    public String openPanelSocial(@RequestParam String id, @RequestParam String guildID, Model model) {

        Session session = null;

        // TODO add actual data getting from JDA Bot Instance.

        try {
            // Try retrieving the Session from the Identifier.
            session = Server.getInstance().getOAuth2Client().getSessionController().getSession(id);

            // Try retrieving the Guild of the OAuth2 User by its ID.
            List<OAuth2Guild> guildList = Server.getInstance().getOAuth2Client().getGuilds(session).complete();
            guildList.removeIf(guild -> !guild.getId().equalsIgnoreCase(guildID) || !guild.hasPermission(Permission.ADMINISTRATOR));

            // If the given Guild ID couldn't be found in his Guild list redirect him to the Error page.
            if (guildList.size() <= 0) return "error/index";

            // Retrieve the Guild by its giving ID.
            Guild guild = BotInfo.botInstance.getGuildById(guildID);

            // If the Guild couldn't be loaded redirect to Error page.
            if (guild == null) return "error/index";

            // Set the Identifier.
            model.addAttribute("identifier", id);

            // If a Guild has been found set it as Attribute.
            model.addAttribute("guild", guildList.stream().findFirst().get());

            // Retrieve every Role and Channel of the Guild and set them as Attribute.
            model.addAttribute("roles", guild.getRoles());
            model.addAttribute("channels", guild.getTextChannels());
        } catch (Exception e) {
            // If the Session is null just return to the default Page.
            if (session == null) return "main/index";

            // If the Session isn't null give the User a Notification that the Guild couldn't be loaded.
            model.addAttribute("IsError", true);
            model.addAttribute("error", "Couldn't load Guild Information! ");
        }

        // Return to the Social Panel Page.
        return "panel/social/index";
    }

    /**
     * Request Mapper for the Logging Panel Page.
     * @param id the Session Identifier.
     * @param guildID the ID of the selected Guild.
     * @param model the ViewModel.
     * @return {@link String} for Thyme to the HTML Page.
     */
    @RequestMapping("/panel/logging")
    public String openPanelLogging(@RequestParam String id, @RequestParam String guildID, Model model) {

        Session session = null;

        // TODO add actual data getting from JDA Bot Instance.

        try {
            // Try retrieving the Session from the Identifier.
            session = Server.getInstance().getOAuth2Client().getSessionController().getSession(id);

            // Try retrieving the Guild of the OAuth2 User by its ID.
            List<OAuth2Guild> guildList = Server.getInstance().getOAuth2Client().getGuilds(session).complete();
            guildList.removeIf(guild -> !guild.getId().equalsIgnoreCase(guildID) || !guild.hasPermission(Permission.ADMINISTRATOR));

            // If the given Guild ID couldn't be found in his Guild list redirect him to the Error page.
            if (guildList.size() <= 0) return "error/index";

            // Retrieve the Guild by its giving ID.
            Guild guild = BotInfo.botInstance.getGuildById(guildID);

            // If the Guild couldn't be loaded redirect to Error page.
            if (guild == null) return "error/index";

            // Set the Identifier.
            model.addAttribute("identifier", id);

            // If a Guild has been found set it as Attribute.
            model.addAttribute("guild", guildList.stream().findFirst().get());

            // Retrieve every Log Option and Channel of the Guild and set them as Attribute.
            model.addAttribute("logs", Server.getInstance().getSqlConnector().getSqlWorker());
            model.addAttribute("channels", guild.getTextChannels());
        } catch (Exception e) {
            // If the Session is null just return to the default Page.
            if (session == null) return "main/index";

            // If the Session isn't null give the User a Notification that the Guild couldn't be loaded.
            model.addAttribute("IsError", true);
            model.addAttribute("error", "Couldn't load Guild Information! ");
        }

        // Return to the Logging Panel Page.
        return "panel/logging/index";
    }

    /**
     * Request Mapper for the Moderation Panel Page.
     * @param id the Session Identifier.
     * @param guildID the ID of the selected Guild.
     * @param model the ViewModel.
     * @return {@link String} for Thyme to the HTML Page.
     */
    @RequestMapping("/panel/moderation")
    public String openPanelModeration(@RequestParam String id, @RequestParam String guildID, Model model) {

        Session session = null;

        // TODO add actual data getting from JDA Bot Instance.

        try {
            // Try retrieving the Session from the Identifier.
            session = Server.getInstance().getOAuth2Client().getSessionController().getSession(id);

            // Try retrieving the Guild of the OAuth2 User by its ID.
            List<OAuth2Guild> guildList = Server.getInstance().getOAuth2Client().getGuilds(session).complete();
            guildList.removeIf(guild -> !guild.getId().equalsIgnoreCase(guildID) || !guild.hasPermission(Permission.ADMINISTRATOR));

            // If the given Guild ID couldn't be found in his Guild list redirect him to the Error page.
            if (guildList.size() <= 0) return "error/index";

            // Retrieve the Guild by its giving ID.
            Guild guild = BotInfo.botInstance.getGuildById(guildID);

            // If the Guild couldn't be loaded redirect to Error page.
            if (guild == null) return "error/index";

            // Set the Identifier.
            model.addAttribute("identifier", id);

            // If a Guild has been found set it as Attribute.
            model.addAttribute("guild", guildList.stream().findFirst().get());

            // Retrieve every Role and Channel of the Guild and set them as Attribute.
            model.addAttribute("roles", guild.getRoles());
            model.addAttribute("channels", guild.getTextChannels());
        } catch (Exception e) {
            // If the Session is null just return to the default Page.
            if (session == null) return "main/index";

            // If the Session isn't null give the User a Notification that the Guild couldn't be loaded.
            model.addAttribute("IsError", true);
            model.addAttribute("error", "Couldn't load Guild Information! ");
        }

        // Return to the Moderation Panel Page.
        return "panel/moderation/index";
    }
}
