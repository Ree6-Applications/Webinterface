package de.presti.ree6.backend.utils.data.container.api;

public record ReactionRoleRequest(String emojiId, String formattedEmoji, String channelId, String messageId, String roleId) {
}
