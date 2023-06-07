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
    public GenericObjectResponse<List<Setting>> retrieveSettings(@RequestHeader(name = "X-Session-Authenticator") String sessionIdentifier,
                                                  @PathVariable(name = "guildId") String guildId) {
        try {
            GuildContainer guildContainer = sessionService.retrieveGuild(sessionIdentifier, guildId);
            return new GenericObjectResponse<>(true, SQLSession.getSqlConnector().getSqlWorker().getAllSettings(guildId), "Setting retrieved!");
        } catch (Exception e) {
            return new GenericObjectResponse<>(false, Collections.emptyList(), e.getMessage());
        }
    }

    /**
     * Retrieve a Setting for a Guild.
     * @param sessionIdentifier Session Identifier to identify the Session.
     * @param guildId Guild ID to identify the Guild.
     * @param settingName Setting Name to identify the Setting.
     * @return Generic Object Response with the Setting.
     */
    @GetMapping(value = "/{settingName}", produces = MediaType.APPLICATION_JSON_VALUE)
    public GenericObjectResponse<Setting> retrieveSetting(@RequestHeader(name = "X-Session-Authenticator") String sessionIdentifier,
                                           @PathVariable(name = "guildId") String guildId,
                                           @PathVariable(name = "settingName") String settingName) {
        try {
            GuildContainer guildContainer = sessionService.retrieveGuild(sessionIdentifier, guildId);
            return new GenericObjectResponse<>(true, SQLSession.getSqlConnector().getSqlWorker().getSetting(guildId, settingName), "Setting retrieved!");
        } catch (Exception e) {
            return new GenericObjectResponse<>(false, null, e.getMessage());
        }
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
    public GenericObjectResponse<Setting> updateSetting(@RequestHeader(name = "X-Session-Authenticator") String sessionIdentifier,
                                         @PathVariable(name = "guildId") String guildId,
                                         @PathVariable(name = "settingName") String settingName,
                                         @RequestBody GenericValueRequest request) {
        try {
            GuildContainer guildContainer = sessionService.retrieveGuild(sessionIdentifier, guildId);
            Setting setting = SQLSession.getSqlConnector().getSqlWorker().getSetting(guildId, settingName);
            setting.setValue(request.value());
            return new GenericObjectResponse<>(true, SQLSession.getSqlConnector().getSqlWorker().updateEntity(setting), "Setting updated!");
        } catch (Exception e) {
            return new GenericObjectResponse<>(false, null, e.getMessage());
        }
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
    public GenericResponse deleteSetting(@RequestHeader(name = "X-Session-Authenticator") String sessionIdentifier,
                                         @PathVariable(name = "guildId") String guildId,
                                         @PathVariable(name = "settingName") String settingName) {
        try {
            GuildContainer guildContainer = sessionService.retrieveGuild(sessionIdentifier, guildId);
            Setting setting = SettingsManager.getDefault(settingName);
            setting.setGuildId(guildId);

            SQLSession.getSqlConnector().getSqlWorker().setSetting(setting);
            return new GenericResponse(true,"Setting deleted!");
        } catch (Exception e) {
            return new GenericResponse(false, e.getMessage());
        }
    }

    //endregion
}
