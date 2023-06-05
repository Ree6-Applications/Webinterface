package de.presti.ree6.backend.utils.data.container.api;

public record CustomCommandRequest(String name, String message, String embedJson, String channelId) {
}
