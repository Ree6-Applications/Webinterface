package de.presti.ree6.backend.controller;


import de.presti.ree6.backend.bot.BotWorker;
import de.presti.ree6.backend.repository.AutoRoleRepository;
import de.presti.ree6.backend.repository.BlacklistRepository;
import de.presti.ree6.backend.repository.ChatLevelRepository;
import de.presti.ree6.backend.repository.VoiceLevelRepository;
import de.presti.ree6.backend.service.SessionService;
import de.presti.ree6.backend.utils.data.container.*;
import de.presti.ree6.sql.entities.Blacklist;
import de.presti.ree6.sql.entities.roles.AutoRole;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/guilds")
public class GuildController {
    private final SessionService sessionService;
    private final ChatLevelRepository chatLevelRepository;
    private final VoiceLevelRepository voiceLevelRepository;
    private final BlacklistRepository blacklistRepository;
    private final AutoRoleRepository autoRoleRepository;

    @Autowired
    public GuildController(SessionService sessionService, ChatLevelRepository chatLevelRepository, VoiceLevelRepository voiceLevelRepository, BlacklistRepository blacklistRepository, AutoRoleRepository autoRoleRepository) {
        this.sessionService = sessionService;
        this.chatLevelRepository = chatLevelRepository;
        this.voiceLevelRepository = voiceLevelRepository;
        this.blacklistRepository = blacklistRepository;
        this.autoRoleRepository = autoRoleRepository;
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
        return sessionService.retrieveGuild(sessionIdentifier, guildId, true, true).flatMap(guild -> Mono.just(new GuildResponse(true, guild, "Guilds retrieved!")))
                .onErrorResume(e -> Mono.just(new GuildResponse(false, null, e.getMessage())))
                .onErrorReturn(new GuildResponse(false, null, "Server error!"));
    }

    //endregion

    //region Guild Channel and Role

    @CrossOrigin
    @GetMapping(value = "/{guildId}/channels", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<GuildChannelResponse> retrieveGuildChannels(@RequestHeader(name = "X-Session-Authenticator") String sessionIdentifier,
                                                            @PathVariable(name = "guildId") String guildId) {
        return sessionService.retrieveGuild(sessionIdentifier, guildId, true).flatMap(guild -> Mono.just(new GuildChannelResponse(true, guild.getChannels(), "Channels retrieved!")))
                .onErrorResume(e -> Mono.just(new GuildChannelResponse(false, null, e.getMessage())))
                .onErrorReturn(new GuildChannelResponse(false, null, "Server error!"));
    }

    @CrossOrigin
    @GetMapping(value = "/{guildId}/roles", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<GuildRoleResponse> retrieveGuildRoles(@RequestHeader(name = "X-Session-Authenticator") String sessionIdentifier,
                                                      @PathVariable(name = "guildId") String guildId) {
        return sessionService.retrieveGuild(sessionIdentifier, guildId, false, true).flatMap(guild -> Mono.just(new GuildRoleResponse(true, guild.getRoles(), "Roles retrieved!")))
                .onErrorResume(e -> Mono.just(new GuildRoleResponse(false, null, e.getMessage())))
                .onErrorReturn(new GuildRoleResponse(false, null, "Server error!"));
    }

    //endregion

    //region Guild Blacklist

    @CrossOrigin
    @GetMapping(value = "/{guildId}/blacklist", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<GuildBlacklistResponse> retrieveGuildBlacklist(@RequestHeader(name = "X-Session-Authenticator") String sessionIdentifier,
                                                               @PathVariable(name = "guildId") String guildId) {
        return sessionService.retrieveGuild(sessionIdentifier, guildId, false, false).publishOn(Schedulers.boundedElastic()).flatMap(guild -> {
                    List<String> blacklist = blacklistRepository.getBlacklistByGuildId(guildId).stream().map(Blacklist::getWord).toList();
                    return Mono.just(new GuildBlacklistResponse(true, blacklist, "Blacklist retrieved!"));
                })
                .onErrorResume(e -> Mono.just(new GuildBlacklistResponse(false, null, e.getMessage())))
                .onErrorReturn(new GuildBlacklistResponse(false, null, "Server error!"));
    }

    @CrossOrigin
    @GetMapping(value = "/{guildId}/blacklist/remove", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<GuildBlacklistResponse> removeGuildBlacklist(@RequestHeader(name = "X-Session-Authenticator") String sessionIdentifier,
                                                             @PathVariable(name = "guildId") String guildId,
                                                             @RequestBody String word) {
        return sessionService.retrieveGuild(sessionIdentifier, guildId, false, false).publishOn(Schedulers.boundedElastic()).flatMap(guild -> {
                    Blacklist blacklist = blacklistRepository.getBlacklistByGuildIdAndWord(guildId, word);
                    blacklistRepository.delete(blacklist);
                    return Mono.just(new GuildBlacklistResponse(true, Collections.emptyList(), "Blacklist removed!"));
                })
                .onErrorResume(e -> Mono.just(new GuildBlacklistResponse(false, null, e.getMessage())))
                .onErrorReturn(new GuildBlacklistResponse(false, null, "Server error!"));
    }

    @CrossOrigin
    @GetMapping(value = "/{guildId}/blacklist/add", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<GuildBlacklistResponse> addGuildBlacklist(@RequestHeader(name = "X-Session-Authenticator") String sessionIdentifier,
                                                          @PathVariable(name = "guildId") String guildId,
                                                          @RequestBody String word) {
        return sessionService.retrieveGuild(sessionIdentifier, guildId, false, false).publishOn(Schedulers.boundedElastic()).flatMap(guild -> {
                    Blacklist blacklist = blacklistRepository.getBlacklistByGuildIdAndWord(guildId, word);
                    if (blacklist == null) {
                        blacklist = new Blacklist(guildId, word);
                        blacklistRepository.save(blacklist);
                        return Mono.just(new GuildBlacklistResponse(true, null, "Blacklist added!"));
                    } else {
                        return Mono.just(new GuildBlacklistResponse(false, null, "Word already blacklisted!"));
                    }
                })
                .onErrorResume(e -> Mono.just(new GuildBlacklistResponse(false, null, e.getMessage())))
                .onErrorReturn(new GuildBlacklistResponse(false, null, "Server error!"));
    }

    //endregion

    //region Guild AutoRole

    @CrossOrigin
    @GetMapping(value = "/{guildId}/autorole", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<GuildRoleResponse> retrieveGuildAutoRole(@RequestHeader(name = "X-Session-Authenticator") String sessionIdentifier,
                                                               @PathVariable(name = "guildId") String guildId) {
        return sessionService.retrieveGuild(sessionIdentifier, guildId, false, true).publishOn(Schedulers.boundedElastic()).flatMap(guild -> {
                    List<RoleContainer> autoRoles = autoRoleRepository.getAutoRoleByGuildId(guildId).stream().map(c -> guild.getRoleById(c.getRoleId())).filter(Objects::nonNull).toList();
                    return Mono.just(new GuildRoleResponse(true, autoRoles, "AutoRole retrieved!"));
                })
                .onErrorResume(e -> Mono.just(new GuildRoleResponse(false, null, e.getMessage())))
                .onErrorReturn(new GuildRoleResponse(false, null, "Server error!"));
    }

    @CrossOrigin
    @GetMapping(value = "/{guildId}/autorole/remove", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<GuildRoleResponse> removeGuildAutoRole(@RequestHeader(name = "X-Session-Authenticator") String sessionIdentifier,
                                                             @PathVariable(name = "guildId") String guildId,
                                                             @RequestBody String roleId) {
        return sessionService.retrieveGuild(sessionIdentifier, guildId, false, false).publishOn(Schedulers.boundedElastic()).flatMap(guild -> {
                    AutoRole autoRole = autoRoleRepository.getAutoRoleByGuildIdAndRoleId(guildId, roleId);
                    autoRoleRepository.delete(autoRole);
                    return Mono.just(new GuildRoleResponse(true, Collections.emptyList(), "AutoRole removed!"));
                })
                .onErrorResume(e -> Mono.just(new GuildRoleResponse(false, null, e.getMessage())))
                .onErrorReturn(new GuildRoleResponse(false, null, "Server error!"));
    }

    @CrossOrigin
    @GetMapping(value = "/{guildId}/autorole/add", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<GuildRoleResponse> addGuildAutoRole(@RequestHeader(name = "X-Session-Authenticator") String sessionIdentifier,
                                                          @PathVariable(name = "guildId") String guildId,
                                                          @RequestBody String roleId) {
        return sessionService.retrieveGuild(sessionIdentifier, guildId, false, false).publishOn(Schedulers.boundedElastic()).flatMap(guild -> {
                    AutoRole autoRole = autoRoleRepository.getAutoRoleByGuildIdAndRoleId(guildId, roleId);
                    if (autoRole == null) {
                        autoRole = new AutoRole(guildId, roleId);
                        autoRoleRepository.save(autoRole);
                        return Mono.just(new GuildRoleResponse(true, null, "AutoRole added!"));
                    } else {
                        return Mono.just(new GuildRoleResponse(false, null, "Role is already in AutoRole!"));
                    }
                })
                .onErrorResume(e -> Mono.just(new GuildRoleResponse(false, null, e.getMessage())))
                .onErrorReturn(new GuildRoleResponse(false, null, "Server error!"));
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

    public record GuildChannelResponse(boolean success, List<ChannelContainer> channels, String message) {
    }

    public record GuildRoleResponse(boolean success, List<RoleContainer> roles, String message) {
    }

    public record GuildBlacklistResponse(boolean success, List<String> blacklist, String message) {
    }
}
