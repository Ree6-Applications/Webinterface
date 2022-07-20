package de.presti.ree6.webinterface.sql.entities.webhook;

import de.presti.ree6.webinterface.sql.base.annotations.Table;

/**
 * SQL Entity for the Welcome-Webhooks.
 */
@Table(name = "WelcomeWebhooks")
public class WebhookWelcome extends Webhook {

    /**
     * Constructor.
     */
    public WebhookWelcome() {
    }

    /**
     * @inheritDoc
     */
    public WebhookWelcome(String guildId, String channelId, String token) {
        super(guildId, channelId, token);
    }
}
