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
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Service meant to handle Sessions.
 */
@Slf4j
@Service
public class SessionService {

    /**
     * Retrieve a Session from the Identifier.
     *
     * @param identifier Identifier to identify the Session.
     * @return Session Container with the Session.
     */
    public Mono<Optional<SessionContainer>> retrieveSession(String identifier) {
        return Mono.fromSupplier(() -> {
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

                return Optional.of(new SessionContainer("", session, oAuth2User));
            } catch (Exception ex) {
                log.debug(ex.getMessage(), ex);
                return Optional.empty();
            }
        });
    }

    /**
     * Create a new Session.
     *
     * @param code  Code to create the Session.
     * @param state State to create the Session.
     * @return Session Container with the Session.
     */
    public Mono<Optional<SessionContainer>> createSession(String code, String state) {
        return Mono.fromSupplier(() -> {
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

                    return Optional.of(new SessionContainer(identifier, session, Server.getInstance().getOAuth2Client().getUser(session).complete()));
                } else {
                    throw new IllegalStateException("Session creation failed!");
                }

            } catch (Exception ex) {
                log.debug(ex.getMessage(), ex);
                return Optional.empty();
            }
        });
    }

    /**
     * Retrieve a Guild from the Identifier and the Guild ID.
     *
     * @param identifier Identifier to identify the Session.
     * @param guildId    Guild ID to identify the Guild.
     * @return Guild Container with the Guild.
     */
    public Mono<Optional<GuildContainer>> retrieveGuild(String identifier, long guildId) {
        return retrieveGuild(identifier, guildId, false);
    }

    /**
     * Retrieve a Guild from the Identifier and the Guild ID.
     *
     * @param identifier       Identifier to identify the Session.
     * @param guildId          Guild ID to identify the Guild.
     * @param retrieveChannels If the Channels should be retrieved.
     * @return Guild Container with the Guild.
     */
    public Mono<Optional<GuildContainer>> retrieveGuild(String identifier, long guildId, boolean retrieveChannels) {
        return retrieveGuild(identifier, guildId, retrieveChannels, false);
    }

    /**
     * Retrieve a Guild from the Identifier and the Guild ID.
     *
     * @param identifier       Identifier to identify the Session.
     * @param guildId          Guild ID to identify the Guild.
     * @param retrieveChannels If the Channels should be retrieved.
     * @param retrieveRoles    If the Roles should be retrieved.
     * @return Guild Container with the Guild.
     */
    public Mono<Optional<GuildContainer>> retrieveGuild(String identifier, long guildId, boolean retrieveChannels, boolean retrieveRoles) {
        return retrieveGuild(identifier, guildId, retrieveChannels, retrieveRoles, true);
    }

    /**
     * Retrieve a Guild from the Identifier and the Guild ID.
     *
     * @param identifier       Identifier to identify the Session.
     * @param guildId          Guild ID to identify the Guild.
     * @param retrieveChannels If the Channels should be retrieved.
     * @param retrieveRoles    If the Roles should be retrieved.
     * @param permissionCheck  If the Permission should be checked.
     * @return Guild Container with the Guild.
     */
    public Mono<Optional<GuildContainer>> retrieveGuild(String identifier, long guildId, boolean retrieveChannels, boolean retrieveRoles, boolean permissionCheck) {
        return retrieveSession(identifier).map(sessionOptional -> {
            if (sessionOptional.isEmpty()) {
                return Optional.empty();
            }

            SessionContainer sessionContainer = sessionOptional.get();

            OAuth2Guild oAuth2Guild = null;
            try {
                oAuth2Guild = Server.getInstance().getOAuth2Client().getGuilds(sessionContainer.getSession()).complete()
                        .stream().filter(c -> c.getIdLong() == guildId && c.hasPermission(Permission.ADMINISTRATOR)).findFirst().orElse(null);
            } catch (Exception ignore) {
            }

            // Retrieve the Guild by its giving ID.
            Guild guild = BotWorker.getShardManager().getGuildById(guildId);

            // If the Guild couldn't be loaded, redirect to Error page.
            if (guild == null) {
                if (oAuth2Guild != null) {
                    return Optional.of(new GuildContainer(oAuth2Guild));
                } else {
                    log.warn("Could not find guild with id {}", guildId);
                    return Optional.empty();
                }
            }

            Member member = guild.retrieveMemberById(sessionContainer.getUser().getId()).complete();
            if (permissionCheck) {
                if (member == null || !member.hasPermission(Permission.ADMINISTRATOR)) {
                    log.warn("User {} has not enough permission for {}", sessionContainer.getUser().getId(), guildId);
                    return Optional.empty();
                }
            }

            GuildContainer guildContainer = new GuildContainer(guild, retrieveChannels, retrieveRoles);
            guildContainer.setAdmin(member.hasPermission(Permission.ADMINISTRATOR));
            return Optional.of(guildContainer);
        });
    }

    /**
     * Retrieve a Guild from the Guild ID.
     *
     * @param guildId Guild ID to identify the Guild.
     * @return Guild Container with the Guild.
     */
    public Mono<Optional<GuildContainer>> retrieveGuild(long guildId) {
        return retrieveGuild(guildId, false);
    }

    /**
     * Retrieve a Guild from the Guild ID.
     *
     * @param guildId          Guild ID to identify the Guild.
     * @param retrieveChannels If the Channels should be retrieved.
     * @return Guild Container with the Guild.
     */
    public Mono<Optional<GuildContainer>> retrieveGuild(long guildId, boolean retrieveChannels) {
        return Mono.fromSupplier(() -> {
            // Retrieve the Guild by its giving ID.
            Guild guild = BotWorker.getShardManager().getGuildById(guildId);

            // If the Guild couldn't be loaded, redirect to Error page.
            if (guild == null) {
                log.warn("Could not find guild with id {}", guildId);
                return Optional.empty();
            }

            return Optional.of(new GuildContainer(guild, retrieveChannels));
        });
    }

    /**
     * Retrieve a List of Guilds from the Identifier.
     *
     * @param identifier Identifier to identify the Session.
     * @return List of Guild Containers with the Guilds.
     */
    public Mono<Optional<List<GuildContainer>>> retrieveGuilds(String identifier) {
        return retrieveGuilds(identifier, false);
    }

    /**
     * Retrieve a List of Guilds from the Identifier.
     *
     * @param identifier       Identifier to identify the Session.
     * @param permissionFilter If the Guilds should be filtered by the Permission.
     * @return List of Guild Containers with the Guilds.
     */
    public Mono<Optional<List<GuildContainer>>> retrieveGuilds(String identifier, boolean permissionFilter) {
        return retrieveSession(identifier).map(sessionOptional -> {
           if (sessionOptional.isEmpty()) {
               return Optional.empty();
           }

           SessionContainer sessionContainer = sessionOptional.get();
            List<OAuth2Guild> guilds = Collections.emptyList();

            try {
                guilds = Server.getInstance().getOAuth2Client().getGuilds(sessionContainer.getSession()).complete();

                if (permissionFilter)
                    guilds.removeIf(oAuth2Guild -> !oAuth2Guild.hasPermission(Permission.ADMINISTRATOR));
            } catch (Exception ignore) {
            }

            if (guilds == null) return Optional.empty();

            return Optional.of(guilds.stream().map(GuildContainer::new).toList());
        });
    }
}
