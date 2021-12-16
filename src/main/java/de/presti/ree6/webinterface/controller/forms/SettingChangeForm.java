package de.presti.ree6.webinterface.controller.forms;

import de.presti.ree6.webinterface.utils.Setting;

/**
 * Class called to give info about the Setting change to the FrontendController.
 */
public class SettingChangeForm {

    // information needed to Validate a Setting Change in the Database.
    private final String name, value, identifierSession, guildSession;

    /**
     * Constructor to get the needed data.
     * @param name the Name of the Setting.
     * @param value the Value of the Setting.
     * @param identifierSession the Identifier of the Session.
     * @param guildSession the ID of the Guild.
     */
    public SettingChangeForm(String name, String value, String identifierSession, String guildSession) {
        this.name = name;
        this.value = value;
        this.identifierSession = identifierSession;
        this.guildSession = guildSession;
    }

    /**
     * Get the Setting.
     * @return {@link Setting} as Setting.
     */
    public Setting getSetting() {
        return new Setting(name, value);
    }

    /**
     * Get the Session Identifier.
     * @return {@link String} as Identifier.
     */
    public String getIdentifier() {
        return identifierSession;
    }

    /**
     * Get the ID of the Guild.
     * @return {@link String} as the Guild ID.
     */
    public String getGuild() {
        return guildSession;
    }
}
