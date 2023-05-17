package de.presti.ree6.backend.controller;

import de.presti.ree6.backend.repository.SettingRepository;
import de.presti.ree6.backend.service.SessionService;
import de.presti.ree6.backend.utils.data.container.GuildContainer;
import de.presti.ree6.sql.entities.Setting;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

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
    public SettingListResponse retrieveSettings(@RequestHeader(name = "X-Session-Authenticator") String sessionIdentifier,
                                                @PathVariable(name = "guildId") String guildId) {
        try {
            GuildContainer guildContainer = sessionService.retrieveGuild(sessionIdentifier, guildId);
            return new SettingListResponse(true, settingRepository.getSettingsByGuildId(guildId), "Setting retrieved!");
        } catch (Exception e) {
            return new SettingListResponse(false, Collections.emptyList(), e.getMessage());
        }
    }

    @CrossOrigin
    @GetMapping(value = "/{settingName}", produces = MediaType.APPLICATION_JSON_VALUE)
    public SettingResponse retrieveSetting(@RequestHeader(name = "X-Session-Authenticator") String sessionIdentifier,
                                           @PathVariable(name = "guildId") String guildId,
                                           @PathVariable(name = "settingName") String settingName) {
        try {
            GuildContainer guildContainer = sessionService.retrieveGuild(sessionIdentifier, guildId);
            return new SettingResponse(true, settingRepository.getSettingByGuildIdAndName(guildId, settingName), "Setting retrieved!");
        } catch (Exception e) {
            return new SettingResponse(false, null, e.getMessage());
        }
    }

    //endregion

    //region Settings Update

    @CrossOrigin
    @GetMapping(value = "/{settingName}/update", produces = MediaType.APPLICATION_JSON_VALUE)
    public SettingResponse updateSetting(@RequestHeader(name = "X-Session-Authenticator") String sessionIdentifier,
                                         @PathVariable(name = "guildId") String guildId,
                                         @PathVariable(name = "settingName") String settingName,
                                         @RequestBody String value) {
        try {
            GuildContainer guildContainer = sessionService.retrieveGuild(sessionIdentifier, guildId);
            Setting setting = settingRepository.getSettingByGuildIdAndName(guildId, settingName);
            setting.setValue(value);
            settingRepository.save(setting);
            return new SettingResponse(true, setting, "Setting updated!");
        } catch (Exception e) {
            return new SettingResponse(false, null, e.getMessage());
        }
    }

    //endregion

    //region Setting Delete

    @CrossOrigin
    @GetMapping(value = "/{settingName}/delete", produces = MediaType.APPLICATION_JSON_VALUE)
    public SettingResponse deleteSetting(@RequestHeader(name = "X-Session-Authenticator") String sessionIdentifier,
                                         @PathVariable(name = "guildId") String guildId,
                                         @PathVariable(name = "settingName") String settingName) {
        try {
            GuildContainer guildContainer = sessionService.retrieveGuild(sessionIdentifier, guildId);
            Setting setting = settingRepository.getSettingByGuildIdAndName(guildId, settingName);
            settingRepository.delete(setting);
            return new SettingResponse(true, null, "Setting deleted!");
        } catch (Exception e) {
            return new SettingResponse(false, null, e.getMessage());
        }
    }

    //endregion


    public record SettingResponse(boolean success, Setting setting, String message) {
    }

    public record SettingListResponse(boolean success, List<Setting> settings, String message) {
    }
}
