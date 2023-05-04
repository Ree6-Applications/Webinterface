package de.presti.ree6.backend.service;

import com.jagrosh.jdautilities.oauth2.Scope;
import com.jagrosh.jdautilities.oauth2.entities.OAuth2User;
import com.jagrosh.jdautilities.oauth2.session.Session;
import de.presti.ree6.backend.Server;
import de.presti.ree6.backend.utils.RandomUtils;
import de.presti.ree6.backend.utils.data.container.SessionContainer;
import io.sentry.Sentry;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

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

}
