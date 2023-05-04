package de.presti.ree6.backend.controller;


import de.presti.ree6.backend.service.SessionService;
import de.presti.ree6.backend.utils.data.container.GuildContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/guilds")
public class GuildController {
    private final SessionService sessionService;

    @Autowired
    public GuildController(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    //region Guild Retrieve

    @CrossOrigin
    @GetMapping(value = "/", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<GuildListResponse> retrieveSettings(@RequestHeader(name = "X-Session-Authenticator") String sessionIdentifier) {
        return sessionService.retrieveGuilds(sessionIdentifier).flatMap(guilds -> Mono.just(new GuildListResponse(true, guilds, "Guilds retrieved!")))
                .onErrorResume(e -> Mono.just(new GuildListResponse(false, null, e.getMessage())))
                .onErrorReturn(new GuildListResponse(false, null, "Server error!"));
    }

    @CrossOrigin
    @GetMapping(value = "/{guildId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<GuildResponse> retrieveSetting(@RequestHeader(name = "X-Session-Authenticator") String sessionIdentifier,
                                               @PathVariable(name = "guildId") String guildId) {
        return sessionService.retrieveGuild(sessionIdentifier, guildId, true).flatMap(guild -> Mono.just(new GuildResponse(true, guild, "Guilds retrieved!")))
                .onErrorResume(e -> Mono.just(new GuildResponse(false, null, e.getMessage())))
                .onErrorReturn(new GuildResponse(false, null, "Server error!"));
    }

    //endregion


    public record GuildResponse(boolean success, GuildContainer guild, String message) {
    }

    public record GuildListResponse(boolean success, List<GuildContainer> guilds, String message) {
    }
}
