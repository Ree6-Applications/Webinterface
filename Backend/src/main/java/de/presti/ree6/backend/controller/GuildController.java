package de.presti.ree6.backend.controller;


import de.presti.ree6.backend.bot.BotWorker;
import de.presti.ree6.backend.service.GuildService;
import de.presti.ree6.backend.service.SessionService;
import de.presti.ree6.backend.utils.data.container.*;
import de.presti.ree6.backend.utils.data.container.api.*;
import de.presti.ree6.backend.utils.data.container.guild.GuildContainer;
import de.presti.ree6.backend.utils.data.container.guild.GuildStatsContainer;
import de.presti.ree6.backend.utils.data.container.role.RoleContainer;
import de.presti.ree6.backend.utils.data.container.role.RoleLevelContainer;
import de.presti.ree6.backend.utils.data.container.user.UserContainer;
import de.presti.ree6.backend.utils.data.container.user.UserLevelContainer;
import de.presti.ree6.sql.SQLSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

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
    public GenericObjectResponse<List<GuildContainer>> retrieveGuilds(@RequestHeader(name = "X-Session-Authenticator") String sessionIdentifier) {
        try {
            return new GenericObjectResponse<>(true, sessionService.retrieveGuilds(sessionIdentifier), "Guilds retrieved!");
        } catch (Exception e) {
            return new GenericObjectResponse<>(false, null, e.getMessage());
        }
    }

    @GetMapping(value = "/{guildId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public GenericObjectResponse<GuildContainer> retrieveGuild(@RequestHeader(name = "X-Session-Authenticator") String sessionIdentifier, @PathVariable(name = "guildId") String guildId) {
        try {
            return new GenericObjectResponse<>(true, sessionService.retrieveGuild(sessionIdentifier, guildId, true, true), "Guild retrieved!");
        } catch (Exception e) {
            return new GenericObjectResponse<>(false, null, e.getMessage());
        }
    }

    //endregion

    //region Guild Channel and Role

    @GetMapping(value = "/{guildId}/channels", produces = MediaType.APPLICATION_JSON_VALUE)
    public GenericObjectResponse<List<ChannelContainer>> retrieveGuildChannels(@RequestHeader(name = "X-Session-Authenticator") String sessionIdentifier, @PathVariable(name = "guildId") String guildId) {
        try {
            return new GenericObjectResponse<>(true, sessionService.retrieveGuild(sessionIdentifier, guildId, true).getChannels(), "Channels retrieved!");
        } catch (Exception e) {
            return new GenericObjectResponse<>(false, null, e.getMessage());
        }
    }

    @GetMapping(value = "/{guildId}/roles", produces = MediaType.APPLICATION_JSON_VALUE)
    public GenericObjectResponse<List<RoleContainer>> retrieveGuildRoles(@RequestHeader(name = "X-Session-Authenticator") String sessionIdentifier, @PathVariable(name = "guildId") String guildId) {
        try {
            return new GenericObjectResponse<>(true, sessionService.retrieveGuild(sessionIdentifier, guildId, false, true).getRoles(), "Roles retrieved!");
        } catch (Exception e) {
            return new GenericObjectResponse<>(false, null, e.getMessage());
        }
    }

    //endregion

    //region Guild Blacklist

    @GetMapping(value = "/{guildId}/blacklist", produces = MediaType.APPLICATION_JSON_VALUE)
    public GenericObjectResponse<List<String>> retrieveGuildBlacklist(@RequestHeader(name = "X-Session-Authenticator") String sessionIdentifier, @PathVariable(name = "guildId") String guildId) {
        try {
            GuildContainer guildContainer = sessionService.retrieveGuild(sessionIdentifier, guildId);
            List<String> blacklist = SQLSession.getSqlConnector().getSqlWorker().getChatProtectorWords(guildId);
            return new GenericObjectResponse<>(true, blacklist, "Blacklist retrieved!");
        } catch (Exception e) {
            return new GenericObjectResponse<>(false, Collections.emptyList(), e.getMessage());
        }
    }

    @PostMapping(value = "/{guildId}/blacklist/remove", produces = MediaType.APPLICATION_JSON_VALUE)
    public GenericResponse removeGuildBlacklist(@RequestHeader(name = "X-Session-Authenticator") String sessionIdentifier, @PathVariable(name = "guildId") String guildId, @RequestBody GenericValueRequest request) {
        try {
            GuildContainer guildContainer = sessionService.retrieveGuild(sessionIdentifier, guildId);

            SQLSession.getSqlConnector().getSqlWorker().removeChatProtectorWord(guildId, request.value());
            return new GenericResponse(true, "Blacklist removed!");
        } catch (Exception e) {
            return new GenericResponse(false, e.getMessage());
        }
    }

    @PostMapping(value = "/{guildId}/blacklist/add", produces = MediaType.APPLICATION_JSON_VALUE)
    public GenericResponse addGuildBlacklist(@RequestHeader(name = "X-Session-Authenticator") String sessionIdentifier, @PathVariable(name = "guildId") String guildId, @RequestBody GenericValueRequest request) {
        try {
            GuildContainer guildContainer = sessionService.retrieveGuild(sessionIdentifier, guildId);

            if (!SQLSession.getSqlConnector().getSqlWorker().isChatProtectorSetup(guildId, request.value())) {
                SQLSession.getSqlConnector().getSqlWorker().addChatProtectorWord(guildId, request.value());
                return new GenericResponse(true, "Blacklist added!");
            } else {
                return new GenericResponse(false, "Word already blacklisted!");
            }
        } catch (Exception e) {
            return new GenericResponse(false, e.getMessage());
        }
    }

    //endregion

    //region Guild AutoRole

    @GetMapping(value = "/{guildId}/autorole", produces = MediaType.APPLICATION_JSON_VALUE)
    public GenericObjectResponse<List<RoleContainer>> retrieveGuildAutoRole(@RequestHeader(name = "X-Session-Authenticator") String sessionIdentifier, @PathVariable(name = "guildId") String guildId) {
        try {
            GuildContainer guildContainer = sessionService.retrieveGuild(sessionIdentifier, guildId, false, true);
            List<RoleContainer> autoRoles = SQLSession.getSqlConnector().getSqlWorker().getAutoRoles(guildId).stream().map(c -> guildContainer.getRoleById(c.getRoleId())).filter(Objects::nonNull).toList();
            return new GenericObjectResponse<>(true, autoRoles, "AutoRole retrieved!");
        } catch (Exception e) {
            return new GenericObjectResponse<>(false, Collections.emptyList(), e.getMessage());
        }
    }

    @PostMapping(value = "/{guildId}/autorole/remove", produces = MediaType.APPLICATION_JSON_VALUE)
    public GenericResponse removeGuildAutoRole(@RequestHeader(name = "X-Session-Authenticator") String sessionIdentifier, @PathVariable(name = "guildId") String guildId, @RequestBody GenericValueRequest request) {
        try {
            GuildContainer guildContainer = sessionService.retrieveGuild(sessionIdentifier, guildId);
            SQLSession.getSqlConnector().getSqlWorker().removeAutoRole(guildId, request.value());
            return new GenericResponse(true, "AutoRole removed!");
        } catch (Exception e) {
            return new GenericResponse(false, e.getMessage());
        }
    }

    @PostMapping(value = "/{guildId}/autorole/add", produces = MediaType.APPLICATION_JSON_VALUE)
    public GenericResponse addGuildAutoRole(@RequestHeader(name = "X-Session-Authenticator") String sessionIdentifier, @PathVariable(name = "guildId") String guildId, @RequestBody GenericValueRequest request) {
        try {
            GuildContainer guildContainer = sessionService.retrieveGuild(sessionIdentifier, guildId, false, true);

            if (guildContainer.getRoleById(request.value()) == null)
                throw new IllegalAccessException("Role not found!");

            if (!SQLSession.getSqlConnector().getSqlWorker().isAutoRoleSetup(guildId, request.value())) {
                SQLSession.getSqlConnector().getSqlWorker().addAutoRole(guildId, request.value());
                return new GenericResponse(true, "AutoRole added!");
            } else {
                return new GenericResponse(false, "Role is already in AutoRole!");
            }
        } catch (Exception e) {
            return new GenericResponse(false, e.getMessage());
        }
    }

    //endregion

    //region Guild Leaderboard

    @GetMapping(value = "/{guildId}/leaderboard/voice", produces = MediaType.APPLICATION_JSON_VALUE)
    public GenericObjectResponse<LeaderboardContainer> retrieveLeaderboardVoice(@PathVariable(name = "guildId") String guildId) {
        try {
            // Call this to check if guild exists. If not exception is thrown.
            GuildContainer guildContainer = sessionService.retrieveGuild(guildId);

            LeaderboardContainer leaderboardContainer = new LeaderboardContainer();

            leaderboardContainer.setVoiceLeaderboard(SQLSession.getSqlConnector().getSqlWorker().getTopVoice(guildId, 5).stream().map(c -> new UserLevelContainer(c, new UserContainer(BotWorker.getShardManager().retrieveUserById(c.getUserId()).complete()))).toList());

            leaderboardContainer.setGuildId(guildContainer.getId());

            return new GenericObjectResponse<>(true, leaderboardContainer, "Leaderboard retrieved!");
        } catch (Exception e) {
            return new GenericObjectResponse<>(false, null, e.getMessage());
        }
    }

    @GetMapping(value = "/{guildId}/leaderboard/chat", produces = MediaType.APPLICATION_JSON_VALUE)
    public GenericObjectResponse<LeaderboardContainer> retrieveLeaderboardChat(@PathVariable(name = "guildId") String guildId) {
        try {
            // Call this to check if guild exists. If not exception is thrown.
            GuildContainer guildContainer = sessionService.retrieveGuild(guildId);

            LeaderboardContainer leaderboardContainer = new LeaderboardContainer();

            leaderboardContainer.setChatLeaderboard(SQLSession.getSqlConnector().getSqlWorker().getTopChat(guildId, 5).stream().map(c -> new UserLevelContainer(c, new UserContainer(BotWorker.getShardManager().retrieveUserById(c.getUserId()).complete()))).toList());

            leaderboardContainer.setGuildId(guildContainer.getId());

            return new GenericObjectResponse<>(true, leaderboardContainer, "Leaderboard retrieved!");
        } catch (Exception e) {
            return new GenericObjectResponse<>(false, null, e.getMessage());
        }
    }

    //endregion

    // region Guild Stats

    @GetMapping(value = "/{guildId}/stats", produces = MediaType.APPLICATION_JSON_VALUE)
    public GenericObjectResponse<GuildStatsContainer> retrieveStats(@RequestHeader(name = "X-Session-Authenticator") String sessionIdentifier, @PathVariable(name = "guildId") String guildId) {
        try {
            return new GenericObjectResponse<>(true, guildService.getStats(sessionIdentifier, guildId), "Stats retrieved!");
        } catch (Exception e) {
            return new GenericObjectResponse<>(false, null, e.getMessage());
        }
    }

    //endregion

    // region Guild Chat Autorole

    @GetMapping(value = "/{guildId}/chatrole", produces = MediaType.APPLICATION_JSON_VALUE)
    public GenericObjectResponse<List<RoleLevelContainer>> retrieveChatRoles(@RequestHeader(name = "X-Session-Authenticator") String sessionIdentifier, @PathVariable(name = "guildId") String guildId) {
        try {
            return new GenericObjectResponse<>(true, guildService.getChatAutoRoles(sessionIdentifier, guildId), "Chat Autorole retrieved!");
        } catch (Exception e) {
            return new GenericObjectResponse<>(false, null, e.getMessage());
        }
    }

    @PostMapping(value = "/{guildId}/chatrole/remove", produces = MediaType.APPLICATION_JSON_VALUE)
    public GenericResponse removeChatAutoRole(@RequestHeader(name = "X-Session-Authenticator") String sessionIdentifier, @PathVariable(name = "guildId") String guildId, @RequestBody GenericValueRequest valueRequest) {
        try {
            long level = Long.parseLong(valueRequest.value());
            guildService.removeChatAutoRole(sessionIdentifier, guildId, level);
            return new GenericResponse(true, "Chat Auto-role removed!");
        } catch (Exception e) {
            return new GenericResponse(false, e.getMessage());
        }
    }

    @PostMapping(value = "/{guildId}/chatrole/add", produces = MediaType.APPLICATION_JSON_VALUE)
    public GenericResponse addChatAutoRole(@RequestHeader(name = "X-Session-Authenticator") String sessionIdentifier, @PathVariable(name = "guildId") String guildId, @RequestBody LevelAutoRoleRequest levelAutoRoleRequest) {
        try {
            guildService.addChatAutoRole(sessionIdentifier, guildId, levelAutoRoleRequest.roleId(), levelAutoRoleRequest.level());
            return new GenericResponse(true, "Chat Auto-role added!");
        } catch (Exception e) {
            return new GenericResponse(false, e.getMessage());
        }
    }

    //endregion

    // region Guild Voice Autorole

    @GetMapping(value = "/{guildId}/voicerole", produces = MediaType.APPLICATION_JSON_VALUE)
    public GenericObjectResponse<List<RoleLevelContainer>> retrieveVoiceRoles(@RequestHeader(name = "X-Session-Authenticator") String sessionIdentifier, @PathVariable(name = "guildId") String guildId) {
        try {
            return new GenericObjectResponse<>(true, guildService.getVoiceAutoRoles(sessionIdentifier, guildId), "Voice Autorole retrieved!");
        } catch (Exception e) {
            return new GenericObjectResponse<>(false, null, e.getMessage());
        }
    }

    @PostMapping(value = "/{guildId}/voicerole/remove", produces = MediaType.APPLICATION_JSON_VALUE)
    public GenericResponse removeVoiceAutoRole(@RequestHeader(name = "X-Session-Authenticator") String sessionIdentifier, @PathVariable(name = "guildId") String guildId, @RequestBody GenericValueRequest valueRequest) {
        try {
            long level = Long.parseLong(valueRequest.value());
            guildService.removeVoiceAutoRole(sessionIdentifier, guildId, level);
            return new GenericResponse(true, "Voice Auto-role removed!");
        } catch (Exception e) {
            return new GenericResponse(false, e.getMessage());
        }
    }

    @PostMapping(value = "/{guildId}/voicerole/add", produces = MediaType.APPLICATION_JSON_VALUE)
    public GenericResponse addVoiceAutoRole(@RequestHeader(name = "X-Session-Authenticator") String sessionIdentifier, @PathVariable(name = "guildId") String guildId, @RequestBody LevelAutoRoleRequest levelAutoRoleRequest) {
        try {
            guildService.addVoiceAutoRole(sessionIdentifier, guildId, levelAutoRoleRequest.roleId(), levelAutoRoleRequest.level());
            return new GenericResponse(true, "Voice Auto-role added!");
        } catch (Exception e) {
            return new GenericResponse(false, e.getMessage());
        }
    }

    //endregion

    //region Recording

    @GetMapping(value = "/recording", produces = MediaType.APPLICATION_JSON_VALUE)
    public GenericObjectResponse<RecordContainer> retrieveRecording(@RequestHeader(name = "X-Session-Authenticator") String sessionIdentifier, @RequestParam(name = "recordId") String recordId) {
        try {
            return new GenericObjectResponse<>(true, guildService.getRecordingContainer(sessionIdentifier, recordId), "Recording retrieved!");
        } catch (Exception e) {
            return new GenericObjectResponse<>(false, null, e.getMessage());
        }
    }

    @GetMapping(value = "/recording/download", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Resource> downloadRecording(@RequestHeader(name = "X-Session-Authenticator") String sessionIdentifier, @RequestParam(name = "recordId") String recordId) {
        try {

            ByteArrayResource resource = new ByteArrayResource(guildService.getRecordingBytes(sessionIdentifier, recordId));
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .contentLength(resource.contentLength())
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            ContentDisposition.attachment()
                                    .filename("recording.wav")
                                    .build().toString())
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(null);
        }
    }

    //endregion

    //region Guild Welcome Channel

    @GetMapping(value = "/{guildId}/welcome", produces = MediaType.APPLICATION_JSON_VALUE)
    public GenericObjectResponse<ChannelContainer> retrieveWelcomeChannel(@RequestHeader(name = "X-Session-Authenticator") String sessionIdentifier, @PathVariable(name = "guildId") String guildId) {
        try {
            return new GenericObjectResponse<>(true, guildService.getWelcomeChannel(sessionIdentifier, guildId), "Welcome channel retrieved!");
        } catch (Exception e) {
            return new GenericObjectResponse<>(false, null, e.getMessage());
        }
    }

    @PostMapping(value = "/{guildId}/welcome/remove", produces = MediaType.APPLICATION_JSON_VALUE)
    public GenericResponse removeWelcomeChannel(@RequestHeader(name = "X-Session-Authenticator") String sessionIdentifier, @PathVariable(name = "guildId") String guildId) {
        try {
            SQLSession.getSqlConnector().getSqlWorker().deleteEntity(guildService.removeWelcomeChannel(sessionIdentifier, guildId));
            return new GenericResponse(true, "Welcome channel removed!");
        } catch (Exception e) {
            return new GenericResponse(false, e.getMessage());
        }
    }

    @PostMapping(value = "/{guildId}/welcome/add", produces = MediaType.APPLICATION_JSON_VALUE)
    public GenericResponse addWelcomeChannel(@RequestHeader(name = "X-Session-Authenticator") String sessionIdentifier, @PathVariable(name = "guildId") String guildId, @RequestBody GenericValueRequest request) {
        try {
            guildService.updateWelcomeChannel(sessionIdentifier, guildId, request.value());
            return new GenericResponse(true, "Welcome channel added!");
        } catch (Exception e) {
            return new GenericResponse(false, e.getMessage());
        }
    }

    //endregion

    //region Guild Log Channel

    @GetMapping(value = "/{guildId}/log", produces = MediaType.APPLICATION_JSON_VALUE)
    public GenericObjectResponse<ChannelContainer> retrieveLogChannel(@RequestHeader(name = "X-Session-Authenticator") String sessionIdentifier, @PathVariable(name = "guildId") String guildId) {
        try {
            return new GenericObjectResponse<>(true, guildService.getLogChannel(sessionIdentifier, guildId), "Log channel retrieved!");
        } catch (Exception e) {
            return new GenericObjectResponse<>(false, null, e.getMessage());
        }
    }

    @PostMapping(value = "/{guildId}/log/remove", produces = MediaType.APPLICATION_JSON_VALUE)
    public GenericResponse removeLogChannel(@RequestHeader(name = "X-Session-Authenticator") String sessionIdentifier, @PathVariable(name = "guildId") String guildId) {
        try {
            SQLSession.getSqlConnector().getSqlWorker().deleteEntity(guildService.removeLogChannel(sessionIdentifier, guildId));
            return new GenericResponse(true, "Log channel removed!");
        } catch (Exception e) {
            return new GenericResponse(false, e.getMessage());
        }
    }

    @PostMapping(value = "/{guildId}/log/add", produces = MediaType.APPLICATION_JSON_VALUE)
    public GenericResponse addLogChannel(@RequestHeader(name = "X-Session-Authenticator") String sessionIdentifier, @PathVariable(name = "guildId") String guildId, @RequestBody GenericValueRequest request) {
        try {
            guildService.updateLogChannel(sessionIdentifier, guildId, request.value());
            return new GenericResponse(true, "Log channel added!");
        } catch (Exception e) {
            return new GenericResponse(false, e.getMessage());
        }
    }

    //endregion

    //region Notifier

    //region Reddit Notifier

    @GetMapping(value = "/{guildId}/reddit", produces = MediaType.APPLICATION_JSON_VALUE)
    public GenericObjectResponse<List<NotifierContainer>> retrieveRedditNotifier(@RequestHeader(name = "X-Session-Authenticator") String sessionIdentifier, @PathVariable(name = "guildId") String guildId) {
        try {
            return new GenericObjectResponse<>(true, guildService.getRedditNotifier(sessionIdentifier, guildId), "Reddit Notifiers retrieved!");
        } catch (Exception e) {
            return new GenericObjectResponse<>(false, null, e.getMessage());
        }
    }

    @PostMapping(value = "/{guildId}/reddit/remove", produces = MediaType.APPLICATION_JSON_VALUE)
    public GenericResponse removeRedditNotifier(@RequestHeader(name = "X-Session-Authenticator") String sessionIdentifier, @PathVariable(name = "guildId") String guildId, @RequestBody GenericValueRequest request) {
        try {
            guildService.removeRedditNotifier(sessionIdentifier, guildId, request.value());
            return new GenericResponse(true, "Reddit Notifier removed!");
        } catch (Exception e) {
            return new GenericResponse(false, e.getMessage());
        }
    }

    @PostMapping(value = "/{guildId}/reddit/add", produces = MediaType.APPLICATION_JSON_VALUE)
    public GenericResponse addRedditNotifier(@RequestHeader(name = "X-Session-Authenticator") String sessionIdentifier, @PathVariable(name = "guildId") String guildId, @RequestBody GenericNotifierRequest notifierRequestObject) {
        try {
            guildService.addRedditNotifier(sessionIdentifier, guildId, notifierRequestObject);
            return new GenericResponse(true, "Reddit Notifier added!");
        } catch (Exception e) {
            return new GenericResponse(false, e.getMessage());
        }
    }

    //endregion

    //region Twitch Notifier

    @GetMapping(value = "/{guildId}/twitch", produces = MediaType.APPLICATION_JSON_VALUE)
    public GenericObjectResponse<List<NotifierContainer>> retrieveTwitchNotifier(@RequestHeader(name = "X-Session-Authenticator") String sessionIdentifier, @PathVariable(name = "guildId") String guildId) {
        try {
            return new GenericObjectResponse<>(true, guildService.getTwitchNotifier(sessionIdentifier, guildId), "Twitch Notifiers retrieved!");
        } catch (Exception e) {
            return new GenericObjectResponse<>(false, null, e.getMessage());
        }
    }

    @PostMapping(value = "/{guildId}/twitch/remove", produces = MediaType.APPLICATION_JSON_VALUE)
    public GenericResponse removeTwitchNotifier(@RequestHeader(name = "X-Session-Authenticator") String sessionIdentifier, @PathVariable(name = "guildId") String guildId, @RequestBody GenericValueRequest request) {
        try {
            guildService.removeTwitchNotifier(sessionIdentifier, guildId, request.value());
            return new GenericResponse(true, "Twitch Notifier removed!");
        } catch (Exception e) {
            return new GenericResponse(false, e.getMessage());
        }
    }

    @PostMapping(value = "/{guildId}/twitch/add", produces = MediaType.APPLICATION_JSON_VALUE)
    public GenericResponse addTwitchNotifier(@RequestHeader(name = "X-Session-Authenticator") String sessionIdentifier, @PathVariable(name = "guildId") String guildId, @RequestBody GenericNotifierRequest notifierRequestObject) {
        try {
            guildService.addTwitchNotifier(sessionIdentifier, guildId, notifierRequestObject);
            return new GenericResponse(true, "Twitch Notifier added!");
        } catch (Exception e) {
            return new GenericResponse(false, e.getMessage());
        }
    }


    //endregion

    //region Twitter Notifier

    @GetMapping(value = "/{guildId}/twitter", produces = MediaType.APPLICATION_JSON_VALUE)
    public GenericObjectResponse<List<NotifierContainer>> retrieveTwitterNotifier(@RequestHeader(name = "X-Session-Authenticator") String sessionIdentifier, @PathVariable(name = "guildId") String guildId) {
        try {
            return new GenericObjectResponse<>(true, guildService.getTwitterNotifier(sessionIdentifier, guildId), "Twitter Notifiers retrieved!");
        } catch (Exception e) {
            return new GenericObjectResponse<>(false, null, e.getMessage());
        }
    }

    @PostMapping(value = "/{guildId}/twitter/remove", produces = MediaType.APPLICATION_JSON_VALUE)
    public GenericResponse removeTwitterNotifier(@RequestHeader(name = "X-Session-Authenticator") String sessionIdentifier, @PathVariable(name = "guildId") String guildId, @RequestBody GenericValueRequest request) {
        try {
            guildService.removeTwitterNotifier(sessionIdentifier, guildId, request.value());
            return new GenericResponse(true, "Twitter Notifier removed!");
        } catch (Exception e) {
            return new GenericResponse(false, e.getMessage());
        }
    }

    @PostMapping(value = "/{guildId}/twitter/add", produces = MediaType.APPLICATION_JSON_VALUE)
    public GenericResponse addTwitterNotifier(@RequestHeader(name = "X-Session-Authenticator") String sessionIdentifier, @PathVariable(name = "guildId") String guildId, @RequestBody GenericNotifierRequest notifierRequestObject) {
        try {
            guildService.addTwitterNotifier(sessionIdentifier, guildId, notifierRequestObject);
            return new GenericResponse(true, "Twitter Notifier added!");
        } catch (Exception e) {
            return new GenericResponse(false, e.getMessage());
        }
    }

    //endregion

    //region YouTube Notifier

    @GetMapping(value = "/{guildId}/youtube", produces = MediaType.APPLICATION_JSON_VALUE)
    public GenericObjectResponse<List<NotifierContainer>> retrieveYoutubeNotifier(@RequestHeader(name = "X-Session-Authenticator") String sessionIdentifier, @PathVariable(name = "guildId") String guildId) {
        try {
            return new GenericObjectResponse<>(true, guildService.getYouTubeNotifier(sessionIdentifier, guildId), "Youtube Notifiers retrieved!");
        } catch (Exception e) {
            return new GenericObjectResponse<>(false, null, e.getMessage());
        }
    }

    @PostMapping(value = "/{guildId}/youtube/remove", produces = MediaType.APPLICATION_JSON_VALUE)
    public GenericResponse removeYoutubeNotifier(@RequestHeader(name = "X-Session-Authenticator") String sessionIdentifier, @PathVariable(name = "guildId") String guildId, @RequestBody GenericValueRequest request) {
        try {
            guildService.removeYouTubeNotifier(sessionIdentifier, guildId, request.value());
            return new GenericResponse(true, "Youtube Notifier removed!");
        } catch (Exception e) {
            return new GenericResponse(false, e.getMessage());
        }
    }

    @PostMapping(value = "/{guildId}/youtube/add", produces = MediaType.APPLICATION_JSON_VALUE)
    public GenericResponse addYoutubeNotifier(@RequestHeader(name = "X-Session-Authenticator") String sessionIdentifier, @PathVariable(name = "guildId") String guildId, @RequestBody GenericNotifierRequest notifierRequestObject) {
        try {
            guildService.addYouTubeNotifier(sessionIdentifier, guildId, notifierRequestObject);
            return new GenericResponse(true, "Youtube Notifier added!");
        } catch (Exception e) {
            return new GenericResponse(false, e.getMessage());
        }
    }

    //endregion

    //region Instagram Notifier

    @GetMapping(value = "/{guildId}/instagram", produces = MediaType.APPLICATION_JSON_VALUE)
    public GenericObjectResponse<List<NotifierContainer>> retrieveInstagramNotifier(@RequestHeader(name = "X-Session-Authenticator") String sessionIdentifier, @PathVariable(name = "guildId") String guildId) {
        try {
            return new GenericObjectResponse<>(true, guildService.getInstagramNotifier(sessionIdentifier, guildId), "Instagram Notifiers retrieved!");
        } catch (Exception e) {
            return new GenericObjectResponse<>(false, null, e.getMessage());
        }
    }

    @PostMapping(value = "/{guildId}/instagram/remove", produces = MediaType.APPLICATION_JSON_VALUE)
    public GenericResponse removeInstagramNotifier(@RequestHeader(name = "X-Session-Authenticator") String sessionIdentifier, @PathVariable(name = "guildId") String guildId, @RequestBody GenericValueRequest request) {
        try {
            guildService.removeInstagramNotifier(sessionIdentifier, guildId, request.value());
            return new GenericResponse(true, "Instagram Notifier removed!");
        } catch (Exception e) {
            return new GenericResponse(false, e.getMessage());
        }
    }

    @PostMapping(value = "/{guildId}/instagram/add", produces = MediaType.APPLICATION_JSON_VALUE)
    public GenericResponse addInstagramNotifier(@RequestHeader(name = "X-Session-Authenticator") String sessionIdentifier, @PathVariable(name = "guildId") String guildId, @RequestBody GenericNotifierRequest notifierRequestObject) {
        try {
            guildService.addInstagramNotifier(sessionIdentifier, guildId, notifierRequestObject);
            return new GenericResponse(true, "Instagram Notifier added!");
        } catch (Exception e) {
            return new GenericResponse(false, e.getMessage());
        }
    }

    //endregion

    //endregion

    //region Temporal Voice Channels

    @GetMapping(value = "/{guildId}/temporalvoice", produces = MediaType.APPLICATION_JSON_VALUE)
    public GenericObjectResponse<ChannelContainer> retrieveTemporalVoice(@RequestHeader(name = "X-Session-Authenticator") String sessionIdentifier, @PathVariable(name = "guildId") String guildId) {
        try {
            return new GenericObjectResponse<>(true, guildService.getTemporalVoice(sessionIdentifier, guildId), "TemporalVoice channel retrieved!");
        } catch (Exception e) {
            return new GenericObjectResponse<>(false, null, e.getMessage());
        }
    }


    @PostMapping(value = "/{guildId}/temporalvoice/remove", produces = MediaType.APPLICATION_JSON_VALUE)
    public GenericResponse removeTemporalVoiceChannel(@RequestHeader(name = "X-Session-Authenticator") String sessionIdentifier, @PathVariable(name = "guildId") String guildId) {
        try {
            guildService.removeTemporalVoice(sessionIdentifier, guildId);
            return new GenericResponse(true, "TemporalVoice channel removed!");
        } catch (Exception e) {
            return new GenericResponse(false, e.getMessage());
        }
    }


    @PostMapping(value = "/{guildId}/temporalvoice/add", produces = MediaType.APPLICATION_JSON_VALUE)
    public GenericResponse addTemporalVoiceChannel(@RequestHeader(name = "X-Session-Authenticator") String sessionIdentifier, @PathVariable(name = "guildId") String guildId, @RequestBody GenericValueRequest request) {
        try {
            guildService.updateTemporalVoice(sessionIdentifier, guildId, request.value());
            return new GenericResponse(true, "TemporalVoice channel added!");
        } catch (Exception e) {
            return new GenericResponse(false, e.getMessage());
        }
    }

    //endregion

    //region Opt-Out

    @GetMapping(value = "/{guildId}/opt-out/check", produces = MediaType.APPLICATION_JSON_VALUE)
    public GenericResponse checkOptOut(@RequestHeader(name = "X-Session-Authenticator") String sessionIdentifier, @PathVariable(name = "guildId") String guildId) {
        try {
            return new GenericResponse(true, guildService.checkOptOut(sessionIdentifier, guildId));
        } catch (Exception e) {
            return new GenericResponse(false, e.getMessage());
        }
    }

    @GetMapping(value = "/{guildId}/opt-out", produces = MediaType.APPLICATION_JSON_VALUE)
    public GenericResponse optOut(@RequestHeader(name = "X-Session-Authenticator") String sessionIdentifier, @PathVariable(name = "guildId") String guildId) {
        try {
            return new GenericResponse(true, guildService.optOut(sessionIdentifier, guildId));
        } catch (Exception e) {
            return new GenericResponse(false, e.getMessage());
        }
    }

    //endregion

    //region Tickets

    @GetMapping(value = "/{guildId}/tickets", produces = MediaType.APPLICATION_JSON_VALUE)
    public GenericObjectResponse<TicketContainer> retrieveTicket(@RequestHeader(name = "X-Session-Authenticator") String sessionIdentifier, @PathVariable(name = "guildId") String guildId) {
        try {
            return new GenericObjectResponse<>(true, guildService.getTicket(sessionIdentifier, guildId), "Tickets retrieved!");
        } catch (Exception e) {
            return new GenericObjectResponse<>(false, null, e.getMessage());
        }
    }

    @PostMapping(value = "/{guildId}/tickets/remove", produces = MediaType.APPLICATION_JSON_VALUE)
    public GenericResponse removeTicket(@RequestHeader(name = "X-Session-Authenticator") String sessionIdentifier, @PathVariable(name = "guildId") String guildId) {
        try {
            guildService.removeTicket(sessionIdentifier, guildId);
            return new GenericResponse(true, "Tickets removed!");
        } catch (Exception e) {
            return new GenericResponse(false, e.getMessage());
        }
    }

    @PostMapping(value = "/{guildId}/tickets/add", produces = MediaType.APPLICATION_JSON_VALUE)
    public GenericResponse addTicket(@RequestHeader(name = "X-Session-Authenticator") String sessionIdentifier, @PathVariable(name = "guildId") String guildId, @RequestBody TicketsRequest request) {
        try {
            guildService.updateTicket(sessionIdentifier, guildId, request.channelId(), request.logChannelId());
            return new GenericResponse(true, "Tickets added!");
        } catch (Exception e) {
            return new GenericResponse(false, e.getMessage());
        }
    }

    //endregion

    //region Suggestion

    @GetMapping(value = "/{guildId}/suggestions", produces = MediaType.APPLICATION_JSON_VALUE)
    public GenericObjectResponse<ChannelContainer> retrieveSuggestion(@RequestHeader(name = "X-Session-Authenticator") String sessionIdentifier, @PathVariable(name = "guildId") String guildId) {
        try {
            return new GenericObjectResponse<>(true, guildService.getSuggestion(sessionIdentifier, guildId), "Suggestions retrieved!");
        } catch (Exception e) {
            return new GenericObjectResponse<>(false, null, e.getMessage());
        }
    }

    @PostMapping(value = "/{guildId}/suggestions/remove", produces = MediaType.APPLICATION_JSON_VALUE)
    public GenericResponse removeSuggestion(@RequestHeader(name = "X-Session-Authenticator") String sessionIdentifier, @PathVariable(name = "guildId") String guildId) {
        try {
            guildService.removeSuggestion(sessionIdentifier, guildId);
            return new GenericResponse(true, "Suggestions removed!");
        } catch (Exception e) {
            return new GenericResponse(false, e.getMessage());
        }
    }

    @PostMapping(value = "/{guildId}/suggestions/add", produces = MediaType.APPLICATION_JSON_VALUE)
    public GenericResponse addSuggestion(@RequestHeader(name = "X-Session-Authenticator") String sessionIdentifier, @PathVariable(name = "guildId") String guildId, @RequestBody GenericValueRequest request) {
        try {
            guildService.updateSuggestion(sessionIdentifier, guildId, request.value());
            return new GenericResponse(true, "Suggestions added!");
        } catch (Exception e) {
            return new GenericResponse(false, e.getMessage());
        }
    }

    //endregion

    //region Warnings

    @GetMapping(value = "/{guildId}/warnings", produces = MediaType.APPLICATION_JSON_VALUE)
    public GenericObjectResponse<List<WarningContainer>> retrieveWarnings(@RequestHeader(name = "X-Session-Authenticator") String sessionIdentifier, @PathVariable(name = "guildId") String guildId) {
        try {
            return new GenericObjectResponse<>(true, guildService.getWarnings(sessionIdentifier, guildId), "Warnings retrieved!");
        } catch (Exception e) {
            return new GenericObjectResponse<>(false, null, e.getMessage());
        }
    }

    @PostMapping(value = "/{guildId}/warnings/add", produces = MediaType.APPLICATION_JSON_VALUE)
    public GenericObjectResponse<WarningContainer> addWarnings(@RequestHeader(name = "X-Session-Authenticator") String sessionIdentifier, @PathVariable(name = "guildId") String guildId, @RequestBody WarningsRequest request) {
        try {
            return new GenericObjectResponse<>(true, guildService.addWarnings(sessionIdentifier, guildId, request.userId(), request.warnings()), "Warnings added!");
        } catch (Exception e) {
            return new GenericObjectResponse<>(false, null, e.getMessage());
        }
    }

    @PostMapping(value = "/{guildId}/warnings/remove", produces = MediaType.APPLICATION_JSON_VALUE)
    public GenericObjectResponse<WarningContainer> removeWarnings(@RequestHeader(name = "X-Session-Authenticator") String sessionIdentifier, @PathVariable(name = "guildId") String guildId, @RequestBody WarningsRequest request) {
        try {
            return new GenericObjectResponse<>(true, guildService.removeWarnings(sessionIdentifier, guildId, request.userId(), request.warnings()), "Warnings removed!");
        } catch (Exception e) {
            return new GenericObjectResponse<>(false, null, e.getMessage());
        }
    }

    @PostMapping(value = "/{guildId}/warnings/clear", produces = MediaType.APPLICATION_JSON_VALUE)
    public GenericResponse clearWarnings(@RequestHeader(name = "X-Session-Authenticator") String sessionIdentifier, @PathVariable(name = "guildId") String guildId) {
        try {
            guildService.clearWarnings(sessionIdentifier, guildId);
            return new GenericResponse(true, "Warnings cleared!");
        } catch (Exception e) {
            return new GenericResponse(false, e.getMessage());
        }
    }

    //region Punishments

    @GetMapping(value = "/{guildId}/warnings/punishments", produces = MediaType.APPLICATION_JSON_VALUE)
    public GenericObjectResponse<List<PunishmentContainer>> retrievePunishments(@RequestHeader(name = "X-Session-Authenticator") String sessionIdentifier, @PathVariable(name = "guildId") String guildId) {
        try {
            return new GenericObjectResponse<>(true, guildService.getPunishments(sessionIdentifier, guildId), "Punishments retrieved!");
        } catch (Exception e) {
            return new GenericObjectResponse<>(false, null, e.getMessage());
        }
    }

    @PostMapping(value = "/{guildId}/warnings/punishments/clear", produces = MediaType.APPLICATION_JSON_VALUE)
    public GenericResponse clearPunishments(@RequestHeader(name = "X-Session-Authenticator") String sessionIdentifier, @PathVariable(name = "guildId") String guildId) {
        try {
            guildService.clearPunishments(sessionIdentifier, guildId);
            return new GenericResponse(true, "Punishments cleared!");
        } catch (Exception e) {
            return new GenericResponse(false, e.getMessage());
        }
    }

    @PostMapping(value = "/{guildId}/warnings/punishments/add", produces = MediaType.APPLICATION_JSON_VALUE)
    public GenericObjectResponse<PunishmentContainer> addPunishments(@RequestHeader(name = "X-Session-Authenticator") String sessionIdentifier, @PathVariable(name = "guildId") String guildId, @RequestBody PunishmentsRequest request) {
        try {
            return new GenericObjectResponse<>(true, guildService.addPunishments(sessionIdentifier, guildId, request.neededWarnings(), request.action(), request.timeoutTime(), request.roleId()), "Punishments added!");
        } catch (Exception e) {
            return new GenericObjectResponse<>(false, null, e.getMessage());
        }
    }

    @PostMapping(value = "/{guildId}/warnings/punishments/remove", produces = MediaType.APPLICATION_JSON_VALUE)
    public GenericResponse removePunishments(@RequestHeader(name = "X-Session-Authenticator") String sessionIdentifier, @PathVariable(name = "guildId") String guildId, @RequestBody GenericValueRequest request) {
        try {
            guildService.removePunishments(sessionIdentifier, guildId, request.value());
            return new GenericResponse(true, "Punishments removed!");
        } catch (Exception e) {
            return new GenericResponse(false, e.getMessage());
        }
    }

    //endregion

    //endregion

    //region Custom commands

    @GetMapping(value = "/{guildId}/commands", produces = MediaType.APPLICATION_JSON_VALUE)
    public GenericObjectResponse<List<CustomCommandContainer>> retrieveCustomCommands(@RequestHeader(name = "X-Session-Authenticator") String sessionIdentifier, @PathVariable(name = "guildId") String guildId) {
        try {
            return new GenericObjectResponse<>(true, guildService.getCustomCommand(sessionIdentifier, guildId), "CustomCommand retrieved!");
        } catch (Exception e) {
            return new GenericObjectResponse<>(false, null, e.getMessage());
        }
    }


    @PostMapping(value = "/{guildId}/commands/add", produces = MediaType.APPLICATION_JSON_VALUE)
    public GenericObjectResponse<CustomCommandContainer> addCustomCommand(@RequestHeader(name = "X-Session-Authenticator") String sessionIdentifier, @PathVariable(name = "guildId") String guildId, @RequestBody CustomCommandRequest request) {
        try {
            return new GenericObjectResponse<>(true, guildService.addCustomCommand(sessionIdentifier, guildId, request.name(), request.channelId(), request.message(), request.embedJson()), "CustomCommand added!");
        } catch (Exception e) {
            return new GenericObjectResponse<>(false, null, e.getMessage());
        }
    }


    @PostMapping(value = "/{guildId}/commands/remove", produces = MediaType.APPLICATION_JSON_VALUE)
    public GenericResponse removeCustomCommand(@RequestHeader(name = "X-Session-Authenticator") String sessionIdentifier, @PathVariable(name = "guildId") String guildId, @RequestBody GenericValueRequest request) {
        try {
            guildService.removeCustomCommand(sessionIdentifier, guildId, request.value());
            return new GenericResponse(true, "CustomCommand removed!");
        } catch (Exception e) {
            return new GenericResponse(false, e.getMessage());
        }
    }

    //endregion

    //region Reaction Roles

    @GetMapping(value = "/{guildId}/reactionroles", produces = MediaType.APPLICATION_JSON_VALUE)
    public GenericObjectResponse<List<MessageReactionRoleContainer>> retrieveReactionRoles(@RequestHeader(name = "X-Session-Authenticator") String sessionIdentifier, @PathVariable(name = "guildId") String guildId) {
        try {
            return new GenericObjectResponse<>(true, guildService.retrieveReactionRoles(sessionIdentifier, guildId), "ReactionRoles retrieved!");
        } catch (Exception e) {
            return new GenericObjectResponse<>(false, null, e.getMessage());
        }
    }

    @PostMapping(value = "/{guildId}/reactionroles/add", produces = MediaType.APPLICATION_JSON_VALUE)
    public GenericResponse addReactionRole(@RequestHeader(name = "X-Session-Authenticator") String sessionIdentifier, @PathVariable(name = "guildId") String guildId, @RequestBody ReactionRoleRequest request) {
        try {
            guildService.addReactionRole(sessionIdentifier, guildId, request.emojiId(), request.formattedEmoji(), request.channelId(), request.messageId(), request.roleId());
            return new GenericResponse(true,"ReactionRole added!");
        } catch (Exception e) {
            return new GenericResponse(false, e.getMessage());
        }
    }

    @PostMapping(value = "/{guildId}/reactionroles/remove", produces = MediaType.APPLICATION_JSON_VALUE)
    public GenericResponse removeReactionRole(@RequestHeader(name = "X-Session-Authenticator") String sessionIdentifier, @PathVariable(name = "guildId") String guildId, @RequestBody ReactionRoleDeleteRequest request) {
        try {
            guildService.removeReactionRole(sessionIdentifier, guildId, request.emojiId(), request.messageId());
            return new GenericResponse(true,"ReactionRole removed!");
        } catch (Exception e) {
            return new GenericResponse(false, e.getMessage());
        }
    }

    //endregion

}
