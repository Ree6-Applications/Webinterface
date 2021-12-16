package de.presti.ree6.webinterface.controller.forms;

/**
 * Class called to give info about the Channel change to the FrontendController.
 */
public class ChannelChangeForm {

    // information needed to Validate a Channel Change in the Database.
    private final String type, channel, identifierSession, guildSession;

    /**
     * Constructor to get the needed data.
     * @param type the Channel Typ.
     * @param channel the ID of the Channel.
     * @param identifierSession the Identifier of the Session.
     * @param guildSession the ID of the Guild.
     */
    public ChannelChangeForm(String type, String channel, String identifierSession, String guildSession) {
        this.type = type;
        this.channel = channel;
        this.identifierSession = identifierSession;
        this.guildSession = guildSession;
    }

    /**
     * Get the Channel Type.
     * @return {@link String} as Type.
     */
    public String getType() {
        return type;
    }

    /**
     * Get the Channel ID.
     * @return {@link String} as ID.
     */
    public String getChannel() {
        return channel;
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
