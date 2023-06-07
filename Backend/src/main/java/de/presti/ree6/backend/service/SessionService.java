package de.presti.ree6.backend.service;

import com.jagrosh.jdautilities.oauth2.Scope;
import com.jagrosh.jdautilities.oauth2.entities.OAuth2Guild;
import com.jagrosh.jdautilities.oauth2.entities.OAuth2User;
import com.jagrosh.jdautilities.oauth2.session.Session;
import de.presti.ree6.backend.Server;
import de.presti.ree6.backend.bot.BotWorker;
import de.presti.ree6.backend.utils.RandomUtils;
import de.presti.ree6.backend.utils.data.container.guild.GuildContainer;
import de.presti.ree6.backend.utils.data.container.SessionContainer;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * Service meant to handle Sessions.
 */
@Service
public class SessionService {

    /**
     * Retrieve a Session from the Identifier.
     * @param identifier Identifier to identify the Session.
     * @return Session Container with the Session.
     * @throws IllegalAccessException If the Session could not be found.
     */
    public SessionContainer retrieveSession(String identifier) throws IllegalAccessException {
        try {
            // Try retrieving the Session from the Identifier.
            Session session = Server.getInstance().getOAuth2Client().getSessionController().getSession(identifier);

            if (session == null) {
                throw new IllegalAccessException("Session not found!");
            }

            // Try retrieving the User from the Session.
            OAuth2User oAuth2User = Server.getInstance().getOAuth2Client().getUser(session).complete();

            if (oAuth2User == null) {
                throw new IllegalAccessException("User not found!");
            }

            return new SessionContainer("", session, oAuth2User);
        } catch (Exception ignore) {
            throw new IllegalAccessException("Session not found!");
        }
    }

    /**
     * Create a new Session.
     * @param code Code to create the Session.
     * @param state State to create the Session.
     * @return Session Container with the Session.
     * @throws IllegalAccessException If the Session could not be created.
     */
    public SessionContainer createSession(String code, String state) throws IllegalAccessException {
        // Generate a secure Base64 String for the Identifier.
        String identifier = RandomUtils.getRandomBase64String(128);

        try {
            // Try creating a Session.
            Session session = Server.getInstance().getOAuth2Client().startSession(code, state, identifier, Scope.GUILDS, Scope.IDENTIFY, Scope.GUILDS_JOIN).complete();

            // If the given data was valid and a Session has been created redirect to the panel Site. If not redirect to error.
            if (session != null) {

                // Try retrieving the User from the Session.
                OAuth2User oAuth2User = Server.getInstance().getOAuth2Client().getUser(session).complete();

                if (oAuth2User == null) {
                    throw new IllegalAccessException("User not found!");
                }

                return new SessionContainer(identifier, session, Server.getInstance().getOAuth2Client().getUser(session).complete());
            } else {
                throw new IllegalStateException("Session creation failed!");
            }

        } catch (Exception exception) {
            throw new IllegalStateException(exception.getMessage());
        }
    }

    /**
     * Retrieve a Guild from the Identifier and the Guild ID.
     * @param identifier Identifier to identify the Session.
     * @param guildId Guild ID to identify the Guild.
     * @return Guild Container with the Guild.
     * @throws IllegalAccessException If the Guild could not be found.
     */
    public GuildContainer retrieveGuild(String identifier, String guildId) throws IllegalAccessException {
        return retrieveGuild(identifier, guildId, false);
    }

    /**
     * Retrieve a Guild from the Identifier and the Guild ID.
     * @param identifier Identifier to identify the Session.
     * @param guildId Guild ID to identify the Guild.
     * @param retrieveChannels If the Channels should be retrieved.
     * @return Guild Container with the Guild.
     * @throws IllegalAccessException If the Guild could not be found.
     */
    public GuildContainer retrieveGuild(String identifier, String guildId, boolean retrieveChannels) throws IllegalAccessException {
        return retrieveGuild(identifier, guildId, retrieveChannels, false);
    }

    /**
     * Retrieve a Guild from the Identifier and the Guild ID.
     * @param identifier Identifier to identify the Session.
     * @param guildId Guild ID to identify the Guild.
     * @param retrieveChannels If the Channels should be retrieved.
     * @param retrieveRoles If the Roles should be retrieved.
     * @return Guild Container with the Guild.
     * @throws IllegalAccessException If the Guild could not be found.
     */
    public GuildContainer retrieveGuild(String identifier, String guildId, boolean retrieveChannels, boolean retrieveRoles) throws IllegalAccessException {
        SessionContainer sessionContainer = retrieveSession(identifier);

        OAuth2Guild oAuth2Guild = null;
        try {
            oAuth2Guild = Server.getInstance().getOAuth2Client().getGuilds(sessionContainer.getSession()).complete()
                    .stream().filter(c -> c.getId().equals(guildId) && c.hasPermission(Permission.ADMINISTRATOR)).findFirst().orElse(null);
        } catch (Exception ignore) {
        }

        // Retrieve the Guild by its giving ID.
        Guild guild = BotWorker.getShardManager().getGuildById(guildId);

        // If the Guild couldn't be loaded redirect to Error page.
        if (guild == null) {
            if (oAuth2Guild != null) {
                return new GuildContainer(oAuth2Guild);
            } else {
                throw new IllegalAccessException("Guild not found!");
            }
        }

        Member member = guild.retrieveMemberById(sessionContainer.getUser().getId()).complete();

        if (member != null && member.hasPermission(Permission.ADMINISTRATOR)) {
            return new GuildContainer(guild, retrieveChannels, retrieveRoles);
        }

        throw new IllegalAccessException("Not enough permissions!");
    }

    /**
     * Retrieve a Guild from the Guild ID.
     * @param guildId Guild ID to identify the Guild.
     * @return Guild Container with the Guild.
     * @throws IllegalAccessException If the Guild could not be found.
     */
    public GuildContainer retrieveGuild(String guildId) throws IllegalAccessException {
        return retrieveGuild(guildId, false);
    }

    /**
     * Retrieve a Guild from the Guild ID.
     * @param guildId Guild ID to identify the Guild.
     * @param retrieveChannels If the Channels should be retrieved.
     * @return Guild Container with the Guild.
     * @throws IllegalAccessException If the Guild could not be found.
     */
    public GuildContainer retrieveGuild(String guildId, boolean retrieveChannels) throws IllegalAccessException {

        // Retrieve the Guild by its giving ID.
        Guild guild = BotWorker.getShardManager().getGuildById(guildId);

        // If the Guild couldn't be loaded redirect to Error page.
        if (guild == null) throw new IllegalAccessException("Guild not found!");

        return new GuildContainer(guild, retrieveChannels);
    }

    /**
     * Retrieve a List of Guilds from the Identifier.
     * @param identifier Identifier to identify the Session.
     * @return List of Guild Containers with the Guilds.
     * @throws IllegalAccessException If the Guilds could not be found.
     */
    public List<GuildContainer> retrieveGuilds(String identifier) throws IllegalAccessException {
        return retrieveGuilds(identifier, true);
    }

    /**
     * Retrieve a List of Guilds from the Identifier.
     * @param identifier Identifier to identify the Session.
     * @param permissionFilter If the Guilds should be filtered by the Permission.
     * @return List of Guild Containers with the Guilds.
     * @throws IllegalAccessException If the Guilds could not be found.
     */
    public List<GuildContainer> retrieveGuilds(String identifier, boolean permissionFilter) throws IllegalAccessException {
        SessionContainer sessionContainer = retrieveSession(identifier);
        List<OAuth2Guild> guilds = Collections.emptyList();

        try {
            guilds = Server.getInstance().getOAuth2Client().getGuilds(sessionContainer.getSession()).complete();

            if (permissionFilter)
                guilds.removeIf(oAuth2Guild -> !oAuth2Guild.hasPermission(Permission.ADMINISTRATOR));
        } catch (Exception ignore) {
        }

        return guilds.stream().map(GuildContainer::new).toList();
    }
}
