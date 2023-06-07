package de.presti.ree6.backend.utils.data.container.api;

/**
 * Request for the Reaction Role Delete.
 * @param messageId Message ID of the Reaction Role.
 * @param emojiId Emoji ID of the Reaction Role.
 */
public record ReactionRoleDeleteRequest(String messageId, String emojiId) {
}
