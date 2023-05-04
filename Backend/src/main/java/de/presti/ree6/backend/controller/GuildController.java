package de.presti.ree6.backend.controller;


import de.presti.ree6.backend.bot.BotWorker;
import de.presti.ree6.backend.repository.ChatLevelRepository;
import de.presti.ree6.backend.repository.VoiceLevelRepository;
import de.presti.ree6.backend.service.SessionService;
import de.presti.ree6.backend.utils.data.container.GuildContainer;
import de.presti.ree6.backend.utils.data.container.LeaderboardContainer;
import de.presti.ree6.backend.utils.data.container.UserContainer;
import de.presti.ree6.backend.utils.data.container.UserLevelContainer;
import de.presti.ree6.sql.entities.level.UserLevel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;

@RestController
@RequestMapping("/guilds")
public class GuildController {
    private final SessionService sessionService;
    private final ChatLevelRepository chatLevelRepository;

    private final VoiceLevelRepository voiceLevelRepository;

    @Autowired
    public GuildController(SessionService sessionService, ChatLevelRepository chatLevelRepository, VoiceLevelRepository voiceLevelRepository) {
        this.sessionService = sessionService;
        this.chatLevelRepository = chatLevelRepository;
        this.voiceLevelRepository = voiceLevelRepository;
    }

    //region Guild Retrieve

    @CrossOrigin
    @GetMapping(value = "/", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<GuildListResponse> retrieveGuilds(@RequestHeader(name = "X-Session-Authenticator") String sessionIdentifier) {
        return sessionService.retrieveGuilds(sessionIdentifier).flatMap(guilds -> Mono.just(new GuildListResponse(true, guilds, "Guilds retrieved!")))
                .onErrorResume(e -> Mono.just(new GuildListResponse(false, null, e.getMessage())))
                .onErrorReturn(new GuildListResponse(false, null, "Server error!"));
    }

    @CrossOrigin
    @GetMapping(value = "/{guildId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<GuildResponse> retrieveGuild(@RequestHeader(name = "X-Session-Authenticator") String sessionIdentifier,
                                             @PathVariable(name = "guildId") String guildId) {
        return sessionService.retrieveGuild(sessionIdentifier, guildId, true).flatMap(guild -> Mono.just(new GuildResponse(true, guild, "Guilds retrieved!")))
                .onErrorResume(e -> Mono.just(new GuildResponse(false, null, e.getMessage())))
                .onErrorReturn(new GuildResponse(false, null, "Server error!"));
    }

    //endregion

    //region Guild Leaderboard

    @CrossOrigin
    @GetMapping(value = "/{guildId}/leaderboard", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<LeaderboardResponse> retrieveSetting(@PathVariable(name = "guildId") String guildId) {
        return sessionService.retrieveGuild(guildId).publishOn(Schedulers.boundedElastic()).flatMap(guild -> {
                    LeaderboardContainer leaderboardContainer = new LeaderboardContainer();

                    leaderboardContainer.setChatLeaderboard(chatLevelRepository.getFirst5getChatUserLevelsByGuildIdOrderByExperienceDesc(guildId).stream()
                            .map(c -> new UserLevelContainer(c, new UserContainer(BotWorker.getShardManager().retrieveUserById(c.getUserId()).complete()))).toList());

                    leaderboardContainer.setVoiceLeaderboard(voiceLevelRepository.getFirst5VoiceLevelsByGuildIdOrderByExperienceDesc(guildId).stream()
                            .map(c -> new UserLevelContainer(c, new UserContainer(BotWorker.getShardManager().retrieveUserById(c.getUserId()).complete()))).toList());

                    leaderboardContainer.setGuildId(Long.parseLong(guildId));

                    return Mono.just(new LeaderboardResponse(true, leaderboardContainer, "Leaderboard retrieved!"));
                })
                .onErrorResume(e -> Mono.just(new LeaderboardResponse(false, null, e.getMessage())))
                .onErrorReturn(new LeaderboardResponse(false, null, "Server error!"));
    }

    //endregion


    public record GuildResponse(boolean success, GuildContainer guild, String message) {
    }

    public record GuildListResponse(boolean success, List<GuildContainer> guilds, String message) {
    }

    public record LeaderboardResponse(boolean success, LeaderboardContainer leaderboard, String message) {
    }
}
