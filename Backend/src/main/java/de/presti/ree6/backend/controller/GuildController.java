package de.presti.ree6.backend.controller;


import de.presti.ree6.backend.bot.BotWorker;
import de.presti.ree6.backend.service.GuildService;
import de.presti.ree6.backend.service.SessionService;
import de.presti.ree6.backend.utils.data.ConverterUtil;
import de.presti.ree6.backend.utils.data.Data;
import de.presti.ree6.backend.utils.data.container.*;
import de.presti.ree6.backend.utils.data.container.api.*;
import de.presti.ree6.backend.utils.data.container.guild.GuildContainer;
import de.presti.ree6.backend.utils.data.container.guild.GuildStatsContainer;
import de.presti.ree6.backend.utils.data.container.role.RoleContainer;
import de.presti.ree6.backend.utils.data.container.role.RoleLevelContainer;
import de.presti.ree6.backend.utils.data.container.user.UserContainer;
import de.presti.ree6.backend.utils.data.container.user.UserLevelContainer;
import de.presti.ree6.sql.SQLSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Slf4j
@RestController
@RequestMapping("/guilds")
public class GuildController {
    private final SessionService sessionService;

    private final GuildService guildService;

    @Autowired
    public GuildController(SessionService sessionService, GuildService guildService) {
        this.sessionService = sessionService;
        this.guildService = guildService;
    }

    //region Guild Retrieve

    @GetMapping(value = "/", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<GenericObjectResponse<List<GuildContainer>>> retrieveGuilds(@RequestHeader(name = "X-Session-Authenticator") String sessionIdentifier) {
        return sessionService.retrieveGuilds(sessionIdentifier)
                .map(x -> x.map(y -> new GenericObjectResponse<>(true, y, "Guilds retrieve!"))
                        .orElse(new GenericObjectResponse<>(false, null, "Could not retrieve guilds!")));
    }

    @GetMapping(value = "/{guildId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<GenericObjectResponse<GuildContainer>> retrieveGuild(@RequestHeader(name = "X-Session-Authenticator") String sessionIdentifier, @PathVariable(name = "guildId") long guildId) {
        return sessionService.retrieveGuild(sessionIdentifier, guildId, true, true)
                .map(x -> x.map(y -> new GenericObjectResponse<>(true, y, "Guild retrieved!"))
                        .orElse(new GenericObjectResponse<>(false, null, "Could not retrieve guild!")));
    }

    //endregion

    //region Guild Channel and Role

    @GetMapping(value = "/{guildId}/channels", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<GenericObjectResponse<List<ChannelContainer>>> retrieveGuildChannels(@RequestHeader(name = "X-Session-Authenticator") String sessionIdentifier, @PathVariable(name = "guildId") long guildId) {
        return sessionService.retrieveGuild(sessionIdentifier, guildId, true, true)
                .map(x -> x.map(y -> new GenericObjectResponse<>(true, y.getChannels(), "Channels retrieved!"))
                        .orElse(new GenericObjectResponse<>(false, null, "Could not retrieve channels!")));
    }

    @GetMapping(value = "/{guildId}/roles", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<GenericObjectResponse<List<RoleContainer>>> retrieveGuildRoles(@RequestHeader(name = "X-Session-Authenticator") String sessionIdentifier, @PathVariable(name = "guildId") long guildId) {
        return sessionService.retrieveGuild(sessionIdentifier, guildId, true, true)
                .map(x -> x.map(y -> new GenericObjectResponse<>(true, y.getRoles(), "Roles retrieved!"))
                        .orElse(new GenericObjectResponse<>(false, null, "Could not retrieve roles!")));
    }

    //endregion

    //region Guild Blacklist

    @GetMapping(value = "/{guildId}/blacklist", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<GenericObjectResponse<List<String>>> retrieveGuildBlacklist(@RequestHeader(name = "X-Session-Authenticator") String sessionIdentifier, @PathVariable(name = "guildId") long guildId) {
        return sessionService.retrieveGuild(sessionIdentifier, guildId)
                .mapNotNull(x -> x.map(y -> SQLSession.getSqlConnector().getSqlWorker().getChatProtectorWords(guildId)
                                .map(z -> new GenericObjectResponse<>(true, z, "Blacklist retrieved!")).block())
                        .orElse(new GenericObjectResponse<>(false, null, "Could not retrieve blacklist!")));
    }

    @PostMapping(value = "/{guildId}/blacklist/remove", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<GenericResponse> removeGuildBlacklist(@RequestHeader(name = "X-Session-Authenticator") String sessionIdentifier, @PathVariable(name = "guildId") long guildId, @RequestBody GenericValueRequest request) {
        return sessionService.retrieveGuild(sessionIdentifier, guildId)
                .map(x -> x.map(y -> {
                    SQLSession.getSqlConnector().getSqlWorker().removeChatProtectorWord(guildId, request.value());
                    return new GenericResponse(true, "Blacklist removed!");
                }).orElse(new GenericResponse(false, "Could not remove!")));
    }

    @PostMapping(value = "/{guildId}/blacklist/add", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<GenericResponse> addGuildBlacklist(@RequestHeader(name = "X-Session-Authenticator") String sessionIdentifier, @PathVariable(name = "guildId") long guildId, @RequestBody GenericValueRequest request) {
        return sessionService.retrieveGuild(sessionIdentifier, guildId)
                .map(x -> x.map(y -> SQLSession.getSqlConnector().getSqlWorker().isChatProtectorSetup(guildId, request.value())
                        .map(z -> {
                            if (!z) {
                                SQLSession.getSqlConnector().getSqlWorker().addChatProtectorWord(guildId, request.value());
                                return new GenericResponse(true, "Blacklist added!");
                            } else {
                                return new GenericResponse(false, "Word already blacklisted!");
                            }
                        }).block()).orElse(new GenericResponse(false, "Could not add!")));
    }

    //endregion

    //region Guild AutoRole

    @GetMapping(value = "/{guildId}/autorole", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<GenericObjectResponse<List<RoleContainer>>> retrieveGuildAutoRole(@RequestHeader(name = "X-Session-Authenticator") String sessionIdentifier, @PathVariable(name = "guildId") long guildId) {
        return sessionService.retrieveGuild(sessionIdentifier, guildId, false, true)
                .map(x -> x.map(y -> SQLSession.getSqlConnector().getSqlWorker().getAutoRoles(guildId)
                        .map(z -> new GenericObjectResponse<>(true, z.stream()
                                .map(c -> y.getRoleById(c.getRoleId())).filter(Objects::nonNull).toList(), "AutoRole retrieved!"))
                        .block()).orElse(new GenericObjectResponse<>(false, Collections.emptyList(), "Couldnt retrieve!")));
    }

    @PostMapping(value = "/{guildId}/autorole/remove", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<GenericResponse> removeGuildAutoRole(@RequestHeader(name = "X-Session-Authenticator") String sessionIdentifier, @PathVariable(name = "guildId") long guildId, @RequestBody GenericValueRequest request) {
        return sessionService.retrieveGuild(sessionIdentifier, guildId)
                .map(x -> x.map(y -> {
                    try {
                        long roleId = ConverterUtil.convertStringToLong(request.value());

                        if (roleId == -1) {
                            return new GenericResponse(false, "Role not found!");
                        }

                        SQLSession.getSqlConnector().getSqlWorker().removeAutoRole(guildId, roleId);
                        return new GenericResponse(true, "AutoRole removed!");
                    } catch (Exception e) {
                        return new GenericResponse(false, e.getMessage());
                    }
                }).orElse(new GenericResponse(false, "Could not remove!")));
    }

    @PostMapping(value = "/{guildId}/autorole/add", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<GenericResponse> addGuildAutoRole(@RequestHeader(name = "X-Session-Authenticator") String sessionIdentifier, @PathVariable(name = "guildId") long guildId, @RequestBody GenericValueRequest request) {
        return sessionService.retrieveGuild(sessionIdentifier, guildId, false, true)
                .map(x -> x.map(y -> {
                    try {
                        long roleId = ConverterUtil.convertStringToLong(request.value());

                        if (roleId == -1) {
                            return new GenericResponse(false, "Role not found!");
                        }

                        if (y.getRoleById(roleId) == null) throw new IllegalAccessException("Role not found!");

                        return SQLSession.getSqlConnector().getSqlWorker().isAutoRoleSetup(guildId, roleId)
                                .map(z -> {
                                    if (!z) {
                                        SQLSession.getSqlConnector().getSqlWorker().addAutoRole(guildId, roleId);
                                        return new GenericResponse(true, "AutoRole added!");
                                    } else {
                                        return new GenericResponse(false, "Role is already in AutoRole!");
                                    }
                                })
                                .block();
                    } catch (Exception e) {
                        return new GenericResponse(false, e.getMessage());
                    }
                }).orElse(new GenericResponse(false, "Could not remove!")));
    }

    //endregion

    //region Guild Leaderboard

    @GetMapping(value = "/{guildId}/leaderboard/voice", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<GenericObjectResponse<LeaderboardContainer>> retrieveLeaderboardVoice(@PathVariable(name = "guildId") long guildId) {
        return sessionService.retrieveGuild(guildId)
                .map(x -> x.map(y -> {
                            GuildContainer guildContainer = x.get();

                            return SQLSession.getSqlConnector().getSqlWorker().getTopVoice(guildId, Data.getLeaderboardTop())
                                    .map(z -> {
                                        LeaderboardContainer leaderboardContainer = new LeaderboardContainer();

                                        leaderboardContainer.setChatLeaderboard(z.stream().map(c -> new UserLevelContainer(c, new UserContainer(BotWorker.getShardManager().retrieveUserById(c.getUserId()).complete()))).toList());

                                        leaderboardContainer.setGuildId(guildContainer.getId());
                                        return new GenericObjectResponse<>(true, leaderboardContainer, "Leaderboard retrieved!");
                                    }).block();
                        })
                        .orElse(new GenericObjectResponse<>(false, null, "Could not retrieve!")));
    }

    @GetMapping(value = "/{guildId}/leaderboard/chat", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<GenericObjectResponse<LeaderboardContainer>> retrieveLeaderboardChat(@PathVariable(name = "guildId") long guildId) {
        return sessionService.retrieveGuild(guildId)
                .map(x -> x.map(y -> {
                            GuildContainer guildContainer = x.get();

                            return SQLSession.getSqlConnector().getSqlWorker().getTopChat(guildId, Data.getLeaderboardTop())
                                    .map(z -> {
                                        LeaderboardContainer leaderboardContainer = new LeaderboardContainer();

                                        leaderboardContainer.setChatLeaderboard(z.stream().map(c -> new UserLevelContainer(c, new UserContainer(BotWorker.getShardManager().retrieveUserById(c.getUserId()).complete()))).toList());

                                        leaderboardContainer.setGuildId(guildContainer.getId());
                                        return new GenericObjectResponse<>(true, leaderboardContainer, "Leaderboard retrieved!");
                                    }).block();
                        })
                        .orElse(new GenericObjectResponse<>(false, null, "Could not retrieve!")));
    }

    //endregion

    // region Guild Stats

    @GetMapping(value = "/{guildId}/stats", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<GenericObjectResponse<GuildStatsContainer>> retrieveStats(@RequestHeader(name = "X-Session-Authenticator") String sessionIdentifier, @PathVariable(name = "guildId") long guildId) {
        return guildService.getStats(sessionIdentifier, guildId)
                .map(x -> new GenericObjectResponse<>(x.isPresent(), x.orElse(null), x.isPresent() ? "Retrieved" : "Failed to retrieve!"));
    }

    //endregion

    // region Guild Chat Autorole

    @GetMapping(value = "/{guildId}/chatrole", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<GenericObjectResponse<List<RoleLevelContainer>>> retrieveChatRoles(@RequestHeader(name = "X-Session-Authenticator") String sessionIdentifier, @PathVariable(name = "guildId") long guildId) {
        return guildService.getChatAutoRoles(sessionIdentifier, guildId)
                .map(x -> new GenericObjectResponse<>(x.isPresent(), x.orElse(Collections.emptyList()), x.isPresent() ? "Retrieved!" : "Could not retrieve!"));
    }

    @PostMapping(value = "/{guildId}/chatrole/remove", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<GenericResponse> removeChatAutoRole(@RequestHeader(name = "X-Session-Authenticator") String sessionIdentifier, @PathVariable(name = "guildId") long guildId, @RequestBody GenericValueRequest valueRequest) {
        try {
            long level = ConverterUtil.convertStringToLong(valueRequest.value());

            return guildService.removeChatAutoRole(sessionIdentifier, guildId, level)
                    .map(x -> new GenericResponse(x, x ? "Removed!" : "Failed to remove!"));
        } catch (Exception e) {
            return Mono.just(new GenericResponse(false, e.getMessage()));
        }
    }

    @PostMapping(value = "/{guildId}/chatrole/add", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<GenericResponse> addChatAutoRole(@RequestHeader(name = "X-Session-Authenticator") String sessionIdentifier, @PathVariable(name = "guildId") long guildId, @RequestBody LevelAutoRoleRequest levelAutoRoleRequest) {
        return guildService.addChatAutoRole(sessionIdentifier, guildId, levelAutoRoleRequest.role(), levelAutoRoleRequest.level())
                .map(x -> new GenericResponse(x, x ? "Added!" : "Failed to add!"));
    }

    //endregion

    // region Guild Voice Autorole

    @GetMapping(value = "/{guildId}/voicerole", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<GenericObjectResponse<List<RoleLevelContainer>>> retrieveVoiceRoles(@RequestHeader(name = "X-Session-Authenticator") String sessionIdentifier, @PathVariable(name = "guildId") long guildId) {
        return guildService.getVoiceAutoRoles(sessionIdentifier, guildId)
                .map(x -> new GenericObjectResponse<>(x.isPresent(), x.orElse(Collections.emptyList()), x.isPresent() ? "Retrieved!" : "Could not retrieve!"));
    }

    @PostMapping(value = "/{guildId}/voicerole/remove", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<GenericResponse> removeVoiceAutoRole(@RequestHeader(name = "X-Session-Authenticator") String sessionIdentifier, @PathVariable(name = "guildId") long guildId, @RequestBody GenericValueRequest valueRequest) {
        try {
            long level = ConverterUtil.convertStringToLong(valueRequest.value());

            return guildService.removeVoiceAutoRole(sessionIdentifier, guildId, level)
                    .map(x -> new GenericResponse(x, x ? "Removed!" : "Failed to remove!"));
        } catch (Exception e) {
            return Mono.just(new GenericResponse(false, e.getMessage()));
        }
    }

    @PostMapping(value = "/{guildId}/voicerole/add", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<GenericResponse> addVoiceAutoRole(@RequestHeader(name = "X-Session-Authenticator") String sessionIdentifier, @PathVariable(name = "guildId") long guildId, @RequestBody LevelAutoRoleRequest levelAutoRoleRequest) {
        return guildService.addVoiceAutoRole(sessionIdentifier, guildId, levelAutoRoleRequest.role(), levelAutoRoleRequest.level())
                .map(x -> new GenericResponse(x, x ? "Added!" : "Failed to add!"));
    }

    //endregion

    //region Recording

    @GetMapping(value = "/recording", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<GenericObjectResponse<RecordContainer>> retrieveRecording(@RequestHeader(name = "X-Session-Authenticator") String sessionIdentifier, @RequestParam(name = "recordId") String recordId) {
        return guildService.getRecordingContainer(sessionIdentifier, recordId)
                .map(x -> new GenericObjectResponse<>(x.isPresent(), x.orElse(null), x.isPresent() ? "Retrieved" : "Failed to retrieve!"));
    }

    @GetMapping(value = "/recording/download", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<Resource>> downloadRecording(@RequestHeader(name = "X-Session-Authenticator") String sessionIdentifier, @RequestParam(name = "recordId") String recordId) {
        return guildService.getRecordingBytes(sessionIdentifier, recordId)
                .map(x -> x.map(y -> {
                    Resource resource = new ByteArrayResource(y);
                    try {
                        return ResponseEntity.ok().contentType(MediaType.APPLICATION_OCTET_STREAM).contentLength(resource.contentLength()).header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename("recording.wav").build().toString()).body(resource);
                    } catch (IOException e) {
                        resource = null;
                        log.error("Failed to download recording", e);
                        return ResponseEntity.badRequest().body(resource);
                    }
                }).orElse(ResponseEntity.badRequest().body(null)));
    }

    //endregion

    //region Guild Welcome Channel

    @GetMapping(value = "/{guildId}/welcome", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<GenericObjectResponse<ChannelContainer>> retrieveWelcomeChannel(@RequestHeader(name = "X-Session-Authenticator") String sessionIdentifier, @PathVariable(name = "guildId") long guildId) {
        return guildService.getWelcomeChannel(sessionIdentifier, guildId)
                .map(x -> new GenericObjectResponse<>(x.isPresent(), x.orElse(null), x.isPresent() ? "Retrieved" : "Failed to retrieve!"));
    }

    @PostMapping(value = "/{guildId}/welcome/remove", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<GenericResponse> removeWelcomeChannel(@RequestHeader(name = "X-Session-Authenticator") String sessionIdentifier, @PathVariable(name = "guildId") long guildId) {
        return guildService.removeWelcomeChannel(sessionIdentifier, guildId)
                .map(x -> new GenericResponse(x.isPresent(), x.isPresent() ? "Removed!" : "Failed to remove!"));
    }

    @PostMapping(value = "/{guildId}/welcome/add", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<GenericResponse> addWelcomeChannel(@RequestHeader(name = "X-Session-Authenticator") String sessionIdentifier, @PathVariable(name = "guildId") long guildId, @RequestBody GenericValueRequest request) {
        return guildService.updateWelcomeChannel(sessionIdentifier, guildId, request.value())
                .map(x -> new GenericResponse(x, x ? "Updated!" : "Failed to update!"));
    }

    //endregion

    //region Guild Log Channel

    @GetMapping(value = "/{guildId}/log", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<GenericObjectResponse<ChannelContainer>> retrieveLogChannel(@RequestHeader(name = "X-Session-Authenticator") String sessionIdentifier, @PathVariable(name = "guildId") long guildId) {
        return guildService.getLogChannel(sessionIdentifier, guildId)
                .map(x -> new GenericObjectResponse<>(x.isPresent(), x.orElse(null), x.isPresent() ? "Retrieved" : "Failed to retrieve!"));
    }

    @PostMapping(value = "/{guildId}/log/remove", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<GenericResponse> removeLogChannel(@RequestHeader(name = "X-Session-Authenticator") String sessionIdentifier, @PathVariable(name = "guildId") long guildId) {
        return guildService.removeLogChannel(sessionIdentifier, guildId)
                .map(x -> new GenericResponse(x.isPresent(), x.isPresent() ? "Removed!" : "Failed to remove!"));
    }

    @PostMapping(value = "/{guildId}/log/add", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<GenericResponse> addLogChannel(@RequestHeader(name = "X-Session-Authenticator") String sessionIdentifier, @PathVariable(name = "guildId") long guildId, @RequestBody GenericValueRequest request) {
        return guildService.updateLogChannel(sessionIdentifier, guildId, request.value())
                .map(x -> new GenericResponse(x, x ? "Updated!" : "Failed to update!"));
    }

    //endregion

    //region Notifier

    //region Reddit Notifier

    @GetMapping(value = "/{guildId}/reddit", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<GenericObjectResponse<List<NotifierContainer>>> retrieveRedditNotifier(@RequestHeader(name = "X-Session-Authenticator") String sessionIdentifier, @PathVariable(name = "guildId") long guildId) {
        return guildService.getRedditNotifier(sessionIdentifier, guildId)
                .map(x -> new GenericObjectResponse<>(true, x, "Notifier received!"));
    }

    @PostMapping(value = "/{guildId}/reddit/remove", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<GenericResponse> removeRedditNotifier(@RequestHeader(name = "X-Session-Authenticator") String sessionIdentifier, @PathVariable(name = "guildId") long guildId, @RequestBody GenericValueRequest request) {
        return guildService.removeRedditNotifier(sessionIdentifier, guildId, request.value())
                .map(x -> x ? new GenericResponse(true, "Notifier remove!") :
                        new GenericResponse(false, "Couldn't remove Notifier!"));
    }

    @PostMapping(value = "/{guildId}/reddit/add", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<GenericResponse> addRedditNotifier(@RequestHeader(name = "X-Session-Authenticator") String sessionIdentifier, @PathVariable(name = "guildId") long guildId, @RequestBody GenericNotifierRequest notifierRequestObject) {
        return guildService.addRedditNotifier(sessionIdentifier, guildId, notifierRequestObject)
                .map(x -> x ? new GenericResponse(true, "Notifier added!") :
                        new GenericResponse(false, "Couldn't add Notifier!"));
    }

    //endregion

    //region Twitch Notifier

    @GetMapping(value = "/{guildId}/twitch", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<GenericObjectResponse<List<NotifierContainer>>> retrieveTwitchNotifier(@RequestHeader(name = "X-Session-Authenticator") String sessionIdentifier, @PathVariable(name = "guildId") long guildId) {
        return guildService.getTwitchNotifier(sessionIdentifier, guildId)
                .map(x -> new GenericObjectResponse<>(true, x, "Notifier received!"));
    }

    @PostMapping(value = "/{guildId}/twitch/remove", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<GenericResponse> removeTwitchNotifier(@RequestHeader(name = "X-Session-Authenticator") String sessionIdentifier, @PathVariable(name = "guildId") long guildId, @RequestBody GenericValueRequest request) {
        return guildService.removeTwitchNotifier(sessionIdentifier, guildId, request.value())
                .map(x -> x ? new GenericResponse(true, "Notifier remove!") :
                        new GenericResponse(false, "Couldn't remove Notifier!"));
    }

    @PostMapping(value = "/{guildId}/twitch/add", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<GenericResponse> addTwitchNotifier(@RequestHeader(name = "X-Session-Authenticator") String sessionIdentifier, @PathVariable(name = "guildId") long guildId, @RequestBody GenericNotifierRequest notifierRequestObject) {
        return guildService.addTwitchNotifier(sessionIdentifier, guildId, notifierRequestObject)
                .map(x -> x ? new GenericResponse(true, "Notifier added!") :
                        new GenericResponse(false, "Couldn't add Notifier!"));
    }


    //endregion

    //region Twitter Notifier

    @GetMapping(value = "/{guildId}/twitter", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<GenericObjectResponse<List<NotifierContainer>>> retrieveTwitterNotifier(@RequestHeader(name = "X-Session-Authenticator") String sessionIdentifier, @PathVariable(name = "guildId") long guildId) {
        return guildService.getTwitterNotifier(sessionIdentifier, guildId)
                .map(x -> new GenericObjectResponse<>(true, x, "Notifier received!"));
    }

    @PostMapping(value = "/{guildId}/twitter/remove", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<GenericResponse> removeTwitterNotifier(@RequestHeader(name = "X-Session-Authenticator") String sessionIdentifier, @PathVariable(name = "guildId") long guildId, @RequestBody GenericValueRequest request) {
        return guildService.removeTwitterNotifier(sessionIdentifier, guildId, request.value())
                .map(x -> x ? new GenericResponse(true, "Notifier remove!") :
                        new GenericResponse(false, "Couldn't remove Notifier!"));
    }

    @PostMapping(value = "/{guildId}/twitter/add", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<GenericResponse> addTwitterNotifier(@RequestHeader(name = "X-Session-Authenticator") String sessionIdentifier, @PathVariable(name = "guildId") long guildId, @RequestBody GenericNotifierRequest notifierRequestObject) {
        return guildService.addTwitterNotifier(sessionIdentifier, guildId, notifierRequestObject)
                .map(x -> x ? new GenericResponse(true, "Notifier added!") :
                        new GenericResponse(false, "Couldn't add Notifier!"));
    }

    //endregion

    //region YouTube Notifier

    @GetMapping(value = "/{guildId}/youtube", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<GenericObjectResponse<List<NotifierContainer>>> retrieveYoutubeNotifier(@RequestHeader(name = "X-Session-Authenticator") String sessionIdentifier, @PathVariable(name = "guildId") long guildId) {
        return guildService.getYouTubeNotifier(sessionIdentifier, guildId)
                .map(x -> new GenericObjectResponse<>(true, x, "Notifier received!"));
    }

    @PostMapping(value = "/{guildId}/youtube/remove", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<GenericResponse> removeYoutubeNotifier(@RequestHeader(name = "X-Session-Authenticator") String sessionIdentifier, @PathVariable(name = "guildId") long guildId, @RequestBody GenericValueRequest request) {
        return guildService.removeYouTubeNotifier(sessionIdentifier, guildId, request.value())
                .map(x -> x ? new GenericResponse(true, "YouTube Notifier remove!") :
                        new GenericResponse(false, "Couldn't remove YouTube Notifier!"));
    }

    @PostMapping(value = "/{guildId}/youtube/add", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<GenericResponse> addYoutubeNotifier(@RequestHeader(name = "X-Session-Authenticator") String sessionIdentifier, @PathVariable(name = "guildId") long guildId, @RequestBody GenericNotifierRequest notifierRequestObject) {
        return guildService.addYouTubeNotifier(sessionIdentifier, guildId, notifierRequestObject)
                .map(x -> x ? new GenericResponse(true, "Notifier added!") :
                        new GenericResponse(false, "Couldn't add Notifier!"));
    }

    //endregion

    //region Instagram Notifier

    @GetMapping(value = "/{guildId}/instagram", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<GenericObjectResponse<List<NotifierContainer>>> retrieveInstagramNotifier(@RequestHeader(name = "X-Session-Authenticator") String sessionIdentifier, @PathVariable(name = "guildId") long guildId) {
        return guildService.getInstagramNotifier(sessionIdentifier, guildId)
                .map(x -> new GenericObjectResponse<>(true, x, "Notifier received!"));
    }

    @PostMapping(value = "/{guildId}/instagram/remove", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<GenericResponse> removeInstagramNotifier(@RequestHeader(name = "X-Session-Authenticator") String sessionIdentifier, @PathVariable(name = "guildId") long guildId, @RequestBody GenericValueRequest request) {
        return guildService.removeInstagramNotifier(sessionIdentifier, guildId, request.value())
                .map(x -> x ? new GenericResponse(true, "Instagram Notifier remove!") :
                        new GenericResponse(false, "Couldn't remove Instagram Notifier!"));
    }

    @PostMapping(value = "/{guildId}/instagram/add", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<GenericResponse> addInstagramNotifier(@RequestHeader(name = "X-Session-Authenticator") String sessionIdentifier, @PathVariable(name = "guildId") long guildId, @RequestBody GenericNotifierRequest notifierRequestObject) {
        return guildService.addInstagramNotifier(sessionIdentifier, guildId, notifierRequestObject)
                .map(x -> x ? new GenericResponse(true, "Notifier added!") :
                        new GenericResponse(false, "Couldn't add Notifier!"));
    }

    //endregion

    //endregion

    //region Temporal Voice Channels

    @GetMapping(value = "/{guildId}/temporalvoice", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<GenericObjectResponse<ChannelContainer>> retrieveTemporalVoice(@RequestHeader(name = "X-Session-Authenticator") String sessionIdentifier, @PathVariable(name = "guildId") long guildId) {
        return guildService.getTemporalVoice(sessionIdentifier, guildId)
                .map(x -> new GenericObjectResponse<>(x.isPresent(), x.orElse(null), x.isPresent() ? "Retrieved" : "Failed to retrieve!"));
    }


    @PostMapping(value = "/{guildId}/temporalvoice/remove", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<GenericResponse> removeTemporalVoiceChannel(@RequestHeader(name = "X-Session-Authenticator") String sessionIdentifier, @PathVariable(name = "guildId") long guildId) {
        return guildService.removeTemporalVoice(sessionIdentifier, guildId)
                .map(x -> new GenericResponse(x, x ? "Removed!" : "Failed to remove!"));
    }


    @PostMapping(value = "/{guildId}/temporalvoice/add", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<GenericResponse> addTemporalVoiceChannel(@RequestHeader(name = "X-Session-Authenticator") String sessionIdentifier, @PathVariable(name = "guildId") long guildId, @RequestBody GenericValueRequest request) {
        try {
            long channelId = ConverterUtil.convertStringToLong(request.value());

            if (channelId == -1) {
                return Mono.just(new GenericResponse(false, "Channel not found!"));
            }

            return guildService.updateTemporalVoice(sessionIdentifier, guildId, channelId)
                    .map(x -> new GenericResponse(x, x ? "Added!" : "Failed to add!"));
        } catch (Exception e) {
            return Mono.just(new GenericResponse(false, e.getMessage()));
        }
    }

    //endregion

    //region Opt-Out

    @GetMapping(value = "/{guildId}/opt-out/check", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<GenericResponse> checkOptOut(@RequestHeader(name = "X-Session-Authenticator") String sessionIdentifier, @PathVariable(name = "guildId") long guildId) {
        return guildService.checkOptOut(sessionIdentifier, guildId)
                .map(x -> new GenericResponse(x.isPresent(), x.orElse("Error")));
    }

    @GetMapping(value = "/{guildId}/opt-out", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<GenericResponse> optOut(@RequestHeader(name = "X-Session-Authenticator") String sessionIdentifier, @PathVariable(name = "guildId") long guildId) {
        return guildService.optOut(sessionIdentifier, guildId)
                .map(x -> new GenericResponse(x.isPresent(), x.orElse("Error")));
    }

    //endregion

    //region Tickets

    @GetMapping(value = "/{guildId}/tickets", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<GenericObjectResponse<TicketContainer>> retrieveTicket(@RequestHeader(name = "X-Session-Authenticator") String sessionIdentifier, @PathVariable(name = "guildId") long guildId) {
        return guildService.getTicket(sessionIdentifier, guildId)
                .map(x -> new GenericObjectResponse<>(x.isPresent(), x.orElse(null), x.isPresent() ? "Retrieved" : "Failed to retrieve!"));
    }

    @PostMapping(value = "/{guildId}/tickets/remove", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<GenericResponse> removeTicket(@RequestHeader(name = "X-Session-Authenticator") String sessionIdentifier, @PathVariable(name = "guildId") long guildId) {
        return guildService.removeTicket(sessionIdentifier, guildId)
                .map(x -> new GenericResponse(x, x ? "Removed!" : "Failed to remove"));
    }

    @PostMapping(value = "/{guildId}/tickets/add", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<GenericResponse> addTicket(@RequestHeader(name = "X-Session-Authenticator") String sessionIdentifier, @PathVariable(name = "guildId") long guildId, @RequestBody TicketsRequest request) {
        return guildService.updateTicket(sessionIdentifier, guildId, request.channelId(), request.logChannelId())
                .map(x -> new GenericResponse(x, x ? "Updated!" : "Failed to update"));
    }

    //endregion

    //region Suggestion

    @GetMapping(value = "/{guildId}/suggestions", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<GenericObjectResponse<ChannelContainer>> retrieveSuggestion(@RequestHeader(name = "X-Session-Authenticator") String sessionIdentifier, @PathVariable(name = "guildId") long guildId) {
        return guildService.getSuggestion(sessionIdentifier, guildId)
                .map(x -> new GenericObjectResponse<>(x.isPresent(), x.orElse(null), x.isPresent() ? "Retrieved" : "Failed to retrieve!"));
    }

    @PostMapping(value = "/{guildId}/suggestions/remove", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<GenericResponse> removeSuggestion(@RequestHeader(name = "X-Session-Authenticator") String sessionIdentifier, @PathVariable(name = "guildId") long guildId) {
        return guildService.removeSuggestion(sessionIdentifier, guildId)
                .map(x -> new GenericResponse(x, x ? "Removed!" : "Failed to remove"));
    }

    @PostMapping(value = "/{guildId}/suggestions/add", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<GenericResponse> addSuggestion(@RequestHeader(name = "X-Session-Authenticator") String sessionIdentifier, @PathVariable(name = "guildId") long guildId, @RequestBody GenericValueRequest request) {
        try {
            long channelId = ConverterUtil.convertStringToLong(request.value());

            if (channelId == -1) {
                return Mono.just(new GenericResponse(false, "Channel not found!"));
            }

            return guildService.updateSuggestion(sessionIdentifier, guildId, channelId)
                    .map(x -> new GenericResponse(x, x ? "Added!" : "Failed to add!"));
        } catch (Exception e) {
            return Mono.just(new GenericResponse(false, e.getMessage()));
        }
    }

    //endregion

    //region Warnings

    @GetMapping(value = "/{guildId}/warnings", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<GenericObjectResponse<List<WarningContainer>>> retrieveWarnings(@RequestHeader(name = "X-Session-Authenticator") String sessionIdentifier, @PathVariable(name = "guildId") long guildId) {
        return guildService.getWarnings(sessionIdentifier, guildId)
                .map(x -> new GenericObjectResponse<>(true, x, "Retrieved!"));
    }

    @PostMapping(value = "/{guildId}/warnings/add", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<GenericObjectResponse<WarningContainer>> addWarnings(@RequestHeader(name = "X-Session-Authenticator") String sessionIdentifier, @PathVariable(name = "guildId") long guildId, @RequestBody WarningsRequest request) {
        return guildService.addWarnings(sessionIdentifier, guildId, request.userId(), request.warnings())
                .map(x -> new GenericObjectResponse<>(x.isPresent(), x.orElse(null), x.isPresent() ? "Added" : "Failed to add"));
    }

    @PostMapping(value = "/{guildId}/warnings/remove", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<GenericObjectResponse<WarningContainer>> removeWarnings(@RequestHeader(name = "X-Session-Authenticator") String sessionIdentifier, @PathVariable(name = "guildId") long guildId, @RequestBody WarningsRequest request) {
        return guildService.removeWarnings(sessionIdentifier, guildId, request.userId(), request.warnings())
                .map(x -> new GenericObjectResponse<>(x.isPresent(), x.orElse(null), x.isPresent() ? "Removed" : "Failed to remove"));
    }

    @PostMapping(value = "/{guildId}/warnings/clear", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<GenericResponse> clearWarnings(@RequestHeader(name = "X-Session-Authenticator") String sessionIdentifier, @PathVariable(name = "guildId") long guildId) {
        return guildService.clearWarnings(sessionIdentifier, guildId)
                .map(x -> new GenericResponse(x, x ? "Cleared!" : "Failed to clear"));
    }

    //region Punishments

    @GetMapping(value = "/{guildId}/warnings/punishments", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<GenericObjectResponse<List<PunishmentContainer>>> retrievePunishments(@RequestHeader(name = "X-Session-Authenticator") String sessionIdentifier, @PathVariable(name = "guildId") long guildId) {
        return guildService.getPunishments(sessionIdentifier, guildId)
                .map(x -> new GenericObjectResponse<>(true, x, "Retrieved!"));
    }

    @PostMapping(value = "/{guildId}/warnings/punishments/clear", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<GenericResponse> clearPunishments(@RequestHeader(name = "X-Session-Authenticator") String sessionIdentifier, @PathVariable(name = "guildId") long guildId) {
        return guildService.clearPunishments(sessionIdentifier, guildId)
                .map(x -> new GenericResponse(x, x ? "Cleared!" : "Failed to clear"));
    }

    @PostMapping(value = "/{guildId}/warnings/punishments/add", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<GenericObjectResponse<PunishmentContainer>> addPunishments(@RequestHeader(name = "X-Session-Authenticator") String sessionIdentifier, @PathVariable(name = "guildId") long guildId, @RequestBody PunishmentsRequest request) {
        return guildService.addPunishments(sessionIdentifier, guildId, request.neededWarnings(), request.action(), request.timeoutTime(), request.roleId())
                .map(x -> new GenericObjectResponse<>(x.isPresent(), x.orElse(null), x.isPresent() ? "Added!" : "Failed to add!"));
    }

    @PostMapping(value = "/{guildId}/warnings/punishments/remove", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<GenericResponse> removePunishments(@RequestHeader(name = "X-Session-Authenticator") String sessionIdentifier, @PathVariable(name = "guildId") long guildId, @RequestBody GenericValueRequest request) {
        return guildService.removePunishments(sessionIdentifier, guildId, request.value())
                .map(x -> new GenericResponse(x, x ? "Removed!" : "Failed to remove"));
    }

    //endregion

    //endregion

    //region Custom commands

    @GetMapping(value = "/{guildId}/commands", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<GenericObjectResponse<List<CustomCommandContainer>>> retrieveCustomCommands(@RequestHeader(name = "X-Session-Authenticator") String sessionIdentifier, @PathVariable(name = "guildId") long guildId) {
        return guildService.getCustomCommand(sessionIdentifier, guildId)
                .map(x -> new GenericObjectResponse<>(true, x, "Retrieved!"));
    }


    @PostMapping(value = "/{guildId}/commands/add", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<GenericObjectResponse<CustomCommandContainer>> addCustomCommand(@RequestHeader(name = "X-Session-Authenticator") String sessionIdentifier, @PathVariable(name = "guildId") long guildId, @RequestBody CustomCommandRequest request) {
        return guildService.addCustomCommand(sessionIdentifier, guildId, request.name(), request.channelId(), request.message(), request.embedJson())
                .map(x -> new GenericObjectResponse<>(x.isPresent(), x.orElse(null), x.isPresent() ? "Retrieved!" : "Could not retrieve!"));
    }


    @PostMapping(value = "/{guildId}/commands/remove", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<GenericResponse> removeCustomCommand(@RequestHeader(name = "X-Session-Authenticator") String sessionIdentifier, @PathVariable(name = "guildId") long guildId, @RequestBody GenericValueRequest request) {
        return guildService.removeCustomCommand(sessionIdentifier, guildId, request.value())
                .map(x -> x ? new GenericResponse(true, "Removed!") : new GenericResponse(false, "Failed to remove!"));
    }

    //endregion

    //region Reaction Roles

    @GetMapping(value = "/{guildId}/reactionroles", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<GenericObjectResponse<List<MessageReactionRoleContainer>>> retrieveReactionRoles(@RequestHeader(name = "X-Session-Authenticator") String sessionIdentifier, @PathVariable(name = "guildId") long guildId) {
        return guildService.retrieveReactionRoles(sessionIdentifier, guildId)
                .map(x -> new GenericObjectResponse<>(true, x, "Retrieved!"));
    }

    @PostMapping(value = "/{guildId}/reactionroles/add", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<GenericResponse> addReactionRole(@RequestHeader(name = "X-Session-Authenticator") String sessionIdentifier, @PathVariable(name = "guildId") long guildId, @RequestBody ReactionRoleRequest request) {
        return guildService.addReactionRole(sessionIdentifier, guildId, request.emojiId(), request.formattedEmoji(), request.channelId(), request.messageId(), request.roleId())
                .map(x -> x ? new GenericResponse(true, "Added!") : new GenericResponse(false, "Failed to add!"));
    }

    @PostMapping(value = "/{guildId}/reactionroles/remove", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<GenericResponse> removeReactionRole(@RequestHeader(name = "X-Session-Authenticator") String sessionIdentifier, @PathVariable(name = "guildId") long guildId, @RequestBody ReactionRoleDeleteRequest request) {
        return guildService.removeReactionRole(sessionIdentifier, guildId, request.emojiId(), request.messageId())
                .map(x -> x ? new GenericResponse(true, "Removed!") : new GenericResponse(false, "Failed to remove!"));
    }

    //endregion

}
