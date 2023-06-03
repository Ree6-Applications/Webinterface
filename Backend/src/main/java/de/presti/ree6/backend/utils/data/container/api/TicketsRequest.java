package de.presti.ree6.backend.utils.data.container.api;

public record TicketsRequest(String channelId, String logChannelId, String ticketMessageMenu, String ticketMessageOpen) {
}
