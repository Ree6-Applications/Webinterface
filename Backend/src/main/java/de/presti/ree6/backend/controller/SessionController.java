package de.presti.ree6.backend.controller;

import com.github.philippheuer.credentialmanager.domain.OAuth2Credential;
import com.github.twitch4j.auth.domain.TwitchScopes;
import com.jagrosh.jdautilities.oauth2.Scope;
import de.presti.ree6.backend.Server;
import de.presti.ree6.backend.service.SessionService;
import de.presti.ree6.backend.utils.RandomUtils;
import de.presti.ree6.backend.utils.data.CustomOAuth2Util;
import de.presti.ree6.backend.utils.data.Data;
import de.presti.ree6.backend.utils.data.container.api.GenericObjectResponse;
import de.presti.ree6.backend.utils.data.container.api.GenericResponse;
import de.presti.ree6.backend.utils.data.container.SessionContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import java.util.List;

/**
 * Controller meant to handle Sessions.
 */
@RestController
@RequestMapping("/auth")
public class SessionController {

    /**
     * Session Service to handle Sessions.
     */
    private final SessionService sessionService;

    /**
     * Controller for the Session Controller.
     * @param sessionService Session Service to handle Sessions.
     */
    @Autowired
    public SessionController(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    //region Session Auth

    /**
     * Create a new Session.
     * @param sessionIdentifier Session Identifier to identify the Session.
     * @return Generic Object Response with the Session.
     */
    @GetMapping(value = "/check", produces = MediaType.APPLICATION_JSON_VALUE)
    public GenericObjectResponse<SessionContainer> checkSession(@RequestHeader(name = "X-Session-Authenticator") String sessionIdentifier) {
        try {
            return new GenericObjectResponse<>(true, sessionService.retrieveSession(sessionIdentifier), "Session valid!");
        } catch (Exception e) {
            return new GenericObjectResponse<>(false, null, e.getMessage());
        }
    }

    //endregion

    //region Discord Auth

    /**
     * Create a new Session.
     * @param code Code to create the Session.
     * @param state State to create the Session.
     * @return Generic Object Response with the Session.
     */
    @GetMapping(value = "/discord", produces = MediaType.APPLICATION_JSON_VALUE)
    public GenericObjectResponse<SessionContainer> completeSession(@RequestParam(name = "code") String code, @RequestParam(name = "state") String state) {
        try {
            return new GenericObjectResponse<>(true, sessionService.createSession(code, state), "Session created!");
        } catch (Exception e) {
            return new GenericObjectResponse<>(false, null, e.getMessage());
        }
    }

    /**
     * Create a new Session.
     * @return Redirect View to the Discord OAuth2 Page.
     */
    @GetMapping(value = "/discord/request")
    public RedirectView createSession() {
        return new RedirectView(Server.getInstance().getOAuth2Client().generateAuthorizationURL(
                Data.getDiscordRedirectUrl(),
                Scope.GUILDS,
                Scope.IDENTIFY,
                Scope.GUILDS_JOIN
        ));
    }

    //endregion

    //region Twitch Auth

    /**
     * Create a new Twitch Session.
     * @return Generic Object Response with the Session.
     */
    @GetMapping(value = "/twitch/request")
    public RedirectView createTwitch() {
        try {
            return new RedirectView(Server.getInstance().getTwitchIdentityProvider()
                    .getAuthenticationUrl(List.of(TwitchScopes.CHAT_CHANNEL_MODERATE, TwitchScopes.CHAT_READ,
                                    TwitchScopes.HELIX_BITS_READ,
                                    TwitchScopes.HELIX_CHANNEL_SUBSCRIPTIONS_READ, TwitchScopes.HELIX_CHANNEL_HYPE_TRAIN_READ,
                                    TwitchScopes.HELIX_CHANNEL_REDEMPTIONS_READ),
                            RandomUtils.randomString(6)));
        } catch (Exception e) {
            return new RedirectView(Data.getErrorRedirectUrl());
        }
    }

    /**
     * Create a new Twitch Session.
     * @param sessionIdentifier Session Identifier to identify the Session.
     * @param code Code to create the Session.
     * @return Generic Object Response with the Session.
     */
    @GetMapping(value = "/twitch", produces = MediaType.APPLICATION_JSON_VALUE)
    public GenericResponse authenticateTwitch(@RequestHeader(name = "X-Session-Authenticator") String sessionIdentifier, @RequestParam(name = "code") String code) {
        try {
            SessionContainer sessionContainer = sessionService.retrieveSession(sessionIdentifier);
            OAuth2Credential oAuth2Credential;

            try {
                // Try building the credentials.
                oAuth2Credential = Server.getInstance().getTwitchIdentityProvider().getCredentialByCode(code);
            } catch (Exception e) {
                throw new IllegalAccessException("Invalid code!");
            }

            // Add the credential to the credential manager.
            Server.getInstance().getCredentialManager().addCredential("twitch", CustomOAuth2Util.convert(sessionContainer.getOAuthUser().getIdLong(), oAuth2Credential));
            Server.getInstance().getCredentialManager().save();

            return new GenericResponse(true, "Twitch authenticated!");
        } catch (Exception e) {
            return new GenericResponse(false, e.getMessage());
        }
    }

    //endregion

}
