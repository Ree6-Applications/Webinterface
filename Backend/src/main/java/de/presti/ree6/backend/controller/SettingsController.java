package de.presti.ree6.backend.controller;

import de.presti.ree6.backend.repository.SettingRepository;
import de.presti.ree6.backend.service.SessionService;
import de.presti.ree6.backend.utils.data.container.api.GenericObjectResponse;
import de.presti.ree6.backend.utils.data.container.api.GenericResponse;
import de.presti.ree6.backend.utils.data.container.api.GenericValueRequest;
import de.presti.ree6.backend.utils.data.container.guild.GuildContainer;
import de.presti.ree6.sql.entities.Setting;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@CrossOrigin
@RestController
@RequestMapping("/settings/{guildId}")
public class SettingsController {

    private final SessionService sessionService;
    private final SettingRepository settingRepository;

    @Autowired
    public SettingsController(SessionService sessionService, SettingRepository settingRepository) {
        this.sessionService = sessionService;
        this.settingRepository = settingRepository;
    }

    //region Settings Retrieve

    @CrossOrigin
    @GetMapping(value = "/", produces = MediaType.APPLICATION_JSON_VALUE)
    public GenericObjectResponse<List<Setting>> retrieveSettings(@RequestHeader(name = "X-Session-Authenticator") String sessionIdentifier,
                                                  @PathVariable(name = "guildId") String guildId) {
        try {
            GuildContainer guildContainer = sessionService.retrieveGuild(sessionIdentifier, guildId);
            return new GenericObjectResponse<>(true, settingRepository.getSettingsByGuildId(guildId), "Setting retrieved!");
        } catch (Exception e) {
            return new GenericObjectResponse<>(false, Collections.emptyList(), e.getMessage());
        }
    }

    @CrossOrigin
    @GetMapping(value = "/{settingName}", produces = MediaType.APPLICATION_JSON_VALUE)
    public GenericObjectResponse<Setting> retrieveSetting(@RequestHeader(name = "X-Session-Authenticator") String sessionIdentifier,
                                           @PathVariable(name = "guildId") String guildId,
                                           @PathVariable(name = "settingName") String settingName) {
        try {
            GuildContainer guildContainer = sessionService.retrieveGuild(sessionIdentifier, guildId);
            return new GenericObjectResponse<>(true, settingRepository.getSettingByGuildIdAndName(guildId, settingName), "Setting retrieved!");
        } catch (Exception e) {
            return new GenericObjectResponse<>(false, null, e.getMessage());
        }
    }

    //endregion

    //region Settings Update

    @CrossOrigin
    @PostMapping(value = "/{settingName}/update", produces = MediaType.APPLICATION_JSON_VALUE)
    public GenericObjectResponse<Setting> updateSetting(@RequestHeader(name = "X-Session-Authenticator") String sessionIdentifier,
                                         @PathVariable(name = "guildId") String guildId,
                                         @PathVariable(name = "settingName") String settingName,
                                         @RequestBody GenericValueRequest request) {
        try {
            GuildContainer guildContainer = sessionService.retrieveGuild(sessionIdentifier, guildId);
            Setting setting = settingRepository.getSettingByGuildIdAndName(guildId, settingName);
            setting.setValue(request.value());
            settingRepository.save(setting);
            return new GenericObjectResponse<>(true, setting, "Setting updated!");
        } catch (Exception e) {
            return new GenericObjectResponse<>(false, null, e.getMessage());
        }
    }

    //endregion

    //region Setting Delete

    @CrossOrigin
    @GetMapping(value = "/{settingName}/delete", produces = MediaType.APPLICATION_JSON_VALUE)
    public GenericResponse deleteSetting(@RequestHeader(name = "X-Session-Authenticator") String sessionIdentifier,
                                         @PathVariable(name = "guildId") String guildId,
                                         @PathVariable(name = "settingName") String settingName) {
        try {
            GuildContainer guildContainer = sessionService.retrieveGuild(sessionIdentifier, guildId);
            Setting setting = settingRepository.getSettingByGuildIdAndName(guildId, settingName);
            settingRepository.delete(setting);
            return new GenericResponse(true,"Setting deleted!");
        } catch (Exception e) {
            return new GenericResponse(false, e.getMessage());
        }
    }

    //endregion
}
