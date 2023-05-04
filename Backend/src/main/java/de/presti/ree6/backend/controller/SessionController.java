package de.presti.ree6.backend.controller;

import com.github.philippheuer.credentialmanager.domain.OAuth2Credential;
import de.presti.ree6.backend.Server;
import de.presti.ree6.backend.service.SessionService;
import de.presti.ree6.backend.utils.data.CustomOAuth2Util;
import de.presti.ree6.backend.utils.data.container.SessionContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

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

    public record AuthResponse(boolean success, SessionContainer sessionContainer, String message) {
    }

    //endregion

    //region Discord Auth

    @CrossOrigin
    @GetMapping(value = "/discord", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<AuthResponse> createSession(@RequestParam(name = "code") String code, @RequestParam(name = "state") String state) {
        return sessionService.createSession(code, state).map(sessionContainer ->
                        new AuthResponse(true, sessionContainer, "Session created!"))
                .onErrorResume(e -> Mono.just(new AuthResponse(false, null, e.getMessage())))
                .onErrorReturn(new AuthResponse(false, null, "Server error!"));
    }

    //endregion

    //region Twitch Auth

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
                    Server.getInstance().getCredentialManager().addCredential("twitch", CustomOAuth2Util.convert(sessionContainer.getUser().getIdLong(), oAuth2Credential));
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
