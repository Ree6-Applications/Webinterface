package de.presti.ree6.backend.service;

import com.jagrosh.jdautilities.oauth2.Scope;
import com.jagrosh.jdautilities.oauth2.entities.OAuth2Guild;
import com.jagrosh.jdautilities.oauth2.entities.OAuth2User;
import com.jagrosh.jdautilities.oauth2.session.Session;
import de.presti.ree6.backend.Server;
import de.presti.ree6.backend.bot.BotWorker;
import de.presti.ree6.backend.utils.RandomUtils;
import de.presti.ree6.backend.utils.data.container.GuildContainer;
import de.presti.ree6.backend.utils.data.container.SessionContainer;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;

@Service
public class SessionService {

    public Mono<SessionContainer> retrieveSession(String identifier) {
        try {
            // Try retrieving the Session from the Identifier.
            Session session = Server.getInstance().getOAuth2Client().getSessionController().getSession(identifier);

            if (session == null) {
                return Mono.error(new IllegalAccessException("Session not found!"));
            }

            // Try retrieving the User from the Session.
            OAuth2User oAuth2User = Server.getInstance().getOAuth2Client().getUser(session).complete();

            if (oAuth2User == null) {
                return Mono.error(new IllegalAccessException("User not found!"));
            }

            return Mono.just(new SessionContainer("", session, oAuth2User));
        } catch (Exception ignore) {
        }

        return Mono.error(new IllegalAccessException("Session not found!"));
    }

    public Mono<SessionContainer> createSession(String code, String state) {
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
                    return Mono.error(new IllegalAccessException("User not found!"));
                }

                return Mono.just(new SessionContainer(identifier, session, Server.getInstance().getOAuth2Client().getUser(session).complete()));
            } else {
                return Mono.error(new IllegalStateException("Session creation failed!"));
            }

        } catch (Exception ignore) {
            return Mono.error(new IllegalStateException("Session creation failed!"));
        }
    }

    public Mono<GuildContainer> retrieveGuild(String identifier, String guildId) {
        return retrieveGuild(identifier, guildId, false);
    }

    public Mono<GuildContainer> retrieveGuild(String identifier, String guildId, boolean retrieveChannels) {
        return retrieveGuild(identifier, guildId, retrieveChannels, false);
    }

    public Mono<GuildContainer> retrieveGuild(String identifier, String guildId, boolean retrieveChannels, boolean retrieveRoles) {
        return retrieveSession(identifier).flatMap(sessionContainer -> {

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
                    return Mono.just(new GuildContainer(oAuth2Guild));
                } else {
                    return Mono.error(new Exception("Guild not found!"));
                }
            }

            Member member = guild.retrieveMemberById(sessionContainer.getUser().getId()).complete();

            if (member != null && member.hasPermission(Permission.ADMINISTRATOR)) {
                return Mono.just(new GuildContainer(guild, retrieveChannels, retrieveRoles));
            }

            return Mono.error(new Exception("Not enough permissions!"));
        });
    }

    public Mono<GuildContainer> retrieveGuild(String guildId) {
        return retrieveGuild(guildId, false);
    }

    public Mono<GuildContainer> retrieveGuild(String guildId, boolean retrieveChannels) {

        // Retrieve the Guild by its giving ID.
        Guild guild = BotWorker.getShardManager().getGuildById(guildId);

        // If the Guild couldn't be loaded redirect to Error page.
        if (guild == null) return Mono.error(new Exception("Guild not found!"));

        return Mono.just(new GuildContainer(guild, retrieveChannels));
    }

    public Mono<List<GuildContainer>> retrieveGuilds(String identifier) {
        return retrieveSession(identifier).flatMap(sessionContainer -> {
            List<OAuth2Guild> guilds = Collections.emptyList();
            try {
                guilds = Server.getInstance().getOAuth2Client().getGuilds(sessionContainer.getSession()).complete();
                guilds.removeIf(oAuth2Guild -> !oAuth2Guild.hasPermission(Permission.ADMINISTRATOR));
            } catch (Exception ignore) {
            }

            return Mono.just(guilds.stream().map(GuildContainer::new).toList());
        });
    }
}
