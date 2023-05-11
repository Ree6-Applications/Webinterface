package de.presti.ree6.backend.controller;

import com.github.philippheuer.credentialmanager.domain.OAuth2Credential;
import com.github.twitch4j.auth.domain.TwitchScopes;
import com.jagrosh.jdautilities.oauth2.Scope;
import de.presti.ree6.backend.Server;
import de.presti.ree6.backend.service.SessionService;
import de.presti.ree6.backend.utils.RandomUtils;
import de.presti.ree6.backend.utils.data.CustomOAuth2Util;
import de.presti.ree6.backend.utils.data.Data;
import de.presti.ree6.backend.utils.data.container.SessionContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/auth")
public class SessionController {

    private final SessionService sessionService;

    @Autowired
    public SessionController(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    //region Session Auth

    @CrossOrigin
    @GetMapping(value = "/check", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<AuthResponse> checkSession(@RequestHeader(name = "X-Session-Authenticator") String sessionIdentifier) {
        return sessionService.retrieveSession(sessionIdentifier).map(sessionContainer ->
                        new AuthResponse(true, sessionContainer, "Session valid!"))
                .onErrorResume(e -> Mono.just(new AuthResponse(false, null, e.getMessage())))
                .onErrorReturn(new AuthResponse(false, null, "Server error!"));
    }

    public record AuthResponse(boolean success, SessionContainer session, String message) {
    }

    //endregion

    //region Discord Auth

    @CrossOrigin
    @GetMapping(value = "/discord", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<AuthResponse> completeSession(@RequestParam(name = "code") String code, @RequestParam(name = "state") String state) {
        return sessionService.createSession(code, state).map(sessionContainer ->
                        new AuthResponse(true, sessionContainer, "Session created!"))
                .onErrorResume(e -> Mono.just(new AuthResponse(false, null, e.getMessage())))
                .onErrorReturn(new AuthResponse(false, null, "Server error!"));
    }

    @CrossOrigin
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

    @CrossOrigin
    @GetMapping(value = "/twitch/request")
    public Mono<ErrorControllerImpl.GenericResponse> createTwitch(@RequestHeader(name = "X-Session-Authenticator") String sessionIdentifier) {
        return sessionService.retrieveSession(sessionIdentifier).flatMap(sessionContainer -> Mono.just(new ErrorControllerImpl.GenericResponse(true, Server.getInstance().getTwitchIdentityProvider()
                .getAuthenticationUrl(List.of(TwitchScopes.CHAT_CHANNEL_MODERATE, TwitchScopes.CHAT_READ,
                                TwitchScopes.HELIX_BITS_READ,
                                TwitchScopes.HELIX_CHANNEL_SUBSCRIPTIONS_READ, TwitchScopes.HELIX_CHANNEL_HYPE_TRAIN_READ,
                                TwitchScopes.HELIX_CHANNEL_REDEMPTIONS_READ),
                        RandomUtils.randomString(6)))))
                .onErrorResume(e -> Mono.just(new ErrorControllerImpl.GenericResponse(false, e.getMessage()))
                .onErrorReturn(new ErrorControllerImpl.GenericResponse(false, "Server error!")));
    }

    @CrossOrigin
    @GetMapping(value = "/twitch", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<TwitchAuthResponse> authenticateTwitch(@RequestHeader(name = "X-Session-Authenticator") String sessionIdentifier, @RequestParam(name = "code") String code) {
        return sessionService.retrieveSession(sessionIdentifier).flatMap(sessionContainer -> {
                    OAuth2Credential oAuth2Credential;
                    try {
                        // Try building the credentials.
                        oAuth2Credential = Server.getInstance().getTwitchIdentityProvider().getCredentialByCode(code);
                    } catch (Exception e) {
                        return Mono.error(new IllegalAccessException("Invalid code!"));
                    }
                    Server.getInstance().getCredentialManager().addCredential("twitch", CustomOAuth2Util.convert(sessionContainer.getOAuthUser().getIdLong(), oAuth2Credential));
                    Server.getInstance().getCredentialManager().save();
                    return Mono.just(new TwitchAuthResponse(true, "Twitch authenticated!"));
                })
                .onErrorResume(e -> Mono.just(new TwitchAuthResponse(false, e.getMessage())))
                .onErrorReturn(new TwitchAuthResponse(false, "Server error!"));
    }

    public record TwitchAuthResponse(boolean success, String message) {
    }

    //endregion

}
