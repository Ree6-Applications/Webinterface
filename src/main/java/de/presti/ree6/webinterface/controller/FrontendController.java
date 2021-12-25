package de.presti.ree6.webinterface.controller;

import com.jagrosh.jdautilities.oauth2.Scope;
import com.jagrosh.jdautilities.oauth2.entities.OAuth2Guild;
import com.jagrosh.jdautilities.oauth2.session.Session;
import de.presti.ree6.webinterface.Server;
import de.presti.ree6.webinterface.bot.BotInfo;
import de.presti.ree6.webinterface.controller.forms.ChannelChangeForm;
import de.presti.ree6.webinterface.controller.forms.RoleChangeForm;
import de.presti.ree6.webinterface.controller.forms.SettingChangeForm;
import de.presti.ree6.webinterface.level.UserLevel;
import de.presti.ree6.webinterface.utils.RandomUtil;
import de.presti.ree6.webinterface.utils.Setting;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Controller for the Frontend to manage what the user sees.
 */
@SuppressWarnings("DuplicatedCode")
@Controller
public class FrontendController {

    // Paths to Thymeleaf Templates.
    private static final String MAIN_PATH = "main/index",
            ERROR_PATH = "error/index",
            MODERATION_PATH = "panel/moderation/index",
            SOCIAL_PATH = "panel/social/index",
            LOGGING_PATH = "panel/logging/index";

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
        return new ModelAndView("redirect:" + Server.getInstance().getOAuth2Client().generateAuthorizationURL("http://cp.ree6.de/discord/auth/callback", Scope.GUILDS, Scope.IDENTIFY, Scope.GUILDS_JOIN));
    }

    /**
     * The Request Mapper for the Discord Auth callback.
     *
     * @param code  the OAuth2 Code from Discord.
     * @param state the local State of the OAuth2 Session.
     * @return {@link ModelAndView} with the redirect data.
     */
    @GetMapping(value = "/discord/auth/callback")
    public ModelAndView discordLogin(@RequestParam String code, @RequestParam String state) {
        Session session = null;

        // Generate a secure Base64 String for the Identifier.
        String identifier = RandomUtil.getRandomBase64String();

        try {
            // Try creating a Session.
            session = Server.getInstance().getOAuth2Client().startSession(code, state, identifier, Scope.GUILDS, Scope.IDENTIFY, Scope.GUILDS_JOIN).complete();
        } catch (Exception ignore) {
        }

        // If the given data was valid and a Session has been created redirect to the panel Site. If not redirect to error.
        if (session != null) return new ModelAndView("redirect:http://cp.ree6.de/panel?id=" + identifier);
        else return new ModelAndView("redirect:http://cp.ree6.de/error");
    }

    //endregion

    //region Leaderboard.

    /**
     * The Request Mapper for the Guild Leaderboard.
     *
     * @param guildId  the ID of the Guild.
     * @param model the ViewModel.
     * @return {@link ModelAndView} with the redirect data.
     */
    @GetMapping(value = "/leaderboard/chat")
    public String getLeaderboardChat(@RequestParam String guildId, Model model) {

        Guild guild = BotInfo.botInstance.getGuildById(guildId);

        if(guild != null) model.addAttribute("guild", guild);

        int i = 1;
        for (String userIds : Server.getInstance().getSqlConnector().getSqlWorker().getTopChat(guildId, 5)) {

            UserLevel userLevel = new UserLevel(userIds, Server.getInstance().getSqlConnector().getSqlWorker().getChatXP(guildId, userIds));

            model.addAttribute("user" + i, userLevel);
        }

        return "leaderboard/index";
    }

    /**
     * The Request Mapper for the Guild Leaderboard.
     *
     * @param guildId  the ID of the Guild.
     * @param model the ViewModel.
     * @return {@link ModelAndView} with the redirect data.
     */
    @GetMapping(value = "/leaderboard/voice")
    public String getLeaderboardVoice(@RequestParam String guildId, Model model) {

        Guild guild = BotInfo.botInstance.getGuildById(guildId);

        if(guild != null) model.addAttribute("guild", guild);

        int i = 1;
        for (String userIds : Server.getInstance().getSqlConnector().getSqlWorker().getTopVoice(guildId, 5)) {

            UserLevel userLevel = new UserLevel(userIds, Server.getInstance().getSqlConnector().getSqlWorker().getVoiceXP(guildId, userIds));

            model.addAttribute("user" + i, userLevel);
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
            if (session == null) return MAIN_PATH;

            // If the Session isn't null give the User a Notification that his Guilds couldn't be loaded.
            model.addAttribute("IsError", true);
            model.addAttribute("error", "Couldn't load Guilds!");
        }

        // Return Panel Page.
        return "panel/index";
    }

    //endregion

    //region Server

    /**
     * Request Mapper for the Server Panel Page.
     *
     * @param id      the Session Identifier.
     * @param guildID the ID of the selected Guild.
     * @param model   the ViewModel.
     * @return {@link String} for Thyme to the HTML Page.
     */
    @GetMapping(path = "/panel/server")
    public String openServerPanel(@RequestParam String id, @RequestParam String guildID, Model model) {

        Session session = null;

        try {
            // Try retrieving the Session from the Identifier.
            session = Server.getInstance().getOAuth2Client().getSessionController().getSession(id);

            // Try retrieving the Guild of the OAuth2 User by its ID.
            List<OAuth2Guild> guildList = Server.getInstance().getOAuth2Client().getGuilds(session).complete();
            guildList.removeIf(guild -> !guild.getId().equalsIgnoreCase(guildID) || !guild.hasPermission(Permission.ADMINISTRATOR));

            // If the given Guild ID couldn't be found in his Guild list redirect him to the Error page.
            if (guildList.isEmpty()) return ERROR_PATH;

            // Retrieve the Guild by its giving ID.
            Guild guild = BotInfo.botInstance.getGuildById(guildID);

            // If the Guild couldn't be loaded redirect to Error page.
            if (guild == null) return ERROR_PATH;

            // Set the Identifier.
            model.addAttribute("identifier", id);

            // Check if there is a Guild in the Guild list else send to error Page.
            if (guildList.stream().findFirst().isEmpty()) {
                model.addAttribute("IsError", true);
                model.addAttribute("error", "Couldn't load Guild Information! ");

                // Return to error page.
                return ERROR_PATH;
            }

            // If a Guild has been found set it as Attribute.
            model.addAttribute("guild", guildList.stream().findFirst().get());

            // Retrieve every Role and Channel of the Guild and set them as Attribute.
            model.addAttribute("invites", Server.getInstance().getSqlConnector().getSqlWorker().getInvites(guild.getId()));

            StringBuilder commandStats = new StringBuilder();

            for (Map.Entry<String, Long> entrySet : Server.getInstance().getSqlConnector().getSqlWorker().getStats(guildID).entrySet()) {
                commandStats.append(entrySet.getKey()).append(" - ").append(entrySet.getValue()).append(", ");
            }

            if (commandStats.length() > 0) {
                commandStats = new StringBuilder(commandStats.substring(0, commandStats.length() - 2));
            } else {
                commandStats = new StringBuilder("None.");
            }

            model.addAttribute("commandstats", commandStats.toString());
        } catch (Exception e) {
            // If the Session is null just return to the default Page.
            if (session == null) return MAIN_PATH;

            // If the Session isn't null give the User a Notification that the Guild couldn't be loaded.
            model.addAttribute("IsError", true);
            model.addAttribute("error", "Couldn't load Guild Information! ");
        }

        // Return to the Server Panel Page.
        return "panel/server/index";
    }

    //endregion

    //region Moderation

    /**
     * Request Mapper for the Moderation Panel Page.
     *
     * @param id      the Session Identifier.
     * @param guildID the ID of the selected Guild.
     * @param model   the ViewModel.
     * @return {@link String} for Thyme to the HTML Page.
     */
    @GetMapping(path = "/panel/moderation")
    public String openPanelModeration(@RequestParam String id, @RequestParam String guildID, Model model) {

        Session session = null;

        try {
            // Try retrieving the Session from the Identifier.
            session = Server.getInstance().getOAuth2Client().getSessionController().getSession(id);

            // Try retrieving the Guild of the OAuth2 User by its ID.
            List<OAuth2Guild> guildList = Server.getInstance().getOAuth2Client().getGuilds(session).complete();
            guildList.removeIf(guild -> !guild.getId().equalsIgnoreCase(guildID) || !guild.hasPermission(Permission.ADMINISTRATOR));

            // If the given Guild ID couldn't be found in his Guild list redirect him to the Error page.
            if (guildList.isEmpty()) return ERROR_PATH;

            // Retrieve the Guild by its giving ID.
            Guild guild = BotInfo.botInstance.getGuildById(guildID);

            // If the Guild couldn't be loaded redirect to Error page.
            if (guild == null) return ERROR_PATH;

            // Set the Identifier.
            model.addAttribute("identifier", id);

            // Check if there is a Guild in the Guild list else send to error Page.
            if (guildList.stream().findFirst().isEmpty()) {
                model.addAttribute("IsError", true);
                model.addAttribute("error", "Couldn't load Guild Information! ");

                // Return to error page.
                return ERROR_PATH;
            }

            // If a Guild has been found set it as Attribute.
            model.addAttribute("guild", guildList.stream().findFirst().get());

            // Retrieve every Role and Channel of the Guild and set them as Attribute.
            model.addAttribute("roles", guild.getRoles());
            model.addAttribute("channels", guild.getTextChannels());
            model.addAttribute("commands", Server.getInstance().getSqlConnector().getSqlWorker().getAllSettings(guild.getId()).stream()
                    .filter(setting -> setting.getName().startsWith("com")).collect(Collectors.toList()));
            model.addAttribute("prefixSetting", Server.getInstance().getSqlConnector().getSqlWorker().getSetting(guildID, "chatprefix"));
            model.addAttribute("words", Server.getInstance().getSqlConnector().getSqlWorker().getChatProtectorWords(guild.getId()));

            List<Role> roles = new ArrayList<>();

            for (String ids : Server.getInstance().getSqlConnector().getSqlWorker().getAutoRoles(guild.getId())) {
                roles.add(guild.getRoleById(ids));
            }

            model.addAttribute("autoroles", roles);
            model.addAttribute("muterole", guild.getRoleById(Server.getInstance().getSqlConnector().getSqlWorker().getMuteRole(guildID)));
        } catch (Exception e) {
            // If the Session is null just return to the default Page.
            if (session == null) return MAIN_PATH;

            // If the Session isn't null give the User a Notification that the Guild couldn't be loaded.
            model.addAttribute("IsError", true);
            model.addAttribute("error", "Couldn't load Guild Information! ");
        }

        // Return to the Moderation Panel Page.
        return MODERATION_PATH;
    }

    /**
     * Request Mapper for the Moderation Role Change Panel.
     *
     * @param roleChangeForm as the Form which contains the needed data.
     * @param model          the ViewModel.
     * @return {@link String} for Thyme to the HTML Page.
     */
    @PostMapping(path = "/panel/moderation/role")
    public String openPanelModeration(@ModelAttribute(name = "roleChangeForm") RoleChangeForm roleChangeForm, Model model) {

        Session session = null;

        try {
            // Try retrieving the Session from the Identifier.
            session = Server.getInstance().getOAuth2Client().getSessionController().getSession(roleChangeForm.getIdentifier());

            // Try retrieving the Guild of the OAuth2 User by its ID.
            List<OAuth2Guild> guildList = Server.getInstance().getOAuth2Client().getGuilds(session).complete();
            guildList.removeIf(guild -> !guild.getId().equalsIgnoreCase(roleChangeForm.getGuild()) || !guild.hasPermission(Permission.ADMINISTRATOR));

            // If the given Guild ID couldn't be found in his Guild list redirect him to the Error page.
            if (guildList.isEmpty()) return ERROR_PATH;

            // Retrieve the Guild by its giving ID.
            Guild guild = BotInfo.botInstance.getGuildById(roleChangeForm.getGuild());

            // If the Guild couldn't be loaded redirect to Error page.
            if (guild == null) return ERROR_PATH;

            // Change the role Data.
            if (roleChangeForm.getType().equalsIgnoreCase("muterole")) {
                Server.getInstance().getSqlConnector().getSqlWorker().setMuteRole(roleChangeForm.getGuild(), roleChangeForm.getRole());
            }

            // Set the Identifier.
            model.addAttribute("identifier", roleChangeForm.getIdentifier());

            // Check if there is a Guild in the Guild list else send to error Page.
            if (guildList.stream().findFirst().isEmpty()) {
                model.addAttribute("IsError", true);
                model.addAttribute("error", "Couldn't load Guild Information! ");

                // Return to error page.
                return ERROR_PATH;
            }

            // If a Guild has been found set it as Attribute.
            model.addAttribute("guild", guildList.stream().findFirst().get());

            // Retrieve every Role and Channel of the Guild and set them as Attribute.
            model.addAttribute("roles", guild.getRoles());
            model.addAttribute("channels", guild.getTextChannels());
            model.addAttribute("commands", Server.getInstance().getSqlConnector().getSqlWorker().getAllSettings(guild.getId()).stream()
                    .filter(setting -> setting.getName().startsWith("com")).collect(Collectors.toList()));
            model.addAttribute("prefixSetting", Server.getInstance().getSqlConnector().getSqlWorker().getSetting(guild.getId(), "chatprefix"));
            model.addAttribute("words", Server.getInstance().getSqlConnector().getSqlWorker().getChatProtectorWords(guild.getId()));

            List<Role> roles = new ArrayList<>();

            for (String ids : Server.getInstance().getSqlConnector().getSqlWorker().getAutoRoles(guild.getId())) {
                roles.add(guild.getRoleById(ids));
            }

            model.addAttribute("autoroles", roles);
            model.addAttribute("muterole", guild.getRoleById(Server.getInstance().getSqlConnector().getSqlWorker().getMuteRole(guild.getId())));
        } catch (Exception e) {
            // If the Session is null just return to the default Page.
            if (session == null) return MAIN_PATH;

            // If the Session isn't null give the User a Notification that the Guild couldn't be loaded.
            model.addAttribute("IsError", true);
            model.addAttribute("error", "Couldn't load Guild Information! ");
        }

        return MODERATION_PATH;
    }

    /**
     * Request Mapper for the Moderation Settings Change Panel.
     *
     * @param settingChangeForm as the Form which contains the needed data.
     * @param model             the ViewModel.
     * @return {@link String} for Thyme to the HTML Page.
     */
    @PostMapping(path = "/panel/moderation/settings")
    public String openPanelModeration(@ModelAttribute(name = "settingChangeForm") SettingChangeForm settingChangeForm, Model model) {
        Session session = null;

        try {
            // Try retrieving the Session from the Identifier.
            session = Server.getInstance().getOAuth2Client().getSessionController().getSession(settingChangeForm.getIdentifier());

            // Try retrieving the Guild of the OAuth2 User by its ID.
            List<OAuth2Guild> guildList = Server.getInstance().getOAuth2Client().getGuilds(session).complete();
            guildList.removeIf(guild -> !guild.getId().equalsIgnoreCase(settingChangeForm.getGuild()) || !guild.hasPermission(Permission.ADMINISTRATOR));

            // If the given Guild ID couldn't be found in his Guild list redirect him to the Error page.
            if (guildList.isEmpty()) return ERROR_PATH;

            // Retrieve the Guild by its giving ID.
            Guild guild = BotInfo.botInstance.getGuildById(settingChangeForm.getGuild());

            // If the Guild couldn't be loaded redirect to Error page.
            if (guild == null) return ERROR_PATH;

            // Change the Setting Data.
            if (!settingChangeForm.getSetting().getName().equalsIgnoreCase("addBadWord") &&
                    !settingChangeForm.getSetting().getName().equalsIgnoreCase("removeBadWord") &&
                    !settingChangeForm.getSetting().getName().equalsIgnoreCase("addAutoRole") &&
                    !settingChangeForm.getSetting().getName().equalsIgnoreCase("removeAutoRole")) {
                Server.getInstance().getSqlConnector().getSqlWorker().setSetting(settingChangeForm.getGuild(), settingChangeForm.getSetting());
            } else {
                switch (settingChangeForm.getSetting().getName()) {
                    case "addBadWord": {
                        Server.getInstance().getSqlConnector().getSqlWorker().addChatProtectorWord(settingChangeForm.getGuild(),
                                settingChangeForm.getSetting().getStringValue());
                        break;
                    }

                    case "removeBadWord": {
                        Server.getInstance().getSqlConnector().getSqlWorker().removeChatProtectorWord(settingChangeForm.getGuild(),
                                settingChangeForm.getSetting().getStringValue());
                        break;
                    }

                    case "addAutoRole": {
                        Server.getInstance().getSqlConnector().getSqlWorker().addAutoRole(settingChangeForm.getGuild(),
                                settingChangeForm.getSetting().getStringValue());
                        break;
                    }

                    case "removeAutoRole": {
                        Server.getInstance().getSqlConnector().getSqlWorker().removeAutoRole(settingChangeForm.getGuild(),
                                settingChangeForm.getSetting().getStringValue());
                        break;
                    }
                }
            }

            // Set the Identifier.
            model.addAttribute("identifier", settingChangeForm.getIdentifier());

            // Check if there is a Guild in the Guild list else send to error Page.
            if (guildList.stream().findFirst().isEmpty()) {
                model.addAttribute("IsError", true);
                model.addAttribute("error", "Couldn't load Guild Information! ");

                // Return to error page.
                return ERROR_PATH;
            }

            // If a Guild has been found set it as Attribute.
            model.addAttribute("guild", guildList.stream().findFirst().get());

            // Retrieve every Role and Channel of the Guild and set them as Attribute.
            model.addAttribute("roles", guild.getRoles());
            model.addAttribute("channels", guild.getTextChannels());
            model.addAttribute("commands", Server.getInstance().getSqlConnector().getSqlWorker().getAllSettings(guild.getId()).stream()
                    .filter(setting -> setting.getName().startsWith("com")).collect(Collectors.toList()));
            model.addAttribute("prefixSetting", Server.getInstance().getSqlConnector().getSqlWorker().getSetting(guild.getId(), "chatprefix"));
            model.addAttribute("words", Server.getInstance().getSqlConnector().getSqlWorker().getChatProtectorWords(guild.getId()));

            List<Role> roles = new ArrayList<>();

            for (String ids : Server.getInstance().getSqlConnector().getSqlWorker().getAutoRoles(guild.getId())) {
                roles.add(guild.getRoleById(ids));
            }

            model.addAttribute("autoroles", roles);
            model.addAttribute("muterole", guild.getRoleById(Server.getInstance().getSqlConnector().getSqlWorker().getMuteRole(guild.getId())));
        } catch (Exception e) {
            // If the Session is null just return to the default Page.
            if (session == null) return MAIN_PATH;

            // If the Session isn't null give the User a Notification that the Guild couldn't be loaded.
            model.addAttribute("IsError", true);
            model.addAttribute("error", "Couldn't load Guild Information! ");
        }

        return MODERATION_PATH;
    }

    //endregion

    //region Social

    /**
     * Request Mapper for the Social Panel Page.
     *
     * @param id      the Session Identifier.
     * @param guildID the ID of the selected Guild.
     * @param model   the ViewModel.
     * @return {@link String} for Thyme to the HTML Page.
     */
    @GetMapping(path = "/panel/social")
    public String openPanelSocial(@RequestParam String id, @RequestParam String guildID, Model model) {

        Session session = null;

        try {
            // Try retrieving the Session from the Identifier.
            session = Server.getInstance().getOAuth2Client().getSessionController().getSession(id);

            // Try retrieving the Guild of the OAuth2 User by its ID.
            List<OAuth2Guild> guildList = Server.getInstance().getOAuth2Client().getGuilds(session).complete();
            guildList.removeIf(guild -> !guild.getId().equalsIgnoreCase(guildID) || !guild.hasPermission(Permission.ADMINISTRATOR));

            // If the given Guild ID couldn't be found in his Guild list redirect him to the Error page.
            if (guildList.isEmpty()) return ERROR_PATH;

            // Retrieve the Guild by its giving ID.
            Guild guild = BotInfo.botInstance.getGuildById(guildID);

            // If the Guild couldn't be loaded redirect to Error page.
            if (guild == null) return ERROR_PATH;

            // Set the Identifier.
            model.addAttribute("identifier", id);

            // Check if there is a Guild in the Guild list else send to error Page.
            if (guildList.stream().findFirst().isEmpty()) {
                model.addAttribute("IsError", true);
                model.addAttribute("error", "Couldn't load Guild Information! ");

                // Return to error page.
                return ERROR_PATH;
            }

            // If a Guild has been found set it as Attribute.
            model.addAttribute("guild", guildList.stream().findFirst().get());

            // Retrieve every Role and Channel of the Guild and set them as Attribute.
            model.addAttribute("roles", guild.getRoles());
            model.addAttribute("channels", guild.getTextChannels());
            model.addAttribute("joinMessage", new Setting("joinMessage", Server.getInstance().getSqlConnector().getSqlWorker().getMessage(guildID)));
        } catch (Exception e) {
            // If the Session is null just return to the default Page.
            if (session == null) return MAIN_PATH;

            // If the Session isn't null give the User a Notification that the Guild couldn't be loaded.
            model.addAttribute("IsError", true);
            model.addAttribute("error", "Couldn't load Guild Information! ");
        }

        // Return to the Social Panel Page.
        return SOCIAL_PATH;
    }

    /**
     * Request Mapper for the Social Channel Change Panel.
     *
     * @param channelChangeForm as the Form which contains the needed data.
     * @param model             the ViewModel.
     * @return {@link String} for Thyme to the HTML Page.
     */
    @PostMapping(path = "/panel/social/channel")
    public String openPanelSocial(@ModelAttribute(name = "channelChangeForm") ChannelChangeForm channelChangeForm, Model model) {

        Session session = null;

        try {
            // Try retrieving the Session from the Identifier.
            session = Server.getInstance().getOAuth2Client().getSessionController().getSession(channelChangeForm.getIdentifier());

            // Try retrieving the Guild of the OAuth2 User by its ID.
            List<OAuth2Guild> guildList = Server.getInstance().getOAuth2Client().getGuilds(session).complete();
            guildList.removeIf(guild -> !guild.getId().equalsIgnoreCase(channelChangeForm.getGuild()) || !guild.hasPermission(Permission.ADMINISTRATOR));

            // If the given Guild ID couldn't be found in his Guild list redirect him to the Error page.
            if (guildList.isEmpty()) return ERROR_PATH;

            // Retrieve the Guild by its giving ID.
            Guild guild = BotInfo.botInstance.getGuildById(channelChangeForm.getGuild());

            // If the Guild couldn't be loaded redirect to Error page.
            if (guild == null) return ERROR_PATH;

            // Change the channel Data.
            // Check if null.
            if (guild.getTextChannelById(channelChangeForm.getChannel()) != null) {
                if (channelChangeForm.getType().equalsIgnoreCase("newsChannel")) {
                    // Create new Webhook, If it has been created successfully add it to our Database.
                    guild.getTextChannelById(channelChangeForm.getChannel()).createWebhook("Ree6-News").queue(webhook ->
                            Server.getInstance().getSqlConnector().getSqlWorker().setNewsWebhook(guild.getId(), webhook.getId(), webhook.getToken()));
                } else if (channelChangeForm.getType().equalsIgnoreCase("mateChannel")) {
                    // Create new Webhook, If it has been created successfully add it to our Database.
                    guild.getTextChannelById(channelChangeForm.getChannel()).createWebhook("Ree6-MateSearcher").queue(webhook ->
                            Server.getInstance().getSqlConnector().getSqlWorker().setRainbowWebhook(guild.getId(), webhook.getId(), webhook.getToken()));
                } else if (channelChangeForm.getType().equalsIgnoreCase("welcomeChannel")) {
                    // Create new Webhook, If it has been created successfully add it to our Database.
                    guild.getTextChannelById(channelChangeForm.getChannel()).createWebhook("Ree6-Welcome").queue(webhook ->
                            Server.getInstance().getSqlConnector().getSqlWorker().setWelcomeWebhook(guild.getId(), webhook.getId(), webhook.getToken()));
                }
            }

            // Set the Identifier.
            model.addAttribute("identifier", channelChangeForm.getIdentifier());

            // Check if there is a Guild in the Guild list else send to error Page.
            if (guildList.stream().findFirst().isEmpty()) {
                model.addAttribute("IsError", true);
                model.addAttribute("error", "Couldn't load Guild Information! ");

                // Return to error page.
                return ERROR_PATH;
            }

            // If a Guild has been found set it as Attribute.
            model.addAttribute("guild", guildList.stream().findFirst().get());

            // Retrieve every Role and Channel of the Guild and set them as Attribute.
            model.addAttribute("roles", guild.getRoles());
            model.addAttribute("channels", guild.getTextChannels());
            model.addAttribute("joinMessage", new Setting("joinMessage", Server.getInstance().getSqlConnector().getSqlWorker().getMessage(guild.getId())));
        } catch (Exception e) {
            // If the Session is null just return to the default Page.
            if (session == null) return MAIN_PATH;

            // If the Session isn't null give the User a Notification that the Guild couldn't be loaded.
            model.addAttribute("IsError", true);
            model.addAttribute("error", "Couldn't load Guild Information! ");
        }

        return SOCIAL_PATH;
    }

    /**
     * Request Mapper for the Social Setting Change Panel.
     *
     * @param settingChangeForm as the Form which contains the needed data.
     * @param model             the ViewModel.
     * @return {@link String} for Thyme to the HTML Page.
     */
    @PostMapping(path = "/panel/social/settings")
    public String openPanelSocial(@ModelAttribute(name = "settingChangeForm") SettingChangeForm settingChangeForm, Model model) {

        Session session = null;

        try {
            // Try retrieving the Session from the Identifier.
            session = Server.getInstance().getOAuth2Client().getSessionController().getSession(settingChangeForm.getIdentifier());

            // Try retrieving the Guild of the OAuth2 User by its ID.
            List<OAuth2Guild> guildList = Server.getInstance().getOAuth2Client().getGuilds(session).complete();
            guildList.removeIf(guild -> !guild.getId().equalsIgnoreCase(settingChangeForm.getGuild()) || !guild.hasPermission(Permission.ADMINISTRATOR));

            // If the given Guild ID couldn't be found in his Guild list redirect him to the Error page.
            if (guildList.isEmpty()) return ERROR_PATH;

            // Retrieve the Guild by its giving ID.
            Guild guild = BotInfo.botInstance.getGuildById(settingChangeForm.getGuild());

            // If the Guild couldn't be loaded redirect to Error page.
            if (guild == null) return ERROR_PATH;

            // Change the setting Data.
            if (!settingChangeForm.getSetting().getName().equalsIgnoreCase("joinMessage")) {
                Server.getInstance().getSqlConnector().getSqlWorker().setSetting(settingChangeForm.getGuild(), settingChangeForm.getSetting());
            } else {
                Server.getInstance().getSqlConnector().getSqlWorker().setMessage(settingChangeForm.getGuild(),
                        settingChangeForm.getSetting().getStringValue());
            }

            // Set the Identifier.
            model.addAttribute("identifier", settingChangeForm.getIdentifier());

            // Check if there is a Guild in the Guild list else send to error Page.
            if (guildList.stream().findFirst().isEmpty()) {
                model.addAttribute("IsError", true);
                model.addAttribute("error", "Couldn't load Guild Information! ");

                // Return to error page.
                return ERROR_PATH;
            }

            // If a Guild has been found set it as Attribute.
            model.addAttribute("guild", guildList.stream().findFirst().get());

            // Retrieve every Role and Channel of the Guild and set them as Attribute.
            model.addAttribute("roles", guild.getRoles());
            model.addAttribute("channels", guild.getTextChannels());
            model.addAttribute("joinMessage", new Setting("joinMessage", Server.getInstance().getSqlConnector().getSqlWorker().getMessage(guild.getId())));
        } catch (Exception e) {
            // If the Session is null just return to the default Page.
            if (session == null) return MAIN_PATH;

            // If the Session isn't null give the User a Notification that the Guild couldn't be loaded.
            model.addAttribute("IsError", true);
            model.addAttribute("error", "Couldn't load Guild Information! ");
        }

        return SOCIAL_PATH;
    }

    //endregion

    //region Logging

    /**
     * Request Mapper for the Logging Panel Page.
     *
     * @param id      the Session Identifier.
     * @param guildID the ID of the selected Guild.
     * @param model   the ViewModel.
     * @return {@link String} for Thyme to the HTML Page.
     */
    @GetMapping(path = "/panel/logging")
    public String openPanelLogging(@RequestParam String id, @RequestParam String guildID, Model model) {

        Session session = null;

        try {
            // Try retrieving the Session from the Identifier.
            session = Server.getInstance().getOAuth2Client().getSessionController().getSession(id);

            // Try retrieving the Guild of the OAuth2 User by its ID.
            List<OAuth2Guild> guildList = Server.getInstance().getOAuth2Client().getGuilds(session).complete();
            guildList.removeIf(guild -> !guild.getId().equalsIgnoreCase(guildID) || !guild.hasPermission(Permission.ADMINISTRATOR));

            // If the given Guild ID couldn't be found in his Guild list redirect him to the Error page.
            if (guildList.isEmpty()) return ERROR_PATH;

            // Retrieve the Guild by its giving ID.
            Guild guild = BotInfo.botInstance.getGuildById(guildID);

            // If the Guild couldn't be loaded redirect to Error page.
            if (guild == null) return ERROR_PATH;

            // Set the Identifier.
            model.addAttribute("identifier", id);

            // Check if there is a Guild in the Guild list else send to error Page.
            if (guildList.stream().findFirst().isEmpty()) {
                model.addAttribute("IsError", true);
                model.addAttribute("error", "Couldn't load Guild Information! ");

                // Return to error page.
                return ERROR_PATH;
            }

            // If a Guild has been found set it as Attribute.
            model.addAttribute("guild", guildList.stream().findFirst().get());

            // Retrieve every Log Option and Channel of the Guild and set them as Attribute.
            model.addAttribute("logs", Server.getInstance().getSqlConnector().getSqlWorker().getAllSettings(guild.getId()).stream()
                    .filter(setting -> setting.getName().startsWith("log")).collect(Collectors.toList()));
            model.addAttribute("channels", guild.getTextChannels());
        } catch (Exception e) {
            // If the Session is null just return to the default Page.
            if (session == null) return MAIN_PATH;

            // If the Session isn't null give the User a Notification that the Guild couldn't be loaded.
            model.addAttribute("IsError", true);
            model.addAttribute("error", "Couldn't load Guild Information! ");
        }

        // Return to the Logging Panel Page.
        return LOGGING_PATH;
    }

    /**
     * Request Mapper for the Logging Channel Change Panel.
     *
     * @param channelChangeForm as the Form which contains the needed data.
     * @param model             the ViewModel.
     * @return {@link String} for Thyme to the HTML Page.
     */
    @PostMapping(path = "/panel/logging/channel")
    public String openPanelLogging(@ModelAttribute(name = "channelChangeForm") ChannelChangeForm channelChangeForm, Model model) {

        Session session = null;

        try {
            // Try retrieving the Session from the Identifier.
            session = Server.getInstance().getOAuth2Client().getSessionController().getSession(channelChangeForm.getIdentifier());

            // Try retrieving the Guild of the OAuth2 User by its ID.
            List<OAuth2Guild> guildList = Server.getInstance().getOAuth2Client().getGuilds(session).complete();
            guildList.removeIf(guild -> !guild.getId().equalsIgnoreCase(channelChangeForm.getGuild()) || !guild.hasPermission(Permission.ADMINISTRATOR));

            // If the given Guild ID couldn't be found in his Guild list redirect him to the Error page.
            if (guildList.isEmpty()) return ERROR_PATH;

            // Retrieve the Guild by its giving ID.
            Guild guild = BotInfo.botInstance.getGuildById(channelChangeForm.getGuild());

            // If the Guild couldn't be loaded redirect to Error page.
            if (guild == null) return ERROR_PATH;

            // Change the channel Data.
            // Check if null.
            if (channelChangeForm.getType().equalsIgnoreCase("logChannel") && guild.getTextChannelById(channelChangeForm.getChannel()) != null) {
                // Create new Webhook, If it has been created successfully add it to our Database.
                guild.getTextChannelById(channelChangeForm.getChannel()).createWebhook("Ree6-Logs").queue(webhook ->
                        Server.getInstance().getSqlConnector().getSqlWorker().setLogWebhook(guild.getId(), webhook.getId(), webhook.getToken()));
            }

            // Set the Identifier.
            model.addAttribute("identifier", channelChangeForm.getIdentifier());

            // Check if there is a Guild in the Guild list else send to error Page.
            if (guildList.stream().findFirst().isEmpty()) {
                model.addAttribute("IsError", true);
                model.addAttribute("error", "Couldn't load Guild Information! ");

                // Return to error page.
                return ERROR_PATH;
            }

            // If a Guild has been found set it as Attribute.
            model.addAttribute("guild", guildList.stream().findFirst().get());

            // Retrieve every Log Option and Channel of the Guild and set them as Attribute.
            model.addAttribute("logs", Server.getInstance().getSqlConnector().getSqlWorker().getAllSettings(guild.getId()).stream()
                    .filter(setting -> setting.getName().startsWith("log")).collect(Collectors.toList()));
            model.addAttribute("channels", guild.getTextChannels());
        } catch (Exception e) {
            // If the Session is null just return to the default Page.
            if (session == null) return MAIN_PATH;

            // If the Session isn't null give the User a Notification that the Guild couldn't be loaded.
            model.addAttribute("IsError", true);
            model.addAttribute("error", "Couldn't load Guild Information! ");
        }

        // Return to the Logging Panel Page.
        return LOGGING_PATH;
    }

    /**
     * Request Mapper for the Logging Setting Change Panel.
     *
     * @param settingChangeForm as the Form which contains the needed data.
     * @param model             the ViewModel.
     * @return {@link String} for Thyme to the HTML Page.
     */
    @PostMapping(path = "/panel/logging/settings")
    public String openPanelLogging(@ModelAttribute(name = "settingChangeForm") SettingChangeForm settingChangeForm, Model model) {

        Session session = null;

        try {
            // Try retrieving the Session from the Identifier.
            session = Server.getInstance().getOAuth2Client().getSessionController().getSession(settingChangeForm.getIdentifier());

            // Try retrieving the Guild of the OAuth2 User by its ID.
            List<OAuth2Guild> guildList = Server.getInstance().getOAuth2Client().getGuilds(session).complete();
            guildList.removeIf(guild -> !guild.getId().equalsIgnoreCase(settingChangeForm.getGuild()) || !guild.hasPermission(Permission.ADMINISTRATOR));

            // If the given Guild ID couldn't be found in his Guild list redirect him to the Error page.
            if (guildList.isEmpty()) return ERROR_PATH;

            // Retrieve the Guild by its giving ID.
            Guild guild = BotInfo.botInstance.getGuildById(settingChangeForm.getGuild());

            // If the Guild couldn't be loaded redirect to Error page.
            if (guild == null) return ERROR_PATH;

            // Change the setting Data.
            Server.getInstance().getSqlConnector().getSqlWorker().setSetting(settingChangeForm.getGuild(), settingChangeForm.getSetting());

            // Set the Identifier.
            model.addAttribute("identifier", settingChangeForm.getIdentifier());

            // Check if there is a Guild in the Guild list else send to error Page.
            if (guildList.stream().findFirst().isEmpty()) {
                model.addAttribute("IsError", true);
                model.addAttribute("error", "Couldn't load Guild Information! ");

                // Return to error page.
                return ERROR_PATH;
            }

            // If a Guild has been found set it as Attribute.
            model.addAttribute("guild", guildList.stream().findFirst().get());

            // Retrieve every Log Option and Channel of the Guild and set them as Attribute.
            model.addAttribute("logs", Server.getInstance().getSqlConnector().getSqlWorker().getAllSettings(guild.getId()).stream()
                    .filter(setting -> setting.getName().startsWith("log")).collect(Collectors.toList()));
            model.addAttribute("channels", guild.getTextChannels());
        } catch (Exception e) {
            // If the Session is null just return to the default Page.
            if (session == null) return MAIN_PATH;

            // If the Session isn't null give the User a Notification that the Guild couldn't be loaded.
            model.addAttribute("IsError", true);
            model.addAttribute("error", "Couldn't load Guild Information! ");
        }

        // Return to the Logging Panel Page.
        return LOGGING_PATH;
    }

    //endregion
}
