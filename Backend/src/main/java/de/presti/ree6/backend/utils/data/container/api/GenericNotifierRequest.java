package de.presti.ree6.backend.utils.data.container.api;

/**
 * Request for the Generic Notifier.
 * @param channelId Channel ID of the Notifier.
 * @param message Message of the Notifier.
 * @param name Name of the Notifier.
 */
public record GenericNotifierRequest(String channelId, String message, String name) {
}
