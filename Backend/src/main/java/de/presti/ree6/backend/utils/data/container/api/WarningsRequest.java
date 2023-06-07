package de.presti.ree6.backend.utils.data.container.api;

/**
 * Request for the Warnings.
 * @param userId User ID of the Warnings.
 * @param warnings Warnings of the Warnings.
 */
public record WarningsRequest(String userId, String warnings) {
}
