package de.presti.ree6.webinterface.sql.entities.webhook;

import de.presti.ree6.webinterface.sql.base.annotations.Property;
import de.presti.ree6.webinterface.sql.base.annotations.Table;

/**
 * SQL Entity for the YouTube-Webhooks.
 */
@Table(name = "YouTubeNotify")
public class WebhookYouTube extends Webhook {

    /**
     * Name of the Channel.
     */
    @Property(name = "name")
    private String name;

    /**
     * Constructor.
     */
    public WebhookYouTube() {
    }


    /**
     * Constructor.
     *
     * @param guildId   The guild ID.
     * @param name      The name of the Channel.
     * @param channelId The channel ID.
     * @param token     The token.
     */
    public WebhookYouTube(String guildId, String name, String channelId, String token) {
        super(guildId, channelId, token);
        this.name = name;
    }

    /**
     * Get the name of the Channel.
     * @return the channel name.
     */
    public String getName() {
        return name;
    }
}
