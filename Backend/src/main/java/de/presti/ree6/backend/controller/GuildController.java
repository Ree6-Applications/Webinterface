package de.presti.ree6.backend.controller;


import de.presti.ree6.backend.bot.BotWorker;
import de.presti.ree6.backend.repository.AutoRoleRepository;
import de.presti.ree6.backend.repository.BlacklistRepository;
import de.presti.ree6.backend.repository.ChatLevelRepository;
import de.presti.ree6.backend.repository.VoiceLevelRepository;
import de.presti.ree6.backend.service.SessionService;
import de.presti.ree6.backend.utils.data.GenericObjectResponse;
import de.presti.ree6.backend.utils.data.GenericResponse;
import de.presti.ree6.backend.utils.data.container.*;
import de.presti.ree6.sql.entities.Blacklist;
import de.presti.ree6.sql.entities.roles.AutoRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

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
    public GenericObjectResponse<List<GuildContainer>> retrieveGuilds(@RequestHeader(name = "X-Session-Authenticator") String sessionIdentifier) {
        try {
            return new GenericObjectResponse<>(true, sessionService.retrieveGuilds(sessionIdentifier), "Guilds retrieved!");
        } catch (Exception e) {
            return new GenericObjectResponse<>(false, null, e.getMessage());
        }
    }

    @CrossOrigin
    @GetMapping(value = "/{guildId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public GenericObjectResponse<GuildContainer> retrieveGuild(@RequestHeader(name = "X-Session-Authenticator") String sessionIdentifier,
                                       @PathVariable(name = "guildId") String guildId) {
        try {
            return new GenericObjectResponse<>(true, sessionService.retrieveGuild(sessionIdentifier, guildId, true, true), "Guild retrieved!");
        } catch (Exception e) {
            return new GenericObjectResponse<>(false, null, e.getMessage());
        }
    }

    //endregion

    //region Guild Channel and Role

    @CrossOrigin
    @GetMapping(value = "/{guildId}/channels", produces = MediaType.APPLICATION_JSON_VALUE)
    public GenericObjectResponse<List<ChannelContainer>> retrieveGuildChannels(@RequestHeader(name = "X-Session-Authenticator") String sessionIdentifier,
                                                      @PathVariable(name = "guildId") String guildId) {
        try {
            return new GenericObjectResponse<>(true, sessionService.retrieveGuild(sessionIdentifier, guildId, true).getChannels(), "Channels retrieved!");
        } catch (Exception e) {
            return new GenericObjectResponse<>(false, null, e.getMessage());
        }
    }

    @CrossOrigin
    @GetMapping(value = "/{guildId}/roles", produces = MediaType.APPLICATION_JSON_VALUE)
    public GenericObjectResponse<List<RoleContainer>> retrieveGuildRoles(@RequestHeader(name = "X-Session-Authenticator") String sessionIdentifier,
                                                @PathVariable(name = "guildId") String guildId) {
        try {
            return new GenericObjectResponse<>(true, sessionService.retrieveGuild(sessionIdentifier, guildId, false, true).getRoles(), "Roles retrieved!");
        } catch (Exception e) {
            return new GenericObjectResponse<>(false, null, e.getMessage());
        }
    }

    //endregion

    //region Guild Blacklist

    @CrossOrigin
    @GetMapping(value = "/{guildId}/blacklist", produces = MediaType.APPLICATION_JSON_VALUE)
    public GenericObjectResponse<List<String>> retrieveGuildBlacklist(@RequestHeader(name = "X-Session-Authenticator") String sessionIdentifier,
                                                  @PathVariable(name = "guildId") String guildId) {
        try {
            GuildContainer guildContainer = sessionService.retrieveGuild(sessionIdentifier, guildId);
            List<String> blacklist = blacklistRepository.getBlacklistByGuildId(guildId).stream().map(Blacklist::getWord).toList();
            return new GenericObjectResponse<>(true, blacklist, "Blacklist retrieved!");
        } catch (Exception e) {
            return new GenericObjectResponse<>(false, Collections.emptyList(), e.getMessage());
        }
    }

    @CrossOrigin
    @GetMapping(value = "/{guildId}/blacklist/remove", produces = MediaType.APPLICATION_JSON_VALUE)
    public GenericResponse removeGuildBlacklist(@RequestHeader(name = "X-Session-Authenticator") String sessionIdentifier,
                                                @PathVariable(name = "guildId") String guildId,
                                                @RequestBody String word) {
        try {
            GuildContainer guildContainer = sessionService.retrieveGuild(sessionIdentifier, guildId);

            Blacklist blacklist = blacklistRepository.getBlacklistByGuildIdAndWord(guildId, word);
            blacklistRepository.delete(blacklist);

            return new GenericResponse(true,"Blacklist removed!");
        } catch (Exception e) {
            return new GenericResponse(false, e.getMessage());
        }
    }

    @CrossOrigin
    @GetMapping(value = "/{guildId}/blacklist/add", produces = MediaType.APPLICATION_JSON_VALUE)
    public GenericResponse addGuildBlacklist(@RequestHeader(name = "X-Session-Authenticator") String sessionIdentifier,
                                             @PathVariable(name = "guildId") String guildId,
                                             @RequestBody String word) {
        try {
            GuildContainer guildContainer = sessionService.retrieveGuild(sessionIdentifier, guildId);

            Blacklist blacklist = blacklistRepository.getBlacklistByGuildIdAndWord(guildId, word);
            if (blacklist == null) {
                blacklist = new Blacklist(guildId, word);
                blacklistRepository.save(blacklist);
                return new GenericResponse(true,"Blacklist added!");
            } else {
                return new GenericResponse(false,"Word already blacklisted!");
            }
        } catch (Exception e) {
            return new GenericResponse(false, e.getMessage());
        }
    }

    //endregion

    //region Guild AutoRole

    @CrossOrigin
    @GetMapping(value = "/{guildId}/autorole", produces = MediaType.APPLICATION_JSON_VALUE)
    public GenericObjectResponse<List<RoleContainer>> retrieveGuildAutoRole(@RequestHeader(name = "X-Session-Authenticator") String sessionIdentifier,
                                                   @PathVariable(name = "guildId") String guildId) {
        try {
            GuildContainer guildContainer = sessionService.retrieveGuild(sessionIdentifier, guildId);
            List<RoleContainer> autoRoles = autoRoleRepository.getAutoRoleByGuildId(guildId).stream().map(c -> guildContainer.getRoleById(c.getRoleId())).filter(Objects::nonNull).toList();
            return new GenericObjectResponse<>(true, autoRoles, "AutoRole retrieved!");
        } catch (Exception e) {
            return new GenericObjectResponse<>(false, Collections.emptyList(), e.getMessage());
        }
    }

    @CrossOrigin
    @GetMapping(value = "/{guildId}/autorole/remove", produces = MediaType.APPLICATION_JSON_VALUE)
    public GenericResponse removeGuildAutoRole(@RequestHeader(name = "X-Session-Authenticator") String sessionIdentifier,
                                                 @PathVariable(name = "guildId") String guildId,
                                                 @RequestBody String roleId) {
        try {
            GuildContainer guildContainer = sessionService.retrieveGuild(sessionIdentifier, guildId);
            AutoRole autoRole = autoRoleRepository.getAutoRoleByGuildIdAndRoleId(guildId, roleId);
            autoRoleRepository.delete(autoRole);
            return new GenericResponse(true,"AutoRole removed!");
        } catch (Exception e) {
            return new GenericResponse(false, e.getMessage());
        }
    }

    @CrossOrigin
    @GetMapping(value = "/{guildId}/autorole/add", produces = MediaType.APPLICATION_JSON_VALUE)
    public GenericResponse addGuildAutoRole(@RequestHeader(name = "X-Session-Authenticator") String sessionIdentifier,
                                              @PathVariable(name = "guildId") String guildId,
                                              @RequestBody String roleId) {
        try {
            GuildContainer guildContainer = sessionService.retrieveGuild(sessionIdentifier, guildId);
            AutoRole autoRole = autoRoleRepository.getAutoRoleByGuildIdAndRoleId(guildId, roleId);
            if (autoRole == null) {
                autoRole = new AutoRole(guildId, roleId);
                autoRoleRepository.save(autoRole);
                return new GenericResponse(true,"AutoRole added!");
            } else {
                return new GenericResponse(false,"Role is already in AutoRole!");
            }
        } catch (Exception e) {
            return new GenericResponse(false, e.getMessage());
        }
    }

    //endregion

    //region Guild Leaderboard

    @CrossOrigin
    @GetMapping(value = "/{guildId}/leaderboard", produces = MediaType.APPLICATION_JSON_VALUE)
    public GenericObjectResponse<LeaderboardContainer> retrieveSetting(@PathVariable(name = "guildId") String guildId) {
        try {
            // Call this to check if guild exists. If not exception is thrown.
            GuildContainer guildContainer = sessionService.retrieveGuild(guildId);

            LeaderboardContainer leaderboardContainer = new LeaderboardContainer();

            leaderboardContainer.setChatLeaderboard(chatLevelRepository.getFirst5getChatUserLevelsByGuildIdOrderByExperienceDesc(guildId).stream()
                    .map(c -> new UserLevelContainer(c, new UserContainer(BotWorker.getShardManager().retrieveUserById(c.getUserId()).complete()))).toList());

            leaderboardContainer.setVoiceLeaderboard(voiceLevelRepository.getFirst5VoiceLevelsByGuildIdOrderByExperienceDesc(guildId).stream()
                    .map(c -> new UserLevelContainer(c, new UserContainer(BotWorker.getShardManager().retrieveUserById(c.getUserId()).complete()))).toList());

            leaderboardContainer.setGuildId(guildContainer.getId());

            return new GenericObjectResponse<>(true, leaderboardContainer, "Leaderboard retrieved!");
        } catch (Exception e) {
            return new GenericObjectResponse<>(false, null, e.getMessage());
        }
    }

    //endregion
}
