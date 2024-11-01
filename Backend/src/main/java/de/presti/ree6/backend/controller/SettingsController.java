package de.presti.ree6.backend.controller;

import de.presti.ree6.backend.service.SessionService;
import de.presti.ree6.backend.utils.data.container.api.GenericObjectResponse;
import de.presti.ree6.backend.utils.data.container.api.GenericResponse;
import de.presti.ree6.backend.utils.data.container.api.GenericValueRequest;
import de.presti.ree6.backend.utils.data.container.guild.GuildContainer;
import de.presti.ree6.sql.SQLSession;
import de.presti.ree6.sql.entities.Setting;
import de.presti.ree6.sql.util.SettingsManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.Collections;
import java.util.List;

/**
 * Controller meant to handle Settings.
 */
@RestController
@RequestMapping("/settings/{guildId}")
public class SettingsController {

    /**
     * Session Service to handle Sessions.
     */
    private final SessionService sessionService;

    /**
     * Controller for the Settings Controller.
     * @param sessionService Session Service to handle Sessions.
     */
    @Autowired
    public SettingsController(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    //region Settings Retrieve

    /**
     * Retrieve all Settings for a Guild.
     * @param sessionIdentifier Session Identifier to identify the Session.
     * @param guildId Guild ID to identify the Guild.
     * @return Generic Object Response with the Settings.
     */
    @GetMapping(value = "/", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<GenericObjectResponse<List<Setting>>> retrieveSettings(@RequestHeader(name = "X-Session-Authenticator") String sessionIdentifier,
                                                                       @PathVariable(name = "guildId") long guildId) {
        return sessionService.retrieveGuild(sessionIdentifier, guildId).publishOn(Schedulers.boundedElastic()).mapNotNull(guildOptional -> {
           if (guildOptional.isEmpty()) {
               return new GenericObjectResponse<>(false, Collections.emptyList(), "Guild doesn't exist.");
           }

           return SQLSession.getSqlConnector().getSqlWorker().getAllSettings(guildId)
                   .map(settings -> {
                       if (settings.isEmpty()) {
                           return new GenericObjectResponse<>(false,
                                   SettingsManager.getSettings().stream()
                                           .map(x -> new Setting(guildId, x.getName(), x.getDisplayName(), x.getValue())).toList(),
                                   null);
                       }

                       return new GenericObjectResponse<>(true, settings, null);
                   }).block();
        });
    }

    /**
     * Retrieve a Setting for a Guild.
     * @param sessionIdentifier Session Identifier to identify the Session.
     * @param guildId Guild ID to identify the Guild.
     * @param settingName Setting Name to identify the Setting.
     * @return Generic Object Response with the Setting.
     */
    @GetMapping(value = "/{settingName}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<GenericObjectResponse<Setting>> retrieveSetting(@RequestHeader(name = "X-Session-Authenticator") String sessionIdentifier,
                                           @PathVariable(name = "guildId") long guildId,
                                           @PathVariable(name = "settingName") String settingName) {
        return sessionService.retrieveGuild(sessionIdentifier, guildId).publishOn(Schedulers.boundedElastic()).mapNotNull(guildOptional -> {
            if (guildOptional.isEmpty()) {
                return new GenericObjectResponse<>(false, null, "Guild doesn't exist.");
            }

            return SQLSession.getSqlConnector().getSqlWorker().getSetting(guildId, settingName)
                    .map(setting -> setting.map(x -> new GenericObjectResponse<>(true, x, null))
                            .orElseGet(() -> new GenericObjectResponse<>(false, null, "Setting not found!"))).block();
        });
    }

    //endregion

    //region Settings Update

    /**
     * Update a Setting for a Guild.
     * @param sessionIdentifier Session Identifier to identify the Session.
     * @param guildId Guild ID to identify the Guild.
     * @param settingName Setting Name to identify the Setting.
     * @param request Generic Value Request with the new Value.
     * @return Generic Object Response with the updated Setting.
     */
    @PostMapping(value = "/{settingName}/update", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<GenericObjectResponse<Setting>> updateSetting(@RequestHeader(name = "X-Session-Authenticator") String sessionIdentifier,
                                         @PathVariable(name = "guildId") long guildId,
                                         @PathVariable(name = "settingName") String settingName,
                                         @RequestBody GenericValueRequest request) {
        return sessionService.retrieveGuild(sessionIdentifier, guildId).publishOn(Schedulers.boundedElastic()).mapNotNull(guildOptional -> {
            if (guildOptional.isEmpty()) {
                return new GenericObjectResponse<>(false, null, "Guild doesn't exist.");
            }

            return SQLSession.getSqlConnector().getSqlWorker().getSetting(guildId, settingName)
                    .publishOn(Schedulers.boundedElastic())
                    .mapNotNull(settingOptional -> {
                        if (settingOptional.isEmpty()) {
                            return new GenericObjectResponse<Setting>(false, null, "Setting doesn't exist.");
                        }

                        Setting setting = settingOptional.get();
                        setting.setValue(request.value());
                        return SQLSession.getSqlConnector().getSqlWorker().updateEntity(setting).map(x -> new GenericObjectResponse<>(true, x, "Setting updated!")).block();
                    }).block();
        });
    }

    //endregion

    //region Setting Delete

    /**
     * Delete a Setting for a Guild.
     * @param sessionIdentifier Session Identifier to identify the Session.
     * @param guildId Guild ID to identify the Guild.
     * @param settingName Setting Name to identify the Setting.
     * @return Generic Response with the result.
     */
    @GetMapping(value = "/{settingName}/delete", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<GenericResponse> deleteSetting(@RequestHeader(name = "X-Session-Authenticator") String sessionIdentifier,
                                         @PathVariable(name = "guildId") long guildId,
                                         @PathVariable(name = "settingName") String settingName) {
        return sessionService.retrieveGuild(sessionIdentifier, guildId).publishOn(Schedulers.boundedElastic()).mapNotNull(guildOptional -> {
            if (guildOptional.isEmpty()) {
                return new GenericResponse(false, "Guild doesn't exist.");
            }

            Setting setting = SettingsManager.getDefault(settingName);
            setting.setGuildId(guildId);

            return SQLSession.getSqlConnector().getSqlWorker().updateEntity(setting).map(x -> new GenericResponse(true, "Setting deleted!")).block();
        });
    }

    //endregion
}
