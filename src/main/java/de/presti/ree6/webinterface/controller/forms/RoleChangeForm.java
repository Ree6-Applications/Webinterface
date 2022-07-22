package de.presti.ree6.webinterface.controller.forms;

/**
 * Class called to give info about the Role change to the FrontendController.
 */
public class RoleChangeForm {

    // information needed to Validate a Role Change in the Database.
    private final String type,
            role,
            identifierSession,
            guildSession;

    /**
     * Constructor to get the needed data.
     * @param type the Role Typ.
     * @param role the ID of the Role.
     * @param identifierSession the Identifier of the Session.
     * @param guildSession the ID of the Guild.
     */
    public RoleChangeForm(String type, String role, String identifierSession, String guildSession) {
        this.type = type;
        this.role = role;
        this.identifierSession = identifierSession;
        this.guildSession = guildSession;
    }

    /**
     * Get the Role Type.
     * @return {@link String} as Type.
     */
    public String getType() {
        return type;
    }

    /**
     * Get the Role ID.
     * @return {@link String} as ID.
     */
    public String getRole() {
        return role;
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
