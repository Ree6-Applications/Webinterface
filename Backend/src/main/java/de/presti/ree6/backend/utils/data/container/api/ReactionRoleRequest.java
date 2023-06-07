package de.presti.ree6.backend.utils.data.container.api;

/**
 * Request for Reaction Role Creation.
 * @param emojiId Emoji ID of the Reaction Role.
 * @param formattedEmoji Emoji formation of the Reaction Role.
 * @param channelId Channel ID of the Reaction Role.
 * @param messageId Message ID of the Reaction Role.
 * @param roleId Role ID of the Reaction Role.
 */
public record ReactionRoleRequest(String emojiId, String formattedEmoji, String channelId, String messageId, String roleId) {
}
