package de.presti.ree6.webinterface.controller;

import com.jagrosh.jdautilities.oauth2.Scope;
import com.jagrosh.jdautilities.oauth2.entities.OAuth2Guild;
import com.jagrosh.jdautilities.oauth2.entities.OAuth2User;
import com.jagrosh.jdautilities.oauth2.session.Session;
import de.presti.ree6.webinterface.Server;
import de.presti.ree6.webinterface.bot.BotWorker;
import de.presti.ree6.webinterface.bot.version.BotVersion;
import de.presti.ree6.webinterface.controller.forms.ChannelChangeForm;
import de.presti.ree6.webinterface.controller.forms.RoleChangeForm;
import de.presti.ree6.webinterface.controller.forms.SettingChangeForm;
import de.presti.ree6.webinterface.sql.entities.UserLevel;
import de.presti.ree6.webinterface.utils.RandomUtil;
import de.presti.ree6.webinterface.utils.Setting;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;

/**
 * Controller for the Frontend to manage what the user sees.
 */
@SuppressWarnings("DuplicatedCode")
@Controller
public class FrontendController {

    // Paths to Thymeleaf Templates.
    private static final String MAIN_PATH = "main/index", ERROR_PATH = "error/index", MODERATION_PATH = "panel/moderation/index", SOCIAL_PATH = "panel/social/index", LOGGING_PATH = "panel/logging/index";

    /**
     * A Get Mapper for the Main Page.
     *
     * @return {@link String} for Thyme to the HTML Page.
     */
    @GetMapping("/")
    public String main() {
        return MAIN_PATH;
    }

    //region Discord.

    /**
     * A Get Mapper for generation of a Discord OAuth2 Session
     *
     * @return {@link ModelAndView} with the redirect data.
     */
    @GetMapping("/discord/auth")
    public ModelAndView startDiscordAuth() {
        return new ModelAndView("redirect:" + Server.getInstance().getOAuth2Client().generateAuthorizationURL((BotWorker.getVersion() != BotVersion.DEV ? "https://cp.ree6.de" : "http://localhost:8888") + "/discord/auth/callback", Scope.GUILDS, Scope.IDENTIFY, Scope.GUILDS_JOIN));
    }

    /**
     * The Request Mapper for the Discord Auth callback.
     *
     * @param httpServletResponse the HTTP Response.
     * @param code                the OAuth2 Code from Discord.
     * @param state               the local State of the OAuth2 Session.
     * @return {@link ModelAndView} with the redirect data.
     */
    @GetMapping(value = "/discord/auth/callback")
    public ModelAndView discordLogin(HttpServletResponse httpServletResponse, @RequestParam String code, @RequestParam String state) {
        Session session = null;

        // Generate a secure Base64 String for the Identifier.
        String identifier = RandomUtil.getRandomBase64String();

        try {
            // Try creating a Session.
            session = Server.getInstance().getOAuth2Client().startSession(code, state, identifier, Scope.GUILDS, Scope.IDENTIFY, Scope.GUILDS_JOIN).complete();
        } catch (Exception ignore) {
        }

        // If the given data was valid and a Session has been created redirect to the panel Site. If not redirect to error.
        if (session != null) {

            Cookie cookie = new Cookie("identifier", Base64.getEncoder().encodeToString(identifier.getBytes(StandardCharsets.UTF_8)));

            cookie.setHttpOnly(true);
            cookie.setMaxAge(7 * 24 * 60 * 60);
            if (BotWorker.getVersion() != BotVersion.DEV) cookie.setSecure(true);
            cookie.setPath("/");

            httpServletResponse.addCookie(cookie);

            try {
                Server.getInstance().getOAuth2Client().getUser(session).queue(oAuth2User -> {
                    if (oAuth2User != null) {
                        Guild guild = BotWorker.getShardManager().getGuildById(805149057004732457L);
                        if (guild != null) {
                            Server.getInstance().getOAuth2Client().joinGuild(oAuth2User, guild).queue();
                        }
                    }
                });
            } catch (Exception ignore) {
            }

            return new ModelAndView("redirect:" + (BotWorker.getVersion() != BotVersion.DEV ? "https://cp.ree6.de" : "http://localhost:8888") + "/panel");
        } else {
            return new ModelAndView("redirect:" + (BotWorker.getVersion() != BotVersion.DEV ? "https://cp.ree6.de" : "http://localhost:8888") + "/error");
        }
    }

    //endregion

    //region Leaderboard.

    /**
     * The Request Mapper for the Guild Leaderboard.
     *
     * @param guildId the ID of the Guild.
     * @param model   the ViewModel.
     * @return {@link ModelAndView} with the redirect data.
     */
    @GetMapping(value = "/leaderboard/chat")
    public String getLeaderboardChat(HttpServletResponse httpServletResponse, @CookieValue(name = "identifier", defaultValue = "-1") String id, @RequestParam String guildId, Model model) {

        // Check and decode the Identifier saved in the Cookies.
        id = getIdentifier(id);

        if (checkIdentifier(id)) {
            model.addAttribute("title", "Insufficient Permissions");
            model.addAttribute("message", new String[] { "Please check if you are logged in!" });
            deleteSessionCookie(httpServletResponse);
            return ERROR_PATH;
        }

        try {
            // Try retrieving the Session from the Identifier.
            Session session = Server.getInstance().getOAuth2Client().getSessionController().getSession(id);

            if (session == null) {
                model.addAttribute("title", "Insufficient Permissions");
                model.addAttribute("message", new String[] { "Please check if you are logged in!" });
                deleteSessionCookie(httpServletResponse);
                return ERROR_PATH;
            }

            // Try retrieving the User from the Session.
            OAuth2User oAuth2User = Server.getInstance().getOAuth2Client().getUser(session).complete();

            // Retrieve the Guild by its giving ID.
            Guild guild = BotWorker.getShardManager().getGuildById(guildId);

            // If the Guild couldn't be loaded redirect to Error page.
            if (guild == null) {
                model.addAttribute("title", "Invalid Guild");
                model.addAttribute("message", new String[] { "The requested Guild is Invalid or not recognized!" });
                return ERROR_PATH;
            }

            Member member = guild.retrieveMemberById(oAuth2User.getId()).complete();

            if (member == null) {
                model.addAttribute("title", "Insufficient Permissions");
                model.addAttribute("message", new String[] { "You are not part of this Guild!" });
                return ERROR_PATH;
            }

            model.addAttribute("guild", guild);
        } catch (Exception exception) {
            model.addAttribute("title", "Unexpected Error, please Report!");
            model.addAttribute("message", new String[] { "We received an unexpected error, please report this to the developer! (" + exception.getMessage() + ")" });
            return ERROR_PATH;
        }

        int i = 1;
        for (UserLevel userLevel : Server.getInstance().getSqlConnector().getSqlWorker().getTopChat(guildId, 5)) {
            try {
                if (BotWorker.getShardManager().getUserById(userLevel.getUserId()) != null) {
                    userLevel.setUser(BotWorker.getShardManager().getUserById(userLevel.getUserId()));
                } else {
                    userLevel.setUser(BotWorker.getShardManager().retrieveUserById(userLevel.getUserId()).complete());
                }
            } catch (Exception ignore) {
                userLevel.setExperience(0);
                Server.getInstance().getSqlConnector().getSqlWorker().addChatLevelData(guildId, userLevel);
            }

            model.addAttribute("user" + i, userLevel);
            i++;
        }

        return "leaderboard/index";
    }

    /**
     * The Request Mapper for the Guild Leaderboard.
     *
     * @param guildId the ID of the Guild.
     * @param model   the ViewModel.
     * @return {@link ModelAndView} with the redirect data.
     */
    @GetMapping(value = "/leaderboard/voice")
    public String getLeaderboardVoice(HttpServletResponse httpServletResponse, @CookieValue(name = "identifier", defaultValue = "-1") String id, @RequestParam String guildId, Model model) {

        // Check and decode the Identifier saved in the Cookies.
        id = getIdentifier(id);

        if (checkIdentifier(id)) {
            model.addAttribute("title", "Insufficient Permissions");
            model.addAttribute("message", new String[] { "Please check if you are logged in!" });
            deleteSessionCookie(httpServletResponse);
            return ERROR_PATH;
        }

        try {
            // Try retrieving the Session from the Identifier.
            Session session = Server.getInstance().getOAuth2Client().getSessionController().getSession(id);

            if (session == null) {
                model.addAttribute("title", "Insufficient Permissions");
                model.addAttribute("message", new String[] { "Please check if you are logged in!" });
                deleteSessionCookie(httpServletResponse);
                return ERROR_PATH;
            }

            // Try retrieving the User from the Session.
            OAuth2User oAuth2User = Server.getInstance().getOAuth2Client().getUser(session).complete();

            // Retrieve the Guild by its giving ID.
            Guild guild = BotWorker.getShardManager().getGuildById(guildId);

            // If the Guild couldn't be loaded redirect to Error page.
            if (guild == null) {
                model.addAttribute("title", "Invalid Guild");
                model.addAttribute("message", new String[] { "The requested Guild is Invalid or not recognized!" });
                return ERROR_PATH;
            }

            Member member = guild.retrieveMemberById(oAuth2User.getId()).complete();

            if (member == null) {
                model.addAttribute("title", "Insufficient Permissions");
                model.addAttribute("message", new String[] { "You are not part of this Guild!" });
                return ERROR_PATH;
            }

            model.addAttribute("guild", guild);
        } catch (Exception exception) {
            model.addAttribute("title", "Unexpected Error, please Report!");
            model.addAttribute("message", new String[] { "We received an unexpected error, please report this to the developer! (" + exception.getMessage() + ")" });
            return ERROR_PATH;
        }

        int i = 1;
        for (UserLevel userLevel : Server.getInstance().getSqlConnector().getSqlWorker().getTopVoice(guildId, 5)) {
            try {
                if (BotWorker.getShardManager().getUserById(userLevel.getUserId()) != null) {
                    userLevel.setUser(BotWorker.getShardManager().getUserById(userLevel.getUserId()));
                } else {
                    userLevel.setUser(BotWorker.getShardManager().retrieveUserById(userLevel.getUserId()).complete());
                }
            } catch (Exception ignore) {
                userLevel.setExperience(0);
                Server.getInstance().getSqlConnector().getSqlWorker().addChatLevelData(guildId, userLevel);
            }

            model.addAttribute("user" + i, userLevel);
            i++;
        }

        return "leaderboard/index";
    }

    //endregion

    //region Panel

    /**
     * Request Mapper for the Server selection Panel.
     *
     * @param id    the Session Identifier.
     * @param model the ViewModel.
     * @return {@link String} for Thyme to the HTML Page.
     */
    @GetMapping(path = "/panel")
    public String openPanel(HttpServletResponse httpServletResponse, @CookieValue(name = "identifier", defaultValue = "-1") String id, Model model) {

        // Check and decode the Identifier saved in the Cookies.
        id = getIdentifier(id);

        if (checkIdentifier(id)) {
            model.addAttribute("IsError", true);
            model.addAttribute("error", "Couldn't load Session!");
            deleteSessionCookie(httpServletResponse);
            return MAIN_PATH;
        }

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
            if (session == null) {
                deleteSessionCookie(httpServletResponse);
                return MAIN_PATH;
            }

            // If the Session isn't null give the User a Notification that his Guilds couldn't be loaded.
            model.addAttribute("IsError", true);
            model.addAttribute("error", "Couldn't load Session!");
        }

        // Return Panel Page.
        return "panel/index";
    }

    //endregion

    //region Server

    /**
     * Request Mapper for the Server Panel Page.
     *
     * @param httpServletResponse the HTTP Response.
     * @param id                  the Session Identifier.
     * @param guildID             the ID of the selected Guild.
     * @param model               the ViewModel.
     * @return {@link String} for Thyme to the HTML Page.
     */
    @GetMapping(path = "/panel/server")
    public String openServerPanel(HttpServletResponse httpServletResponse, @CookieValue(name = "identifier", defaultValue = "-1") String id, @RequestParam String guildID, Model model) {

        // Check and decode the Identifier saved in the Cookies.
        id = getIdentifier(id);

        if (checkIdentifier(id)) {
            model.addAttribute("IsError", true);
            model.addAttribute("error", "Couldn't load Session!");
            deleteSessionCookie(httpServletResponse);
            return "main/index";
        }

        // Set default Data and If there was an error return to the Error Page.
        if (setDefaultInformation(model, httpServletResponse, guildID, id)) return ERROR_PATH;

        // Retrieve every Role and Channel of the Guild and set them as Attribute.
        model.addAttribute("invites", Server.getInstance().getSqlConnector().getSqlWorker().getInvites(guildID));

        StringBuilder commandStats = new StringBuilder();

        for (String[] entrySet : Server.getInstance().getSqlConnector().getSqlWorker().getStats(guildID)) {
            commandStats.append(entrySet[0]).append(" - ").append(entrySet[1]).append(", ");
        }

        if (commandStats.length() > 0) {
            commandStats = new StringBuilder(commandStats.substring(0, commandStats.length() - 2));
        } else {
            commandStats = new StringBuilder("None.");
        }

        model.addAttribute("commandstats", commandStats.toString());

        // Return to the Server Panel Page.
        return "panel/server/index";
    }

    //endregion

    //region Moderation

    /**
     * Request Mapper for the Moderation Panel Page.
     *
     * @param httpServletResponse the HTTP Response.
     * @param id                  the Session Identifier.
     * @param guildID             the ID of the selected Guild.
     * @param model               the ViewModel.
     * @return {@link String} for Thyme to the HTML Page.
     */
    @GetMapping(path = "/panel/moderation")
    public String openPanelModeration(HttpServletResponse httpServletResponse, @CookieValue(name = "identifier", defaultValue = "-1") String id, @RequestParam String guildID, Model model) {

        // Check and decode the Identifier saved in the Cookies.
        id = getIdentifier(id);

        if (checkIdentifier(id)) {
            model.addAttribute("IsError", true);
            model.addAttribute("error", "Couldn't load Session!");
            deleteSessionCookie(httpServletResponse);
            return "main/index";
        }

        // Set default Data and If there was an error return to the Error Page.
        if (setDefaultInformation(model, httpServletResponse, guildID, id)) return ERROR_PATH;

        // Get the Guild from the Model.
        Guild guild = null;

        if (model.getAttribute("guild") instanceof Guild guild1) guild = guild1;

        // If null return to Error page.
        if (guild == null) return ERROR_PATH;

        // Retrieve every Role and Channel of the Guild and set them as Attribute.
        model.addAttribute("roles", guild.getRoles());
        model.addAttribute("channels", guild.getTextChannels());
        model.addAttribute("commands", Server.getInstance().getSqlConnector().getSqlWorker().getAllSettings(guildID).stream().filter(setting -> setting.getName().startsWith("com")).toList());
        model.addAttribute("prefixSetting", Server.getInstance().getSqlConnector().getSqlWorker().getSetting(guildID, "chatprefix"));
        model.addAttribute("words", Server.getInstance().getSqlConnector().getSqlWorker().getChatProtectorWords(guildID));

        List<Role> roles = new ArrayList<>();

        for (String ids : Server.getInstance().getSqlConnector().getSqlWorker().getAutoRoles(guild.getId())) {
            try {
                roles.add(guild.getRoleById(ids));
            } catch (Exception ignore) {
                Server.getInstance().getSqlConnector().getSqlWorker().removeAutoRole(guild.getId(), ids);
            }
        }

        model.addAttribute("autoroles", roles);

        Role muteRole = null;

        try {
            muteRole = guild.getRoleById(Server.getInstance().getSqlConnector().getSqlWorker().getMuteRole(guildID));
        } catch (Exception ignore) {
            Server.getInstance().getSqlConnector().getSqlWorker().removeMuteRole(guildID);
        }

        model.addAttribute("muterole", muteRole);

        // Return to the Moderation Panel Page.
        return MODERATION_PATH;
    }

    /**
     * Request Mapper for the Moderation Role Change Panel.
     *
     * @param httpServletResponse the HTTP Response.
     * @param roleChangeForm      as the Form which contains the needed data.
     * @param model               the ViewModel.
     * @return {@link String} for Thyme to the HTML Page.
     */
    @PostMapping(path = "/panel/moderation/role")
    public String openPanelModeration(HttpServletResponse httpServletResponse, @ModelAttribute(name = "roleChangeForm") RoleChangeForm roleChangeForm, Model model) {

        // Set default Data and If there was an error return to the Error Page.
        if (setDefaultInformation(model, httpServletResponse, roleChangeForm.getGuild(), roleChangeForm.getIdentifier()))
            return ERROR_PATH;

        // Get the Guild from the Model.
        Guild guild = null;

        if (model.getAttribute("guild") instanceof Guild guild1) guild = guild1;

        // If null return to Error page.
        if (guild == null) return ERROR_PATH;

        // Change the role Data.
        if (roleChangeForm.getType().equalsIgnoreCase("muterole")) {
            Server.getInstance().getSqlConnector().getSqlWorker().setMuteRole(roleChangeForm.getGuild(), roleChangeForm.getRole());
        }

        // Retrieve every Role and Channel of the Guild and set them as Attribute.
        model.addAttribute("roles", guild.getRoles());
        model.addAttribute("channels", guild.getTextChannels());
        model.addAttribute("commands", Server.getInstance().getSqlConnector().getSqlWorker().getAllSettings(guild.getId()).stream().filter(setting -> setting.getName().startsWith("com")).toList());
        model.addAttribute("prefixSetting", Server.getInstance().getSqlConnector().getSqlWorker().getSetting(guild.getId(), "chatprefix"));
        model.addAttribute("words", Server.getInstance().getSqlConnector().getSqlWorker().getChatProtectorWords(guild.getId()));

        List<Role> roles = new ArrayList<>();

        for (String ids : Server.getInstance().getSqlConnector().getSqlWorker().getAutoRoles(guild.getId())) {
            roles.add(guild.getRoleById(ids));
        }

        model.addAttribute("autoroles", roles);

        Role muteRole = null;

        try {
            muteRole = guild.getRoleById(Server.getInstance().getSqlConnector().getSqlWorker().getMuteRole(guild.getId()));
        } catch (Exception ignore) {
            Server.getInstance().getSqlConnector().getSqlWorker().removeMuteRole(guild.getId());
        }

        model.addAttribute("muterole", muteRole);

        return MODERATION_PATH;
    }

    /**
     * Request Mapper for the Moderation Settings Change Panel.
     *
     * @param httpServletResponse the HTTP Response.
     * @param settingChangeForm   as the Form which contains the needed data.
     * @param model               the ViewModel.
     * @return {@link String} for Thyme to the HTML Page.
     */
    @PostMapping(path = "/panel/moderation/settings")
    public String openPanelModeration(HttpServletResponse httpServletResponse, @ModelAttribute(name = "settingChangeForm") SettingChangeForm settingChangeForm, Model model) {

        // Set default Data and If there was an error return to the Error Page.
        if (setDefaultInformation(model, httpServletResponse, settingChangeForm.getGuild(), settingChangeForm.getIdentifier()))
            return ERROR_PATH;

        // Get the Guild from the Model.
        Guild guild = null;

        if (model.getAttribute("guild") instanceof Guild guild1) guild = guild1;

        // If null return to Error page.
        if (guild == null) return ERROR_PATH;

        // Change the Setting Data.
        if (!settingChangeForm.getSetting().getName().equalsIgnoreCase("addBadWord") && !settingChangeForm.getSetting().getName().equalsIgnoreCase("removeBadWord") && !settingChangeForm.getSetting().getName().equalsIgnoreCase("addAutoRole") && !settingChangeForm.getSetting().getName().equalsIgnoreCase("removeAutoRole")) {
            Server.getInstance().getSqlConnector().getSqlWorker().setSetting(settingChangeForm.getGuild(), settingChangeForm.getSetting());
        } else {
            switch (settingChangeForm.getSetting().getName()) {
                case "addBadWord" -> Server.getInstance().getSqlConnector().getSqlWorker().addChatProtectorWord(settingChangeForm.getGuild(), settingChangeForm.getSetting().getStringValue());
                case "removeBadWord" -> Server.getInstance().getSqlConnector().getSqlWorker().removeChatProtectorWord(settingChangeForm.getGuild(), settingChangeForm.getSetting().getStringValue());
                case "addAutoRole" -> Server.getInstance().getSqlConnector().getSqlWorker().addAutoRole(settingChangeForm.getGuild(), settingChangeForm.getSetting().getStringValue());
                case "removeAutoRole" -> Server.getInstance().getSqlConnector().getSqlWorker().removeAutoRole(settingChangeForm.getGuild(), settingChangeForm.getSetting().getStringValue());
            }
        }

        // Retrieve every Role and Channel of the Guild and set them as Attribute.
        model.addAttribute("roles", guild.getRoles());
        model.addAttribute("channels", guild.getTextChannels());
        model.addAttribute("commands", Server.getInstance().getSqlConnector().getSqlWorker().getAllSettings(guild.getId()).stream().filter(setting -> setting.getName().startsWith("com")).toList());
        model.addAttribute("prefixSetting", Server.getInstance().getSqlConnector().getSqlWorker().getSetting(guild.getId(), "chatprefix"));
        model.addAttribute("words", Server.getInstance().getSqlConnector().getSqlWorker().getChatProtectorWords(guild.getId()));

        List<Role> roles = new ArrayList<>();

        for (String ids : Server.getInstance().getSqlConnector().getSqlWorker().getAutoRoles(guild.getId())) {
            roles.add(guild.getRoleById(ids));
        }

        model.addAttribute("autoroles", roles);

        Role muteRole = null;

        try {
            muteRole = guild.getRoleById(Server.getInstance().getSqlConnector().getSqlWorker().getMuteRole(guild.getId()));
        } catch (Exception ignore) {
            Server.getInstance().getSqlConnector().getSqlWorker().removeMuteRole(guild.getId());
        }

        model.addAttribute("muterole", muteRole);

        return MODERATION_PATH;
    }

    //endregion

    //region Social

    /**
     * Request Mapper for the Social Panel Page.
     *
     * @param httpServletResponse the HTTP Response.
     * @param id                  the Session Identifier.
     * @param guildID             the ID of the selected Guild.
     * @param model               the ViewModel.
     * @return {@link String} for Thyme to the HTML Page.
     */
    @GetMapping(path = "/panel/social")
    public String openPanelSocial(HttpServletResponse httpServletResponse, @CookieValue(name = "identifier", defaultValue = "-1") String id, @RequestParam String guildID, Model model) {

        // Check and decode the Identifier saved in the Cookies.
        id = getIdentifier(id);

        if (checkIdentifier(id)) {
            model.addAttribute("IsError", true);
            model.addAttribute("error", "Couldn't load Session!");
            deleteSessionCookie(httpServletResponse);
            return "main/index";
        }

        // Set default Data and If there was an error return to the Error Page.
        if (setDefaultInformation(model, httpServletResponse, guildID, id)) return ERROR_PATH;

        // Get the Guild from the Model.
        Guild guild = null;

        if (model.getAttribute("guild") instanceof Guild guild1) guild = guild1;

        // If null return to Error page.
        if (guild == null) return ERROR_PATH;

        // Retrieve every Role and Channel of the Guild and set them as Attribute.
        model.addAttribute("roles", guild.getRoles());
        model.addAttribute("channels", guild.getTextChannels());
        model.addAttribute("joinMessage", new Setting("joinMessage", Server.getInstance().getSqlConnector().getSqlWorker().getMessage(guildID)));

        // Return to the Social Panel Page.
        return SOCIAL_PATH;
    }

    /**
     * Request Mapper for the Social Channel Change Panel.
     *
     * @param httpServletResponse the HTTP Response.
     * @param channelChangeForm   as the Form which contains the needed data.
     * @param model               the ViewModel.
     * @return {@link String} for Thyme to the HTML Page.
     */
    @PostMapping(path = "/panel/social/channel")
    public String openPanelSocial(HttpServletResponse httpServletResponse, @ModelAttribute(name = "channelChangeForm") ChannelChangeForm channelChangeForm, Model model) {

        // Set default Data and If there was an error return to the Error Page.
        if (setDefaultInformation(model, httpServletResponse, channelChangeForm.getGuild(), channelChangeForm.getIdentifier()))
            return ERROR_PATH;

        // Get the Guild from the Model.
        Guild guild = null;

        if (model.getAttribute("guild") instanceof Guild guild1) guild = guild1;

        // If null return to Error page.
        if (guild == null) return ERROR_PATH;

        // Change the channel Data.
        // Check if null.
        if (guild.getTextChannelById(channelChangeForm.getChannel()) != null) {
            if (channelChangeForm.getType().equalsIgnoreCase("newsChannel")) {
                // Create new Webhook, If it has been created successfully add it to our Database.
                Guild finalGuild = guild;
                guild.getTextChannelById(channelChangeForm.getChannel()).createWebhook("Ree6-News").queue(webhook -> Server.getInstance().getSqlConnector().getSqlWorker().setNewsWebhook(finalGuild.getId(), webhook.getId(), webhook.getToken()));
            } else if (channelChangeForm.getType().equalsIgnoreCase("welcomeChannel")) {
                // Create new Webhook, If it has been created successfully add it to our Database.
                Guild finalGuild = guild;
                guild.getTextChannelById(channelChangeForm.getChannel()).createWebhook("Ree6-Welcome").queue(webhook -> Server.getInstance().getSqlConnector().getSqlWorker().setWelcomeWebhook(finalGuild.getId(), webhook.getId(), webhook.getToken()));
            }
        }

        // Retrieve every Role and Channel of the Guild and set them as Attribute.
        model.addAttribute("roles", guild.getRoles());
        model.addAttribute("channels", guild.getTextChannels());
        model.addAttribute("joinMessage", new Setting("joinMessage", Server.getInstance().getSqlConnector().getSqlWorker().getMessage(guild.getId())));

        return SOCIAL_PATH;
    }

    /**
     * Request Mapper for the Social Setting Change Panel.
     *
     * @param httpServletResponse the HTTP Response.
     * @param settingChangeForm   as the Form which contains the needed data.
     * @param model               the ViewModel.
     * @return {@link String} for Thyme to the HTML Page.
     */
    @PostMapping(path = "/panel/social/settings")
    public String openPanelSocial(HttpServletResponse httpServletResponse, @ModelAttribute(name = "settingChangeForm") SettingChangeForm settingChangeForm, Model model) {

        // Set default Data and If there was an error return to the Error Page.
        if (setDefaultInformation(model, httpServletResponse, settingChangeForm.getGuild(), settingChangeForm.getIdentifier()))
            return ERROR_PATH;

        // Get the Guild from the Model.
        Guild guild = null;

        if (model.getAttribute("guild") instanceof Guild guild1) guild = guild1;

        // If null return to Error page.
        if (guild == null) return ERROR_PATH;

        // Change the setting Data.
        if (!settingChangeForm.getSetting().getName().equalsIgnoreCase("joinMessage")) {
            Server.getInstance().getSqlConnector().getSqlWorker().setSetting(settingChangeForm.getGuild(), settingChangeForm.getSetting());
        } else {
            Server.getInstance().getSqlConnector().getSqlWorker().setMessage(settingChangeForm.getGuild(), settingChangeForm.getSetting().getStringValue());
        }

        // Retrieve every Role and Channel of the Guild and set them as Attribute.
        model.addAttribute("roles", guild.getRoles());
        model.addAttribute("channels", guild.getTextChannels());
        model.addAttribute("joinMessage", new Setting("joinMessage", Server.getInstance().getSqlConnector().getSqlWorker().getMessage(guild.getId())));

        return SOCIAL_PATH;
    }

    //endregion

    //region Logging

    /**
     * Request Mapper for the Logging Panel Page.
     *
     * @param httpServletResponse the HTTP Response.
     * @param id                  the Session Identifier.
     * @param guildID             the ID of the selected Guild.
     * @param model               the ViewModel.
     * @return {@link String} for Thyme to the HTML Page.
     */
    @GetMapping(path = "/panel/logging")
    public String openPanelLogging(HttpServletResponse httpServletResponse, @CookieValue(name = "identifier", defaultValue = "-1") String id, @RequestParam String guildID, Model model) {

        // Check and decode the Identifier saved in the Cookies.
        id = getIdentifier(id);

        if (checkIdentifier(id)) {
            model.addAttribute("IsError", true);
            model.addAttribute("error", "Couldn't load Session!");
            deleteSessionCookie(httpServletResponse);
            return "main/index";
        }

        // Set default Data and If there was an error return to the Error Page.
        if (setDefaultInformation(model, httpServletResponse, guildID, id)) return ERROR_PATH;

        // Get the Guild from the Model.
        Guild guild = null;

        if (model.getAttribute("guild") instanceof Guild guild1) guild = guild1;

        // If null return to Error page.
        if (guild == null) return ERROR_PATH;

        // Retrieve every Log Option and Channel of the Guild and set them as Attribute.
        model.addAttribute("logs", Server.getInstance().getSqlConnector().getSqlWorker().getAllSettings(guild.getId()).stream().filter(setting -> setting.getName().startsWith("log")).toList());
        model.addAttribute("channels", guild.getTextChannels());

        // Return to the Logging Panel Page.
        return LOGGING_PATH;
    }

    /**
     * Request Mapper for the Logging Channel Change Panel.
     *
     * @param httpServletResponse the HTTP Response.
     * @param channelChangeForm   as the Form which contains the needed data.
     * @param model               the ViewModel.
     * @return {@link String} for Thyme to the HTML Page.
     */
    @PostMapping(path = "/panel/logging/channel")
    public String openPanelLogging(HttpServletResponse httpServletResponse, @ModelAttribute(name = "channelChangeForm") ChannelChangeForm channelChangeForm, Model model) {

        // Set default Data and If there was an error return to the Error Page.
        if (setDefaultInformation(model, httpServletResponse, channelChangeForm.getGuild(), channelChangeForm.getIdentifier()))
            return ERROR_PATH;

        // Get the Guild from the Model.
        Guild guild = null;

        if (model.getAttribute("guild") instanceof Guild guild1) guild = guild1;

        // If null return to Error page.
        if (guild == null) return ERROR_PATH;

        // Change the channel Data.
        // Check if null.
        if (channelChangeForm.getType().equalsIgnoreCase("logChannel") && guild.getTextChannelById(channelChangeForm.getChannel()) != null) {
            // Create new Webhook, If it has been created successfully add it to our Database.
            Guild finalGuild = guild;
            guild.getTextChannelById(channelChangeForm.getChannel()).createWebhook("Ree6-Logs").queue(webhook -> Server.getInstance().getSqlConnector().getSqlWorker().setLogWebhook(finalGuild.getId(), webhook.getId(), webhook.getToken()));
        }

        // Retrieve every Log Option and Channel of the Guild and set them as Attribute.
        model.addAttribute("logs", Server.getInstance().getSqlConnector().getSqlWorker().getAllSettings(guild.getId()).stream().filter(setting -> setting.getName().startsWith("log")).toList());
        model.addAttribute("channels", guild.getTextChannels());

        // Return to the Logging Panel Page.
        return LOGGING_PATH;
    }

    /**
     * Request Mapper for the Logging Setting Change Panel.
     *
     * @param httpServletResponse the HTTP Response.
     * @param settingChangeForm   as the Form which contains the needed data.
     * @param model               the ViewModel.
     * @return {@link String} for Thyme to the HTML Page.
     */
    @PostMapping(path = "/panel/logging/settings")
    public String openPanelLogging(HttpServletResponse httpServletResponse, @ModelAttribute(name = "settingChangeForm") SettingChangeForm settingChangeForm, Model model) {

        // Set default Data and If there was an error return to the Error Page.
        if (setDefaultInformation(model, httpServletResponse, settingChangeForm.getGuild(), settingChangeForm.getIdentifier()))
            return ERROR_PATH;

        // Get the Guild from the Model.
        Guild guild = null;

        if (model.getAttribute("guild") instanceof Guild guild1) guild = guild1;

        // If null return to Error page.
        if (guild == null) return ERROR_PATH;

        // Change the setting Data.
        Server.getInstance().getSqlConnector().getSqlWorker().setSetting(settingChangeForm.getGuild(), settingChangeForm.getSetting());
        // Retrieve every Log Option and Channel of the Guild and set them as Attribute.
        model.addAttribute("logs", Server.getInstance().getSqlConnector().getSqlWorker().getAllSettings(guild.getId()).stream().filter(setting -> setting.getName().startsWith("log")).toList());
        model.addAttribute("channels", guild.getTextChannels());

        // Return to the Logging Panel Page.
        return LOGGING_PATH;
    }

    //endregion

    //region Utility

    /**
     * Set default information such as the Session Identifier and {@link Guild} Entity.
     *
     * @param model               the View Model.
     * @param httpServletResponse the HTTP Response.
     * @param guildId             the ID of the Guild
     * @param identifier          the Session Identifier.
     * @return true, if there was an error | false, if everything was alright.
     */
    public boolean setDefaultInformation(Model model, HttpServletResponse httpServletResponse, String guildId, String identifier) {
        try {
            // Try retrieving the Session from the Identifier.
            Session session = Server.getInstance().getOAuth2Client().getSessionController().getSession(identifier);

            if (session == null) {
                deleteSessionCookie(httpServletResponse);
                return true;
            }

            // Try retrieving the User from the Session.
            OAuth2User oAuth2User = Server.getInstance().getOAuth2Client().getUser(session).complete();

            // Retrieve the Guild by its giving ID.
            Guild guild = BotWorker.getShardManager().getGuildById(guildId);

            // If the Guild couldn't be loaded redirect to Error page.
            if (guild == null) return true;

            Member member = guild.retrieveMemberById(oAuth2User.getId()).complete();

            if (member != null && member.hasPermission(Permission.ADMINISTRATOR)) {
                // Set the Guild.
                model.addAttribute("guild", guild);

                // Set the Identifier.
                model.addAttribute("identifier", identifier);
            } else {
                return true;
            }

            return false;
        } catch (Exception ignore) {
        }

        return true;
    }

    /**
     * Delete a Session Cookie that has been set.
     *
     * @param httpServletResponse the HTTP Response.
     */
    public void deleteSessionCookie(HttpServletResponse httpServletResponse) {
        Cookie cookie = new Cookie("identifier", null);

        cookie.setHttpOnly(true);
        cookie.setMaxAge(0);
        if (BotWorker.getVersion() != BotVersion.DEV) cookie.setSecure(true);
        cookie.setPath("/");

        httpServletResponse.addCookie(cookie);
    }

    /**
     * Get the Identifier out of the Cookie-Value.
     *
     * @param identifier the encoded Identifier.
     * @return the decoded Identifier.
     */
    public String getIdentifier(String identifier) {
        try {
            identifier = new String(Base64.getDecoder().decode(identifier));
            return identifier;
        } catch (Exception ignored) {}

        return null;
    }

    /**
     * Check if a String is a valid identifier.
     *
     * @param identifier the "identifier".
     * @return true, if it is a valid identifier | false, if not.
     */
    public boolean checkIdentifier(String identifier) {
        return identifier == null || identifier.equals("-1");
    }

    //endregion
}
