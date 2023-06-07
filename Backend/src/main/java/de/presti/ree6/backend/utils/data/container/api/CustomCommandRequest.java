package de.presti.ree6.backend.utils.data.container.api;

/**
 * Request for the Custom Command.
 * @param name Name of the Command.
 * @param message Message of the Command.
 * @param embedJson Embed of the Command.
 * @param channelId Channel ID of the Command.
 */
public record CustomCommandRequest(String name, String message, String embedJson, String channelId) {
}
