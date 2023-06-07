package de.presti.ree6.backend.utils.data.container.api;

/**
 * Request for the Tickets.
 * @param channelId Channel ID of the Tickets.
 * @param logChannelId Channel ID of the Tickets Log.
 */
public record TicketsRequest(String channelId, String logChannelId) {
}
